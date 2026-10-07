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
import dagger.hilt.android.AndroidEntryPoint

/**
 * The Dashboard screen: a RecyclerView of entity summaries.
 * Tapping an item opens the Details screen for that entity.
 */
@AndroidEntryPoint
class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val viewModel: DashboardViewModel by viewModels()

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val entityAdapter = EntityAdapter(onEntityClick = ::openDetails)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDashboardBinding.bind(view)

        // The LinearLayoutManager is set in the XML layout, so only the adapter
        // needs connecting here.
        binding.entityRecyclerView.adapter = entityAdapter
        binding.retryButton.setOnClickListener { viewModel.loadEntities() }

        setUpLogOutMenu()
        collectWhenStarted(viewModel.uiState, ::render)
    }

    private fun render(state: DashboardUiState) {
        binding.progressIndicator.isVisible = state is DashboardUiState.Loading
        binding.errorLayout.isVisible = state is DashboardUiState.Error
        binding.contentLayout.isVisible =
            state is DashboardUiState.Success && state.entities.isNotEmpty()
        binding.emptyText.isVisible =
            state is DashboardUiState.Success && state.entities.isEmpty()

        when (state) {
            is DashboardUiState.Loading -> Unit
            is DashboardUiState.Success -> {
                binding.entityCountText.text = resources.getQuantityString(
                    R.plurals.entity_count,
                    state.total,
                    state.total
                )
                entityAdapter.submitList(state.entities)
            }

            is DashboardUiState.Error -> binding.errorMessageText.setText(state.messageRes)
        }
    }

    private fun openDetails(entity: Entity) {
        navigateSafely(DashboardFragmentDirections.actionDashboardFragmentToDetailsFragment(entity))
    }

    /**
     * Adds a "Log out" item to the toolbar while this screen is showing.
     * Passing viewLifecycleOwner removes the item automatically when the user
     * leaves the Dashboard.
     */
    private fun setUpLogOutMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_dashboard, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return if (menuItem.itemId == R.id.action_log_out) {
                    navigateSafely(DashboardFragmentDirections.actionDashboardFragmentToLoginFragment())
                    true
                } else {
                    false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * Only navigates if the Dashboard is still the current screen. Without this
     * check, tapping two items very quickly would try to navigate twice and the
     * second attempt would crash, because the action no longer exists from the
     * Details screen.
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
        _binding = null
    }
}
