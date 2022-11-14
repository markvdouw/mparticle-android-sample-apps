package com.mparticle.example.higgsshopsampleapp.repositories

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mparticle.example.higgsshopsampleapp.repositories.network.models.Product
import com.mparticle.example.higgsshopsampleapp.repositories.network.models.Products
import com.mparticle.example.higgsshopsampleapp.utils.getJsonDataFromAsset

class ProductsRepository(private val context: Context) {

    companion object {
        const val TAG = "ProductsRepository"
        private const val PRODUCTS_JSON = "products.json"
    }

    var products: List<Product> = mutableListOf()
        private set

    init {
        context.getJsonDataFromAsset(PRODUCTS_JSON)?.let {
            try {
                products = Gson().fromJson(it, Products::class.java).products
            } catch (e: Exception) {
                e.toString()
            }
        }
    }

    fun getProductById(id: Int): Product? = products.firstOrNull { it.id == id.toString() }


}
