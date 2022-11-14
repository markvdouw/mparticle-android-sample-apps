package com.mparticle.example.higgsshopsampleapp.repositories.network.models

data class Product(
    val id: String,
    val label: String,
    val imageUrl: String?,
    val price: String,
    val variants: Variants?,
    val description: String
)