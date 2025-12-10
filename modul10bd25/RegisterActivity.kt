// com.ro2santi.modul10bd25/RegisterActivity.kt
package com.ro2santi.modul10bd25

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ro2santi.modul10bd25.model.Buyer

class RegisterActivity : AppCompatActivity() {

    private lateinit var btn_register: Button
    private lateinit var tv_login_link: TextView
    private lateinit var et_email: EditText
    private lateinit var et_password: EditText

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val DEFAULT_ROLE = "Pembeli"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btn_register = findViewById(R.id.btn_register)
        tv_login_link = findViewById(R.id.tv_login_link)
        et_email = findViewById(R.id.et_email)
        et_password = findViewById(R.id.et_password)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        btn_register.setOnClickListener {
            registerUser()
        }

        tv_login_link.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registerUser() {
        val email = et_email.text.toString().trim()
        val password = et_password.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || password.length < 6) {
            Toast.makeText(this, "Email dan Password harus diisi (min 6 karakter).", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        saveUserData(it.uid, email)
                    }
                } else {
                    Log.e("REGISTER", "Gagal Pendaftaran: ${task.exception?.message}")
                    Toast.makeText(this, "Pendaftaran Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserData(uid: String, email: String) {
        val userMap = mapOf(
            "email" to email,
            "role" to DEFAULT_ROLE
        )

        val buyerData = Buyer(
            // PERBAIKAN: Ganti buyerId = uid menjadi uid = uid
            buyerUid = uid,
            name = "Pembeli Baru",
            phone = "",
            address = "",
            email = email
        )

        val usersRef: DatabaseReference = database.getReference("users").child(uid)
        val buyersRef: DatabaseReference = database.getReference("buyers").child(uid)

        usersRef.setValue(userMap)
            .addOnSuccessListener {
                buyersRef.setValue(buyerData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Pendaftaran Pembeli Berhasil! Silakan lengkapi profil Anda.", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e("REGISTER", "Gagal menyimpan entri awal Pembeli: ${e.message}", e)
                        Toast.makeText(this, "Gagal menyimpan data pembeli. Coba lagi.", Toast.LENGTH_LONG).show()
                        // Hapus entri user jika gagal menyimpan buyer
                        usersRef.removeValue()
                        auth.currentUser?.delete()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("REGISTER", "Gagal menyimpan data peran: ${e.message}", e)
                Toast.makeText(this, "Gagal menyimpan data peran: ${e.message}", Toast.LENGTH_LONG).show()
                auth.currentUser?.delete()
            }
    }
}