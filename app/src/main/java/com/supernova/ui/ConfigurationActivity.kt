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
import com.supernova.network.models.ApiResult
import com.supernova.utils.SecureDataStore
import com.supernova.utils.ValidationUtils
import androidx.lifecycle.viewModelScope
import com.supernova.R
import com.supernova.network.ApiService
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.supernova.data.database.SupernovaDatabase
import com.supernova.utils.SecureStorageKeys
import com.supernova.work.SyncManager
import kotlin.collections.set

class ConfigurationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigurationBinding
    private val viewModel: ConfigurationViewModel by viewModels {
        ConfigurationViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.portalEditText.setText("http://ky-iptv.com:25461/")
        binding.usernameEditText.setText("Spicysoda56")
        binding.passwordEditText.setText("Pass8248")

        binding.continueButton.setOnClickListener {
            validateAndTestConnection()
        }
        binding.continueButton.setOnClickListener {
            validateAndTestConnection()
        }

        binding.parentalLockCheckbox.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch { SecureDataStore.putBoolean(SecureStorageKeys.PARENTAL_LOCK, isChecked) }
        }

        // Set initial parental lock state
        lifecycleScope.launch {
            binding.parentalLockCheckbox.isChecked = SecureDataStore.getBoolean(SecureStorageKeys.PARENTAL_LOCK)
        }
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
                    val errorMsg = when (result.message) {
                        "AUTHENTICATION_FAILED" -> getString(R.string.authentication_failed_please_check_your_credentials)
                        "CONNECTION_FAILED" -> getString(R.string.connection_failed_please_check_your_portal_url)
                        else -> "${getString(R.string.network_error_)}  ${result.message}"
                    }
                    showError(errorMsg)
                    setLoadingState(false)
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
            val errorMessage =
                usernameValidation.exceptionOrNull()?.message ?: getString(R.string.invalid_user)
            binding.usernameInputLayout.error = errorMessage
            return
        }
        binding.usernameInputLayout.error = null

        val passwordValidation = ValidationUtils.validatePassword(password)
        if (passwordValidation.isFailure) {
            val errorMessage = passwordValidation.exceptionOrNull()?.message
                ?: getString(R.string.invalid_password)
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

    suspend fun saveCredentials(portal: String, username: String, password: String) {
        SecureDataStore.putString(SecureStorageKeys.PORTAL, portal)
        SecureDataStore.putString(SecureStorageKeys.USERNAME, username)
        SecureDataStore.putString(SecureStorageKeys.PASSWORD, password)
    }


    private fun handleSuccessfulTest() {
        val portal = ValidationUtils.normalizePortalUrl(binding.portalEditText.text.toString())
        val username = binding.usernameEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        lifecycleScope.launch {
            saveCredentials(portal, username, password)
            SyncManager(this@ConfigurationActivity).scheduleDailySync()

            val db = SupernovaDatabase.getDatabase(this@ConfigurationActivity)
            val profileCount = db.profileDao().getProfileCount()
            val lockedCount = db.profileDao().getLockedProfileCount()

            val parental = SecureDataStore.getBoolean(SecureStorageKeys.PARENTAL_LOCK)

            val next = if (profileCount == 0 || (parental && lockedCount == 0)) {
                Intent(this@ConfigurationActivity, ProfileCreationActivity::class.java)
            } else {
                Intent(this@ConfigurationActivity, LoadingActivity::class.java)
            }

            startActivity(next)
            finish()
        }
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
                        _testResult.value = ApiResult.Error("AUTH_FAILED_MSG")
                    }
                } else {
                    _testResult.value = ApiResult.Error("CONNECTION_FAILED")
                }
            } catch (e: Exception) {
                _testResult.value = ApiResult.Error("${e.message}")
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