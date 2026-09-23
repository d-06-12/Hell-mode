package com.example.hell_mode.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hell_mode.R
import com.example.hell_mode.data.GameRepository
import com.example.hell_mode.databinding.FragmentSpecializationBinding
import com.example.hell_mode.model.Specialization
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class SpecializationFragment : Fragment() {

    private var _binding: FragmentSpecializationBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: GameRepository
    private lateinit var specAdapter: SpecializationAdapter
    private var specializations: List<Specialization> = emptyList()
    private var selectedSpec: Specialization? = null

    // Timer state
    private var timer: CountDownTimer? = null
    private var initialTimeMillis: Long = 25 * 60 * 1000L
    private var timeLeftMillis: Long = initialTimeMillis
    private var isTimerRunning: Boolean = false
    private var isStopwatchMode: Boolean = false
    private var stopwatchSecondsElapsed: Long = 0
    private var stopwatchHandler: Handler? = null
    private var stopwatchRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpecializationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = GameRepository(requireContext())

        setupRecyclerView()
        setupTimerButtons()

        binding.btnAddSpecialization.setOnClickListener {
            val dialog = AddSpecializationDialogFragment { newSpec ->
                repository.addSpecialization(newSpec)
                refreshData()
                Toast.makeText(requireContext(), "Mata Pelajaran ditambahkan!", Toast.LENGTH_SHORT).show()
            }
            dialog.show(parentFragmentManager, "AddSpecDialog")
        }

        refreshData()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupRecyclerView() {
        specAdapter = SpecializationAdapter(
            specializations = emptyList(),
            onEditClick = { spec ->
                val dialog = AddSpecializationDialogFragment(existingSpec = spec) { updatedSpec ->
                    repository.updateSpecialization(updatedSpec)
                    refreshData()
                    Toast.makeText(requireContext(), "Mata Pelajaran diperbarui!", Toast.LENGTH_SHORT).show()
                }
                dialog.show(parentFragmentManager, "EditSpecDialog")
            },
            onDeleteClick = { spec ->
                repository.deleteSpecialization(spec.id)
                refreshData()
                Toast.makeText(requireContext(), "Mata Pelajaran dihapus!", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvSpecializations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSpecializations.adapter = specAdapter
    }

    private fun setupTimerButtons() {
        binding.btnTimer15.setOnClickListener { setTimerDuration(15) }
        binding.btnTimer25.setOnClickListener { setTimerDuration(25) }
        binding.btnTimer45.setOnClickListener { setTimerDuration(45) }
        binding.btnTimerStopwatch.setOnClickListener { setStopwatchMode() }

        binding.btnStartPauseTimer.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        binding.btnResetTimer.setOnClickListener { resetTimer() }

        binding.btnFinishTimer.setOnClickListener { completeTimerSession() }
    }

    private fun setTimerDuration(minutes: Int) {
        if (isTimerRunning) pauseTimer()
        isStopwatchMode = false
        initialTimeMillis = minutes * 60 * 1000L
        timeLeftMillis = initialTimeMillis
        updateTimerDisplay()
        updateRewardPreview()
    }

    private fun setStopwatchMode() {
        if (isTimerRunning) pauseTimer()
        isStopwatchMode = true
        stopwatchSecondsElapsed = 0
        binding.tvTimerDisplay.text = "00:00"
        updateRewardPreview()
    }

    private fun startTimer() {
        isTimerRunning = true
        binding.btnStartPauseTimer.text = "Pause"
        binding.btnStartPauseTimer.setBackgroundTintList(
            ContextCompat.getColorStateList(requireContext(), R.color.rpg_hp_red)
        )

        if (isStopwatchMode) {
            stopwatchHandler = Handler(Looper.getMainLooper())
            stopwatchRunnable = object : Runnable {
                override fun run() {
                    stopwatchSecondsElapsed++
                    val mins = stopwatchSecondsElapsed / 60
                    val secs = stopwatchSecondsElapsed % 60
                    binding.tvTimerDisplay.text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
                    updateRewardPreview()
                    stopwatchHandler?.postDelayed(this, 1000)
                }
            }
            stopwatchHandler?.postDelayed(stopwatchRunnable!!, 1000)
        } else {
            timer = object : CountDownTimer(timeLeftMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeftMillis = millisUntilFinished
                    updateTimerDisplay()
                }

                override fun onFinish() {
                    isTimerRunning = false
                    updateTimerDisplay()
                    completeTimerSession()
                }
            }.start()
        }
    }

    private fun pauseTimer() {
        isTimerRunning = false
        binding.btnStartPauseTimer.text = "Mulai Timer"
        binding.btnStartPauseTimer.setBackgroundTintList(
            ContextCompat.getColorStateList(requireContext(), R.color.rpg_gold)
        )

        timer?.cancel()
        stopwatchRunnable?.let { stopwatchHandler?.removeCallbacks(it) }
    }

    private fun resetTimer() {
        pauseTimer()
        if (isStopwatchMode) {
            stopwatchSecondsElapsed = 0
            binding.tvTimerDisplay.text = "00:00"
        } else {
            timeLeftMillis = initialTimeMillis
            updateTimerDisplay()
        }
        updateRewardPreview()
    }

    private fun completeTimerSession() {
        pauseTimer()
        val minutesStudied = if (isStopwatchMode) {
            (stopwatchSecondsElapsed / 60).coerceAtLeast(1)
        } else {
            val elapsed = initialTimeMillis - timeLeftMillis
            (elapsed / 60000L).coerceAtLeast(1)
        }

        val spec = selectedSpec
        if (spec == null) {
            Toast.makeText(requireContext(), "Pilih mata pelajaran dulu!", Toast.LENGTH_SHORT).show()
            return
        }

        val baseExp = (minutesStudied * 3).toInt()
        val leveledUp = repository.addExpToSpecialization(spec.id, minutesStudied, baseExp)

        val profile = repository.getPlayerProfile()
        // Hell mode rewards multiplier 1.5x
        val mult = if (profile.isHellMode) 1.5 else 1.0
        val expGained = (baseExp * mult).toInt()
        val goldGained = (minutesStudied * 2 * mult).toInt()

        val msg = if (leveledUp) {
            "🎉 LEVEL UP! $minutesStudied Mnt Fokus: +$expGained EXP & +$goldGained Gold!"
        } else {
            "Sesi Fokus Selesai! ($minutesStudied Mnt): +$expGained EXP & +$goldGained Gold!"
        }

        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.rpg_gold_dark))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            .show()

        resetTimer()
        refreshData()
    }

    private fun updateTimerDisplay() {
        val minutes = (timeLeftMillis / 1000) / 60
        val seconds = (timeLeftMillis / 1000) % 60
        binding.tvTimerDisplay.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun updateRewardPreview() {
        val minutes = if (isStopwatchMode) {
            (stopwatchSecondsElapsed / 60).coerceAtLeast(1)
        } else {
            initialTimeMillis / 60000L
        }
        val profile = repository.getPlayerProfile()
        val mult = if (profile.isHellMode) 1.5 else 1.0
        val exp = (minutes * 3 * mult).toInt()
        val gold = (minutes * 2 * mult).toInt()

        binding.tvTimerRewardPreview.text = "Reward ($minutes Mnt): +$exp EXP | +$gold Gold"
    }

    fun refreshData() {
        if (_binding == null) return

        specializations = repository.getSpecializations()
        specAdapter.updateData(specializations)

        // Setup Spinner
        val specNames = specializations.map { "${it.name} (Lvl ${it.level})" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, specNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSpecialization.adapter = adapter

        binding.spinnerSpecialization.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position in specializations.indices) {
                    selectedSpec = specializations[position]
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        if (specializations.isNotEmpty()) {
            selectedSpec = specializations[0]
        }

        updateTimerDisplay()
        updateRewardPreview()
    }

    fun prefillTimerForSpecialization(specId: String?, timerMinutes: Int) {
        if (specId != null) {
            val idx = specializations.indexOfFirst { it.id == specId }
            if (idx != -1) {
                binding.spinnerSpecialization.setSelection(idx)
                selectedSpec = specializations[idx]
            }
        }
        if (timerMinutes > 0) {
            setTimerDuration(timerMinutes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pauseTimer()
        _binding = null
    }
}
