package com.supernova.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.entities.ProfileEntity
import com.supernova.databinding.ActivityProfileCreationBinding
import com.supernova.network.AvatarService
import com.supernova.utils.AvatarPreloader
import com.supernova.utils.SecureStorage
import com.supernova.utils.ValidationUtils
import kotlinx.coroutines.launch

class ProfileCreationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileCreationBinding
    private lateinit var secureStorage: SecureStorage
    private lateinit var avatarAdapter: AvatarAdapter
    private lateinit var avatarPreloader: AvatarPreloader
    private val viewModel: ProfileCreationViewModel by viewModels {
        ProfileCreationViewModelFactory(SupernovaDatabase.getDatabase(this))
    }

    private var selectedAvatarUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        secureStorage = SecureStorage(this)
        avatarPreloader = AvatarPreloader(this)

        setupViews()
        setupAvatarGrid()
        observeViewModel()
        loadAvatars()
    }

    private fun setupViews() {
        binding.saveButton.setOnClickListener {
            createProfile()
        }

        // Show/hide PIN section based on parental lock setting
        if (secureStorage.isParentalLockEnabled()) {
            binding.pinSection.visibility = View.VISIBLE
        } else {
            binding.pinSection.visibility = View.GONE
        }
    }

    private fun setupAvatarGrid() {
        avatarAdapter = AvatarAdapter { avatarUrl ->
            selectedAvatarUrl = avatarUrl
            binding.saveButton.isEnabled = true
        }

        binding.avatarRecyclerView.apply {
            layoutManager = GridLayoutManager(this@ProfileCreationActivity, 3)
            adapter = avatarAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.avatarUrls.observe(this, Observer { urls ->
            avatarAdapter.updateAvatars(urls)
            // Preload avatars in background after they're generated
            avatarPreloader.preloadRandomAvatars(urls.size)
        })

        viewModel.profileCreation.observe(this, Observer { result ->
            when (result) {
                is ProfileCreationResult.Success -> {
                    navigateToProfileSelection()
                }
                is ProfileCreationResult.Error -> {
                    showError(result.message)
                    setLoadingState(false)
                }
                is ProfileCreationResult.Loading -> {
                    setLoadingState(true)
                }
            }
        })
    }

    private fun loadAvatars() {
        viewModel.generateAvatars()
    }

    private fun createProfile() {
        val profileName = binding.profileNameEditText.text.toString()

        // Validate profile name
        val nameValidation = ValidationUtils.validateProfileName(profileName)

        if (nameValidation.isFailure) {
            binding.profileNameInputLayout.error = nameValidation.exceptionOrNull()?.message
            return
        }
        binding.profileNameInputLayout.error = null

        val validName = nameValidation.getOrNull()!!

        // Check if avatar is selected
        if (selectedAvatarUrl == null) {
            showError("Please select an avatar")
            return
        }

        // Handle PIN if parental lock is enabled
        var pin: Int? = null
        if (secureStorage.isParentalLockEnabled()) {
            val pinText = binding.pinEditText.text.toString()
            val confirmPinText = binding.confirmPinEditText.text.toString()

            if (pinText != confirmPinText) {
                showError("PIN confirmation does not match")
                return
            }

            val pinValidation = ValidationUtils.validatePin(pinText)

            if (pinValidation.isFailure) {
                showError(pinValidation.exceptionOrNull()?.message ?: "Invalid PIN")
                return
            }

            pin = pinValidation.getOrNull()!!
        }

        // Create profile
        val profile = ProfileEntity(
            name = validName,
            pin = pin,
            avatar = selectedAvatarUrl!! // Store URL instead of bytes
        )

        viewModel.createProfile(profile)
    }

    private fun navigateToProfileSelection() {
        val intent = Intent(this, ProfileSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.saveButton.isEnabled = !isLoading && selectedAvatarUrl != null
        binding.profileNameEditText.isEnabled = !isLoading
        binding.pinEditText.isEnabled = !isLoading
        binding.confirmPinEditText.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}

class ProfileCreationViewModel(private val database: SupernovaDatabase) : ViewModel() {

    private val _avatarUrls = MutableLiveData<List<String>>()
    val avatarUrls: LiveData<List<String>> = _avatarUrls

    private val _profileCreation = MutableLiveData<ProfileCreationResult>()
    val profileCreation: LiveData<ProfileCreationResult> = _profileCreation

    fun generateAvatars() {
        val urls = AvatarService.generateRandomAvatarUrls(6) // 6 avatars as shown in mockup
        _avatarUrls.value = urls
    }

    fun createProfile(profile: ProfileEntity) {
        _profileCreation.value = ProfileCreationResult.Loading

        viewModelScope.launch {
            try {
                database.profileDao().insertProfile(profile)
                _profileCreation.value = ProfileCreationResult.Success
            } catch (e: Exception) {
                _profileCreation.value = ProfileCreationResult.Error(e.message ?: "Failed to create profile")
            }
        }
    }
}

class ProfileCreationViewModelFactory(
    private val database: SupernovaDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileCreationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileCreationViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class ProfileCreationResult {
    object Success : ProfileCreationResult()
    data class Error(val message: String) : ProfileCreationResult()
    object Loading : ProfileCreationResult()
}