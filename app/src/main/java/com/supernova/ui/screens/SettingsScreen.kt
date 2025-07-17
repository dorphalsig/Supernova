package com.supernova.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onProviderSettings: () -> Unit,
    onProfiles: () -> Unit
) {
    val showReset by viewModel.showResetConfirmation.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val lastSuccess by viewModel.lastSyncSuccess.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SettingsHeader(onBack)
            Spacer(modifier = Modifier.height(16.dp))
            SettingsMenuItem(
                icon = Icons.Default.Tune,
                title = "Provider Settings",
                description = "Configure portal and credentials",
                onClick = onProviderSettings,
                tag = "provider"
            )
            SettingsMenuItem(
                icon = Icons.Default.Person,
                title = "Profiles",
                description = "Manage profiles",
                onClick = onProfiles,
                tag = "profiles"
            )
            SettingsMenuItem(
                icon = Icons.Default.Sync,
                title = "Sync Settings",
                description = syncState?.name ?: lastSyncTime?.let { "Last sync: $it" } ?: "",
                onClick = { viewModel.triggerManualSync() },
                tag = "sync"
            )
            SettingsMenuItem(
                icon = Icons.Default.Info,
                title = "About",
                description = viewModel.buildInfo,
                onClick = {},
                tag = "about"
            )
            Spacer(modifier = Modifier.weight(1f))
            ResetDataButton(onClick = { viewModel.confirmReset() })
        }
        if (showReset) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissReset() },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetAllData() }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissReset() }) { Text("Cancel") }
                },
                title = { Text("Reset All Data") },
                text = { Text("Are you sure you want to delete all data?") }
            )
        }
    }
}

@Composable
fun SettingsHeader(onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        var focused by remember { mutableStateOf(false) }
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier
                .size(32.dp)
                .focusable()
                .onFocusChanged { focused = it.isFocused }
                .clickable { onBack() }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun SettingsMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    tag: String
) {
    var focused by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .focusable()
            .onFocusChanged { focused = it.isFocused }
            .clickable { onClick() }
            .background(if (focused) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp)
            .testTag("menu_$tag")
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Icon(Icons.Default.ArrowForward, contentDescription = null)
    }
}

@Composable
fun ResetDataButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .testTag("reset_button")
    ) {
        Text("Reset All Data")
    }
}
