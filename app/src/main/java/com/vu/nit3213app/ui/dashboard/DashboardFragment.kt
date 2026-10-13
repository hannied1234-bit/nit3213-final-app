// Says which folder (package) this file belongs to.
package com.vu.nit3213app.ui.dashboard

// Bundle: a box of saved values Android passes to screens.
import android.os.Bundle
// Menu: the list of items in the toolbar.
import android.view.Menu
// MenuInflater: builds menu items from an XML file.
import android.view.MenuInflater
// MenuItem: one item in the toolbar menu, such as "Log out".
import android.view.MenuItem
// View: the base class for everything you see on screen.
import android.view.View
// MenuProvider: the modern way for a screen to add items to the toolbar.
import androidx.core.view.MenuProvider
// isVisible: a shortcut for showing or hiding a view with true or false.
import androidx.core.view.isVisible
// Fragment: the base class for a screen inside the Activity.
import androidx.fragment.app.Fragment
// viewModels: the "by viewModels()" helper that creates the ViewModel.
import androidx.fragment.app.viewModels
// Lifecycle: describes the stages a screen goes through, such as RESUMED.
import androidx.lifecycle.Lifecycle
// NavDirections: an action that says which screen to go to, with any values attached.
import androidx.navigation.NavDirections
// findNavController: finds the object that moves between screens.
import androidx.navigation.fragment.findNavController
// R: the generated list of IDs for layouts, strings and images.
import com.vu.nit3213app.R
// FragmentDashboardBinding: generated from fragment_dashboard.xml, holds every view on this screen.
import com.vu.nit3213app.databinding.FragmentDashboardBinding
// Entity: one tidy item from the list.
import com.vu.nit3213app.domain.model.Entity
// collectWhenStarted: our helper that watches the ViewModel's state.
import com.vu.nit3213app.ui.common.collectWhenStarted
// DetailsFragment: the next screen. Imported so the comment below can link to it.
import com.vu.nit3213app.ui.details.DetailsFragment
// AndroidEntryPoint: the Hilt label that lets Hilt inject into this class.
import dagger.hilt.android.AndroidEntryPoint

/**
 * The Dashboard screen: a RecyclerView of entity summaries.
 * Tapping an item opens the [DetailsFragment] for that entity.
 *
 * The Fragment holds no data of its own. It watches [DashboardViewModel.uiState]
 * and redraws the screen from whatever state arrives. After a rotation the new
 * Fragment gets the same ViewModel back and draws the list that was already
 * loaded, with no second network call.
 */
// Tells Hilt it's allowed to deliver things (the ViewModel) to this screen.
@AndroidEntryPoint
// Creates the Dashboard screen class, built from fragment_dashboard.xml.
class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    // Gets this screen's ViewModel. Hilt builds it and gives it the repository and the keypass.
    private val viewModel: DashboardViewModel by viewModels()

    // Holds the views of this screen. Null until the views are built.
    private var _binding: FragmentDashboardBinding? = null
    // A shortcut that returns _binding without the null check. Crashes if used after the views are gone.
    private val binding get() = _binding!!

    // Creates the list adapter once. ::openDetails is the function to run when an item is tapped.
    // Why here and not in onViewCreated: so the adapter survives the views being rebuilt.
    private val entityAdapter = EntityAdapter(onEntityClick = ::openDetails)

    // Android calls this once the screen's views have been built.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Lets the parent Fragment class do its own set-up first.
        super.onViewCreated(view, savedInstanceState)
        // Connects the binding to the views that were just built.
        _binding = FragmentDashboardBinding.bind(view)

        // Gives the list its adapter, which creates and fills the rows.
        // Why only the adapter: the layout manager (vertical list) is already set in the XML.
        binding.entityRecyclerView.adapter = entityAdapter
        // When "Try again" is tapped, ask the ViewModel to load the list again.
        binding.retryButton.setOnClickListener { viewModel.loadEntities() }

        // Adds the "Log out" item to the toolbar (see below).
        setUpLogOutMenu()
        // Starts watching the ViewModel's state. Every time it changes, render() redraws the screen.
        collectWhenStarted(viewModel.uiState, ::render)
    }

    /**
     * Draws one [DashboardUiState] onto the screen. Every view is set on every
     * call, so nothing from an old state can stay visible by mistake.
     */
    private fun render(state: DashboardUiState) {
        // Shows the spinning circle only while loading.
        binding.progressIndicator.isVisible = state is DashboardUiState.Loading
        // Shows the error message and "Try again" button only when there's an error.
        binding.errorLayout.isVisible = state is DashboardUiState.Error
        // Shows the list only when loading worked and there's at least one item.
        binding.contentLayout.isVisible =
            // "is" checks the state type, and && means both must be true.
            state is DashboardUiState.Success && state.entities.isNotEmpty()
        // Shows the "nothing here" message when loading worked but the list is empty.
        binding.emptyText.isVisible =
            // Success, and the list has no items.
            state is DashboardUiState.Success && state.entities.isEmpty()

        // Now fills in the content that only the current state has.
        when (state) {
            // Loading: nothing extra to fill in. Unit means "do nothing".
            is DashboardUiState.Loading -> Unit
            // Success: fill in the count and the list.
            is DashboardUiState.Success -> {
                // Sets the count text, such as "7 items" or "1 item".
                binding.entityCountText.text = resources.getQuantityString(
                    // The plurals entry in strings.xml that has "item" and "items".
                    R.plurals.entity_count,
                    // This number picks the singular or plural form.
                    state.total,
                    // This number fills in the %d in the text.
                    state.total
                )
                // Hands the new list to the adapter, which redraws only the rows that changed.
                entityAdapter.submitList(state.entities)
            }

            // Error: put the error message text on screen.
            is DashboardUiState.Error -> binding.errorMessageText.setText(state.messageRes)
        }
    }

    /**
     * Opens the Details screen for the tapped [entity].
     */
    // Runs when a card in the list is tapped.
    private fun openDetails(entity: Entity) {
        // Builds the "go to Details" action with the tapped Entity attached, then goes there safely.
        navigateSafely(DashboardFragmentDirections.actionDashboardFragmentToDetailsFragment(entity))
    }

    /**
     * Adds a "Log out" item to the toolbar while this screen is showing.
     * Passing viewLifecycleOwner removes the item automatically when the user
     * leaves the Dashboard.
     */
    private fun setUpLogOutMenu() {
        // Adds a menu provider to the Activity, since the toolbar belongs to the Activity.
        requireActivity().addMenuProvider(object : MenuProvider {
            // Android calls this to build the menu.
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Builds the "Log out" item from res/menu/menu_dashboard.xml.
                menuInflater.inflate(R.menu.menu_dashboard, menu)
            }

            // Android calls this when a menu item is tapped.
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Checks whether the tapped item is "Log out".
                return if (menuItem.itemId == R.id.action_log_out) {
                    // Goes back to the Login screen.
                    // Why Back then exits: nav_graph.xml removes the Dashboard from the history.
                    navigateSafely(DashboardFragmentDirections.actionDashboardFragmentToLoginFragment())
                    // Returns true to say "handled".
                    true
                // Any other item.
                } else {
                    // Returns false to say "not handled here".
                    false
                }
            }
        // viewLifecycleOwner and RESUMED: the item only shows while this screen is in front.
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * Only moves to another screen if the Dashboard is still the current one.
     * Without this check, tapping two items very quickly would try to move twice
     * and the second attempt would crash.
     */
    private fun navigateSafely(directions: NavDirections) {
        // Gets the object that moves between screens.
        val navController = findNavController()
        // Checks that we're still on the Dashboard.
        if (navController.currentDestination?.id == R.id.dashboardFragment) {
            // We are, so perform the move.
            navController.navigate(directions)
        }
    }

    // Android calls this when the screen's views are destroyed.
    override fun onDestroyView() {
        // Lets the parent Fragment class do its own clean-up first.
        super.onDestroyView()
        // Unhooks the adapter from the list.
        // Why: the adapter outlives the views, and holding the old list would leak memory.
        binding.entityRecyclerView.adapter = null
        // Forgets the views so they can be removed from memory.
        // Why last: the line above still needed the binding.
        _binding = null
    }
}
