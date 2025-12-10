package com.ro2santi.modul10bd25.model

data class Order(
    var orderId: String = "",
    val buyerUid: String = "",
    val buyerName: String = "",
    val buyerAddress: String? = null,
    val buyerPhone: String = "",
    val timestamp: Long = 0,
    val totalAmount: Int = 0,

    // Properti lama (Map) - Digunakan saat Kasir membuat pesanan di tempat
    val orderItems: Map<String, OrderItem> = emptyMap(),

    // *** PERBAIKAN: Ganti 'orderItemList' menjadi 'items' agar sesuai dengan OrderPembeli.kt ***
    val items: List<OrderItem> = emptyList(),

    // Status Pesanan 
    var status: String = "BARU",
    var paymentStatus: String = "BELUM_BAYAR",
    var pickupMethod: String = "DI_KASIR",
    var processedBy: String? = null
)