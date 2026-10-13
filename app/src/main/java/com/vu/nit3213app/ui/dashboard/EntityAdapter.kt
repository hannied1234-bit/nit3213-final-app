// Says which folder (package) this file belongs to.
package com.vu.nit3213app.ui.dashboard

// LayoutInflater: builds views from an XML layout file.
import android.view.LayoutInflater
// ViewGroup: a view that holds other views, like the list itself.
import android.view.ViewGroup
// bold: makes part of a text bold.
import androidx.core.text.bold
// buildSpannedString: builds a text that can have mixed styles, like bold labels.
import androidx.core.text.buildSpannedString
// isVisible: a shortcut for showing or hiding a view with true or false.
import androidx.core.view.isVisible
// DiffUtil: works out which rows changed between two lists.
import androidx.recyclerview.widget.DiffUtil
// ListAdapter: a RecyclerView adapter that uses DiffUtil for us.
import androidx.recyclerview.widget.ListAdapter
// RecyclerView: the scrolling list that reuses its rows.
import androidx.recyclerview.widget.RecyclerView
// R: the generated list of IDs for layouts, strings and images.
import com.vu.nit3213app.R
// ItemEntityBinding: generated from item_entity.xml, holds the views of one row.
import com.vu.nit3213app.databinding.ItemEntityBinding
// Entity: one tidy item from the list.
import com.vu.nit3213app.domain.model.Entity

/**
 * RecyclerView adapter for the Dashboard list (Lecture 6).
 *
 * It extends ListAdapter rather than the basic RecyclerView.Adapter. When a new
 * list is submitted, ListAdapter uses DiffUtil to work out which rows actually
 * changed and only redraws those, instead of redrawing everything.
 */
// Creates the adapter. It's given a function to run when a row is tapped.
class EntityAdapter(
    // The function to run on a tap. The Fragment decides what a tap does.
    private val onEntityClick: (Entity) -> Unit
// Builds on ListAdapter, with Entity as the item type and EntityDiffCallback to compare items.
) : ListAdapter<Entity, EntityAdapter.EntityViewHolder>(EntityDiffCallback) {

    // RecyclerView calls this when it needs a brand new row (about one screenful, plus a few spare).
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityViewHolder {
        // Builds one row's views from item_entity.xml.
        // Why false: RecyclerView adds the row to the list itself, later.
        val binding = ItemEntityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Wraps the views in a ViewHolder and hands it back.
        return EntityViewHolder(binding, onEntityClick)
    }

    // RecyclerView calls this every time a row scrolls into view.
    override fun onBindViewHolder(holder: EntityViewHolder, position: Int) {
        // Gets the Entity at this position and fills the row with it.
        holder.bind(getItem(position))
    }

    /**
     * Holds the views for one row so they are looked up once and then reused as
     * the list scrolls. bind() fills them with a different Entity each time, so
     * it must set every view on every call.
     */
    // Creates the ViewHolder class, which holds one row's views.
    class EntityViewHolder(
        // The row's views.
        private val binding: ItemEntityBinding,
        // The function to run when this row is tapped.
        private val onEntityClick: (Entity) -> Unit
    // Builds on RecyclerView's ViewHolder, giving it the row's top view.
    ) : RecyclerView.ViewHolder(binding.root) {

        // Fills this row with one Entity's data.
        fun bind(entity: Entity) {
            // Gets the context, which is needed to read text from strings.xml.
            val context = binding.root.context

            // Puts the first field's label (such as "Artist name") above the headline.
            binding.titleLabelText.text = entity.titleLabel
            // Hides that label if it's empty, so there's no blank gap.
            binding.titleLabelText.isVisible = entity.titleLabel.isNotBlank()
            // Puts the headline on the card, or "Untitled item" if it's empty.
            binding.titleText.text = entity.title.ifBlank { context.getString(R.string.entity_untitled) }

            // Reads "Not provided" once, for any empty values below.
            val notProvided = context.getString(R.string.value_not_provided)
            // Builds the summary text: one "Label: value" line per field, with the label in bold.
            binding.summaryText.text = buildSpannedString {
                // Goes through every field except the headline. index counts 0, 1, 2...
                entity.summaryProperties.forEachIndexed { index, property ->
                    // Starts a new line before every field except the first.
                    if (index > 0) append("\n")
                    // Adds the label in bold, such as "Year".
                    bold { append(property.label) }
                    // Adds ": " after the label.
                    append(": ")
                    // Adds the value, or "Not provided" if it's empty.
                    append(property.value.ifBlank { notProvided })
                }
            }
            // Hides the summary if there are no extra fields.
            binding.summaryText.isVisible = entity.summaryProperties.isNotEmpty()

            // When the card is tapped, run the tap function with this Entity.
            // Why set it every time: rows are reused, so it must point at the current Entity.
            binding.root.setOnClickListener { onEntityClick(entity) }
        }
    }
}

/**
 * Tells DiffUtil how to compare two entities. The API does not provide a unique
 * ID, so two entities count as the same item when all of their data matches.
 */
// Creates the comparison rules. "object" means there's only one, since it holds no data.
private object EntityDiffCallback : DiffUtil.ItemCallback<Entity>() {

    // Are these the same item? With no ID, they are if every field matches.
    override fun areItemsTheSame(oldItem: Entity, newItem: Entity): Boolean = oldItem == newItem

    // Has the item changed? Same check: if every field matches, it hasn't.
    override fun areContentsTheSame(oldItem: Entity, newItem: Entity): Boolean = oldItem == newItem
}
