package com.example.hell_mode.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hell_mode.R
import com.example.hell_mode.model.Specialization

class SpecializationAdapter(
    private var specializations: List<Specialization>,
    private val onEditClick: (Specialization) -> Unit,
    private val onDeleteClick: (Specialization) -> Unit
) : RecyclerView.Adapter<SpecializationAdapter.SpecViewHolder>() {

    class SpecViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorIndicator: View = itemView.findViewById(R.id.view_spec_color_indicator)
        val tvName: TextView = itemView.findViewById(R.id.tv_spec_name)
        val tvLevel: TextView = itemView.findViewById(R.id.tv_spec_level)
        val tvCategory: TextView = itemView.findViewById(R.id.tv_spec_category)
        val tvTimeStudied: TextView = itemView.findViewById(R.id.tv_spec_time_studied)
        val tvExpText: TextView = itemView.findViewById(R.id.tv_spec_exp_text)
        val progressExp: ProgressBar = itemView.findViewById(R.id.progress_spec_exp)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_spec)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_spec)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_specialization, parent, false)
        return SpecViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpecViewHolder, position: Int) {
        val spec = specializations[position]

        holder.tvName.text = spec.name
        holder.tvLevel.text = "Level ${spec.level}"
        holder.tvCategory.text = "Kategori: ${spec.category}"

        // Format total focus time
        val hours = spec.totalMinutesStudied / 60
        val mins = spec.totalMinutesStudied % 60
        holder.tvTimeStudied.text = "Total Fokus: ${hours}j ${mins}m"

        holder.tvExpText.text = "${spec.currentExp} / ${spec.maxExp} EXP"
        holder.progressExp.max = spec.maxExp
        holder.progressExp.progress = spec.currentExp.coerceAtMost(spec.maxExp)

        try {
            holder.colorIndicator.setBackgroundColor(Color.parseColor(spec.colorHex))
        } catch (_: Exception) {
            holder.colorIndicator.setBackgroundColor(Color.parseColor("#3498DB"))
        }

        holder.btnEdit.setOnClickListener { onEditClick(spec) }
        holder.btnDelete.setOnClickListener { onDeleteClick(spec) }
    }

    override fun getItemCount(): Int = specializations.size

    fun updateData(newList: List<Specialization>) {
        this.specializations = newList
        notifyDataSetChanged()
    }
}
