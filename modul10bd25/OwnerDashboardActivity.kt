package com.ro2santi.modul10bd25

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class OwnerDashboardActivity : AppCompatActivity() {

    private lateinit var btnManageProducts: Button
    private lateinit var btnCheckBuyers: Button
    private lateinit var btnManageCashiers: Button
    private lateinit var btnLogout: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        auth = FirebaseAuth.getInstance()

        btnManageProducts = findViewById(R.id.btn_manage_products)
        btnCheckBuyers = findViewById(R.id.btn_check_buyers)
        btnManageCashiers = findViewById(R.id.btn_manage_cashiers)
        btnLogout = findViewById(R.id.btn_owner_logout)

        btnManageProducts.setOnClickListener {
            Toast.makeText(this, "Menu: Kelola Produk", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ProductManagementActivity::class.java))
        }

        btnCheckBuyers.setOnClickListener {
            Toast.makeText(this, "Menu: Cek Pembeli", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, BuyerManagementActivity::class.java))
        }

        btnManageCashiers.setOnClickListener {
            Toast.makeText(this, "Menu: Kelola Kasir", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, CashierManagementActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Anda telah logout.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}