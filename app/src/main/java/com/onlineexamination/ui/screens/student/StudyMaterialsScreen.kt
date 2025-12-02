package com.onlineexamination.ui.screens.student

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.onlineexamination.data.model.StudyMaterial
import com.onlineexamination.data.model.StudentInfo
import com.onlineexamination.ui.theme.StudentColor
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StudyMaterialsScreen(
    studentId: String,
    onBack: () -> Unit
) {
    var materials by remember { mutableStateOf<List<StudyMaterial>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("All Subjects") }
    var studentGradeLevel by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    LaunchedEffect(studentId) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val studentProfile = firestore.collection("users")
                .document(studentId)
                .collection("profiles")
                .document("student")
                .get()
                .await()
                .toObject(StudentInfo::class.java)
            studentGradeLevel = studentProfile?.gradeLevelToEnroll

            val snapshot = firestore.collection("study_materials")
                .orderBy("uploadedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            materials = snapshot.documents.mapNotNull { doc ->
                doc.toObject(StudyMaterial::class.java)?.copy(id = doc.id)
            }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load study materials."
            isLoading = false
        }
    }

    val subjects = listOf("All Subjects") + materials.map { it.subject }.filter { it.isNotBlank() }.distinct()

    val filteredMaterials = materials.filter { material ->
        val matchesSearch = searchQuery.isBlank() ||
            material.title.contains(searchQuery, ignoreCase = true) ||
            material.description.contains(searchQuery, ignoreCase = true) ||
            material.subject.contains(searchQuery, ignoreCase = true)
        val matchesSubject = selectedSubject == "All Subjects" || material.subject.equals(selectedSubject, true)
        val matchesGrade = studentGradeLevel.isNullOrBlank() ||
            material.gradeLevel.equals("All", true) ||
            material.gradeLevel.equals(studentGradeLevel, true)
        matchesSearch && matchesSubject && matchesGrade
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Materials") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudentColor,
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "Unable to load resources.",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = {
                            isLoading = true
                            errorMessage = null
                            materials = emptyList()
                        }) {
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
                                text = "Find materials tailored to your grade level and subjects.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Search title or description") },
                                singleLine = true
                            )

                            androidx.compose.foundation.layout.FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                subjects.forEach { subject ->
                                    val isSelected = selectedSubject == subject
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (isSelected) StudentColor else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        TextButton(onClick = { selectedSubject = subject }) {
                                            Text(
                                                text = subject,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (filteredMaterials.isEmpty()) {
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
                                    Icons.Default.Article,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "No study materials found.",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Try a different subject or search.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
                            items(filteredMaterials) { material ->
                                StudyMaterialCard(
                                    material = material,
                                    onOpenLink = {
                                        if (material.downloadUrl.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(material.downloadUrl))
                                            context.startActivity(intent)
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
private fun StudyMaterialCard(
    material: StudyMaterial,
    onOpenLink: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
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
                        text = material.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = material.subject,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StudentColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = material.resourceType,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = StudentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (material.description.isNotBlank()) {
                Text(
                    text = material.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Article, contentDescription = null, tint = StudentColor)
                Text(
                    text = "Grade: ${material.gradeLevel.ifBlank { "All" }}",
                    fontSize = 12.sp
                )
                Text(
                    text = "Uploaded: ${dateFormat.format(Date(material.uploadedAt))}",
                    fontSize = 12.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenLink,
                    modifier = Modifier.weight(1f),
                    enabled = material.downloadUrl.isNotBlank()
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open Resource")
                }
                TextButton(
                    onClick = onOpenLink,
                    enabled = material.downloadUrl.isNotBlank()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download")
                }
            }
        }
    }
}


