package com.example.hell_mode.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.hell_mode.R
import com.example.hell_mode.databinding.DialogAddSpecializationBinding
import com.example.hell_mode.model.Specialization

class AddSpecializationDialogFragment(
    private val existingSpec: Specialization? = null,
    private val onSpecSaved: (Specialization) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddSpecializationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddSpecializationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = listOf("Akademik", "Skill", "Fisik", "Kreatif", "Bahasa")
        val catAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSpecCategory.adapter = catAdapter

        // Pre-fill if editing existing spec
        existingSpec?.let { spec ->
            binding.etSpecName.setText(spec.name)
            val pos = categories.indexOf(spec.category)
            if (pos != -1) binding.spinnerSpecCategory.setSelection(pos)
        }

        binding.btnCancelSpec.setOnClickListener { dismiss() }

        binding.btnSaveSpec.setOnClickListener {
            val name = binding.etSpecName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Nama mata pelajaran wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val catPos = binding.spinnerSpecCategory.selectedItemPosition
            val selectedCat = categories.getOrElse(catPos) { "Akademik" }

            val colors = listOf("#3498DB", "#2ECC71", "#E67E22", "#E74C3C", "#9B59B6", "#00E5FF")
            val currentColor = existingSpec?.colorHex ?: colors.random()

            val specToSave = existingSpec?.copy(
                name = name,
                category = selectedCat,
                colorHex = currentColor
            ) ?: Specialization(
                name = name,
                category = selectedCat,
                colorHex = currentColor
            )

            onSpecSaved(specToSave)
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
