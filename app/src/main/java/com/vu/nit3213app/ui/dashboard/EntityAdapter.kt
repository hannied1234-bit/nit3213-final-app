package com.vu.nit3213app.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vu.nit3213app.R
import com.vu.nit3213app.databinding.ItemEntityBinding
import com.vu.nit3213app.domain.model.Entity

/**
 * RecyclerView adapter for the Dashboard list (Lecture 6).
 *
 * It extends ListAdapter rather than the basic RecyclerView.Adapter. When a new
 * list is submitted, ListAdapter uses DiffUtil to work out which rows actually
 * changed and only redraws those, instead of redrawing everything as
 * notifyDataSetChanged() would.
 *
 * @param onEntityClick called with the tapped Entity. The adapter does not know
 * about navigation; the Fragment decides what a tap does.
 */
class EntityAdapter(
    private val onEntityClick: (Entity) -> Unit
) : ListAdapter<Entity, EntityAdapter.EntityViewHolder>(EntityDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityViewHolder {
        val binding = ItemEntityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EntityViewHolder(binding, onEntityClick)
    }

    override fun onBindViewHolder(holder: EntityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Holds the views for one row so they are looked up once and then reused as
     * the list scrolls. bind() fills them with a different Entity each time.
     */
    class EntityViewHolder(
        private val binding: ItemEntityBinding,
        private val onEntityClick: (Entity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entity: Entity) {
            val context = binding.root.context

            binding.titleLabelText.text = entity.titleLabel
            binding.titleLabelText.isVisible = entity.titleLabel.isNotBlank()
            binding.titleText.text = entity.title.ifBlank { context.getString(R.string.entity_untitled) }

            // The summary lists every field except the description, as the spec
            // requires. Each line reads "Label: value" with the label in bold.
            val notProvided = context.getString(R.string.value_not_provided)
            binding.summaryText.text = buildSpannedString {
                entity.summaryProperties.forEachIndexed { index, property ->
                    if (index > 0) append("\n")
                    bold { append(property.label) }
                    append(": ")
                    append(property.value.ifBlank { notProvided })
                }
            }
            binding.summaryText.isVisible = entity.summaryProperties.isNotEmpty()

            binding.root.setOnClickListener { onEntityClick(entity) }
        }
    }
}

/**
 * Tells DiffUtil how to compare two entities. The API does not provide a unique
 * ID, so two entities count as the same item when all of their data matches.
 */
private object EntityDiffCallback : DiffUtil.ItemCallback<Entity>() {

    override fun areItemsTheSame(oldItem: Entity, newItem: Entity): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: Entity, newItem: Entity): Boolean = oldItem == newItem
}
