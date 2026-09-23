package com.example.hell_mode.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.hell_mode.R
import com.example.hell_mode.data.GameRepository
import com.example.hell_mode.databinding.DialogAddTransactionBinding
import com.example.hell_mode.model.RealMoneyTransaction
import com.example.hell_mode.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionDialogFragment(
    private val existingTransaction: RealMoneyTransaction? = null,
    private val presetType: TransactionType = TransactionType.EXPENSE,
    private val onTransactionSaved: (RealMoneyTransaction) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: GameRepository
    private var selectedType: TransactionType = presetType
    private var selectedDateMillis: Long = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = GameRepository(requireContext())

        // Load Categories
        val categories = listOf("Gaji/Saku", "Makanan & Minuman", "Belanja & Hobi", "Transportasi", "Tagihan", "Investasi", "Lainnya")
        val catAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTransCategory.adapter = catAdapter

        // Pre-fill if editing existing transaction
        existingTransaction?.let { trans ->
            selectedType = trans.type
            selectedDateMillis = trans.timestamp
            binding.etTransTitle.setText(trans.title)
            binding.etTransAmount.setText(trans.amount.toString())

            val catPos = categories.indexOf(trans.category)
            if (catPos != -1) binding.spinnerTransCategory.setSelection(catPos)
        }

        updateDateButtonText()

        // Set initial toggle selection
        if (selectedType == TransactionType.INCOME) {
            binding.toggleType.check(R.id.btn_type_income)
        } else {
            binding.toggleType.check(R.id.btn_type_expense)
        }

        binding.btnTypeExpense.setOnClickListener { selectedType = TransactionType.EXPENSE }
        binding.btnTypeIncome.setOnClickListener { selectedType = TransactionType.INCOME }

        binding.btnSelectDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedCal = Calendar.getInstance()
                    selectedCal.set(year, month, dayOfMonth)
                    selectedDateMillis = selectedCal.timeInMillis
                    updateDateButtonText()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        binding.btnCancelTrans.setOnClickListener { dismiss() }

        binding.btnSaveTrans.setOnClickListener {
            val title = binding.etTransTitle.text.toString().trim()
            val amountStr = binding.etTransAmount.text.toString().trim()
            val amount = amountStr.toLongOrNull() ?: 0L

            if (title.isEmpty() || amount <= 0) {
                Toast.makeText(requireContext(), "Judul dan nominal wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val catPos = binding.spinnerTransCategory.selectedItemPosition
            val category = categories.getOrElse(catPos) { "Lainnya" }

            val transToSave = existingTransaction?.copy(
                title = title,
                amount = amount,
                type = selectedType,
                category = category,
                timestamp = selectedDateMillis
            ) ?: RealMoneyTransaction(
                title = title,
                amount = amount,
                type = selectedType,
                category = category,
                timestamp = selectedDateMillis
            )

            onTransactionSaved(transToSave)
            dismiss()
        }
    }

    private fun updateDateButtonText() {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        binding.btnSelectDate.text = "📅 Tanggal: ${sdf.format(selectedDateMillis)}"
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
