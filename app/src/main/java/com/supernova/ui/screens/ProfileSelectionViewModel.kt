package com.supernova.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.entities.ProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileSelectionViewModel(
    private val database: SupernovaDatabase
) : ViewModel() {

    private val _profiles = MutableStateFlow<List<ProfileEntity>>(emptyList())
    val profiles: StateFlow<List<ProfileEntity>> = _profiles.asStateFlow()

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