package com.mparticle.example.higgsshopsampleapp.repositories

import android.content.Context
import com.mparticle.example.higgsshopsampleapp.repositories.database.MpDatabase
import com.mparticle.example.higgsshopsampleapp.repositories.database.entities.CartItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CartRepository(context: Context) {

    private val dao = MpDatabase.getDatabase(context).mpDao()

    companion object {
        private const val TAG = "CartRepository"
    }

    suspend fun getCartItems(): List<CartItemEntity> =
        withContext(Dispatchers.IO) { dao.getAllCartItems() }

    suspend fun addToCart(entity: CartItemEntity) = withContext(Dispatchers.IO) {
        val item = dao.getCartItemByKey("${entity.id}-${entity.color}-${entity.size}")
        item?.let { entity.quantity += it.quantity }
        dao.addToCart(entity)
    }

    suspend fun removeFromCart(entity: CartItemEntity) =
        withContext(Dispatchers.IO) { dao.removeFromCart(entity) }

    suspend fun clearCart() = withContext(Dispatchers.IO) { dao.clearCart() }
}