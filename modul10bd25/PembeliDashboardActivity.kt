package com.ro2santi.modul10bd25

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ro2santi.modul10bd25.adapter.BuyerOrderAdapter
import com.ro2santi.modul10bd25.adapter.BuyerProductAdapter
import com.ro2santi.modul10bd25.adapter.OnProductAddToCartListener
import com.ro2santi.modul10bd25.model.OrderPembeli
import com.ro2santi.modul10bd25.model.OrderItemPembeli
import com.ro2santi.modul10bd25.model.Pembeli
import com.ro2santi.modul10bd25.model.ProductPembeli
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

// --- Cart Data Class (Internal) ---
data class CartItem(
    val product: ProductPembeli,
    var quantity: Int = 1
) {
    val subtotal: Double
        get() = product.price * quantity
}

class PembeliDashboardActivity : AppCompatActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    // UI Umum & Containers
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var rvBuyerMenu: RecyclerView
    private lateinit var rvBuyerOrders: RecyclerView
    private lateinit var menuAndCartContainer: LinearLayout
    private lateinit var svBuyerProfile: ScrollView

    // UI Cart & Menu
    private lateinit var tvBuyerCartTotal: TextView
    private lateinit var btnViewCart: Button
    private lateinit var productAdapter: BuyerProductAdapter
    private val productList = mutableListOf<ProductPembeli>()
    private val cartMap = mutableMapOf<String, CartItem>() // Key: productId

    // UI Order
    private lateinit var orderAdapter: BuyerOrderAdapter
    private val orderList = mutableListOf<OrderPembeli>()

    // UI Profile
    private lateinit var etBuyerName: EditText
    private lateinit var etBuyerPhone: EditText
    private lateinit var etBuyerAddress: EditText
    private lateinit var tvBuyerEmail: TextView
    private lateinit var btnSaveBuyerProfile: Button
    private var currentPembeli: Pembeli? = null

    // Format Rupiah
    private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pembeli_dashboard)

        // Cek login
        if (currentUid == null) {
            performLogout() // Jika UID tidak ada, langsung logout
            return
        }

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Inisialisasi UI
        initUI()

        // Setup RecylerViews
        setupAdapters()

        // Listeners
        setupListeners()

        // Muat data awal
        loadProducts()
        loadBuyerProfile() // Muat profil saat awal
        // loadBuyerOrders akan dipanggil saat menu orders diklik

        // Default View: Menu
        bottomNav.selectedItemId = R.id.nav_buyer_menu
        showView(R.id.nav_buyer_menu)
        updateCartDisplay()
    }

    private fun initUI() {
        bottomNav = findViewById(R.id.bottom_nav)

        // Containers
        menuAndCartContainer = findViewById(R.id.menu_and_cart_container)
        svBuyerProfile = findViewById(R.id.sv_buyer_profile)

        // Menu & Cart
        rvBuyerMenu = findViewById(R.id.rv_buyer_menu)
        tvBuyerCartTotal = findViewById(R.id.tv_buyer_cart_total)
        btnViewCart = findViewById(R.id.btn_view_cart)

        // Orders
        rvBuyerOrders = findViewById(R.id.rv_buyer_orders)

        // Profile
        etBuyerName = findViewById(R.id.et_buyer_name)
        etBuyerPhone = findViewById(R.id.et_buyer_phone)
        etBuyerAddress = findViewById(R.id.et_buyer_address)
        tvBuyerEmail = findViewById(R.id.tv_buyer_email)
        btnSaveBuyerProfile = findViewById(R.id.btn_save_buyer_profile)
    }

    private fun setupAdapters() {
        // Adapter Produk (Menu)
        productAdapter = BuyerProductAdapter(productList, listener = { product ->
            addToCart(product)
        })
        rvBuyerMenu.layoutManager = LinearLayoutManager(this)
        rvBuyerMenu.adapter = productAdapter

        // Adapter Order
        orderAdapter = BuyerOrderAdapter(this, orderList)
        rvBuyerOrders.layoutManager = LinearLayoutManager(this)
        rvBuyerOrders.adapter = orderAdapter
    }

    private fun setupListeners() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_buyer_menu -> showView(R.id.nav_buyer_menu)
                R.id.nav_buyer_orders -> {
                    loadBuyerOrders()
                    showView(R.id.nav_buyer_orders)
                }
                R.id.nav_buyer_profile -> showView(R.id.nav_buyer_profile)
                R.id.nav_buyer_logout -> performLogout()
            }
            true
        }

        btnViewCart.setOnClickListener {
            checkoutCart()
        }

        btnSaveBuyerProfile.setOnClickListener {
            saveBuyerProfile()
        }
    }

    private fun showView(itemId: Int) {
        // Sembunyikan semua container
        menuAndCartContainer.visibility = View.GONE
        rvBuyerOrders.visibility = View.GONE
        svBuyerProfile.visibility = View.GONE

        // Tampilkan container yang dipilih
        when (itemId) {
            R.id.nav_buyer_menu -> menuAndCartContainer.visibility = View.VISIBLE
            R.id.nav_buyer_orders -> rvBuyerOrders.visibility = View.VISIBLE
            R.id.nav_buyer_profile -> svBuyerProfile.visibility = View.VISIBLE
        }
    }

    // ==============================================
    // --- LOGIKA MENU (PRODUK) & CART ---
    // ==============================================

    private fun loadProducts() {
        database.getReference("products")
            .orderByChild("active") // Urutkan untuk memastikan produk aktif muncul
            .equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val products = mutableListOf<ProductPembeli>()
                    for (productSnapshot in snapshot.children) {
                        val product = productSnapshot.getValue(ProductPembeli::class.java)?.apply {
                            productId = productSnapshot.key ?: ""
                        }
                        product?.let { products.add(it) }
                    }
                    productAdapter.updateProductList(products)
                    Log.d("BUYER_MENU", "Produk dimuat: ${products.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("BUYER_MENU", "Gagal memuat produk: ${error.message}")
                    Toast.makeText(this@PembeliDashboardActivity, "Gagal memuat menu.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun addToCart(product: ProductPembeli) {
        val productId = product.productId
        if (cartMap.containsKey(productId)) {
            cartMap[productId]?.quantity = cartMap[productId]!!.quantity + 1
        } else {
            cartMap[productId] = CartItem(product, 1)
        }
        updateCartDisplay()
        Toast.makeText(this, "${product.productName} ditambahkan (Qty: ${cartMap[productId]?.quantity})", Toast.LENGTH_SHORT).show()
    }

    private fun updateCartDisplay() {
        val totalItems = cartMap.values.sumOf { it.quantity }
        val grandTotal = cartMap.values.sumOf { it.subtotal }

        tvBuyerCartTotal.text = "Total: ${rupiahFormat.format(grandTotal)} (Item: $totalItems)"

        if (totalItems > 0) {
            btnViewCart.isEnabled = true
            btnViewCart.alpha = 1.0f
        } else {
            btnViewCart.isEnabled = false
            btnViewCart.alpha = 0.5f
        }
    }

    private fun checkoutCart() {
        if (cartMap.isEmpty()) {
            Toast.makeText(this, "Keranjang belanja kosong.", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Validasi Profil Pembeli
        if (currentPembeli?.name.isNullOrEmpty() || currentPembeli?.phone.isNullOrEmpty()) {
            Toast.makeText(this, "Mohon lengkapi Nama dan Telepon di menu Profil sebelum checkout.", Toast.LENGTH_LONG).show()
            bottomNav.selectedItemId = R.id.nav_buyer_profile
            showView(R.id.nav_buyer_profile)
            return
        }

        // 2. Konversi Cart Map menjadi List OrderItemPembeli
        val orderItems = cartMap.values.map { item ->
            OrderItemPembeli(
                productName = item.product.productName,
                qty = item.quantity,
                price = item.product.price.roundToInt()
            )
        }

        val totalAmount = cartMap.values.sumOf { it.subtotal }.roundToInt()

        // 3. Buat objek OrderPembeli
        val newOrderRef = database.getReference("orders").push()
        val orderId = newOrderRef.key ?: return

        val newOrder = OrderPembeli(
            orderId = orderId,
            buyerUid = currentUid!!,
            timestamp = System.currentTimeMillis(),
            status = "BARU",
            paymentStatus = "BELUM_BAYAR",
            pickupMethod = "DI_KASIR", // Asumsi default, bisa diganti di UI checkout terpisah
            totalAmount = totalAmount,
            items = orderItems,
            buyerName = currentPembeli?.name ?: "N/A",
            buyerPhone = currentPembeli?.phone ?: "N/A",
            buyerAddress = currentPembeli?.address // Nullable
        )

        // 4. Simpan ke Firebase
        newOrderRef.setValue(newOrder)
            .addOnSuccessListener {
                Toast.makeText(this, "Pesanan berhasil dibuat! Cek di menu Pesanan.", Toast.LENGTH_LONG).show()
                cartMap.clear()
                updateCartDisplay()
                // Pindah ke halaman order
                bottomNav.selectedItemId = R.id.nav_buyer_orders
                showView(R.id.nav_buyer_orders)
                loadBuyerOrders()
            }
            .addOnFailureListener { e ->
                Log.e("BUYER_CHECKOUT", "Gagal membuat pesanan: ${e.message}", e)
                Toast.makeText(this, "Gagal membuat pesanan: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // ==============================================
    // --- LOGIKA ORDER ---
    // ==============================================

    fun loadBuyerOrders() {
        if (currentUid == null) return

        database.getReference("orders")
            .orderByChild("buyerUid")
            .equalTo(currentUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val orders = mutableListOf<OrderPembeli>()
                    for (orderSnapshot in snapshot.children) {
                        val order = orderSnapshot.getValue(OrderPembeli::class.java)?.apply {
                            orderId = orderSnapshot.key ?: ""
                        }
                        order?.let { orders.add(it) }
                    }
                    // Tampilkan pesanan terbaru di atas
                    orders.sortByDescending { it.timestamp }
                    orderAdapter.updateList(orders)
                    Log.d("BUYER_ORDER", "Pesanan dimuat: ${orders.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("BUYER_ORDER", "Gagal memuat pesanan: ${error.message}")
                }
            })
    }

    fun updateOrderPaymentStatus(orderId: String, newStatus: String) {
        // Status: SUDAH_BAYAR / BELUM_BAYAR
        database.getReference("orders").child(orderId).child("paymentStatus").setValue(newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Konfirmasi pembayaran berhasil.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal konfirmasi pembayaran.", Toast.LENGTH_SHORT).show()
            }
    }

    fun confirmOrderReceived(orderId: String) {
        // Status: SELESAI
        database.getReference("orders").child(orderId).child("status").setValue("SELESAI")
            .addOnSuccessListener {
                Toast.makeText(this, "Pesanan diterima, transaksi selesai.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal konfirmasi penerimaan pesanan.", Toast.LENGTH_SHORT).show()
            }
    }

    // ==============================================
    // --- LOGIKA PROFIL PEMBELI ---
    // ==============================================

    private fun loadBuyerProfile() {
        if (currentUid == null) return

        database.getReference("buyers").child(currentUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentPembeli = snapshot.getValue(Pembeli::class.java)?.apply {
                        // Jika data di RTDB tidak memiliki UID, gunakan UID auth
                        uid = currentUid
                    }
                    currentPembeli?.let { buyer ->
                        etBuyerName.setText(buyer.name)
                        etBuyerPhone.setText(buyer.phone)
                        etBuyerAddress.setText(buyer.address)
                        tvBuyerEmail.text = "Email: ${buyer.email}"
                    } ?: run {
                        // Kasus Pembeli baru yang belum memiliki entri di 'buyers'
                        val email = auth.currentUser?.email ?: "N/A"
                        currentPembeli = Pembeli(currentUid, email, "Pembeli Baru", "", "")
                        tvBuyerEmail.text = "Email: $email"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("BUYER_PROFILE", "Gagal memuat profil: ${error.message}")
                    Toast.makeText(this@PembeliDashboardActivity, "Gagal memuat profil.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveBuyerProfile() {
        val name = etBuyerName.text.toString().trim()
        val phone = etBuyerPhone.text.toString().trim()
        val address = etBuyerAddress.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Nama dan Telepon wajib diisi.", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUid == null || currentPembeli == null) return

        // Update objek Pembeli
        val updatedBuyer = currentPembeli!!.copy(
            name = name,
            phone = phone,
            address = address
        )

        // Simpan ke Firebase
        database.getReference("buyers").child(currentUid).setValue(updatedBuyer)
            .addOnSuccessListener {
                currentPembeli = updatedBuyer
                Toast.makeText(this, "Profil Pembeli berhasil disimpan!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("BUYER_PROFILE", "Gagal menyimpan profil: ${e.message}")
                Toast.makeText(this, "Gagal menyimpan profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ==============================================
    // --- LOGIKA LOGOUT ---
    // ==============================================

    private fun performLogout() {
        auth.signOut()
        // Ganti LoginActivity::class.java dengan Activity Login Anda
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Toast.makeText(this, "Berhasil logout.", Toast.LENGTH_SHORT).show()
    }
}