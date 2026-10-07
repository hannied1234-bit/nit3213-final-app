package com.vu.nit3213app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.vu.nit3213app.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * The only Activity in the app. It hosts the NavHostFragment, which swaps the
 * Login, Dashboard and Details Fragments in and out (Lecture 4), and owns the
 * toolbar shared by all three screens.
 *
 * @AndroidEntryPoint is required here because the Fragments it hosts use Hilt.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        // Draws the app behind the status and navigation bars. The insets
        // listener below then adds padding so nothing is hidden behind them.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        applyWindowInsets()
        setUpNavigation()
    }

    private fun setUpNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Login and Dashboard are "top level" screens, so they show no back arrow.
        // Details is not, so the toolbar shows an up arrow that returns to the list.
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.loginFragment, R.id.dashboardFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /** Makes the toolbar's up arrow go back through the navigation graph. */
    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    /**
     * Pads the toolbar below the status bar, and pads the screen content above the
     * navigation bar or the on-screen keyboard, whichever is taller. This stops
     * the keyboard covering the Sign in button.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())

            binding.appBarLayout.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right
            )
            binding.navHostFragment.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = maxOf(systemBars.bottom, keyboard.bottom)
            )
            WindowInsetsCompat.CONSUMED
        }
    }
}
