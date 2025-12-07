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
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.onlineexamination.data.model.User
import com.onlineexamination.ui.theme.AdminColor
import com.onlineexamination.ui.theme.AdminGradientEnd
import com.onlineexamination.ui.theme.AdminGradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    user: User,
    onSignOut: () -> Unit,
    onManageUsers: () -> Unit = {},
    onAnalytics: () -> Unit = {},
    onManageExams: () -> Unit = {},
    onSettings: () -> Unit = {},
    onViewProfile: () -> Unit = {}
) {
    var totalUsers by remember { mutableStateOf(0) }
    var totalExams by remember { mutableStateOf(0) }
    var teachers by remember { mutableStateOf(0) }
    var students by remember { mutableStateOf(0) }
    var isLoadingStats by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        val usersListener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    totalUsers = snapshot.size()
                    teachers = snapshot.documents.count { 
                        it.toObject(com.onlineexamination.data.model.User::class.java)?.role == com.onlineexamination.data.model.UserRole.TEACHER 
                    }
                    students = snapshot.documents.count { 
                        it.toObject(com.onlineexamination.data.model.User::class.java)?.role == com.onlineexamination.data.model.UserRole.STUDENT 
                    }
                    isLoadingStats = false
                }
            }

        val examsListener = firestore.collection("exams")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    totalExams = snapshot.size()
                }
            }

        onDispose {
            usersListener.remove()
            examsListener.remove()
        }
    }

    val gradientBrush = Brush.verticalGradient(
        listOf(
            AdminGradientStart,
            AdminGradientEnd,
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
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
                            text = "Welcome back, ${user.username}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminColor
                        )
                        Text(
                            text = "Track users, coordinate exams, and keep the platform humming smoothly—all in one vibrant panel.",
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
                    items(getAdminQuickActions()) { action ->
                        QuickActionCard(
                            title = action.title,
                            icon = action.icon,
                            color = AdminColor,
                            onClick = {
                                when (action.title) {
                                    "Manage Users" -> onManageUsers()
                                    "Manage Exams" -> onManageExams()
                                    "Analytics" -> onAnalytics()
                                    "Settings" -> onSettings()
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
                        title = "Total Users",
                        value = if (isLoadingStats) "..." else totalUsers.toString(),
                        icon = Icons.Default.People,
                        color = AdminColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Exams",
                        value = if (isLoadingStats) "..." else totalExams.toString(),
                        icon = Icons.Default.Assignment,
                        color = AdminColor,
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
                        title = "Teachers",
                        value = if (isLoadingStats) "..." else teachers.toString(),
                        icon = Icons.Default.School,
                        color = AdminColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Students",
                        value = if (isLoadingStats) "..." else students.toString(),
                        icon = Icons.Default.Person,
                        color = AdminColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recent Activity
            item {
                Text(
                    text = "Recent Activity",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
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
                            text = "No recent alerts",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "System notifications, audit logs, and approvals will show up here once activity picks up.",
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

fun getAdminQuickActions(): List<QuickAction> {
    return listOf(
        QuickAction("Manage Users", Icons.Default.People),
        QuickAction("Manage Exams", Icons.Default.Assignment),
        QuickAction("Analytics", Icons.Default.Analytics),
        QuickAction("Settings", Icons.Default.Settings)
    )
}
