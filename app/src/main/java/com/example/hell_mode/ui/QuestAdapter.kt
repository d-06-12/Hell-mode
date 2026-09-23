package com.example.hell_mode.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hell_mode.R
import com.example.hell_mode.model.Quest
import com.example.hell_mode.model.QuestDifficulty
import com.google.android.material.button.MaterialButton

class QuestAdapter(
    private var quests: MutableList<Quest>,
    private var isHellMode: Boolean,
    private val onCompleteClick: (Quest) -> Unit,
    private val onStartTimerClick: (Quest) -> Unit,
    private val onDeleteClick: (Quest) -> Unit
) : RecyclerView.Adapter<QuestAdapter.QuestViewHolder>() {

    class QuestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: View = itemView.findViewById(R.id.card_quest_root)
        val tvDifficulty: TextView = itemView.findViewById(R.id.tv_quest_difficulty)
        val tvRewardExp: TextView = itemView.findViewById(R.id.tv_quest_reward_exp)
        val tvRewardGold: TextView = itemView.findViewById(R.id.tv_quest_reward_gold)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_quest_title)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_quest_description)
        val tvSpecialization: TextView = itemView.findViewById(R.id.tv_quest_specialization)
        val tvTimerReq: TextView = itemView.findViewById(R.id.tv_quest_timer_req)
        val btnStartTimer: MaterialButton = itemView.findViewById(R.id.btn_start_timer)
        val btnComplete: MaterialButton = itemView.findViewById(R.id.btn_complete_quest)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_quest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quest, parent, false)
        return QuestViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        val quest = quests[position]

        holder.tvTitle.text = quest.title
        holder.tvDescription.text = quest.description.ifEmpty { "Tanpa deskripsi" }

        // Hell Mode: 1.5x EXP and 1.5x Gold
        val multiplier = if (isHellMode) 1.5 else 1.0
        val exp = (quest.expReward * multiplier).toInt()
        val gold = (quest.goldReward * multiplier).toInt()
        holder.tvRewardExp.text = "+$exp EXP"
        holder.tvRewardGold.text = "+$gold Gold"

        // Difficulty tag styling
        holder.tvDifficulty.text = quest.difficulty.displayName
        val diffColor = when (quest.difficulty) {
            QuestDifficulty.EASY -> ContextCompat.getColor(holder.itemView.context, R.color.diff_easy)
            QuestDifficulty.MEDIUM -> ContextCompat.getColor(holder.itemView.context, R.color.diff_medium)
            QuestDifficulty.HARD -> ContextCompat.getColor(holder.itemView.context, R.color.diff_hard)
            QuestDifficulty.HELL -> ContextCompat.getColor(holder.itemView.context, R.color.diff_hell)
        }
        holder.tvDifficulty.setTextColor(diffColor)

        // Specialization Tag
        if (!quest.specializationName.isNullOrEmpty()) {
            holder.tvSpecialization.visibility = View.VISIBLE
            holder.tvSpecialization.text = "📚 ${quest.specializationName}"
        } else {
            holder.tvSpecialization.visibility = View.GONE
        }

        // Timer Requirement Tag (Hell Mode doubles required timer duration!)
        val timerTargetMins = if (isHellMode && quest.timerMinutesRequired > 0) {
            quest.timerMinutesRequired * 2
        } else {
            quest.timerMinutesRequired
        }

        if (timerTargetMins > 0) {
            holder.tvTimerReq.visibility = View.VISIBLE
            holder.tvTimerReq.text = if (isHellMode) "⏱️ $timerTargetMins Mnt (Hell 2x)" else "⏱️ $timerTargetMins Mnt"
            holder.btnStartTimer.visibility = View.VISIBLE
        } else {
            holder.tvTimerReq.visibility = View.GONE
            holder.btnStartTimer.visibility = View.GONE
        }

        // Completed State Styling
        if (quest.isCompleted) {
            holder.root.setBackgroundResource(R.drawable.bg_rpg_card)
            holder.btnComplete.text = "SELESAI ✓"
            holder.btnComplete.isEnabled = false
            holder.btnComplete.setBackgroundColor(Color.GRAY)
            holder.btnStartTimer.visibility = View.GONE
        } else {
            if (isHellMode || quest.difficulty == QuestDifficulty.HELL) {
                holder.root.setBackgroundResource(R.drawable.bg_hell_card)
            } else {
                holder.root.setBackgroundResource(R.drawable.bg_rpg_card)
            }
            holder.btnComplete.text = "Selesai"
            holder.btnComplete.isEnabled = true
            holder.btnComplete.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.rpg_hp_green)
            )
        }

        holder.btnComplete.setOnClickListener { onCompleteClick(quest) }
        holder.btnStartTimer.setOnClickListener { onStartTimerClick(quest) }
        holder.btnDelete.setOnClickListener { onDeleteClick(quest) }
    }

    override fun getItemCount(): Int = quests.size

    fun updateData(newQuests: List<Quest>, hellMode: Boolean) {
        this.quests = newQuests.toMutableList()
        this.isHellMode = hellMode
        notifyDataSetChanged()
    }
}
