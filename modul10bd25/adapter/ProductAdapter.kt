package com.ro2santi.modul10bd25.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ro2santi.modul10bd25.R
import com.ro2santi.modul10bd25.model.Product
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.text.NumberFormat
import java.util.Locale

typealias OnProductActionListener = (product: Product, action: String) -> Unit

class ProductAdapter(
    private val productList: MutableList<Product>,
    private val listener: OnProductActionListener // Callback untuk aksi: Edit, Delete, Toggle
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_product_name)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status_label)
        val switchActive: Switch = itemView.findViewById(R.id.switch_product_active)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit_product)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete_product)
        val ivImage: ImageView = itemView.findViewById(R.id.iv_product_item_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        val context = holder.itemView.context

        holder.tvName.text = product.productName

        val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        holder.tvPrice.text = format.format(product.price)

        holder.switchActive.isChecked = product.isActive

        if (product.isActive) {
            holder.tvStatus.text = "Status: AKTIF"
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
        } else {
            holder.tvStatus.text = "Status: NONAKTIF"
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
        }

        if (product.imageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(product.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_image_placeholder) // Ganti dengan drawable placeholder
                .error(R.drawable.ic_image_error) // Ganti dengan drawable error 
                .into(holder.ivImage)
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_image_placeholder)
        }

        holder.btnEdit.setOnClickListener {
            listener.invoke(product, "EDIT")
        }

        holder.btnDelete.setOnClickListener {
            listener.invoke(product, "DELETE")
        }

        holder.switchActive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != product.isActive) {
                listener.invoke(product.copy(isActive = isChecked), "TOGGLE_STATUS")
            }
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateProductList(newProductList: List<Product>) {
        productList.clear()
        productList.addAll(newProductList)
        notifyDataSetChanged()
    }
}

