package com.example.hell_mode.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.hell_mode.R
import com.example.hell_mode.data.GameRepository
import com.example.hell_mode.databinding.DialogAddQuestBinding
import com.example.hell_mode.model.Quest
import com.example.hell_mode.model.QuestDifficulty

class AddQuestDialogFragment(
    private val presetDueDateMillis: Long = System.currentTimeMillis(),
    private val onQuestAdded: (Quest) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddQuestBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: GameRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddQuestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = GameRepository(requireContext())

        // Category Spinner
        val categories = listOf("Olahraga", "Belajar", "Kebiasaan", "Rutinitas", "Boss Quest")
        val catAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerQuestCategory.adapter = catAdapter

        // Specializations Spinner
        val specs = repository.getSpecializations()
        val specNames = mutableListOf("Tidak Ada")
        specNames.addAll(specs.map { it.name })
        val specAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, specNames)
        specAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerQuestSpec.adapter = specAdapter

        // Difficulty Spinner
        val difficulties = QuestDifficulty.entries.map { it.displayName }
        val diffAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, difficulties)
        diffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerQuestDifficulty.adapter = diffAdapter

        binding.btnCancelQuest.setOnClickListener { dismiss() }

        binding.btnSaveQuest.setOnClickListener {
            val title = binding.etQuestTitle.text.toString().trim()
            val desc = binding.etQuestDesc.text.toString().trim()
            val timerStr = binding.etQuestTimer.text.toString().trim()
            val timerMins = timerStr.toIntOrNull() ?: 0

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Judul Quest tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val catPos = binding.spinnerQuestCategory.selectedItemPosition
            val selectedCategory = categories.getOrElse(catPos) { "Belajar" }

            val specPos = binding.spinnerQuestSpec.selectedItemPosition
            var linkedSpecId: String? = null
            var linkedSpecName: String? = null
            if (specPos > 0 && specPos - 1 in specs.indices) {
                val selectedSpec = specs[specPos - 1]
                linkedSpecId = selectedSpec.id
                linkedSpecName = selectedSpec.name
            }

            val diffPos = binding.spinnerQuestDifficulty.selectedItemPosition
            val difficulty = QuestDifficulty.entries.getOrElse(diffPos) { QuestDifficulty.EASY }

            val isHellExclusive = difficulty == QuestDifficulty.HELL

            val quest = Quest(
                title = title,
                description = desc,
                category = selectedCategory,
                specializationId = linkedSpecId,
                specializationName = linkedSpecName,
                difficulty = difficulty,
                expReward = difficulty.baseExp,
                goldReward = difficulty.baseGold,
                timerMinutesRequired = timerMins,
                dueDateMillis = presetDueDateMillis,
                isHellExclusive = isHellExclusive
            )

            onQuestAdded(quest)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_rpg_card)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
