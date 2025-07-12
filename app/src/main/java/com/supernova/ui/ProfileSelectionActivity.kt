package com.supernova.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.entities.ProfileEntity
import com.supernova.ui.screens.ProfileSelectionScreen
import com.supernova.ui.screens.ProfileSelectionViewModelFactory
import com.supernova.ui.theme.SupernovaTheme
import com.supernova.utils.ValidationUtils

class ProfileSelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = SupernovaDatabase.getDatabase(this)
        val viewModelFactory = ProfileSelectionViewModelFactory(database)

        setContent {
            SupernovaTheme {
                ProfileSelectionScreen(
                    onProfileSelected = { profile -> selectProfile(profile) },
                    onAddProfile = { navigateToProfileCreation() },
                    viewModel = viewModel(factory = viewModelFactory)
                )
            }
        }
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
        showError("Selected profile: ${profile.name}")
    }

    private fun navigateToProfileCreation() {
        val intent = Intent(this, ProfileCreationActivity::class.java)
        startActivity(intent)
        // Don't finish() here - allow user to return
    }

    private fun showError(message: String) {
        // For Compose, we might want to use a different approach
        // For now, using the legacy method
        runOnUiThread {
            // Since we're using Compose, we'd typically handle this through state
            // But for simplicity, keeping the existing approach
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSelectionPreview() {
    SupernovaTheme {
        // Preview would need mock data
    }
}