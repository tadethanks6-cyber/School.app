package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    onRecordAttendance: (AttendanceRecord) -> Unit,
    onRecordStaffAttendance: (Long, String, String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    // Mode switcher: Default to STUDENTS unless user explicitly switches to STAFF
    var activeSubMode by remember { mutableStateOf(AttendanceSubMode.STUDENTS) }

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
                        .padding(vertical = 6.dp)
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
                                            Text(
                                                text = staffItem.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "${staffItem.role} • ${staffItem.department}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (staffItem.status) {
                                                "ተገኝቷል" -> EthioGreen.copy(alpha = 0.15f)
                                                "ቀርቷል" -> EthioRed.copy(alpha = 0.15f)
                                                "አርፍዷል" -> Color(0xFFD97706).copy(alpha = 0.15f)
                                                else -> Color(0xFF2563EB).copy(alpha = 0.15f)
                                            }
                                        ) {
                                            Text(
                                                text = staffItem.status,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = when (staffItem.status) {
                                                    "ተገኝቷል" -> EthioGreen
                                                    "ቀርቷል" -> EthioRed
                                                    "አርፍዷል" -> Color(0xFFD97706)
                                                    else -> Color(0xFF2563EB)
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action buttons for Vice Principal & Admin
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = staffItem.checkInTime, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "• ${staffItem.remarks}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            AttendanceStatusButton(
                                                label = "✓ ተገኝቷል",
                                                isSelected = staffItem.status == "ተገኝቷል",
                                                color = EthioGreen,
                                                onClick = { onRecordStaffAttendance(staffItem.id, "ተገኝቷል", "በሰዓቱ ተገኝቷል") }
                                            )
                                            AttendanceStatusButton(
                                                label = "✕ ቀርቷል",
                                                isSelected = staffItem.status == "ቀርቷል",
                                                color = EthioRed,
                                                onClick = { onRecordStaffAttendance(staffItem.id, "ቀርቷል", "ያለፈቃድ የቀረ") }
                                            )
                                            AttendanceStatusButton(
                                                label = "⏱ አርፍዷል",
                                                isSelected = staffItem.status == "አርፍዷል",
                                                color = Color(0xFFD97706),
                                                onClick = { onRecordStaffAttendance(staffItem.id, "አርፍዷል", "15 ደቂቃ ዘግይቷል") }
                                            )
                                            AttendanceStatusButton(
                                                label = "📝 ፍቃድ",
                                                isSelected = staffItem.status == "በፍቃድ",
                                                color = Color(0xFF2563EB),
                                                onClick = { onRecordStaffAttendance(staffItem.id, "በፍቃድ", "የህመም/ስራ ፍቃድ") }
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
            }
        }
    }
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
