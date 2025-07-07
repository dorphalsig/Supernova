package com.supernova.ui

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.supernova.data.entities.ProfileEntity
import com.supernova.databinding.ItemProfileCardBinding

class ProfileAdapter(
    private val onProfileClick: (ProfileEntity) -> Unit
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    private var profiles: List<ProfileEntity> = emptyList()

    fun updateProfiles(newProfiles: List<ProfileEntity>) {
        profiles = newProfiles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding = ItemProfileCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(profiles[position])
    }

    override fun getItemCount(): Int = profiles.size

    inner class ProfileViewHolder(
        private val binding: ItemProfileCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: ProfileEntity) {
            binding.profileNameTextView.text = profile.name

            // Load avatar from byte array
            val bitmap = BitmapFactory.decodeByteArray(profile.avatar, 0, profile.avatar.size)
            binding.profileAvatarImageView.setImageBitmap(bitmap)

            // Show lock indicator if profile has PIN
            binding.lockIndicator.visibility = if (profile.pin != null) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }

            // Set click listener
            binding.profileCard.setOnClickListener {
                onProfileClick(profile)
            }

            // Handle focus changes for TV navigation
            binding.profileCard.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    // Scale up when focused
                    view.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(200)
                        .start()

                    binding.profileCard.strokeWidth = 4
                } else {
                    // Scale down when not focused
                    view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(200)
                        .start()

                    binding.profileCard.strokeWidth = 0
                }
            }
        }
    }
}