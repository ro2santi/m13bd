package com.ro2santi.modul10bd25

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ro2santi.modul10bd25.adapter.ProductAdapter
import com.ro2santi.modul10bd25.dialog.AddEditProductDialog
import com.ro2santi.modul10bd25.model.Product

class ProductManagementActivity : AppCompatActivity(), AddEditProductDialog.ProductDialogListener {

    private lateinit var rvProducts: RecyclerView
    private lateinit var fabAddProduct: FloatingActionButton

    private lateinit var productList: MutableList<Product>
    private lateinit var productAdapter: ProductAdapter

    private lateinit var database: FirebaseDatabase
    private lateinit var productsRef: DatabaseReference // Referensi ke node 'products'

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_management)

        supportActionBar?.title = "Kelola Produk"

        database = FirebaseDatabase.getInstance()
        productsRef = database.getReference("products") // Nama node utama di RTDB

        rvProducts = findViewById(R.id.rv_products)
        fabAddProduct = findViewById(R.id.fab_add_product)

        productList = mutableListOf()
        productAdapter = ProductAdapter(productList, this::onProductAction)

        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = productAdapter

        loadProducts()

        fabAddProduct.setOnClickListener {
            showAddProductDialog(null)
        }
    }

    private fun loadProducts() {
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newProductList = mutableListOf<Product>() // Gunakan list lokal
                if (snapshot.exists()) {
                    for (productSnapshot in snapshot.children) {
                        val product = productSnapshot.getValue(Product::class.java)
                        product?.let {
                            newProductList.add(it.copy(productId = productSnapshot.key ?: ""))
                        }
                    }
                }
                // Panggil update
                productAdapter.updateProductList(newProductList)
                Log.d("FINAL_DEBUG", "Data berhasil di-update ke adapter, jumlah: ${newProductList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RTDB", "Gagal memuat data produk: ${error.message}")
                Toast.makeText(this@ProductManagementActivity,
                    "Gagal memuat data produk: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddProductDialog(product: Product?) {
        val dialog = AddEditProductDialog.newInstance(product)
        dialog.show(supportFragmentManager, "AddEditProductDialog")
    }

    private fun onProductAction(product: Product, action: String) {
        when (action) {
            "EDIT" -> showAddProductDialog(product) // Buka dialog dalam mode edit
            "DELETE" -> Toast.makeText(this, "TODO: Hapus ${product.productName}", Toast.LENGTH_SHORT).show()
            "TOGGLE_STATUS" -> Toast.makeText(this, "TODO: Ubah status ${product.productName}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onProductSaved(product: Product) {
        saveProductToRTDB(product)
    }

    private fun saveProductToRTDB(product: Product) {
        val isNewProduct = product.productId.isEmpty()

        val productRef: DatabaseReference = if (isNewProduct) {
            productsRef.push()
        } else {
            productsRef.child(product.productId)
        }

        val productData = product.copy(productId = productRef.key ?: "")

        productRef.setValue(productData)
            .addOnSuccessListener {
                val action = if (isNewProduct) "ditambahkan" else "diupdate"
                Toast.makeText(this, "Produk ${product.productName} berhasil $action.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan produk: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("RTDB", "Error saving data", e)
            }
    }
}

