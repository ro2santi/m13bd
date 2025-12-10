package com.ro2santi.modul10bd25

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var btn_login: Button
    private lateinit var tv_register_link: TextView
    private lateinit var et_email: EditText
    private lateinit var et_password: EditText

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login = findViewById(R.id.btn_login)
        tv_register_link = findViewById(R.id.tv_register_link)
        et_email = findViewById(R.id.et_email)
        et_password = findViewById(R.id.et_password)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        if (auth.currentUser != null) {
            checkUserRole(auth.currentUser!!.uid)
        }

        btn_login.setOnClickListener {
            loginUser()
        }

        tv_register_link.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = et_email.text.toString().trim()
        val password = et_password.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password harus diisi.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser!!.uid
                    // 2. Cek Peran Pengguna dari RTDB
                    checkUserRole(uid)
                } else {
                    Toast.makeText(this, "Login Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkUserRole(uid: String) {
        database.getReference("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val role = snapshot.child("role").getValue(String::class.java)

                    if (role != null) {
                        Toast.makeText(this@LoginActivity, "Selamat datang sebagai $role!", Toast.LENGTH_SHORT).show()
                        navigateToDashboard(role)
                    } else {
                        Toast.makeText(this@LoginActivity, "Peran tidak ditemukan.", Toast.LENGTH_LONG).show()
                        auth.signOut()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Data pengguna tidak ditemukan di RTDB.", Toast.LENGTH_LONG).show()
                    auth.signOut()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginActivity, "Gagal membaca data dari RTDB: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun navigateToDashboard(role: String) {
        val intent: Intent = when (role) {
            "Owner" -> Intent(this, OwnerDashboardActivity::class.java)
            "Kasir" -> Intent(this, KasirDashboardActivity::class.java)
            "Pembeli" -> Intent(this, PembeliDashboardActivity::class.java)
            else -> {
                Toast.makeText(this, "Peran tidak valid.", Toast.LENGTH_SHORT).show()
                return
            }
        }
        startActivity(intent)
        finish()
    }
}