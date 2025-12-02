package com.onlineexamination.ui.screens.exam

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlineexamination.data.model.ExamResult
import com.onlineexamination.data.model.QuestionResult
import com.onlineexamination.ui.theme.StudentColor
import com.onlineexamination.ui.viewmodel.ExamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultDetailScreen(
    attemptId: String,
    examId: String,
    viewModel: ExamViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val result = uiState.currentResult
    var hasRequestedResult by remember(examId, attemptId) { mutableStateOf(false) }

    LaunchedEffect(examId) {
        viewModel.loadExamById(examId)
    }

    LaunchedEffect(uiState.currentExam, attemptId, examId) {
        val exam = uiState.currentExam
        if (!hasRequestedResult && exam != null && exam.id == examId) {
            hasRequestedResult = true
            viewModel.loadResultByAttemptId(attemptId, exam)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exam Results") },
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
        if (result == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ResultSummaryCard(result = result)
                }

                item {
                    Text(
                        text = "Question Review",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(result.questionResults) { questionResult ->
                    QuestionResultCard(questionResult = questionResult)
                }
            }
        }
    }
}

@Composable
fun ResultSummaryCard(result: ExamResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isPassed) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = result.examTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (result.isPassed) 
                    MaterialTheme.colorScheme.primaryContainer
                else 
                    MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = if (result.isPassed) "PASSED" else "FAILED",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (result.isPassed) 
                        MaterialTheme.colorScheme.primary
                    else 
                        MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${result.score}/${result.totalPoints}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudentColor
                    )
                    Text(
                        text = "Score",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.1f", result.percentage)}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudentColor
                    )
                    Text(
                        text = "Percentage",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionResultCard(questionResult: QuestionResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (questionResult.isCorrect) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        )
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
                Text(
                    text = questionResult.questionText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (questionResult.isCorrect) 
                        MaterialTheme.colorScheme.primaryContainer
                    else 
                        MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = "${questionResult.points}/${questionResult.maxPoints}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (questionResult.isCorrect) 
                            MaterialTheme.colorScheme.primary
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }

            Divider()

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Your Answer:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = questionResult.studentAnswer.ifBlank { "No answer" },
                    fontSize = 14.sp,
                    color = if (questionResult.isCorrect) 
                        MaterialTheme.colorScheme.primary
                    else 
                        MaterialTheme.colorScheme.error
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Correct Answer:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = questionResult.correctAnswer,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}



