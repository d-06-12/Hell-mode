package com.example.hell_mode.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hell_mode.R
import com.example.hell_mode.model.ShopItem
import com.google.android.material.button.MaterialButton

class ShopAdapter(
    private var items: List<ShopItem>,
    private val onBuyClick: (ShopItem) -> Unit
) : RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {

    class ShopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_shop_item_title)
        val tvDesc: TextView = itemView.findViewById(R.id.tv_shop_item_desc)
        val tvCost: TextView = itemView.findViewById(R.id.tv_shop_item_cost)
        val btnBuy: MaterialButton = itemView.findViewById(R.id.btn_buy_shop_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shop, parent, false)
        return ShopViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val item = items[position]

        holder.tvTitle.text = item.title
        holder.tvDesc.text = item.description
        holder.tvCost.text = "Harga: 🪙 ${item.cost} Gold"

        holder.btnBuy.setOnClickListener { onBuyClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newList: List<ShopItem>) {
        this.items = newList
        notifyDataSetChanged()
    }
}
