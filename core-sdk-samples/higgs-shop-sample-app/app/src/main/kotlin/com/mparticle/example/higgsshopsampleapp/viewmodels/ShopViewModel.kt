package com.mparticle.example.higgsshopsampleapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mparticle.example.higgsshopsampleapp.repositories.CartRepository
import com.mparticle.example.higgsshopsampleapp.repositories.ProductsRepository
import com.mparticle.example.higgsshopsampleapp.repositories.network.models.Product
import com.mparticle.example.higgsshopsampleapp.utils.log
import kotlinx.coroutines.launch

class ShopViewModel(
    private val cartRepository: CartRepository,
    private val productsRepository: ProductsRepository
) : ViewModel() {
    companion object {
        const val TAG = "ProductsViewModel"
    }

    val inventoryResponseLiveData = MutableLiveData<List<Product>>()
    val cartTotalSizeResponseLiveData = MutableLiveData<Int>()

    fun loadItems() {
        viewModelScope.launch {
            with(cartRepository.getCartItems().sumOf { it.quantity }) {
                TAG.log("Total Cart Items: $this")
                cartTotalSizeResponseLiveData.value = this
            }
            with(productsRepository.products) {
                TAG.log("Products inventory $this")
                inventoryResponseLiveData.value = this
            }
        }
    }

    class Factory(
        private val cartRepository: CartRepository,
        private val productsRepository: ProductsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ShopViewModel(cartRepository, productsRepository) as T
        }
    }
}