package com.onlineexamination.ui.screens.profile

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
import coil.compose.AsyncImage
import com.onlineexamination.data.model.User
import com.onlineexamination.data.model.UserRole
import com.onlineexamination.data.model.StudentInfo
import com.onlineexamination.data.model.TeacherInfo
import com.onlineexamination.data.repository.AuthRepository
import com.onlineexamination.ui.theme.StudentColor
import com.onlineexamination.ui.theme.TeacherColor
import com.onlineexamination.ui.theme.AdminColor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    onBack: () -> Unit,
    onEditProfile: () -> Unit = {}
) {
    var studentInfo by remember { mutableStateOf<StudentInfo?>(null) }
    var teacherInfo by remember { mutableStateOf<TeacherInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val roleColor = when (user.role) {
        UserRole.STUDENT -> StudentColor
        UserRole.TEACHER -> TeacherColor
        UserRole.ADMIN -> AdminColor
    }

    LaunchedEffect(user.uid) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            when (user.role) {
                UserRole.STUDENT -> {
                    val profileDoc = firestore.collection("users")
                        .document(user.uid)
                        .collection("profiles")
                        .document("student")
                        .get()
                        .await()
                    studentInfo = profileDoc.toObject(StudentInfo::class.java)
                }
                UserRole.TEACHER -> {
                    val profileDoc = firestore.collection("users")
                        .document(user.uid)
                        .collection("profiles")
                        .document("teacher")
                        .get()
                        .await()
                    teacherInfo = profileDoc.toObject(TeacherInfo::class.java)
                }
                else -> {}
            }
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = roleColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditProfile() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
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
                    ProfileHeaderCard(user = user, roleColor = roleColor)
                }

                when (user.role) {
                    UserRole.STUDENT -> {
                        studentInfo?.let { info ->
                            item { SectionHeader("Personal Information") }
                            item { InfoRow("LRN", info.lrn) }
                            item { InfoRow("Full Name", "${info.lastName}, ${info.firstName} ${info.middleName}".trim()) }
                            item { InfoRow("Suffix", info.suffix.ifBlank { "N/A" }) }
                            item { InfoRow("Gender", info.sex) }
                            item { InfoRow("Date of Birth", info.dateOfBirth) }
                            item { InfoRow("Age", info.age) }
                            item { InfoRow("Nationality", info.nationality) }
                            item { InfoRow("Religion", info.religion) }

                            item { SectionHeader("Address & Contact") }
                            item { InfoRow("Address", "${info.houseStreet}, ${info.barangay}") }
                            item { InfoRow("City/Municipality", info.cityMunicipality) }
                            item { InfoRow("Province", info.province) }
                            item { InfoRow("ZIP Code", info.zipCode) }
                            item { InfoRow("Contact Number", info.contactNumber) }
                            item { InfoRow("Email", info.emailAddress.ifBlank { user.email }) }

                            item { SectionHeader("School Information") }
                            item { InfoRow("Grade Level", info.gradeLevelToEnroll) }
                            item { InfoRow("Section", info.section.ifBlank { "N/A" }) }
                            item { InfoRow("School Year", info.schoolYear) }
                            item { InfoRow("Previous School", info.previousSchool.ifBlank { "N/A" }) }
                            item { InfoRow("School Type", info.schoolType) }
                        }
                    }
                    UserRole.TEACHER -> {
                        teacherInfo?.let { info ->
                            item { SectionHeader("Personal Information") }
                            item { InfoRow("Employee ID", info.employeeId) }
                            item { InfoRow("Full Name", info.fullName) }
                            item { InfoRow("Gender", info.gender) }
                            item { InfoRow("Date of Birth", info.dateOfBirth) }
                            item { InfoRow("Age", info.age) }
                            item { InfoRow("Nationality", info.nationality) }
                            item { InfoRow("Civil Status", info.civilStatus) }
                            item { InfoRow("Religion", info.religion) }

                            item { SectionHeader("Contact Information") }
                            item { InfoRow("Address", "${info.addressHouseStreet}, ${info.addressBarangay}") }
                            item { InfoRow("City/Municipality", info.addressCityMunicipality) }
                            item { InfoRow("Province", info.addressProvince) }
                            item { InfoRow("ZIP Code", info.addressZipCode) }
                            item { InfoRow("Mobile", info.mobileNumber) }
                            item { InfoRow("Email", info.contactEmail.ifBlank { user.email }) }

                            item { SectionHeader("Professional Information") }
                            item { InfoRow("Position", info.position) }
                            item { InfoRow("Subjects Handled", info.subjectsHandled) }
                            item { InfoRow("Grade Level Assigned", info.gradeLevelAssigned) }
                            item { InfoRow("Department", info.department) }
                            item { InfoRow("School Assigned", info.schoolAssigned) }
                            item { InfoRow("PRC License", info.prcLicenseNumber.ifBlank { "N/A" }) }
                            item { InfoRow("Employment Status", info.employmentStatus) }
                        }
                    }
                    else -> {
                        item {
                            Text(
                                text = "Admin profile information",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeaderCard(user: User, roleColor: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = roleColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (user.photoUrl.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = roleColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(90.dp)
                ) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = roleColor,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.username.take(1).uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            Text(
                text = user.username,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = roleColor
            )
            Text(
                text = user.email,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = roleColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = user.role.name,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = roleColor
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (user.emailVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = if (user.emailVerified) "Email Verified" else "Email Not Verified",
                    fontSize = 12.sp,
                    color = if (user.emailVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value.ifBlank { "N/A" },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}


