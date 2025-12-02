package com.onlineexamination.ui.screens.exam

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlineexamination.data.model.SpecialExamRequest
import com.onlineexamination.ui.viewmodel.SpecialExamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestSpecialExamScreen(
    studentId: String,
    studentName: String,
    examId: String,
    examTitle: String,
    onBack: () -> Unit,
    onRequestSubmitted: () -> Unit,
    specialExamViewModel: SpecialExamViewModel = viewModel()
) {
    var reason by remember { mutableStateOf("") }
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    val uiState by specialExamViewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        fileUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Special Exam") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(it).padding(16.dp)) {
            Text("Exam: $examTitle", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason for missing the exam") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { filePickerLauncher.launch("*/*") }) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.height(8.dp))
                Text(if (fileUri == null) "Select Justification File" else "File Selected")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        if (fileUri != null) {
                            val request = SpecialExamRequest(
                                studentId = studentId,
                                studentName = studentName,
                                examId = examId,
                                examTitle = examTitle,
                                reason = reason
                            )
                            specialExamViewModel.createSpecialExamRequest(request, fileUri!!)
                        }
                    },
                    enabled = reason.isNotBlank() && fileUri != null
                ) {
                    Text("Submit Request")
                }
            }
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            uiState.successMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
                onRequestSubmitted()
            }
        }
    }
}
