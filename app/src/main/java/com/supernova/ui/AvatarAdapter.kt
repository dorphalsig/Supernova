package com.supernova.ui

import android.util.Log
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

    companion object {
        private const val TAG = "AvatarAdapter"
    }

    private var avatars: List<String> = emptyList()
    private var selectedPosition = -1

    fun updateAvatars(newAvatars: List<String>) {
        Log.d(TAG, "Updating avatars: ${newAvatars.size} items")
        newAvatars.forEachIndexed { index, url ->
            Log.d(TAG, "Avatar [$index]: $url")
        }

        val diffCallback = AvatarDiffCallback(avatars, newAvatars)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        avatars = newAvatars
        selectedPosition = -1
        diffResult.dispatchUpdatesTo(this)

        Log.d(TAG, "Avatar list updated, notifying adapter")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        Log.d(TAG, "Creating new ViewHolder")
        val binding = ItemAvatarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AvatarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        Log.d(TAG, "Binding ViewHolder at position $position")
        holder.bind(avatars[position], position == selectedPosition)
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${avatars.size}")
        return avatars.size
    }

    inner class AvatarViewHolder(
        private val binding: ItemAvatarBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.avatarCard.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val previousPosition = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(position)
                    onAvatarClick(avatars[position])
                }
            }
        }

        fun bind(avatarUrl: String, isSelected: Boolean) {
            Log.d(TAG, "Binding avatar: $avatarUrl, selected: $isSelected")

            // Load image using Coil
            binding.avatarImageView.load(avatarUrl) {
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_close_clear_cancel)
                crossfade(true)
                // Apply circle crop transformation to match the avatar style
                transformations(CircleCropTransformation())
            }

            // Update selection state
            binding.selectionIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE

            // Update card state for TV focus
            binding.avatarCard.isSelected = isSelected
        }
    }
}