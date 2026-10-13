// Says which folder (package) this file belongs to.
package com.vu.nit3213app

// Bundle: a box of saved values Android passes to screens.
import android.os.Bundle
// enableEdgeToEdge: lets the app draw behind the status bar and bottom bar.
import androidx.activity.enableEdgeToEdge
// AppCompatActivity: the base class for an Activity with a toolbar.
import androidx.appcompat.app.AppCompatActivity
// ViewCompat: helper for listening to system area sizes (insets).
import androidx.core.view.ViewCompat
// WindowInsetsCompat: describes system areas such as the status bar and keyboard.
import androidx.core.view.WindowInsetsCompat
// updatePadding: a shortcut for changing only some sides of a view's padding.
import androidx.core.view.updatePadding
// NavController: the object that moves between screens.
import androidx.navigation.NavController
// NavHostFragment: the container that swaps the screens in and out.
import androidx.navigation.fragment.NavHostFragment
// AppBarConfiguration: says which screens are "top level" (no back arrow).
import androidx.navigation.ui.AppBarConfiguration
// navigateUp: handles the toolbar's back arrow.
import androidx.navigation.ui.navigateUp
// setupActionBarWithNavController: links the toolbar to the screen changes.
import androidx.navigation.ui.setupActionBarWithNavController
// ActivityMainBinding: generated from activity_main.xml, holds the toolbar and screen container.
import com.vu.nit3213app.databinding.ActivityMainBinding
// AndroidEntryPoint: the Hilt label that lets Hilt inject into this class.
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
 */
// Lets Hilt work here.
// Why: Hilt can only inject into a Fragment if the Activity holding it is also marked.
@AndroidEntryPoint
// Creates the main Activity class, the one window that holds every screen.
class MainActivity : AppCompatActivity() {

    // Will hold the views from activity_main.xml. "lateinit" means it's set later, in onCreate.
    private lateinit var binding: ActivityMainBinding
    // Will hold the object that moves between screens. Also set in onCreate.
    private lateinit var navController: NavController
    // Will hold the list of top-level screens.
    // Why a field: onSupportNavigateUp() needs the same one later.
    private lateinit var appBarConfiguration: AppBarConfiguration

    // Android calls this once when the Activity is created.
    override fun onCreate(savedInstanceState: Bundle?) {
        // Lets the app draw behind the status bar and bottom bar.
        // Why before super: the window settings must be set before anything is drawn.
        enableEdgeToEdge()
        // Lets the parent Activity class do its own set-up.
        super.onCreate(savedInstanceState)

        // Builds the views from activity_main.xml and keeps them in binding.
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Puts those views on the screen.
        setContentView(binding.root)
        // Makes our MaterialToolbar act as the app's top bar, so it shows titles and the back arrow.
        setSupportActionBar(binding.toolbar)

        // Adds padding so nothing hides behind the status bar or keyboard (see below).
        applyWindowInsets()
        // Connects the toolbar to the screen changes (see below).
        setUpNavigation()
    }

    /**
     * Finds the NavController and links it to the toolbar, so the toolbar title
     * and up arrow update by themselves whenever the current screen changes.
     */
    private fun setUpNavigation() {
        // Finds the screen container declared in activity_main.xml.
        val navHostFragment =
            // Looks it up by ID, then treats it as a NavHostFragment.
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // Gets the object that moves between screens from that container.
        navController = navHostFragment.navController

        // Lists the screens that are "top level", so they get no back arrow.
        // Why: after signing in, a back arrow on the Dashboard pointing to Login makes no sense.
        appBarConfiguration = AppBarConfiguration(
            // Login and Dashboard are top level. Details isn't, so it gets the back arrow.
            setOf(R.id.loginFragment, R.id.dashboardFragment)
        )
        // Links the toolbar to the screen changes. Titles now come from nav_graph.xml.
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Makes the toolbar's up arrow go back through the navigation graph.
     */
    // Runs when the toolbar's back arrow is tapped. Goes back one screen, or does the default if it can't.
    override fun onSupportNavigateUp(): Boolean =
        // The || means "or": try the NavController first, and if it can't go back, use the default.
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    /**
     * Pads the toolbar below the status bar, and pads the screen content above the
     * navigation bar or the on-screen keyboard, whichever is taller. This stops
     * the keyboard covering the Sign in button.
     */
    private fun applyWindowInsets() {
        // Listens for changes in system area sizes, such as the keyboard opening or the phone rotating.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            // Measures the status bar, bottom bar and camera notch.
            val systemBars = insets.getInsets(
                // The "or" combines them, so all three are measured together.
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            // Measures the keyboard. Its height is 0 when it's closed.
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())

            // Pads the toolbar so it sits below the status bar.
            binding.appBarLayout.updatePadding(
                // Left padding clears a notch when the phone is sideways.
                left = systemBars.left,
                // Top padding pushes the toolbar below the clock and battery icons.
                top = systemBars.top,
                // Right padding clears a notch on the other side.
                right = systemBars.right
            )
            // Pads the screen area so its content stays clear of the edges.
            binding.navHostFragment.updatePadding(
                // Left side, for a notch.
                left = systemBars.left,
                // Right side, for a notch.
                right = systemBars.right,
                // Bottom: whichever is taller, the bottom bar or the keyboard.
                // Why: so the keyboard never covers the Sign in button.
                bottom = maxOf(systemBars.bottom, keyboard.bottom)
            )
            // Says the insets are dealt with, so child views don't add the padding a second time.
            WindowInsetsCompat.CONSUMED
        }
    }
}
