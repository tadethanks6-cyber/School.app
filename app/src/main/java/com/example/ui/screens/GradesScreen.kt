package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import com.example.data.model.AssessmentScheme
import com.example.data.model.Student
import com.example.data.model.StudentGrade
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherWorkload
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.EthiopianFlagAccent
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.EthioRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen(
    students: List<Student>,
    selectedStudent: Student?,
    onSelectStudent: (Student) -> Unit,
    grades: List<StudentGrade>,
    subjects: List<Subject>,
    isAmharic: Boolean,
    isAdmin: Boolean = true,
    isStudent: Boolean = false,
    currentTeacher: Teacher? = null,
    currentStudent: Student? = null,
    teacherWorkloads: List<TeacherWorkload> = emptyList(),
    assessmentScheme: AssessmentScheme = AssessmentScheme.MOE_STANDARD,
    onConfigureSchemeClick: (() -> Unit)? = null,
    onAddGradeClick: () -> Unit,
    onDeleteGradeClick: (StudentGrade) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSemester by remember { mutableStateOf("1ኛ ሴሚስተር") }
    var studentDropdownExpanded by remember { mutableStateOf(false) }
    var gradeToDelete by remember { mutableStateOf<StudentGrade?>(null) }

    val isTeacherRole = !isAdmin && currentTeacher != null
    val assignedClasses = remember(teacherWorkloads) {
        teacherWorkloads.map { "${it.grade}-${it.section}" }.distinct()
    }
    val assignedSubjectNames = remember(teacherWorkloads) {
        teacherWorkloads.map { it.subjectName }.distinct()
    }

    // Filter students strictly if teacher or student is logged in
    val visibleStudents = remember(students, isTeacherRole, isStudent, currentStudent, assignedClasses, currentTeacher) {
        when {
            isStudent -> listOfNotNull(currentStudent ?: selectedStudent ?: students.firstOrNull())
            isTeacherRole && assignedClasses.isNotEmpty() -> {
                val list = students.filter { s ->
                    assignedClasses.contains("${s.grade}-${s.section}") ||
                    (currentTeacher?.isHomeroomTeacher == true && s.grade == currentTeacher.homeroomClass)
                }
                if (list.isEmpty()) students else list
            }
            else -> students
        }
    }

    // Grades visible on the report card
    val allowedGrades = grades

    val filteredGrades = allowedGrades.filter { it.semester == selectedSemester }
    val totalScore = filteredGrades.sumOf { it.totalScore.toDouble() }
    val averageScore = if (filteredGrades.isNotEmpty()) totalScore / filteredGrades.size else 0.0

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("grades_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Confidential Role Security Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isStudent -> Color(0xFF0284C7).copy(alpha = 0.10f)
                        isTeacherRole -> Color(0xFF6D28D9).copy(alpha = 0.10f)
                        else -> NavyPrimary.copy(alpha = 0.08f)
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            isStudent -> Icons.Default.School
                            isTeacherRole -> Icons.Default.Lock
                            else -> Icons.Default.Security
                        },
                        contentDescription = null,
                        tint = when {
                            isStudent -> Color(0xFF0284C7)
                            isTeacherRole -> Color(0xFF6D28D9)
                            else -> NavyPrimary
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = when {
                                isStudent -> if (isAmharic)
                                    "🎓 የተማሪ ${currentStudent?.fullName ?: selectedStudent?.fullName ?: ""} ገፅ፦ የራስዎ ውጤት"
                                else
                                    "🎓 Student Academic Portal: My Official Grades"
                                isTeacherRole -> if (isAmharic)
                                    "🔒 የመምህር ${currentTeacher?.fullName ?: ""} ገፅ፦ የተመደቡ ክፍሎች ብቻ"
                                else
                                    "🔒 Teacher ${currentTeacher?.fullName ?: ""}'s Portal: Assigned Classes Only"
                                else -> if (isAmharic)
                                    "👑 የሲስተም አድሚን ገፅ (Executive Admin Oversight)"
                                else
                                    "👑 Executive Admin Grade Oversight"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = when {
                                isStudent -> Color(0xFF0369A1)
                                isTeacherRole -> Color(0xFF5B21B6)
                                else -> NavyPrimary
                            }
                        )
                        Text(
                            text = when {
                                isStudent -> if (isAmharic)
                                    "የእርስዎን ሴሚስተር ማርክ እና ደረጃ ብቻ ይመልከቱ። ማንም ሌላ ሰው የእርስዎን ውጤት ማየት ወይም መቀየር አይችልም።"
                                else
                                    "Confidential student report card. No other student can view or modify your grades."
                                isTeacherRole -> if (isAmharic)
                                    "የሚያስተምሯቸውን የትምህርት ክፍሎችና ዓይነቶች ብቻ ማርክ መሙላት ይችላሉ። ከእርስዎ ውጭ ማንም ማርክ አይቀይርም።"
                                else
                                    "You can only enter & manage grades for your assigned classes. Other teachers cannot edit your grades."
                                else -> if (isAmharic)
                                    "ሁሉንም ክፍሎች፣ መምህራን እና ውጤቶች የመቆጣጠር እና የማስተዳደር ሙሉ ስልጣን አልዎት።"
                                else
                                    "Full executive authorization to review, edit, and audit all school grades."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Executive Assessment Scheme Banner for Principal / Headmaster (ርዕሰ መ/ር)
            if (isAdmin && onConfigureSchemeClick != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary.copy(alpha = 0.07f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚙️", fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
                                Text(
                                    text = if (isAmharic) "የኢትዮጵያ ስርዓተ-ትምህርት ምዘና ማዋቀሪያ" else "Curriculum Evaluation Scheme",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = NavyPrimary
                                )
                            }
                            Text(
                                text = "${if (isAmharic) assessmentScheme.titleAmharic else assessmentScheme.titleEnglish} • ${assessmentScheme.schoolType}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EthioGreen
                            )
                            Text(
                                text = "${assessmentScheme.test1Label}: ${assessmentScheme.test1Weight.toInt()}% | ${assessmentScheme.assignmentLabel}: ${assessmentScheme.assignmentWeight.toInt()}% | ${assessmentScheme.midExamLabel}: ${assessmentScheme.midExamWeight.toInt()}% | ${assessmentScheme.finalExamLabel}: ${assessmentScheme.finalExamWeight.toInt()}%",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = onConfigureSchemeClick,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = if (isAmharic) "አዋቅር" else "Configure",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }

            // Student Picker Dropdown
            ExposedDropdownMenuBox(
                expanded = studentDropdownExpanded,
                onExpandedChange = { studentDropdownExpanded = !studentDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedStudent?.let { "${it.fullName} (${it.grade}-${it.section})" }
                        ?: (if (isAmharic) "ተማሪ ምረጥ..." else "Select Student..."),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isAmharic) "ተማሪ ምረጥ" else "Select Student") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("student_picker_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = studentDropdownExpanded,
                    onDismissRequest = { studentDropdownExpanded = false }
                ) {
                    visibleStudents.forEach { s ->
                        DropdownMenuItem(
                            text = { Text("${s.fullName} • ${s.studentIdNumber} (${s.grade}-${s.section})") },
                            onClick = {
                                onSelectStudent(s)
                                studentDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Semester Switcher
            TabRow(
                selectedTabIndex = if (selectedSemester == "1ኛ ሴሚስተር") 0 else 1,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedSemester == "1ኛ ሴሚስተር",
                    onClick = { selectedSemester = "1ኛ ሴሚስተር" },
                    text = {
                        Text(
                            if (isAmharic) "1ኛ ሴሚስተር" else "Semester 1",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedSemester == "2ኛ ሴሚስተር",
                    onClick = { selectedSemester = "2ኛ ሴሚስተር" },
                    text = {
                        Text(
                            if (isAmharic) "2ኛ ሴሚስተር" else "Semester 2",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedStudent == null) {
                EmptyStateCard(
                    message = if (isAmharic) "የውጤት መዝገብ ወይም ካርድ ለማየት እባክዎ ከላይ ተማሪ ይምረጡ" else "Please select a student above to view report card",
                    icon = Icons.Default.Assessment
                )
            } else {
                // Report Card Header (የተማሪ የውጤት ካርድ ማጠቃለያ)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = if (isAmharic) "የትምህርት ውጤት ካርድ (Report Card)" else "Official Academic Report Card",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${selectedStudent.fullName} • ${selectedStudent.grade}-${selectedStudent.section}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldAccent
                            ) {
                                Text(
                                    text = selectedStudent.academicYear,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        EthiopianFlagAccent()
                        Spacer(modifier = Modifier.height(10.dp))

                        // Average & Status Row
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = if (isAmharic) "አማካይ ውጤት (Average)" else "Average Score",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format("%.1f%%", averageScore),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (averageScore >= 50) EthioGreen else EthioRed
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isAmharic) "የማለፊያ ሁኔታ (Status)" else "Academic Status",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val statusText = when {
                                    filteredGrades.isEmpty() -> if (isAmharic) "ውጤት አልተመዘገበም" else "No Data"
                                    averageScore >= 85 -> if (isAmharic) "በጣም ከፍተኛ ማዕረግ" else "High Distinction"
                                    averageScore >= 75 -> if (isAmharic) "ከፍተኛ ማዕረግ" else "Distinction"
                                    averageScore >= 50 -> if (isAmharic) "አልፏል (Promoted)" else "Promoted"
                                    else -> if (isAmharic) "ያልተጠናቀቀ/የወደቀ" else "Detained"
                                }
                                Text(
                                    text = statusText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (averageScore >= 50) EthioGreen else EthioRed
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isAmharic) "የተመዘገቡ የትምህርት ዓይነቶች (${filteredGrades.size})" else "Registered Subjects (${filteredGrades.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (filteredGrades.isEmpty()) {
                    EmptyStateCard(
                        message = if (isAmharic) "ለዚህ ሴሚስተር የተመዘገበ ውጤት የለም። ለመመዝገብ + ይጫኑ" else "No grades recorded for this semester. Click + to add"
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredGrades, key = { it.id }) { gradeItem ->
                            val isMySubject = isTeacherRole && assignedSubjectNames.any {
                                gradeItem.subjectName.contains(it, ignoreCase = true) || it.contains(gradeItem.subjectName, ignoreCase = true)
                            }
                            val canManage = (isAdmin || isMySubject) && !isStudent

                            GradeCardItem(
                                grade = gradeItem,
                                isAmharic = isAmharic,
                                canManage = canManage,
                                isMySubject = isMySubject,
                                isAdmin = isAdmin,
                                assessmentScheme = assessmentScheme,
                                onDelete = { gradeToDelete = gradeItem }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }

        val canAddGrade = (isAdmin || (isTeacherRole && assignedSubjectNames.isNotEmpty())) && !isStudent
        if (selectedStudent != null && canAddGrade) {
            FloatingActionButton(
                onClick = onAddGradeClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("fab_add_grade")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isAmharic) "ውጤት መዝግብ" else "Add Grade"
                )
            }
        }
    }

    // Delete Confirmation
    gradeToDelete?.let { gradeItem ->
        AlertDialog(
            onDismissRequest = { gradeToDelete = null },
            title = {
                Text(if (isAmharic) "ውጤት ሰርዝ" else "Delete Grade")
            },
            text = {
                Text(
                    if (isAmharic)
                        "የ '${gradeItem.subjectName}' ውጤት ይሰረዝ?"
                    else
                        "Delete grade record for '${gradeItem.subjectName}'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteGradeClick(gradeItem)
                        gradeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EthioRed)
                ) {
                    Text(if (isAmharic) "ሰርዝ" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { gradeToDelete = null }) {
                    Text(if (isAmharic) "ይቅር" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun GradeCardItem(
    grade: StudentGrade,
    isAmharic: Boolean,
    canManage: Boolean = true,
    isMySubject: Boolean = false,
    isAdmin: Boolean = true,
    assessmentScheme: AssessmentScheme = AssessmentScheme.MOE_STANDARD,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = grade.subjectName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (isMySubject && !isAdmin) {
                        Text(
                            text = if (isAmharic) "✓ የእርስዎ የት/ት ዓይነት (የተፈቀደ)" else "✓ Your Subject (Authorized)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EthioGreen
                        )
                    } else if (!canManage) {
                        Text(
                            text = if (isAmharic) "🔒 በሌላ መምህር የተሞላ (የተቆለፈ)" else "🔒 Managed by Subject Teacher (Locked)",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (grade.totalScore >= 50) EthioGreen.copy(alpha = 0.15f) else EthioRed.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${grade.letterGrade} (${String.format("%.1f", grade.totalScore)}/100)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (grade.totalScore >= 50) EthioGreen else EthioRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    if (canManage) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = EthioRed,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    } else {
                        Box(modifier = Modifier.padding(start = 6.dp)) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color.Gray.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Score breakdown row with dynamic labels from AssessmentScheme
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                ScoreColumn(label = "${assessmentScheme.test1Label} (${assessmentScheme.test1Weight.toInt()}%)", score = grade.quizScore)
                ScoreColumn(label = "${assessmentScheme.assignmentLabel} (${assessmentScheme.assignmentWeight.toInt()}%)", score = grade.assignmentScore)
                ScoreColumn(label = "${assessmentScheme.midExamLabel} (${assessmentScheme.midExamWeight.toInt()}%)", score = grade.midExamScore)
                ScoreColumn(label = "${assessmentScheme.finalExamLabel} (${assessmentScheme.finalExamWeight.toInt()}%)", score = grade.finalExamScore)
            }
        }
    }
}

@Composable
private fun ScoreColumn(label: String, score: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = String.format("%.1f", score),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeFormDialog(
    student: Student,
    subjects: List<Subject>,
    isAmharic: Boolean,
    assignedSubjectNames: List<String> = emptyList(),
    assessmentScheme: AssessmentScheme = AssessmentScheme.MOE_STANDARD,
    onDismiss: () -> Unit,
    onSave: (StudentGrade) -> Unit
) {
    val allowedSubjects = remember(subjects, assignedSubjectNames) {
        if (assignedSubjectNames.isEmpty()) subjects
        else {
            val filtered = subjects.filter { s ->
                assignedSubjectNames.any { asSub ->
                    s.name.contains(asSub, ignoreCase = true) || asSub.contains(s.name, ignoreCase = true)
                }
            }
            if (filtered.isEmpty()) subjects else filtered
        }
    }
    var selectedSubject by remember { mutableStateOf(allowedSubjects.firstOrNull()) }
    var semester by remember { mutableStateOf("1ኛ ሴሚስተር") }
    var quiz by remember { mutableStateOf("") }
    var assignment by remember { mutableStateOf("") }
    var midExam by remember { mutableStateOf("") }
    var finalExam by remember { mutableStateOf("") }
    var subjectExpanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    val handleSave = {
        if (selectedSubject == null) {
            errorMsg = if (isAmharic) "የትምህርት ዓይነት ይምረጡ" else "Select subject"
        } else {
            val q = quiz.toFloatOrNull() ?: 0f
            val a = assignment.toFloatOrNull() ?: 0f
            val m = midExam.toFloatOrNull() ?: 0f
            val f = finalExam.toFloatOrNull() ?: 0f

            val maxQ = assessmentScheme.test1Weight
            val maxA = assessmentScheme.assignmentWeight
            val maxM = assessmentScheme.midExamWeight
            val maxF = assessmentScheme.finalExamWeight

            if (q > maxQ || a > maxA || m > maxM || f > maxF) {
                errorMsg = if (isAmharic)
                    "እባክዎ ትክክለኛ የነጥብ ወሰን ያስገቡ (ከፍተኛ: ${maxQ.toInt()}፣ ${maxA.toInt()}፣ ${maxM.toInt()}፣ ${maxF.toInt()})"
                else
                    "Scores exceed maximum limits (${maxQ.toInt()}, ${maxA.toInt()}, ${maxM.toInt()}, ${maxF.toInt()})"
            } else {
                val gradeRecord = StudentGrade(
                    studentId = student.id,
                    studentName = student.fullName,
                    grade = student.grade,
                    section = student.section,
                    subjectName = selectedSubject!!.name,
                    semester = semester,
                    academicYear = student.academicYear,
                    quizScore = q,
                    assignmentScore = a,
                    midExamScore = m,
                    finalExamScore = f
                )
                onSave(gradeRecord)
            }
        }
    }

    BackHandler(onBack = onDismiss)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isAmharic) "የትምህርት ውጤት መዝግብ" else "Record Student Grade",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        Button(
                            onClick = { handleSave() },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("btn_save_grade_top")
                        ) {
                            Text(
                                text = if (isAmharic) "መዝግብ" else "Save",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) { innerPadding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${student.fullName} (${student.grade}-${student.section})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                if (errorMsg.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EthioRed.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMsg,
                            color = EthioRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Subject picker
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = !subjectExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedSubject?.name ?: (if (isAmharic) "የትምህርት ዓይነት ምረጥ" else "Select Subject"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isAmharic) "የትምህርት ዓይነት *" else "Subject *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        allowedSubjects.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.name) },
                                onClick = {
                                    selectedSubject = s
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                // Semester Selection Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.RadioButton(
                                selected = semester == "1ኛ ሴሚስተር",
                                onClick = { semester = "1ኛ ሴሚስተር" }
                            )
                            Text(if (isAmharic) "1ኛ ሴሚስተር" else "Sem 1", fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.RadioButton(
                                selected = semester == "2ኛ ሴሚስተር",
                                onClick = { semester = "2ኛ ሴሚስተር" }
                            )
                            Text(if (isAmharic) "2ኛ ሴሚስተር" else "Sem 2", fontSize = 13.sp)
                        }
                    }
                }

                // Component 1 (Quiz / Test 1)
                OutlinedTextField(
                    value = quiz,
                    onValueChange = { quiz = it },
                    label = { Text("${assessmentScheme.test1Label} (ከ ${assessmentScheme.test1Weight.toInt()})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Component 2 (Assignment / Homework)
                OutlinedTextField(
                    value = assignment,
                    onValueChange = { assignment = it },
                    label = { Text("${assessmentScheme.assignmentLabel} (ከ ${assessmentScheme.assignmentWeight.toInt()})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Component 3 (Mid Exam)
                OutlinedTextField(
                    value = midExam,
                    onValueChange = { midExam = it },
                    label = { Text("${assessmentScheme.midExamLabel} (ከ ${assessmentScheme.midExamWeight.toInt()})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Component 4 (Final Exam)
                OutlinedTextField(
                    value = finalExam,
                    onValueChange = { finalExam = it },
                    label = { Text("${assessmentScheme.finalExamLabel} (ከ ${assessmentScheme.finalExamWeight.toInt()})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { handleSave() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_save_grade")
                ) {
                    Text(
                        text = if (isAmharic) "ውጤቱን መዝግብ" else "Save Grade",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
