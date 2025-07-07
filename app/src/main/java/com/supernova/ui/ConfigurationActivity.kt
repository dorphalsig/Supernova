package com.supernova.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.supernova.databinding.ActivityConfigurationBinding
import com.supernova.network.ApiService
import com.supernova.network.models.ApiResult
import com.supernova.utils.SecureStorage
import com.supernova.utils.ValidationUtils
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ConfigurationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigurationBinding
    private lateinit var secureStorage: SecureStorage
    private val viewModel: ConfigurationViewModel by viewModels {
        ConfigurationViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        secureStorage = SecureStorage(this)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.continueButton.setOnClickListener {
            validateAndTestConnection()
        }

        binding.parentalLockCheckbox.setOnCheckedChangeListener { _, isChecked ->
            secureStorage.setParentalLock(isChecked)
        }

        // Set initial parental lock state
        binding.parentalLockCheckbox.isChecked = secureStorage.isParentalLockEnabled()
    }

    private fun observeViewModel() {
        viewModel.testResult.observe(this, Observer { result ->
            when (result) {
                is ApiResult.Loading -> {
                    setLoadingState(result.isLoading)
                }
                is ApiResult.Success -> {
                    handleSuccessfulTest()
                }
                is ApiResult.Error -> {
                    showError(result.message)
                }
            }
        })
    }

    private fun validateAndTestConnection() {
        val portal = binding.portalEditText.text.toString()
        val username = binding.usernameEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        // Validate portal
        val portalValidation = ValidationUtils.validatePortalUrl(portal)

        binding.portalInputLayout.error = null

        val usernameValidation = ValidationUtils.validateUsername(username)
        if (usernameValidation.isFailure) {
            val errorMessage = usernameValidation.exceptionOrNull()?.message ?: "Invalid username"
            binding.usernameInputLayout.error = errorMessage
            return
        }
        binding.usernameInputLayout.error = null

        val passwordValidation = ValidationUtils.validatePassword(password)
        if (passwordValidation.isFailure) {
            val errorMessage = passwordValidation.exceptionOrNull()?.message ?: "Invalid password"
            binding.passwordInputLayout.error = errorMessage
            return
        }
        binding.passwordInputLayout.error = null

// All validations passed, test connection
        val normalizedPortal = portalValidation.getOrNull()!!
        val validUsername = usernameValidation.getOrNull()!!
        val validPassword = passwordValidation.getOrNull()!!

        viewModel.testConnection(normalizedPortal, validUsername, validPassword)
    }

    private fun handleSuccessfulTest() {
        val portal = ValidationUtils.normalizePortalUrl(binding.portalEditText.text.toString())
        val username = binding.usernameEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        // Save credentials
        secureStorage.saveCredentials(portal, username, password)

        // Navigate to profile creation
        val intent = Intent(this, ProfileCreationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.continueButton.isEnabled = !isLoading
        binding.portalEditText.isEnabled = !isLoading
        binding.usernameEditText.isEnabled = !isLoading
        binding.passwordEditText.isEnabled = !isLoading
        binding.parentalLockCheckbox.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}

class ConfigurationViewModel : ViewModel() {

    private val _testResult = androidx.lifecycle.MutableLiveData<ApiResult<Unit>>()
    val testResult: androidx.lifecycle.LiveData<ApiResult<Unit>> = _testResult

    fun testConnection(portal: String, username: String, password: String) {
        _testResult.value = ApiResult.Loading()

        viewModelScope.launch {
            try {
                val apiService = ApiService.create(portal)
                val loginUrl = ApiService.buildLoginUrl(portal)
                val response = apiService.testLogin(loginUrl, username, password)

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse?.userInfo?.auth == 1 && loginResponse.userInfo.status == "Active") {
                        _testResult.value = ApiResult.Success(Unit)
                    } else {
                        _testResult.value = ApiResult.Error("Authentication failed. Please check your credentials.")
                    }
                } else {
                    _testResult.value = ApiResult.Error("Connection failed. Please check your portal URL.")
                }
            } catch (e: Exception) {
                _testResult.value = ApiResult.Error("Network error: ${e.message}")
            }
        }
    }
}

class ConfigurationViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfigurationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConfigurationViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}