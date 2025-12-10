package com.ro2santi.modul10bd25.model

data class OrderItem(
    // *** Ganti 'name' menjadi 'productName' ***
    val productName: String = "",
    val qty: Int = 0,
    val price: Int = 0
)