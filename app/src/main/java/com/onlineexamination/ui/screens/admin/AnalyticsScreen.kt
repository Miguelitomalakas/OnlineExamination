package com.onlineexamination.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlineexamination.data.model.UserRole
import com.onlineexamination.ui.theme.AdminColor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit
) {
    var stats by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            
            // Get user counts
            val usersSnapshot = firestore.collection("users").get().await()
            val totalUsers = usersSnapshot.size()
            val students = usersSnapshot.documents.count { 
                it.toObject(com.onlineexamination.data.model.User::class.java)?.role == UserRole.STUDENT 
            }
            val teachers = usersSnapshot.documents.count { 
                it.toObject(com.onlineexamination.data.model.User::class.java)?.role == UserRole.TEACHER 
            }
            val admins = usersSnapshot.documents.count { 
                it.toObject(com.onlineexamination.data.model.User::class.java)?.role == UserRole.ADMIN 
            }
            
            // Get exam counts
            val examsSnapshot = firestore.collection("exams").get().await()
            val totalExams = examsSnapshot.size()
            
            // Get attempt counts
            val attemptsSnapshot = firestore.collection("exam_attempts").get().await()
            val totalAttempts = attemptsSnapshot.size()
            
            stats = mapOf(
                "totalUsers" to totalUsers,
                "students" to students,
                "teachers" to teachers,
                "admins" to admins,
                "totalExams" to totalExams,
                "totalAttempts" to totalAttempts
            )
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdminColor,
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
        if (isLoading) {
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
                        text = "System Statistics",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Total Users",
                            value = stats["totalUsers"]?.toString() ?: "0",
                            icon = Icons.Default.People,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Total Exams",
                            value = stats["totalExams"]?.toString() ?: "0",
                            icon = Icons.Default.Assignment,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Students",
                            value = stats["students"]?.toString() ?: "0",
                            icon = Icons.Default.Person,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Teachers",
                            value = stats["teachers"]?.toString() ?: "0",
                            icon = Icons.Default.School,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    StatCard(
                        title = "Total Exam Attempts",
                        value = stats["totalAttempts"]?.toString() ?: "0",
                        icon = Icons.Default.Assessment,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AdminColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = AdminColor
            )
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AdminColor
            )
            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}



