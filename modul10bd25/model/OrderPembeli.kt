package com.ro2santi.modul10bd25.model

data class OrderPembeli(
    var orderId: String = "",
    val buyerUid: String = "",
    val buyerName: String = "",
    val buyerAddress: String? = null,
    val buyerPhone: String = "",
    val timestamp: Long = 0,
    val totalAmount: Int = 0,

    // <--- FIELD INI WAJIB SAMA DENGAN YANG DITAMBAHKAN DI Order.kt KASIR --->
    val orderItemList: List<OrderItemPembeli> = emptyList(),

    var status: String = "BARU",
    var paymentStatus: String = "BELUM_BAYAR",
    var pickupMethod: String = "DI_KASIR",
    var processedBy: String? = null,

    // *** SOLUSI: Tambahkan nilai default emptyList() ***
    val items: List<OrderItemPembeli> = emptyList()
)