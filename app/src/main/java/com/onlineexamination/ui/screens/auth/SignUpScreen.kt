package com.onlineexamination.ui.screens.auth

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlineexamination.data.model.PsgcData
import com.onlineexamination.data.model.StudentInfo
import com.onlineexamination.data.model.TeacherInfo
import com.onlineexamination.data.model.UserRole
import com.onlineexamination.ui.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

// Lists for dropdown menus
private val gradeLevels = listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12")
private val employmentStatuses = listOf("Permanent", "Contractual", "Substitute", "Part-Time")
private val positions = listOf("Teacher I", "Teacher II", "Teacher III", "Master Teacher I", "Master Teacher II", "Master Teacher III", "Master Teacher IV", "Professor", "Instructor", "Others")
private val gradeLevelAssignments = listOf("Kindergarten", "Grade 1", "Grade 2", "Grade 3", "Grade 4", "Grade 5", "Grade 6", "Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12", "College")
private val educationalAttainments = listOf("High School Graduate", "Vocational", "Bachelor's Degree", "Master's Degree", "Doctorate", "Post-Graduate Studies")
private val genderOptions = listOf("Male", "Female")
private val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
private val relationshipTypes = listOf("Parent", "Sibling", "Grandparent", "Aunt/Uncle", "Legal Guardian", "Other")

// Validation and utility functions
private fun String.isMeaningful(): Boolean = this.isEmpty() || this.trim().isNotEmpty()
private fun String.trimmedOrEmpty(): String = this.trim()
private fun String.isValidEmail(): Boolean = Patterns.EMAIL_ADDRESS.matcher(this).matches()
private fun String.isStrongPassword(): Boolean = this.length >= 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToPendingVerification: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }

    var studentForm by remember { mutableStateOf(StudentForm()) }
    var teacherForm by remember { mutableStateOf(TeacherForm()) }

    var currentStep by remember { mutableStateOf(0) }

    val uiState by viewModel.uiState.collectAsState()
    val provinces = remember { PsgcData.provinces }

    val isStudent = selectedRole == UserRole.STUDENT
    val isTeacher = selectedRole == UserRole.TEACHER
    val totalSteps = when (selectedRole) {
        UserRole.STUDENT -> 5
        UserRole.TEACHER -> 6
        UserRole.ADMIN -> 1
    }
    val isLastStep = currentStep == totalSteps - 1

    LaunchedEffect(uiState.successMessage, uiState.currentUser) {
        if (uiState.successMessage != null && uiState.currentUser != null) {
            onNavigateToPendingVerification()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(selectedRole) {
        currentStep = 0
        if (selectedRole != UserRole.TEACHER) {
            teacherForm = teacherForm.copy(contactEmail = "")
        } else if (teacherForm.contactEmail.isBlank()) {
            teacherForm = teacherForm.copy(contactEmail = email)
        }
    }

    LaunchedEffect(email, selectedRole) {
        if (selectedRole == UserRole.TEACHER && teacherForm.contactEmail.isBlank()) {
            teacherForm = teacherForm.copy(contactEmail = email)
        }
    }

    fun isCurrentStepValid(): Boolean {
        return when (selectedRole) {
            UserRole.STUDENT -> when (currentStep) {
                0 -> email.isValidEmail() && password.isStrongPassword() && password == confirmPassword && username.isMeaningful()
                1 -> listOf(studentForm.lrn, studentForm.lastName, studentForm.firstName, studentForm.gender, studentForm.dateOfBirth, studentForm.placeOfBirth, studentForm.age, studentForm.nationality, studentForm.religion).all { it.trim().isNotEmpty() }
                2 -> listOf(studentForm.houseStreet, studentForm.barangayName, studentForm.provinceCode, studentForm.municipalityCode, studentForm.zipCode, studentForm.contactNumber).all { it.trim().isNotEmpty() }
                3 -> listOf(studentForm.fatherName, studentForm.fatherContact, studentForm.motherName, studentForm.motherContact).all { it.trim().isNotEmpty() }
                4 -> listOf(studentForm.gradeLevel, studentForm.schoolYear).all { it.trim().isNotEmpty() }
                else -> false
            }
            UserRole.TEACHER -> when (currentStep) {
                0 -> email.isValidEmail() && password.isStrongPassword() && password == confirmPassword && username.isMeaningful()
                1 -> listOf(teacherForm.employeeId, teacherForm.firstName, teacherForm.lastName, teacherForm.gender, teacherForm.dateOfBirth, teacherForm.age, teacherForm.nationality, teacherForm.civilStatus, teacherForm.religion).all { it.trim().isNotEmpty() }
                2 -> listOf(teacherForm.addressHouseStreet, teacherForm.addressBarangayName, teacherForm.provinceCode, teacherForm.municipalityCode, teacherForm.zipCode, teacherForm.mobileNumber).all { it.trim().isNotEmpty() }
                3 -> listOf(teacherForm.position, teacherForm.gradeLevelAssigned, teacherForm.department, teacherForm.schoolAssigned, teacherForm.employmentStatus).all { it.trim().isNotEmpty() }
                4 -> listOf(teacherForm.highestEducationalAttainment, teacherForm.degreeMajor, teacherForm.educationSchoolName, teacherForm.educationYearGraduated).all { it.trim().isNotEmpty() }
                5 -> listOf(teacherForm.emergencyContactName, teacherForm.emergencyContactNumber).all { it.trim().isNotEmpty() }
                else -> false
            }
            UserRole.ADMIN -> email.isValidEmail() && password.isStrongPassword() && password == confirmPassword && username.isMeaningful()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "App Logo",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Progress Indicator
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { (currentStep + 1).toFloat() / totalSteps },
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Step ${currentStep + 1} of $totalSteps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Stepper content
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (currentStep) {
                        0 -> AccountInformationStep(username, { if (it.isMeaningful()) username = it }, email, { email = it.replace(" ", "") }, password, { password = it }, confirmPassword, { confirmPassword = it }, selectedRole) {
                            selectedRole = it
                            if (it == UserRole.TEACHER && teacherForm.contactEmail.isBlank()) {
                                teacherForm = teacherForm.copy(contactEmail = email)
                            }
                        }
                        1 -> if (isStudent) StudentPersonalInformationStep(studentForm) { studentForm = it.copy(age = calculateAgeFromDob(it.dateOfBirth)) } else TeacherPersonalInformationStep(teacherForm) { teacherForm = it.copy(age = calculateAgeFromDob(it.dateOfBirth)) }
                        2 -> if (isStudent) StudentAddressInformationStep(studentForm, provinces) { studentForm = it } else TeacherContactInformationStep(teacherForm, provinces) { teacherForm = it }
                        3 -> if (isStudent) StudentParentInformationStep(studentForm) { studentForm = it } else TeacherProfessionalInformationStep(teacherForm) { teacherForm = it }
                        4 -> if (isStudent) StudentSchoolInformationStep(studentForm) { studentForm = it } else TeacherEducationInformationStep(teacherForm) { teacherForm = it }
                        5 -> TeacherHealthAndUploadsStep(teacherForm) { teacherForm = it }
                    }
                }
            }


            // Error and Success Messages
            uiState.errorMessage?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp), textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (currentStep > 0) Arrangement.SpaceBetween else Arrangement.End
            ) {
                if (currentStep > 0) {
                    OutlinedButton(onClick = { currentStep-- }) { Text("Back") }
                }

                Button(
                    onClick = {
                        if (isLastStep) {
                            val studentInfo = if (isStudent) studentForm.toStudentInfo() else null
                            val teacherInfo = if (isTeacher) teacherForm.toTeacherInfo(email) else null
                            viewModel.signUp(email.trim(), password, username.trimmedOrEmpty(), selectedRole, studentInfo, teacherInfo)
                        } else {
                            currentStep++
                        }
                    },
                    enabled = !uiState.isLoading && isCurrentStepValid()
                ) {
                    if (uiState.isLoading && isLastStep) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Text(if (isLastStep) "Sign Up" else "Next")
                    }
                }
            }

            TextButton(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), enabled = !uiState.isLoading) {
                Text("Already have an account? Sign In", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Step Composables ---
@Composable
private fun AccountInformationStep(
    username: String, onUsernameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    confirmPassword: String, onConfirmPasswordChange: (String) -> Unit,
    selectedRole: UserRole, onRoleSelected: (UserRole) -> Unit
) {
    SectionHeader("Account Information")
    LabeledField("Username", username, leadingIcon = { Icon(Icons.Default.Person, null) }) { onUsernameChange(it) }
    LabeledField("Email", email, KeyboardType.Email, leadingIcon = { Icon(Icons.Default.Email, null) }) { onEmailChange(it) }
    PasswordField("Password", password, onPasswordChange)
    PasswordField("Confirm Password", confirmPassword, onConfirmPasswordChange)

    if (password.isNotEmpty() && !password.isStrongPassword()) {
        Text("Password must be at least 6 characters.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
    }
    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
        Text("Passwords do not match.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
    }

    Text("Select Role", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        UserRole.values().filter { it != UserRole.ADMIN }.forEach { role ->
            FilterChip(selected = selectedRole == role, onClick = { onRoleSelected(role) }, label = { Text(role.name) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StudentPersonalInformationStep(form: StudentForm, onFormChange: (StudentForm) -> Unit) {
    SectionHeader("Personal Information")
    LabeledField("Learner Reference Number (LRN)", form.lrn, KeyboardType.Number) { onFormChange(form.copy(lrn = it)) }
    TwoColumn(
        { LabeledField("Last Name", form.lastName) { onFormChange(form.copy(lastName = it)) } },
        { LabeledField("First Name", form.firstName) { onFormChange(form.copy(firstName = it)) } }
    )
    TwoColumn(
        { LabeledField("Middle Name (Optional)", form.middleName) { onFormChange(form.copy(middleName = it)) } },
        { LabeledField("Suffix (Optional)", form.suffix) { onFormChange(form.copy(suffix = it)) } }
    )
    GenderSelection(form.gender) { onFormChange(form.copy(gender = it)) }
    TwoColumn(
        { DatePickerField("Date of Birth", form.dateOfBirth) { onFormChange(form.copy(dateOfBirth = it, age = calculateAgeFromDob(it))) } },
        { ReadOnlyField("Age", form.age) }
    )
    LabeledField("Place of Birth", form.placeOfBirth) { onFormChange(form.copy(placeOfBirth = it)) }
    LabeledField("Nationality", form.nationality) { onFormChange(form.copy(nationality = it)) }
    LabeledField("Religion", form.religion) { onFormChange(form.copy(religion = it)) }
}

@Composable
private fun StudentAddressInformationStep(form: StudentForm, provinces: List<com.onlineexamination.data.model.Province>, onFormChange: (StudentForm) -> Unit) {
    SectionHeader("Home Address")
    LabeledField("House No. / Street", form.houseStreet) { onFormChange(form.copy(houseStreet = it)) }
    PsgcDropdowns(
        provinces = provinces,
        selectedProvinceCode = form.provinceCode,
        selectedMunicipalityCode = form.municipalityCode,
        selectedBarangayCode = form.barangayCode,
        onProvinceSelected = { province -> onFormChange(form.copy(provinceCode = province.key, provinceName = province.label, municipalityCode = "", municipalityName = "", barangayCode = "", barangayName = "")) },
        onMunicipalitySelected = { municipality -> onFormChange(form.copy(municipalityCode = municipality.key, municipalityName = municipality.label, barangayCode = "", barangayName = "")) },
        onBarangaySelected = { barangay -> onFormChange(form.copy(barangayCode = barangay.key, barangayName = barangay.label)) }
    )
    TwoColumn(
        { LabeledField("ZIP Code", form.zipCode, KeyboardType.Number) { onFormChange(form.copy(zipCode = it)) } },
        { LabeledField("Contact Number", form.contactNumber, KeyboardType.Phone) { onFormChange(form.copy(contactNumber = it)) } }
    )
}

@Composable
private fun StudentParentInformationStep(form: StudentForm, onFormChange: (StudentForm) -> Unit) {
    SectionHeader("Parent / Guardian Information")
    Text("Father's Information", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
    LabeledField("Full Name", form.fatherName) { onFormChange(form.copy(fatherName = it)) }
    TwoColumn(
        { LabeledField("Occupation", form.fatherOccupation) { onFormChange(form.copy(fatherOccupation = it)) } },
        { LabeledField("Contact Number", form.fatherContact, KeyboardType.Phone) { onFormChange(form.copy(fatherContact = it)) } }
    )
    Text("Mother's Information", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
    LabeledField("Maiden Name", form.motherName) { onFormChange(form.copy(motherName = it)) }
    TwoColumn(
        { LabeledField("Occupation", form.motherOccupation) { onFormChange(form.copy(motherOccupation = it)) } },
        { LabeledField("Contact Number", form.motherContact, KeyboardType.Phone) { onFormChange(form.copy(motherContact = it)) } }
    )
    Text("Guardian (Optional)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
    LabeledField("Full Name", form.guardianName) { onFormChange(form.copy(guardianName = it)) }
    TwoColumn(
        { DropdownField("Relationship", form.guardianRelationship, relationshipTypes.map { DropdownOption(it, it) }) { onFormChange(form.copy(guardianRelationship = it.key)) } },
        { LabeledField("Contact Number", form.guardianContact, KeyboardType.Phone) { onFormChange(form.copy(guardianContact = it)) } }
    )
    LabeledField("Address", form.guardianAddress) { onFormChange(form.copy(guardianAddress = it)) }
}

@Composable
private fun StudentSchoolInformationStep(form: StudentForm, onFormChange: (StudentForm) -> Unit) {
    SectionHeader("School Information")
    DropdownField("Grade Level to Enroll", form.gradeLevel, gradeLevels.map { DropdownOption(it, it) }) { onFormChange(form.copy(gradeLevel = it.key)) }
    LabeledField("School Year", form.schoolYear) { onFormChange(form.copy(schoolYear = it)) }
    LabeledField("Section (if assigned)", form.section) { onFormChange(form.copy(section = it)) }
    LabeledField("Previous School", form.previousSchool) { onFormChange(form.copy(previousSchool = it)) }
    LabeledField("Previous School Address", form.previousSchoolAddress) { onFormChange(form.copy(previousSchoolAddress = it)) }
    LabeledField("School ID (optional)", form.schoolId) { onFormChange(form.copy(schoolId = it)) }
    LabeledField("School Type", form.schoolType) { onFormChange(form.copy(schoolType = it)) }
}

@Composable
private fun TeacherPersonalInformationStep(form: TeacherForm, onFormChange: (TeacherForm) -> Unit) {
    SectionHeader("Personal Information")
    LabeledField("Employee ID / Teacher ID", form.employeeId) { onFormChange(form.copy(employeeId = it)) }
    TwoColumn(
        { LabeledField("Last Name", form.lastName) { onFormChange(form.copy(lastName = it)) } },
        { LabeledField("First Name", form.firstName) { onFormChange(form.copy(firstName = it)) } }
    )
    TwoColumn(
        { LabeledField("Middle Name (Optional)", form.middleName) { onFormChange(form.copy(middleName = it)) } },
        { LabeledField("Suffix (Optional)", form.suffix) { onFormChange(form.copy(suffix = it)) } }
    )
    GenderSelection(form.gender) { onFormChange(form.copy(gender = it)) }
    TwoColumn(
        { DatePickerField("Date of Birth", form.dateOfBirth) { onFormChange(form.copy(dateOfBirth = it, age = calculateAgeFromDob(it))) } },
        { ReadOnlyField("Age", form.age) }
    )
    TwoColumn(
        { LabeledField("Nationality", form.nationality) { onFormChange(form.copy(nationality = it)) } },
        { LabeledField("Civil Status", form.civilStatus) { onFormChange(form.copy(civilStatus = it)) } }
    )
    LabeledField("Religion", form.religion) { onFormChange(form.copy(religion = it)) }
}

@Composable
private fun TeacherContactInformationStep(form: TeacherForm, provinces: List<com.onlineexamination.data.model.Province>, onFormChange: (TeacherForm) -> Unit) {
    SectionHeader("Contact Information")
    LabeledField("House No. / Street", form.addressHouseStreet) { onFormChange(form.copy(addressHouseStreet = it)) }
     PsgcDropdowns(
        provinces = provinces,
        selectedProvinceCode = form.provinceCode,
        selectedMunicipalityCode = form.municipalityCode,
        selectedBarangayCode = form.addressBarangayCode,
        onProvinceSelected = { province -> onFormChange(form.copy(provinceCode = province.key, provinceName = province.label, municipalityCode = "", municipalityName = "", addressBarangayCode = "", addressBarangayName = "")) },
        onMunicipalitySelected = { municipality -> onFormChange(form.copy(municipalityCode = municipality.key, municipalityName = municipality.label, addressBarangayCode = "", addressBarangayName = "")) },
        onBarangaySelected = { barangay -> onFormChange(form.copy(addressBarangayCode = barangay.key, addressBarangayName = barangay.label)) }
    )
    TwoColumn(
        { LabeledField("ZIP Code", form.zipCode, KeyboardType.Number) { onFormChange(form.copy(zipCode = it)) } },
        { LabeledField("Mobile Number", form.mobileNumber, KeyboardType.Phone) { onFormChange(form.copy(mobileNumber = it)) } }
    )
    TwoColumn(
        { LabeledField("Landline (Optional)", form.landlineNumber, KeyboardType.Phone) { onFormChange(form.copy(landlineNumber = it)) } },
        { LabeledField("Email Address", form.contactEmail, KeyboardType.Email) { onFormChange(form.copy(contactEmail = it)) } }
    )
}

@Composable
private fun TeacherProfessionalInformationStep(form: TeacherForm, onFormChange: (TeacherForm) -> Unit) {
    SectionHeader("Professional Information")
    DropdownField("Position / Designation", form.position, positions.map { DropdownOption(it, it) }) { onFormChange(form.copy(position = it.key)) }
    LabeledField("Subjects Handled", form.subjectsHandled) { onFormChange(form.copy(subjectsHandled = it)) }
    DropdownField("Grade Level Assigned", form.gradeLevelAssigned, gradeLevelAssignments.map { DropdownOption(it, it) }) { onFormChange(form.copy(gradeLevelAssigned = it.key)) }
    LabeledField("Department / Strand", form.department) { onFormChange(form.copy(department = it)) }
    LabeledField("School Assigned", form.schoolAssigned) { onFormChange(form.copy(schoolAssigned = it)) }
    LabeledField("PRC License Number", form.prcLicenseNumber) { onFormChange(form.copy(prcLicenseNumber = it)) }
    LabeledField("Civil Service Eligibility", form.civilServiceEligibility) { onFormChange(form.copy(civilServiceEligibility = it)) }
    DropdownField("Employment Status", form.employmentStatus, employmentStatuses.map { DropdownOption(it, it) }) { onFormChange(form.copy(employmentStatus = it.key)) }
}

@Composable
private fun TeacherEducationInformationStep(form: TeacherForm, onFormChange: (TeacherForm) -> Unit) {
    SectionHeader("Education Background")
    DropdownField("Highest Educational Attainment", form.highestEducationalAttainment, educationalAttainments.map { DropdownOption(it, it) }) { onFormChange(form.copy(highestEducationalAttainment = it.key)) }
    LabeledField("Degree / Major", form.degreeMajor) { onFormChange(form.copy(degreeMajor = it)) }
    LabeledField("School Name", form.educationSchoolName) { onFormChange(form.copy(educationSchoolName = it)) }
    LabeledField("Year Graduated", form.educationYearGraduated, KeyboardType.Number) { onFormChange(form.copy(educationYearGraduated = it)) }
    LabeledField("Trainings / Seminars (Optional)", form.trainings) { onFormChange(form.copy(trainings = it)) }
}

@Composable
private fun TeacherHealthAndUploadsStep(form: TeacherForm, onFormChange: (TeacherForm) -> Unit) {
    SectionHeader("Health & Emergency")
    TwoColumn(
        { DropdownField("Blood Type", form.bloodType, bloodTypes.map { DropdownOption(it, it) }) { onFormChange(form.copy(bloodType = it.key)) } },
        { LabeledField("Vaccination Status", form.vaccinationStatus) { onFormChange(form.copy(vaccinationStatus = it)) } }
    )
    LabeledField("Allergies / Medical Conditions (Optional)", form.allergies) { onFormChange(form.copy(allergies = it)) }
    SectionHeader("Emergency Contact")
    LabeledField("Full Name", form.emergencyContactName) { onFormChange(form.copy(emergencyContactName = it)) }
    TwoColumn(
        { DropdownField("Relationship", form.emergencyContactRelationship, relationshipTypes.map { DropdownOption(it, it) }) { onFormChange(form.copy(emergencyContactRelationship = it.key)) } },
        { LabeledField("Contact Number", form.emergencyContactNumber, KeyboardType.Phone) { onFormChange(form.copy(emergencyContactNumber = it)) } }
    )

    SectionHeader("File Uploads (Link or Reference)")
    LabeledField("PRC License / Eligibility", form.prcLicenseFile) { onFormChange(form.copy(prcLicenseFile = it)) }
    LabeledField("Diploma / Transcript", form.diplomaFile) { onFormChange(form.copy(diplomaFile = it)) }
    LabeledField("1x1 ID Photo", form.idPhotoFile) { onFormChange(form.copy(idPhotoFile = it)) }
}

// --- Reusable Composables ---
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    )
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.isMeaningful()) onChange(it)
            else if (it.isEmpty()) onChange("")
        },
        label = { Text(label) },
        leadingIcon = leadingIcon,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

@Composable
private fun ReadOnlyField(label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        enabled = false,
        readOnly = true,
        colors = TextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            disabledIndicatorColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun TwoColumn(left: @Composable () -> Unit, right: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) { left() }
        Column(modifier = Modifier.weight(1f)) { right() }
    }
}

@Composable
private fun PsgcDropdowns(
    provinces: List<com.onlineexamination.data.model.Province>,
    selectedProvinceCode: String,
    selectedMunicipalityCode: String,
    selectedBarangayCode: String,
    onProvinceSelected: (DropdownOption) -> Unit,
    onMunicipalitySelected: (DropdownOption) -> Unit,
    onBarangaySelected: (DropdownOption) -> Unit
) {
    val provinceOptions = provinces.map { DropdownOption(it.code, it.name) }
    DropdownField("Province", selectedProvinceCode, provinceOptions) { onProvinceSelected(it) }

    val selectedProvince = provinces.find { it.code == selectedProvinceCode }
    val municipalityOptions = selectedProvince?.municipalities?.map { DropdownOption(it.code, it.name) } ?: emptyList()
    DropdownField("Municipality / City", selectedMunicipalityCode, municipalityOptions, enabled = selectedProvince != null) { onMunicipalitySelected(it) }

    val selectedMunicipality = selectedProvince?.municipalities?.find { it.code == selectedMunicipalityCode }
    val barangayOptions = selectedMunicipality?.barangays?.map { DropdownOption(it.code, it.name) } ?: emptyList()
    DropdownField("Barangay", selectedBarangayCode, barangayOptions, enabled = selectedMunicipality != null) { onBarangaySelected(it) }
}


@Composable
private fun DropdownField(
    label: String,
    selectedKey: String,
    options: List<DropdownOption>,
    enabled: Boolean = true,
    onOptionSelected: (DropdownOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.key == selectedKey }?.label ?: ""
    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp)) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { textFieldSize = it.size },
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    "Dropdown",
                    Modifier.clickable(enabled = enabled) { if (options.isNotEmpty()) expanded = true }
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(density) { textFieldSize.width.toDp() })
        ) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option.label) }, onClick = {
                    onOptionSelected(option)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun GenderSelection(selectedGender: String, onGenderChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text("Gender / Sex", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            genderOptions.forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onGenderChange(option) }) {
                    RadioButton(selected = selectedGender == option, onClick = { onGenderChange(option) })
                    Text(option, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(label: String, value: String, onValueChange: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            trailingIcon = { Icon(Icons.Default.CalendarMonth, "Select Date") },
            colors = TextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                disabledIndicatorColor = MaterialTheme.colorScheme.outline
            )
        )
        Box(modifier = Modifier
            .matchParentSize()
            .clickable { showDialog = true })
    }

    if (showDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = parseDateToMillis(value))
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Date") },
            text = { DatePicker(state = datePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onValueChange(formatter.format(Date(it))) }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun PasswordField(label: String, value: String, onValueChange: (String) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, null) },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff, if (visible) "Hide" else "Show")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true
    )
}

// --- Data Classes and Mappers ---
private data class DropdownOption(val key: String, val label: String)

data class StudentForm(
    val lrn: String = "", val lastName: String = "", val firstName: String = "", val middleName: String = "",
    val suffix: String = "", val gender: String = "", val dateOfBirth: String = "", val placeOfBirth: String = "",
    val age: String = "", val nationality: String = "", val religion: String = "", val houseStreet: String = "",
    val barangayCode: String = "", val barangayName: String = "", val provinceCode: String = "", val provinceName: String = "",
    val municipalityCode: String = "", val municipalityName: String = "", val zipCode: String = "", val contactNumber: String = "",
    val contactEmail: String = "", val fatherName: String = "", val fatherOccupation: String = "", val fatherContact: String = "",
    val motherName: String = "", val motherOccupation: String = "", val motherContact: String = "", val guardianName: String = "",
    val guardianRelationship: String = "", val guardianContact: String = "", val guardianAddress: String = "",
    val gradeLevel: String = "", val section: String = "", val previousSchool: String = "", val previousSchoolAddress: String = "",
    val schoolYear: String = "", val schoolId: String = "", val schoolType: String = ""
) {
    fun toStudentInfo(): StudentInfo = StudentInfo(
        lrn = lrn.trimmedOrEmpty(), lastName = lastName.trimmedOrEmpty(), firstName = firstName.trimmedOrEmpty(), middleName = middleName.trimmedOrEmpty(),
        suffix = suffix.trimmedOrEmpty(), sex = gender, dateOfBirth = dateOfBirth, placeOfBirth = placeOfBirth.trimmedOrEmpty(),
        age = age, religion = religion.trimmedOrEmpty(), nationality = nationality.trimmedOrEmpty(), houseStreet = houseStreet.trimmedOrEmpty(),
        barangay = barangayName.trimmedOrEmpty(), cityMunicipality = municipalityName, province = provinceName,
        zipCode = zipCode.trimmedOrEmpty(), contactNumber = contactNumber.trimmedOrEmpty(), emailAddress = contactEmail.trim(),
        fatherFullName = fatherName.trimmedOrEmpty(), fatherOccupation = fatherOccupation.trimmedOrEmpty(), fatherContact = fatherContact.trimmedOrEmpty(),
        motherMaidenName = motherName.trimmedOrEmpty(), motherOccupation = motherOccupation.trimmedOrEmpty(), motherContact = motherContact.trimmedOrEmpty(),
        guardianFullName = guardianName.trimmedOrEmpty(), guardianRelationship = guardianRelationship.trimmedOrEmpty(), guardianContact = guardianContact.trimmedOrEmpty(),
        guardianAddress = guardianAddress.trimmedOrEmpty(), gradeLevelToEnroll = gradeLevel.trimmedOrEmpty(), schoolYear = schoolYear.trimmedOrEmpty(),
        section = section.trimmedOrEmpty(), previousSchool = previousSchool.trimmedOrEmpty(), previousSchoolAddress = previousSchoolAddress.trimmedOrEmpty(),
        schoolId = schoolId.trimmedOrEmpty(), schoolType = schoolType.trimmedOrEmpty()
    )
}

data class TeacherForm(
    val employeeId: String = "", val firstName: String = "", val lastName: String = "", val middleName: String = "",
    val suffix: String = "", val gender: String = "", val dateOfBirth: String = "", val age: String = "",
    val nationality: String = "", val civilStatus: String = "", val religion: String = "", val addressHouseStreet: String = "",
    val addressBarangayCode: String = "", val addressBarangayName: String = "", val provinceCode: String = "", val provinceName: String = "",
    val municipalityCode: String = "", val municipalityName: String = "", val zipCode: String = "", val mobileNumber: String = "",
    val landlineNumber: String = "", val contactEmail: String = "", val position: String = "", val subjectsHandled: String = "",
    val gradeLevelAssigned: String = "", val department: String = "", val schoolAssigned: String = "",
    val prcLicenseNumber: String = "", val civilServiceEligibility: String = "", val employmentStatus: String = "",
    val highestEducationalAttainment: String = "", val degreeMajor: String = "", val educationSchoolName: String = "",
    val educationYearGraduated: String = "", val trainings: String = "", val bloodType: String = "", val allergies: String = "",
    val emergencyContactName: String = "", val emergencyContactRelationship: String = "", val emergencyContactNumber: String = "",
    val vaccinationStatus: String = "", val prcLicenseFile: String = "", val diplomaFile: String = "", val idPhotoFile: String = ""
) {
    fun toTeacherInfo(email: String): TeacherInfo = TeacherInfo(
        employeeId = employeeId.trimmedOrEmpty(),
        fullName = buildFullName(firstName.trimmedOrEmpty(), middleName.trimmedOrEmpty(), lastName.trimmedOrEmpty(), suffix.trimmedOrEmpty()),
        suffix = suffix.trimmedOrEmpty(), gender = gender, dateOfBirth = dateOfBirth, age = age, nationality = nationality.trimmedOrEmpty(),
        civilStatus = civilStatus.trimmedOrEmpty(), religion = religion.trimmedOrEmpty(), addressHouseStreet = addressHouseStreet.trimmedOrEmpty(),
        addressBarangay = addressBarangayName.trimmedOrEmpty(), addressCityMunicipality = municipalityName, addressProvince = provinceName,
        addressZipCode = zipCode.trimmedOrEmpty(), mobileNumber = mobileNumber.trimmedOrEmpty(), landlineNumber = landlineNumber.trimmedOrEmpty(),
        contactEmail = contactEmail.trim().ifBlank { email.trim() }, position = position.trimmedOrEmpty(),
        subjectsHandled = subjectsHandled.trimmedOrEmpty(), gradeLevelAssigned = gradeLevelAssigned.trimmedOrEmpty(),
        department = department.trimmedOrEmpty(), schoolAssigned = schoolAssigned.trimmedOrEmpty(), prcLicenseNumber = prcLicenseNumber.trimmedOrEmpty(),
        civilServiceEligibility = civilServiceEligibility.trimmedOrEmpty(), employmentStatus = employmentStatus.trimmedOrEmpty(),
        highestEducationalAttainment = highestEducationalAttainment.trimmedOrEmpty(), degreeMajor = degreeMajor.trimmedOrEmpty(),
        educationSchoolName = educationSchoolName.trimmedOrEmpty(), educationYearGraduated = educationYearGraduated.trimmedOrEmpty(),
        trainings = trainings.trimmedOrEmpty(), bloodType = bloodType.trimmedOrEmpty(), allergies = allergies.trimmedOrEmpty(),
        emergencyContactName = emergencyContactName.trimmedOrEmpty(), emergencyContactRelationship = emergencyContactRelationship.trimmedOrEmpty(),
        emergencyContactNumber = emergencyContactNumber.trimmedOrEmpty(), vaccinationStatus = vaccinationStatus.trimmedOrEmpty(),
        prcLicenseFile = prcLicenseFile.trimmedOrEmpty(), diplomaFile = diplomaFile.trimmedOrEmpty(), idPhotoFile = idPhotoFile.trimmedOrEmpty()
    )
}

// --- Helper Functions ---
private fun buildFullName(firstName: String, middleName: String, lastName: String, suffix: String): String {
    return listOf(firstName, middleName, lastName, suffix).filter { it.isNotEmpty() }.joinToString(" ")
}

private fun calculateAgeFromDob(dateString: String): String {
    return try {
        val parsed = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).apply { isLenient = false }.parse(dateString) ?: return ""
        val birthCal = Calendar.getInstance().apply { time = parsed }
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        age.coerceAtLeast(0).toString()
    } catch (e: Exception) { "" }
}

private fun parseDateToMillis(dateString: String): Long? {
    return try {
        if (dateString.isBlank()) return null
        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).apply { isLenient = false }.parse(dateString)?.time
    } catch (e: Exception) { null }
}
