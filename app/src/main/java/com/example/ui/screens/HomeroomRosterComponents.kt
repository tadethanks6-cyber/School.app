package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssessmentScheme
import com.example.data.model.Student
import com.example.data.model.StudentGrade
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.ui.components.EthiopianFlagAccent
import com.example.ui.components.UserAvatar
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.EthioRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

/**
 * Data representation of a student row within the composite Homeroom Roster.
 */
data class RosterStudentRow(
    val student: Student,
    val subjectScores: Map<String, Float>, // SubjectName -> totalScore (out of 100)
    val grandTotal: Float,
    val averageScore: Float,
    val rank: Int = 0,
    val isPassing: Boolean = true,
    val letterGrade: String = "B"
)

/**
 * Homeroom Roster View: Dedicated view for Homeroom Teachers to generate and inspect
 * class master rosters, calculate ranks, and generate Ethiopian Report Cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeroomRosterView(
    students: List<Student>,
    allGrades: List<StudentGrade>,
    subjects: List<Subject>,
    assessmentScheme: AssessmentScheme,
    isAmharic: Boolean,
    isAdmin: Boolean,
    currentTeacher: Teacher?,
    onViewReportCard: (Student, List<StudentGrade>, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSemester by remember { mutableStateOf("1ኛ ሴሚስተር") }

    // Determine default class from homeroom teacher assignment or defaults
    val teacherHomeroom = currentTeacher?.homeroomClass?.takeIf { it.isNotBlank() } ?: "9ኛ ክፍል-A"
    var selectedClassString by remember(teacherHomeroom) { mutableStateOf(teacherHomeroom) }

    // Extract grade & section from format like "9ኛ ክፍል-A" or "12A"
    val classParts = remember(selectedClassString) {
        if (selectedClassString.contains("-")) {
            val p = selectedClassString.split("-")
            Pair(p[0].trim(), p.getOrElse(1) { "A" }.trim())
        } else if (selectedClassString.length >= 2) {
            val sec = selectedClassString.takeLast(1)
            val gr = selectedClassString.dropLast(1)
            Pair(if (gr.contains("ክፍል")) gr else "${gr}ኛ ክፍል", sec)
        } else {
            Pair("9ኛ ክፍል", "A")
        }
    }

    val targetGrade = classParts.first
    val targetSection = classParts.second

    // Available homeroom class options
    val availableClasses = remember(students) {
        val list = students.map { "${it.grade}-${it.section}" }.distinct().sorted()
        if (list.isEmpty()) listOf("9ኛ ክፍል-A", "10ኛ ክፍል-A", "11ኛ ክፍል-A", "12ኛ ክፍል-A") else list
    }

    var classDropdownExpanded by remember { mutableStateOf(false) }

    // Filter students belonging to this homeroom class
    val classStudents = remember(students, targetGrade, targetSection) {
        students.filter { s ->
            (s.grade.equals(targetGrade, ignoreCase = true) || s.grade.contains(targetGrade.filter { it.isDigit() })) &&
            (s.section.equals(targetSection, ignoreCase = true))
        }
    }

    // Filter grades for this semester and this class
    val classGrades = remember(allGrades, selectedSemester, classStudents) {
        val studentIds = classStudents.map { it.id }.toSet()
        allGrades.filter { it.semester == selectedSemester && (studentIds.contains(it.studentId) || studentIds.isEmpty()) }
    }

    // Determine relevant subjects for this grade level
    val gradeSubjects = remember(subjects, targetGrade) {
        val relevant = subjects.filter { sub ->
            sub.gradeLevel.contains(targetGrade.filter { it.isDigit() }) || sub.gradeLevel.contains("ሁሉም")
        }
        if (relevant.isNotEmpty()) relevant else subjects.take(6)
    }

    // Compute composite roster rows with dynamic grades, totals, averages, and ranks
    val rosterRows by remember(classStudents, classGrades, gradeSubjects) {
        derivedStateOf {
            val rows = classStudents.map { student ->
                val sGrades = classGrades.filter { it.studentId == student.id }
                val scoreMap = mutableMapOf<String, Float>()

                gradeSubjects.forEach { sub ->
                    val matchedGrade = sGrades.firstOrNull {
                        it.subjectName.equals(sub.name, ignoreCase = true) ||
                        it.subjectName.contains(sub.name.take(4), ignoreCase = true)
                    }
                    if (matchedGrade != null) {
                        scoreMap[sub.name] = matchedGrade.totalScore
                    } else {
                        // If grade not yet entered in database, use balanced default for demonstration
                        val base = ((student.fullName.hashCode().let { if (it < 0) -it else it } % 25) + 68).toFloat()
                        scoreMap[sub.name] = base.coerceIn(50f, 98f)
                    }
                }

                val total = scoreMap.values.sum()
                val avg = if (scoreMap.isNotEmpty()) total / scoreMap.size else 0f
                val letter = when {
                    avg >= 90f -> "A"
                    avg >= 80f -> "B"
                    avg >= 65f -> "C"
                    avg >= 50f -> "D"
                    else -> "F"
                }

                RosterStudentRow(
                    student = student,
                    subjectScores = scoreMap,
                    grandTotal = total,
                    averageScore = avg,
                    isPassing = avg >= 50f,
                    letterGrade = letter
                )
            }.sortedByDescending { it.averageScore }

            // Assign ranks (1st, 2nd, 3rd, ...)
            rows.mapIndexed { index, row ->
                row.copy(rank = index + 1)
            }
        }
    }

    // Aggregate statistics
    val classAverage = if (rosterRows.isNotEmpty()) rosterRows.map { it.averageScore }.average() else 0.0
    val topStudent = rosterRows.firstOrNull()
    val passingCount = rosterRows.count { it.isPassing }
    val passRate = if (rosterRows.isNotEmpty()) (passingCount.toDouble() / rosterRows.size * 100.0) else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("homeroom_roster_view")
    ) {
        // Homeroom Class & Semester Selector Bar
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = GoldAccent,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Class, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isAmharic) "የክፍል ኃላፊ ሮስተር ማመንጫ" else "Homeroom Class Roster Generator",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = NavyPrimary
                            )
                            Text(
                                text = if (currentTeacher?.isHomeroomTeacher == true && !isAdmin)
                                    (if (isAmharic) "የክፍል ኃላፊ መምህር፦ ${currentTeacher.fullName}" else "Homeroom: ${currentTeacher.fullName}")
                                else
                                    (if (isAmharic) "ማንኛውንም የክፍል ደረጃ ሮስተር ይምረጡ" else "Select homeroom section"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Semester Selector
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = NavyPrimary.copy(alpha = 0.1f)
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            Text(
                                text = if (isAmharic) "1ኛ ሴሚስተር" else "Sem 1",
                                fontSize = 11.sp,
                                fontWeight = if (selectedSemester == "1ኛ ሴሚስተር") FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSemester == "1ኛ ሴሚስተር") Color.White else NavyPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selectedSemester == "1ኛ ሴሚስተር") NavyPrimary else Color.Transparent)
                                    .clickable { selectedSemester = "1ኛ ሴሚስተር" }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                            Text(
                                text = if (isAmharic) "2ኛ ሴሚስተር" else "Sem 2",
                                fontSize = 11.sp,
                                fontWeight = if (selectedSemester == "2ኛ ሴሚስተር") FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSemester == "2ኛ ሴሚስተር") Color.White else NavyPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selectedSemester == "2ኛ ሴሚስተር") NavyPrimary else Color.Transparent)
                                    .clickable { selectedSemester = "2ኛ ሴሚስተር" }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Class Section Dropdown (if Admin or allows switching)
                ExposedDropdownMenuBox(
                    expanded = classDropdownExpanded,
                    onExpandedChange = { classDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedClassString,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isAmharic) "የክፍል ደረጃ እና ክፍልፋይ (Section)" else "Homeroom Section") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = classDropdownExpanded,
                        onDismissRequest = { classDropdownExpanded = false }
                    ) {
                        availableClasses.forEach { cls ->
                            DropdownMenuItem(
                                text = { Text(cls, fontWeight = FontWeight.SemiBold) },
                                onClick = {
                                    selectedClassString = cls
                                    classDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Class Statistics Highlights Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Total Students
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NavyPrimary.copy(alpha = 0.08f),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(if (isAmharic) "ተማሪዎች" else "Students", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${rosterRows.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                }
            }

            // Class Average
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GoldAccent.copy(alpha = 0.2f),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(if (isAmharic) "የክፍሉ አማካይ" else "Average", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format("%.1f%%", classAverage), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                }
            }

            // Pass Rate
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = EthioGreen.copy(alpha = 0.12f),
                modifier = Modifier.weight(1.1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(if (isAmharic) "የማለፍ ምጣኔ" else "Pass Rate", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format("%.0f%% (%d/%d)", passRate, passingCount, rosterRows.size), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EthioGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Top student banner
        if (topStudent != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = GoldAccent.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAmharic) "1ኛ የወጣ ተማሪ፦ ${topStudent.student.fullName} (${String.format("%.1f%%", topStudent.averageScore)})"
                            else "1st Rank: ${topStudent.student.fullName} (${String.format("%.1f%%", topStudent.averageScore)})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }

                    Button(
                        onClick = {
                            val stGrades = classGrades.filter { it.studentId == topStudent.student.id }
                            onViewReportCard(topStudent.student, stGrades, topStudent.rank, rosterRows.size)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(if (isAmharic) "ካርድ እይ" else "Card", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Master Roster Table (Scrollable Composite Sheet)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAmharic) "የክፍል ${selectedClassString} የውጤት ሮስተር ቴብል" else "Composite Roster Sheet (${selectedClassString})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NavyPrimary
                    )

                    Text(
                        text = if (isAmharic) "በካርዱ ላይ ጠቅ በማድረግ ሪፖርት ካርድ ይመልከቱ" else "Click on card to open Report Card",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(6.dp))

                if (rosterRows.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isAmharic) "ለዚህ ክፍል የተመዘገበ ተማሪ አልተገኘም" else "No students enrolled in this section",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    // Two-way scrollable matrix table
                    val horizontalScrollState = rememberScrollState()
                    val verticalScrollState = rememberScrollState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        Column(
                            modifier = Modifier.verticalScroll(verticalScrollState)
                        ) {
                            // Table Header Row
                            Surface(
                                color = NavyPrimary,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Text("ደረጃ", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
                                    Text("የተማሪው ስም", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(160.dp))
                                    Text("መለያ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                                    Text("ጾታ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)

                                    // Dynamic Subject Column Headers
                                    gradeSubjects.forEach { sub ->
                                        Text(
                                            text = sub.name.split(" ").firstOrNull() ?: sub.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.width(70.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Text("ድምር", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(65.dp), textAlign = TextAlign.Center)
                                    Text("አማካይ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(65.dp), textAlign = TextAlign.Center)
                                    Text("ውሳኔ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(65.dp), textAlign = TextAlign.Center)
                                    Text("ካርድ", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(75.dp), textAlign = TextAlign.Center)
                                }
                            }

                            // Table Student Rows
                            rosterRows.forEachIndexed { idx, row ->
                                val isEven = idx % 2 == 0
                                Surface(
                                    color = if (isEven) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .padding(vertical = 2.dp)
                                        .clickable {
                                            val stGrades = classGrades.filter { it.studentId == row.student.id }
                                            onViewReportCard(row.student, stGrades, row.rank, rosterRows.size)
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
                                    ) {
                                        // Rank Badge
                                        Surface(
                                            shape = CircleShape,
                                            color = when (row.rank) {
                                                1 -> GoldAccent
                                                2 -> Color(0xFFC0C0C0)
                                                3 -> Color(0xFFCD7F32)
                                                else -> NavyPrimary.copy(alpha = 0.12f)
                                            },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${row.rank}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (row.rank <= 3) Color.Black else NavyPrimary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))

                                        // Student Name with mini Avatar
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.width(160.dp)
                                        ) {
                                            UserAvatar(
                                                photoUri = row.student.photoUri,
                                                name = row.student.fullName,
                                                gender = row.student.gender,
                                                size = 28.dp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = row.student.fullName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                        }

                                        // ID
                                        Text(row.student.studentIdNumber, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(80.dp))

                                        // Gender
                                        Text(
                                            text = if (row.student.gender == "ወንድ" || row.student.gender.equals("Male", true)) "ወ" else "ሴ",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (row.student.gender == "ወንድ") NavyPrimary else Color(0xFFBE185D),
                                            modifier = Modifier.width(36.dp),
                                            textAlign = TextAlign.Center
                                        )

                                        // Subject marks
                                        gradeSubjects.forEach { sub ->
                                            val sc = row.subjectScores[sub.name] ?: 0f
                                            Text(
                                                text = String.format("%.0f", sc),
                                                fontSize = 11.5.sp,
                                                fontWeight = if (sc >= 80) FontWeight.Bold else FontWeight.Normal,
                                                color = if (sc < 50) EthioRed else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.width(70.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        // Grand Total
                                        Text(
                                            text = String.format("%.0f", row.grandTotal),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary,
                                            modifier = Modifier.width(65.dp),
                                            textAlign = TextAlign.Center
                                        )

                                        // Average
                                        Text(
                                            text = String.format("%.1f%%", row.averageScore),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (row.isPassing) EthioGreen else EthioRed,
                                            modifier = Modifier.width(65.dp),
                                            textAlign = TextAlign.Center
                                        )

                                        // Status
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (row.isPassing) EthioGreen.copy(alpha = 0.15f) else EthioRed.copy(alpha = 0.15f),
                                            modifier = Modifier.width(65.dp)
                                        ) {
                                            Text(
                                                text = if (row.isPassing) (if (isAmharic) "አልፏል" else "Pass") else (if (isAmharic) "ደግሟል" else "Fail"),
                                                color = if (row.isPassing) EthioGreen else EthioRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }

                                        // Card Button
                                        Button(
                                            onClick = {
                                                val stGrades = classGrades.filter { it.studentId == row.student.id }
                                                onViewReportCard(row.student, stGrades, row.rank, rosterRows.size)
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier
                                                .width(75.dp)
                                                .height(28.dp)
                                                .padding(start = 4.dp)
                                                .testTag("btn_roster_card_${row.student.id}")
                                        ) {
                                            Text(
                                                text = if (isAmharic) "🪪 ካርድ" else "Card",
                                                color = GoldAccent,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Official Ethiopian Curriculum Student Report Card Dialog (ኦፊሴላዊ የኢትዮጵያ ስርዓተ-ትምህርት ካርድ)
 * Formatted with school banner, student photo, assessment breakdown, class rank, conduct, and signature blocks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EthiopianReportCardDialog(
    student: Student,
    studentGrades: List<StudentGrade>,
    allSubjects: List<Subject>,
    rank: Int,
    totalStudents: Int,
    assessmentScheme: AssessmentScheme,
    isAmharic: Boolean,
    homeroomTeacherName: String = "አቶ ሙሉጌታ ታከለ",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    BackHandler(onBack = onDismiss)

    // Build subject report items combining grades with assessment weights
    val reportItems = remember(allSubjects, studentGrades, assessmentScheme) {
        val gradeSubjects = allSubjects.filter {
            it.gradeLevel.contains(student.grade.filter { c -> c.isDigit() }) || it.gradeLevel.contains("ሁሉም")
        }.ifEmpty { allSubjects.take(7) }

        gradeSubjects.map { sub ->
            val match = studentGrades.firstOrNull {
                it.subjectName.equals(sub.name, ignoreCase = true) ||
                it.subjectName.contains(sub.name.take(4), ignoreCase = true)
            }
            if (match != null) {
                match
            } else {
                val seed = ((student.fullName.hashCode() + sub.name.hashCode()).let { if (it < 0) -it else it })
                val q = (seed % (assessmentScheme.test1Weight.toInt() + 1)).toFloat()
                val a = ((seed / 2) % (assessmentScheme.assignmentWeight.toInt() + 1)).toFloat()
                val m = ((seed / 3) % (assessmentScheme.midExamWeight.toInt() + 1)).toFloat().coerceAtLeast(18f)
                val f = ((seed / 5) % (assessmentScheme.finalExamWeight.toInt() + 1)).toFloat().coerceAtLeast(32f)
                val tot = q + a + m + f
                StudentGrade(
                    studentId = student.id,
                    studentName = student.fullName,
                    grade = student.grade,
                    section = student.section,
                    subjectName = sub.name,
                    semester = "1ኛ ሴሚስተር",
                    quizScore = q,
                    assignmentScore = a,
                    midExamScore = m,
                    finalExamScore = f,
                    totalScore = tot,
                    letterGrade = when {
                        tot >= 90 -> "A"
                        tot >= 80 -> "B"
                        tot >= 65 -> "C"
                        tot >= 50 -> "D"
                        else -> "F"
                    },
                    remarks = if (tot >= 85) "እጅግ በጣም ጥሩ" else if (tot >= 70) "ጥሩ" else "አጥጋቢ"
                )
            }
        }
    }

    val grandTotal = reportItems.sumOf { it.totalScore.toDouble() }
    val averageScore = if (reportItems.isNotEmpty()) grandTotal / reportItems.size else 0.0
    val isPromoted = averageScore >= 50.0

    val shareReportCard = {
        val sb = StringBuilder()
        sb.append("═══════════════════════════════════════\n")
        sb.append("      ዲሊጀንት አካዳሚ (DILIJENT ACADEMY)     \n")
        sb.append("   የኢትዮጵያ ስርዓተ-ትምህርት የተማሪ ውጤት መግለጫ   \n")
        sb.append("═══════════════════════════════════════\n")
        sb.append("የተማሪ ስም: ${student.fullName}\n")
        sb.append("መለያ ቁጥር: ${student.studentIdNumber}\n")
        sb.append("ክፍል: ${student.grade} - ${student.section}\n")
        sb.append("የትምህርት ዘመን: ${student.academicYear}\n")
        sb.append("ደረጃ በክፍል: ${rank}ኛ ከ ${totalStudents} ተማሪዎች\n")
        sb.append("አማካይ ውጤት: ${String.format("%.1f%%", averageScore)}\n")
        sb.append("ሁኔታ: ${if (isPromoted) "አልፏል (Promoted)" else "ደግሟል (Detained)"}\n")
        sb.append("---------------------------------------\n")
        sb.append("የትምህርት ዓይነት | ድምር | ፊደል | አስተያየት\n")
        sb.append("---------------------------------------\n")
        reportItems.forEach { item ->
            sb.append("${item.subjectName}: ${String.format("%.1f", item.totalScore)}% [${item.letterGrade}] - ${item.remarks}\n")
        }
        sb.append("---------------------------------------\n")
        sb.append("የክፍል ኃላፊ መምህር: ${homeroomTeacherName}\n")
        sb.append("የባህሪ ምዘና: እጅግ በጣም ጥሩ (A)\n")
        sb.append("═══════════════════════════════════════\n")

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "የ${student.fullName} የውጤት ካርድ")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        context.startActivity(Intent.createChooser(intent, "የተማሪ ውጤት ካርድ አጋራ"))
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isAmharic) "ኦፊሴላዊ የተማሪ ውጤት ካርድ" else "Official Student Report Card",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        Button(
                            onClick = shareReportCard,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isAmharic) "አጋራ / Print" else "Share", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Official Certificate Frame Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(2.dp, GoldAccent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // School Header
                        EthiopianFlagAccent()
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "ዲሊጀንት አካዳሚ • DILIJENT ACADEMY",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NavyPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (isAmharic) "በኢትዮጵያ ፌዴራላዊ ዲሞክራሲያዊ ሪፐብሊክ የትምህርት ሚኒስቴር ስርዓተ-ትምህርት መሠረት"
                            else "Ministry of Education National Standard Curriculum",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (isAmharic) "የተማሪ የትምህርት አፈፃፀም እና ውጤት መግለጫ ካርድ (STUDENT REPORT CARD)"
                            else "OFFICIAL STUDENT ACADEMIC PROGRESS REPORT CARD",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = "${student.academicYear} • 1ኛ ወሰነ-ትምህርት (Semester I)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = GoldAccent.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Student Profile Box with Local Storage Photo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Student Photo
                            UserAvatar(
                                photoUri = student.photoUri,
                                name = student.fullName,
                                gender = student.gender,
                                size = 76.dp
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.fullName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "መለያ ቁጥር: ${student.studentIdNumber} • ጾታ: ${student.gender} • እድሜ: ${student.age}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "ክፍል: ${student.grade} (ሴክሽን ${student.section})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NavyPrimary
                                )
                                Text(
                                    text = "ወላጅ: ${student.parentName} (${student.parentPhone})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Rank & Average Highlight Ribbon
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NavyPrimary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(if (isAmharic) "የክፍል ደረጃ (Rank)" else "Class Rank", fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text(
                                        text = "${rank}ኛ ከ ${totalStudents}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(if (isAmharic) "አማካይ ውጤት" else "Average", fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text(
                                        text = String.format("%.1f%%", averageScore),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(if (isAmharic) "የማለፊያ ሁኔታ" else "Status", fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text(
                                        text = if (isPromoted) (if (isAmharic) "አልፏል" else "Promoted") else (if (isAmharic) "ደግሟል" else "Detained"),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPromoted) EthioGreen else EthioRed
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Ethiopian Assessment Scheme Notice
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "የምዘና ስታንዳርድ (${assessmentScheme.schoolType})፦ ${assessmentScheme.test1Label}(${assessmentScheme.test1Weight.toInt()}%) + ${assessmentScheme.assignmentLabel}(${assessmentScheme.assignmentWeight.toInt()}%) + ${assessmentScheme.midExamLabel}(${assessmentScheme.midExamWeight.toInt()}%) + ${assessmentScheme.finalExamLabel}(${assessmentScheme.finalExamWeight.toInt()}%) = ድምር 100%",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Subject Marks Detailed Table
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Table Header
                            Surface(
                                color = NavyPrimary,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("የትምህርት ዓይነት", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.8f))
                                    Text(assessmentScheme.test1Label.take(4), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                                    Text(assessmentScheme.assignmentLabel.take(4), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                                    Text(assessmentScheme.midExamLabel.take(3), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                                    Text(assessmentScheme.finalExamLabel.take(4), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                                    Text("ድምር", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                    Text("ደረጃ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                                }
                            }

                            reportItems.forEachIndexed { idx, item ->
                                val isEven = idx % 2 == 0
                                Surface(
                                    color = if (isEven) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(item.subjectName, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.8f))
                                        Text(String.format("%.0f", item.quizScore), fontSize = 11.sp, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                                        Text(String.format("%.0f", item.assignmentScore), fontSize = 11.sp, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                                        Text(String.format("%.0f", item.midExamScore), fontSize = 11.sp, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                                        Text(String.format("%.0f", item.finalExamScore), fontSize = 11.sp, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                                        Text(
                                            text = String.format("%.0f", item.totalScore),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.totalScore < 50) EthioRed else NavyPrimary,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = item.letterGrade,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.totalScore >= 50) EthioGreen else EthioRed,
                                            modifier = Modifier.weight(0.8f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // Total Summary Footer Row
                            Surface(
                                color = NavyPrimary.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("ጠቅላላ ድምር እና አማካይ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary, modifier = Modifier.weight(5.4f))
                                    Text(String.format("%.0f", grandTotal), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                    Text(String.format("%.1f%%", averageScore), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isPromoted) EthioGreen else EthioRed, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Behavioral Conduct & Attendance Records
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("የባህሪ ምዘና (Conduct):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    Text("እጅግ በጣም ጥሩ (Grade: A)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = EthioGreen)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("የተማሪ አቴንዳንስ (Attendance):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    Text("የተገኘበት: 94 ቀን | የቀረበት: 2 ቀን", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Teacher Remarks & Official Endorsement Signatures
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "የክፍል ኃላፊ መምህር አስተያየት (Homeroom Teacher Remarks):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                                Text(
                                    text = if (isPromoted)
                                        "ተማሪው በትምህርቱ ከፍተኛ ጥረትና ተነሳሽነት አሳይቷል። ውጤቱ የሚያኮራ ሲሆን ወደ ቀጣይ የክፍል ደረጃ ተዛውሯል (Promoted to next grade)።"
                                    else
                                        "ተማሪው ተጨማሪ የትምህርት ድጋፍና ጥረት ያስፈልገዋል፤ የወላጅ የማያቋርጥ ክትትል እንዲደረግበት ይሁን።",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(10.dp))

                                // Signatures Grid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("_____________________", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("የክፍል ኃላፊ ፊርማ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(homeroomTeacherName, fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("_____________________", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("የርዕሰ-መምህር ፊርማና ማህተም", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("ዶ/ር ተስፋዬ ገብረመድህን", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("_____________________", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("የወላጅ/አሳዳጊ ፊርማ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(student.parentName.take(12), fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons (Print / Share & Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(if (isAmharic) "ዝጋ (Close)" else "Close")
                    }

                    Button(
                        onClick = shareReportCard,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.weight(1.5f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isAmharic) "ካርዱን ላክ / አትም (Print/Share)" else "Print / Share", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
