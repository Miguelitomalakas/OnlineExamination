package com.onlineexamination.ui.screens.exam

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.onlineexamination.data.model.Exam
import com.onlineexamination.data.model.Question
import com.onlineexamination.data.model.QuestionType
import com.onlineexamination.ui.theme.TeacherColor
import com.onlineexamination.ui.viewmodel.ExamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExamScreen(
    teacherId: String,
    teacherName: String,
    viewModel: ExamViewModel,
    onBack: () -> Unit,
    onExamCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var gradeLevel by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("60") }
    var passingScore by remember { mutableStateOf("60") }
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var term by remember { mutableStateOf("Prelim") } // Added term state

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onExamCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Exam") },
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
                Divider()
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                        if (title.isNotBlank() && subject.isNotBlank() && questions.isNotEmpty()) {
                            val totalPoints = questions.sumOf { it.points }
                            val exam = Exam(
                                title = title,
                                description = description,
                                subject = subject,
                                gradeLevel = gradeLevel,
                                durationMinutes = durationMinutes.toIntOrNull() ?: 60,
                                totalPoints = totalPoints,
                                passingScore = passingScore.toIntOrNull() ?: 60,
                                teacherId = teacherId,
                                teacherName = teacherName,
                                questions = questions,
                                term = term // Added term to exam object
                            )
                            viewModel.createExam(exam)
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
                        Text("Create Exam")
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

@Composable
fun QuestionEditor(
    question: Question,
    index: Int,
    onQuestionChange: (Question) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question $index",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            OutlinedTextField(
                value = question.questionText,
                onValueChange = { onQuestionChange(question.copy(questionText = it)) },
                label = { Text("Question Text *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Question Type
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = question.type.name.replace("_", " "),
                    onValueChange = {},
                    label = { Text("Question Type") },
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
                    QuestionType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name.replace("_", " ")) },
                            onClick = {
                                onQuestionChange(question.copy(type = type))
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Options (for multiple choice)
            if (question.type == QuestionType.MULTIPLE_CHOICE) {
                Text("Options:", fontWeight = FontWeight.Medium)
                question.options.forEachIndexed { optIndex, option ->
                    OutlinedTextField(
                        value = option,
                        onValueChange = { newOption ->
                            val newOptions = question.options.toMutableList()
                            newOptions[optIndex] = newOption
                            onQuestionChange(question.copy(options = newOptions))
                        },
                        label = { Text("Option ${optIndex + 1}") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            RadioButton(
                                selected = question.correctAnswer == option,
                                onClick = { onQuestionChange(question.copy(correctAnswer = option)) }
                            )
                        }
                    )
                }
            } else {
                // True/False or Short Answer
                OutlinedTextField(
                    value = question.correctAnswer,
                    onValueChange = { onQuestionChange(question.copy(correctAnswer = it)) },
                    label = { Text("Correct Answer *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = question.type == QuestionType.TRUE_FALSE
                )
            }

            OutlinedTextField(
                value = question.points.toString(),
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        onQuestionChange(question.copy(points = it.toIntOrNull() ?: 10))
                    }
                },
                label = { Text("Points") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

