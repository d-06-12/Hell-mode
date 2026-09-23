package com.example.hell_mode.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hell_mode.R
import com.example.hell_mode.data.GameRepository
import com.example.hell_mode.databinding.FragmentShopBinding
import com.google.android.material.snackbar.Snackbar

class ShopFragment : Fragment() {

    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: GameRepository
    private lateinit var shopAdapter: ShopAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = GameRepository(requireContext())

        setupRecyclerView()

        binding.btnAddShopItem.setOnClickListener {
            val dialog = AddShopItemDialogFragment { newItem ->
                repository.addShopItem(newItem)
                refreshData()
                Toast.makeText(requireContext(), "Hadiah custom ditambahkan!", Toast.LENGTH_SHORT).show()
            }
            dialog.show(parentFragmentManager, "AddShopDialog")
        }

        binding.btnHealHp.setOnClickListener {
            val profile = repository.getPlayerProfile()
            if (profile.saldo < 50) {
                Toast.makeText(requireContext(), "Gold tidak cukup! Butuh 50 Gold.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            profile.saldo -= 50
            profile.hp = (profile.hp + 30).coerceAtMost(profile.maxHp)
            repository.savePlayerProfile(profile)
            refreshData()
            Snackbar.make(binding.root, "❤️ HP Pulih +30 HP!", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.rpg_hp_green))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                .show()
        }

        refreshData()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupRecyclerView() {
        shopAdapter = ShopAdapter(
            items = emptyList(),
            onBuyClick = { item ->
                val (success, message) = repository.buyShopItem(item.id)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), if (success) R.color.rpg_gold_dark else R.color.rpg_hp_red))
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    .show()
                if (success) refreshData()
            }
        )
        binding.rvShopItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvShopItems.adapter = shopAdapter
    }

    fun refreshData() {
        if (_binding == null) return

        val profile = repository.getPlayerProfile()
        binding.tvShopSaldoDisplay.text = "${profile.saldo} Gold"

        val items = repository.getShopItems()
        shopAdapter.updateData(items)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
