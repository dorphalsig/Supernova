package com.supernova.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.supernova.R
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.entities.ProfileEntity
import com.supernova.databinding.ActivityProfileSelectionBinding
import com.supernova.utils.ValidationUtils
import kotlinx.coroutines.launch

class ProfileSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSelectionBinding
    private lateinit var profileAdapter: ProfileAdapter
    private val viewModel: ProfileSelectionViewModel by viewModels {
        ProfileSelectionViewModelFactory(SupernovaDatabase.getDatabase(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupRecyclerView()
        observeViewModel()
        loadProfiles()
    }

    private fun setupViews() {
        binding.addProfileFab.setOnClickListener {
            navigateToProfileCreation()
        }
    }

    private fun setupRecyclerView() {
        profileAdapter = ProfileAdapter { profile ->
            selectProfile(profile)
        }

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.profilesRecyclerView.apply {
            this.layoutManager = layoutManager
            adapter = profileAdapter

            // Add snap helper for centered scrolling
            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(this)

            // Add spacing decoration for TV-friendly navigation
            addItemDecoration(ProfileItemDecoration())
        }
    }

    private fun observeViewModel() {
        viewModel.profiles.observe(this, Observer { profiles ->
            if (profiles.isEmpty()) {
                navigateToProfileCreation()
            } else {
                profileAdapter.updateProfiles(profiles)
                binding.instructionText.setText(R.string.instruction_select_profile)

                // Center single profile
                if (profiles.size == 1) {
                    binding.profilesRecyclerView.setPadding(0, binding.profilesRecyclerView.paddingTop, 0, binding.profilesRecyclerView.paddingBottom)
                } else {
                    val sidePadding = resources.getDimensionPixelSize(R.dimen.margin_xlarge)
                    binding.profilesRecyclerView.setPadding(sidePadding, binding.profilesRecyclerView.paddingTop, sidePadding, binding.profilesRecyclerView.paddingBottom)
                }
            }
        })
    }

    private fun loadProfiles() {
        viewModel.loadProfiles()
    }

    private fun selectProfile(profile: ProfileEntity) {
        if (profile.pin != null) {
            showPinDialog(profile)
        } else {
            proceedWithProfile(profile)
        }
    }

    private fun showPinDialog(profile: ProfileEntity) {
        val pinInput = TextInputEditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Enter PIN"
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("PIN Required")
            .setMessage("Enter PIN for ${profile.name}")
            .setView(pinInput)
            .setPositiveButton("OK") { _, _ ->
                val enteredPin = pinInput.text.toString()
                val pinValidation = ValidationUtils.validatePin(enteredPin)
                val validPin = pinValidation.getOrNull()

                when (validPin) {
                    null -> showError("Please enter a valid 4-digit PIN")
                    profile.pin -> proceedWithProfile(profile)
                    else -> showError("Incorrect PIN")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()



    }

    private fun proceedWithProfile(profile: ProfileEntity) {
        // TODO: Navigate to main IPTV interface
        // For now, show success message
        Snackbar.make(binding.root, "Selected profile: ${profile.name}", Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToProfileCreation() {
        val intent = Intent(this, ProfileCreationActivity::class.java)
        startActivity(intent)
        // Don't finish() here - allow user to return
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        // Reload profiles when returning from profile creation
        loadProfiles()
    }
}

class ProfileSelectionViewModel(private val database: SupernovaDatabase) : ViewModel() {

    private val _profiles = androidx.lifecycle.MutableLiveData<List<ProfileEntity>>()
    val profiles: androidx.lifecycle.LiveData<List<ProfileEntity>> = _profiles

    fun loadProfiles() {
        viewModelScope.launch {
            database.profileDao().getAllProfiles().collect { profileList ->
                _profiles.value = profileList
            }
        }
    }
}

class ProfileSelectionViewModelFactory(
    private val database: SupernovaDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileSelectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileSelectionViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProfileItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: android.graphics.Rect,
        view: android.view.View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val margin = parent.context.resources.getDimensionPixelSize(com.supernova.R.dimen.margin_large)
        outRect.left = margin
        outRect.right = margin
    }
}