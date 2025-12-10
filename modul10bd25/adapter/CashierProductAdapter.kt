package com.ro2santi.modul10bd25.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ro2santi.modul10bd25.R
import com.ro2santi.modul10bd25.model.Product
import java.text.NumberFormat
import java.util.Locale

class CashierProductAdapter(
    private val productList: MutableList<Product>,
    private val listener: OnAddToCartListener
) : RecyclerView.Adapter<CashierProductAdapter.ProductViewHolder>() {
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_product_name)
        // Pastikan Anda memiliki tv_product_ingredients di layout kasir
        val tvIngredients: TextView = itemView.findViewById(R.id.tv_product_ingredients)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        val btnAddToCart: ImageButton = itemView.findViewById(R.id.btn_add_to_cart)
        val ivImage: ImageView = itemView.findViewById(R.id.iv_product_item_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_kasir, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        val context = holder.itemView.context

        holder.tvName.text = product.productName
        holder.tvIngredients.text = product.ingredients

        val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        holder.tvPrice.text = format.format(product.price)

        if (product.imageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(product.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivImage)
        } else {
            holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.btnAddToCart.setOnClickListener {
            listener.invoke(product)
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateProductList(newProductList: List<Product>) {
        productList.clear()
        productList.addAll(newProductList)
        notifyDataSetChanged()
    }
}

