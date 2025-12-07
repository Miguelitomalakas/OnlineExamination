package com.onlineexamination.ui.screens.exam

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlineexamination.data.model.Question
import com.onlineexamination.data.model.QuestionType
import com.onlineexamination.ui.components.*
import com.onlineexamination.ui.theme.TeacherColor
import com.onlineexamination.ui.viewmodel.ExamViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

private val subjects = listOf(
    "Mathematics",
    "Science",
    "English",
    "Filipino",
    "Technology and Livelihood Education (TLE)",
    "Music, Arts, Physical Education, and Health (MAPEH)",
    "Edukasyon sa Pagpapakatao (ESP)",
    "Araling Panlipunan (AP)"
)
private val gradeLevels = listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExamScreen(
    examId: String,
    viewModel: ExamViewModel,
    onBack: () -> Unit,
    onExamUpdated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val exam = uiState.currentExam

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var gradeLevel by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("60") }
    var passingScore by remember { mutableStateOf("60") }
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var term by remember { mutableStateOf("Prelim") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val newQuestions = mutableListOf<Question>()
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.lineSequence().forEach { line ->
                            val parts = line.split(",")
                            if (parts.size >= 8) {
                                val questionText = parts[0]
                                val type = QuestionType.valueOf(parts[1])
                                val points = parts[2].toInt()
                                val options = parts.subList(3, 7)
                                val correctAnswer = parts[7]
                                newQuestions.add(
                                    Question(
                                        id = System.currentTimeMillis().toString(),
                                        questionText = questionText,
                                        type = type,
                                        options = options,
                                        correctAnswer = correctAnswer,
                                        points = points
                                    )
                                )
                            }
                        }
                    }
                }
                questions = questions + newQuestions
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    LaunchedEffect(examId) {
        viewModel.loadExamById(examId)
    }

    LaunchedEffect(exam) {
        exam?.let {
            title = it.title
            description = it.description
            subject = it.subject
            gradeLevel = it.gradeLevel
            durationMinutes = it.durationMinutes.toString()
            passingScore = it.passingScore.toString()
            questions = it.questions
            term = it.term
            startDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date(it.startDate))
            endDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date(it.endDate))
            startTime = it.startTime
            endTime = it.endTime
            isActive = it.isActive
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage == "Exam updated successfully!") {
            viewModel.clearMessages()
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && exam == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownField(
                                label = "Subject *",
                                selectedKey = subject,
                                options = subjects.map { DropdownOption(it, it) }
                            ) { subject = it.key }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownField(
                                label = "Grade Level",
                                selectedKey = gradeLevel,
                                options = gradeLevels.map { DropdownOption(it, it) }
                            ) { gradeLevel = it.key }
                        }
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
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            DatePickerField(label = "Start Date", value = startDate) { startDate = it }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TimePickerField(label = "Start Time", value = startTime) { startTime = it }
                        }
                    }
                }

                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            DatePickerField(label = "End Date", value = endDate) { endDate = it }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TimePickerField(label = "End Time", value = endTime) { endTime = it }
                        }
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Exam", modifier = Modifier.weight(1f))
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }
                }

                item {
                    HorizontalDivider()
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    questions = questions + Question(
                                        id = System.currentTimeMillis().toString(),
                                        questionText = "",
                                        type = QuestionType.MULTIPLE_CHOICE,
                                        options = listOf("", "", "", ""),
                                        correctAnswer = "",
                                        points = 1
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TeacherColor)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add")
                            }
                            OutlinedButton(
                                onClick = { filePickerLauncher.launch("*/*") },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TeacherColor),
                                border = BorderStroke(1.dp, TeacherColor)
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import")
                            }
                        }
                    }
                }

                itemsIndexed(questions) { index, question ->
                    QuestionEditor(
                        question = question,
                        index = index + 1,
                        onQuestionChange = { updatedQuestion: Question ->
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
                                val updatedExam = exam?.copy(
                                    title = title,
                                    description = description,
                                    subject = subject,
                                    gradeLevel = gradeLevel,
                                    durationMinutes = durationMinutes.toIntOrNull() ?: 60,
                                    totalPoints = totalPoints,
                                    passingScore = passingScore.toIntOrNull() ?: 60,
                                    questions = questions,
                                    term = term,
                                    startDate = parseDateTimeToMillis(startDate, startTime),
                                    endDate = parseDateTimeToMillis(endDate, endTime),
                                    startTime = startTime,
                                    endTime = endTime,
                                    isActive = isActive
                                )
                                updatedExam?.let { viewModel.updateExam(it) }
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
}
