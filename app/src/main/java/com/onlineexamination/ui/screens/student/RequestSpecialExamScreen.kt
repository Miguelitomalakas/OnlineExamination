package com.onlineexamination.ui.screens.student

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlineexamination.ui.theme.StudentColor
import com.onlineexamination.ui.viewmodel.SpecialExamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestSpecialExamScreen(
    studentId: String,
    studentName: String,
    examId: String,
    examTitle: String,
    viewModel: SpecialExamViewModel = viewModel(),
    onBack: () -> Unit,
    onRequestSubmitted: () -> Unit
) {
    var reason by remember { mutableStateOf("Medical Certificate") }
    var description by remember { mutableStateOf("") }
    var fileUrl by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            // Get rudimentary file name
            fileName = it.lastPathSegment ?: "Selected File"
            
            viewModel.uploadFile(it) { url ->
                isUploading = false
                if (url != null) {
                    fileUrl = url
                } else {
                    fileName = "" // Reset on failure
                    // Ideally show error snackbar here
                }
            }
        }
    }

    val reasons = listOf("Medical Certificate", "Death Certificate", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Special Exam") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudentColor,
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Request Retake for: $examTitle",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Please provide a valid reason and supporting documents for your request.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Text("Reason", fontWeight = FontWeight.SemiBold)
            Column {
                reasons.forEach { r ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reason = r }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = reason == r,
                            onClick = { reason = r }
                        )
                        Text(text = r, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Additional Details") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Text("Supporting Document", fontWeight = FontWeight.SemiBold)
            
            if (fileName.isNotEmpty() && !isUploading) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("File Uploaded", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(fileName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        TextButton(onClick = { 
                            fileUrl = ""
                            fileName = ""
                        }) {
                            Text("Remove")
                        }
                    }
                }
            } else if (isUploading) {
                 OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Uploading document...")
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Document")
                }
                Text(
                    "Supported formats: PDF, JPG, PNG", 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isSubmitting = true
                    viewModel.submitRequest(
                        studentId, studentName, examId, examTitle, reason, description, fileUrl
                    ) { success ->
                        isSubmitting = false
                        if (success) {
                            showSuccessDialog = true
                        }
                    }
                },
                enabled = !isSubmitting && !isUploading && description.isNotBlank() && fileUrl.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = StudentColor)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit Request")
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                onRequestSubmitted()
            },
            title = { Text("Request Submitted") },
            text = { Text("Your request has been submitted to your teacher for review.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showSuccessDialog = false
                        onRequestSubmitted()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}
