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
 * ListAdapter also stores the current list itself, so there's no list field
 * here and no getItemCount() override. getItem(position) reads from that
 * stored list.
 *
 * @param onEntityClick called with the tapped Entity. The adapter doesn't know
 * which screen comes next. The Fragment decides what a tap does.
 */
class EntityAdapter(
    private val onEntityClick: (Entity) -> Unit
) : ListAdapter<Entity, EntityAdapter.EntityViewHolder>(EntityDiffCallback) {

    // Called only when RecyclerView needs a brand new row, which is about one
    // screenful plus a few spare. After that, rows scrolling off the top are
    // recycled for the ones coming in at the bottom.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityViewHolder {
        // attachToParent is false because RecyclerView adds the row to itself
        // later. Passing parent still matters, since it lets the row's
        // layout_width and margins from the XML take effect.
        val binding = ItemEntityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EntityViewHolder(binding, onEntityClick)
    }

    // Called every time a row scrolls into view. It has to be fast, so all
    // it does is hand the right Entity to a ViewHolder that already exists.
    override fun onBindViewHolder(holder: EntityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Holds the views for one row so they are looked up once and then reused as
     * the list scrolls. bind() fills them with a different Entity each time.
     *
     * Because rows are reused, bind() has to set every view on every call. If
     * it skipped one, the row would keep showing text from whichever entity
     * used it before.
     */
    class EntityViewHolder(
        private val binding: ItemEntityBinding,
        private val onEntityClick: (Entity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entity: Entity) {
            val context = binding.root.context

            // The small label above the headline is hidden when blank, so a
            // missing label doesn't leave an empty gap at the top of the card.
            binding.titleLabelText.text = entity.titleLabel
            binding.titleLabelText.isVisible = entity.titleLabel.isNotBlank()
            binding.titleText.text = entity.title.ifBlank { context.getString(R.string.entity_untitled) }

            // The summary lists every field except the description, as the spec
            // requires. Each line reads "Label: value" with the label in bold.
            // Fetched once here instead of inside the loop, so a card with many
            // empty fields doesn't look the string up again and again.
            val notProvided = context.getString(R.string.value_not_provided)
            // buildSpannedString builds styled text in one TextView. That's
            // lighter than inflating a separate row view for each field, which
            // matters here because this code runs for every row as it scrolls.
            binding.summaryText.text = buildSpannedString {
                entity.summaryProperties.forEachIndexed { index, property ->
                    // A line break goes before every line except the first, so
                    // the text has no trailing blank line.
                    if (index > 0) append("\n")
                    bold { append(property.label) }
                    append(": ")
                    append(property.value.ifBlank { notProvided })
                }
            }
            // An entity with only one field has no summary. Hiding the view
            // removes its top margin as well, so the card stays compact.
            binding.summaryText.isVisible = entity.summaryProperties.isNotEmpty()

            // The listener is set again on each bind so it captures the current
            // entity, not the one this row showed before it was recycled.
            binding.root.setOnClickListener { onEntityClick(entity) }
        }
    }
}

/**
 * Tells DiffUtil how to compare two entities. The API does not provide a unique
 * ID, so two entities count as the same item when all of their data matches.
 *
 * DiffUtil asks two questions. areItemsTheSame asks whether two entries
 * represent the same thing, which decides whether a row moved or was replaced.
 * areContentsTheSame asks whether that thing changed, which decides whether
 * the row needs redrawing. With no ID, both come down to data class equality.
 *
 * It's an object because it holds no state, so one shared instance is enough.
 */
private object EntityDiffCallback : DiffUtil.ItemCallback<Entity>() {

    override fun areItemsTheSame(oldItem: Entity, newItem: Entity): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: Entity, newItem: Entity): Boolean = oldItem == newItem
}
