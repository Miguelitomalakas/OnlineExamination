package com.onlineexamination.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlineexamination.data.model.User
import com.onlineexamination.ui.theme.TeacherColor
import com.onlineexamination.ui.theme.TeacherGradientEnd
import com.onlineexamination.ui.theme.TeacherGradientStart
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboard(
    user: User,
    onSignOut: () -> Unit,
    onCreateExam: () -> Unit = {},
    onViewExams: () -> Unit = {},
    onViewResults: () -> Unit = {},
    onViewProfile: () -> Unit = {},
    onAnalytics: () -> Unit = {}
) {
    var examCount by remember { mutableStateOf(0) }
    var studentCount by remember { mutableStateOf(0) }
    var isLoadingStats by remember { mutableStateOf(true) }

    DisposableEffect(user.uid) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        val examsListener = firestore.collection("exams")
            .whereEqualTo("teacherId", user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    examCount = snapshot.size()
                }
            }

        val attemptsListener = firestore.collection("exam_attempts")
            .whereEqualTo("teacherId", user.uid)
            .whereNotEqualTo("submittedAt", null)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoadingStats = false
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val uniqueStudents = snapshot.documents
                        .mapNotNull { it.getString("studentId") }
                        .distinct()
                        .count()
                    
                    studentCount = uniqueStudents
                    isLoadingStats = false
                }
            }

        onDispose {
            examsListener.remove()
            attemptsListener.remove()
        }
    }

    val gradientBrush = Brush.verticalGradient(
        listOf(
            TeacherGradientStart,
            TeacherGradientEnd,
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Teacher Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    var showLogoutDialog by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Sign Out")
                    }
                    
                    if (showLogoutDialog) {
                        AlertDialog(
                            onDismissRequest = { showLogoutDialog = false },
                            title = { Text("Confirm Logout") },
                            text = { Text("Are you sure you want to logout?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showLogoutDialog = false
                                        onSignOut()
                                    }
                                ) {
                                    Text("Logout")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showLogoutDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
            // Welcome Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Great to see you, ${user.username}",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = TeacherColor
                        )
                        Text(
                            text = "Monitor your classes, publish exams, and track performance from a single, vibrant workspace.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // Quick Actions
            item {
                Text(
                    text = "Quick Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(220.dp)
                ) {
                    items(getTeacherQuickActions()) { action ->
                        QuickActionCard(
                            title = action.title,
                            icon = action.icon,
                            color = TeacherColor,
                            onClick = {
                                when (action.title) {
                                    "Create Exam" -> onCreateExam()
                                    "View Exams" -> onViewExams()
                                    "Grade Exams" -> onViewResults()
                                    "Analytics" -> onAnalytics()
                                }
                            }
                        )
                    }
                }
            }

            // Statistics
            item {
                Text(
                    text = "Statistics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Exams Created",
                        value = if (isLoadingStats) "..." else examCount.toString(),
                        icon = Icons.Default.Create,
                        color = TeacherColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Students",
                        value = if (isLoadingStats) "..." else studentCount.toString(),
                        icon = Icons.Default.People,
                        color = TeacherColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recent Activity
            item {
                Text(
                    text = "Recent Activity",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "No recent submissions",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Once students start submitting, their latest work will appear here for quick review.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
}

fun getTeacherQuickActions(): List<QuickAction> {
    return listOf(
        QuickAction("Create Exam", Icons.Default.Create),
        QuickAction("View Exams", Icons.Default.Assignment),
        QuickAction("Grade Exams", Icons.Default.Grade),
        QuickAction("Analytics", Icons.Default.Analytics)
    )
}
