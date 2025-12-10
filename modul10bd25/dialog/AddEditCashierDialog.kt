package com.ro2santi.modul10bd25.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.ro2santi.modul10bd25.R
import com.ro2santi.modul10bd25.model.Cashier

class AddEditCashierDialog : DialogFragment() {

    interface CashierDialogListener {
        fun onCashierRegistered(email: String, password: String, cashier: Cashier)
        fun onCashierProfileEdited(cashier: Cashier)
    }

    private var listener: CashierDialogListener? = null
    private var cashierToEdit: Cashier? = null
    private var isEditMode: Boolean = false

    private lateinit var tvDialogTitle: TextView
    private lateinit var tvRegistrationInfo: TextView
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPhotoUrl: EditText
    private lateinit var btnSave: Button

    companion object {
        private const val ARG_CASHIER = "cashier_to_edit"

        fun newInstance(cashier: Cashier?): AddEditCashierDialog {
            val fragment = AddEditCashierDialog()
            val args = Bundle()
            args.putParcelable(ARG_CASHIER, cashier)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as CashierDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement CashierDialogListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cashierToEdit = arguments?.getParcelable(ARG_CASHIER)
        isEditMode = cashierToEdit != null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_add_edit_cashier, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDialogTitle = view.findViewById(R.id.tv_dialog_title)
        tvRegistrationInfo = view.findViewById(R.id.tv_registration_info)
        tilEmail = view.findViewById(R.id.til_email)
        tilPassword = view.findViewById(R.id.til_password)
        etEmail = view.findViewById(R.id.et_cashier_email)
        etPassword = view.findViewById(R.id.et_cashier_password)
        etName = view.findViewById(R.id.et_cashier_name)
        etPhone = view.findViewById(R.id.et_cashier_phone)
        etAddress = view.findViewById(R.id.et_cashier_address)
        etPhotoUrl = view.findViewById(R.id.et_cashier_photo_url)
        btnSave = view.findViewById(R.id.btn_save_cashier)
        val btnCancel: Button = view.findViewById(R.id.btn_cancel_cashier)

        setupUiMode()

        btnSave.setOnClickListener {
            validateAndSaveCashier()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun setupUiMode() {
        if (isEditMode) {
            tvDialogTitle.text = "Edit Profil Kasir"
            tilEmail.visibility = View.GONE
            tilPassword.visibility = View.GONE
            tvRegistrationInfo.visibility = View.GONE
            fillForm(cashierToEdit!!)
            btnSave.text = "Update Profil"
        } else {
            tvDialogTitle.text = "Tambah Akun Kasir Baru"
            tilEmail.visibility = View.VISIBLE
            tilPassword.visibility = View.VISIBLE
            tvRegistrationInfo.visibility = View.VISIBLE
            btnSave.text = "Daftar & Simpan"
        }
    }

    private fun fillForm(cashier: Cashier) {
        etEmail.setText(cashier.email)
        etName.setText(cashier.name)
        etPhone.setText(cashier.phone)
        etAddress.setText(cashier.address)
        etPhotoUrl.setText(cashier.photoUrl)
    }

    private fun validateAndSaveCashier() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val photoUrl = etPhotoUrl.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(context, "Nama, Telepon, dan Alamat harus diisi.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isEditMode) {
            if (email.isEmpty() || password.isEmpty() || password.length < 6) {
                Toast.makeText(context, "Email dan Password minimal 6 karakter harus diisi.", Toast.LENGTH_LONG).show()
                return
            }

            val newCashier = Cashier(
                uid = "", // ID akan diisi setelah Auth berhasil
                email = email,
                name = name,
                phone = phone,
                address = address,
                photoUrl = photoUrl,
                isActive = true
            )
            listener?.onCashierRegistered(email, password, newCashier)

        } else {
            val editedCashier = Cashier(
                uid = cashierToEdit!!.uid,
                email = cashierToEdit!!.email, // Email tidak diubah di sini
                name = name,
                phone = phone,
                address = address,
                photoUrl = photoUrl,
                isActive = cashierToEdit!!.isActive
            )
            listener?.onCashierProfileEdited(editedCashier)
        }

        dismiss()
    }
}