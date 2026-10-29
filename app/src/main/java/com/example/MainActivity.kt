package com.example

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import com.example.ui.components.ShareAppDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ManageAccounts
import com.example.data.model.SchoolStats
import com.example.data.model.Staff
import com.example.data.model.Student
import com.example.data.model.StudentGrade
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherWorkload
import com.example.data.model.UserAccount
import com.example.ui.SchoolNavTab
import com.example.ui.SchoolViewModel
import com.example.ui.components.EthiopianFlagAccent
import com.example.ui.screens.AdminPasswordManagerDialog
import com.example.ui.screens.AssessmentSchemeDialog
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GradeFormDialog
import com.example.ui.screens.GradesScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.StaffDetailDialog
import com.example.ui.screens.StaffFormDialog
import com.example.ui.screens.StaffScreen
import com.example.ui.screens.StudentFormDialog
import com.example.ui.screens.StudentsScreen
import com.example.ui.screens.SubjectFormDialog
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.TeacherDetailDialog
import com.example.ui.screens.TeacherFormDialog
import com.example.ui.screens.ImportExportCenterDialog
import com.example.ui.screens.TeachersScreen
import com.example.ui.screens.WorkloadFormDialog
import com.example.ui.screens.WorkloadScreen
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavyLight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Settings
import coil.compose.AsyncImage
import com.example.ui.screens.AdminSchoolSettingsDialog

class MainActivity : ComponentActivity() {

    private val viewModel: SchoolViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Support display cutout / camera notch area on modern mobile phones without letterbox borders
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // Apply sticky immersive system bar behavior
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MyApplicationTheme {
                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val isAmharic by viewModel.isAmharic.collectAsStateWithLifecycle()
                val isFullScreen by viewModel.isFullScreen.collectAsStateWithLifecycle()
                var showShareDialog by remember { mutableStateOf(false) }

                val stats by viewModel.schoolStats.collectAsStateWithLifecycle()
                val students by viewModel.filteredStudents.collectAsStateWithLifecycle()
                val teachers by viewModel.filteredTeachers.collectAsStateWithLifecycle()
                val subjects by viewModel.filteredSubjects.collectAsStateWithLifecycle()
                val workloads by viewModel.allWorkloads.collectAsStateWithLifecycle()

                // Search & Filter queries
                val studentQuery by viewModel.studentSearchQuery.collectAsStateWithLifecycle()
                val studentGradeFilter by viewModel.selectedGradeFilter.collectAsStateWithLifecycle()
                val studentSectionFilter by viewModel.selectedSectionFilter.collectAsStateWithLifecycle()

                val teacherQuery by viewModel.teacherSearchQuery.collectAsStateWithLifecycle()
                val teacherDeptFilter by viewModel.teacherDepartmentFilter.collectAsStateWithLifecycle()
                val subjectGradeFilter by viewModel.subjectGradeFilter.collectAsStateWithLifecycle()

                // Dialog and detail state
                val showAddStudent by viewModel.showAddStudentDialog.collectAsStateWithLifecycle()
                val studentToEdit by viewModel.studentToEdit.collectAsStateWithLifecycle()

                val showAddTeacher by viewModel.showAddTeacherDialog.collectAsStateWithLifecycle()
                val teacherToEdit by viewModel.teacherToEdit.collectAsStateWithLifecycle()
                val selectedTeacherForDetails by viewModel.selectedTeacherForDetails.collectAsStateWithLifecycle()

                val showAddSubject by viewModel.showAddSubjectDialog.collectAsStateWithLifecycle()
                val subjectToEdit by viewModel.subjectToEdit.collectAsStateWithLifecycle()

                val showAddWorkload by viewModel.showAddWorkloadDialog.collectAsStateWithLifecycle()
                val workloadToEdit by viewModel.workloadToEdit.collectAsStateWithLifecycle()

                val showAddGrade by viewModel.showAddGradeDialog.collectAsStateWithLifecycle()
                val showAssessmentSchemeDialog by viewModel.showAssessmentSchemeDialog.collectAsStateWithLifecycle()
                val assessmentScheme by viewModel.assessmentScheme.collectAsStateWithLifecycle()
                val showImportExport by viewModel.showImportExportDialog.collectAsStateWithLifecycle()
                val importExportTargetTab by viewModel.importExportTargetTab.collectAsStateWithLifecycle()
                val selectedStudentForGrades by viewModel.selectedStudentForGrades.collectAsStateWithLifecycle()
                val gradesList by viewModel.studentGrades.collectAsStateWithLifecycle()
                val allGradesList by viewModel.allGrades.collectAsStateWithLifecycle()

                // Staff state
                val staffList by viewModel.filteredStaff.collectAsStateWithLifecycle()
                val staffQuery by viewModel.staffSearchQuery.collectAsStateWithLifecycle()
                val staffRoleFilter by viewModel.staffRoleFilter.collectAsStateWithLifecycle()
                val staffDeptFilter by viewModel.staffDepartmentFilter.collectAsStateWithLifecycle()
                val showAddStaff by viewModel.showAddStaffDialog.collectAsStateWithLifecycle()
                val staffToEdit by viewModel.staffToEdit.collectAsStateWithLifecycle()
                val selectedStaffForDetails by viewModel.selectedStaffForDetails.collectAsStateWithLifecycle()

                // Authentication state
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                val visibleTabs by viewModel.visibleTabs.collectAsStateWithLifecycle()
                val currentTeacher by viewModel.currentTeacher.collectAsStateWithLifecycle()
                val currentStaff by viewModel.currentStaff.collectAsStateWithLifecycle()
                val currentStudent by viewModel.currentStudent.collectAsStateWithLifecycle()
                val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
                val isVicePrincipal by viewModel.isVicePrincipal.collectAsStateWithLifecycle()
                val canAccessStaffAttendance by viewModel.canAccessStaffAttendance.collectAsStateWithLifecycle()
                val staffAttendanceList by viewModel.staffAttendanceList.collectAsStateWithLifecycle()
                val currentStaffSession by viewModel.currentStaffAttendanceSession.collectAsStateWithLifecycle()
                val staffRoleSchedules by viewModel.staffRoleSchedules.collectAsStateWithLifecycle()
                val isSuperAdmin by viewModel.isSuperAdmin.collectAsStateWithLifecycle()
                val isTeacher by viewModel.isTeacher.collectAsStateWithLifecycle()
                val isStudent by viewModel.isStudent.collectAsStateWithLifecycle()
                val teacherWorkloads by viewModel.teacherWorkloads.collectAsStateWithLifecycle()
                val isAdminLoginMode by viewModel.isAdminLoginMode.collectAsStateWithLifecycle()
                val loginEmail by viewModel.loginEmail.collectAsStateWithLifecycle()
                val loginPhone by viewModel.loginPhone.collectAsStateWithLifecycle()
                val loginPassword by viewModel.loginPassword.collectAsStateWithLifecycle()
                val loginError by viewModel.loginErrorMessage.collectAsStateWithLifecycle()
                val isAuthenticating by viewModel.isAuthenticating.collectAsStateWithLifecycle()
                val showAdminPasswordManager by viewModel.showAdminPasswordManagerDialog.collectAsStateWithLifecycle()
                val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

                // School Settings & Profile
                val availableGrades by viewModel.availableGrades.collectAsStateWithLifecycle()
                val formGradeOptions by viewModel.formGradeOptions.collectAsStateWithLifecycle()
                val schoolLogoUri by viewModel.schoolLogoUri.collectAsStateWithLifecycle()
                val schoolGradeLevel by viewModel.schoolGradeLevel.collectAsStateWithLifecycle()
                val schoolName by viewModel.schoolName.collectAsStateWithLifecycle()
                val schoolNameEn by viewModel.schoolNameEn.collectAsStateWithLifecycle()
                val showSchoolSettingsDialog by viewModel.showSchoolSettingsDialog.collectAsStateWithLifecycle()

                // Dynamically sync fullscreen state with system bars
                LaunchedEffect(isFullScreen) {
                    val controller = WindowCompat.getInsetsController(window, window.decorView)
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    if (isFullScreen) {
                        controller.hide(WindowInsetsCompat.Type.systemBars())
                    } else {
                        controller.show(WindowInsetsCompat.Type.systemBars())
                    }
                }

                if (!isLoggedIn) {
                    LoginScreen(
                        isAdminMode = isAdminLoginMode,
                        onToggleAdminMode = { viewModel.isAdminLoginMode.value = it },
                        email = loginEmail,
                        onEmailChange = { viewModel.loginEmail.value = it },
                        phone = loginPhone,
                        onPhoneChange = { viewModel.loginPhone.value = it },
                        password = loginPassword,
                        onPasswordChange = { viewModel.loginPassword.value = it },
                        errorMessage = loginError,
                        isLoading = isAuthenticating,
                        isAmharic = isAmharic,
                        isFullScreen = isFullScreen,
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onToggleFullScreen = { viewModel.toggleFullScreen() },
                        onLoginClick = {
                            if (isAdminLoginMode) {
                                viewModel.login(loginEmail, loginPassword, isEmailLogin = true)
                            } else {
                                viewModel.login(loginPhone, loginPassword, isEmailLogin = false)
                            }
                        },
                        onQuickDemoLogin = { viewModel.quickLoginAsAdmin() },
                        onQuickRoleLogin = { identifier, isEmail, pass ->
                            viewModel.isAdminLoginMode.value = isEmail
                            if (isEmail) {
                                viewModel.loginEmail.value = identifier
                            } else {
                                viewModel.loginPhone.value = identifier
                            }
                            viewModel.loginPassword.value = pass
                            viewModel.login(identifier, pass, isEmailLogin = isEmail)
                        },
                        onRegisterNewUser = { name, regPhone, regPass, role ->
                            viewModel.registerAccount(name, regPhone, regPass, role) {}
                        },
                        onGoogleSignInSuccess = { name, email, uid ->
                            viewModel.onGoogleSignInSuccess(name, email, uid)
                        },
                        onGoogleSignInError = { errorMsg ->
                            viewModel.onGoogleSignInError(errorMsg)
                        },
                        schoolLogoUri = schoolLogoUri,
                        schoolNameAm = schoolName,
                        schoolNameEn = schoolNameEn,
                        schoolGradeLevel = schoolGradeLevel
                    )
                } else {
                    // Proper Back navigation handler
                    BackHandler(enabled = currentTab != SchoolNavTab.DASHBOARD) {
                        viewModel.handleBack()
                    }

                    // Responsive Container: Adapts seamlessly to Phone (<700dp) and Computer / Tablet (>=700dp)
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isWideScreen = maxWidth >= 700.dp

                    @Composable
                    fun SchoolContentPane() {
                        if (!visibleTabs.contains(currentTab)) {
                            DashboardScreen(
                                currentUser = currentUser,
                                currentTeacher = currentTeacher,
                                currentStaff = currentStaff,
                                currentStudent = currentStudent,
                                stats = stats,
                                recentStudents = students,
                                recentTeachers = teachers,
                                recentStaff = staffList,
                                recentWorkloads = workloads,
                                isAmharic = isAmharic,
                                onNavigateTab = { viewModel.navigateTo(it) },
                                onRegisterStudentClick = { viewModel.showAddStudentDialog.value = true },
                                onRegisterTeacherClick = { viewModel.showAddTeacherDialog.value = true },
                                onAddSubjectClick = { viewModel.showAddSubjectDialog.value = true },
                                onAssignWorkloadClick = { viewModel.showAddWorkloadDialog.value = true },
                                onAddGradeClick = { viewModel.showAddGradeDialog.value = true },
                                onTakeAttendanceClick = { viewModel.navigateTo(SchoolNavTab.ATTENDANCE) },
                                schoolLogoUri = schoolLogoUri,
                                schoolNameAm = schoolName,
                                schoolNameEn = schoolNameEn,
                                schoolGradeLevel = schoolGradeLevel
                            )
                        } else {
                            when (currentTab) {
                                SchoolNavTab.DASHBOARD -> {
                                    DashboardScreen(
                                        currentUser = currentUser,
                                        currentTeacher = currentTeacher,
                                        currentStaff = currentStaff,
                                        currentStudent = currentStudent,
                                        stats = stats,
                                        recentStudents = students,
                                        recentTeachers = teachers,
                                        recentStaff = staffList,
                                        recentWorkloads = workloads,
                                        isAmharic = isAmharic,
                                        onNavigateTab = { viewModel.navigateTo(it) },
                                        onRegisterStudentClick = { viewModel.showAddStudentDialog.value = true },
                                        onRegisterTeacherClick = { viewModel.showAddTeacherDialog.value = true },
                                        onAddSubjectClick = { viewModel.showAddSubjectDialog.value = true },
                                        onAssignWorkloadClick = { viewModel.showAddWorkloadDialog.value = true },
                                        onAddGradeClick = { viewModel.showAddGradeDialog.value = true },
                                        onTakeAttendanceClick = { viewModel.navigateTo(SchoolNavTab.ATTENDANCE) },
                                        schoolLogoUri = schoolLogoUri,
                                        schoolNameAm = schoolName,
                                        schoolNameEn = schoolNameEn,
                                        schoolGradeLevel = schoolGradeLevel
                                    )
                                }

                            SchoolNavTab.STUDENTS -> {
                                StudentsScreen(
                                    students = students,
                                    searchQuery = studentQuery,
                                    onSearchChange = { viewModel.studentSearchQuery.value = it },
                                    gradeFilter = studentGradeFilter,
                                    onGradeFilterChange = { viewModel.selectedGradeFilter.value = it },
                                    sectionFilter = studentSectionFilter,
                                    onSectionFilterChange = { viewModel.selectedSectionFilter.value = it },
                                    availableGrades = availableGrades,
                                    formGradeOptions = formGradeOptions,
                                    sectionOptions = viewModel.sectionOptions,
                                    isAmharic = isAmharic,
                                    onAddStudentClick = { viewModel.showAddStudentDialog.value = true },
                                    onEditStudentClick = { student ->
                                        viewModel.studentToEdit.value = student
                                        viewModel.showAddStudentDialog.value = true
                                    },
                                    onDeleteStudentClick = { student ->
                                        viewModel.deleteStudent(student)
                                    },
                                    onViewGradesClick = { student ->
                                        viewModel.selectStudentForGrades(student)
                                        viewModel.navigateTo(SchoolNavTab.GRADES)
                                    },
                                    onOpenImportExportClick = {
                                        viewModel.openImportExportDialog("students")
                                    }
                                )
                            }

                            SchoolNavTab.TEACHERS -> {
                                TeachersScreen(
                                    teachers = teachers,
                                    workloads = workloads,
                                    searchQuery = teacherQuery,
                                    onSearchChange = { viewModel.teacherSearchQuery.value = it },
                                    departmentFilter = teacherDeptFilter,
                                    onDepartmentFilterChange = { viewModel.teacherDepartmentFilter.value = it },
                                    isAmharic = isAmharic,
                                    onTeacherClick = { teacher ->
                                        viewModel.selectedTeacherForDetails.value = teacher
                                    },
                                    onAddTeacherClick = { viewModel.showAddTeacherDialog.value = true },
                                    onEditTeacherClick = { teacher ->
                                        viewModel.teacherToEdit.value = teacher
                                        viewModel.showAddTeacherDialog.value = true
                                    },
                                    onDeleteTeacherClick = { teacher ->
                                        viewModel.deleteTeacher(teacher)
                                    },
                                    onAssignWorkloadClick = { teacher ->
                                        viewModel.workloadToEdit.value = TeacherWorkload(
                                            teacherId = teacher.id,
                                            teacherName = teacher.fullName,
                                            subjectId = subjects.firstOrNull()?.id ?: 0L,
                                            subjectName = subjects.firstOrNull()?.name ?: "",
                                            grade = "9ኛ ክፍል",
                                            section = "A"
                                        )
                                        viewModel.showAddWorkloadDialog.value = true
                                    },
                                    onOpenImportExportClick = {
                                        viewModel.openImportExportDialog("teachers")
                                    }
                                )
                            }

                            SchoolNavTab.STAFF -> {
                                StaffScreen(
                                    staffList = staffList,
                                    searchQuery = staffQuery,
                                    onSearchChange = { viewModel.staffSearchQuery.value = it },
                                    roleFilter = staffRoleFilter,
                                    onRoleFilterChange = { viewModel.staffRoleFilter.value = it },
                                    departmentFilter = staffDeptFilter,
                                    onDepartmentFilterChange = { viewModel.staffDepartmentFilter.value = it },
                                    isAmharic = isAmharic,
                                    onStaffClick = { staff ->
                                        viewModel.selectedStaffForDetails.value = staff
                                    },
                                    onAddStaffClick = { viewModel.showAddStaffDialog.value = true },
                                    onEditStaffClick = { staff ->
                                        viewModel.staffToEdit.value = staff
                                        viewModel.showAddStaffDialog.value = true
                                    },
                                    onDeleteStaffClick = { staff ->
                                        viewModel.deleteStaff(staff)
                                    }
                                )
                            }

                            SchoolNavTab.SUBJECTS -> {
                                SubjectsScreen(
                                    subjects = subjects,
                                    gradeFilter = subjectGradeFilter,
                                    onGradeFilterChange = { viewModel.subjectGradeFilter.value = it },
                                    availableGrades = availableGrades,
                                    isAmharic = isAmharic,
                                    onAddSubjectClick = { viewModel.showAddSubjectDialog.value = true },
                                    onEditSubjectClick = { subject ->
                                        viewModel.subjectToEdit.value = subject
                                        viewModel.showAddSubjectDialog.value = true
                                    },
                                    onDeleteSubjectClick = { subject ->
                                        viewModel.deleteSubject(subject)
                                    }
                                )
                            }

                            SchoolNavTab.WORKLOAD -> {
                                WorkloadScreen(
                                    workloads = workloads,
                                    teachers = teachers,
                                    subjects = subjects,
                                    isAmharic = isAmharic,
                                    onAddWorkloadClick = { viewModel.showAddWorkloadDialog.value = true },
                                    onEditWorkloadClick = { wl ->
                                        viewModel.workloadToEdit.value = wl
                                        viewModel.showAddWorkloadDialog.value = true
                                    },
                                    onDeleteWorkloadClick = { wl ->
                                        viewModel.deleteWorkload(wl)
                                    }
                                )
                            }

                            SchoolNavTab.GRADES -> {
                                GradesScreen(
                                    students = students,
                                    selectedStudent = selectedStudentForGrades ?: (if (isStudent) currentStudent else null) ?: students.firstOrNull(),
                                    onSelectStudent = { student ->
                                        viewModel.selectStudentForGrades(student)
                                    },
                                    grades = gradesList,
                                    allGrades = allGradesList,
                                    subjects = subjects,
                                    isAmharic = isAmharic,
                                    isAdmin = isAdmin,
                                    isStudent = isStudent,
                                    currentTeacher = currentTeacher,
                                    currentStudent = currentStudent,
                                    teacherWorkloads = teacherWorkloads,
                                    assessmentScheme = assessmentScheme,
                                    onConfigureSchemeClick = { viewModel.showAssessmentSchemeDialog.value = true },
                                    onAddGradeClick = { viewModel.showAddGradeDialog.value = true },
                                    onDeleteGradeClick = { grade ->
                                        viewModel.deleteGrade(grade)
                                    },
                                    schoolLogoUri = schoolLogoUri,
                                    schoolNameAm = schoolName,
                                    schoolNameEn = schoolNameEn
                                )
                            }

                            SchoolNavTab.ATTENDANCE -> {
                                AttendanceScreen(
                                    students = students,
                                    availableGrades = availableGrades,
                                    sectionOptions = viewModel.sectionOptions,
                                    isAmharic = isAmharic,
                                    isAdmin = isAdmin,
                                    isVicePrincipal = isVicePrincipal,
                                    canAccessStaffAttendance = canAccessStaffAttendance,
                                    isTeacher = isTeacher,
                                    isStudent = isStudent,
                                    currentTeacher = currentTeacher,
                                    currentStudent = currentStudent,
                                    teacherWorkloads = teacherWorkloads,
                                    staffAttendanceList = staffAttendanceList,
                                    staffRoleSchedules = staffRoleSchedules,
                                    activeStaffSession = currentStaffSession,
                                    onSelectStaffSession = { session -> viewModel.currentStaffAttendanceSession.value = session },
                                    onRecordAttendance = { record ->
                                        viewModel.recordAttendance(record)
                                    },
                                    onRecordStaffAttendance = { id, status, remarks ->
                                        viewModel.updateStaffAttendance(id, status, remarks)
                                    },
                                    onAutoTickStaffAttendance = { id, session, status ->
                                        viewModel.autoTickStaffAttendance(id, session, status)
                                    },
                                    onSaveStaffAttendanceManual = { id, session, status, time, remarks ->
                                        viewModel.saveStaffAttendanceManual(id, session, status, time, remarks)
                                    },
                                    onUpdateRoleSchedule = { cat, eS, eE, xS, xE, desc ->
                                        viewModel.updateStaffRoleSchedule(cat, eS, eE, xS, xE, desc)
                                    },
                                    onResetRoleSchedules = {
                                        viewModel.resetStaffRoleSchedulesToDefault()
                                    }
                                )
                            }
                        }
                    }
                }

                    if (isWideScreen) {
                        // =================== COMPUTER / TABLET DESKTOP MODE ===================
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Left Navigation Rail (Enterprise Sidebar)
                            NavigationRail(
                                modifier = Modifier
                                    .width(115.dp)
                                    .fillMaxHeight(),
                                containerColor = NavyPrimary,
                                contentColor = Color.White,
                                header = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .padding(top = 16.dp, bottom = 10.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, GoldAccent, CircleShape)
                                                .background(GoldAccent)
                                        ) {
                                            if (schoolLogoUri.isNotEmpty()) {
                                                AsyncImage(
                                                    model = schoolLogoUri,
                                                    contentDescription = "School Logo",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.School,
                                                    contentDescription = null,
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (isAmharic) schoolName else schoolNameEn,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = when (schoolGradeLevel) {
                                                "1_8" -> "1-8"
                                                "9_12" -> "9-12"
                                                else -> "K-12"
                                            },
                                            fontSize = 10.sp,
                                            color = GoldAccent
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        EthiopianFlagAccent()
                                    }
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    visibleTabs.forEach { tab ->
                                        val isSelected = currentTab == tab
                                        val tabIcon = when (tab) {
                                            SchoolNavTab.DASHBOARD -> Icons.Default.Dashboard
                                            SchoolNavTab.STUDENTS -> Icons.Default.People
                                            SchoolNavTab.TEACHERS -> Icons.Default.School
                                            SchoolNavTab.STAFF -> Icons.Default.ManageAccounts
                                            SchoolNavTab.SUBJECTS -> Icons.Default.MenuBook
                                            SchoolNavTab.WORKLOAD -> Icons.Default.Assignment
                                            SchoolNavTab.GRADES -> Icons.Default.Assessment
                                            SchoolNavTab.ATTENDANCE -> Icons.Default.CheckCircle
                                        }
                                        NavigationRailItem(
                                            selected = isSelected,
                                            onClick = { viewModel.navigateTo(tab) },
                                            icon = {
                                                Icon(
                                                    imageVector = tabIcon,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = if (isAmharic) tab.titleAm else tab.titleEn,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    maxLines = 1
                                                )
                                            },
                                            colors = NavigationRailItemDefaults.colors(
                                                selectedIconColor = Color.Black,
                                                selectedTextColor = GoldAccent,
                                                indicatorColor = GoldAccent,
                                                unselectedIconColor = Color.White.copy(alpha = 0.8f),
                                                unselectedTextColor = Color.White.copy(alpha = 0.8f)
                                            ),
                                            modifier = Modifier.testTag("rail_${tab.name.lowercase()}")
                                        )
                                    }
                                }

                                // Bottom utility toggles in Navigation Rail
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(bottom = 14.dp)
                                        .fillMaxWidth()
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White.copy(alpha = 0.18f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.toggleLanguage() }
                                            .testTag("btn_rail_language_toggle")
                                    ) {
                                        Text(
                                            text = if (isAmharic) "አማ/EN" else "EN/አማ",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isFullScreen) GoldAccent else Color.White.copy(alpha = 0.18f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.toggleFullScreen() }
                                            .testTag("btn_rail_fullscreen_toggle")
                                    ) {
                                        Icon(
                                            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                            contentDescription = "Toggle Fullscreen",
                                            tint = if (isFullScreen) Color.Black else Color.White,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .padding(5.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    // Rail Admin Password Manager Button
                                    if (isAdmin || isSuperAdmin) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = GoldAccent.copy(alpha = 0.25f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { viewModel.showAdminPasswordManagerDialog.value = true }
                                                .testTag("btn_rail_password_manager")
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                                            ) {
                                                Text(text = "🔐", fontSize = 14.sp)
                                                Text(
                                                    text = if (isAmharic) "የይለፍ ቃል" else "Pass",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GoldAccent
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                    // Rail School Settings Button for Admin
                                    if (isAdmin || isSuperAdmin) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = NavyLight,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { viewModel.showSchoolSettingsDialog.value = true }
                                                .testTag("btn_rail_school_settings")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = "School Settings",
                                                tint = GoldAccent,
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .padding(5.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                    // Rail Share Web Link Button
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = GoldAccent,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { showShareDialog = true }
                                            .testTag("btn_rail_share_app")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share App Link",
                                            tint = Color.Black,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .padding(5.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.Red.copy(alpha = 0.25f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.logout() }
                                            .testTag("btn_rail_logout")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = "Logout",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .padding(5.dp)
                                        )
                                    }
                                }
                            }

                            VerticalDivider(color = Color.LightGray.copy(alpha = 0.2f))

                            // Right Wide-Screen Canvas
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Surface(
                                    color = NavyPrimary,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = if (isAmharic) "${schoolName} • ${currentTab.titleAm}" else "${schoolNameEn} • ${currentTab.titleEn}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = GoldAccent.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = if (isAmharic) "🖥️ የኮምፒውተር / ታብሌት እይታ" else "🖥️ Desktop / Tablet Mode",
                                                fontSize = 11.sp,
                                                color = GoldAccent,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                                EthiopianFlagAccent()

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f)
                                ) {
                                    SchoolContentPane()
                                }
                            }
                        }
                    } else {
                        // =================== PHONE MOBILE MODE ===================
                        Scaffold(
                            topBar = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(NavyPrimary)
                                        .statusBarsPadding()
                                ) {
                                    TopAppBar(
                                        title = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .padding(vertical = 2.dp)
                                                    .clickable { viewModel.navigateTo(SchoolNavTab.DASHBOARD) }
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .border(1.5.dp, GoldAccent, CircleShape)
                                                        .background(GoldAccent)
                                                ) {
                                                    if (schoolLogoUri.isNotEmpty()) {
                                                        AsyncImage(
                                                            model = schoolLogoUri,
                                                            contentDescription = "School Logo",
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    } else {
                                                        Icon(
                                                            imageVector = Icons.Default.School,
                                                            contentDescription = null,
                                                            tint = Color.Black,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = if (isAmharic) schoolName else schoolNameEn,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = when (schoolGradeLevel) {
                                                            "1_8" -> if (isAmharic) "1ኛ - 8ኛ ክፍል • 2017 ዓ.ም" else "Grade 1-8 • 2017 E.C."
                                                            "9_12" -> if (isAmharic) "9ኛ - 12ኛ ክፍል • 2017 ዓ.ም" else "Grade 9-12 • 2017 E.C."
                                                            else -> if (isAmharic) "KG - 12ኛ ክፍል • 2017 ዓ.ም" else "K-12 • 2017 E.C."
                                                        },
                                                        fontSize = 10.sp,
                                                        color = Color.White.copy(alpha = 0.85f)
                                                    )
                                                }
                                            }
                                        },
                                        actions = {
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = if (isFullScreen) GoldAccent else MaterialTheme.colorScheme.surface.copy(alpha = 0.25f),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .clickable { viewModel.toggleFullScreen() }
                                                    .testTag("btn_fullscreen_toggle")
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                                        contentDescription = "Full Screen",
                                                        tint = if (isFullScreen) Color.Black else Color.White,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (isFullScreen) (if (isAmharic) "ሙሉ እስክሪን" else "Fullscreen")
                                                        else (if (isAmharic) "መደበኛ" else "Normal"),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isFullScreen) Color.Black else Color.White
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.25f),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .clickable { viewModel.toggleLanguage() }
                                                    .testTag("btn_language_toggle")
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Language,
                                                        contentDescription = "Language",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (isAmharic) "አማ/EN" else "EN/አማ",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            // Mobile Admin School Profile & Settings Button
                                            if (isAdmin || isSuperAdmin) {
                                                Surface(
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = NavyLight,
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .clickable { viewModel.showSchoolSettingsDialog.value = true }
                                                        .testTag("btn_topbar_school_settings_mobile")
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Settings,
                                                            contentDescription = "Settings",
                                                            tint = GoldAccent,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = if (isAmharic) "ቅንብር" else "Settings",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = GoldAccent
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }

                                            // Mobile Admin Password Manager Button
                                            if (isAdmin || isSuperAdmin) {
                                                Surface(
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = GoldAccent.copy(alpha = 0.25f),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .clickable { viewModel.showAdminPasswordManagerDialog.value = true }
                                                        .testTag("btn_topbar_passwords_mobile")
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                                    ) {
                                                        Text(text = "🔐", fontSize = 11.sp)
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = if (isAmharic) "የይለፍ ቃል" else "Pass",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = GoldAccent
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }

                                            // Mobile Share Web Link Button
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = GoldAccent,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .clickable { showShareDialog = true }
                                                    .testTag("btn_topbar_share_mobile")
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Share,
                                                        contentDescription = "Share App Link",
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (isAmharic) "ሊንክ" else "Link",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.Black
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            // Mobile Logout Button
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.35f),
                                                modifier = Modifier
                                                    .padding(end = 8.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .clickable { viewModel.logout() }
                                                    .testTag("btn_logout")
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ExitToApp,
                                                        contentDescription = "Logout",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (isAmharic) "ውጣ" else "Exit",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = NavyPrimary,
                                            titleContentColor = Color.White
                                        )
                                    )
                                    EthiopianFlagAccent()

                                    val currentTabIndex = visibleTabs.indexOf(currentTab).coerceAtLeast(0)
                                    ScrollableTabRow(
                                        selectedTabIndex = currentTabIndex,
                                        edgePadding = 4.dp,
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        visibleTabs.forEach { tab ->
                                            val isSelected = currentTab == tab
                                            val tabIcon = when (tab) {
                                                SchoolNavTab.DASHBOARD -> Icons.Default.Dashboard
                                                SchoolNavTab.STUDENTS -> Icons.Default.People
                                                SchoolNavTab.TEACHERS -> Icons.Default.School
                                                SchoolNavTab.STAFF -> Icons.Default.ManageAccounts
                                                SchoolNavTab.SUBJECTS -> Icons.Default.MenuBook
                                                SchoolNavTab.WORKLOAD -> Icons.Default.Assignment
                                                SchoolNavTab.GRADES -> Icons.Default.Assessment
                                                SchoolNavTab.ATTENDANCE -> Icons.Default.CheckCircle
                                            }
                                            Tab(
                                                selected = isSelected,
                                                onClick = { viewModel.navigateTo(tab) },
                                                icon = {
                                                    Icon(
                                                        imageVector = tabIcon,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                },
                                                text = {
                                                    Text(
                                                        text = if (isAmharic) tab.titleAm else tab.titleEn,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                },
                                                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                                            )
                                        }
                                    }
                                }
                            },
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            modifier = Modifier.fillMaxSize()
                        ) { innerPadding ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = innerPadding.calculateTopPadding())
                                    .then(if (!isFullScreen) Modifier.navigationBarsPadding() else Modifier)
                            ) {
                                // Prominent Active User & Logout Banner (Always visible on all screens)
                                val activeUser = currentUser
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (isAdmin || isSuperAdmin) "👑" else if (isTeacher) "👨‍🏫" else "🎓",
                                                fontSize = 15.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = activeUser?.fullName ?: (if (isAmharic) "ተጠቃሚ" else "User"),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = NavyPrimary,
                                                    maxLines = 1
                                                )
                                                val contactInfo = if (activeUser != null && activeUser.email.isNotBlank()) activeUser.email else (activeUser?.phone ?: "")
                                                Text(
                                                    text = "${activeUser?.role ?: ""} • $contactInfo",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // RED LOGOUT BUTTON (ውጣ)
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = Color(0xFFDC2626),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable { viewModel.logout() }
                                                .testTag("btn_strip_logout")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ExitToApp,
                                                    contentDescription = "Logout",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isAmharic) "ውጣ" else "Logout",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    SchoolContentPane()
                                }
                            }
                        }
                    }
                }

                // Modal Dialogs
                if (showAddStudent) {
                    StudentFormDialog(
                        initialStudent = studentToEdit,
                        availableGrades = formGradeOptions,
                        availableSections = viewModel.sectionOptions,
                        isAmharic = isAmharic,
                        onDismiss = {
                            viewModel.showAddStudentDialog.value = false
                            viewModel.studentToEdit.value = null
                        },
                        onSave = { student ->
                            viewModel.saveStudent(student)
                        }
                    )
                }

                if (showAddTeacher) {
                    TeacherFormDialog(
                        initialTeacher = teacherToEdit,
                        isAmharic = isAmharic,
                        onDismiss = {
                            viewModel.showAddTeacherDialog.value = false
                            viewModel.teacherToEdit.value = null
                        },
                        onSave = { teacher ->
                            viewModel.saveTeacher(teacher)
                        }
                    )
                }

                selectedTeacherForDetails?.let { teacher ->
                    val currentTeacher = teachers.find { it.id == teacher.id } ?: teacher
                    TeacherDetailDialog(
                        teacher = currentTeacher,
                        workloads = workloads.filter { it.teacherId == currentTeacher.id },
                        isAmharic = isAmharic,
                        onDismiss = { viewModel.selectedTeacherForDetails.value = null },
                        onEditTeacher = { t ->
                            viewModel.teacherToEdit.value = t
                            viewModel.showAddTeacherDialog.value = true
                        },
                        onDeleteTeacher = { t ->
                            viewModel.deleteTeacher(t)
                            viewModel.selectedTeacherForDetails.value = null
                        },
                        onAddAssignment = { t ->
                            viewModel.workloadToEdit.value = TeacherWorkload(
                                teacherId = t.id,
                                teacherName = t.fullName,
                                subjectId = subjects.firstOrNull()?.id ?: 0L,
                                subjectName = subjects.firstOrNull()?.name ?: "",
                                grade = "9ኛ ክፍል",
                                section = "A"
                            )
                            viewModel.showAddWorkloadDialog.value = true
                        },
                        onEditAssignment = { wl ->
                            viewModel.workloadToEdit.value = wl
                            viewModel.showAddWorkloadDialog.value = true
                        },
                        onDeleteAssignment = { wl ->
                            viewModel.deleteWorkload(wl)
                        }
                    )
                }

                if (showAddSubject) {
                    SubjectFormDialog(
                        initialSubject = subjectToEdit,
                        availableGrades = availableGrades,
                        isAmharic = isAmharic,
                        onDismiss = {
                            viewModel.showAddSubjectDialog.value = false
                            viewModel.subjectToEdit.value = null
                        },
                        onSave = { subject ->
                            viewModel.saveSubject(subject)
                        }
                    )
                }

                if (showAddWorkload) {
                    WorkloadFormDialog(
                        initialWorkload = workloadToEdit,
                        teachers = teachers,
                        subjects = subjects,
                        availableGrades = availableGrades,
                        availableSections = viewModel.sectionOptions,
                        isAmharic = isAmharic,
                        onDismiss = {
                            viewModel.showAddWorkloadDialog.value = false
                            viewModel.workloadToEdit.value = null
                        },
                        onSave = { wl ->
                            viewModel.saveWorkload(wl)
                        }
                    )
                }

                if (showAddGrade && (selectedStudentForGrades ?: students.firstOrNull()) != null) {
                    GradeFormDialog(
                        student = (selectedStudentForGrades ?: (if (isStudent) currentStudent else null) ?: students.first()),
                        subjects = subjects,
                        isAmharic = isAmharic,
                        assignedSubjectNames = if (isAdmin) emptyList() else teacherWorkloads.map { it.subjectName },
                        assessmentScheme = assessmentScheme,
                        onDismiss = { viewModel.showAddGradeDialog.value = false },
                        onSave = { grade ->
                            viewModel.saveGrade(grade)
                        }
                    )
                }

                if (showAssessmentSchemeDialog) {
                    AssessmentSchemeDialog(
                        currentScheme = assessmentScheme,
                        isAmharic = isAmharic,
                        onDismiss = { viewModel.showAssessmentSchemeDialog.value = false },
                        onSave = { newScheme ->
                            viewModel.updateAssessmentScheme(newScheme)
                        }
                    )
                }

                if (showAddStaff) {
                    StaffFormDialog(
                        initialStaff = staffToEdit,
                        isAmharic = isAmharic,
                        onDismiss = {
                            viewModel.showAddStaffDialog.value = false
                            viewModel.staffToEdit.value = null
                        },
                        onSave = { staff ->
                            viewModel.saveStaff(staff)
                        }
                    )
                }

                selectedStaffForDetails?.let { staff ->
                    val currentStaff = staffList.find { it.id == staff.id } ?: staff
                    StaffDetailDialog(
                        staff = currentStaff,
                        isAmharic = isAmharic,
                        onDismiss = { viewModel.selectedStaffForDetails.value = null },
                        onEditStaff = { s ->
                            viewModel.staffToEdit.value = s
                            viewModel.showAddStaffDialog.value = true
                        },
                        onDeleteStaff = { s ->
                            viewModel.deleteStaff(s)
                            viewModel.selectedStaffForDetails.value = null
                        }
                    )
                }

                if (showAdminPasswordManager) {
                    AdminPasswordManagerDialog(
                        users = allUsers,
                        isAmharic = isAmharic,
                        onDismiss = { viewModel.showAdminPasswordManagerDialog.value = false },
                        onUpdatePassword = { userId, newPass ->
                            viewModel.updateUserPassword(userId, newPass)
                        }
                    )
                }

                if (showImportExport) {
                    ImportExportCenterDialog(
                        initialTarget = importExportTargetTab,
                        isAmharic = isAmharic,
                        onImportStudents = { csv, onDone ->
                            viewModel.importStudentsFromCsv(csv, onDone)
                        },
                        onImportTeachers = { csv, onDone ->
                            viewModel.importTeachersFromCsv(csv, onDone)
                        },
                        onExportStudents = { viewModel.exportStudentsToCsv() },
                        onExportTeachers = { viewModel.exportTeachersToCsv() },
                        onDismiss = { viewModel.closeImportExportDialog() }
                    )
                }

                if (showSchoolSettingsDialog) {
                    AdminSchoolSettingsDialog(
                        viewModel = viewModel,
                        onDismiss = { viewModel.showSchoolSettingsDialog.value = false }
                    )
                }
                } // End of else (isLoggedIn)

                if (showShareDialog) {
                    ShareAppDialog(
                        isAmharic = isAmharic,
                        onDismiss = { showShareDialog = false }
                    )
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && viewModel.isFullScreen.value) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}

