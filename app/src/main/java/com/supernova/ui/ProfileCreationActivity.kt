package com.supernova.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.graphics.RenderEffect
import android.graphics.Shader
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.entities.ProfileEntity
import com.supernova.databinding.ActivityProfileCreationBinding
import com.supernova.network.AvatarService
import com.supernova.utils.SecureDataStore
import com.supernova.utils.SecureDataStore.getBoolean
import com.supernova.utils.SecureStorageKeys
import com.supernova.utils.ValidationUtils
import com.supernova.R
import kotlinx.coroutines.launch

class ProfileCreationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileCreationBinding
    private lateinit var avatarAdapter: AvatarAdapter
    private val viewModel: ProfileCreationViewModel by viewModels {
        ProfileCreationViewModelFactory(SupernovaDatabase.getDatabase(this))
    }

    private var selectedAvatarUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()
        setupAvatarGrid()
        observeViewModel()
        loadAvatars()
    }

    private fun setupViews() {
        binding.saveButton.setOnClickListener {
            createProfile()
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
        })

        viewModel.profileCreation.observe(this, Observer { result ->
            when (result) {
                is ProfileCreationResult.Success -> {
                    lifecycleScope.launch {
                        if (getBoolean(SecureStorageKeys.LAST_SYNC_SUCCESS)) {
                            navigateToProfileSelection()
                        } else {
                            val intent =
                                Intent(this@ProfileCreationActivity, LoadingActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
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
        val parentalEnabled = kotlinx.coroutines.runBlocking {
            getBoolean(SecureStorageKeys.PARENTAL_LOCK)
        }

        if (parentalEnabled) {
            showPinDialog(validName)
        } else {
            val profile = ProfileEntity(
                name = validName,
                pin = null,
                avatar = selectedAvatarUrl!!
            )
            viewModel.createProfile(profile)
        }
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
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun applyBlur(apply: Boolean) {
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            if (apply) {
                binding.root.setRenderEffect(
                    RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
                )
            } else {
                binding.root.setRenderEffect(null)
            }
        } else {
            binding.root.alpha = if (apply) 0.7f else 1f
        }
    }

    private fun showPinDialog(validName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pin, null)
        val pinEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.dialogPinEditText)
        val okButton = dialogView.findViewById<Button>(R.id.pinOkButton)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        okButton.setOnClickListener {
            val pinText = pinEditText.text.toString()
            val pinValidation = ValidationUtils.validatePin(pinText)
            if (pinValidation.isFailure) {
                pinEditText.error = pinValidation.exceptionOrNull()?.message
            } else {
                dialog.dismiss()
                applyBlur(false)
                val profile = ProfileEntity(
                    name = validName,
                    pin = pinValidation.getOrNull(),
                    avatar = selectedAvatarUrl!!
                )
                viewModel.createProfile(profile)
            }
        }

        dialog.setOnShowListener { applyBlur(true) }
        dialog.setOnDismissListener { applyBlur(false) }
        dialog.show()
    }
}

class ProfileCreationViewModel(private val database: SupernovaDatabase) : ViewModel() {

    private val _avatarUrls = MutableLiveData<List<String>>()
    val avatarUrls: LiveData<List<String>> = _avatarUrls

    private val _profileCreation = MutableLiveData<ProfileCreationResult>()
    val profileCreation: LiveData<ProfileCreationResult> = _profileCreation

    fun generateAvatars() {
        val urls = AvatarService.generateRandomAvatarUrls(6)
        _avatarUrls.value = urls
    }

    fun createProfile(profile: ProfileEntity) {
        _profileCreation.value = ProfileCreationResult.Loading

        viewModelScope.launch {
            try {
                database.profileDao().insertProfile(profile)
                _profileCreation.value = ProfileCreationResult.Success
            } catch (e: Exception) {
                _profileCreation.value =
                    ProfileCreationResult.Error(e.message ?: "Failed to create profile")
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