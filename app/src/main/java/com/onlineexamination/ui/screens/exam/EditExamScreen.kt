package com.onlineexamination.ui.screens.exam

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlineexamination.data.model.Exam
import com.onlineexamination.data.model.Question
import com.onlineexamination.data.model.QuestionType
import com.onlineexamination.ui.theme.TeacherColor
import com.onlineexamination.ui.viewmodel.ExamViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExamScreen(
    examId: String,
    teacherId: String,
    viewModel: ExamViewModel,
    onBack: () -> Unit,
    onExamUpdated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val exam = uiState.currentExam

    LaunchedEffect(examId) {
        viewModel.loadExamById(examId)
    }

    if (exam == null && !uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
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

    var title by remember { mutableStateOf(exam?.title ?: "") }
    var description by remember { mutableStateOf(exam?.description ?: "") }
    var subject by remember { mutableStateOf(exam?.subject ?: "") }
    var gradeLevel by remember { mutableStateOf(exam?.gradeLevel ?: "") }
    var durationMinutes by remember { mutableStateOf(exam?.durationMinutes?.toString() ?: "60") }
    var passingScore by remember { mutableStateOf(exam?.passingScore?.toString() ?: "60") }
    var isActive by remember { mutableStateOf(exam?.isActive ?: true) }
    var questions by remember { mutableStateOf(exam?.questions ?: emptyList()) }
    var term by remember { mutableStateOf(exam?.term ?: "Prelim") } // Added term state

    LaunchedEffect(exam) {
        exam?.let {
            title = it.title
            description = it.description
            subject = it.subject
            gradeLevel = it.gradeLevel
            durationMinutes = it.durationMinutes.toString()
            passingScore = it.passingScore.toString()
            isActive = it.isActive
            questions = it.questions
            term = it.term
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onExamUpdated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Exam") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TeacherColor,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Exam Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Exam Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = gradeLevel,
                        onValueChange = { gradeLevel = it },
                        label = { Text("Grade Level") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = durationMinutes,
                        onValueChange = { if (it.all { char -> char.isDigit() }) durationMinutes = it },
                        label = { Text("Duration (minutes) *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = passingScore,
                        onValueChange = { if (it.all { char -> char.isDigit() }) passingScore = it },
                        label = { Text("Passing Score (%)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            item {
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = term,
                        onValueChange = {},
                        label = { Text("Term") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("Prelim", "Midterm", "Finals").forEach { termOption ->
                            DropdownMenuItem(
                                text = { Text(termOption) },
                                onClick = {
                                    term = termOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Active Status", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
            }

            item {
                Divider()
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "Questions (${questions.size})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            questions = questions + Question(
                                id = System.currentTimeMillis().toString(),
                                questionText = "",
                                type = QuestionType.MULTIPLE_CHOICE,
                                options = listOf("", "", "", ""),
                                correctAnswer = "",
                                points = 10
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TeacherColor)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Question")
                    }
                }
            }

            itemsIndexed(questions) { index, question ->
                QuestionEditor(
                    question = question,
                    index = index + 1,
                    onQuestionChange = { updatedQuestion ->
                        questions = questions.mapIndexed { i, q ->
                            if (i == index) updatedQuestion else q
                        }
                    },
                    onDelete = {
                        questions = questions.filterIndexed { i, _ -> i != index }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank() && subject.isNotBlank() && questions.isNotEmpty() && exam != null) {
                            val totalPoints = questions.sumOf { it.points }
                            val updatedExam = exam.copy(
                                title = title,
                                description = description,
                                subject = subject,
                                gradeLevel = gradeLevel,
                                durationMinutes = durationMinutes.toIntOrNull() ?: 60,
                                totalPoints = totalPoints,
                                passingScore = passingScore.toIntOrNull() ?: 60,
                                isActive = isActive,
                                questions = questions,
                                term = term // Added term to exam object
                            )
                            viewModel.updateExam(updatedExam)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && title.isNotBlank() && subject.isNotBlank() && questions.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = TeacherColor)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Update Exam")
                    }
                }
            }

            item {
                uiState.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}


