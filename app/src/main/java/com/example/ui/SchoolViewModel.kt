package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.SchoolDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.SchoolStats
import com.example.data.model.Staff
import com.example.data.model.Student
import com.example.data.model.AssessmentScheme
import android.content.Context
import com.example.data.model.StudentGrade
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherWorkload
import com.example.data.model.UserAccount
import com.example.data.repository.SchoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.data.firestore.FirestoreSchoolRepository
import com.example.data.firestore.CloudStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

enum class SchoolNavTab(val titleAm: String, val titleEn: String) {
    DASHBOARD("ዳሽቦርድ", "Dashboard"),
    STUDENTS("ተማሪዎች", "Students"),
    TEACHERS("መምህራን", "Teachers"),
    STAFF("አስተዳደር", "Staff"),
    SUBJECTS("የት/ት ዓይነት", "Subjects"),
    WORKLOAD("የሥራ ክፍፍል", "Workload"),
    GRADES("ውጤት", "Grades"),
    ATTENDANCE("ክትትል", "Attendance")
}

class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SchoolRepository
    private val firestoreRepository: FirestoreSchoolRepository = FirestoreSchoolRepository(application)
    val cloudSyncStatus: StateFlow<CloudStatus> = firestoreRepository.syncStatus

    private val auth = Firebase.auth
    val authUid: String
        get() = auth.currentUser?.uid ?: "uid_${_currentUser.value?.phone ?: "admin"}"

    init {
        val db = SchoolDatabase.getDatabase(application, viewModelScope)
        repository = SchoolRepository(
            studentDao = db.studentDao(),
            teacherDao = db.teacherDao(),
            subjectDao = db.subjectDao(),
            workloadDao = db.workloadDao(),
            gradeDao = db.gradeDao(),
            attendanceDao = db.attendanceDao(),
            staffDao = db.staffDao(),
            userAccountDao = db.userAccountDao()
        )
    }

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(SchoolNavTab.DASHBOARD)
    val currentTab: StateFlow<SchoolNavTab> = _currentTab.asStateFlow()

    // Navigation backstack for back handling
    private val _navStack = MutableStateFlow<List<SchoolNavTab>>(listOf(SchoolNavTab.DASHBOARD))
    val navStack: StateFlow<List<SchoolNavTab>> = _navStack.asStateFlow()

    // Language Toggle: Amharic (true) / English (false)
    private val _isAmharic = MutableStateFlow(true)
    val isAmharic: StateFlow<Boolean> = _isAmharic.asStateFlow()

    fun toggleLanguage() {
        _isAmharic.value = !_isAmharic.value
    }

    // Full Screen Immersive Mode (Enabled by default for mobile phone immersion)
    private val _isFullScreen = MutableStateFlow(true)
    val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()

    fun toggleFullScreen() {
        _isFullScreen.value = !_isFullScreen.value
    }

    fun setFullScreen(enabled: Boolean) {
        _isFullScreen.value = enabled
    }

    fun navigateTo(tab: SchoolNavTab) {
        if (!visibleTabs.value.contains(tab)) return
        if (_currentTab.value != tab) {
            _currentTab.value = tab
            val currentList = _navStack.value.toMutableList()
            if (tab == SchoolNavTab.DASHBOARD) {
                _navStack.value = listOf(SchoolNavTab.DASHBOARD)
            } else {
                currentList.add(tab)
                _navStack.value = currentList
            }
        }
    }

    fun handleBack(): Boolean {
        val currentList = _navStack.value.toMutableList()
        if (currentList.size > 1) {
            currentList.removeAt(currentList.lastIndex)
            _navStack.value = currentList
            _currentTab.value = currentList.last()
            return true
        } else if (_currentTab.value != SchoolNavTab.DASHBOARD) {
            _currentTab.value = SchoolNavTab.DASHBOARD
            _navStack.value = listOf(SchoolNavTab.DASHBOARD)
            return true
        }
        return false
    }

    // Filters and Search
    val studentSearchQuery = MutableStateFlow("")
    val selectedGradeFilter = MutableStateFlow("ሁሉም") // All, 9ኛ ክፍል, 10ኛ ክፍል, etc.
    val selectedSectionFilter = MutableStateFlow("ሁሉም") // All, A, B, C, D

    val teacherSearchQuery = MutableStateFlow("")
    val subjectGradeFilter = MutableStateFlow("ሁሉም")

    // Grade and Section lists standard in Ethiopian schools
    val availableGrades = listOf(
        "ሁሉም",
        "KG 1", "KG 2", "KG 3",
        "1ኛ ክፍል", "2ኛ ክፍል", "3ኛ ክፍል", "4ኛ ክፍል", "5ኛ ክፍል", "6ኛ ክፍል",
        "7ኛ ክፍል", "8ኛ ክፍል",
        "9ኛ ክፍል", "10ኛ ክፍል", "11ኛ ክፍል", "12ኛ ክፍል"
    )

    val formGradeOptions = availableGrades.filter { it != "ሁሉም" }
    val sectionOptions = listOf("A", "B", "C", "D")

    // Reactive Data Sources
    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredStudents: StateFlow<List<Student>> = combine(
        allStudents,
        studentSearchQuery,
        selectedGradeFilter,
        selectedSectionFilter
    ) { students, query, grade, section ->
        students.filter { student ->
            val matchesQuery = query.isBlank() ||
                    student.fullName.contains(query, ignoreCase = true) ||
                    student.studentIdNumber.contains(query, ignoreCase = true) ||
                    student.parentPhone.contains(query, ignoreCase = true)
            val matchesGrade = grade == "ሁሉም" || student.grade == grade
            val matchesSection = section == "ሁሉም" || student.section == section
            matchesQuery && matchesGrade && matchesSection
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeachers: StateFlow<List<Teacher>> = repository.allTeachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teacherDepartmentFilter = MutableStateFlow("ሁሉም")

    val filteredTeachers: StateFlow<List<Teacher>> = combine(
        allTeachers,
        teacherSearchQuery,
        teacherDepartmentFilter
    ) { teachers, query, dept ->
        teachers.filter { teacher ->
            val matchesQuery = query.isBlank() ||
                    teacher.fullName.contains(query, ignoreCase = true) ||
                    teacher.teacherCode.contains(query, ignoreCase = true) ||
                    teacher.specialization.contains(query, ignoreCase = true) ||
                    teacher.department.contains(query, ignoreCase = true)
            val matchesDept = when (dept) {
                "ሁሉም" -> true
                "የክፍል ተጠሪዎች" -> teacher.isHomeroomTeacher
                else -> teacher.department.contains(dept, ignoreCase = true) ||
                        teacher.specialization.contains(dept, ignoreCase = true)
            }
            matchesQuery && matchesDept
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubjects: StateFlow<List<Subject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSubjects: StateFlow<List<Subject>> = combine(
        allSubjects,
        subjectGradeFilter
    ) { subjects, grade ->
        if (grade == "ሁሉም") subjects
        else subjects.filter { it.gradeLevel == grade || it.gradeLevel == "ሁሉም" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Staff Members (የአስተዳደር ሰራተኞች)
    val allStaff: StateFlow<List<Staff>> = repository.allStaff
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staffSearchQuery = MutableStateFlow("")
    val staffRoleFilter = MutableStateFlow("ሁሉም")
    val staffDepartmentFilter = MutableStateFlow("ሁሉም")

    val filteredStaff: StateFlow<List<Staff>> = combine(
        allStaff,
        staffSearchQuery,
        staffRoleFilter,
        staffDepartmentFilter
    ) { staffList, query, role, dept ->
        staffList.filter { staff ->
            val matchesQuery = query.isBlank() ||
                    staff.fullName.contains(query, ignoreCase = true) ||
                    staff.staffCode.contains(query, ignoreCase = true) ||
                    staff.phone.contains(query) ||
                    staff.role.contains(query, ignoreCase = true)
            val matchesRole = role == "ሁሉም" || staff.role.contains(role, ignoreCase = true)
            val matchesDept = dept == "ሁሉም" || staff.department.contains(dept, ignoreCase = true)
            matchesQuery && matchesRole && matchesDept
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkloads: StateFlow<List<TeacherWorkload>> = repository.allWorkloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // School statistics calculation
    val schoolStats: StateFlow<SchoolStats> = combine(
        allStudents,
        allTeachers,
        allStaff,
        allSubjects,
        allWorkloads
    ) { students, teachers, staff, subjects, workloads ->
        SchoolStats(
            totalStudents = students.size,
            maleStudents = students.count { it.gender == "ወንድ" || it.gender.equals("Male", ignoreCase = true) },
            femaleStudents = students.count { it.gender == "ሴት" || it.gender.equals("Female", ignoreCase = true) },
            totalTeachers = teachers.size,
            totalStaff = staff.size,
            totalSubjects = subjects.size,
            totalAllocatedPeriods = workloads.sumOf { it.periodsPerWeek },
            activeGradesCount = students.map { it.grade }.distinct().size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SchoolStats())

    // Authentication / Login Gate State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    // Role-based visible navigation tabs (ተጠቃሚው የሚመለከተውን ወሳኝ ክፍል ብቻ ያገኛል)
    val visibleTabs: StateFlow<List<SchoolNavTab>> = _currentUser.map { user ->
        val role = user?.role ?: ""
        when {
            role.contains("ርዕሰ መምህር") || role.contains("Admin") || role.contains("አስተዳዳሪ") -> {
                // Principal / Full Administrator: Has executive oversight across all school components
                listOf(
                    SchoolNavTab.DASHBOARD,
                    SchoolNavTab.STUDENTS,
                    SchoolNavTab.TEACHERS,
                    SchoolNavTab.STAFF,
                    SchoolNavTab.SUBJECTS,
                    SchoolNavTab.WORKLOAD,
                    SchoolNavTab.GRADES,
                    SchoolNavTab.ATTENDANCE
                )
            }
            role.contains("ምክትል ርዕሰ") -> {
                // Vice Principal: Focus on academic leadership, teachers, subjects, grades, students
                listOf(
                    SchoolNavTab.DASHBOARD,
                    SchoolNavTab.STUDENTS,
                    SchoolNavTab.TEACHERS,
                    SchoolNavTab.SUBJECTS,
                    SchoolNavTab.WORKLOAD,
                    SchoolNavTab.GRADES,
                    SchoolNavTab.ATTENDANCE
                )
            }
            role.contains("መምህር") || role.contains("Teacher") -> {
                // Teacher: Strictly teaching duties (workload schedule, students, grades, attendance)
                listOf(
                    SchoolNavTab.DASHBOARD,
                    SchoolNavTab.WORKLOAD,
                    SchoolNavTab.STUDENTS,
                    SchoolNavTab.GRADES,
                    SchoolNavTab.ATTENDANCE
                )
            }
            role.contains("ተማሪ") || role.contains("Student") || role.contains("ወላጅ") -> {
                // Student / Parent: Strictly personal report card grades and personal attendance
                listOf(
                    SchoolNavTab.DASHBOARD,
                    SchoolNavTab.GRADES,
                    SchoolNavTab.ATTENDANCE
                )
            }
            role.contains("ፋይናንስ") || role.contains("ሂሳብ") || role.contains("Finance") -> {
                // Finance: Staff salaries & payroll, Student fee payments
                listOf(
                    SchoolNavTab.DASHBOARD,
                    SchoolNavTab.STAFF,
                    SchoolNavTab.STUDENTS
                )
            }
            role.contains("ንብረት") || role.contains("Store") || role.contains("Property") -> {
                // Store / Property: School asset distribution and staff allocations
                listOf(
                    SchoolNavTab.DASHBOARD,
                    SchoolNavTab.STAFF
                )
            }
            role.contains("ጸሐፊ") || role.contains("መዝጋቢ") || role.contains("Registrar") -> {
                // Secretary / Registrar: Student registration, staff directory, attendance archives
                listOf(
                    SchoolNavTab.DASHBOARD,
                    SchoolNavTab.STUDENTS,
                    SchoolNavTab.STAFF,
                    SchoolNavTab.ATTENDANCE
                )
            }
            role.contains("ፅዳት") || role.contains("ጥበቃ") || role.contains("Cleaner") || role.contains("Security") -> {
                // Support Staff: Personal duty schedule, Attendance check-in
                listOf(
                    SchoolNavTab.DASHBOARD,
                    SchoolNavTab.ATTENDANCE
                )
            }
            else -> {
                listOf(
                    SchoolNavTab.DASHBOARD,
                    SchoolNavTab.ATTENDANCE
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(SchoolNavTab.DASHBOARD))

    // Matching Teacher record for the logged in user
    val currentTeacher: StateFlow<Teacher?> = combine(
        _currentUser,
        allTeachers
    ) { user, teachers ->
        if (user == null) null
        else {
            teachers.firstOrNull { it.phone == user.phone || it.fullName.contains(user.fullName) || user.fullName.contains(it.fullName) }
                ?: if (user.role.contains("መምህር")) teachers.firstOrNull() else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Matching Student record for logged in student
    val currentStudent: StateFlow<Student?> = combine(
        _currentUser,
        allStudents
    ) { user, students ->
        if (user == null) null
        else {
            students.firstOrNull { it.parentPhone == user.phone || it.fullName.contains(user.fullName) || user.fullName.contains(it.fullName) }
                ?: if (user.role.contains("ተማሪ") || user.role.contains("Student")) students.firstOrNull() else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Matching Staff record for the logged in user
    val currentStaff: StateFlow<Staff?> = combine(
        _currentUser,
        allStaff
    ) { user, staffList ->
        if (user == null) null
        else {
            staffList.firstOrNull { it.phone == user.phone || it.fullName.contains(user.fullName) || user.fullName.contains(it.fullName) }
                ?: staffList.firstOrNull { it.role.contains(user.role) || user.role.contains(it.role) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Logged in teacher's assigned workloads (የእኔ የሥራ ክፍፍል - Strictly assigned classes only!)
    val teacherWorkloads: StateFlow<List<TeacherWorkload>> = combine(
        currentTeacher,
        allWorkloads
    ) { teacher, workloads ->
        if (teacher == null) {
            emptyList()
        } else {
            // Strictly only workloads allocated to this teacher
            workloads.filter {
                it.teacherId == teacher.id ||
                it.teacherName.trim().equals(teacher.fullName.trim(), ignoreCase = true) ||
                (it.teacherName.isNotBlank() && teacher.fullName.contains(it.teacherName))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isAdmin: StateFlow<Boolean> = _currentUser.map {
        val role = it?.role ?: ""
        role.contains("ርዕሰ መምህር") || role.contains("Admin") || role.contains("አስተዳዳሪ")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isTeacher: StateFlow<Boolean> = _currentUser.map {
        val role = it?.role ?: ""
        role.contains("መምህር") && !role.contains("ርዕሰ")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isStudent: StateFlow<Boolean> = _currentUser.map {
        val role = it?.role ?: ""
        role.contains("ተማሪ") || role.contains("Student")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val allUsers: StateFlow<List<UserAccount>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val canManageStudents: StateFlow<Boolean> = combine(_currentUser, isAdmin) { user, admin ->
        val role = user?.role ?: ""
        admin || role.contains("ጸሐፊ") || role.contains("መዝጋቢ") || role.contains("ምክትል")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val loginPhone = MutableStateFlow("0911000000")
    val loginPassword = MutableStateFlow("1234")
    val loginErrorMessage = MutableStateFlow<String?>(null)
    val isAuthenticating = MutableStateFlow(false)

    fun login(phone: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            isAuthenticating.value = true
            loginErrorMessage.value = null
            var user = repository.authenticate(phone.trim(), pass.trim())

            // If account is not in user_accounts table yet but matches a Teacher, Student, or Staff member with default pin 1234
            if (user == null && pass.trim() == "1234") {
                val matchingTeacher = allTeachers.value.firstOrNull { it.phone == phone.trim() }
                if (matchingTeacher != null) {
                    val created = UserAccount(
                        fullName = matchingTeacher.fullName,
                        phone = matchingTeacher.phone,
                        password = pass.trim(),
                        role = "መምህር (Teacher)"
                    )
                    repository.registerUser(created)
                    user = created
                } else {
                    val matchingStudent = allStudents.value.firstOrNull { it.parentPhone == phone.trim() || it.studentIdNumber.equals(phone.trim(), ignoreCase = true) }
                    if (matchingStudent != null) {
                        val created = UserAccount(
                            fullName = matchingStudent.fullName,
                            phone = matchingStudent.parentPhone,
                            password = pass.trim(),
                            role = "ተማሪ (Student)"
                        )
                        repository.registerUser(created)
                        user = created
                    } else {
                        val matchingStaff = allStaff.value.firstOrNull { it.phone == phone.trim() }
                        if (matchingStaff != null) {
                            val created = UserAccount(
                                fullName = matchingStaff.fullName,
                                phone = matchingStaff.phone,
                                password = pass.trim(),
                                role = matchingStaff.role
                            )
                            repository.registerUser(created)
                            user = created
                        }
                    }
                }
            }

            if (user != null) {
                _currentUser.value = user
                _isLoggedIn.value = true
                _currentTab.value = SchoolNavTab.DASHBOARD
                _navStack.value = listOf(SchoolNavTab.DASHBOARD)
                if (user.role.contains("ተማሪ") || user.role.contains("Student")) {
                    val std = allStudents.value.firstOrNull { it.fullName.contains(user.fullName) || it.parentPhone == user.phone } ?: allStudents.value.firstOrNull()
                    if (std != null) {
                        selectedStudentForGrades.value = std
                    }
                }
                isAuthenticating.value = false
                onSuccess()
            } else {
                isAuthenticating.value = false
                loginErrorMessage.value = if (_isAmharic.value)
                    "የስልክ ቁጥር ወይም የይለፍ ቃል ልክ አይደለም! እባክዎ እንደገና ይሞክሩ።"
                else
                    "Incorrect phone number or password! Please try again."
            }
        }
    }

    // Role-specific quick login for testing and instant access
    fun quickLoginAs(phone: String, pass: String = "1234") {
        loginPhone.value = phone
        loginPassword.value = pass
        login(phone, pass)
    }

    fun quickLoginAsPrincipal() {
        quickLoginAs("0911000000", "1234")
    }

    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
        loginPassword.value = ""
        loginErrorMessage.value = null
        _currentTab.value = SchoolNavTab.DASHBOARD
        _navStack.value = listOf(SchoolNavTab.DASHBOARD)
    }

    fun onGoogleSignInSuccess(name: String, email: String, uid: String) {
        val googleUser = UserAccount(
            fullName = name.ifBlank { "የክላውድ ተጠቃሚ (Cloud Admin)" },
            phone = if (email.isNotBlank()) email else "Google Auth",
            password = "",
            role = "ርዕሰ መምህር (Admin)"
        )
        viewModelScope.launch {
            repository.registerUser(googleUser)
        }
        _currentUser.value = googleUser
        _isLoggedIn.value = true
        _currentTab.value = SchoolNavTab.DASHBOARD
        _navStack.value = listOf(SchoolNavTab.DASHBOARD)
    }

    fun onGoogleSignInError(errorMsg: String) {
        loginErrorMessage.value = if (_isAmharic.value)
            "በGoogle መግባት አልተሳካም፦ $errorMsg"
        else
            "Google Sign-In failed: $errorMsg"
    }

    fun registerAccount(name: String, phone: String, pass: String, role: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val newUser = UserAccount(
                fullName = name,
                phone = phone,
                password = pass,
                role = role
            )
            repository.registerUser(newUser)
            _currentUser.value = newUser
            _isLoggedIn.value = true
            _currentTab.value = SchoolNavTab.DASHBOARD
            _navStack.value = listOf(SchoolNavTab.DASHBOARD)
            onDone()
        }
    }

    // Dialog & Interaction States
    val showAddStudentDialog = MutableStateFlow(false)
    val studentToEdit = MutableStateFlow<Student?>(null)
    val selectedStudentDetail = MutableStateFlow<Student?>(null)

    val showAddTeacherDialog = MutableStateFlow(false)
    val teacherToEdit = MutableStateFlow<Teacher?>(null)
    val selectedTeacherForDetails = MutableStateFlow<Teacher?>(null)
    val showQuickAssignDialog = MutableStateFlow(false)
    val teacherForQuickAssign = MutableStateFlow<Teacher?>(null)

    val showAddStaffDialog = MutableStateFlow(false)
    val staffToEdit = MutableStateFlow<Staff?>(null)
    val selectedStaffForDetails = MutableStateFlow<Staff?>(null)

    fun saveStaff(staff: Staff) {
        viewModelScope.launch {
            val id = if (staff.id == 0L) {
                repository.insertStaff(staff)
            } else {
                repository.updateStaff(staff)
                staff.id
            }
            firestoreRepository.saveStaff(authUid, staff.copy(id = id))
            showAddStaffDialog.value = false
            staffToEdit.value = null
        }
    }

    fun deleteStaff(staff: Staff) {
        viewModelScope.launch {
            repository.deleteStaff(staff)
            val docId = "staff_${staff.staffCode.replace(Regex("[^a-zA-Z0-9_]"), "_")}"
            firestoreRepository.deleteStudent(docId)
            if (selectedStaffForDetails.value?.id == staff.id) {
                selectedStaffForDetails.value = null
            }
        }
    }

    val showAddSubjectDialog = MutableStateFlow(false)
    val subjectToEdit = MutableStateFlow<Subject?>(null)

    val showAddWorkloadDialog = MutableStateFlow(false)
    val workloadToEdit = MutableStateFlow<TeacherWorkload?>(null)

    val showAddGradeDialog = MutableStateFlow(false)
    val showAssessmentSchemeDialog = MutableStateFlow(false)
    val selectedStudentForGrades = MutableStateFlow<Student?>(null)

    // Dynamic Assessment Scheme (የኢትዮጵያ ስርዓተ-ትምህርት ውጤት አሰጣጥ ማዋቀሪያ)
    private val prefs = application.getSharedPreferences("dilijent_school_prefs", Context.MODE_PRIVATE)
    private val _assessmentScheme = MutableStateFlow(loadAssessmentScheme())
    val assessmentScheme: StateFlow<AssessmentScheme> = _assessmentScheme.asStateFlow()

    private fun loadAssessmentScheme(): AssessmentScheme {
        val id = prefs.getString("scheme_id", AssessmentScheme.MOE_STANDARD.id) ?: AssessmentScheme.MOE_STANDARD.id
        val preset = AssessmentScheme.PRESETS.find { it.id == id }
        return if (preset != null && id != "custom") {
            preset
        } else {
            AssessmentScheme(
                id = prefs.getString("scheme_id", "custom") ?: "custom",
                titleAmharic = prefs.getString("scheme_title_am", "ብጁ ስርዓተ-ትምህርት") ?: "ብጁ ስርዓተ-ትምህርት",
                titleEnglish = prefs.getString("scheme_title_en", "Custom Assessment Scheme") ?: "Custom Assessment Scheme",
                schoolType = prefs.getString("scheme_school_type", "የግል") ?: "የግል",
                test1Label = prefs.getString("scheme_t1_label", "ጥያቄ/ፈተና 1") ?: "ጥያቄ/ፈተና 1",
                test1Weight = prefs.getFloat("scheme_t1_weight", 10f),
                assignmentLabel = prefs.getString("scheme_as_label", "የቤት ስራ/ፕሮጀክት") ?: "የቤት ስራ/ፕሮጀክት",
                assignmentWeight = prefs.getFloat("scheme_as_weight", 10f),
                midExamLabel = prefs.getString("scheme_mid_label", "አጋማሽ ፈተና") ?: "አጋማሽ ፈተና",
                midExamWeight = prefs.getFloat("scheme_mid_weight", 30f),
                finalExamLabel = prefs.getString("scheme_final_label", "ማጠቃለያ ፈተና") ?: "ማጠቃለያ ፈተና",
                finalExamWeight = prefs.getFloat("scheme_final_weight", 50f)
            )
        }
    }

    fun updateAssessmentScheme(newScheme: AssessmentScheme) {
        _assessmentScheme.value = newScheme
        prefs.edit()
            .putString("scheme_id", newScheme.id)
            .putString("scheme_title_am", newScheme.titleAmharic)
            .putString("scheme_title_en", newScheme.titleEnglish)
            .putString("scheme_school_type", newScheme.schoolType)
            .putString("scheme_t1_label", newScheme.test1Label)
            .putFloat("scheme_t1_weight", newScheme.test1Weight)
            .putString("scheme_as_label", newScheme.assignmentLabel)
            .putFloat("scheme_as_weight", newScheme.assignmentWeight)
            .putString("scheme_mid_label", newScheme.midExamLabel)
            .putFloat("scheme_mid_weight", newScheme.midExamWeight)
            .putString("scheme_final_label", newScheme.finalExamLabel)
            .putFloat("scheme_final_weight", newScheme.finalExamWeight)
            .apply()
        showAssessmentSchemeDialog.value = false
    }

    // All Grades and Grades for selected student
    val allGrades: StateFlow<List<StudentGrade>> = repository.allGrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentGrades = MutableStateFlow<List<StudentGrade>>(emptyList())

    fun selectStudentForGrades(student: Student) {
        selectedStudentForGrades.value = student
        viewModelScope.launch {
            repository.getGradesForStudent(student.id).collect {
                studentGrades.value = it
            }
        }
    }

    // CRUD Operations: Student
    fun saveStudent(student: Student) {
        viewModelScope.launch {
            val id = if (student.id == 0L) {
                repository.insertStudent(student)
            } else {
                repository.updateStudent(student)
                student.id
            }
            firestoreRepository.saveStudent(authUid, student.copy(id = id))
            showAddStudentDialog.value = false
            studentToEdit.value = null
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            val docId = "student_${student.studentIdNumber.replace(Regex("[^a-zA-Z0-9_]"), "_")}"
            firestoreRepository.deleteStudent(docId)
            if (selectedStudentDetail.value?.id == student.id) {
                selectedStudentDetail.value = null
            }
            if (selectedStudentForGrades.value?.id == student.id) {
                selectedStudentForGrades.value = null
            }
        }
    }

    // CRUD Operations: Teacher
    fun saveTeacher(teacher: Teacher) {
        viewModelScope.launch {
            val id = if (teacher.id == 0L) {
                repository.insertTeacher(teacher)
            } else {
                repository.updateTeacher(teacher)
                teacher.id
            }
            firestoreRepository.saveTeacher(authUid, teacher.copy(id = id))
            showAddTeacherDialog.value = false
            teacherToEdit.value = null
        }
    }

    fun deleteTeacher(teacher: Teacher) {
        viewModelScope.launch {
            repository.deleteTeacher(teacher)
            val docId = "teacher_${teacher.teacherCode.replace(Regex("[^a-zA-Z0-9_]"), "_")}"
            firestoreRepository.deleteStudent(docId)
            if (selectedTeacherForDetails.value?.id == teacher.id) {
                selectedTeacherForDetails.value = null
            }
        }
    }

    // CRUD Operations: Subject
    fun saveSubject(subject: Subject) {
        viewModelScope.launch {
            val id = if (subject.id == 0L) {
                repository.insertSubject(subject)
            } else {
                repository.updateSubject(subject)
                subject.id
            }
            firestoreRepository.saveSubject(authUid, subject.copy(id = id))
            showAddSubjectDialog.value = false
            subjectToEdit.value = null
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    // CRUD Operations: Workload Allotment (የሥራ ክፍፍል)
    fun saveWorkload(workload: TeacherWorkload) {
        viewModelScope.launch {
            val id = if (workload.id == 0L) {
                repository.insertWorkload(workload)
            } else {
                repository.updateWorkload(workload)
                workload.id
            }
            firestoreRepository.saveWorkload(authUid, workload.copy(id = id))
            showAddWorkloadDialog.value = false
            workloadToEdit.value = null
        }
    }

    fun deleteWorkload(workload: TeacherWorkload) {
        viewModelScope.launch {
            repository.deleteWorkload(workload)
        }
    }

    // CRUD Operations: Grades
    fun saveGrade(grade: StudentGrade) {
        viewModelScope.launch {
            val total = grade.quizScore + grade.assignmentScore + grade.midExamScore + grade.finalExamScore
            val letter = when {
                total >= 90 -> "A"
                total >= 80 -> "B"
                total >= 65 -> "C"
                total >= 50 -> "D"
                else -> "F"
            }
            val computedGrade = grade.copy(totalScore = total, letterGrade = letter)
            val id = if (computedGrade.id == 0L) {
                repository.insertGrade(computedGrade)
            } else {
                repository.updateGrade(computedGrade)
                computedGrade.id
            }
            firestoreRepository.saveGrade(authUid, computedGrade.copy(id = id))
            showAddGradeDialog.value = false
        }
    }

    fun deleteGrade(grade: StudentGrade) {
        viewModelScope.launch {
            repository.deleteGrade(grade)
        }
    }

    // Attendance Operations
    val attendanceDate = MutableStateFlow("2017-02-16")
    val attendanceGrade = MutableStateFlow("9ኛ ክፍል")
    val attendanceSection = MutableStateFlow("A")

    val attendanceList = MutableStateFlow<List<AttendanceRecord>>(emptyList())

    fun recordAttendance(record: AttendanceRecord) {
        viewModelScope.launch {
            repository.insertAttendance(record)
            firestoreRepository.saveAttendance(authUid, record)
        }
    }
}
