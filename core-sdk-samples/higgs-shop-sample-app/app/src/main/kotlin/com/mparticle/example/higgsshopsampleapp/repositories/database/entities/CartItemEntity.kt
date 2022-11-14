package com.mparticle.example.higgsshopsampleapp.repositories.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mparticle.commerce.Product

@Entity(tableName = "CartItems")
data class CartItemEntity(
    @PrimaryKey val sku: String,
    val id: Int,
    val label: String,
    val imageUrl: String?,
    val color: String?,
    val size: String?,
    val price: String,
    var quantity: Int
)