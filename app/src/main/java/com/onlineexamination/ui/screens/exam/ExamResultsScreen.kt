package com.onlineexamination.ui.screens.exam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dvr
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlineexamination.data.model.ExamAttempt
import com.onlineexamination.ui.theme.TeacherColor
import com.onlineexamination.ui.viewmodel.ExamViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultsScreen(
    examId: String,
    examTitle: String,
    viewModel: ExamViewModel,
    onBack: () -> Unit,
    onResultClick: (String) -> Unit,
    onAnalyticsClick: (String) -> Unit,
    onLogsClick: (studentId: String, studentName: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(examId) {
        viewModel.loadExamAttempts(examId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exam Results: $examTitle") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TeacherColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onAnalyticsClick(examId) }) {
                        Icon(Icons.Default.Analytics, contentDescription = "Item Analysis")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.examAttempts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.examAttempts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Assessment,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "No results yet",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Submissions: ${uiState.examAttempts.size}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Button(onClick = { onAnalyticsClick(examId) }) {
                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Item Analysis")
                        }
                    }
                }
                items(uiState.examAttempts) { attempt ->
                    ResultCard(
                        attempt = attempt,
                        onClick = { onResultClick(attempt.id) },
                        onLogsClick = { onLogsClick(attempt.studentId, attempt.studentName) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultCard(
    attempt: ExamAttempt,
    onClick: () -> Unit,
    onLogsClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val percentage = attempt.percentage

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = attempt.studentName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = attempt.studentEmail,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Row {
                    IconButton(onClick = onLogsClick) {
                        Icon(Icons.Default.Dvr, contentDescription = "View Logs")
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (attempt.isPassed)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = if (attempt.isPassed) "PASSED" else "FAILED",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (attempt.isPassed)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = "${attempt.score}/${attempt.totalPoints}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TeacherColor
                    )
                    Text(
                        text = "Score",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Column {
                    Text(
                        text = "${String.format("%.1f", percentage)}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TeacherColor
                    )
                    Text(
                        text = "Percentage",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Column {
                    Text(
                        text = "${attempt.timeSpentMinutes} min",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TeacherColor
                    )
                    Text(
                        text = "Time Spent",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            attempt.submittedAt?.let {
                Text(
                    text = "Submitted: ${dateFormat.format(java.util.Date(it))}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
