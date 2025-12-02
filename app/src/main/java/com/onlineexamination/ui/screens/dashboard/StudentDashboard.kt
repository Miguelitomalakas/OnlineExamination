package com.onlineexamination.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlineexamination.data.model.User
import com.onlineexamination.ui.theme.StudentColor
import com.onlineexamination.ui.theme.StudentGradientEnd
import com.onlineexamination.ui.theme.StudentGradientStart
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(
    user: User,
    onSignOut: () -> Unit,
    onTakeExam: () -> Unit = {},
    onViewResults: () -> Unit = {},
    onViewProfile: () -> Unit = {},
    onStudyMaterials: () -> Unit = {},
    onLeaderboards: () -> Unit = {}
) {
    var examsTaken by remember { mutableStateOf(0) }
    var averageScore by remember { mutableStateOf<String?>(null) }
    var isLoadingStats by remember { mutableStateOf(true) }

    LaunchedEffect(user.uid) {
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val attemptsSnapshot = firestore.collection("exam_attempts")
                .whereEqualTo("studentId", user.uid)
                .whereNotEqualTo("submittedAt", null)
                .get()
                .await()

            examsTaken = attemptsSnapshot.size()

            val attempts = attemptsSnapshot.documents.mapNotNull { 
                it.toObject(com.onlineexamination.data.model.ExamAttempt::class.java) 
            }

            if (attempts.isNotEmpty()) {
                val totalPercentage = attempts.sumOf { it.percentage }
                val avg = totalPercentage / attempts.size
                averageScore = String.format("%.1f", avg)
            } else {
                averageScore = "N/A"
            }
            isLoadingStats = false
        } catch (e: Exception) {
            isLoadingStats = false
        }
    }
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            StudentGradientStart,
            StudentGradientEnd,
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Student Dashboard") },
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
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Hello, ${user.username} 👋",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudentColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You're all set to continue learning. Jump back into your exams or review your performance.",
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
                        fontWeight = FontWeight.Black
                    )
                }

                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(330.dp) // Adjusted height for 3 rows
                    ) {
                        items(getStudentQuickActions()) { action ->
                            QuickActionCard(
                                title = action.title,
                                icon = action.icon,
                                color = StudentColor,
                                onClick = {
                                    when (action.title) {
                                        "Take Exam" -> onTakeExam()
                                        "View Results" -> onViewResults()
                                        "Study Materials" -> onStudyMaterials()
                                        "Profile" -> onViewProfile()
                                        "Leaderboards" -> onLeaderboards()
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
                            title = "Exams Taken",
                            value = if (isLoadingStats) "..." else examsTaken.toString(),
                            icon = Icons.Default.Assignment,
                            color = StudentColor,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Average Score",
                            value = if (isLoadingStats) "..." else (averageScore ?: "N/A"),
                            icon = Icons.Default.TrendingUp,
                            color = StudentColor,
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
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "No recent activity yet",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Once you take exams, your latest attempts and achievements will appear here.",
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

fun getStudentQuickActions(): List<QuickAction> {
    return listOf(
        QuickAction("Take Exam", Icons.Default.Quiz),
        QuickAction("View Results", Icons.Default.Assignment),
        QuickAction("Study Materials", Icons.Default.MenuBook),
        QuickAction("Profile", Icons.Default.Person),
        QuickAction("Leaderboards", Icons.Default.EmojiEvents)
    )
}
