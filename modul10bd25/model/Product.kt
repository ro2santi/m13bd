package com.ro2santi.modul10bd25.model

import android.os.Parcel
import android.os.Parcelable

data class Product(
    // ID unik produk. Diisi dengan key RTDB.
    var productId: String = "",

    // Data Produk
    val productName: String = "",
    val imageUrl: String = "",
    val price: Double = 0.0,
    val ingredients: String = "",
    val packaging: String = "",

    // Status Produk
    val isActive: Boolean = true

) : Parcelable {

    // 1. Konstruktor Sekunder untuk membaca data dari Parcel
    constructor(parcel: Parcel) : this(
        // Urutan pembacaan harus sama dengan urutan penulisan di writeToParcel
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte() // Membaca Boolean (disimpan sebagai Byte)
    )

    // 2. Menulis data objek ke Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        // Urutan penulisan harus konsisten
        parcel.writeString(productId)
        parcel.writeString(productName)
        parcel.writeString(imageUrl)
        parcel.writeDouble(price)
        parcel.writeString(ingredients)
        parcel.writeString(packaging)
        parcel.writeByte(if (isActive) 1 else 0) // Menulis Boolean (1=true, 0=false)
    }

    // 3. Deskripsi Konten (biasanya 0)
    override fun describeContents(): Int {
        return 0
    }

    // 4. Companion Object CREATOR yang diperlukan oleh Android
    companion object CREATOR : Parcelable.Creator<Product> {
        // Membuat objek dari Parcel
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        // Membuat array dari objek Parcelable
        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }
}

