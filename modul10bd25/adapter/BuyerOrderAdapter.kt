package com.ro2santi.modul10bd25.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ro2santi.modul10bd25.PembeliDashboardActivity
import com.ro2santi.modul10bd25.R
import com.ro2santi.modul10bd25.model.OrderPembeli // Menggunakan OrderPembeli
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BuyerOrderAdapter(
    private val activity: PembeliDashboardActivity,
    private val orders: MutableList<OrderPembeli>
) : RecyclerView.Adapter<BuyerOrderAdapter.OrderViewHolder>() {

    private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_pembeli_id)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tv_order_pembeli_status)
        val tvOrderTotal: TextView = itemView.findViewById(R.id.tv_order_pembeli_total)
        val tvOrderTime: TextView = itemView.findViewById(R.id.tv_order_pembeli_time)
        val tvOrderItemsSummary: TextView = itemView.findViewById(R.id.tv_order_pembeli_items_summary)
        val tvPaymentStatus: TextView = itemView.findViewById(R.id.tv_order_pembeli_payment_status)
        val btnConfirmPayment: Button = itemView.findViewById(R.id.btn_pembeli_confirm_payment)
        val btnConfirmReceived: Button = itemView.findViewById(R.id.btn_pembeli_confirm_received)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_pembeli, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context

        // Tampilkan 6 digit terakhir ID order
        holder.tvOrderId.text = "Order #${order.orderId.takeLast(6).uppercase(Locale.getDefault())}"
        holder.tvOrderTime.text = dateFormat.format(Date(order.timestamp))
        holder.tvOrderTotal.text = rupiahFormat.format(order.totalAmount)
        holder.tvOrderStatus.text = order.status.replace("_", " ")
        holder.tvPaymentStatus.text = order.paymentStatus.replace("_", " ")

        // Summary items
        val summary = order.items.joinToString(", ") { item ->
            "${item.qty}x ${item.productName}"
        }
        holder.tvOrderItemsSummary.text = summary.ifEmpty { "Tidak ada item" }

        // --- Atur Tampilan Status ---
        // Anda perlu memastikan drawables atau background colors yang sesuai tersedia.
        // Di sini kita gunakan warna standar Android untuk contoh:
        when (order.status) {
            "BARU" -> holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#4CAF50")) // Green
            "DIPROSES" -> holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#FFC107")) // Yellow
            "SIAP_DIAMBIL", "SIAP_DIKIRIM" -> holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#2196F3")) // Blue
            "SELESAI" -> holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#9E9E9E")) // Gray
            else -> holder.tvOrderStatus.setBackgroundColor(Color.GRAY)
        }
        holder.tvOrderStatus.setTextColor(Color.WHITE)


        // --- Atur Tombol Aksi & Status Pembayaran ---
        holder.btnConfirmPayment.visibility = View.GONE
        holder.btnConfirmReceived.visibility = View.GONE

        // 1. Tombol Konfirmasi Pembayaran
        if (order.paymentStatus == "BELUM_BAYAR") {
            holder.btnConfirmPayment.visibility = View.VISIBLE
            holder.tvPaymentStatus.setTextColor(Color.RED)
        } else {
            holder.tvPaymentStatus.setTextColor(Color.parseColor("#006400")) // Hijau Tua
        }

        // 2. Tombol Pesanan Diterima (Hanya muncul jika status SIAP dan sudah BAYAR)
        if ((order.status == "SIAP_DIAMBIL" || order.status == "SIAP_DIKIRIM") && order.paymentStatus == "SUDAH_BAYAR") {
            holder.btnConfirmReceived.visibility = View.VISIBLE
        }

        // --- Listener ---
        holder.btnConfirmPayment.setOnClickListener {
            activity.updateOrderPaymentStatus(order.orderId, "SUDAH_BAYAR")
        }

        holder.btnConfirmReceived.setOnClickListener {
            activity.confirmOrderReceived(order.orderId)
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateList(newOrders: List<OrderPembeli>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}