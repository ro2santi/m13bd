package com.ro2santi.modul10bd25.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ro2santi.modul10bd25.R
import com.ro2santi.modul10bd25.model.Product

class AddEditProductDialog : DialogFragment() {

    interface ProductDialogListener {
        fun onProductSaved(product: Product)
    }

    private var listener: ProductDialogListener? = null
    private var productToEdit: Product? = null
    private var imageUri: Uri? = null

    private lateinit var tvDialogTitle: TextView
    private lateinit var etProductName: EditText
    private lateinit var etPrice: EditText
    private lateinit var etIngredients: EditText
    private lateinit var etPackaging: EditText
    private lateinit var ivProductImage: ImageView
    private lateinit var btnPickImage: Button
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    companion object {
        private const val ARG_PRODUCT = "product_to_edit"
        private const val IMAGE_PICK_CODE = 1000

        fun newInstance(product: Product?): AddEditProductDialog {
            val fragment = AddEditProductDialog()
            val args = Bundle()
            args.putParcelable(ARG_PRODUCT, product)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as ProductDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement ProductDialogListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productToEdit = arguments?.getParcelable(ARG_PRODUCT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_edit_product, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDialogTitle = view.findViewById(R.id.tv_dialog_title)
        etProductName = view.findViewById(R.id.et_product_name)
        etPrice = view.findViewById(R.id.et_product_price)
        etIngredients = view.findViewById(R.id.et_product_ingredients)
        etPackaging = view.findViewById(R.id.et_product_packaging)
        ivProductImage = view.findViewById(R.id.iv_product_image)
        btnPickImage = view.findViewById(R.id.btn_pick_image)
        btnSave = view.findViewById(R.id.btn_save_product)
        btnCancel = view.findViewById(R.id.btn_cancel_product)

        if (productToEdit != null) {
            tvDialogTitle.text = "Edit Produk"
            fillForm(productToEdit!!)
            btnSave.text = "Update"
        } else {
            tvDialogTitle.text = "Tambah Produk Baru"
            btnSave.text = "Simpan"
        }

        btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        btnSave.setOnClickListener {
            validateAndSaveProduct()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun fillForm(product: Product) {
        etProductName.setText(product.productName)
        etPrice.setText(product.price.toString())
        etIngredients.setText(product.ingredients)
        etPackaging.setText(product.packaging)

        if (product.imageUrl.isNotEmpty() && context != null) {
            Glide.with(this)
                .load(product.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(ivProductImage)
        } else {
            ivProductImage.setImageResource(R.drawable.ic_image_placeholder)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageUri = data?.data
            ivProductImage.setImageURI(imageUri)
        }
    }

    private fun validateAndSaveProduct() {
        val name = etProductName.text.toString().trim()
        val priceStr = etPrice.text.toString().trim()
        val ingredients = etIngredients.text.toString().trim()
        val packaging = etPackaging.text.toString().trim()

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(context, "Nama dan Harga harus diisi.", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            Toast.makeText(context, "Harga tidak valid.", Toast.LENGTH_SHORT).show()
            return
        }

        val finalImageUrl = productToEdit?.imageUrl ?: ""

        val productToSave = Product(
            productId = productToEdit?.productId ?: "",
            productName = name,
            imageUrl = finalImageUrl,
            price = price,
            ingredients = ingredients,
            packaging = packaging,
            isActive = productToEdit?.isActive ?: true
        )

        listener?.onProductSaved(productToSave)
        dismiss()
    }
}