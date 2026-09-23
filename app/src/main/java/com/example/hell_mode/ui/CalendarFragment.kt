package com.example.hell_mode.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hell_mode.MainActivity
import com.example.hell_mode.data.GameRepository
import com.example.hell_mode.databinding.FragmentCalendarBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: GameRepository
    private lateinit var questAdapter: QuestAdapter
    private var selectedCalendarMillis: Long = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = GameRepository(requireContext())

        setupRecyclerView()
        setupCalendar()

        binding.btnAddQuestForDate.setOnClickListener {
            val dialog = AddQuestDialogFragment(presetDueDateMillis = selectedCalendarMillis) { newQuest ->
                repository.addQuest(newQuest)
                refreshData()
                Toast.makeText(requireContext(), "Quest berhasil dijadwalkan!", Toast.LENGTH_SHORT).show()
            }
            dialog.show(parentFragmentManager, "AddQuestDialogCalendar")
        }

        refreshData()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupRecyclerView() {
        questAdapter = QuestAdapter(
            quests = mutableListOf(),
            isHellMode = false,
            onCompleteClick = { quest ->
                repository.completeQuest(quest.id)
                refreshData()
            },
            onStartTimerClick = { quest ->
                (activity as? MainActivity)?.navigateToSpecializationTab(quest.specializationId, quest.timerMinutesRequired)
            },
            onDeleteClick = { quest ->
                repository.deleteQuest(quest.id)
                refreshData()
            }
        )
        binding.rvCalendarQuests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCalendarQuests.adapter = questAdapter
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth, 12, 0, 0)
            selectedCalendarMillis = cal.timeInMillis
            refreshData()
        }
    }

    fun refreshData() {
        if (_binding == null) return

        val profile = repository.getPlayerProfile()
        val allQuests = repository.getQuests()

        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        val dateString = sdf.format(selectedCalendarMillis)
        binding.tvSelectedDateTitle.text = "Quest Tanggal: $dateString"

        // Filter quests by same day
        val calSelected = Calendar.getInstance().apply { timeInMillis = selectedCalendarMillis }
        val questsForDate = allQuests.filter { quest ->
            val calQuest = Calendar.getInstance().apply { timeInMillis = quest.dueDateMillis }
            calSelected.get(Calendar.YEAR) == calQuest.get(Calendar.YEAR) &&
                    calSelected.get(Calendar.DAY_OF_YEAR) == calQuest.get(Calendar.DAY_OF_YEAR)
        }

        if (questsForDate.isEmpty()) {
            binding.tvEmptyCalendarQuests.visibility = View.VISIBLE
            binding.rvCalendarQuests.visibility = View.GONE
        } else {
            binding.tvEmptyCalendarQuests.visibility = View.GONE
            binding.rvCalendarQuests.visibility = View.VISIBLE
            questAdapter.updateData(questsForDate, profile.isHellMode)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
