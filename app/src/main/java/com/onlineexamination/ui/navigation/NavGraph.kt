package com.onlineexamination.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.onlineexamination.data.model.UserRole
import com.onlineexamination.ui.screens.admin.AdminManageExamsScreen
import com.onlineexamination.ui.screens.admin.AdminSettingsScreen
import com.onlineexamination.ui.screens.admin.AnalyticsScreen
import com.onlineexamination.ui.screens.admin.ManageUsersScreen
import com.onlineexamination.ui.screens.auth.ForgotPasswordScreen
import com.onlineexamination.ui.screens.auth.LoginScreen
import com.onlineexamination.ui.screens.auth.PendingVerificationScreen
import com.onlineexamination.ui.screens.auth.SignUpScreen
import com.onlineexamination.ui.screens.dashboard.AdminDashboard
import com.onlineexamination.ui.screens.dashboard.StudentDashboard
import com.onlineexamination.ui.screens.dashboard.TeacherAnalyticsScreen
import com.onlineexamination.ui.screens.dashboard.TeacherDashboard
import com.onlineexamination.ui.screens.exam.*
import com.onlineexamination.ui.screens.profile.EditProfileScreen
import com.onlineexamination.ui.screens.profile.ProfileScreen
import com.onlineexamination.ui.screens.student.LeaderboardScreen
import com.onlineexamination.ui.screens.student.StudyMaterialsScreen
import com.onlineexamination.ui.screens.teacher.StudentLogsScreen
import com.onlineexamination.ui.viewmodel.AuthViewModel
import com.onlineexamination.ui.viewmodel.ExamViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object PendingVerification : Screen("pending_verification")
    object StudentDashboard : Screen("student_dashboard")
    object TeacherDashboard : Screen("teacher_dashboard")
    object AdminDashboard : Screen("admin_dashboard")
    object StudyMaterials : Screen("study_materials")
    object Leaderboards : Screen("leaderboards")

    // Student screens
    object AvailableExams : Screen("available_exams")
    object TakeExam : Screen("take_exam/{examId}") {
        fun createRoute(examId: String) = "take_exam/$examId"
    }

    object StudentResults : Screen("student_results")
    object ResultDetail : Screen("result_detail/{attemptId}/{examId}") {
        fun createRoute(attemptId: String, examId: String) = "result_detail/$attemptId/$examId"
    }

    object RequestSpecialExam : Screen("request_special_exam/{examId}/{examTitle}") {
        fun createRoute(examId: String, examTitle: String) = "request_special_exam/$examId/$examTitle"
    }

    // Teacher screens
    object CreateExam : Screen("create_exam")
    object ViewExams : Screen("view_exams")
    object EditExam : Screen("edit_exam/{examId}") {
        fun createRoute(examId: String) = "edit_exam/$examId"
    }

    object ExamResults : Screen("exam_results/{examId}") {
        fun createRoute(examId: String) = "exam_results/$examId"
    }

    object ItemAnalysis : Screen("item_analysis/{examId}") {
        fun createRoute(examId: String) = "item_analysis/$examId"
    }

    object StudentLogs : Screen("student_logs/{studentId}/{studentName}") {
        fun createRoute(studentId: String, studentName: String) = "student_logs/$studentId/$studentName"
    }

    // Admin screens
    object ManageUsers : Screen("manage_users")
    object Analytics : Screen("analytics")
    object AdminManageExams : Screen("admin_manage_exams")
    object AdminSettings : Screen("admin_settings")

    // Profile screen
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object TeacherAnalytics : Screen("teacher_analytics")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    startDestination: String = Screen.Login.route
) {
    val uiState by authViewModel.uiState.collectAsState()
    val examViewModel: ExamViewModel = viewModel()

    // Check authentication state and navigate accordingly
    LaunchedEffect(uiState.currentUser, uiState.userData) {
        val user = uiState.currentUser
        val userData = uiState.userData

        if (user != null && user.isEmailVerified && userData != null) {
            when (userData.role) {
                UserRole.STUDENT -> {
                    if (navController.currentDestination?.route != Screen.StudentDashboard.route) {
                        navController.navigate(Screen.StudentDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
                UserRole.TEACHER -> {
                    if (navController.currentDestination?.route != Screen.TeacherDashboard.route) {
                        navController.navigate(Screen.TeacherDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
                UserRole.ADMIN -> {
                    if (navController.currentDestination?.route != Screen.AdminDashboard.route) {
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            }
        } else if (user == null) {
            if (navController.currentDestination?.route != Screen.Login.route &&
                navController.currentDestination?.route != Screen.SignUp.route &&
                navController.currentDestination?.route != Screen.ForgotPassword.route &&
                navController.currentDestination?.route != Screen.PendingVerification.route
            ) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Navigation handled by LaunchedEffect
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToPendingVerification = {
                    navController.navigate(Screen.PendingVerification.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.PendingVerification.route) {
            PendingVerificationScreen(onNavigateToLogin = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.PendingVerification.route) { inclusive = true }
                }
            })
        }

        composable(Screen.StudentDashboard.route) {
            uiState.userData?.let { user ->
                StudentDashboard(
                    user = user,
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onTakeExam = {
                        navController.navigate(Screen.AvailableExams.route)
                    },
                    onViewResults = {
                        navController.navigate(Screen.StudentResults.route)
                    },
                    onViewProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onStudyMaterials = {
                        navController.navigate(Screen.StudyMaterials.route)
                    },
                    onLeaderboards = {
                        navController.navigate(Screen.Leaderboards.route)
                    }
                )
            }
        }

        composable(Screen.AvailableExams.route) {
            uiState.userData?.let { user ->
                AvailableExamsScreen(
                    studentId = user.uid,
                    viewModel = examViewModel,
                    onBack = { navController.popBackStack() },
                    onTakeExam = { examId ->
                        navController.navigate(Screen.TakeExam.createRoute(examId))
                    },
                    onRequestSpecialExam = { examId, examTitle ->
                        navController.navigate(Screen.RequestSpecialExam.createRoute(examId, examTitle))
                    }
                )
            }
        }

        composable(
            route = Screen.RequestSpecialExam.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType },
                navArgument("examTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            uiState.userData?.let { user ->
                val examId = backStackEntry.arguments?.getString("examId") ?: return@let
                val examTitle = backStackEntry.arguments?.getString("examTitle") ?: return@let
                RequestSpecialExamScreen(
                    studentId = user.uid,
                    studentName = user.username,
                    examId = examId,
                    examTitle = examTitle,
                    onBack = { navController.popBackStack() },
                    onRequestSubmitted = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.TakeExam.route,
            arguments = listOf(navArgument("examId") { type = NavType.StringType })
        ) { backStackEntry ->
            uiState.userData?.let { user ->
                val examId = backStackEntry.arguments?.getString("examId") ?: return@let
                TakeExamScreen(
                    examId = examId,
                    studentId = user.uid,
                    studentName = user.username,
                    studentEmail = user.email,
                    viewModel = examViewModel,
                    onBack = { navController.popBackStack() },
                    onExamSubmitted = { attemptId ->
                        navController.navigate(Screen.ResultDetail.createRoute(attemptId, examId)) {
                            popUpTo(Screen.StudentDashboard.route)
                        }
                    }
                )
            }
        }

        composable(Screen.StudentResults.route) {
            uiState.userData?.let { user ->
                StudentResultsScreen(
                    studentId = user.uid,
                    viewModel = examViewModel,
                    onBack = { navController.popBackStack() },
                    onResultClick = { attemptId, examId ->
                        navController.navigate(Screen.ResultDetail.createRoute(attemptId, examId))
                    }
                )
            }
        }

        composable(Screen.StudyMaterials.route) {
            uiState.userData?.let { user ->
                StudyMaterialsScreen(
                    studentId = user.uid,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Leaderboards.route) {
            LeaderboardScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.ResultDetail.route,
            arguments = listOf(
                navArgument("attemptId") { type = NavType.StringType },
                navArgument("examId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val attemptId = backStackEntry.arguments?.getString("attemptId") ?: return@composable
            val examId = backStackEntry.arguments?.getString("examId") ?: return@composable
            ResultDetailScreen(
                attemptId = attemptId,
                examId = examId,
                viewModel = examViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TeacherDashboard.route) {
            uiState.userData?.let { user ->
                TeacherDashboard(
                    user = user,
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onCreateExam = {
                        navController.navigate(Screen.CreateExam.route)
                    },
                    onViewExams = {
                        navController.navigate(Screen.ViewExams.route)
                    },
                    onViewResults = {
                        // Navigate to view exams first, then user can select exam to view results
                        navController.navigate(Screen.ViewExams.route)
                    },
                    onViewProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onAnalytics = {
                        navController.navigate(Screen.TeacherAnalytics.route)
                    }
                )
            }
        }

        composable(Screen.CreateExam.route) {
            uiState.userData?.let { user ->
                CreateExamScreen(
                    teacherId = user.uid,
                    teacherName = user.username,
                    viewModel = examViewModel,
                    onBack = { navController.popBackStack() },
                    onExamCreated = {
                        navController.navigate(Screen.ViewExams.route) {
                            popUpTo(Screen.CreateExam.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Screen.TeacherAnalytics.route) {
            uiState.userData?.let { user ->
                TeacherAnalyticsScreen(
                    teacherId = user.uid,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.ViewExams.route) {
            uiState.userData?.let { user ->
                ViewExamsScreen(
                    teacherId = user.uid,
                    viewModel = examViewModel,
                    onBack = { navController.popBackStack() },
                    onViewResults = { examId ->
                        navController.navigate(Screen.ExamResults.createRoute(examId))
                    },
                    onEditExam = { examId ->
                        navController.navigate(Screen.EditExam.createRoute(examId))
                    }
                )
            }
        }

        composable(
            route = Screen.EditExam.route,
            arguments = listOf(navArgument("examId") { type = NavType.StringType })
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: return@composable
            EditExamScreen(
                examId = examId,
                viewModel = examViewModel,
                onBack = { navController.popBackStack() },
                onExamUpdated = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ExamResults.route,
            arguments = listOf(navArgument("examId") { type = NavType.StringType })
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: return@composable
            var examTitle by remember { mutableStateOf("Exam Results") }
            LaunchedEffect(examId) {
                examViewModel.loadExamById(examId)
            }
            val examUiState by examViewModel.uiState.collectAsState()
            examTitle = examUiState.currentExam?.title ?: "Exam Results"

            ExamResultsScreen(
                examId = examId,
                examTitle = examTitle,
                viewModel = examViewModel,
                onBack = { navController.popBackStack() },
                onResultClick = { attemptId ->
                    navController.navigate(Screen.ResultDetail.createRoute(attemptId, examId))
                },
                onAnalyticsClick = {
                    navController.navigate(Screen.ItemAnalysis.createRoute(examId))
                },
                onLogsClick = { studentId, studentName ->
                    navController.navigate(Screen.StudentLogs.createRoute(studentId, studentName))
                }
            )
        }

        composable(
            route = Screen.ItemAnalysis.route,
            arguments = listOf(navArgument("examId") { type = NavType.StringType })
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: return@composable
            ItemAnalysisScreen(
                examId = examId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.StudentLogs.route,
            arguments = listOf(
                navArgument("studentId") { type = NavType.StringType },
                navArgument("studentName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: return@composable
            val studentName = backStackEntry.arguments?.getString("studentName") ?: return@composable
            StudentLogsScreen(
                studentId = studentId,
                studentName = studentName,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminDashboard.route) {
            uiState.userData?.let { user ->
                AdminDashboard(
                    user = user,
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onManageUsers = {
                        navController.navigate(Screen.ManageUsers.route)
                    },
                    onAnalytics = {
                        navController.navigate(Screen.Analytics.route)
                    },
                    onManageExams = {
                        navController.navigate(Screen.AdminManageExams.route)
                    },
                    onSettings = {
                        navController.navigate(Screen.AdminSettings.route)
                    },
                    onViewProfile = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }
        }

        composable(Screen.ManageUsers.route) {
            ManageUsersScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminManageExams.route) {
            AdminManageExamsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminSettings.route) {
            AdminSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            uiState.userData?.let { user ->
                ProfileScreen(
                    user = user,
                    onBack = { navController.popBackStack() },
                    onEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    }
                )
            }
        }

        composable(Screen.EditProfile.route) {
            uiState.userData?.let { user ->
                EditProfileScreen(
                    user = user,
                    onBack = { navController.popBackStack() },
                    onProfileUpdated = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
