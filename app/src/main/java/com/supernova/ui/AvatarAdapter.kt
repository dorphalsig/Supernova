package com.supernova.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.supernova.databinding.ItemAvatarBinding

private class AvatarDiffCallback(
    private val oldList: List<String>,
    private val newList: List<String>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
}

class AvatarAdapter(
    private val onAvatarClick: (String) -> Unit
) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

    private var avatars: List<String> = emptyList()
    private var selectedPosition = -1

    fun updateAvatars(newAvatars: List<String>) {
        val diffCallback = AvatarDiffCallback(avatars, newAvatars)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        avatars = newAvatars
        selectedPosition = -1
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val binding = ItemAvatarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AvatarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        holder.bind(avatars[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = avatars.size

    inner class AvatarViewHolder(
        private val binding: ItemAvatarBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(avatarUrl: String, isSelected: Boolean) {
            // Use Coil for image loading
            binding.avatarImageView.load(avatarUrl) {
                transformations(CircleCropTransformation())
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_close_clear_cancel)
            }

            // Show selection indicator
            binding.selectionIndicator.visibility = if (isSelected) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Set focus and click handlers
            binding.avatarCard.setOnClickListener {
                val previous = selectedPosition
                val current = bindingAdapterPosition
                if (current == RecyclerView.NO_POSITION) return@setOnClickListener

                selectedPosition = current
                if (previous != -1) notifyItemChanged(previous)
                notifyItemChanged(current)
                onAvatarClick(avatarUrl)
            }

            binding.avatarCard.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.avatarCard.scaleX = 1.1f
                    binding.avatarCard.scaleY = 1.1f
                } else {
                    binding.avatarCard.scaleX = 1.0f
                    binding.avatarCard.scaleY = 1.0f
                }
            }
        }
    }
}