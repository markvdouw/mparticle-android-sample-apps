package com.mparticle.example.higgsshopsampleapp.viewmodels

import androidx.lifecycle.*
import com.mparticle.MParticle
import com.mparticle.commerce.CommerceEvent
import com.mparticle.commerce.Product
import com.mparticle.example.higgsshopsampleapp.repositories.CartRepository
import com.mparticle.example.higgsshopsampleapp.repositories.database.entities.CartItemEntity
import com.mparticle.example.higgsshopsampleapp.utils.logFailure
import com.mparticle.example.higgsshopsampleapp.utils.logSuccess
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    val cartResponseLiveData = MutableLiveData<List<CartItemEntity>>()

    companion object {
        const val TAG = "CartViewModel"
    }

    fun getCartItems() {
        viewModelScope.launch {
            cartResponseLiveData.value = cartRepository.getCartItems()
        }
    }

    fun getQuantity(items: List<CartItemEntity>): Int = items.sumOf { it.quantity }

    fun getSubtotalPrice(): BigDecimal =
        cartResponseLiveData.value?.sumOf { BigDecimal(it.quantity.toString()) * BigDecimal(it.price) }
            ?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal(0.0)

    fun removeFromCart(entity: CartItemEntity) {
        viewModelScope.launch {
            val rowsAffected = cartRepository.removeFromCart(entity)
            if (rowsAffected > 0) {
                val event =
                    CommerceEvent.Builder(Product.REMOVE_FROM_CART, entity.toProduct()).build()
                MParticle.getInstance()?.logEvent(event)
                TAG.logSuccess()
            } else {
                TAG.logFailure()
            }
            getCartItems()
        }
    }

    private fun CartItemEntity.toProduct(): Product =
        Product.Builder(label, id.toString(), price.toDouble()).customAttributes(
            mapOf(
                "size" to size, "color" to color
            )
        ).quantity(quantity.toDouble()).build()

    class Factory(private val repository: CartRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CartViewModel(repository) as T
        }
    }

}