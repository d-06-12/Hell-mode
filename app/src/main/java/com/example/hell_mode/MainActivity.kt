package com.example.hell_mode

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.hell_mode.databinding.ActivityMainBinding
import com.example.hell_mode.ui.CalendarFragment
import com.example.hell_mode.ui.FinancialFragment
import com.example.hell_mode.ui.QuestListFragment
import com.example.hell_mode.ui.ShopFragment
import com.example.hell_mode.ui.SpecializationFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val questListFragment = QuestListFragment()
    private val specializationFragment = SpecializationFragment()
    private val calendarFragment = CalendarFragment()
    private val financialFragment = FinancialFragment()
    private val shopFragment = ShopFragment()

    private var activeFragment: Fragment = questListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupFragments()
        setupBottomNavigation()
    }

    private fun setupFragments() {
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment_container, shopFragment, "SHOP").hide(shopFragment)
            add(R.id.fragment_container, financialFragment, "FINANCIAL").hide(financialFragment)
            add(R.id.fragment_container, calendarFragment, "CALENDAR").hide(calendarFragment)
            add(R.id.fragment_container, specializationFragment, "SPEC").hide(specializationFragment)
            add(R.id.fragment_container, questListFragment, "QUESTS")
        }.commit()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_quests -> {
                    switchFragment(questListFragment)
                    questListFragment.refreshData()
                    true
                }
                R.id.nav_specialization -> {
                    switchFragment(specializationFragment)
                    specializationFragment.refreshData()
                    true
                }
                R.id.nav_calendar -> {
                    switchFragment(calendarFragment)
                    calendarFragment.refreshData()
                    true
                }
                R.id.nav_financial -> {
                    switchFragment(financialFragment)
                    financialFragment.refreshData()
                    true
                }
                R.id.nav_shop -> {
                    switchFragment(shopFragment)
                    shopFragment.refreshData()
                    true
                }
                else -> false
            }
        }
    }

    private fun switchFragment(target: Fragment) {
        if (activeFragment != target) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit()
            activeFragment = target
        }
    }

    fun navigateToSpecializationTab(specId: String?, timerMinutes: Int) {
        binding.bottomNavigation.selectedItemId = R.id.nav_specialization
        specializationFragment.prefillTimerForSpecialization(specId, timerMinutes)
    }

    fun navigateToFinancialTab() {
        binding.bottomNavigation.selectedItemId = R.id.nav_financial
        financialFragment.refreshData()
    }
}
