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
import com.example.hell_mode.databinding.FragmentFinancialBinding
import com.example.hell_mode.model.TransactionType
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Locale

class FinancialFragment : Fragment() {

    private var _binding: FragmentFinancialBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: GameRepository
    private lateinit var transactionAdapter: TransactionAdapter
    private var selectedFilter: String = "Semua"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinancialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = GameRepository(requireContext())

        setupRecyclerView()
        setupFilterChips()

        binding.btnAddIncome.setOnClickListener {
            val dialog = AddTransactionDialogFragment(presetType = TransactionType.INCOME) { newTrans ->
                repository.addRealMoneyTransaction(newTrans)
                refreshData()
                Snackbar.make(binding.root, "Pemasukan +${formatRupiah(newTrans.amount)} dicatat!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.rpg_hp_green))
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    .show()
            }
            dialog.show(parentFragmentManager, "AddIncomeDialog")
        }

        binding.btnAddExpense.setOnClickListener {
            val dialog = AddTransactionDialogFragment(presetType = TransactionType.EXPENSE) { newTrans ->
                repository.addRealMoneyTransaction(newTrans)
                refreshData()
                Snackbar.make(binding.root, "Pengeluaran -${formatRupiah(newTrans.amount)} dicatat!", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.rpg_hp_red))
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    .show()
            }
            dialog.show(parentFragmentManager, "AddExpenseDialog")
        }

        refreshData()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            transactions = emptyList(),
            onEditClick = { trans ->
                val dialog = AddTransactionDialogFragment(existingTransaction = trans) { updatedTrans ->
                    repository.updateRealMoneyTransaction(updatedTrans)
                    refreshData()
                    Toast.makeText(requireContext(), "Transaksi diperbarui!", Toast.LENGTH_SHORT).show()
                }
                dialog.show(parentFragmentManager, "EditTransDialog")
            },
            onDeleteClick = { trans ->
                repository.deleteRealMoneyTransaction(trans.id)
                refreshData()
                Toast.makeText(requireContext(), "Transaksi dihapus!", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvFinancialLedger.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFinancialLedger.adapter = transactionAdapter
    }

    private fun setupFilterChips() {
        binding.chipGroupFinancial.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedFilter = if (checkedIds.isEmpty()) {
                "Semua"
            } else {
                when (checkedIds[0]) {
                    R.id.chip_trans_income -> "Pemasukan"
                    R.id.chip_trans_expense -> "Pengeluaran"
                    else -> "Semua"
                }
            }
            refreshTransactionList()
        }
    }

    fun refreshData() {
        if (_binding == null) return
        updateAccountingCalculations()
    }

    private fun updateAccountingCalculations() {
        // Real-time Accounting: Saldo Akhir = Total Pemasukan - Total Pengeluaran
        val totalIncome = repository.getTotalIncome()
        val totalExpense = repository.getTotalExpense()
        val netBalance = totalIncome - totalExpense

        binding.tvFinancialTotalIncome.text = "+ ${formatRupiah(totalIncome)}"
        binding.tvFinancialTotalExpense.text = "- ${formatRupiah(totalExpense)}"
        binding.tvFinancialNetBalance.text = formatRupiah(netBalance)

        val cashflowPrefix = if (netBalance >= 0) "+ " else ""
        binding.tvFinancialCashflow.text = "Arus Kas Bersih: $cashflowPrefix${formatRupiah(netBalance)}"

        if (netBalance < 0) {
            binding.tvFinancialNetBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.rpg_hp_red))
        } else {
            binding.tvFinancialNetBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.rpg_hp_green))
        }

        refreshTransactionList()
    }

    private fun refreshTransactionList() {
        var allTrans = repository.getRealMoneyTransactions()

        if (selectedFilter == "Pemasukan") {
            allTrans = allTrans.filter { it.type == TransactionType.INCOME }
        } else if (selectedFilter == "Pengeluaran") {
            allTrans = allTrans.filter { it.type == TransactionType.EXPENSE }
        }

        if (allTrans.isEmpty()) {
            binding.tvEmptyTransactions.visibility = View.VISIBLE
            binding.rvFinancialLedger.visibility = View.GONE
        } else {
            binding.tvEmptyTransactions.visibility = View.GONE
            binding.rvFinancialLedger.visibility = View.VISIBLE
            transactionAdapter.updateData(allTrans)
        }
    }

    private fun formatRupiah(amount: Long): String {
        val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return rupiahFormat.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
