package com.onlineexamination.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.onlineexamination.data.model.User
import com.onlineexamination.data.model.UserRole
import com.onlineexamination.ui.theme.AdminColor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(
    onBack: () -> Unit
) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun fetchUsers() {
        isLoading = true
        coroutineScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("users").get().await()
                users = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                }
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message
                isLoading = false
            }
        }
    }

    fun verifyUser(userId: String) {
        coroutineScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("users").document(userId).update("isVerified", true).await()
                fetchUsers() // Refresh the list
            } catch (e: Exception) {
                errorMessage = "Failed to verify user: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Users") },
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
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = errorMessage ?: "Error loading users",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Total Users: ${users.size}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(users) { user ->
                    UserCard(user = user, onVerifyUser = { verifyUser(user.uid) })
                }
            }
        }
    }
}

@Composable
fun UserCard(user: User, onVerifyUser: () -> Unit) {
    val roleColor = when (user.role) {
        UserRole.STUDENT -> MaterialTheme.colorScheme.primary
        UserRole.TEACHER -> MaterialTheme.colorScheme.secondary
        UserRole.ADMIN -> AdminColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.username,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = roleColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = user.role.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = roleColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (user.emailVerified) "Verified" else "Not Verified",
                        fontSize = 12.sp,
                        color = if (user.emailVerified) 
                            MaterialTheme.colorScheme.primary
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
                if (!user.isVerified) {
                    Button(onClick = onVerifyUser) {
                        Text("Verify")
                    }
                }
            }
        }
    }
}
