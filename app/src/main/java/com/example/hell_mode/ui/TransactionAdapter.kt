package com.example.hell_mode.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hell_mode.R
import com.example.hell_mode.model.RealMoneyTransaction
import com.example.hell_mode.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<RealMoneyTransaction>,
    private val onEditClick: (RealMoneyTransaction) -> Unit,
    private val onDeleteClick: (RealMoneyTransaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_trans_title)
        val tvCategoryDate: TextView = itemView.findViewById(R.id.tv_trans_category_date)
        val tvAmount: TextView = itemView.findViewById(R.id.tv_trans_amount)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_trans)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_trans)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val trans = transactions[position]

        holder.tvTitle.text = trans.title

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val dateFormatted = sdf.format(trans.timestamp)
        holder.tvCategoryDate.text = "${trans.category} • $dateFormatted"

        val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val formattedAmount = rupiahFormat.format(trans.amount)

        if (trans.type == TransactionType.INCOME) {
            holder.tvAmount.text = "+ $formattedAmount"
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.rpg_hp_green))
        } else {
            holder.tvAmount.text = "- $formattedAmount"
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.rpg_hp_red))
        }

        holder.btnEdit.setOnClickListener { onEditClick(trans) }
        holder.btnDelete.setOnClickListener { onDeleteClick(trans) }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateData(newList: List<RealMoneyTransaction>) {
        this.transactions = newList
        notifyDataSetChanged()
    }
}
