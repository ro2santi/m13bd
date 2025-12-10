package com.ro2santi.modul10bd25.model

import android.os.Parcel
import android.os.Parcelable

data class OrderItemPembeli(
    val productName: String = "",
    val qty: Int = 0,
    val price: Int = 0
) : Parcelable {
    // Constructor untuk membaca dari Parcel
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productName)
        parcel.writeInt(qty)
        parcel.writeInt(price)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrderItemPembeli> {
        override fun createFromParcel(parcel: Parcel): OrderItemPembeli {
            return OrderItemPembeli(parcel)
        }

        override fun newArray(size: Int): Array<OrderItemPembeli?> {
            return arrayOfNulls(size)
        }
    }
}