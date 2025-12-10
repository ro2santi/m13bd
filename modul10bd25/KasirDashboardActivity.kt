package com.ro2santi.modul10bd25

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ro2santi.modul10bd25.adapter.OrderAdapter
import com.ro2santi.modul10bd25.adapter.CashierProductAdapter
import com.ro2santi.modul10bd25.model.Cashier
import com.ro2santi.modul10bd25.model.Order
import com.ro2santi.modul10bd25.model.OrderItem
import com.ro2santi.modul10bd25.model.Product
import java.text.NumberFormat
import java.util.Locale

class KasirDashboardActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    private lateinit var bottomNav: BottomNavigationView

    private lateinit var svProfile: ScrollView
    private lateinit var etProfileName: EditText
    private lateinit var etProfilePhone: EditText
    private lateinit var etProfileAddress: EditText
    private lateinit var tvProfileEmail: TextView
    private lateinit var btnSaveProfile: Button

    private lateinit var rvActiveMenu: RecyclerView
    private lateinit var rvOrders: RecyclerView

    private lateinit var tvCartTotal: TextView
    private lateinit var btnCheckout: Button
    private lateinit var menuContainer: LinearLayout

    private val cartItems = mutableMapOf<String, OrderItem>()
    private lateinit var productAdapter: CashierProductAdapter
    private val productList = mutableListOf<Product>()
    private lateinit var orderAdapter: OrderAdapter
    private val orderList = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kasir_dashboard)

        if (currentUid == null) {
            Toast.makeText(this, "Sesi habis. Silakan Login ulang.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        svProfile = findViewById(R.id.sv_profile)
        rvActiveMenu = findViewById(R.id.rv_active_menu)
        rvOrders = findViewById(R.id.rv_orders)
        bottomNav = findViewById(R.id.bottom_nav)

        tvCartTotal = findViewById(R.id.tv_cart_total)
        btnCheckout = findViewById(R.id.btn_checkout)
        menuContainer = findViewById(R.id.menu_container)

        btnCheckout.setOnClickListener { createOrder() }

        orderAdapter = OrderAdapter(this, orderList)
        productAdapter = CashierProductAdapter(productList) { product ->
            addToCart(product)
        }

        initProfileViews()
        initRecyclerViews()
        updateCartDisplay()

        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected)
        bottomNav.selectedItemId = R.id.nav_menu
    }

    private fun initProfileViews() {
        etProfileName = findViewById(R.id.et_profile_name)
        etProfilePhone = findViewById(R.id.et_profile_phone)
        etProfileAddress = findViewById(R.id.et_profile_address)
        tvProfileEmail = findViewById(R.id.tv_profile_email)
        btnSaveProfile = findViewById(R.id.btn_save_profile)

        btnSaveProfile.setOnClickListener { saveCashierProfile() }
    }

    private fun initRecyclerViews() {
        rvActiveMenu.layoutManager = LinearLayoutManager(this)
        rvActiveMenu.adapter = productAdapter

        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = orderAdapter
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        menuContainer.visibility = View.GONE
        rvOrders.visibility = View.GONE
        svProfile.visibility = View.GONE

        when (item.itemId) {
            R.id.nav_menu -> {
                supportActionBar?.title = "Proses Order (Menu Aktif)"
                menuContainer.visibility = View.VISIBLE // Tampilkan wadah menu/cart
                loadActiveMenu()
            }
            R.id.nav_orders -> {
                supportActionBar?.title = "Data Pesanan Masuk"
                rvOrders.visibility = View.VISIBLE
                loadOrders()
            }
            R.id.nav_profile -> {
                supportActionBar?.title = "Profil Kasir"
                svProfile.visibility = View.VISIBLE
                loadCashierProfile()
            }
            R.id.nav_logout -> {
                performLogout()
                return true
            }
            else -> return false
        }
        return true
    }

    private fun performLogout() {
        auth.signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        Toast.makeText(this, "Berhasil logout.", Toast.LENGTH_SHORT).show()
    }

    private fun loadCashierProfile() {
        if (currentUid == null) return
        database.getReference("cashiers").child(currentUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cashier = snapshot.getValue(Cashier::class.java)
                cashier?.let {
                    etProfileName.setText(it.name)
                    etProfilePhone.setText(it.phone)
                    etProfileAddress.setText(it.address)
                    tvProfileEmail.text = "Email: ${it.email} (Tidak dapat diubah)"
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@KasirDashboardActivity, "Gagal memuat profil: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveCashierProfile() {
        val name = etProfileName.text.toString().trim()
        val phone = etProfilePhone.text.toString().trim()
        val address = etProfileAddress.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Nama, Telepon, dan Alamat harus diisi.", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentUid == null) return
        val updateMap = mapOf("name" to name, "phone" to phone, "address" to address)
        database.getReference("cashiers").child(currentUid).updateChildren(updateMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadActiveMenu() {
        Log.d("KASIR_MENU", "Mencoba memuat produk aktif...")

        database.getReference("products")
            .orderByChild("active").equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val activeProductList = mutableListOf<Product>()
                    var totalData = 0

                    for (productSnapshot in snapshot.children) {
                        totalData++
                        var product = productSnapshot.getValue(Product::class.java)?.apply {
                            productId = productSnapshot.key ?: ""
                        }

                        if (product != null && product.isActive) {
                            activeProductList.add(product)
                            Log.d("KASIR_MENU", "Produk Ditemukan: ${product.productName}")
                        } else {
                            Log.w("KASIR_MENU", "Gagal memproses produk. Snapshot key: ${productSnapshot.key}. Product is null: ${product == null}")
                        }
                    }

                    Log.d("KASIR_MENU", "Total data dari Firebase: $totalData, Total produk yang tampil: ${activeProductList.size}")

                    productAdapter.updateProductList(activeProductList)

                    if (activeProductList.isEmpty()) {
                        Toast.makeText(this@KasirDashboardActivity, "Tidak ada menu aktif yang tersedia.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("KASIR_MENU", "Gagal memuat menu: ${error.message}")
                    Toast.makeText(this@KasirDashboardActivity, "Gagal koneksi ke database: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    fun addToCart(product: Product) {
        val priceInt = product.price.toInt()

        if (cartItems.containsKey(product.productId)) {
            val currentItem = cartItems[product.productId]!!
            val newQty = currentItem.qty + 1
            cartItems[product.productId] = currentItem.copy(qty = newQty)
        } else {
            val newItem = OrderItem(product.productName, 1, priceInt)
            cartItems[product.productId] = newItem
        }

        updateCartDisplay()
        Toast.makeText(this, "${product.productName} ditambahkan (x${cartItems[product.productId]?.qty})", Toast.LENGTH_SHORT).show()
    }

    private fun updateCartDisplay() {
        var total = 0
        cartItems.values.forEach {
            total += it.qty * it.price
        }

        val formattedTotal = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(total)
        tvCartTotal.text = "Keranjang: $formattedTotal"
        btnCheckout.isEnabled = total > 0
    }

    private fun createOrder() {
        // Logika Checkout (Tidak ada perubahan)
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Keranjang kosong! Tambahkan menu terlebih dahulu.", Toast.LENGTH_SHORT).show()
            return
        }

        var finalTotal = 0
        cartItems.values.forEach { finalTotal += it.qty * it.price }

        val newOrder = Order(
            buyerName = "Pelanggan di Tempat",
            timestamp = System.currentTimeMillis(),
            totalAmount = finalTotal,
            status = "BARU",
            orderItems = cartItems
        )

        val newOrderRef = database.getReference("orders").push()

        newOrderRef.setValue(newOrder)
            .addOnSuccessListener {
                val orderKey = newOrderRef.key ?: "ORD-Error"
                Toast.makeText(this, "Pesanan #$orderKey berhasil dibuat!", Toast.LENGTH_LONG).show()
                cartItems.clear()
                updateCartDisplay()
                bottomNav.selectedItemId = R.id.nav_orders
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal membuat pesanan: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("KASIR_ORDER", "Error creating order: ${e.message}")
            }
    }

    private fun loadOrders() {
        database.getReference("orders")
            .orderByChild("status")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val incomingOrderList = mutableListOf<Order>()
                    for (orderSnapshot in snapshot.children) {
                        val order = orderSnapshot.getValue(Order::class.java)?.apply {
                            orderId = orderSnapshot.key ?: ""
                        }
                        order?.let {
                            if (it.status == "BARU" || it.status == "DIPROSES") {
                                incomingOrderList.add(it)
                            }
                        }
                    }
                    orderAdapter.updateList(incomingOrderList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("KASIR_ORDER", "Gagal memuat pesanan: ${error.message}")
                }
            })
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        val updateMap = mapOf("status" to newStatus, "processedBy" to currentUid)
        database.getReference("orders").child(orderId).updateChildren(updateMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Status Order $orderId diubah menjadi $newStatus.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengubah status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

