package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.School
import com.example.data.model.AttendanceRecord
import com.example.data.model.Student
import com.example.data.model.Teacher
import com.example.data.model.TeacherWorkload
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FilterChipRow
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.EthioRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

@Composable
fun AttendanceScreen(
    students: List<Student>,
    availableGrades: List<String>,
    sectionOptions: List<String>,
    isAmharic: Boolean,
    isAdmin: Boolean = true,
    isTeacher: Boolean = false,
    isStudent: Boolean = false,
    currentTeacher: Teacher? = null,
    currentStudent: Student? = null,
    teacherWorkloads: List<TeacherWorkload> = emptyList(),
    onRecordAttendance: (AttendanceRecord) -> Unit,
    modifier: Modifier = Modifier
) {
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

    // Local attendance statuses map: studentId -> status
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

    val presentCount = classStudents.count { attendanceStatusMap[it.id] == "ተገኝቷል" }
    val absentCount = classStudents.count { attendanceStatusMap[it.id] == "ቀርቷል" }
    val excusedCount = classStudents.count { attendanceStatusMap[it.id] == "በፍቃድ" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("attendance_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Role Security Banner
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
                                "የተመደቡልዎትን ክፍሎች ብቻ አቴንዳንስ መውሰድ ይችላሉ። ሌላ ሰው የእርስዎን ክፍል አይወስድም።"
                            else
                                "You can only record attendance for sections assigned to your teaching workload."
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
            options = sectionOptions,
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
                count = presentCount,
                color = EthioGreen,
                modifier = Modifier.weight(1f)
            )
            SummaryBadge(
                label = if (isAmharic) "የቀሩ" else "Absent",
                count = absentCount,
                color = EthioRed,
                modifier = Modifier.weight(1f)
            )
            SummaryBadge(
                label = if (isAmharic) "በፍቃድ" else "Excused",
                count = excusedCount,
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
                                    text = student.studentIdNumber,
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
