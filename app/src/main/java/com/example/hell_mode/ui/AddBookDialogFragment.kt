package com.example.hell_mode.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.hell_mode.R
import com.example.hell_mode.databinding.DialogAddBookBinding
import com.example.hell_mode.model.FinancialBook

class AddBookDialogFragment(
    private val onBookAdded: (FinancialBook) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddBookBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancelBook.setOnClickListener { dismiss() }

        binding.btnSaveBook.setOnClickListener {
            val name = binding.etBookName.text.toString().trim()
            val desc = binding.etBookDesc.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Nama Buku wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val book = FinancialBook(
                name = name,
                description = desc
            )

            onBookAdded(book)
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
