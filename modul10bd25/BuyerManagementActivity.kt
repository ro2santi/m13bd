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
import com.ro2santi.modul10bd25.adapter.BuyerAdapter
import com.ro2santi.modul10bd25.dialog.AddEditBuyerDialog
import com.ro2santi.modul10bd25.model.Buyer

class BuyerManagementActivity : AppCompatActivity(), AddEditBuyerDialog.BuyerDialogListener {

    private lateinit var rvBuyers: RecyclerView

    private lateinit var buyerList: MutableList<Buyer>
    private lateinit var buyerAdapter: BuyerAdapter

    private lateinit var database: FirebaseDatabase
    private lateinit var buyersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_management)

        supportActionBar?.title = "Kelola Pembeli"

        database = FirebaseDatabase.getInstance()
        buyersRef = database.getReference("buyers")

        rvBuyers = findViewById(R.id.rv_buyers)

        buyerList = mutableListOf()
        buyerAdapter = BuyerAdapter(buyerList, this::onBuyerAction)

        rvBuyers.layoutManager = LinearLayoutManager(this)
        rvBuyers.adapter = buyerAdapter

        loadBuyers()
    }

    private fun loadBuyers() {
        buyersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newBuyerList = mutableListOf<Buyer>()
                if (snapshot.exists()) {
                    for (buyerSnapshot in snapshot.children) {
                        val buyer = buyerSnapshot.getValue(Buyer::class.java)
                        buyer?.let {
                            newBuyerList.add(it.copy(buyerUid = buyerSnapshot.key ?: ""))
                        }
                    }
                }
                buyerAdapter.updateBuyerList(newBuyerList)
                Log.d("FINAL_DEBUG", "Data berhasil di-update ke adapter, jumlah: ${newBuyerList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RTDB_BUYER", "Gagal memuat data pembeli: ${error.message}")
                Toast.makeText(this@BuyerManagementActivity,
                    "Gagal memuat data pembeli: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddBuyerDialog(buyer: Buyer?) {
        val dialog = AddEditBuyerDialog.newInstance(buyer)
        dialog.show(supportFragmentManager, "AddEditBuyerDialog")
    }

    private fun onBuyerAction(buyer: Buyer, action: String) {
        when (action) {
            "EDIT" -> showAddBuyerDialog(buyer) // Buka dialog dalam mode edit
            "DELETE" -> showDeleteConfirmationDialog(buyer)
        }
    }

    private fun showDeleteConfirmationDialog(buyer: Buyer) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Hapus Pembeli")
            .setMessage("Apakah Anda yakin ingin menghapus data pembeli '${buyer.name}'? Aksi ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { dialog, which ->
                deleteBuyerFromRTDB(buyer)
            }
            .setNegativeButton("Batal", null)
            .show()
    }


    private fun deleteBuyerFromRTDB(buyer: Buyer) {
        if (buyer.buyerUid.isEmpty()) {
            Toast.makeText(this, "ID Pembeli tidak valid.", Toast.LENGTH_SHORT).show()
            return
        }

        buyersRef.child(buyer.buyerUid).removeValue()
            .addOnSuccessListener {
                database.getReference("users").child(buyer.buyerUid).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Pembeli '${buyer.name}' berhasil dihapus.", Toast.LENGTH_SHORT).show()
                        Log.d("DELETE_BUYER", "Data peran user juga berhasil dihapus.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("DELETE_BUYER", "Gagal menghapus data peran user.", e)
                    }

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus pembeli: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("DELETE_BUYER", "Error deleting data", e)
            }
    }

    override fun onBuyerSaved(buyer: Buyer) {
        saveBuyerToRTDB(buyer)
    }

    private fun saveBuyerToRTDB(buyer: Buyer) {
        val isNewBuyer = buyer.buyerUid.isEmpty()

        val buyerRef: DatabaseReference = if (isNewBuyer) {
            buyersRef.push()
        } else {
            buyersRef.child(buyer.buyerUid)
        }

        val buyerData = buyer.copy(buyerUid = buyerRef.key ?: "")

        buyerRef.setValue(buyerData)
            .addOnSuccessListener {
                val action = if (isNewBuyer) "ditambahkan" else "diupdate"
                Toast.makeText(this, "Pembeli ${buyer.name} berhasil $action.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan pembeli: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("RTDB_BUYER", "Error saving data", e)
            }
    }
}