package com.onlineexamination.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val role: UserRole = UserRole.STUDENT,
    val photoUrl: String = "",
    val emailVerified: Boolean = false,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val fcmToken: String = "",
    // Leaderboard stats
    val totalScore: Int = 0,
    val examsTaken: Int = 0
)

enum class UserRole {
    STUDENT,
    TEACHER,
    ADMIN
}

data class StudentInfo(
    val lrn: String = "",
    val lastName: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val suffix: String = "",
    val sex: String = "", // Male/Female
    val dateOfBirth: String = "", // MM/DD/YYYY
    val placeOfBirth: String = "",
    val age: String = "",
    val religion: String = "",
    val nationality: String = "",
    // Address & Contact
    val houseStreet: String = "",
    val barangay: String = "",
    val cityMunicipality: String = "",
    val province: String = "",
    val zipCode: String = "",
    val contactNumber: String = "",
    val emailAddress: String = "",
    // Father
    val fatherFullName: String = "",
    val fatherOccupation: String = "",
    val fatherContact: String = "",
    // Mother
    val motherMaidenName: String = "",
    val motherOccupation: String = "",
    val motherContact: String = "",
    // Guardian (optional)
    val guardianFullName: String = "",
    val guardianRelationship: String = "",
    val guardianContact: String = "",
    val guardianAddress: String = "",
    // School Information
    val gradeLevelToEnroll: String = "",
    val schoolYear: String = "",
    val section: String = "",
    val previousSchool: String = "",
    val previousSchoolAddress: String = "",
    val schoolId: String = "",
    val schoolType: String = "" // Public/Private
)

data class TeacherInfo(
    val employeeId: String = "",
    val fullName: String = "",
    val suffix: String = "",
    val gender: String = "",
    val dateOfBirth: String = "",
    val age: String = "",
    val nationality: String = "",
    val civilStatus: String = "",
    val religion: String = "",
    val addressHouseStreet: String = "",
    val addressBarangay: String = "",
    val addressCityMunicipality: String = "",
    val addressProvince: String = "",
    val addressZipCode: String = "",
    val mobileNumber: String = "",
    val landlineNumber: String = "",
    val contactEmail: String = "",
    val position: String = "",
    val subjectsHandled: String = "",
    val gradeLevelAssigned: String = "",
    val department: String = "",
    val schoolAssigned: String = "",
    val prcLicenseNumber: String = "",
    val civilServiceEligibility: String = "",
    val employmentStatus: String = "",
    val highestEducationalAttainment: String = "",
    val degreeMajor: String = "",
    val educationSchoolName: String = "",
    val educationYearGraduated: String = "",
    val trainings: String = "",
    val bloodType: String = "",
    val allergies: String = "",
    val emergencyContactName: String = "",
    val emergencyContactRelationship: String = "",
    val emergencyContactNumber: String = "",
    val vaccinationStatus: String = "",
    val prcLicenseFile: String = "",
    val diplomaFile: String = "",
    val idPhotoFile: String = ""
)
