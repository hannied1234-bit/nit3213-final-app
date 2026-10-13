// Says which folder (package) this file belongs to.
package com.vu.nit3213app.ui.details

// Bundle: a box of saved values Android passes to screens.
import android.os.Bundle
// View: the base class for everything you see on screen.
import android.view.View
// isVisible: a shortcut for showing or hiding a view with true or false.
import androidx.core.view.isVisible
// Fragment: the base class for a screen inside the Activity.
import androidx.fragment.app.Fragment
// navArgs: reads the values passed to this screen with Safe Args.
import androidx.navigation.fragment.navArgs
// R: the generated list of IDs for layouts, strings and images.
import com.vu.nit3213app.R
// FragmentDetailsBinding: generated from fragment_details.xml, holds every view on this screen.
import com.vu.nit3213app.databinding.FragmentDetailsBinding
// ItemPropertyRowBinding: generated from item_property_row.xml, holds one "label and value" row.
import com.vu.nit3213app.databinding.ItemPropertyRowBinding
// Entity: one tidy item.
import com.vu.nit3213app.domain.model.Entity
// EntityProperty: one label and value pair.
import com.vu.nit3213app.domain.model.EntityProperty

/**
 * The Details screen: shows every field of the selected entity, including the
 * description.
 *
 * The Entity arrives as a Safe Args argument and needs no loading or business
 * logic, so this screen doesn't need a ViewModel or Hilt.
 */
// Creates the Details screen class, built from fragment_details.xml. No Hilt label: nothing to inject.
class DetailsFragment : Fragment(R.layout.fragment_details) {

    // Reads the values passed to this screen. Here that's the tapped Entity.
    private val args: DetailsFragmentArgs by navArgs()

    // Holds the views of this screen. Null until the views are built.
    private var _binding: FragmentDetailsBinding? = null
    // A shortcut that returns _binding without the null check.
    private val binding get() = _binding!!

    // Android calls this once the screen's views have been built.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Lets the parent Fragment class do its own set-up first.
        super.onViewCreated(view, savedInstanceState)
        // Connects the binding to the views that were just built.
        _binding = FragmentDetailsBinding.bind(view)
        // Fills the screen with the Entity that was passed in.
        showEntity(args.entity)
    }

    /** Fills the header, the field list and the description from [entity]. */
    private fun showEntity(entity: Entity) {
        // Puts the first field's label (such as "Artist name") in the header.
        binding.titleLabelText.text = entity.titleLabel
        // Hides that label if it's empty.
        binding.titleLabelText.isVisible = entity.titleLabel.isNotBlank()
        // Puts the headline in the header, or "Untitled item" if it's empty.
        binding.titleText.text = entity.title.ifBlank { getString(R.string.entity_untitled) }

        // Clears any old rows from the field list.
        // Why: if this ever ran twice, every field would otherwise show twice.
        binding.propertiesContainer.removeAllViews()
        // Adds one row for each field.
        // Why at runtime: each topic has a different number of fields, so they can't be fixed in XML.
        entity.properties.forEach(::addPropertyRow)
        // Hides the whole "Information" section if there are no fields.
        binding.informationSection.isVisible = entity.properties.isNotEmpty()

        // Puts the description on screen.
        binding.descriptionText.text =
            // Uses "No description has been provided" if it's empty.
            entity.description.ifBlank { getString(R.string.description_not_provided) }
    }

    /**
     * Inflates one item_property_row.xml and adds it to the field list.
     */
    // Adds one "label above value" row for a single field.
    private fun addPropertyRow(property: EntityProperty) {
        // Builds the row's views from item_property_row.xml.
        val rowBinding = ItemPropertyRowBinding.inflate(
            // The tool that builds views from XML.
            layoutInflater,
            // The container the row goes into.
            binding.propertiesContainer,
            // true adds the row to the container straight away.
            true
        )
        // Puts the label in the row, such as "Year".
        rowBinding.labelText.text = property.label
        // Puts the value in the row, or "Not provided" if it's empty.
        rowBinding.valueText.text = property.value.ifBlank { getString(R.string.value_not_provided) }
    }

    // Android calls this when the screen's views are destroyed.
    override fun onDestroyView() {
        // Lets the parent Fragment class do its own clean-up first.
        super.onDestroyView()
        // Forgets the views so they can be removed from memory.
        _binding = null
    }
}
