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
 * Why a single Activity? Each screen is a Fragment, so moving between them is a
 * Fragment transaction handled by the NavController. That's cheaper than starting
 * a new Activity, and it lets one toolbar stay on screen the whole time while
 * only the content underneath changes.
 *
 * @AndroidEntryPoint is required here because the Fragments it hosts use Hilt.
 * Hilt can only inject into a Fragment if the Activity holding it is also an
 * entry point, so leaving this off would crash the app the moment LoginFragment
 * asked for its ViewModel.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // lateinit because these can't be created until onCreate runs. The view
    // binding needs a LayoutInflater, and the NavController only exists once the
    // layout holding the NavHostFragment has been inflated.
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // Kept as a field because onSupportNavigateUp() needs the same configuration
    // that was given to the toolbar in setUpNavigation().
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        // Draws the app behind the status and navigation bars. The insets
        // listener below then adds padding so nothing is hidden behind them.
        // It's called before super.onCreate() so the window flags are set before
        // any view is attached to the window.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // ViewBinding builds every view in activity_main.xml and hands back a
        // typed object, so binding.toolbar is a MaterialToolbar with no casting
        // and no chance of a null from a mistyped ID.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Makes the MaterialToolbar act as this Activity's action bar. The theme
        // has no built-in action bar, so without this call the toolbar would be
        // a plain view that never shows screen titles or the up arrow.
        setSupportActionBar(binding.toolbar)

        applyWindowInsets()
        setUpNavigation()
    }

    /**
     * Finds the NavController and links it to the toolbar, so the toolbar title
     * and up arrow update by themselves whenever the current screen changes.
     */
    private fun setUpNavigation() {
        // The NavHostFragment is declared in XML with a FragmentContainerView.
        // findNavController() from an Activity can fail in onCreate with that
        // setup, so the NavController is read straight from the host Fragment.
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Login and Dashboard are "top level" screens, so they show no back arrow.
        // Details is not, so the toolbar shows an up arrow that returns to the list.
        // Without this set, the Dashboard would show an up arrow pointing back to
        // Login, which makes no sense once the user has signed in.
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.loginFragment, R.id.dashboardFragment)
        )

        // From here on the toolbar title comes from each destination's
        // android:label in nav_graph.xml, so no Fragment has to set it by hand.
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Makes the toolbar's up arrow go back through the navigation graph.
     *
     * The NavController gets the first chance to handle the tap. It returns
     * false only when there's nothing left to pop, and the call then falls back
     * to the default Activity behaviour.
     */
    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    /**
     * Pads the toolbar below the status bar, and pads the screen content above the
     * navigation bar or the on-screen keyboard, whichever is taller. This stops
     * the keyboard covering the Sign in button.
     *
     * Edge-to-edge is the default on Android 15, so this padding is what keeps
     * the toolbar from sliding under the clock and battery icons.
     */
    private fun applyWindowInsets() {
        // The listener runs every time the insets change, for example when the
        // keyboard opens or closes or the phone is rotated.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            // displayCutout is included so content also stays clear of a camera
            // notch when the phone is held sideways.
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())

            // The toolbar only needs top and side padding. Its bottom edge sits
            // against the content, not against a system bar.
            binding.appBarLayout.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right
            )

            // When the keyboard is open it's taller than the navigation bar, so
            // maxOf picks it. Closed, its inset is 0 and the navigation bar wins.
            binding.navHostFragment.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = maxOf(systemBars.bottom, keyboard.bottom)
            )

            // CONSUMED stops the insets reaching the child views. The padding
            // has already been applied here, and letting a child pad itself
            // again would leave a double gap.
            WindowInsetsCompat.CONSUMED
        }
    }
}
