package com.example.hell_mode.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.hell_mode.R
import com.example.hell_mode.databinding.DialogAddShopItemBinding
import com.example.hell_mode.model.ShopItem

class AddShopItemDialogFragment(
    private val onItemAdded: (ShopItem) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddShopItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddShopItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancelShop.setOnClickListener { dismiss() }

        binding.btnSaveShop.setOnClickListener {
            val title = binding.etShopTitle.text.toString().trim()
            val desc = binding.etShopDesc.text.toString().trim()
            val costStr = binding.etShopCost.text.toString().trim()
            val cost = costStr.toIntOrNull() ?: 100

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Nama Hadiah tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item = ShopItem(
                title = title,
                description = desc.ifEmpty { "Hadiah kustom hasil kerja keras kamu!" },
                cost = cost,
                category = "Hadiah"
            )

            onItemAdded(item)
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
