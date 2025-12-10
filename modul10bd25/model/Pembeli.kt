package com.ro2santi.modul10bd25.model

import android.os.Parcel
import android.os.Parcelable

data class Pembeli(
    var uid: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = ""
) : Parcelable {
    // Constructor untuk membaca dari Parcel
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(email)
        parcel.writeString(name)
        parcel.writeString(phone)
        parcel.writeString(address)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Pembeli> {
        override fun createFromParcel(parcel: Parcel): Pembeli {
            return Pembeli(parcel)
        }

        override fun newArray(size: Int): Array<Pembeli?> {
            return arrayOfNulls(size)
        }
    }
}