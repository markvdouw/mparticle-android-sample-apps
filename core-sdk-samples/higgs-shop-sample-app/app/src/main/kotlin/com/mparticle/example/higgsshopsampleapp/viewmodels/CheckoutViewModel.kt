package com.mparticle.example.higgsshopsampleapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mparticle.MParticle
import com.mparticle.commerce.CommerceEvent
import com.mparticle.commerce.Product
import com.mparticle.commerce.TransactionAttributes
import com.mparticle.example.higgsshopsampleapp.repositories.CartRepository
import com.mparticle.example.higgsshopsampleapp.repositories.database.entities.CartItemEntity
import com.mparticle.example.higgsshopsampleapp.utils.Constants
import com.mparticle.example.higgsshopsampleapp.utils.log
import com.mparticle.example.higgsshopsampleapp.utils.logFailure
import com.mparticle.example.higgsshopsampleapp.utils.logSuccess
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class CheckoutViewModel(private val cartRepository: CartRepository) : ViewModel() {

    val cartResponseLiveData = MutableLiveData<List<CartItemEntity>>()
    val checkoutTotals = MutableLiveData<Map<String, Any>>()

    companion object {
        const val TAG = "PaymentViewModel"
    }

    fun getCartItems() {
        viewModelScope.launch {
            cartResponseLiveData.value = cartRepository.getCartItems()

            with(cartRepository.getCartItems()) {
                TAG.log("Cart size: ${this.size}")
                val pricesMap = calculateCheckoutTotals(this)
                createCommerceEventConversion(this, pricesMap, Product.CHECKOUT)?.let {
                    MParticle.getInstance()?.logEvent(it)
                }
                cartResponseLiveData.value = this
            }
        }
    }

    private fun createCommerceEventConversion(
        items: List<CartItemEntity>,
        priceMap: Map<String, Any>,
        productAction: String
    ): CommerceEvent? {
        val builder = if (items.isNotEmpty()) {
            with(items.first()) {
                val event = CommerceEvent.Builder(productAction, createProduct())
                if (items.size > 1) {
                    for (i in 1 until items.size) {
                        event.addProduct(items[i].createProduct())
                    }
                }
                event
            }
        } else null

        builder?.let {
            if (productAction == Product.PURCHASE) {
                val attributes: TransactionAttributes =
                    TransactionAttributes(Calendar.getInstance().time.toString())
                        .setRevenue(priceMap["grandTotal"].toString().toDouble())
                        .setTax(priceMap["salesTax"].toString().toDouble())
                        .setShipping(BigDecimal(Constants.CHECKOUT_SHIPPING_COST).toDouble())
                it.transactionAttributes(attributes)
            }
        }

        return builder?.build()
    }

    private fun CartItemEntity.createProduct(): Product =
        Product.Builder(label, id.toString(), price.toDouble())
            .customAttributes(
                mapOf(
                    "size" to size,
                    "color" to color
                )
            )
            .quantity(quantity.toDouble())
            .build()

    private fun calculateCheckoutTotals(items: List<CartItemEntity>): Map<String, Any> {
        val subTotal = (items.sumOf { BigDecimal(it.quantity.toString()) * BigDecimal(it.price) })
            .setScale(2, RoundingMode.HALF_UP)
        val salesTax =
            (subTotal * BigDecimal(Constants.CHECKOUT_SALES_TAX).divide("100.0".toBigDecimal()))
                .setScale(2, RoundingMode.HALF_UP)
        val shipping =
            (subTotal * BigDecimal(Constants.CHECKOUT_SHIPPING_COST).divide("100.0".toBigDecimal()))
                .setScale(2, RoundingMode.HALF_UP)
        val grandTotal = (subTotal + salesTax + shipping).setScale(2, RoundingMode.HALF_UP)
        val checkoutPrices = mapOf(
            "subTotal" to subTotal,
            "salesTax" to salesTax,
            "shipping" to shipping,
            "grandTotal" to grandTotal
        )
        checkoutTotals.value = checkoutPrices
        return checkoutPrices
    }

    suspend fun clearCart(): Boolean {
        val rowsAffected = cartRepository.clearCart()
        return if (rowsAffected > 0) {
            TAG.logSuccess()
            true
        } else {
            TAG.logFailure()
            false
        }
    }

    class Factory(private val repository: CartRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CheckoutViewModel(repository) as T
        }
    }
}