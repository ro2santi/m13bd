package com.ro2santi.modul10bd25.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ro2santi.modul10bd25.R
import com.ro2santi.modul10bd25.model.Cashier

typealias OnCashierActionListener = (cashier: Cashier, action: String, isChecked: Boolean?) -> Unit

class CashierAdapter(
    private val cashierList: MutableList<Cashier>,
    private val listener: OnCashierActionListener
) : RecyclerView.Adapter<CashierAdapter.CashierViewHolder>() {
    inner class CashierViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_cashier_name)
        val tvEmail: TextView = itemView.findViewById(R.id.tv_cashier_email)
        val tvPhone: TextView = itemView.findViewById(R.id.tv_cashier_phone)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_cashier_status)
        val statusSwitch: Switch = itemView.findViewById(R.id.switch_cashier_status)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit_cashier)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete_cashier)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CashierViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cashier, parent, false)
        return CashierViewHolder(view)
    }

    override fun onBindViewHolder(holder: CashierViewHolder, position: Int) {
        val cashier = cashierList[position]
        val context = holder.itemView.context

        holder.tvName.text = cashier.name
        holder.tvEmail.text = "Email: ${cashier.email}"
        holder.tvPhone.text = "WA: ${cashier.phone}"

        holder.statusSwitch.isChecked = cashier.isActive
        holder.tvStatus.text = if (cashier.isActive) "Status: Aktif" else "Status: Nonaktif"
        holder.tvStatus.setTextColor(ContextCompat.getColor(context,
            if (cashier.isActive) android.R.color.holo_green_dark else android.R.color.holo_red_dark))

        holder.btnEdit.setOnClickListener {
            listener.invoke(cashier, "EDIT", null)
        }

        holder.btnDelete.setOnClickListener {
            listener.invoke(cashier, "DELETE", null)
        }

        holder.statusSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != cashier.isActive) {
                listener.invoke(cashier, "TOGGLE_STATUS", isChecked)
            }
        }
    }

    override fun getItemCount(): Int = cashierList.size

    fun updateCashierList(newCashierList: List<Cashier>) {
        cashierList.clear()
        cashierList.addAll(newCashierList)
        notifyDataSetChanged()
    }
}