package com.supernova.ui

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.transform.CircleCropTransformation
import com.supernova.databinding.ItemAvatarBinding
import com.supernova.network.AvatarService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        fun bind(avatarUrl: String, isSelected: Boolean) {
            Log.d(TAG, "Binding avatar: $avatarUrl")

            // Show placeholder while loading
            binding.avatarImageView.setImageResource(android.R.drawable.ic_menu_gallery)

            // Download and display
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val avatarService = AvatarService.create()
                    val response = avatarService.downloadAvatar(avatarUrl)
                    if (response.isSuccessful) {
                        val bytes = response.body()?.bytes()
                        if (bytes != null) {
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            withContext(Dispatchers.Main) {
                                binding.avatarImageView.setImageBitmap(bitmap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load avatar", e)
                }
            }

            // Keep your click handlers...
        }
    }
}