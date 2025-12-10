package com.ro2santi.modul10bd25

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ro2santi.modul10bd25.adapter.CashierAdapter
import com.ro2santi.modul10bd25.dialog.AddEditCashierDialog
import com.ro2santi.modul10bd25.model.Cashier

class CashierManagementActivity : AppCompatActivity(), AddEditCashierDialog.CashierDialogListener {

    private lateinit var rvCashiers: RecyclerView
    private lateinit var fabAddCashier: FloatingActionButton

    private lateinit var cashierList: MutableList<Cashier>
    private lateinit var cashierAdapter: CashierAdapter

    private lateinit var database: FirebaseDatabase
    private lateinit var cashiersRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cashier_management)

        supportActionBar?.title = "Kelola Akun Kasir"

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        cashiersRef = database.getReference("cashiers")

        rvCashiers = findViewById(R.id.rv_cashiers) // Asumsi ID
        fabAddCashier = findViewById(R.id.fab_add_cashier) // Asumsi ID

        cashierList = mutableListOf()
        cashierAdapter = CashierAdapter(cashierList, this::onCashierAction)

        rvCashiers.layoutManager = LinearLayoutManager(this)
        rvCashiers.adapter = cashierAdapter

        loadCashiers()

        fabAddCashier.setOnClickListener {
            showAddEditDialog(null) // Mode Tambah/Registrasi
        }
    }

    private fun loadCashiers() {

        cashiersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newCashierList = mutableListOf<Cashier>()
                if (snapshot.exists()) {
                    for (cashierSnapshot in snapshot.children) {
                        val cashier = cashierSnapshot.getValue(Cashier::class.java)
                        cashier?.let {
                            newCashierList.add(it.copy(uid = cashierSnapshot.key ?: ""))
                        }
                    }
                }
                cashierAdapter.updateCashierList(newCashierList)
                Log.d("FINAL_DEBUG", "Data berhasil di-update ke adapter, jumlah: ${newCashierList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RTDB_CASHIER", "Gagal memuat data: ${error.message}")
                Toast.makeText(this@CashierManagementActivity, "Gagal memuat data kasir.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCashierRegistered(email: String, password: String, newCashier: Cashier) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { authTask ->
                if (authTask.isSuccessful) {
                    val uid = authTask.result?.user?.uid ?: return@addOnCompleteListener

                    saveUserRole(uid, email, "Kasir")

                    val finalCashier = newCashier.copy(uid = uid)
                    saveCashierProfile(finalCashier, isNewRegistration = true)

                } else {
                    Toast.makeText(this, "Registrasi Auth Gagal: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserRole(uid: String, email: String, role: String) {
        val userMap = mapOf("email" to email, "role" to role)
        database.getReference("users").child(uid).setValue(userMap)
            .addOnFailureListener { e ->
                Log.e("CASHIER_REG", "Gagal simpan peran user: ${e.message}")
                auth.currentUser?.delete()
            }
    }

    override fun onCashierProfileEdited(editedCashier: Cashier) {
        saveCashierProfile(editedCashier, isNewRegistration = false)
    }

    private fun saveCashierProfile(cashier: Cashier, isNewRegistration: Boolean) {
        cashiersRef.child(cashier.uid).setValue(cashier)
            .addOnSuccessListener {
                val message = if (isNewRegistration) "Akun Kasir berhasil didaftarkan." else "Profil Kasir berhasil diupdate."
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan data kasir: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun deleteCashier(cashier: Cashier) {
        cashiersRef.child(cashier.uid).removeValue()
            .addOnSuccessListener {
                database.getReference("users").child(cashier.uid).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Kasir '${cashier.name}' berhasil dihapus (Akun Auth tetap ada, tapi nonaktif).", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal hapus data peran kasir: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus kasir: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun toggleCashierStatus(cashier: Cashier, isChecked: Boolean) {
        cashiersRef.child(cashier.uid).child("isActive").setValue(isChecked)
            .addOnSuccessListener {
                val status = if (isChecked) "diaktifkan" else "dinonaktifkan"
                Toast.makeText(this, "Akun ${cashier.name} berhasil $status.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengubah status: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showAddEditDialog(cashier: Cashier?) {
        AddEditCashierDialog.newInstance(cashier).show(supportFragmentManager, "AddEditCashierDialog")
    }

    private fun onCashierAction(cashier: Cashier, action: String, isChecked: Boolean?) {
        when (action) {
            "EDIT" -> showAddEditDialog(cashier)
            "DELETE" -> showDeleteConfirmationDialog(cashier)
            "TOGGLE_STATUS" -> isChecked?.let { toggleCashierStatus(cashier, it) }
        }
    }

    private fun showDeleteConfirmationDialog(cashier: Cashier) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Hapus Akun Kasir")
            .setMessage("Apakah yakin ingin menghapus akun ${cashier.name} (Email: ${cashier.email})?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteCashier(cashier)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}

