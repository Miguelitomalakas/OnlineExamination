package com.onlineexamination.ui.screens.dashboard

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.onlineexamination.data.model.Exam
import com.onlineexamination.data.model.ExamAttempt
import com.onlineexamination.ui.theme.TeacherColor
import kotlin.math.roundToInt

data class TeacherExamPerformance(
    val exam: Exam,
    val submissions: Int,
    val averageScore: Double,
    val passRate: Double,
    val scoreDistribution: Map<String, Int>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAnalyticsScreen(
    teacherId: String,
    onBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var exams by remember { mutableStateOf<List<Exam>>(emptyList()) }
    var attempts by remember { mutableStateOf<List<ExamAttempt>>(emptyList()) }

    DisposableEffect(teacherId) {
        val firestore = FirebaseFirestore.getInstance()

        val examsListener = firestore.collection("exams")
            .whereEqualTo("teacherId", teacherId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    errorMessage = error.message ?: "Unable to load exams."
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    exams = snapshot.documents.mapNotNull { it.toObject(Exam::class.java) }
                    isLoading = false
                }
            }

        val attemptsListener = firestore.collection("exam_attempts")
            .whereNotEqualTo("submittedAt", null)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    errorMessage = error.message ?: "Unable to load attempts."
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    attempts = snapshot.documents.mapNotNull { it.toObject(ExamAttempt::class.java) }
                }
            }

        onDispose {
            examsListener.remove()
            attemptsListener.remove()
        }
    }

    val totalSubmissions = attempts.size
    val averagePercentage = if (totalSubmissions > 0) {
        attempts.sumOf { it.percentage } / totalSubmissions
    } else 0.0
    val passRate = if (totalSubmissions > 0) {
        attempts.count { it.isPassed }.toDouble() / totalSubmissions * 100
    } else 0.0
    val activeExams = exams.count { it.isActive && System.currentTimeMillis() < it.endDate }

    val performance = exams.map { exam ->
        val examAttempts = attempts.filter { it.examId == exam.id }
        val submissions = examAttempts.size
        val avgScore = if (submissions > 0) examAttempts.sumOf { it.percentage } / submissions else 0.0
        val pass = if (submissions > 0) examAttempts.count { it.isPassed }.toDouble() / submissions * 100 else 0.0
        val scoreDistribution = examAttempts.groupingBy { 
            val percentage = it.percentage
            when {
                percentage >= 90 -> "90-100%"
                percentage >= 80 -> "80-89%"
                percentage >= 70 -> "70-79%"
                percentage >= 60 -> "60-69%"
                else -> "<60%"
            }
        }.eachCount()

        TeacherExamPerformance(
            exam = exam,
            submissions = submissions,
            averageScore = avgScore,
            passRate = pass,
            scoreDistribution = scoreDistribution
        )
    }.sortedByDescending { it.averageScore }

    val recentAttempts = attempts.sortedByDescending { it.submittedAt ?: 0L }.take(5)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics Overview") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TeacherColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Failed to load analytics.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Performance Summary",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AnalyticsStatCard(
                                title = "Exams Created",
                                value = exams.size.toString(),
                                icon = Icons.Default.Analytics
                            )
                            AnalyticsStatCard(
                                title = "Active Exams",
                                value = activeExams.toString(),
                                icon = Icons.Default.CheckCircle
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AnalyticsStatCard(
                                title = "Avg. Score",
                                value = "${averagePercentage.roundToInt()}%",
                                icon = Icons.Default.Assessment
                            )
                            AnalyticsStatCard(
                                title = "Pass Rate",
                                value = "${passRate.roundToInt()}%",
                                icon = Icons.Default.People
                            )
                        }
                    }

                    if (performance.isNotEmpty()) {
                        item {
                            Text(
                                text = "Exam Performance",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(performance.take(5)) { perf ->
                            ExamPerformanceCard(performance = perf)
                        }
                    }

                    if (recentAttempts.isNotEmpty()) {
                        item {
                            Text(
                                text = "Latest Submissions",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(recentAttempts) { attempt ->
                            RecentAttemptCard(attempt = attempt)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = TeacherColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = TeacherColor, modifier = Modifier.size(32.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TeacherColor
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ExamPerformanceCard(performance: TeacherExamPerformance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = performance.exam.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${performance.submissions} submissions • ${performance.exam.subject}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${performance.averageScore.roundToInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TeacherColor
                    )
                    Text(
                        text = "Average Score",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${performance.passRate.roundToInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TeacherColor
                    )
                    Text(
                        text = "Pass Rate",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            ScoreDistributionChart(distribution = performance.scoreDistribution)
        }
    }
}

@Composable
private fun ScoreDistributionChart(distribution: Map<String, Int>) {
    val sortedDistribution = distribution.entries.sortedBy { it.key }
    val maxValue = sortedDistribution.maxOfOrNull { it.value } ?: 0

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Score Distribution", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) { 
            val barWidth = size.width / (sortedDistribution.size * 2)
            sortedDistribution.forEachIndexed { index, entry ->
                val barHeight = if (maxValue > 0) (entry.value / maxValue.toFloat()) * size.height else 0f
                drawRect(
                    color = TeacherColor,
                    topLeft = Offset(x = (index * 2 + 0.5f) * barWidth, y = size.height - barHeight),
                    size = Size(width = barWidth, height = barHeight)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            sortedDistribution.forEach { entry ->
                Text(entry.key, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun RecentAttemptCard(attempt: ExamAttempt) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attempt.studentName,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Score: ${attempt.score}/${attempt.totalPoints}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (attempt.isPassed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = if (attempt.isPassed) "PASSED" else "FAILED",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
