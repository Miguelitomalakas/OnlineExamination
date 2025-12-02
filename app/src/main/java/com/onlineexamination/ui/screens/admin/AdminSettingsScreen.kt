package com.onlineexamination.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.onlineexamination.ui.theme.AdminColor
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminSettings(
    val maintenanceMode: Boolean = false,
    val allowRegistrations: Boolean = true,
    val autoApproveTeachers: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    onBack: () -> Unit
) {
    var settings by remember { mutableStateOf(AdminSettings()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val firestore = remember { FirebaseFirestore.getInstance() }

    suspend fun loadSettings() {
        isLoading = true
        errorMessage = null
        try {
            val doc = firestore.collection("app_settings")
                .document("general")
                .get()
                .await()
            doc.toObject(AdminSettings::class.java)?.let {
                settings = it
            }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unable to load settings."
            isLoading = false
        }
    }

    suspend fun saveSettings() {
        isSaving = true
        successMessage = null
        errorMessage = null
        try {
            firestore.collection("app_settings")
                .document("general")
                .set(settings)
                .await()
            successMessage = "Settings saved successfully."
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to save settings."
        } finally {
            isSaving = false
        }
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        loadSettings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdminColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            } else {
                SettingCard(
                    title = "Maintenance Mode",
                    description = "Temporarily disable the application for all users while performing updates.",
                    checked = settings.maintenanceMode,
                    onToggle = { settings = settings.copy(maintenanceMode = it) }
                )
                SettingCard(
                    title = "Allow New Registrations",
                    description = "Controls whether students and teachers can create new accounts.",
                    checked = settings.allowRegistrations,
                    onToggle = { settings = settings.copy(allowRegistrations = it) }
                )
                SettingCard(
                    title = "Auto-approve Teacher Accounts",
                    description = "Automatically grant teacher privileges upon registration without manual review.",
                    checked = settings.autoApproveTeachers,
                    onToggle = { settings = settings.copy(autoApproveTeachers = it) }
                )

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                successMessage?.let { success ->
                    Text(
                        text = success,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = { coroutineScope.launch { saveSettings() } },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSaving) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Changes")
                    }
                }

                TextButton(
                    onClick = { coroutineScope.launch { loadSettings() } },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Reset")
                }
            }
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    description: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Switch(checked = checked, onCheckedChange = onToggle)
        }
    }
}


