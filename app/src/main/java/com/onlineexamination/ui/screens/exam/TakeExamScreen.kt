package com.onlineexamination.ui.screens.exam

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlineexamination.data.model.Exam
import com.onlineexamination.data.model.ExamAttempt
import com.onlineexamination.data.model.Question
import com.onlineexamination.data.model.QuestionType
import com.onlineexamination.data.model.StudentLog
import com.onlineexamination.ui.theme.StudentColor
import com.onlineexamination.ui.viewmodel.ExamViewModel
import com.onlineexamination.ui.viewmodel.LogViewModel
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeExamScreen(
    examId: String,
    studentId: String,
    studentName: String,
    studentEmail: String,
    viewModel: ExamViewModel,
    logViewModel: LogViewModel = viewModel(),
    onBack: () -> Unit,
    onExamSubmitted: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var answers by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var timeRemaining by remember { mutableStateOf(0) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var hasSubmitted by remember { mutableStateOf(false) }
    var examStartTime by remember { mutableStateOf(0L) }
    var showCheatingDialog by remember { mutableStateOf(false) }

    val submitExamAction: () -> Unit = {
        uiState.currentExam?.let { currentExam ->
            if (isSubmitting || hasSubmitted) {
                return@let
            }
            triggerSubmission(
                exam = currentExam,
                studentId = studentId,
                studentName = studentName,
                studentEmail = studentEmail,
                answers = answers,
                examStartTime = examStartTime,
                timeRemaining = timeRemaining,
                onSubmit = { attemptExam ->
                    isSubmitting = true
                    hasSubmitted = true
                    viewModel.submitExamAttempt(attemptExam, currentExam)
                }
            )
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                if (!hasSubmitted) {
                    showCheatingDialog = true
                    val log = StudentLog(
                        studentId = studentId,
                        studentName = studentName,
                        eventType = "Tab Change",
                        eventDetails = "Student switched to another tab during the exam.",
                        timestamp = Date()
                    )
                    logViewModel.addLog(log)
                    submitExamAction()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(examId) {
        viewModel.loadExamById(examId)
    }

    val exam = uiState.currentExam
    if (exam == null && !uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Exam not found", fontSize = 18.sp)
                Button(onClick = onBack) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    LaunchedEffect(exam?.id) {
        exam?.let {
            answers = emptyMap()
            hasSubmitted = false
            isSubmitting = false
            examStartTime = System.currentTimeMillis()
            timeRemaining = it.durationMinutes * 60

            while (timeRemaining > 0 && !hasSubmitted) {
                delay(1000)
                timeRemaining--
            }

            if (timeRemaining == 0 && !hasSubmitted && !isSubmitting) {
                triggerSubmission(
                    exam = it,
                    studentId = studentId,
                    studentName = studentName,
                    studentEmail = studentEmail,
                    answers = answers,
                    examStartTime = examStartTime,
                    timeRemaining = timeRemaining,
                    onSubmit = { attemptExam ->
                        isSubmitting = true
                        hasSubmitted = true
                        viewModel.submitExamAttempt(attemptExam, it)
                    }
                )
            }
        }
    }

    LaunchedEffect(uiState.currentResult) {
        uiState.currentResult?.let { result ->
            onExamSubmitted(result.attemptId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(exam?.title ?: "Exam", fontSize = 16.sp)
                        Text(
                            text = formatTime(timeRemaining),
                            fontSize = 12.sp,
                            color = if (timeRemaining < 300) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudentColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { showConfirmDialog = true }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            submitExamAction()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting && exam != null,
                        colors = ButtonDefaults.buttonColors(containerColor = StudentColor)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Submit Exam")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (exam == null) {
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
                    Text(
                        text = "Instructions: Answer all questions. Time will auto-submit when expired.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                itemsIndexed(exam.questions) { index, question ->
                    QuestionCard(
                        question = question,
                        index = index + 1,
                        answer = answers[question.id] ?: "",
                        onAnswerChange = { answer ->
                            answers = answers + (question.id to answer)
                        }
                    )
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Submission") },
            text = { Text("Are you sure you want to submit the exam? You cannot change your answers after submission.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        submitExamAction()
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCheatingDialog) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismissing */ },
            title = { Text("Cheating Detected") },
            text = { Text("You have left the exam screen. Your exam will be submitted automatically.") },
            confirmButton = {
                TextButton(onClick = { showCheatingDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun QuestionCard(
    question: Question,
    index: Int,
    answer: String,
    onAnswerChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Question $index (${question.points} points)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = question.questionText,
                fontSize = 16.sp
            )

            when (question.type) {
                QuestionType.MULTIPLE_CHOICE -> {
                    question.options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = answer == option,
                                onClick = { onAnswerChange(option) }
                            )
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 8.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                QuestionType.TRUE_FALSE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onAnswerChange("True") }
                        ) {
                            RadioButton(
                                selected = answer == "True",
                                onClick = { onAnswerChange("True") }
                            )
                            Text("True", modifier = Modifier.padding(start = 8.dp))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onAnswerChange("False") }
                        ) {
                            RadioButton(
                                selected = answer == "False",
                                onClick = { onAnswerChange("False") }
                            )
                            Text("False", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
                QuestionType.SHORT_ANSWER -> {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = onAnswerChange,
                        label = { Text("Your Answer") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

private fun triggerSubmission(
    exam: Exam,
    studentId: String,
    studentName: String,
    studentEmail: String,
    answers: Map<String, String>,
    examStartTime: Long,
    timeRemaining: Int,
    onSubmit: (ExamAttempt) -> Unit
) {
    val startedAt = if (examStartTime != 0L) {
        examStartTime
    } else {
        System.currentTimeMillis() - ((exam.durationMinutes * 60 - timeRemaining).coerceAtLeast(0) * 1000L)
    }

    val elapsedMillis = System.currentTimeMillis() - startedAt
    val timeSpentMinutes = max(1, (elapsedMillis / 60000.0).roundToInt())

    val attempt = ExamAttempt(
        examId = exam.id,
        teacherId = exam.teacherId, // Added teacherId from exam
        studentId = studentId,
        studentName = studentName,
        studentEmail = studentEmail,
        startedAt = startedAt,
        answers = answers,
        timeSpentMinutes = timeSpentMinutes
    )

    onSubmit(attempt)
}
