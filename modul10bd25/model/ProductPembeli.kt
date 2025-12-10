package com.ro2santi.modul10bd25.model

import android.os.Parcel
import android.os.Parcelable

data class ProductPembeli(
    var productId: String = "",
    val productName: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "",
    val isActive: Boolean = true
) : Parcelable {
    // Constructor untuk membaca dari Parcel
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte() // Membaca Boolean
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productId)
        parcel.writeString(productName)
        parcel.writeString(description)
        parcel.writeDouble(price)
        parcel.writeString(imageUrl)
        parcel.writeString(category)
        parcel.writeByte(if (isActive) 1 else 0) // Menulis Boolean
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductPembeli> {
        override fun createFromParcel(parcel: Parcel): ProductPembeli {
            return ProductPembeli(parcel)
        }

        override fun newArray(size: Int): Array<ProductPembeli?> {
            return arrayOfNulls(size)
        }
    }
}