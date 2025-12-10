package com.ro2santi.modul10bd25.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Cashier(
    @get:Exclude
    var uid: String = "", // UID dari Firebase Auth, diisi setelah diambil dari key RTDB

    val email: String = "",
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val isActive: Boolean = true // Status Aktif/Nonaktif

) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", // cashierId
        parcel.readString() ?: "", // email
        parcel.readString() ?: "", // name
        parcel.readString() ?: "", // address
        parcel.readString() ?: "", // phone
        parcel.readString() ?: "", // photoUrl
        parcel.readByte() != 0.toByte() // isActive (Byte ke Boolean)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(email)
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeString(phone)
        parcel.writeString(photoUrl)
        parcel.writeByte(if (isActive) 1 else 0) // Boolean ke Byte
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Cashier> {
        override fun createFromParcel(parcel: Parcel): Cashier {
            return Cashier(parcel)
        }

        override fun newArray(size: Int): Array<Cashier?> {
            return arrayOfNulls(size)
        }
    }
}