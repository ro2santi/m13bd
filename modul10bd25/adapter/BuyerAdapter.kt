package com.ro2santi.modul10bd25.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ro2santi.modul10bd25.R
import com.ro2santi.modul10bd25.model.Buyer

typealias OnBuyerActionListener = (buyer: Buyer, action: String) -> Unit

class BuyerAdapter(
    private val buyerList: MutableList<Buyer>,
    private val listener: OnBuyerActionListener
) : RecyclerView.Adapter<BuyerAdapter.BuyerViewHolder>() {
    inner class BuyerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_buyer_name)
        val tvPhone: TextView = itemView.findViewById(R.id.tv_buyer_phone)
        val tvAddress: TextView = itemView.findViewById(R.id.tv_buyer_address)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit_buyer)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete_buyer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_buyer, parent, false)
        return BuyerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuyerViewHolder, position: Int) {
        val buyer = buyerList[position]

        holder.tvName.text = buyer.name
        holder.tvPhone.text = buyer.phone
        holder.tvAddress.text = buyer.address

        holder.btnEdit.setOnClickListener {
            listener.invoke(buyer, "EDIT")
        }

        holder.btnDelete.setOnClickListener {
            listener.invoke(buyer, "DELETE")
        }
    }

    override fun getItemCount(): Int = buyerList.size

    fun updateBuyerList(newBuyerList: List<Buyer>) {
        buyerList.clear()
        buyerList.addAll(newBuyerList)
        notifyDataSetChanged()
    }
}