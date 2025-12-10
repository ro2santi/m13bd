package com.ro2santi.modul10bd25.model

import android.os.Parcel
import android.os.Parcelable

data class Buyer(
    val buyerUid: String = "",

    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val email: String = ""

) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(buyerUid)
        parcel.writeString(name)
        parcel.writeString(phone)
        parcel.writeString(address)
        parcel.writeString(email)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Buyer> {
        override fun createFromParcel(parcel: Parcel): Buyer {
            return Buyer(parcel)
        }

        override fun newArray(size: Int): Array<Buyer?> {
            return arrayOfNulls(size)
        }
    }
}