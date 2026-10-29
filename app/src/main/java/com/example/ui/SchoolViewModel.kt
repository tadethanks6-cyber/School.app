package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.SchoolDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.SchoolStats
import com.example.data.model.Staff
import com.example.data.model.StaffAttendanceItem
import com.example.data.model.StaffAttendanceSession
import com.example.data.model.StaffRoleSchedule
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
import kotlinx.coroutines.flow.first
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

    private val prefs = application.getSharedPreferences("dilijent_school_prefs", Context.MODE_PRIVATE)
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

        // Ensure System Admin account exists for Email login and seed Primary 1-8 data if missing
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            seedPrimarySchoolDataIfNeeded()
            val adminUser = repository.getUserByEmail("admin@dilijent.edu.et")
            if (adminUser == null) {
                repository.registerUser(
                    UserAccount(
                        fullName = "ሲስተም አድሚን (እኔ)",
                        email = "admin@dilijent.edu.et",
                        phone = "0900000000",
                        password = "admin123",
                        role = "ሲስተም አድሚን (Super Admin)"
                    )
                )
            }
        }
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

    // Dynamic School Settings & Profile
    val showSchoolSettingsDialog = MutableStateFlow(false)
    private val _schoolLogoUri = MutableStateFlow(loadInitialLogo())
    val schoolLogoUri: StateFlow<String> = _schoolLogoUri.asStateFlow()

    private val _schoolGradeLevel = MutableStateFlow(prefs.getString("school_grade_level", "1_8") ?: "1_8")
    val schoolGradeLevel: StateFlow<String> = _schoolGradeLevel.asStateFlow()

    private val _schoolName = MutableStateFlow(prefs.getString("school_name", "ዲሊጀንት ትምህርት ቤት") ?: "ዲሊጀንት ትምህርት ቤት")
    val schoolName: StateFlow<String> = _schoolName.asStateFlow()

    private val _schoolNameEn = MutableStateFlow(prefs.getString("school_name_en", "Dilijent School") ?: "Dilijent School")
    val schoolNameEn: StateFlow<String> = _schoolNameEn.asStateFlow()

    private fun loadInitialLogo(): String {
        val saved = prefs.getString("school_logo_uri", "") ?: ""
        if (saved.isNotEmpty()) {
            try {
                val file = java.io.File(getApplication<Application>().filesDir, "custom_school_logo.png")
                if (file.exists()) return file.toURI().toString()
            } catch (_: Exception) {}
        }
        return saved
    }

    private fun getGradesForLevel(level: String, includeAll: Boolean): List<String> {
        val list = when (level) {
            "1_8" -> listOf(
                "1ኛ ክፍል", "2ኛ ክፍል", "3ኛ ክፍል", "4ኛ ክፍል", "5ኛ ክፍል", "6ኛ ክፍል", "7ኛ ክፍል", "8ኛ ክፍል"
            )
            "9_12" -> listOf(
                "9ኛ ክፍል", "10ኛ ክፍል", "11ኛ ክፍል", "12ኛ ክፍል"
            )
            else -> listOf(
                "KG 1", "KG 2", "KG 3",
                "1ኛ ክፍል", "2ኛ ክፍል", "3ኛ ክፍል", "4ኛ ክፍል", "5ኛ ክፍል", "6ኛ ክፍል", "7ኛ ክፍል", "8ኛ ክፍል",
                "9ኛ ክፍል", "10ኛ ክፍል", "11ኛ ክፍል", "12ኛ ክፍል"
            )
        }
        return if (includeAll) listOf("ሁሉም") + list else list
    }

    // Dynamic Grade and Section lists standard in Ethiopian schools
    val availableGrades: StateFlow<List<String>> = _schoolGradeLevel.map { level ->
        getGradesForLevel(level, true)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, getGradesForLevel(prefs.getString("school_grade_level", "1_8") ?: "1_8", true))

    val formGradeOptions: StateFlow<List<String>> = _schoolGradeLevel.map { level ->
        getGradesForLevel(level, false)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, getGradesForLevel(prefs.getString("school_grade_level", "1_8") ?: "1_8", false))

    val sectionOptions = listOf("A", "B", "C", "D")

    // Strict School Level Data Segregation Helpers (ዳታ እንዳይቀላቀል ጥብቅ ማጣሪያ)
    fun isGradeInLevel(grade: String, level: String): Boolean {
        if (level == "ALL") return true
        val clean = grade.trim()
        val numbers = Regex("\\d+").findAll(clean).map { it.value.toIntOrNull() }.filterNotNull().toList()
        return if (level == "1_8") {
            if (numbers.any { it in 9..12 }) return false
            if (clean.contains("9ኛ") || clean.contains("10ኛ") || clean.contains("11ኛ") || clean.contains("12ኛ")) return false
            if (clean.startsWith("9") || clean.startsWith("10") || clean.startsWith("11") || clean.startsWith("12")) return false
            if (numbers.any { it in 1..8 }) return true
            if (clean.contains("1ኛ") || clean.contains("2ኛ") || clean.contains("3ኛ") || clean.contains("4ኛ") ||
                clean.contains("5ኛ") || clean.contains("6ኛ") || clean.contains("7ኛ") || clean.contains("8ኛ") ||
                clean.contains("KG") || clean.contains("መሰረታዊ") || clean.contains("መካከለኛ")) return true
            true
        } else if (level == "9_12") {
            if (numbers.any { it in 9..12 }) return true
            if (clean.contains("9ኛ") || clean.contains("10ኛ") || clean.contains("11ኛ") || clean.contains("12ኛ")) return true
            if (clean.startsWith("9") || clean.startsWith("10") || clean.startsWith("11") || clean.startsWith("12")) return true
            if (clean.contains("Natural") || clean.contains("Social") || clean.contains("ሁለተኛ ደረጃ") || clean.contains("High")) return true
            false
        } else {
            true
        }
    }

    fun isTeacherInLevel(teacher: Teacher, workloads: List<TeacherWorkload>, level: String): Boolean {
        if (level == "ALL") return true
        if (teacher.homeroomClass.isNotBlank()) {
            if (isGradeInLevel(teacher.homeroomClass, level)) return true
            val otherLevel = if (level == "1_8") "9_12" else "1_8"
            if (isGradeInLevel(teacher.homeroomClass, otherLevel)) return false
        }
        val teacherWls = workloads.filter { it.teacherId == teacher.id }
        if (teacherWls.isNotEmpty()) {
            return teacherWls.any { isGradeInLevel(it.grade, level) }
        }
        val text = "${teacher.specialization} ${teacher.department}"
        if (level == "1_8") {
            if (text.contains("9") || text.contains("10") || text.contains("11") || text.contains("12") || text.contains("ሁለተኛ ደረጃ")) return false
            return true
        } else if (level == "9_12") {
            if (text.contains("1-8") || text.contains("መሰረታዊ") || text.contains("መካከለኛ") || text.contains("Primary")) return false
            return true
        }
        return true
    }

    fun isSubjectInLevel(subject: Subject, level: String): Boolean {
        if (level == "ALL") return true
        if (subject.gradeLevel == "ሁሉም") return true
        return isGradeInLevel(subject.gradeLevel, level)
    }

    // Raw Database Flows
    val rawStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawTeachers: StateFlow<List<Teacher>> = repository.allTeachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawSubjects: StateFlow<List<Subject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawWorkloads: StateFlow<List<TeacherWorkload>> = repository.allWorkloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawGrades: StateFlow<List<StudentGrade>> = repository.allGrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Strictly Segregated Level-Specific Flows (ዳታ በፍፁም አይቀላቀልም)
    val allStudents: StateFlow<List<Student>> = combine(
        rawStudents,
        _schoolGradeLevel
    ) { list, level ->
        list.filter { isGradeInLevel(it.grade, level) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkloads: StateFlow<List<TeacherWorkload>> = combine(
        rawWorkloads,
        _schoolGradeLevel
    ) { list, level ->
        list.filter { isGradeInLevel(it.grade, level) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeachers: StateFlow<List<Teacher>> = combine(
        rawTeachers,
        rawWorkloads,
        _schoolGradeLevel
    ) { teachers, workloads, level ->
        teachers.filter { isTeacherInLevel(it, workloads, level) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubjects: StateFlow<List<Subject>> = combine(
        rawSubjects,
        _schoolGradeLevel
    ) { list, level ->
        list.filter { isSubjectInLevel(it, level) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGrades: StateFlow<List<StudentGrade>> = combine(
        rawGrades,
        _schoolGradeLevel
    ) { list, level ->
        list.filter { isGradeInLevel(it.grade, level) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    val isSuperAdmin: StateFlow<Boolean> = _currentUser.map {
        val role = it?.role ?: ""
        val email = it?.email ?: ""
        role.contains("ሲስተም") || email.equals("admin@dilijent.edu.et", ignoreCase = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isAdmin: StateFlow<Boolean> = _currentUser.map {
        val role = it?.role ?: ""
        role.contains("ርዕሰ መምህር") || role.contains("Admin") || role.contains("አስተዳዳሪ") || role.contains("ሲስተም")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isVicePrincipal: StateFlow<Boolean> = _currentUser.map {
        val role = it?.role ?: ""
        role.contains("ምክትል") || role.contains("Vice")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Staff Attendance access is strictly reserved for Vice Principal and Admin / Principal
    val canAccessStaffAttendance: StateFlow<Boolean> = _currentUser.map {
        val role = it?.role ?: ""
        val email = it?.email ?: ""
        role.contains("ምክትል") || role.contains("Vice") ||
        role.contains("ርዕሰ መምህር") || role.contains("Principal") ||
        role.contains("Admin") || role.contains("አስተዳዳሪ") || role.contains("ሲስተም") ||
        email.equals("admin@dilijent.edu.et", ignoreCase = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isTeacher: StateFlow<Boolean> = _currentUser.map {
        val role = it?.role ?: ""
        role.contains("መምህር") && !role.contains("ርዕሰ") && !role.contains("ምክትል")
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

    // Default Shift Schedules per staff category (የሰራተኛ የስራ ሰዓት ገደብ ማዋቀሪያ)
    val defaultStaffRoleSchedules = listOf(
        StaffRoleSchedule(
            roleCategory = "መምህራን",
            entryStart = "01:30 ጧት",
            entryEnd = "01:51 ጧት",
            exitStart = "09:15 ከሰዓት",
            exitEnd = "09:40 ከሰዓት",
            description = "የመማር ማስተማር መደበኛ ክፍለ ጊዜያት"
        ),
        StaffRoleSchedule(
            roleCategory = "ፅዳት ሰራተኞች",
            entryStart = "12:30 ጧት",
            entryEnd = "01:15 ጧት",
            exitStart = "08:30 ከሰዓት",
            exitEnd = "09:00 ከሰዓት",
            description = "የትምህርት ቤቱን ግቢና ክፍሎች ቀድመው ማዘጋጀት"
        ),
        StaffRoleSchedule(
            roleCategory = "ደህንነትና ጥበቃ",
            entryStart = "12:00 ጧት",
            entryEnd = "12:45 ጧት",
            exitStart = "11:30 ከሰዓት",
            exitEnd = "12:00 ከሰዓት",
            description = "የበርና የግቢ ጥበቃ የስራ ፈረቃ"
        ),
        StaffRoleSchedule(
            roleCategory = "አስተዳደርና ፋይናንስ",
            entryStart = "01:45 ጧት",
            entryEnd = "02:00 ጧት",
            exitStart = "10:00 ከሰዓት",
            exitEnd = "10:30 ከሰዓት",
            description = "የጽህፈት ቤት፣ የሂሳብና የሰው ኃይል ሰዓት"
        ),
        StaffRoleSchedule(
            roleCategory = "ሌሎች ሰራተኞች",
            entryStart = "01:30 ጧት",
            entryEnd = "02:00 ጧት",
            exitStart = "09:00 ከሰዓት",
            exitEnd = "09:30 ከሰዓት",
            description = "አጠቃላይ ድጋፍ ሰጪ ሰራተኞች"
        )
    )

    private fun loadStaffRoleSchedules(): List<StaffRoleSchedule> {
        val raw = prefs.getString("staff_role_schedules_data", null) ?: return defaultStaffRoleSchedules
        return try {
            val lines = raw.split("###")
            val result = lines.mapNotNull { line ->
                val parts = line.split("|||")
                if (parts.size >= 5) {
                    StaffRoleSchedule(
                        roleCategory = parts[0],
                        entryStart = parts[1],
                        entryEnd = parts[2],
                        exitStart = parts[3],
                        exitEnd = parts[4],
                        description = parts.getOrNull(5) ?: ""
                    )
                } else null
            }
            if (result.isNotEmpty()) result else defaultStaffRoleSchedules
        } catch (e: Exception) {
            defaultStaffRoleSchedules
        }
    }

    private fun saveStaffRoleSchedulesToPrefs(list: List<StaffRoleSchedule>) {
        val serialized = list.joinToString("###") {
            "${it.roleCategory}|||${it.entryStart}|||${it.entryEnd}|||${it.exitStart}|||${it.exitEnd}|||${it.description}"
        }
        prefs.edit().putString("staff_role_schedules_data", serialized).apply()
    }

    private val _staffRoleSchedules = MutableStateFlow<List<StaffRoleSchedule>>(loadStaffRoleSchedules())
    val staffRoleSchedules: StateFlow<List<StaffRoleSchedule>> = _staffRoleSchedules.asStateFlow()

    val showStaffScheduleDialog = MutableStateFlow(false)

    fun updateStaffRoleSchedule(
        category: String,
        entryStart: String,
        entryEnd: String,
        exitStart: String,
        exitEnd: String,
        description: String = ""
    ) {
        val current = _staffRoleSchedules.value.toMutableList()
        val index = current.indexOfFirst { it.roleCategory == category }
        val updated = StaffRoleSchedule(
            roleCategory = category,
            entryStart = entryStart.trim(),
            entryEnd = entryEnd.trim(),
            exitStart = exitStart.trim(),
            exitEnd = exitEnd.trim(),
            description = description
        )
        if (index >= 0) {
            current[index] = updated
        } else {
            current.add(updated)
        }
        _staffRoleSchedules.value = current
        saveStaffRoleSchedulesToPrefs(current)
    }

    fun resetStaffRoleSchedulesToDefault() {
        _staffRoleSchedules.value = defaultStaffRoleSchedules
        saveStaffRoleSchedulesToPrefs(defaultStaffRoleSchedules)
    }

    fun getStaffRoleCategory(isTeacher: Boolean, role: String, department: String): String {
        if (isTeacher) return "መምህራን"
        val combined = "$role $department".lowercase()
        return when {
            combined.contains("ፅዳት") || combined.contains("cleaner") -> "ፅዳት ሰራተኞች"
            combined.contains("ጥበቃ") || combined.contains("ደህንነት") || combined.contains("guard") || combined.contains("security") -> "ደህንነትና ጥበቃ"
            combined.contains("አስተዳደር") || combined.contains("ፋይናንስ") || combined.contains("ሂሳብ") || combined.contains("ጸሐፊ") || combined.contains("መዝጋቢ") || combined.contains("admin") || combined.contains("finance") || combined.contains("accountant") || combined.contains("hr") -> "አስተዳደርና ፋይናንስ"
            else -> "ሌሎች ሰራተኞች"
        }
    }

    fun getRoleSchedule(roleCategory: String): StaffRoleSchedule {
        return _staffRoleSchedules.value.firstOrNull { it.roleCategory == roleCategory }
            ?: defaultStaffRoleSchedules.firstOrNull { it.roleCategory == roleCategory }
            ?: defaultStaffRoleSchedules.first()
    }

    // Staff Attendance Tracking: Two Sessions (የመግቢያ Check-In እና የመውጫ Check-Out)
    data class StaffSessionRecord(
        val entryStatus: String = "ተገኝቷል",
        val entryTime: String = "01:42 ጧት",
        val entryRemarks: String = "በሰዓቱ ገብተዋል",
        val exitStatus: String = "ተገኝቷል",
        val exitTime: String = "09:25 ከሰዓት",
        val exitRemarks: String = "በሰዓቱ ወጥተዋል",
        val morningStatus: String = entryStatus,
        val morningTime: String = entryTime,
        val morningRemarks: String = entryRemarks,
        val afternoonStatus: String = exitStatus,
        val afternoonTime: String = exitTime,
        val afternoonRemarks: String = exitRemarks
    )

    val currentStaffAttendanceSession = MutableStateFlow(StaffAttendanceSession.CHECK_IN)
    private val _staffAttendanceRecords = MutableStateFlow<Map<Long, StaffSessionRecord>>(emptyMap())

    val staffAttendanceList: StateFlow<List<StaffAttendanceItem>> = combine(
        filteredTeachers,
        filteredStaff,
        _staffAttendanceRecords,
        currentStaffAttendanceSession,
        _staffRoleSchedules
    ) { teachers, staffList, records, activeSession, _ ->
        val list = mutableListOf<StaffAttendanceItem>()
        val isEntry = activeSession == StaffAttendanceSession.CHECK_IN

        // Teachers
        teachers.forEach { t ->
            val roleCat = "መምህራን"
            val rec = records[t.id] ?: StaffSessionRecord()
            val activeStatus = if (isEntry) rec.entryStatus else rec.exitStatus
            val activeTime = if (isEntry) rec.entryTime else rec.exitTime
            val activeRemarks = if (isEntry) rec.entryRemarks else rec.exitRemarks

            list.add(
                StaffAttendanceItem(
                    id = t.id,
                    name = t.fullName,
                    role = if (t.isHomeroomTeacher) "መምህር (የክፍል ኃላፊ - ${t.homeroomClass})" else "መምህር (${t.specialization})",
                    department = t.department,
                    phone = t.phone,
                    isTeacher = true,
                    roleCategory = roleCat,
                    entryStatus = rec.entryStatus,
                    entryTime = rec.entryTime,
                    entryRemarks = rec.entryRemarks,
                    exitStatus = rec.exitStatus,
                    exitTime = rec.exitTime,
                    exitRemarks = rec.exitRemarks,
                    morningStatus = rec.entryStatus,
                    morningTime = rec.entryTime,
                    morningRemarks = rec.entryRemarks,
                    afternoonStatus = rec.exitStatus,
                    afternoonTime = rec.exitTime,
                    afternoonRemarks = rec.exitRemarks,
                    status = activeStatus,
                    checkInTime = activeTime,
                    remarks = activeRemarks
                )
            )
        }
        // Staff
        staffList.forEach { s ->
            val staffKey = -(s.id) // Negative ID to prevent collision with teachers
            val roleCat = getStaffRoleCategory(false, s.role, s.department)
            val rec = records[staffKey] ?: StaffSessionRecord()
            val activeStatus = if (isEntry) rec.entryStatus else rec.exitStatus
            val activeTime = if (isEntry) rec.entryTime else rec.exitTime
            val activeRemarks = if (isEntry) rec.entryRemarks else rec.exitRemarks

            list.add(
                StaffAttendanceItem(
                    id = staffKey,
                    name = s.fullName,
                    role = s.role,
                    department = s.department,
                    phone = s.phone,
                    isTeacher = false,
                    roleCategory = roleCat,
                    entryStatus = rec.entryStatus,
                    entryTime = rec.entryTime,
                    entryRemarks = rec.entryRemarks,
                    exitStatus = rec.exitStatus,
                    exitTime = rec.exitTime,
                    exitRemarks = rec.exitRemarks,
                    morningStatus = rec.entryStatus,
                    morningTime = rec.entryTime,
                    morningRemarks = rec.entryRemarks,
                    afternoonStatus = rec.exitStatus,
                    afternoonTime = rec.exitTime,
                    afternoonRemarks = rec.exitRemarks,
                    status = activeStatus,
                    checkInTime = activeTime,
                    remarks = activeRemarks
                )
            )
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Converts current time into Ethiopian 12-hour clock (6-hour difference)
     */
    fun getNowEthiopianTime(isEntry: Boolean): String {
        val cal = java.util.Calendar.getInstance()
        val hour24 = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val min = cal.get(java.util.Calendar.MINUTE)
        val ethHourRaw = (hour24 - 6 + 24) % 24
        val ethHour = when (val h = ethHourRaw % 12) {
            0 -> 12
            else -> h
        }
        val period = if (isEntry) "ጧት" else "ከሰዓት"
        return String.format(java.util.Locale.US, "%02d:%02d %s", ethHour, min, period)
    }

    /**
     * Parses time string like "01:51 ጧት" or "09:40 ከሰዓት" into minutes of the day/period.
     */
    private fun parseTimeToMinutes(timeStr: String): Int {
        val clean = timeStr.replace("ጧት", "").replace("ከሰዓት", "").trim()
        val parts = clean.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val normalizedH = if (h == 12) 0 else h
        return normalizedH * 60 + m
    }

    /**
     * Evaluates attendance status based on the specific staff category schedule:
     * - የመግቢያ (Check-In):
     *   * Within or before role's entryEnd -> "ተገኝቷል" ("በሰዓቱ ገብተዋል")
     *   * After role's entryEnd -> "አርፍዷል" ("ከ... በኋላ አርፍደዋል")
     * - የመውጫ (Check-Out):
     *   * Before role's exitStart -> "ቀድሞ ወጥቷል" ("ከ... በፊት ቀድመው ወጥተዋል")
     *   * Between exitStart and exitEnd -> "ተገኝቷል" ("በሰዓቱ ወጥተዋል")
     *   * After role's exitEnd -> "ዘግይቶ ወጥቷል" ("የትርፍ ሰዓት/ከ... በኋላ ወጥተዋል")
     */
    fun evaluateAttendanceStatus(
        timeStr: String,
        session: StaffAttendanceSession,
        roleCategory: String
    ): Pair<String, String> {
        val sched = getRoleSchedule(roleCategory)
        val isEntry = session == StaffAttendanceSession.CHECK_IN
        val timeMin = parseTimeToMinutes(timeStr)

        return if (isEntry) {
            val entryEndMin = parseTimeToMinutes(sched.entryEnd)
            if (timeMin <= entryEndMin) {
                Pair("ተገኝቷል", "በሰዓቱ ገብተዋል (${sched.entryStart} - ${sched.entryEnd})")
            } else {
                Pair("አርፍዷል", "ከ${sched.entryEnd} በኋላ አርፍደዋል")
            }
        } else {
            val exitStartMin = parseTimeToMinutes(sched.exitStart)
            val exitEndMin = parseTimeToMinutes(sched.exitEnd)
            when {
                timeMin < exitStartMin -> {
                    Pair("ቀድሞ ወጥቷል", "ከ${sched.exitStart} በፊት ቀድመው ወጥተዋል")
                }
                timeMin <= exitEndMin -> {
                    Pair("ተገኝቷል", "በሰዓቱ ወጥተዋል (${sched.exitStart} - ${sched.exitEnd})")
                }
                else -> {
                    Pair("ዘግይቶ ወጥቷል", "የትርፍ ሰዓት/ከ${sched.exitEnd} በኋላ ወጥተዋል")
                }
            }
        }
    }

    /**
     * Auto tick action: When Vice Principal / Admin ticks attendance,
     * records EXACT current time automatically and checks against the staff member's role schedule.
     */
    fun autoTickStaffAttendance(id: Long, session: StaffAttendanceSession, statusOverride: String? = null) {
        val isEntry = session == StaffAttendanceSession.CHECK_IN
        val currentTime = getNowEthiopianTime(isEntry)

        val currentItem = staffAttendanceList.value.firstOrNull { it.id == id }
        val roleCat = currentItem?.roleCategory ?: "መምህራን"

        val (calcStatus, defaultRemarks) = evaluateAttendanceStatus(currentTime, session, roleCat)
        val finalStatus = statusOverride ?: calcStatus
        val finalRemarks = when (finalStatus) {
            "ተገኝቷል" -> defaultRemarks
            "ቀርቷል" -> "ያለፈቃድ የቀረ"
            "አርፍዷል" -> if (defaultRemarks.contains("አርፍደዋል")) defaultRemarks else "ዘግይቶ መግባት ተመዝግቧል"
            "ቀድሞ ወጥቷል" -> defaultRemarks
            "ዘግይቶ ወጥቷል" -> defaultRemarks
            "በፍቃድ" -> "የህመም ወይም የስራ ፍቃድ"
            else -> defaultRemarks
        }

        saveStaffAttendanceManual(
            id = id,
            session = session,
            status = finalStatus,
            time = currentTime,
            remarks = finalRemarks
        )
    }

    /**
     * Manual edit action: For correcting forgotten records or manual adjustments.
     */
    fun saveStaffAttendanceManual(
        id: Long,
        session: StaffAttendanceSession,
        status: String,
        time: String,
        remarks: String
    ) {
        val map = _staffAttendanceRecords.value.toMutableMap()
        val current = map[id] ?: StaffSessionRecord()
        val isEntry = session == StaffAttendanceSession.CHECK_IN
        val formattedTime = time.ifBlank {
            if (isEntry) "01:40 ጧት" else "09:25 ከሰዓት"
        }

        map[id] = if (isEntry) {
            current.copy(
                entryStatus = status,
                entryTime = formattedTime,
                entryRemarks = remarks.ifBlank { "በም/ር ተስተካክሎ ተመዝግቧል" },
                morningStatus = status,
                morningTime = formattedTime,
                morningRemarks = remarks.ifBlank { "በም/ር ተስተካክሎ ተመዝግቧል" }
            )
        } else {
            current.copy(
                exitStatus = status,
                exitTime = formattedTime,
                exitRemarks = remarks.ifBlank { "በም/ር ተስተካክሎ ተመዝግቧል" },
                afternoonStatus = status,
                afternoonTime = formattedTime,
                afternoonRemarks = remarks.ifBlank { "በም/ር ተስተካክሎ ተመዝግቧል" }
            )
        }
        _staffAttendanceRecords.value = map
    }

    fun updateStaffAttendance(id: Long, newStatus: String, remarks: String = "") {
        autoTickStaffAttendance(id, currentStaffAttendanceSession.value, newStatus)
    }

    // Login Form State
    val isAdminLoginMode = MutableStateFlow(false) // false = Phone mode (Teacher/Student/Staff), true = Email mode (Admin)
    val loginEmail = MutableStateFlow("admin@dilijent.edu.et")
    val loginPhone = MutableStateFlow("0911000000")
    val loginPassword = MutableStateFlow("1234")
    val loginErrorMessage = MutableStateFlow<String?>(null)
    val isAuthenticating = MutableStateFlow(false)

    // Admin Credentials & Password Manager Dialog
    val showAdminPasswordManagerDialog = MutableStateFlow(false)

    fun login(
        identifier: String,
        pass: String,
        isEmailLogin: Boolean = isAdminLoginMode.value,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            isAuthenticating.value = true
            loginErrorMessage.value = null
            var user: UserAccount? = if (isEmailLogin) {
                // Email login for System Admin
                val cleanEmail = identifier.trim()
                val found = repository.authenticateByEmail(cleanEmail, pass.trim())
                if (found == null && cleanEmail.equals("admin@dilijent.edu.et", ignoreCase = true) && pass.trim() == "admin123") {
                    val fallbackAdmin = UserAccount(
                        fullName = "ሲስተም አድሚን (እኔ)",
                        email = "admin@dilijent.edu.et",
                        phone = "0900000000",
                        password = "admin123",
                        role = "ሲስተም አድሚን (Super Admin)"
                    )
                    repository.registerUser(fallbackAdmin)
                    fallbackAdmin
                } else {
                    found
                }
            } else {
                // Phone login for Teachers, Students, Staff, and Principal
                repository.authenticate(identifier.trim(), pass.trim())
            }

            // Fallback for teacher/student/staff matching with initial PIN 1234
            if (!isEmailLogin && user == null && pass.trim() == "1234") {
                val matchingTeacher = allTeachers.value.firstOrNull { it.phone == identifier.trim() }
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
                    val matchingStudent = allStudents.value.firstOrNull { it.parentPhone == identifier.trim() || it.studentIdNumber.equals(identifier.trim(), ignoreCase = true) }
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
                        val matchingStaff = allStaff.value.firstOrNull { it.phone == identifier.trim() }
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
                loginErrorMessage.value = if (_isAmharic.value) {
                    if (isEmailLogin) "የአድሚን ኢሜል ወይም የይለፍ ቃል ልክ አይደለም! እባክዎ እንደገና ይሞክሩ።"
                    else "የስልክ ቁጥር ወይም የይለፍ ቃል ልክ አይደለም! እባክዎ እንደገና ይሞክሩ።"
                } else {
                    if (isEmailLogin) "Incorrect admin email or password! Please try again."
                    else "Incorrect phone number or password! Please try again."
                }
            }
        }
    }

    // Role-specific quick login for testing and instant access
    fun quickLoginAsAdmin() {
        isAdminLoginMode.value = true
        loginEmail.value = "admin@dilijent.edu.et"
        loginPassword.value = "admin123"
        login("admin@dilijent.edu.et", "admin123", isEmailLogin = true)
    }

    fun quickLoginAs(phone: String, pass: String = "1234") {
        isAdminLoginMode.value = false
        loginPhone.value = phone
        loginPassword.value = pass
        login(phone, pass, isEmailLogin = false)
    }

    fun quickLoginAsPrincipal() {
        quickLoginAs("0911000000", "1234")
    }

    fun updateUserPassword(userId: Long, newPass: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updatePassword(userId, newPass.trim())
            onDone()
        }
    }

    fun updateUserAccount(user: UserAccount, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateUser(user)
            onDone()
        }
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

    // Dynamic School Settings Management
    fun saveSchoolLogo(sourceUri: android.net.Uri): String {
        return try {
            val context = getApplication<Application>()
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val file = java.io.File(context.filesDir, "custom_school_logo.png")
            file.outputStream().use { output ->
                inputStream?.copyTo(output)
            }
            val fileUri = file.toURI().toString()
            _schoolLogoUri.value = fileUri
            prefs.edit().putString("school_logo_uri", fileUri).apply()
            fileUri
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun resetSchoolLogo() {
        _schoolLogoUri.value = ""
        prefs.edit().putString("school_logo_uri", "").apply()
        try {
            val file = java.io.File(getApplication<Application>().filesDir, "custom_school_logo.png")
            if (file.exists()) file.delete()
        } catch (_: Exception) {}
    }

    fun updateSchoolSettings(gradeLevel: String, nameAm: String, nameEn: String) {
        _schoolGradeLevel.value = gradeLevel
        _schoolName.value = nameAm
        _schoolNameEn.value = nameEn
        prefs.edit()
            .putString("school_grade_level", gradeLevel)
            .putString("school_name", nameAm)
            .putString("school_name_en", nameEn)
            .apply()
        selectedGradeFilter.value = "ሁሉም"
        subjectGradeFilter.value = "ሁሉም"
        showSchoolSettingsDialog.value = false
    }

    private suspend fun seedPrimarySchoolDataIfNeeded() {
        try {
            val existing = repository.allStudents.first()
            if (existing.none { isGradeInLevel(it.grade, "1_8") }) {
                val primaryStudents = listOf(
                    Student(
                        studentIdNumber = "ET-2017-101",
                        fullName = "ናታን ዳንኤል ዘለቀ",
                        gender = "ወንድ",
                        age = 13,
                        grade = "7ኛ ክፍል",
                        section = "A",
                        academicYear = "2017 ዓ.ም",
                        parentName = "ዳንኤል ዘለቀ",
                        parentPhone = "0911334455",
                        address = "አዲስ አበባ፣ የካ ክፍለ ከተማ ወረዳ 04",
                        status = "ንቁ"
                    ),
                    Student(
                        studentIdNumber = "ET-2017-102",
                        fullName = "ኤልሳቤጥ ተክሌ መንግስቱ",
                        gender = "ሴት",
                        age = 13,
                        grade = "7ኛ ክፍል",
                        section = "A",
                        academicYear = "2017 ዓ.ም",
                        parentName = "ተክሌ መንግስቱ",
                        parentPhone = "0912445566",
                        address = "አዲስ አበባ፣ ቦሌ ክፍለ ከተማ ወረዳ 05",
                        status = "ንቁ"
                    ),
                    Student(
                        studentIdNumber = "ET-2017-103",
                        fullName = "ዮሴፍ ከበደ ታደለ",
                        gender = "ወንድ",
                        age = 14,
                        grade = "8ኛ ክፍል",
                        section = "A",
                        academicYear = "2017 ዓ.ም",
                        parentName = "ከበደ ታደለ",
                        parentPhone = "0913556677",
                        address = "አዲስ አበባ፣ ቂርቆስ ክፍለ ከተማ ወረዳ 01",
                        status = "ንቁ"
                    ),
                    Student(
                        studentIdNumber = "ET-2017-104",
                        fullName = "ሃና ፍቃዱ ገብሬ",
                        gender = "ሴት",
                        age = 14,
                        grade = "8ኛ ክፍል",
                        section = "B",
                        academicYear = "2017 ዓ.ም",
                        parentName = "ፍቃዱ ገብሬ",
                        parentPhone = "0914667788",
                        address = "አዲስ አበባ፣ አራዳ ክፍለ ከተማ ወረዳ 02",
                        status = "ንቁ"
                    ),
                    Student(
                        studentIdNumber = "ET-2017-105",
                        fullName = "አቤኔዘር ዮሐንስ ሃይሌ",
                        gender = "ወንድ",
                        age = 11,
                        grade = "5ኛ ክፍል",
                        section = "A",
                        academicYear = "2017 ዓ.ም",
                        parentName = "ዮሐንስ ሃይሌ",
                        parentPhone = "0915778899",
                        address = "አዲስ አበባ፣ ንፋስ ስልክ ወረዳ 03",
                        status = "ንቁ"
                    ),
                    Student(
                        studentIdNumber = "ET-2017-106",
                        fullName = "ሩት ግርማ ወልዴ",
                        gender = "ሴት",
                        age = 12,
                        grade = "6ኛ ክፍል",
                        section = "A",
                        academicYear = "2017 ዓ.ም",
                        parentName = "ግርማ ወልዴ",
                        parentPhone = "0916889900",
                        address = "አዲስ አበባ፣ ጉለሌ ወረዳ 07",
                        status = "ንቁ"
                    )
                )
                primaryStudents.forEach { repository.insertStudent(it) }

                val primaryTeachers = listOf(
                    Teacher(
                        teacherCode = "T-101",
                        fullName = "አቶ ግርማቸው አሰፋ",
                        title = "አቶ",
                        gender = "ወንድ",
                        phone = "0911778899",
                        email = "girmachew.a@ethioschool.edu.et",
                        qualification = "ዲፕሎማ",
                        specialization = "አጠቃላይ ሳይንስ (General Science 1-8)",
                        department = "የመሰረታዊ ሳይንስ ክፍል",
                        isHomeroomTeacher = true,
                        homeroomClass = "7ኛ ክፍል-A"
                    ),
                    Teacher(
                        teacherCode = "T-102",
                        fullName = "ወ/ሮ ሰላማዊት አበራ",
                        title = "ወ/ሮ",
                        gender = "ሴት",
                        phone = "0922889900",
                        email = "selamawit.a@ethioschool.edu.et",
                        qualification = "ባችለር ዲግሪ (BEd)",
                        specialization = "ሂሳብ (Mathematics 1-8)",
                        department = "የሂሳብ ክፍል",
                        isHomeroomTeacher = true,
                        homeroomClass = "8ኛ ክፍል-A"
                    ),
                    Teacher(
                        teacherCode = "T-103",
                        fullName = "አቶ በየነ ኃይሉ",
                        title = "አቶ",
                        gender = "ወንድ",
                        phone = "0933990011",
                        email = "beyene.h@ethioschool.edu.et",
                        qualification = "ባችለር ዲግሪ (BA)",
                        specialization = "እንግሊዝኛ (English 1-8)",
                        department = "የቋንቋዎች ክፍል",
                        isHomeroomTeacher = false,
                        homeroomClass = ""
                    ),
                    Teacher(
                        teacherCode = "T-104",
                        fullName = "ወ/ሪት ትዕግስት ወርቄ",
                        title = "ወ/ሪት",
                        gender = "ሴት",
                        phone = "0944001122",
                        email = "tigist.w@ethioschool.edu.et",
                        qualification = "ዲፕሎማ",
                        specialization = "ህብረተሰብ ጥናት (Social Studies 1-8)",
                        department = "የማህበራዊ ሳይንስ ክፍል",
                        isHomeroomTeacher = true,
                        homeroomClass = "6ኛ ክፍል-A"
                    )
                )
                primaryTeachers.forEach { repository.insertTeacher(it) }

                val primarySubjects = listOf(
                    Subject(name = "አጠቃላይ ሳይንስ (General Science)", code = "GSCI-07", gradeLevel = "7ኛ ክፍል", periodsPerWeek = 4, passingScore = 50),
                    Subject(name = "አጠቃላይ ሳይንስ (General Science)", code = "GSCI-08", gradeLevel = "8ኛ ክፍል", periodsPerWeek = 4, passingScore = 50),
                    Subject(name = "አካባቢ ሳይንስ (Environmental Science)", code = "ENVS-05", gradeLevel = "5ኛ ክፍል", periodsPerWeek = 4, passingScore = 50),
                    Subject(name = "ህብረተሰብ ጥናት (Social Studies)", code = "SST-07", gradeLevel = "7ኛ ክፍል", periodsPerWeek = 3, passingScore = 50),
                    Subject(name = "ዜግነት ትምህርት (Citizenship)", code = "CTZ-08", gradeLevel = "8ኛ ክፍል", periodsPerWeek = 2, passingScore = 50),
                    Subject(name = "ሂሳብ (Mathematics)", code = "MTH-07", gradeLevel = "7ኛ ክፍል", periodsPerWeek = 5, passingScore = 50),
                    Subject(name = "እንግሊዝኛ (English)", code = "ENG-07", gradeLevel = "7ኛ ክፍል", periodsPerWeek = 5, passingScore = 50),
                    Subject(name = "አማርኛ ቋንቋ (Amharic)", code = "AMH-07", gradeLevel = "7ኛ ክፍል", periodsPerWeek = 4, passingScore = 50),
                    Subject(name = "የሙያ እና የቴክኒክ ትምህርት (CTE)", code = "CTE-08", gradeLevel = "8ኛ ክፍል", periodsPerWeek = 2, passingScore = 50)
                )
                primarySubjects.forEach { repository.insertSubject(it) }

                val primaryWorkloads = listOf(
                    TeacherWorkload(teacherId = 6, teacherName = "አቶ ግርማቸው አሰፋ", subjectId = 11, subjectName = "አጠቃላይ ሳይንስ (General Science)", grade = "7ኛ ክፍል", section = "A", periodsPerWeek = 4),
                    TeacherWorkload(teacherId = 7, teacherName = "ወ/ሮ ሰላማዊት አበራ", subjectId = 16, subjectName = "ሂሳብ (Mathematics)", grade = "8ኛ ክፍል", section = "A", periodsPerWeek = 5),
                    TeacherWorkload(teacherId = 8, teacherName = "አቶ በየነ ኃይሉ", subjectId = 17, subjectName = "እንግሊዝኛ (English)", grade = "7ኛ ክፍል", section = "A", periodsPerWeek = 5)
                )
                primaryWorkloads.forEach { repository.insertWorkload(it) }
            }
        } catch (_: Exception) {}
    }

    // Grades for selected student

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

    // ==========================================
    // EXCEL / CSV / PDF AUTO-IMPORT & EXPORT CENTER
    // ==========================================
    val showImportExportDialog = MutableStateFlow(false)
    val importExportTargetTab = MutableStateFlow("students") // "students" or "teachers"

    fun openImportExportDialog(target: String = "students") {
        importExportTargetTab.value = target
        showImportExportDialog.value = true
    }

    fun closeImportExportDialog() {
        showImportExportDialog.value = false
    }

    fun importStudentsFromCsv(csvText: String, onComplete: (Int, String) -> Unit) {
        viewModelScope.launch {
            try {
                val lines = csvText.lines().map { it.trim() }.filter { it.isNotBlank() }
                if (lines.isEmpty()) {
                    onComplete(0, "ምንም መረጃ በፋይሉ ውስጥ አልተገኘም!")
                    return@launch
                }
                val first = lines.first().lowercase()
                val hasHeader = first.contains("ስም") || first.contains("name") || first.contains("ክፍል") || first.contains("grade") || first.contains("phone") || first.contains("ስልክ")
                val dataLines = if (hasHeader) lines.drop(1) else lines

                var count = 0
                val baseCount = allStudents.value.size
                dataLines.forEachIndexed { idx, line ->
                    val cols = line.split(Regex("[,;\t]")).map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                    if (cols.isNotEmpty() && cols[0].isNotBlank()) {
                        val name = cols[0]
                        val gender = if (cols.size > 1 && (cols[1].contains("ሴት") || cols[1].equals("Female", ignoreCase = true) || cols[1].equals("F", ignoreCase = true))) "ሴት" else "ወንድ"
                        val grade = if (cols.size > 2 && cols[2].isNotBlank()) cols[2] else "12ኛ ክፍል"
                        val phone = if (cols.size > 3 && cols[3].isNotBlank()) cols[3] else "+251 911 000000"
                        val section = if (cols.size > 4 && cols[4].isNotBlank()) cols[4] else "A"
                        val studentId = "ET-2017-${String.format("%03d", baseCount + idx + 1)}"

                        val student = Student(
                            studentIdNumber = studentId,
                            fullName = name,
                            gender = gender,
                            age = 17,
                            grade = grade,
                            section = section,
                            academicYear = "2017 ዓ.ም",
                            parentName = "የተማሪ አሳዳጊ",
                            parentPhone = phone,
                            address = "አዲስ አበባ",
                            status = "ንቁ"
                        )
                        val id = repository.insertStudent(student)
                        firestoreRepository.saveStudent(authUid, student.copy(id = id))
                        count++
                    }
                }
                onComplete(count, "$count አዳዲስ ተማሪዎች ወደ ሲስተሙ ተመዝግበዋል!")
            } catch (e: Exception) {
                onComplete(0, "ስህተት ተከስቷል: ${e.message}")
            }
        }
    }

    fun importTeachersFromCsv(csvText: String, onComplete: (Int, String) -> Unit) {
        viewModelScope.launch {
            try {
                val lines = csvText.lines().map { it.trim() }.filter { it.isNotBlank() }
                if (lines.isEmpty()) {
                    onComplete(0, "ምንም መረጃ በፋይሉ ውስጥ አልተገኘም!")
                    return@launch
                }
                val first = lines.first().lowercase()
                val hasHeader = first.contains("ስም") || first.contains("name") || first.contains("role") || first.contains("phone") || first.contains("ስልክ")
                val dataLines = if (hasHeader) lines.drop(1) else lines

                var count = 0
                val baseCount = allTeachers.value.size
                dataLines.forEachIndexed { idx, line ->
                    val cols = line.split(Regex("[,;\t]")).map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                    if (cols.isNotEmpty() && cols[0].isNotBlank()) {
                        val name = cols[0]
                        val title = if (name.startsWith("መምህርት") || name.startsWith("ወ/ሮ") || name.startsWith("ወ/ሪት")) "ወ/ሮ" else "አቶ"
                        val gender = if (title == "ወ/ሮ" || (cols.size > 1 && cols[1].contains("ሴት"))) "ሴት" else "ወንድ"
                        val dept = if (cols.size > 1 && cols[1].isNotBlank()) cols[1] else "አጠቃላይ የትምህርት ክፍል"
                        val phone = if (cols.size > 2 && cols[2].isNotBlank()) cols[2] else "+251 911 000000"
                        val qual = if (cols.size > 3 && cols[3].isNotBlank()) cols[3] else "ባችለር ዲግሪ"
                        val teacherCode = "T-${String.format("%03d", 100 + baseCount + idx + 1)}"

                        val teacher = Teacher(
                            teacherCode = teacherCode,
                            fullName = name,
                            title = title,
                            gender = gender,
                            phone = phone,
                            qualification = qual,
                            specialization = dept,
                            department = dept,
                            status = "ንቁ"
                        )
                        val id = repository.insertTeacher(teacher)
                        firestoreRepository.saveTeacher(authUid, teacher.copy(id = id))
                        count++
                    }
                }
                onComplete(count, "$count አዳዲስ መምህራን ወደ ሲስተሙ ተመዝግበዋል!")
            } catch (e: Exception) {
                onComplete(0, "ስህተት ተከስቷል: ${e.message}")
            }
        }
    }

    fun exportStudentsToCsv(): String {
        val students = allStudents.value
        val sb = StringBuilder()
        sb.append("መለያ ቁጥር,ሙሉ ስም,ጾታ,ክፍል,ክፍልፋይ,የአሳዳጊ ስልክ,ሁኔታ\n")
        students.forEach { s ->
            sb.append("\"${s.studentIdNumber}\",\"${s.fullName}\",\"${s.gender}\",\"${s.grade}\",\"${s.section}\",\"${s.parentPhone}\",\"${s.status}\"\n")
        }
        return sb.toString()
    }

    fun exportTeachersToCsv(): String {
        val teachers = allTeachers.value
        val sb = StringBuilder()
        sb.append("የመምህር መለያ,ሙሉ ስም,ጾታ,ዲፓርትመንት,ስልክ ቁጥር,የትምህርት ደረጃ,ሁኔታ\n")
        teachers.forEach { t ->
            sb.append("\"${t.teacherCode}\",\"${t.fullName}\",\"${t.gender}\",\"${t.department}\",\"${t.phone}\",\"${t.qualification}\",\"${t.status}\"\n")
        }
        return sb.toString()
    }
}
