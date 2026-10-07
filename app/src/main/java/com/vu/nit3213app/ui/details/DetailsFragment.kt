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
 */
class DetailsFragment : Fragment(R.layout.fragment_details) {

    // navArgs() reads the arguments Safe Args generated from nav_graph.xml.
    private val args: DetailsFragmentArgs by navArgs()

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailsBinding.bind(view)
        showEntity(args.entity)
    }

    private fun showEntity(entity: Entity) {
        binding.titleLabelText.text = entity.titleLabel
        binding.titleLabelText.isVisible = entity.titleLabel.isNotBlank()
        binding.titleText.text = entity.title.ifBlank { getString(R.string.entity_untitled) }

        // The number of fields depends on the topic, so one row is added per
        // field at runtime instead of fixing the rows in the XML layout.
        binding.propertiesContainer.removeAllViews()
        entity.properties.forEach(::addPropertyRow)
        binding.informationSection.isVisible = entity.properties.isNotEmpty()

        binding.descriptionText.text =
            entity.description.ifBlank { getString(R.string.description_not_provided) }
    }

    private fun addPropertyRow(property: EntityProperty) {
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
