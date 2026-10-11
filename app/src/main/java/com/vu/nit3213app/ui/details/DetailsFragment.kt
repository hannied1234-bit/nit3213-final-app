package com.vu.nit3213app.ui.details

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.vu.nit3213app.R
import com.vu.nit3213app.databinding.FragmentDetailsBinding
import com.vu.nit3213app.databinding.ItemPropertyRowBinding
import com.vu.nit3213app.domain.model.Entity
import com.vu.nit3213app.domain.model.EntityProperty

/**
 * The Details screen: shows every field of the selected entity, including the
 * description.
 *
 * The Entity arrives as a Safe Args argument and needs no loading or business
 * logic, so this screen does not need a ViewModel or any injected dependencies.
 *
 * That also explains the missing @AndroidEntryPoint. Hilt has nothing to inject
 * here. The arguments are stored in the Fragment's Bundle, which Android saves
 * and restores on rotation, so the screen redraws the same entity afterwards
 * with no extra code.
 */
class DetailsFragment : Fragment(R.layout.fragment_details) {

    // navArgs() reads the arguments Safe Args generated from nav_graph.xml.
    // It's lazy, so the Bundle is only read the first time args is used. By
    // then onViewCreated is running and the arguments are guaranteed to be set.
    private val args: DetailsFragmentArgs by navArgs()

    // Same nullable binding pattern as the other Fragments, cleared in
    // onDestroyView so the views aren't held in memory after they're gone.
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailsBinding.bind(view)
        showEntity(args.entity)
    }

    /** Fills the header, the field list and the description from [entity]. */
    private fun showEntity(entity: Entity) {
        // The header uses the first field, matching the headline on the card
        // the user tapped, so it's clear the right item opened.
        binding.titleLabelText.text = entity.titleLabel
        binding.titleLabelText.isVisible = entity.titleLabel.isNotBlank()
        binding.titleText.text = entity.title.ifBlank { getString(R.string.entity_untitled) }

        // The number of fields depends on the topic, so one row is added per
        // field at runtime instead of fixing the rows in the XML layout.
        // removeAllViews() runs first in case this is ever called on a view
        // that already has rows, which would otherwise show every field twice.
        binding.propertiesContainer.removeAllViews()
        entity.properties.forEach(::addPropertyRow)
        // With no fields, the whole "Information" section is hidden so the
        // screen doesn't show a heading over an empty card.
        binding.informationSection.isVisible = entity.properties.isNotEmpty()

        binding.descriptionText.text =
            entity.description.ifBlank { getString(R.string.description_not_provided) }
    }

    /**
     * Inflates one item_property_row.xml and adds it to the field list.
     *
     * A plain LinearLayout is used here instead of a RecyclerView. An entity
     * only has a handful of fields and they all sit inside a scrolling page, so
     * recycling rows wouldn't save anything.
     */
    private fun addPropertyRow(property: EntityProperty) {
        // attachToParent = true adds the new row to propertiesContainer as
        // part of inflating it, so there's no separate addView() call.
        val rowBinding = ItemPropertyRowBinding.inflate(
            layoutInflater,
            binding.propertiesContainer,
            true
        )
        rowBinding.labelText.text = property.label
        rowBinding.valueText.text = property.value.ifBlank { getString(R.string.value_not_provided) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
