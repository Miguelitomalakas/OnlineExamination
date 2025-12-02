package com.onlineexamination.ui.screens.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlineexamination.data.model.StudentLog
import com.onlineexamination.ui.viewmodel.LogViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLogsScreen(
    studentId: String,
    studentName: String,
    onBack: () -> Unit,
    logViewModel: LogViewModel = viewModel()
) {
    val uiState by logViewModel.uiState.collectAsState()

    LaunchedEffect(studentId) {
        logViewModel.getStudentLogs(studentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logs for $studentName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(it)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            } else if (uiState.studentLogs.isNotEmpty()) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(uiState.studentLogs) { log ->
                        LogCard(log = log)
                    }
                }
            } else {
                Text("No logs found for this student.", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun LogCard(log: StudentLog) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = log.eventType,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = log.eventDetails,
                style = MaterialTheme.typography.bodyMedium
            )
            log.timestamp?.let {
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault()).format(it),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
