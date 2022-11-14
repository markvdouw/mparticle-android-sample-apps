package com.mparticle.example.higgsshopsampleapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mparticle.MParticle
import com.mparticle.commerce.CommerceEvent
import com.mparticle.example.higgsshopsampleapp.repositories.CartRepository
import com.mparticle.example.higgsshopsampleapp.repositories.ProductsRepository
import com.mparticle.example.higgsshopsampleapp.repositories.database.entities.CartItemEntity
import com.mparticle.example.higgsshopsampleapp.repositories.network.models.Product
import com.mparticle.example.higgsshopsampleapp.utils.logFailure
import com.mparticle.example.higgsshopsampleapp.utils.logSuccess
import kotlinx.coroutines.launch


class ProductDetailViewModel(
    private val cartRepository: CartRepository,
    private val productsRepository: ProductsRepository
) : ViewModel() {

    val detailResponseLiveData = MutableLiveData<Product>()

    var quantity: Int = 0
    var color: String? = null
    var size: String? = null

    companion object {
        const val TAG = "ProductDetailViewModel"
    }

    fun getProductById(id: Int) {
        viewModelScope.launch {
            productsRepository.getProductById(id)?.let {
                val product = com.mparticle.commerce.Product.Builder(
                    it.label, it.id.toString(),
                    it.price.toDouble()
                ).unitPrice(it.price.toDouble())
                    .build()

                MParticle.getInstance()?.logEvent(
                    CommerceEvent.Builder(com.mparticle.commerce.Product.DETAIL, product)
                        .build()
                )
                detailResponseLiveData.value = it
            }
        }
    }

    suspend fun addToCart(cartItem: CartItemEntity): Boolean {
        return if (cartRepository.addToCart(cartItem) > 0) {
            val event = CommerceEvent.Builder(
                com.mparticle.commerce.Product.ADD_TO_CART,
                cartItem.toProduct()
            ).build()
            MParticle.getInstance()?.logEvent(event)
            TAG.logSuccess()
            true
        } else {
            TAG.logFailure()
            false
        }
    }

    private fun CartItemEntity.toProduct() = com.mparticle.commerce.Product.Builder(
        label,
        id.toString(),
        price.toDouble()
    )
        .customAttributes(
            mapOf(
                "size" to size,
                "color" to color
            )
        )
        .unitPrice(price.toDouble())
        .quantity(quantity.toDouble())
        .build()

    class Factory(
        private val cartRepository: CartRepository,
        private val productsRepository: ProductsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductDetailViewModel(cartRepository, productsRepository) as T
        }
    }
}

