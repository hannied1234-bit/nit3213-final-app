package com.vu.nit3213app.ui.dashboard

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.vu.nit3213app.R
import com.vu.nit3213app.databinding.FragmentDashboardBinding
import com.vu.nit3213app.domain.model.Entity
import com.vu.nit3213app.ui.common.collectWhenStarted
import com.vu.nit3213app.ui.details.DetailsFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * The Dashboard screen: a RecyclerView of entity summaries.
 * Tapping an item opens the [DetailsFragment] for that entity.
 *
 * The Fragment holds no data of its own. It watches [DashboardViewModel.uiState]
 * and redraws the screen from whatever state arrives, so the screen always
 * matches the ViewModel. After a rotation the new Fragment gets the same
 * ViewModel back and draws the list that was already loaded, with no second
 * network call.
 *
 * Passing the layout ID to the Fragment constructor lets Fragment inflate the
 * view itself, so there's no onCreateView override.
 */
@AndroidEntryPoint
class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    // by viewModels() ties the ViewModel to this Fragment. Because of
    // @AndroidEntryPoint, Hilt builds it, injects the repository and fills its
    // SavedStateHandle with the Safe Args arguments (the keypass).
    private val viewModel: DashboardViewModel by viewModels()

    // The binding only exists between onViewCreated and onDestroyView. Holding it
    // in a nullable field and clearing it in onDestroyView avoids a memory leak.
    // The non-null "binding" getter saves writing _binding!! everywhere, but it
    // will throw if it's used after the view is gone.
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // Created once per Fragment rather than once per view, so the adapter
    // survives the view being rebuilt. ::openDetails passes the function itself
    // as the click handler, which keeps the adapter unaware of what a tap does.
    private val entityAdapter = EntityAdapter(onEntityClick = ::openDetails)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // bind() wraps the view Fragment already inflated instead of
        // inflating a second copy.
        _binding = FragmentDashboardBinding.bind(view)

        // The LinearLayoutManager is set in the XML layout, so only the adapter
        // needs connecting here.
        binding.entityRecyclerView.adapter = entityAdapter

        // "Try again" just asks the ViewModel to load again. The ViewModel
        // switches to Loading and render() swaps the error for the spinner.
        binding.retryButton.setOnClickListener { viewModel.loadEntities() }

        setUpLogOutMenu()

        // Starts drawing states. The first value arrives straight away, because
        // a StateFlow always holds one.
        collectWhenStarted(viewModel.uiState, ::render)
    }

    /**
     * Draws one [DashboardUiState] onto the screen.
     *
     * Every visibility flag is set on every call, whatever the state. That way
     * a view from an old state can't stay visible by mistake. Going from Error
     * to Success, for example, also hides the error layout.
     */
    private fun render(state: DashboardUiState) {
        // Exactly one of these four views is visible at a time. They're stacked
        // in a FrameLayout in fragment_dashboard.xml.
        binding.progressIndicator.isVisible = state is DashboardUiState.Loading
        binding.errorLayout.isVisible = state is DashboardUiState.Error
        // A successful but empty reply gets its own message, since an empty
        // list with no explanation looks like the app is broken.
        binding.contentLayout.isVisible =
            state is DashboardUiState.Success && state.entities.isNotEmpty()
        binding.emptyText.isVisible =
            state is DashboardUiState.Success && state.entities.isEmpty()

        // The visibility above is shared. This "when" fills in the content only
        // the current state has.
        when (state) {
            is DashboardUiState.Loading -> Unit
            is DashboardUiState.Success -> {
                // getQuantityString picks "1 item" or "7 items" from the plurals
                // resource. The total is passed twice. The first picks the
                // plural form and the second fills the %d in the text.
                binding.entityCountText.text = resources.getQuantityString(
                    R.plurals.entity_count,
                    state.total,
                    state.total
                )
                // ListAdapter compares the new list with the old one on a
                // background thread and only redraws rows that changed.
                entityAdapter.submitList(state.entities)
            }

            // The ViewModel sends a string resource ID, so the text is looked
            // up here, where a Context is available.
            is DashboardUiState.Error -> binding.errorMessageText.setText(state.messageRes)
        }
    }

    /**
     * Opens the Details screen for the tapped [entity].
     *
     * The Safe Args Directions class takes the Entity as a typed parameter, so
     * passing the wrong type is a compile error instead of a crash at runtime.
     */
    private fun openDetails(entity: Entity) {
        navigateSafely(DashboardFragmentDirections.actionDashboardFragmentToDetailsFragment(entity))
    }

    /**
     * Adds a "Log out" item to the toolbar while this screen is showing.
     * Passing viewLifecycleOwner removes the item automatically when the user
     * leaves the Dashboard.
     *
     * MenuProvider replaces the old setHasOptionsMenu() and
     * onCreateOptionsMenu() pair, which are deprecated. The toolbar belongs to
     * MainActivity, so the provider is added to the Activity.
     */
    private fun setUpLogOutMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_dashboard, menu)
            }

            // Returning true tells the system this item was handled here.
            // False lets other providers, or the up arrow, handle it instead.
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return if (menuItem.itemId == R.id.action_log_out) {
                    // The action pops the Dashboard off the back stack (see
                    // nav_graph.xml), so pressing Back on Login closes the app
                    // instead of returning to the signed-in list.
                    navigateSafely(DashboardFragmentDirections.actionDashboardFragmentToLoginFragment())
                    true
                } else {
                    false
                }
            }
            // RESUMED means the menu item only shows while this screen is in
            // front. It disappears when Details opens on top.
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * Only moves to another screen if the Dashboard is still the current one.
     * Without this check, tapping two items very quickly would try to move twice
     * and the second attempt would crash, because the action no longer exists
     * from the Details screen.
     */
    private fun navigateSafely(directions: NavDirections) {
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.dashboardFragment) {
            navController.navigate(directions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // The adapter outlives the view, so it is detached to avoid leaking the
        // old RecyclerView.
        binding.entityRecyclerView.adapter = null
        // Cleared last, because the line above still needs the binding. When
        // the user opens Details, this Fragment stays on the back stack without
        // a view, and holding the binding would keep the old views in memory.
        _binding = null
    }
}
