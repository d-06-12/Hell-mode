package com.example.hell_mode.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hell_mode.MainActivity
import com.example.hell_mode.R
import com.example.hell_mode.data.GameRepository
import com.example.hell_mode.databinding.FragmentQuestListBinding
import com.example.hell_mode.model.Quest
import com.google.android.material.snackbar.Snackbar

class QuestListFragment : Fragment() {

    private var _binding: FragmentQuestListBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: GameRepository
    private lateinit var questAdapter: QuestAdapter
    private var selectedCategory: String = "Semua"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = GameRepository(requireContext())

        setupRecyclerView()
        setupCategoryChips()
        setupHellModeSwitch()

        binding.fabAddQuest.setOnClickListener {
            val dialog = AddQuestDialogFragment { newQuest ->
                repository.addQuest(newQuest)
                refreshData()
                Toast.makeText(requireContext(), "Quest berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            }
            dialog.show(parentFragmentManager, "AddQuestDialog")
        }

        checkOverduePenalties()
        refreshData()
    }

    override fun onResume() {
        super.onResume()
        checkOverduePenalties()
        refreshData()
    }

    private fun checkOverduePenalties() {
        val (hpLost, penalizedQuests) = repository.checkAndApplyOverdueQuestPenalties()
        if (hpLost > 0 && _binding != null) {
            val questTitles = penalizedQuests.joinToString(", ")
            val alertMsg = "⚠️ Penalti Tugas Belum Selesai! Darah berkurang -$hpLost HP ($questTitles)"
            Snackbar.make(binding.root, alertMsg, Snackbar.LENGTH_INDEFINITE)
                .setAction("Tutup") {}
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.rpg_hp_red))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                .show()
        }
    }

    private fun setupRecyclerView() {
        questAdapter = QuestAdapter(
            quests = mutableListOf(),
            isHellMode = false,
            onCompleteClick = { quest -> handleCompleteQuest(quest) },
            onStartTimerClick = { quest -> handleStartTimerForQuest(quest) },
            onDeleteClick = { quest ->
                repository.deleteQuest(quest.id)
                refreshData()
                Toast.makeText(requireContext(), "Quest dihapus", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvQuests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvQuests.adapter = questAdapter
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedCategory = if (checkedIds.isEmpty()) {
                "Semua"
            } else {
                when (checkedIds[0]) {
                    R.id.chip_olahraga -> "Olahraga"
                    R.id.chip_belajar -> "Belajar"
                    R.id.chip_kebiasaan -> "Kebiasaan"
                    R.id.chip_boss -> "Boss Quest"
                    else -> "Semua"
                }
            }
            refreshQuestList()
        }
    }

    private fun setupHellModeSwitch() {
        val profile = repository.getPlayerProfile()
        binding.switchHellMode.isChecked = profile.isHellMode

        binding.switchHellMode.setOnCheckedChangeListener { _, isChecked ->
            repository.setHellMode(isChecked)
            refreshData()
            val message = if (isChecked) "🔥 HELL MODE DIAKTIFKAN! Hadiah 1.5x & Waktu Timer 2x!" else "Mode Normal Diaktifkan"
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun handleCompleteQuest(quest: Quest) {
        val (leveledUp, message) = repository.completeQuest(quest.id)

        // Show reward snackbar
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), if (leveledUp) R.color.rpg_gold_dark else R.color.rpg_hp_green))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            .show()

        refreshData()
    }

    private fun handleStartTimerForQuest(quest: Quest) {
        val profile = repository.getPlayerProfile()
        val timerTarget = if (profile.isHellMode && quest.timerMinutesRequired > 0) quest.timerMinutesRequired * 2 else quest.timerMinutesRequired
        (activity as? MainActivity)?.navigateToSpecializationTab(quest.specializationId, timerTarget)
    }

    fun refreshData() {
        if (_binding == null) return

        val profile = repository.getPlayerProfile()

        // Player Header Info
        binding.tvPlayerTitle.text = profile.title
        binding.tvPlayerLevel.text = "Level ${profile.level}"
        binding.tvGoldSaldo.text = "${profile.saldo} Gold"
        binding.tvHpStatus.text = "${profile.hp} / ${profile.maxHp} HP"

        binding.progressExp.max = profile.maxExp
        binding.progressExp.progress = profile.currentExp.coerceAtMost(profile.maxExp)
        binding.tvExpProgressText.text = "${profile.currentExp} / ${profile.maxExp} EXP"

        // Hell mode visual theme styling
        if (profile.isHellMode) {
            binding.cardPlayerHeader.setBackgroundResource(R.drawable.bg_hell_header)
            binding.tvPlayerTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.hell_red))
            binding.tvHellModeBanner.visibility = View.VISIBLE
            binding.tvHellModeBanner.text = "🔥 HELL MODE AKTIF! Hadiah 1.5x Lipat & Target Waktu Timer 2x Lipat! Quest yang gagal akan mengurangi HP!"
        } else {
            binding.cardPlayerHeader.setBackgroundResource(R.drawable.bg_rpg_header)
            binding.tvPlayerTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.rpg_gold))
            binding.tvHellModeBanner.visibility = View.GONE
        }

        refreshQuestList()
    }

    private fun refreshQuestList() {
        val profile = repository.getPlayerProfile()
        var allQuests = repository.getQuests()

        if (selectedCategory != "Semua") {
            allQuests = allQuests.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }

        if (allQuests.isEmpty()) {
            binding.tvEmptyQuests.visibility = View.VISIBLE
            binding.rvQuests.visibility = View.GONE
        } else {
            binding.tvEmptyQuests.visibility = View.GONE
            binding.rvQuests.visibility = View.VISIBLE
            questAdapter.updateData(allQuests, profile.isHellMode)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
