package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.StaffAttendanceItem
import com.example.data.model.StaffAttendanceSession
import com.example.data.model.StaffRoleSchedule
import com.example.data.model.Student
import com.example.data.model.Teacher
import com.example.data.model.TeacherWorkload
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FilterChipRow
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.EthioRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

enum class AttendanceSubMode {
    STUDENTS,
    STAFF
}

@Composable
fun AttendanceScreen(
    students: List<Student>,
    availableGrades: List<String>,
    sectionOptions: List<String>,
    isAmharic: Boolean,
    isAdmin: Boolean = true,
    isVicePrincipal: Boolean = false,
    canAccessStaffAttendance: Boolean = false,
    isTeacher: Boolean = false,
    isStudent: Boolean = false,
    currentTeacher: Teacher? = null,
    currentStudent: Student? = null,
    teacherWorkloads: List<TeacherWorkload> = emptyList(),
    staffAttendanceList: List<StaffAttendanceItem> = emptyList(),
    staffRoleSchedules: List<StaffRoleSchedule> = emptyList(),
    activeStaffSession: StaffAttendanceSession = StaffAttendanceSession.CHECK_IN,
    schoolNameAm: String = "ዲሊጀንት ትምህርት ቤት",
    schoolNameEn: String = "Dilijent School",
    schoolLogoUri: String = "",
    academicYear: String = "2017 ዓ.ም",
    allAttendanceRecords: List<AttendanceRecord> = emptyList(),
    onSelectStaffSession: (StaffAttendanceSession) -> Unit = {},
    onRecordAttendance: (AttendanceRecord) -> Unit,
    onRecordStaffAttendance: (Long, String, String) -> Unit = { _, _, _ -> },
    onAutoTickStaffAttendance: (Long, StaffAttendanceSession, String?) -> Unit = { _, _, _ -> },
    onSaveStaffAttendanceManual: (Long, StaffAttendanceSession, String, String, String) -> Unit = { _, _, _, _, _ -> },
    onUpdateRoleSchedule: (String, String, String, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
    onResetRoleSchedules: () -> Unit = {},
    onDownloadAttendanceReport: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    // Mode switcher: Default to STUDENTS unless user explicitly switches to STAFF
    var activeSubMode by remember { mutableStateOf(AttendanceSubMode.STUDENTS) }

    // Automatic Parent SMS Notification on Absence State
    var autoSmsEnabled by remember { mutableStateOf(true) }
    var absentStudentForSms by remember { mutableStateOf<Student?>(null) }
    var smsCustomMessage by remember { mutableStateOf("") }

    // Dialog state for manually editing staff attendance time & status
    var editingStaffItem by remember { mutableStateOf<StaffAttendanceItem?>(null) }
    var editSession by remember { mutableStateOf(activeStaffSession) }
    var editTime by remember { mutableStateOf("") }
    var editStatus by remember { mutableStateOf("ተገኝቷል") }
    var editRemarks by remember { mutableStateOf("") }

    // Dialog state for configuring role schedules
    var showRoleScheduleDialog by remember { mutableStateOf(false) }

    val assignedGrades = remember(teacherWorkloads) {
        val list = teacherWorkloads.map { it.grade }.distinct()
        if (list.isEmpty() && currentTeacher?.isHomeroomTeacher == true && currentTeacher.homeroomClass.isNotBlank()) {
            listOf(currentTeacher.homeroomClass.split("-").firstOrNull() ?: "9ኛ ክፍል")
        } else list
    }

    val usableGrades = remember(isTeacher, assignedGrades, availableGrades) {
        if (isTeacher && assignedGrades.isNotEmpty()) assignedGrades
        else availableGrades.filter { it != "ሁሉም" }
    }

    var selectedGrade by remember(usableGrades) {
        mutableStateOf(usableGrades.firstOrNull() ?: "9ኛ ክፍል")
    }

    val assignedSections = remember(teacherWorkloads, selectedGrade) {
        val list = teacherWorkloads.filter { it.grade == selectedGrade }.map { it.section }.distinct()
        if (list.isEmpty()) sectionOptions else list
    }

    val usableSections = remember(isTeacher, assignedSections, sectionOptions) {
        if (isTeacher && assignedSections.isNotEmpty()) assignedSections
        else sectionOptions
    }

    var selectedSection by remember(usableSections) {
        mutableStateOf(usableSections.firstOrNull() ?: "A")
    }
    val currentDate = "2017-02-16" // Ethiopian Academic Year Date

    // Local attendance statuses map for students: studentId -> status
    val attendanceStatusMap = remember {
        mutableStateMapOf<Long, String>()
    }

    val classStudents = remember(students, isStudent, currentStudent, selectedGrade, selectedSection) {
        if (isStudent && currentStudent != null) {
            listOf(currentStudent)
        } else {
            students.filter { it.grade == selectedGrade && it.section == selectedSection }
        }
    }

    // Initialize unrecorded students as "ተገኝቷል" (Present)
    classStudents.forEach { student ->
        if (!attendanceStatusMap.containsKey(student.id)) {
            attendanceStatusMap[student.id] = "ተገኝቷል"
        }
    }

    val studentPresentCount = classStudents.count { attendanceStatusMap[it.id] == "ተገኝቷል" }
    val studentAbsentCount = classStudents.count { attendanceStatusMap[it.id] == "ቀርቷል" }
    val studentExcusedCount = classStudents.count { attendanceStatusMap[it.id] == "በፍቃድ" }

    // Staff Attendance filters
    var staffCategoryFilter by remember { mutableStateOf("ሁሉም") }
    var staffSearchQuery by remember { mutableStateOf("") }

    val filteredStaffAttendance = remember(staffAttendanceList, staffCategoryFilter, staffSearchQuery) {
        staffAttendanceList.filter { item ->
            val matchesCategory = when (staffCategoryFilter) {
                "መምህራን" -> item.isTeacher
                "አስተዳደር" -> !item.isTeacher && (item.department.contains("አስተዳደር") || item.department.contains("ፋይናንስ"))
                "አገልግሎትና ደህንነት" -> !item.isTeacher && item.department.contains("አገልግሎት")
                else -> true
            }
            val matchesSearch = staffSearchQuery.isBlank() ||
                    item.name.contains(staffSearchQuery, ignoreCase = true) ||
                    item.role.contains(staffSearchQuery, ignoreCase = true) ||
                    item.department.contains(staffSearchQuery, ignoreCase = true) ||
                    item.phone.contains(staffSearchQuery)
            matchesCategory && matchesSearch
        }
    }

    val staffPresentCount = filteredStaffAttendance.count { it.status == "ተገኝቷል" }
    val staffAbsentCount = filteredStaffAttendance.count { it.status == "ቀርቷል" }
    val staffLateCount = filteredStaffAttendance.count { it.status == "አርፍዷል" }
    val staffLeaveCount = filteredStaffAttendance.count { it.status == "በፍቃድ" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("attendance_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Executive Privilege Switcher: ONLY VISIBLE TO VICE PRINCIPAL & ADMIN
        if (canAccessStaffAttendance) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Option 1: Student Attendance
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (activeSubMode == AttendanceSubMode.STUDENTS) NavyPrimary else Color.Transparent,
                            contentColor = if (activeSubMode == AttendanceSubMode.STUDENTS) Color.White else NavyPrimary,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { activeSubMode = AttendanceSubMode.STUDENTS }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAmharic) "👨‍🎓 የተማሪዎች ክትትል" else "Student Attendance",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Option 2: School Staff Attendance (Restricted to Vice Principal & Admin)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (activeSubMode == AttendanceSubMode.STAFF) GoldAccent else Color.Transparent,
                            contentColor = if (activeSubMode == AttendanceSubMode.STAFF) Color(0xFF78350F) else Color(0xFF92400E),
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { activeSubMode = AttendanceSubMode.STAFF }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAmharic) "👩‍🏫 የሰራተኞች ክትትል (ምክትል & አድሚን 🔒)" else "Staff Attendance (Admin/VP 🔒)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // VIEW 1: STUDENT ATTENDANCE
        // =========================================================================
        if (activeSubMode == AttendanceSubMode.STUDENTS) {
            // Role Security Banner for Students View
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isStudent -> Color(0xFF0284C7).copy(alpha = 0.10f)
                        isTeacher -> Color(0xFF6D28D9).copy(alpha = 0.10f)
                        else -> NavyPrimary.copy(alpha = 0.08f)
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            isStudent -> Icons.Default.School
                            isTeacher -> Icons.Default.Lock
                            else -> Icons.Default.Security
                        },
                        contentDescription = null,
                        tint = when {
                            isStudent -> Color(0xFF0284C7)
                            isTeacher -> Color(0xFF6D28D9)
                            else -> NavyPrimary
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = when {
                                isStudent -> if (isAmharic)
                                    "🎓 የተማሪ ${currentStudent?.fullName ?: ""} የዕለት ክትትል"
                                else
                                    "🎓 Student Attendance Record"
                                isTeacher -> if (isAmharic)
                                    "🔒 የመምህር ${currentTeacher?.fullName ?: ""} የክትትል ገፅ፦ የተመደቡ ክፍሎች ብቻ"
                                else
                                    "🔒 Teacher ${currentTeacher?.fullName ?: ""} Attendance: Assigned Sections"
                                isVicePrincipal -> if (isAmharic)
                                    "🎓 የምክትል ርዕሰ መምህር የክትትል ቁጥጥር (Vice Principal Oversight)"
                                else
                                    "🎓 Vice Principal Academic Attendance Oversight"
                                else -> if (isAmharic)
                                    "👑 የሲስተም አድሚን አጠቃላይ ክትትል (Admin Attendance Oversight)"
                                else
                                    "👑 Executive Attendance Administration"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = when {
                                isStudent -> Color(0xFF0369A1)
                                isTeacher -> Color(0xFF5B21B6)
                                else -> NavyPrimary
                            }
                        )
                        Text(
                            text = when {
                                isStudent -> if (isAmharic)
                                    "የእርስዎን የክፍል መገኘት እና የፍቃድ ሁኔታ ይመልከቱ።"
                                else
                                    "Personal student presence record and excused absences."
                                isTeacher -> if (isAmharic)
                                    "የተመደቡልዎትን ክፍሎች ብቻ አቴንዳንስ መውሰድ ይችላሉ።"
                                else
                                    "You can only record attendance for sections assigned to your teaching workload."
                                isVicePrincipal -> if (isAmharic)
                                    "የተማሪዎችንም ሆነ የሰራተኞችን አቴንዳንስ የመከታተል እና የመቆጣጠር ሙሉ ስልጣን አልዎት።"
                                else
                                    "Authorized to oversee both student and staff attendance."
                                else -> if (isAmharic)
                                    "ሁሉንም የትምህርት ቤት ክፍሎች እና ተማሪዎች የመከታተል ሙሉ ስልጣን አልዎት።"
                                else
                                    "Full administrative oversight across all classes."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Date and Info Header
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isAmharic) "የዕለት የተማሪዎች ክትትል" else "Daily Student Attendance",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (isAmharic) "ቀን: $currentDate ዓ.ም" else "Date: $currentDate",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EthioGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isAmharic) "ወቅታዊ" else "Live",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EthioGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Download Attendance Report (Monthly PDF) Bar
                Surface(
                    shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp),
                    color = NavyPrimary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isAmharic) "📄 ወርሃዊ የተማሪዎች አቴንዳንስ ማጠቃለያ" else "📄 Monthly Attendance Summary",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Text(
                                text = if (isAmharic) "ተደጋጋሚ ቀሪዎችን (>3 ቀናት) በቀይ ቀለም ያደምቃል" else "Highlights frequent absentees (>3 days)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                if (onDownloadAttendanceReport != null) {
                                    onDownloadAttendanceReport(selectedGrade, selectedSection)
                                } else {
                                    com.example.ui.components.AttendancePdfGenerator.generateAndShareMonthlyReport(
                                        context = context,
                                        schoolNameAm = schoolNameAm,
                                        schoolNameEn = schoolNameEn,
                                        academicYear = academicYear,
                                        selectedGrade = selectedGrade,
                                        selectedSection = selectedSection,
                                        students = students,
                                        attendanceRecords = allAttendanceRecords,
                                        isAmharic = isAmharic
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EthioGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_download_attendance_report")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAmharic) "📥 ሪፖርት አውርድ (PDF)" else "📥 Download PDF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Parent SMS Auto-Notification Banner
                Surface(
                    shape = RoundedCornerShape(0.dp),
                    color = if (autoSmsEnabled) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📱",
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (isAmharic) "የወላጅ ፈጣን የኤስኤምኤስ (SMS) መልእክት" else "Parent Instant SMS Alerts",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (autoSmsEnabled) Color(0xFF1D4ED8) else Color(0xFF475569)
                                )
                                Text(
                                    text = if (isAmharic)
                                        (if (autoSmsEnabled) "ተማሪ ሲቀር ለወላጅ ወዲያውኑ የSMS ማሳወቂያ ይላካል (በርቷል)" else "የSMS ማሳወቂያ ጠፍቷል")
                                    else
                                        (if (autoSmsEnabled) "Auto SMS is triggered to parent when marked absent (ON)" else "SMS alerts disabled"),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        androidx.compose.material3.Switch(
                            checked = autoSmsEnabled,
                            onCheckedChange = { autoSmsEnabled = it },
                            modifier = Modifier.testTag("switch_auto_sms")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grade selector
            Text(
                text = if (isAmharic) "ክፍል ምረጥ:" else "Select Grade:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilterChipRow(
                options = availableGrades.filter { it != "ሁሉም" },
                selectedOption = selectedGrade,
                onSelectOption = { selectedGrade = it },
                testTagPrefix = "att_grade"
            )

            // Section selector
            FilterChipRow(
                options = usableSections,
                selectedOption = selectedSection,
                onSelectOption = { selectedSection = it },
                testTagPrefix = "att_section"
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Roll Call Summary Bar
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SummaryBadge(
                    label = if (isAmharic) "የተገኙ" else "Present",
                    count = studentPresentCount,
                    color = EthioGreen,
                    modifier = Modifier.weight(1f)
                )
                SummaryBadge(
                    label = if (isAmharic) "የቀሩ" else "Absent",
                    count = studentAbsentCount,
                    color = EthioRed,
                    modifier = Modifier.weight(1f)
                )
                SummaryBadge(
                    label = if (isAmharic) "በፍቃድ" else "Excused",
                    count = studentExcusedCount,
                    color = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (classStudents.isEmpty()) {
                EmptyStateCard(
                    message = if (isAmharic) "በዚህ ክፍል እና ሴክሽን የተመዘገበ ተማሪ የለም" else "No students enrolled in this section"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(classStudents, key = { it.id }) { student ->
                        val currentStatus = attendanceStatusMap[student.id] ?: "ተገኝቷል"

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${student.studentIdNumber} • ${student.grade} - ${student.section}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Quick status buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    AttendanceStatusButton(
                                        label = if (isAmharic) "ተገኝቷል" else "P",
                                        isSelected = currentStatus == "ተገኝቷል",
                                        color = EthioGreen,
                                        onClick = {
                                            attendanceStatusMap[student.id] = "ተገኝቷል"
                                            onRecordAttendance(
                                                AttendanceRecord(
                                                    studentId = student.id,
                                                    studentName = student.fullName,
                                                    grade = student.grade,
                                                    section = student.section,
                                                    date = currentDate,
                                                    status = "ተገኝቷል"
                                                )
                                            )
                                        }
                                    )
                                    AttendanceStatusButton(
                                        label = if (isAmharic) "ቀርቷል" else "A",
                                        isSelected = currentStatus == "ቀርቷል",
                                        color = EthioRed,
                                        onClick = {
                                            attendanceStatusMap[student.id] = "ቀርቷል"
                                            onRecordAttendance(
                                                AttendanceRecord(
                                                    studentId = student.id,
                                                    studentName = student.fullName,
                                                    grade = student.grade,
                                                    section = student.section,
                                                    date = currentDate,
                                                    status = "ቀርቷል"
                                                )
                                            )
                                            // Automatically trigger SMS notification to parent
                                            if (autoSmsEnabled) {
                                                if (student.parentPhone.isNotBlank()) {
                                                    smsCustomMessage = if (isAmharic)
                                                        "ውድ ወላጅ/አሳዳጊ፡ ልጆ ${student.fullName} ዛሬ በቀን $currentDate ዓ.ም በ${schoolNameAm} ክፍል ውስጥ አልተገኘም (ቀርቷል)። ለተጨማሪ መረጃ ትምህርት ቤቱን ያነጋግሩ።"
                                                    else
                                                        "Dear Parent/Guardian: Your child ${student.fullName} was marked absent from ${schoolNameEn} on $currentDate. Please contact the school."
                                                    absentStudentForSms = student
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        if (isAmharic) "ለተማሪ ${student.fullName} የተመዘገበ የወላጅ ስልክ የለም" else "No parent phone number registered for ${student.fullName}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    )
                                    AttendanceStatusButton(
                                        label = if (isAmharic) "በፍቃድ" else "E",
                                        isSelected = currentStatus == "በፍቃድ",
                                        color = GoldAccent,
                                        onClick = {
                                            attendanceStatusMap[student.id] = "በፍቃድ"
                                            onRecordAttendance(
                                                AttendanceRecord(
                                                    studentId = student.id,
                                                    studentName = student.fullName,
                                                    grade = student.grade,
                                                    section = student.section,
                                                    date = currentDate,
                                                    status = "በፍቃድ"
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        } else {
            // =========================================================================
            // VIEW 2: STAFF & TEACHERS ATTENDANCE (Strictly Vice Principal & Admin Only!)
            // =========================================================================
            if (!canAccessStaffAttendance) {
                // Strict Gate: Unauthorized attempt
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isAmharic) "⛔ የተከለከለ ገፅ!" else "⛔ Access Restricted!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = if (isAmharic)
                                "የትምህርት ቤቱ ሰራተኞች አቴንዳንስ በምክትል ርዕሰ መምህር እና በሲስተም አድሚን ገፅ ብቻ የሚገኝ ነው።"
                            else
                                "School staff attendance is strictly restricted to the Vice Principal and System Administrator.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            } else {
                // Authorized Staff Attendance Management Dashboard
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GoldAccent)
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF78350F), modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isAmharic) "የመምህራን እና ሰራተኞች ክትትል" else "Staff & Teachers Attendance",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GoldAccent
                                ) {
                                    Text(
                                        text = if (isAmharic) "ምክትል & አድሚን ብቻ 🔒" else "Admin / VP Only",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF78350F),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isAmharic) "ቀን: $currentDate ዓ.ም • በምክትል ርዕሰ መምህር እና በአድሚን የሚመራ" else "Date: $currentDate • Executive Oversight",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Bar for Staff Attendance: 30-Day PDF Report Download with School Logo
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isAmharic) "📥 የሰራተኞች የ30 ቀናት አቴንዳንስ ሪፖርት" else "📥 Staff 30-Day Attendance Report",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = NavyPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = EthioGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "PDF",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EthioGreen,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isAmharic) "የት/ቤቱ ሎጎ (Logo)፣ የ30 ቀናት ክትትልና የፊርማ ማረጋገጫ የያዘ" else "With school logo header, 30-day KPIs & signatures",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Button(
                            onClick = {
                                com.example.ui.components.AttendancePdfGenerator.generateAndShareStaff30DaysReport(
                                    context = context,
                                    schoolNameAm = schoolNameAm,
                                    schoolNameEn = schoolNameEn,
                                    schoolLogoUri = schoolLogoUri,
                                    academicYear = academicYear,
                                    staffList = staffAttendanceList,
                                    isAmharic = isAmharic
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EthioGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isAmharic) "📄 PDF አውርድ" else "📄 Download PDF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Two Attendance Sessions Switcher: የመግቢያ (Check-In) & የመውጫ (Check-Out)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isAmharic) "የዕለቱ የክትትል ክፍለ-ጊዜ ይምረጡ (Shift):" else "Select Shift Session:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )

                            if (isAdmin || isVicePrincipal) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GoldAccent.copy(alpha = 0.25f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { showRoleScheduleDialog = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Tune,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp),
                                            tint = Color(0xFF78350F)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isAmharic) "⚙️ የሰዓት ገደብ አዋቅር" else "⚙️ Shift Schedules",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF78350F)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val isEntryActive = activeStaffSession == StaffAttendanceSession.CHECK_IN
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isEntryActive) NavyPrimary else Color(0xFFF1F5F9),
                                contentColor = if (isEntryActive) Color.White else Color(0xFF334155),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelectStaffSession(StaffAttendanceSession.CHECK_IN) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.WbSunny,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isEntryActive) GoldAccent else Color(0xFFD97706)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = if (isAmharic) "🚪 የመግቢያ አቴንዳንስ" else "Check-In (Arrival)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = if (isAmharic) "እንደየሰራተኛው የስራ ሰዓት" else "By Staff Shift Schedule",
                                            fontSize = 10.sp,
                                            color = if (isEntryActive) GoldAccent else Color(0xFF64748B)
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!isEntryActive) NavyPrimary else Color(0xFFF1F5F9),
                                contentColor = if (!isEntryActive) Color.White else Color(0xFF334155),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelectStaffSession(StaffAttendanceSession.CHECK_OUT) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.NightsStay,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (!isEntryActive) GoldAccent else Color(0xFF2563EB)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = if (isAmharic) "🚶‍♂️ የመውጫ አቴንዳንስ" else "Check-Out (Departure)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = if (isAmharic) "እንደየሰራተኛው የስራ ሰዓት" else "By Staff Shift Schedule",
                                            fontSize = 10.sp,
                                            color = if (!isEntryActive) GoldAccent else Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }

                        // Auto-time notice banner
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (activeStaffSession == StaffAttendanceSession.CHECK_IN) Color(0xFFFEF3C7) else Color(0xFFEFF6FF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (activeStaffSession == StaffAttendanceSession.CHECK_IN) Color(0xFF92400E) else Color(0xFF1E40AF)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (activeStaffSession == StaffAttendanceSession.CHECK_IN)
                                        "⚡ የመግቢያ አቴንዳንስ፦ ም/ር ወይም አድሚን ቲክ ሲያደርግ የአሁን ሰዓት ይመዘገባል • ከገደቡ ካለፈ 'አርፍዷል' ይባላል"
                                    else
                                        "⚡ የመውጫ አቴንዳንስ፦ ም/ር ወይም አድሚን ቲክ ሲያደርግ የአሁን ሰዓት ይመዘገባል • ቀድሞ/ዘግይቶ መውጣት በእራሱ ይወሰናል",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (activeStaffSession == StaffAttendanceSession.CHECK_IN) Color(0xFF92400E) else Color(0xFF1E40AF)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Staff Attendance Statistics Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SummaryBadge(label = if (isAmharic) "ተገኝቷል" else "Present", count = staffPresentCount, color = EthioGreen, modifier = Modifier.weight(1f))
                    SummaryBadge(label = if (isAmharic) "ቀርቷል" else "Absent", count = staffAbsentCount, color = EthioRed, modifier = Modifier.weight(1f))
                    SummaryBadge(label = if (isAmharic) "አርፍዷል" else "Late", count = staffLateCount, color = Color(0xFFD97706), modifier = Modifier.weight(1f))
                    SummaryBadge(label = if (isAmharic) "በፍቃድ" else "Leave", count = staffLeaveCount, color = Color(0xFF2563EB), modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips (መምህራን፣ አስተዳደር፣ አገልግሎትና ደህንነት)
                FilterChipRow(
                    options = listOf("ሁሉም", "መምህራን", "አስተዳደር", "አገልግሎትና ደህንነት"),
                    selectedOption = staffCategoryFilter,
                    onSelectOption = { staffCategoryFilter = it },
                    testTagPrefix = "staff_att_cat"
                )

                // Search field for Staff
                OutlinedTextField(
                    value = staffSearchQuery,
                    onValueChange = { staffSearchQuery = it },
                    placeholder = { Text(if (isAmharic) "በስም፣ በስራ መደብ ወይም ስልክ ፈልግ..." else "Search staff by name, role...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                if (filteredStaffAttendance.isEmpty()) {
                    EmptyStateCard(
                        message = if (isAmharic) "በዚህ ምድብ ውስጥ የተገኘ ሰራተኛ/መምህር የለም" else "No staff found in this category"
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredStaffAttendance, key = { it.id }) { staffItem ->
                            val isEntry = activeStaffSession == StaffAttendanceSession.CHECK_IN
                            val currentStatus = if (isEntry) staffItem.entryStatus else staffItem.exitStatus
                            val currentTime = if (isEntry) staffItem.entryTime else staffItem.exitTime
                            val currentRemarks = if (isEntry) staffItem.entryRemarks else staffItem.exitRemarks

                            // Role schedule for this staff category
                            val roleSched = staffRoleSchedules.firstOrNull { it.roleCategory == staffItem.roleCategory }

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(if (staffItem.isTeacher) Color(0xFFEDE9FE) else Color(0xFFFEF3C7))
                                        ) {
                                            Text(
                                                text = staffItem.name.take(1),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (staffItem.isTeacher) Color(0xFF6D28D9) else Color(0xFFB45309)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = staffItem.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFF1F5F9)
                                                ) {
                                                    Text(
                                                        text = staffItem.roleCategory,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF475569),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "${staffItem.role} • ${staffItem.department}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Status Pill for current active session
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (currentStatus) {
                                                "ተገኝቷል" -> EthioGreen.copy(alpha = 0.15f)
                                                "ቀርቷል" -> EthioRed.copy(alpha = 0.15f)
                                                "አርፍዷል" -> Color(0xFFD97706).copy(alpha = 0.15f)
                                                "ቀድሞ ወጥቷል" -> Color(0xFFD97706).copy(alpha = 0.15f)
                                                "ዘግይቶ ወጥቷል" -> Color(0xFF2563EB).copy(alpha = 0.15f)
                                                else -> Color(0xFF2563EB).copy(alpha = 0.15f)
                                            }
                                        ) {
                                            Text(
                                                text = currentStatus,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = when (currentStatus) {
                                                    "ተገኝቷል" -> EthioGreen
                                                    "ቀርቷል" -> EthioRed
                                                    "አርፍዷል" -> Color(0xFFD97706)
                                                    "ቀድሞ ወጥቷል" -> Color(0xFFD97706)
                                                    "ዘግይቶ ወጥቷል" -> Color(0xFF2563EB)
                                                    else -> Color(0xFF2563EB)
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    // Role Schedule Time Window Badge
                                    if (roleSched != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFF8FAFC),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "⏱ የ${staffItem.roleCategory} ሰዓት ገደብ፦ መግቢያ፡ ${roleSched.entryStart} - ${roleSched.entryEnd} | መውጫ፡ ${roleSched.exitStart} - ${roleSched.exitEnd}",
                                                fontSize = 9.sp,
                                                color = Color(0xFF64748B),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Display Both Sessions History Bar (መግቢያ & መውጫ)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFF8FAFC))
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🚪 መግቢያ: ${staffItem.entryTime} (${staffItem.entryStatus})",
                                            fontSize = 10.sp,
                                            fontWeight = if (isEntry) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isEntry) NavyPrimary else Color(0xFF64748B)
                                        )
                                        Text(
                                            text = "🚶‍♂️ መውጫ: ${staffItem.exitTime} (${staffItem.exitStatus})",
                                            fontSize = 10.sp,
                                            fontWeight = if (!isEntry) FontWeight.Bold else FontWeight.Normal,
                                            color = if (!isEntry) NavyPrimary else Color(0xFF64748B)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Check-in / Check-out Time & Remarks
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = currentTime,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "• $currentRemarks",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }

                                        // Edit Button for manual correction
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFE0F2FE),
                                            contentColor = Color(0xFF0369A1),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable {
                                                    editingStaffItem = staffItem
                                                    editSession = activeStaffSession
                                                    editTime = if (activeStaffSession == StaffAttendanceSession.CHECK_IN) staffItem.entryTime else staffItem.exitTime
                                                    editStatus = if (activeStaffSession == StaffAttendanceSession.CHECK_IN) staffItem.entryStatus else staffItem.exitStatus
                                                    editRemarks = if (activeStaffSession == StaffAttendanceSession.CHECK_IN) staffItem.entryRemarks else staffItem.exitRemarks
                                                }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(11.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = if (isAmharic) "ሰዓት አርም" else "Edit Time",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action buttons for Vice Principal & Admin
                                    // Clicking automatically records exact current time and checks role limits
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (isEntry) {
                                            // CHECK-IN BUTTONS
                                            AttendanceStatusButton(
                                                label = "✓ በሰዓቱ ገብቷል",
                                                isSelected = currentStatus == "ተገኝቷል",
                                                color = EthioGreen,
                                                onClick = {
                                                    onAutoTickStaffAttendance(staffItem.id, activeStaffSession, "ተገኝቷል")
                                                }
                                            )
                                            AttendanceStatusButton(
                                                label = "⏱ አርፍዷል",
                                                isSelected = currentStatus == "አርፍዷል",
                                                color = Color(0xFFD97706),
                                                onClick = {
                                                    onAutoTickStaffAttendance(staffItem.id, activeStaffSession, "አርፍዷል")
                                                }
                                            )
                                            AttendanceStatusButton(
                                                label = "✕ ቀርቷል",
                                                isSelected = currentStatus == "ቀርቷል",
                                                color = EthioRed,
                                                onClick = {
                                                    onAutoTickStaffAttendance(staffItem.id, activeStaffSession, "ቀርቷል")
                                                }
                                            )
                                            AttendanceStatusButton(
                                                label = "📝 ፍቃድ",
                                                isSelected = currentStatus == "በፍቃድ",
                                                color = Color(0xFF2563EB),
                                                onClick = {
                                                    onAutoTickStaffAttendance(staffItem.id, activeStaffSession, "በፍቃድ")
                                                }
                                            )
                                        } else {
                                            // CHECK-OUT BUTTONS
                                            AttendanceStatusButton(
                                                label = "✓ በሰዓቱ ወጥቷል",
                                                isSelected = currentStatus == "ተገኝቷል",
                                                color = EthioGreen,
                                                onClick = {
                                                    onAutoTickStaffAttendance(staffItem.id, activeStaffSession, "ተገኝቷል")
                                                }
                                            )
                                            AttendanceStatusButton(
                                                label = "🏃‍♂️ ቀድሞ ወጥቷል",
                                                isSelected = currentStatus == "ቀድሞ ወጥቷል",
                                                color = Color(0xFFD97706),
                                                onClick = {
                                                    onAutoTickStaffAttendance(staffItem.id, activeStaffSession, "ቀድሞ ወጥቷል")
                                                }
                                            )
                                            AttendanceStatusButton(
                                                label = "⌛ ዘግይቶ ወጥቷል",
                                                isSelected = currentStatus == "ዘግይቶ ወጥቷል",
                                                color = Color(0xFF2563EB),
                                                onClick = {
                                                    onAutoTickStaffAttendance(staffItem.id, activeStaffSession, "ዘግይቶ ወጥቷል")
                                                }
                                            )
                                            AttendanceStatusButton(
                                                label = "✕ ቀርቷል",
                                                isSelected = currentStatus == "ቀርቷል",
                                                color = EthioRed,
                                                onClick = {
                                                    onAutoTickStaffAttendance(staffItem.id, activeStaffSession, "ቀርቷል")
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }

                // =========================================================================
                // DIALOG: EDIT STAFF ATTENDANCE TIME & STATUS (እረስቶት ዘግይቶ ቲክ ለማድረግ)
                // =========================================================================
                editingStaffItem?.let { staffItem ->
                    val isDialogEntry = editSession == StaffAttendanceSession.CHECK_IN
                    val roleSched = staffRoleSchedules.firstOrNull { it.roleCategory == staffItem.roleCategory }

                    AlertDialog(
                        onDismissRequest = { editingStaffItem = null },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = NavyPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isAmharic) "የአቴንዳንስ ሰዓት ማረሚያ" else "Edit Attendance Time",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "${staffItem.name} (${staffItem.role} • ${staffItem.roleCategory})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = NavyPrimary
                                )
                                if (roleSched != null) {
                                    Text(
                                        text = "የስራ ሰዓት፦ መግቢያ ${roleSched.entryStart}-${roleSched.entryEnd} | መውጫ ${roleSched.exitStart}-${roleSched.exitEnd}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))

                                // Session switcher inside dialog
                                Text(
                                    text = if (isAmharic) "ክፍለ-ጊዜ (Shift):" else "Session:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isDialogEntry) NavyPrimary else Color(0xFFF1F5F9),
                                        contentColor = if (isDialogEntry) Color.White else Color(0xFF334155),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                editSession = StaffAttendanceSession.CHECK_IN
                                                editTime = staffItem.entryTime
                                                editStatus = staffItem.entryStatus
                                                editRemarks = staffItem.entryRemarks
                                            }
                                    ) {
                                        Text(
                                            text = "🚪 የመግቢያ (Check-In)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (!isDialogEntry) NavyPrimary else Color(0xFFF1F5F9),
                                        contentColor = if (!isDialogEntry) Color.White else Color(0xFF334155),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                editSession = StaffAttendanceSession.CHECK_OUT
                                                editTime = staffItem.exitTime
                                                editStatus = staffItem.exitStatus
                                                editRemarks = staffItem.exitRemarks
                                            }
                                    ) {
                                        Text(
                                            text = "🚶‍♂️ የመውጫ (Check-Out)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Time Field
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isDialogEntry)
                                            (if (isAmharic) "የገቡበት ሰዓት (Check-In Time):" else "Check-In Time:")
                                        else
                                            (if (isAmharic) "የወጡበት ሰዓት (Check-Out Time):" else "Check-Out Time:"),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = GoldAccent.copy(alpha = 0.25f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                editTime = if (isDialogEntry) (roleSched?.entryStart ?: "01:42 ጧት") else (roleSched?.exitStart ?: "09:25 ከሰዓት")
                                            }
                                    ) {
                                        Text(
                                            text = "🕒 መደበኛ ሰዓት",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF78350F),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = editTime,
                                    onValueChange = { editTime = it },
                                    placeholder = { Text(if (isDialogEntry) "ምሳሌ፡ 01:40 ጧት" else "ምሳሌ፡ 09:20 ከሰዓት") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Status Picker
                                Text(
                                    text = if (isAmharic) "ሁኔታ (Status):" else "Status:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isDialogEntry) {
                                        AttendanceStatusButton(
                                            label = "ተገኝቷል",
                                            isSelected = editStatus == "ተገኝቷል",
                                            color = EthioGreen,
                                            onClick = { editStatus = "ተገኝቷል" }
                                        )
                                        AttendanceStatusButton(
                                            label = "አርፍዷል",
                                            isSelected = editStatus == "አርፍዷል",
                                            color = Color(0xFFD97706),
                                            onClick = { editStatus = "አርፍዷል" }
                                        )
                                        AttendanceStatusButton(
                                            label = "ቀርቷል",
                                            isSelected = editStatus == "ቀርቷል",
                                            color = EthioRed,
                                            onClick = { editStatus = "ቀርቷል" }
                                        )
                                        AttendanceStatusButton(
                                            label = "ፍቃድ",
                                            isSelected = editStatus == "በፍቃድ",
                                            color = Color(0xFF2563EB),
                                            onClick = { editStatus = "በፍቃድ" }
                                        )
                                    } else {
                                        AttendanceStatusButton(
                                            label = "ተገኝቷል",
                                            isSelected = editStatus == "ተገኝቷል",
                                            color = EthioGreen,
                                            onClick = { editStatus = "ተገኝቷል" }
                                        )
                                        AttendanceStatusButton(
                                            label = "ቀድሞ ወጥቷል",
                                            isSelected = editStatus == "ቀድሞ ወጥቷል",
                                            color = Color(0xFFD97706),
                                            onClick = { editStatus = "ቀድሞ ወጥቷል" }
                                        )
                                        AttendanceStatusButton(
                                            label = "ዘግይቶ ወጥቷል",
                                            isSelected = editStatus == "ዘግይቶ ወጥቷል",
                                            color = Color(0xFF2563EB),
                                            onClick = { editStatus = "ዘግይቶ ወጥቷል" }
                                        )
                                        AttendanceStatusButton(
                                            label = "ቀርቷል",
                                            isSelected = editStatus == "ቀርቷል",
                                            color = EthioRed,
                                            onClick = { editStatus = "ቀርቷል" }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Remarks input
                                Text(
                                    text = if (isAmharic) "ማስታወሻ / ምክንያት (Remarks):" else "Remarks / Reason:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = editRemarks,
                                    onValueChange = { editRemarks = it },
                                    placeholder = { Text(if (isAmharic) "ምሳሌ፡ ዘግይቶ በምክትል ር/መ ተመዝግቧል" else "Reason...") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    onSaveStaffAttendanceManual(
                                        staffItem.id,
                                        editSession,
                                        editStatus,
                                        editTime,
                                        editRemarks.ifBlank { "በም/ር ተስተካክሎ ተመዝግቧል" }
                                    )
                                    editingStaffItem = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                            ) {
                                Text(if (isAmharic) "አስቀምጥ" else "Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { editingStaffItem = null }) {
                                Text(if (isAmharic) "ሰርዝ" else "Cancel")
                            }
                        }
                    )
                }

                // =========================================================================
                // DIALOG: CONFIGURE STAFF SHIFT SCHEDULES (ተለዋዋጭ የሰዓት ገደብ ማዋቀሪያ)
                // =========================================================================
                if (showRoleScheduleDialog) {
                    StaffRoleScheduleConfigDialog(
                        schedules = staffRoleSchedules,
                        isAmharic = isAmharic,
                        onSaveSchedule = { cat, eStart, eEnd, xStart, xEnd, desc ->
                            onUpdateRoleSchedule(cat, eStart, eEnd, xStart, xEnd, desc)
                        },
                        onResetDefaults = onResetRoleSchedules,
                        onDismiss = { showRoleScheduleDialog = false }
                    )
                }
                // =========================================================================
                // DIALOG: AUTOMATIC PARENT SMS NOTIFICATION ON STUDENT ABSENCE
                // =========================================================================
                absentStudentForSms?.let { student ->
                    AlertDialog(
                        onDismissRequest = { absentStudentForSms = null },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📱", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isAmharic) "ለወላጅ የSMS ማሳወቂያ መላኪያ" else "Send Absence SMS to Parent",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = NavyPrimary
                                )
                            }
                        },
                        text = {
                            Column {
                                Text(
                                    text = if (isAmharic)
                                        "ተማሪው ቀርቷል ተብሎ ተመዝግቧል። ለወላጁ/አሳዳጊው የሚከተለው አጭር የጽሁፍ መልዕክት (SMS) ወዲያውኑ ይላካል፦"
                                    else
                                        "The student was marked absent. The following SMS notification will be sent to the parent:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "ተቀባይ ወላጅ: ${student.parentName} (${student.parentPhone})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = NavyPrimary
                                        )
                                        Text(
                                            text = "ተማሪ: ${student.fullName} • ${student.grade} - ${student.section}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = smsCustomMessage,
                                    onValueChange = { smsCustomMessage = it },
                                    label = { Text(if (isAmharic) "የመልዕክቱ ይዘት" else "SMS Message Body") },
                                    minLines = 3,
                                    maxLines = 5,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    try {
                                        val smsUri = Uri.parse("smsto:${student.parentPhone}")
                                        val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                                            putExtra("sms_body", smsCustomMessage)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(smsIntent)
                                        Toast.makeText(
                                            context,
                                            if (isAmharic) "የSMS መልዕክት መተግበሪያ ተከፍቷል!" else "SMS app opened!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "SMS መላክ አልተሳካም፦ ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    absentStudentForSms = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EthioGreen)
                            ) {
                                Text(
                                    text = if (isAmharic) "📨 አሁን ላክ (Send SMS)" else "Send SMS",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { absentStudentForSms = null }) {
                                Text(if (isAmharic) "ሰርዝ (ይቅር)" else "Skip")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StaffRoleScheduleConfigDialog(
    schedules: List<StaffRoleSchedule>,
    isAmharic: Boolean,
    onSaveSchedule: (String, String, String, String, String, String) -> Unit,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit
) {
    val categories = remember {
        listOf("መምህራን", "ፅዳት ሰራተኞች", "ደህንነትና ጥበቃ", "አስተዳደርና ፋይናንስ", "ሌሎች ሰራተኞች")
    }
    var selectedCat by remember { mutableStateOf(categories.first()) }

    val activeSched = schedules.firstOrNull { it.roleCategory == selectedCat }

    var entryStartInput by remember(selectedCat, activeSched) {
        mutableStateOf(activeSched?.entryStart ?: "01:30 ጧት")
    }
    var entryEndInput by remember(selectedCat, activeSched) {
        mutableStateOf(activeSched?.entryEnd ?: "01:51 ጧት")
    }
    var exitStartInput by remember(selectedCat, activeSched) {
        mutableStateOf(activeSched?.exitStart ?: "09:15 ከሰዓት")
    }
    var exitEndInput by remember(selectedCat, activeSched) {
        mutableStateOf(activeSched?.exitEnd ?: "09:40 ከሰዓት")
    }
    var descInput by remember(selectedCat, activeSched) {
        mutableStateOf(activeSched?.description ?: "")
    }

    var savedMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = NavyPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isAmharic) "የሰራተኞች የስራ ሰዓት ገደብ ማዋቀሪያ" else "Configure Staff Shift Schedules",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isAmharic) "ምክትል ር/መምህር እና አድሚን ብቻ የሚወስኑት" else "Vice Principal & Admin Configuration",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Category Selector
                Text(
                    text = if (isAmharic) "የሰራተኛ አይነት ይምረጡ፦" else "Select Staff Category:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                FilterChipRow(
                    options = categories,
                    selectedOption = selectedCat,
                    onSelectOption = {
                        selectedCat = it
                        savedMessage = null
                    },
                    testTagPrefix = "sched_cat"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Entry time group
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAmharic) "🚪 የመግቢያ ሰዓት ገደብ (Check-In)" else "Check-In Window",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = entryStartInput,
                                onValueChange = { entryStartInput = it },
                                label = { Text("መጀመሪያ", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = entryEndInput,
                                onValueChange = { entryEndInput = it },
                                label = { Text("ገደብ/ማብቂያ", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text(
                            text = "💡 ከ'${entryEndInput}' በኋላ የገባ ሰራተኛ እንደ 'አርፍዷል' ይመዘገባል።",
                            fontSize = 10.sp,
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Exit time group
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NightsStay, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAmharic) "🚶‍♂️ የመውጫ ሰዓት ገደብ (Check-Out)" else "Check-Out Window",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF1E40AF)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = exitStartInput,
                                onValueChange = { exitStartInput = it },
                                label = { Text("መጀመሪያ", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = exitEndInput,
                                onValueChange = { exitEndInput = it },
                                label = { Text("ገደብ/ማብቂያ", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text(
                            text = "💡 ከ'${exitStartInput}' በፊት የወጣ 'ቀድሞ ወጥቷል'፤ ከ'${exitEndInput}' በኋላ 'ዘግይቶ ወጥቷል' ይሆናል።",
                            fontSize = 10.sp,
                            color = Color(0xFF1E40AF),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                if (savedMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = savedMessage ?: "",
                        color = EthioGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(
                    onClick = {
                        onResetDefaults()
                        savedMessage = "ወደ ነባሪ ሰዓቶች ተመልሷል!"
                    }
                ) {
                    Text(if (isAmharic) "ወደ ነባሪ መልስ" else "Reset Defaults", fontSize = 11.sp)
                }
                Button(
                    onClick = {
                        onSaveSchedule(selectedCat, entryStartInput, entryEndInput, exitStartInput, exitEndInput, descInput)
                        savedMessage = "የ${selectedCat} የሰዓት ገደብ በተሳካ ሁኔታ ተቀምጧል!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text(if (isAmharic) "ለውጡን አስቀምጥ" else "Save Schedule")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isAmharic) "ዝጋ" else "Close")
            }
        }
    )
}

@Composable
private fun SummaryBadge(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(text = "$count", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
private fun AttendanceStatusButton(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) color else color.copy(alpha = 0.1f),
        contentColor = if (isSelected) Color.White else color,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}
