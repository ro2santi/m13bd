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
import com.ro2santi.modul10bd25.R
import com.ro2santi.modul10bd25.model.Buyer

class AddEditBuyerDialog : DialogFragment() {

    interface BuyerDialogListener {
        fun onBuyerSaved(buyer: Buyer)
    }

    private var listener: BuyerDialogListener? = null
    private var buyerToEdit: Buyer? = null

    private lateinit var tvDialogTitle: TextView
    private lateinit var etBuyerName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    companion object {
        private const val ARG_BUYER = "buyer_to_edit"

        fun newInstance(buyer: Buyer?): AddEditBuyerDialog {
            val fragment = AddEditBuyerDialog()
            val args = Bundle()
            args.putParcelable(ARG_BUYER, buyer)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as BuyerDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement BuyerDialogListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buyerToEdit = arguments?.getParcelable(ARG_BUYER)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_edit_buyer, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDialogTitle = view.findViewById(R.id.tv_dialog_title)
        etBuyerName = view.findViewById(R.id.et_buyer_name)
        etPhone = view.findViewById(R.id.et_buyer_phone)
        etAddress = view.findViewById(R.id.et_buyer_address)
        etEmail = view.findViewById(R.id.et_buyer_email)
        btnSave = view.findViewById(R.id.btn_save_buyer)
        btnCancel = view.findViewById(R.id.btn_cancel_buyer)

        if (buyerToEdit != null) {
            tvDialogTitle.text = "Edit Pembeli"
            fillForm(buyerToEdit!!)
            btnSave.text = "Update"
        } else {
            tvDialogTitle.text = "Tambah Pembeli Baru"
            btnSave.text = "Simpan"
        }

        btnSave.setOnClickListener {
            validateAndSaveBuyer()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun fillForm(buyer: Buyer) {
        etBuyerName.setText(buyer.name)
        etPhone.setText(buyer.phone)
        etAddress.setText(buyer.address)
        etEmail.setText(buyer.email)
    }

    private fun validateAndSaveBuyer() {
        val name = etBuyerName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val email = etEmail.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(context, "Nama, Telepon, dan Alamat harus diisi.", Toast.LENGTH_SHORT).show()
            return
        }

        val buyerToSave = Buyer(
            buyerUid = buyerToEdit?.buyerUid ?: "", // ID akan di-handle oleh Activity
            name = name,
            phone = phone,
            address = address,
            email = email
        )

        listener?.onBuyerSaved(buyerToSave)
        dismiss()
    }
}