package com.onlineexamination.ui.screens.profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import com.onlineexamination.data.model.User
import com.onlineexamination.data.model.UserRole
import com.onlineexamination.data.model.StudentInfo
import com.onlineexamination.data.model.TeacherInfo
import com.onlineexamination.ui.theme.StudentColor
import com.onlineexamination.ui.theme.TeacherColor
import com.onlineexamination.ui.theme.AdminColor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User,
    onBack: () -> Unit,
    onProfileUpdated: () -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var studentInfo by remember { mutableStateOf<StudentInfo?>(null) }
    var teacherInfo by remember { mutableStateOf<TeacherInfo?>(null) }
    var isLoadingData by remember { mutableStateOf(true) }

    var username by remember { mutableStateOf(user.username) }
    var studentContact by remember { mutableStateOf("") }
    var studentEmail by remember { mutableStateOf("") }
    var studentSection by remember { mutableStateOf("") }

    var teacherMobile by remember { mutableStateOf("") }
    var teacherEmail by remember { mutableStateOf("") }
    var teacherSubjects by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val photoPickerAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val legacyPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    var hasStoragePermission by remember {
        mutableStateOf(photoPickerAvailable || legacyPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }

    val roleColor = when (user.role) {
        UserRole.STUDENT -> StudentColor
        UserRole.TEACHER -> TeacherColor
        UserRole.ADMIN -> AdminColor
    }

    val firestore = remember { FirebaseFirestore.getInstance() }
    val storage = remember { FirebaseStorage.getInstance() }
    val coroutineScope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    val legacyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        hasStoragePermission = granted
        if (granted) {
            legacyPickerLauncher.launch("image/*")
        } else {
            errorMessage = "Please allow photo access to update your profile picture."
        }
    }

    LaunchedEffect(user.uid) {
        try {
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
            isLoadingData = false
        } catch (e: Exception) {
            isLoadingData = false
        }
    }

    LaunchedEffect(studentInfo) {
        studentInfo?.let {
            studentContact = it.contactNumber
            studentEmail = it.emailAddress.ifBlank { user.email }
            studentSection = it.section
        }
    }

    LaunchedEffect(teacherInfo) {
        teacherInfo?.let {
            teacherMobile = it.mobileNumber
            teacherEmail = it.contactEmail.ifBlank { user.email }
            teacherSubjects = it.subjectsHandled
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = roleColor,
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
        if (isLoadingData) {
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box {
                            Surface(
                                shape = CircleShape,
                                color = roleColor.copy(alpha = 0.1f),
                                modifier = Modifier.size(120.dp)
                            ) {
                                if (selectedImageUri != null) {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "Selected profile photo",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else if (user.photoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = user.photoUrl,
                                        contentDescription = "Profile photo",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.username.take(1).uppercase(),
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = roleColor
                                        )
                                    }
                                }
                            }

                            FloatingActionButton(
                                onClick = {
                                    if (photoPickerAvailable) {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    } else {
                                        if (hasStoragePermission) {
                                            legacyPickerLauncher.launch("image/*")
                                        } else {
                                            permissionLauncher.launch(legacyPermissions)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(40.dp),
                                containerColor = roleColor,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Change Picture",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Tap the camera to update your photo",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                item { Divider() }

                item {
                    Text(
                        text = "Profile Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Display name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                }

                when (user.role) {
                    UserRole.STUDENT -> {
                        studentInfo?.let {
                            item {
                                OutlinedTextField(
                                    value = studentContact,
                                    onValueChange = { studentContact = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Contact Number") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = studentEmail,
                                    onValueChange = { studentEmail = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Contact Email") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = studentSection,
                                    onValueChange = { studentSection = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Section") },
                                    leadingIcon = { Icon(Icons.Default.Class, contentDescription = null) }
                                )
                            }
                        }
                    }
                    UserRole.TEACHER -> {
                        teacherInfo?.let {
                            item {
                                OutlinedTextField(
                                    value = teacherMobile,
                                    onValueChange = { teacherMobile = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Mobile Number") },
                                    leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) }
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = teacherEmail,
                                    onValueChange = { teacherEmail = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Contact Email") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = teacherSubjects,
                                    onValueChange = { teacherSubjects = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Subjects Handled") },
                                    leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null) }
                                )
                            }
                        }
                    }
                    else -> {
                        item {
                            Text(
                                text = "Admins can update their display name and profile photo.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    errorMessage?.let { error ->
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

                item {
                    successMessage?.let { success ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = success,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null
                                successMessage = null
                                try {
                                    val userUpdates = mutableMapOf<String, Any>(
                                        "username" to username.trim()
                                    )
                                    if (selectedImageUri != null) {
                                        val ref = storage.reference.child("profileImages/${user.uid}.jpg")
                                        ref.putFile(selectedImageUri!!).await()
                                        val downloadUrl = ref.downloadUrl.await().toString()
                                        userUpdates["photoUrl"] = downloadUrl
                                    }
                                    firestore.collection("users")
                                        .document(user.uid)
                                        .update(userUpdates as Map<String, Any>)
                                        .await()

                                    when (user.role) {
                                        UserRole.STUDENT -> {
                                            studentInfo?.let {
                                                firestore.collection("users")
                                                    .document(user.uid)
                                                    .collection("profiles")
                                                    .document("student")
                                                    .update(
                                                        mapOf(
                                                            "contactNumber" to studentContact,
                                                            "emailAddress" to studentEmail,
                                                            "section" to studentSection
                                                        )
                                                    )
                                                    .await()
                                            }
                                        }
                                        UserRole.TEACHER -> {
                                            teacherInfo?.let {
                                                firestore.collection("users")
                                                    .document(user.uid)
                                                    .collection("profiles")
                                                    .document("teacher")
                                                    .update(
                                                        mapOf(
                                                            "mobileNumber" to teacherMobile,
                                                            "contactEmail" to teacherEmail,
                                                            "subjectsHandled" to teacherSubjects
                                                        )
                                                    )
                                                    .await()
                                            }
                                        }
                                        else -> Unit
                                    }

                                    successMessage = "Profile updated successfully!"
                                    onProfileUpdated()
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Failed to update profile."
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = roleColor)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}


