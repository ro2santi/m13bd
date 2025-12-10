package com.ro2santi.modul10bd25.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ro2santi.modul10bd25.KasirDashboardActivity
import com.ro2santi.modul10bd25.R
import com.ro2santi.modul10bd25.model.Order
import com.ro2santi.modul10bd25.model.OrderItem // Import OrderItem agar bisa digunakan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(
    private val activity: KasirDashboardActivity,
    private val orders: MutableList<Order>
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {
    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tv_order_status)
        val tvOrderTotal: TextView = itemView.findViewById(R.id.tv_order_total)
        val tvOrderTime: TextView = itemView.findViewById(R.id.tv_order_time)
        val tvOrderItemsSummary: TextView = itemView.findViewById(R.id.tv_order_items_summary)
        val btnProcessOrder: Button = itemView.findViewById(R.id.btn_process_order)
        val btnFinishOrder: Button = itemView.findViewById(R.id.btn_finish_order)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_cashier, parent, false)
        return OrderViewHolder(view)
    }

    override fun getItemCount(): Int = orders.size

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        val formattedTotal = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(order.totalAmount)
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val formattedTime = dateFormat.format(Date(order.timestamp))

        // *** PERBAIKAN LOGIKA PENGAMBILAN ITEM PESANAN ***
        // Prioritaskan 'items' (List) dari pesanan Pembeli, jika kosong gunakan 'orderItems' (Map) dari pesanan Kasir
        val itemsToDisplay: List<OrderItem> = if (order.items.isNotEmpty()) {
            order.items // Gunakan List<OrderItem> dari Pembeli
        } else {
            order.orderItems.values.toList() // Konversi Map<String, OrderItem> dari Kasir menjadi List
        }

        // Buat summary menggunakan properti 'productName' yang sudah diperbaiki
        val summary = itemsToDisplay.joinToString(", ") { "${it.productName} (x${it.qty})" }
        // *** AKHIR PERBAIKAN LOGIKA ***


        holder.tvOrderId.text = "#${order.orderId} (${order.buyerName})"
        holder.tvOrderTotal.text = "Total Pembayaran: $formattedTotal"
        holder.tvOrderTime.text = "Waktu Pesanan: $formattedTime"
        holder.tvOrderItemsSummary.text = "Isi: $summary" // Ringkasan item sekarang akan tampil

        holder.tvOrderStatus.text = order.status

        when (order.status) {
            "BARU" -> {
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_new)
                holder.btnProcessOrder.visibility = View.VISIBLE
                holder.btnFinishOrder.visibility = View.GONE
            }
            "DIPROSES" -> {
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_processing)
                holder.btnProcessOrder.visibility = View.GONE
                holder.btnFinishOrder.visibility = View.VISIBLE
            }
            "SELESAI" -> {
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_finished)
                holder.btnProcessOrder.visibility = View.GONE
                holder.btnFinishOrder.visibility = View.GONE
            }
            else -> {
                holder.tvOrderStatus.setBackgroundColor(Color.GRAY)
                holder.btnProcessOrder.visibility = View.GONE
                holder.btnFinishOrder.visibility = View.GONE
            }
        }

        holder.btnProcessOrder.setOnClickListener {
            activity.updateOrderStatus(order.orderId, "DIPROSES")
        }

        holder.btnFinishOrder.setOnClickListener {
            activity.updateOrderStatus(order.orderId, "SELESAI")
        }
    }

    fun updateList(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}