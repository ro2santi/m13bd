package com.ro2santi.modul10bd25

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var btn_to_login: Button
    private lateinit var btn_to_register: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_to_login = findViewById(R.id.btn_to_login)
        btn_to_register = findViewById(R.id.btn_to_register)


        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        btn_to_login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btn_to_register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}