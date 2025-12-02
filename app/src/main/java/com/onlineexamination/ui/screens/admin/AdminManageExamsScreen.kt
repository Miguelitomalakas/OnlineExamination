package com.onlineexamination.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.onlineexamination.data.model.Exam
import com.onlineexamination.ui.theme.AdminColor
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageExamsScreen(
    onBack: () -> Unit
) {
    var exams by remember { mutableStateOf<List<Exam>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showActiveOnly by remember { mutableStateOf(false) }
    var showInactiveOnly by remember { mutableStateOf(false) }

    suspend fun loadExams() {
        isLoading = true
        errorMessage = null
        try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("exams")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            exams = snapshot.documents.mapNotNull { it.toObject(Exam::class.java) }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unable to load exams."
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadExams()
    }

    val coroutineScope = rememberCoroutineScope()

    val filteredExams = exams.filter { exam ->
        val matchesSearch = searchQuery.isBlank() ||
            exam.title.contains(searchQuery, ignoreCase = true) ||
            exam.subject.contains(searchQuery, ignoreCase = true) ||
            exam.teacherName.contains(searchQuery, ignoreCase = true)
        val now = System.currentTimeMillis()
        val isCurrentlyActive = exam.isActive && now < exam.endDate
        val matchesActiveFlag = when {
            showActiveOnly -> isCurrentlyActive
            showInactiveOnly -> !isCurrentlyActive
            else -> true
        }
        matchesSearch && matchesActiveFlag
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Exams") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdminColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch { loadExams() }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = errorMessage ?: "Something went wrong.",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { coroutineScope.launch { loadExams() } }) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Search & Filter",
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Search by title, subject, or teacher") }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = showActiveOnly,
                                        onCheckedChange = {
                                            showActiveOnly = it
                                            if (it) showInactiveOnly = false
                                        }
                                    )
                                    Text("Show Active Only", modifier = Modifier.padding(start = 8.dp))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = showInactiveOnly,
                                        onCheckedChange = {
                                            showInactiveOnly = it
                                            if (it) showActiveOnly = false
                                        }
                                    )
                                    Text("Show Inactive Only", modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }

                    if (filteredExams.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "No exams match your filters.",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(filteredExams) { exam ->
                                AdminExamCard(
                                    exam = exam,
                                    onToggleStatus = { newStatus ->
                                        coroutineScope.launch {
                                            val firestore = FirebaseFirestore.getInstance()
                                            firestore.collection("exams")
                                                .document(exam.id)
                                                .update("isActive", newStatus)
                                                .await()
                                            loadExams()
                                        }
                                    },
                                    onDelete = {
                                        coroutineScope.launch {
                                            val firestore = FirebaseFirestore.getInstance()
                                            firestore.collection("exams")
                                                .document(exam.id)
                                                .delete()
                                                .await()
                                            loadExams()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminExamCard(
    exam: Exam,
    onToggleStatus: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val isCurrentlyActive = exam.isActive && System.currentTimeMillis() < exam.endDate

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exam.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = exam.subject,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Teacher: ${exam.teacherName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isCurrentlyActive) "Active" else "Inactive",
                        color = if (isCurrentlyActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ends ${dateFormat.format(Date(exam.endDate))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onToggleStatus(!isCurrentlyActive) }) {
                    Icon(Icons.Default.ToggleOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isCurrentlyActive) "Deactivate" else "Activate")
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Exam", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}


