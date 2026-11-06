package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.example.data.model.Student
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FilterChipRow
import com.example.ui.components.PhotoPickerAvatar
import com.example.ui.components.SchoolSearchBar
import com.example.ui.components.UserAvatar
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.EthioRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    students: List<Student>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    gradeFilter: String,
    onGradeFilterChange: (String) -> Unit,
    sectionFilter: String,
    onSectionFilterChange: (String) -> Unit,
    availableGrades: List<String>,
    formGradeOptions: List<String>,
    sectionOptions: List<String>,
    isAmharic: Boolean,
    onAddStudentClick: () -> Unit,
    onEditStudentClick: (Student) -> Unit,
    onDeleteStudentClick: (Student) -> Unit,
    onViewGradesClick: (Student) -> Unit,
    onOpenImportExportClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedStudentForDetail by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }
    var isTableView by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("students_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            SchoolSearchBar(
                query = searchQuery,
                onQueryChange = onSearchChange,
                placeholder = if (isAmharic) "በተማሪ ስም፣ መታወቂያ ወይም ስልክ ፈልግ..." else "Search by name, ID or phone...",
                testTag = "student_search_input"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Grade Filter Chips
            Text(
                text = if (isAmharic) "የክፍል ደረጃ ምረጥ:" else "Filter by Grade:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilterChipRow(
                options = availableGrades,
                selectedOption = gradeFilter,
                onSelectOption = onGradeFilterChange,
                testTagPrefix = "grade_filter"
            )

            // Section Filter Chips
            FilterChipRow(
                options = listOf("ሁሉም") + sectionOptions,
                selectedOption = sectionFilter,
                onSelectOption = onSectionFilterChange,
                testTagPrefix = "section_filter"
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isAmharic) "ተማሪዎች: ${students.size}" else "Students: ${students.size}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // View Mode Toggle (ሰንጠረዥ vs ካርዶች)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isTableView) NavyPrimary else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { isTableView = true }
                                    .testTag("btn_view_table")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TableChart,
                                        contentDescription = "Table View",
                                        tint = if (isTableView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isAmharic) "ሰንጠረዥ" else "Table",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTableView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (!isTableView) NavyPrimary else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { isTableView = false }
                                    .testTag("btn_view_cards")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ViewAgenda,
                                        contentDescription = "Card View",
                                        tint = if (!isTableView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isAmharic) "ካርድ" else "Cards",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isTableView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenImportExportClick() }
                        .testTag("btn_students_import_export")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "📥", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAmharic) "ከExcel / CSV አስመጣ" else "Import Excel/CSV",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                }
            }

            if (students.isEmpty()) {
                EmptyStateCard(
                    message = if (isAmharic) "ምንም የተመዘገበ ተማሪ አልተገኘም" else "No students match this filter"
                )
            } else if (isTableView) {
                // Beautiful Table View representation
                StudentsDataTable(
                    students = students,
                    isAmharic = isAmharic,
                    onStudentClick = { selectedStudentForDetail = it },
                    onEditStudent = onEditStudentClick,
                    onDeleteStudent = { studentToDelete = it },
                    onViewGrades = onViewGradesClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(students, key = { it.id }) { student ->
                        StudentCardItem(
                            student = student,
                            isAmharic = isAmharic,
                            onClick = { selectedStudentForDetail = student },
                            onEdit = { onEditStudentClick(student) },
                            onDelete = { studentToDelete = student },
                            onGrades = { onViewGradesClick(student) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddStudentClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_student")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isAmharic) "ተማሪ መዝግብ" else "Add Student"
            )
        }
    }

    // Detail Dialog
    selectedStudentForDetail?.let { student ->
        StudentDetailDialog(
            student = student,
            isAmharic = isAmharic,
            onDismiss = { selectedStudentForDetail = null },
            onEdit = {
                selectedStudentForDetail = null
                onEditStudentClick(student)
            },
            onViewGrades = {
                selectedStudentForDetail = null
                onViewGradesClick(student)
            }
        )
    }

    // Delete Confirmation Dialog
    studentToDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = {
                Text(if (isAmharic) "ተማሪ ሰርዝ" else "Delete Student")
            },
            text = {
                Text(
                    if (isAmharic)
                        "የተማሪውን '${student.fullName}' ሙሉ መረጃ ከሲስተሙ መሰረዝ ይፈልጋሉ?"
                    else
                        "Are you sure you want to delete student '${student.fullName}'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteStudentClick(student)
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EthioRed)
                ) {
                    Text(if (isAmharic) "ሰርዝ" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text(if (isAmharic) "ይቅር" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun StudentCardItem(
    student: Student,
    isAmharic: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onGrades: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("student_card_${student.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Student Avatar (with photo support)
                UserAvatar(
                    photoUri = student.photoUri,
                    name = student.fullName,
                    gender = student.gender,
                    size = 46.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${student.grade} - ${student.section}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NavyPrimary
                        ) {
                            Text(
                                text = student.studentIdNumber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EthioGreen.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = student.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EthioGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Parent & Contact line
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${student.parentName} (${student.parentPhone})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action row
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onGrades,
                    modifier = Modifier.testTag("btn_grades_${student.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAmharic) "ውጤት" else "Grades",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_edit_${student.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_delete_${student.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = EthioRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailDialog(
    student: Student,
    isAmharic: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onViewGrades: () -> Unit
) {
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
                            text = if (isAmharic) "የተማሪ ሙሉ መረጃ" else "Student Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Student Profile Header with Photo
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        UserAvatar(
                            photoUri = student.photoUri,
                            name = student.fullName,
                            gender = student.gender,
                            size = 80.dp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = student.fullName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${student.studentIdNumber} • ${student.grade} (${student.section})",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailRow(label = if (isAmharic) "ሙሉ ስም:" else "Full Name:", value = student.fullName)
                        DetailRow(label = if (isAmharic) "መታወቂያ ቁጥር:" else "Student ID:", value = student.studentIdNumber)
                        DetailRow(label = if (isAmharic) "ጾታ:" else "Gender:", value = student.gender)
                        DetailRow(label = if (isAmharic) "ዕድሜ:" else "Age:", value = "${student.age}")
                        DetailRow(label = if (isAmharic) "ክፍል እና ሴክሽን:" else "Grade & Section:", value = "${student.grade} - ${student.section}")
                        DetailRow(label = if (isAmharic) "የትምህርት ዘመን:" else "Academic Year:", value = student.academicYear)
                        DetailRow(label = if (isAmharic) "የወላጅ/አሳዳጊ ስም:" else "Parent Name:", value = student.parentName)
                        DetailRow(label = if (isAmharic) "የወላጅ ስልክ:" else "Parent Phone:", value = student.parentPhone)
                        DetailRow(label = if (isAmharic) "አድራሻ:" else "Address:", value = student.address)
                        DetailRow(label = if (isAmharic) "ሁኔታ:" else "Status:", value = student.status)
                        if (student.specialNeeds.isNotBlank()) {
                            DetailRow(label = if (isAmharic) "ልዩ ፍላጎት/ማስታወሻ:" else "Special Needs:", value = student.specialNeeds)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onViewGrades,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Assessment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isAmharic) "የተማሪውን የውጤት ካርድ እይ" else "View Student Report Card", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isAmharic) "የተማሪውን መረጃ አስተካክል" else "Edit Profile", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormDialog(
    initialStudent: Student?,
    availableGrades: List<String>,
    availableSections: List<String>,
    isAmharic: Boolean,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var fullName by remember { mutableStateOf(initialStudent?.fullName ?: "") }
    var photoUri by remember { mutableStateOf(initialStudent?.photoUri ?: "") }
    var studentIdNumber by remember {
        mutableStateOf(
            initialStudent?.studentIdNumber ?: "ET-2017-${(100..999).random()}"
        )
    }
    var gender by remember { mutableStateOf(initialStudent?.gender ?: "ወንድ") }
    var age by remember { mutableStateOf(initialStudent?.age?.toString() ?: "15") }
    var grade by remember { mutableStateOf(initialStudent?.grade ?: availableGrades.firstOrNull() ?: "9ኛ ክፍል") }
    var section by remember { mutableStateOf(initialStudent?.section ?: "A") }
    var parentName by remember { mutableStateOf(initialStudent?.parentName ?: "") }
    var parentPhone by remember { mutableStateOf(initialStudent?.parentPhone ?: "09") }
    var address by remember { mutableStateOf(initialStudent?.address ?: "") }
    var specialNeeds by remember { mutableStateOf(initialStudent?.specialNeeds ?: "") }

    var gradeExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }

    var errorMsg by remember { mutableStateOf("") }

    val handleSave = {
        if (fullName.isBlank()) {
            errorMsg = if (isAmharic) "እባክዎ የተማሪውን ሙሉ ስም ያስገቡ" else "Please enter full name"
        } else if (studentIdNumber.isBlank()) {
            errorMsg = if (isAmharic) "የተማሪ መታወቂያ ቁጥር ያስፈልጋል" else "Student ID required"
        } else {
            val parsedAge = age.toIntOrNull() ?: 15
            val newStudent = (initialStudent ?: Student(
                studentIdNumber = studentIdNumber,
                fullName = fullName,
                gender = gender,
                age = parsedAge,
                grade = grade,
                section = section,
                parentName = parentName.ifBlank { "አልተገለጸም" },
                parentPhone = parentPhone,
                address = address.ifBlank { "አዲስ አበባ" },
                specialNeeds = specialNeeds,
                photoUri = photoUri
            )).copy(
                fullName = fullName,
                studentIdNumber = studentIdNumber,
                gender = gender,
                age = parsedAge,
                grade = grade,
                section = section,
                parentName = parentName,
                parentPhone = parentPhone,
                address = address,
                specialNeeds = specialNeeds,
                photoUri = photoUri
            )
            onSave(newStudent)
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
                            text = if (initialStudent == null)
                                (if (isAmharic) "አዲስ ተማሪ ምዝገባ" else "Student Registration")
                            else
                                (if (isAmharic) "የተማሪ መረጃ ማስተካከያ" else "Edit Student Profile"),
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
                                .testTag("btn_save_student_top")
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

                // Student Photo Upload & Preview
                PhotoPickerAvatar(
                    photoUri = photoUri,
                    onPhotoSelected = { photoUri = it },
                    name = fullName,
                    gender = gender,
                    isAmharic = isAmharic,
                    size = 96.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(if (isAmharic) "የተማሪ ሙሉ ስም *" else "Student Full Name *") },
                    placeholder = { Text("አበበ ታደሰ ከበደ") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_name")
                )

                // Student ID
                OutlinedTextField(
                    value = studentIdNumber,
                    onValueChange = { studentIdNumber = it },
                    label = { Text(if (isAmharic) "የተማሪ መለያ ቁጥር *" else "Student ID *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_id")
                )

                // Gender Selection
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isAmharic) "ጾታ *" else "Gender *",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { gender = "ወንድ" }
                            ) {
                                RadioButton(
                                    selected = gender == "ወንድ" || gender == "Male",
                                    onClick = { gender = "ወንድ" }
                                )
                                Text(if (isAmharic) "ወንድ" else "Male", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(24.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { gender = "ሴት" }
                            ) {
                                RadioButton(
                                    selected = gender == "ሴት" || gender == "Female",
                                    onClick = { gender = "ሴት" }
                                )
                                Text(if (isAmharic) "ሴት" else "Female", fontSize = 14.sp)
                            }
                        }
                    }
                }

                // Age
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text(if (isAmharic) "ዕድሜ" else "Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Grade Dropdown
                ExposedDropdownMenuBox(
                    expanded = gradeExpanded,
                    onExpandedChange = { gradeExpanded = !gradeExpanded }
                ) {
                    OutlinedTextField(
                        value = grade,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isAmharic) "ክፍል *" else "Grade *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = gradeExpanded,
                        onDismissRequest = { gradeExpanded = false }
                    ) {
                        availableGrades.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = {
                                    grade = g
                                    gradeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Section Dropdown
                ExposedDropdownMenuBox(
                    expanded = sectionExpanded,
                    onExpandedChange = { sectionExpanded = !sectionExpanded }
                ) {
                    OutlinedTextField(
                        value = section,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isAmharic) "ክፍልፋይ/ሴክሽን *" else "Section *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = sectionExpanded,
                        onDismissRequest = { sectionExpanded = false }
                    ) {
                        availableSections.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    section = s
                                    sectionExpanded = false
                                }
                            )
                        }
                    }
                }

                // Parent Name
                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it },
                    label = { Text(if (isAmharic) "የወላጅ/አሳዳጊ ስም *" else "Parent Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Parent Phone
                OutlinedTextField(
                    value = parentPhone,
                    onValueChange = { parentPhone = it },
                    label = { Text(if (isAmharic) "የወላጅ ስልክ ቁጥር *" else "Parent Phone *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (isAmharic) "አድራሻ (ከተማ፣ ክ/ከተማ)" else "Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Special Needs
                OutlinedTextField(
                    value = specialNeeds,
                    onValueChange = { specialNeeds = it },
                    label = { Text(if (isAmharic) "ልዩ ፍላጎት / ተጨማሪ ማስታወሻ" else "Special Needs / Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Full-width Bottom Submit Button
                Button(
                    onClick = { handleSave() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_save_student")
                ) {
                    Text(
                        text = if (isAmharic) "መዝግብ / አስቀምጥ" else "Save Student",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun StudentsDataTable(
    students: List<Student>,
    isAmharic: Boolean,
    onStudentClick: (Student) -> Unit,
    onEditStudent: (Student) -> Unit,
    onDeleteStudent: (Student) -> Unit,
    onViewGrades: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    var sortColumn by remember { mutableStateOf<String?>("name") }
    var sortAscending by remember { mutableStateOf(true) }

    val sortedStudents = remember(students, sortColumn, sortAscending) {
        when (sortColumn) {
            "name" -> if (sortAscending) students.sortedBy { it.fullName } else students.sortedByDescending { it.fullName }
            "grade" -> if (sortAscending) students.sortedWith(compareBy({ it.grade }, { it.section })) else students.sortedWith(compareByDescending<Student> { it.grade }.thenByDescending { it.section })
            "id" -> if (sortAscending) students.sortedBy { it.studentIdNumber } else students.sortedByDescending { it.studentIdNumber }
            else -> students
        }
    }

    Card(
        modifier = modifier.testTag("students_data_table"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        val hScrollState = rememberScrollState()

        Column(modifier = Modifier.fillMaxSize()) {
            // Horizontal scroll container holding header and rows
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .horizontalScroll(hScrollState)
            ) {
                Column(modifier = Modifier.width(820.dp)) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyPrimary)
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAmharic) "ተ.ቁ" else "No.",
                            modifier = Modifier.width(44.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        // Full Name Header (Clickable Sort)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .width(190.dp)
                                .clickable {
                                    if (sortColumn == "name") {
                                        sortAscending = !sortAscending
                                    } else {
                                        sortColumn = "name"
                                        sortAscending = true
                                    }
                                }
                        ) {
                            Text(
                                text = if (isAmharic) "የተማሪው ሙሉ ስም" else "Student Full Name",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (sortColumn == "name") (if (sortAscending) "▲" else "▼") else "⇅",
                                color = if (sortColumn == "name") GoldAccent else Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // ID Header (Clickable Sort)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .width(90.dp)
                                .clickable {
                                    if (sortColumn == "id") {
                                        sortAscending = !sortAscending
                                    } else {
                                        sortColumn = "id"
                                        sortAscending = true
                                    }
                                }
                        ) {
                            Text(
                                text = if (isAmharic) "መለያ ቁጥር" else "ID",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (sortColumn == "id") (if (sortAscending) "▲" else "▼") else "⇅",
                                color = if (sortColumn == "id") GoldAccent else Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Grade Header (Clickable Sort)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .width(85.dp)
                                .clickable {
                                    if (sortColumn == "grade") {
                                        sortAscending = !sortAscending
                                    } else {
                                        sortColumn = "grade"
                                        sortAscending = true
                                    }
                                }
                        ) {
                            Text(
                                text = if (isAmharic) "ክፍል" else "Grade",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (sortColumn == "grade") (if (sortAscending) "▲" else "▼") else "⇅",
                                color = if (sortColumn == "grade") GoldAccent else Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (isAmharic) "ጾታ" else "Gender",
                            modifier = Modifier.width(60.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (isAmharic) "የወላጅ ስልክ" else "Parent Phone",
                            modifier = Modifier.width(120.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (isAmharic) "ሁኔታ" else "Status",
                            modifier = Modifier.width(75.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (isAmharic) "ተግባራት" else "Actions",
                            modifier = Modifier.width(156.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

                    // Table Body Rows
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(sortedStudents, key = { _, s -> s.id }) { index, student ->
                            val isEven = index % 2 == 0
                            val rowBg = if (isEven) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowBg)
                                    .clickable { onStudentClick(student) }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Index
                                Text(
                                    text = "${index + 1}",
                                    modifier = Modifier.width(44.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Name with mini avatar
                                Row(
                                    modifier = Modifier.width(190.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isFemale = student.gender.contains("ሴት") || student.gender.equals("F", ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(if (isFemale) Color(0xFFFCE7F3) else Color(0xFFDBEAFE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = student.fullName.take(1),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isFemale) Color(0xFFBE185D) else Color(0xFF1E40AF)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = student.fullName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // ID
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NavyPrimary,
                                    modifier = Modifier.width(90.dp)
                                ) {
                                    Text(
                                        text = student.studentIdNumber.ifBlank { student.id.toString() },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Grade + Section
                                Text(
                                    text = "${student.grade} - ${student.section}",
                                    modifier = Modifier.width(85.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Gender
                                val isFemale = student.gender.contains("ሴት") || student.gender.equals("F", ignoreCase = true)
                                Box(
                                    modifier = Modifier.width(60.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isFemale) Color(0xFFFCE7F3) else Color(0xFFDBEAFE)
                                    ) {
                                        Text(
                                            text = if (isFemale) "ሴት" else "ወንድ",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isFemale) Color(0xFFBE185D) else Color(0xFF1E40AF),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Phone
                                Text(
                                    text = student.parentPhone.ifBlank { "-" },
                                    modifier = Modifier.width(120.dp),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )

                                // Status
                                val isActive = student.status.contains("ንቁ") || student.status.equals("Active", ignoreCase = true)
                                Box(
                                    modifier = Modifier.width(75.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                    ) {
                                        Text(
                                            text = if (isActive) "ንቁ" else "የታገደ",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isActive) Color(0xFF15803D) else Color(0xFFB91C1C),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Action Buttons
                                Row(
                                    modifier = Modifier.width(156.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { onStudentClick(student) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = "ማየት",
                                            tint = NavyPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onEditStudent(student) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "ማረም",
                                            tint = GoldAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onViewGrades(student) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Assessment,
                                            contentDescription = "ውጤት",
                                            tint = EthioGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteStudent(student) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "ሰርዝ",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp)
                        }
                    }
                }
            }

            // Footer showing count
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAmharic) "በሰንጠረዡ የቀረቡ ተማሪዎች: ${students.size}" else "Students in table: ${students.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isAmharic) "↔ ወደ ጎን በማንሸራተት ሙሉውን ይመልከቱ" else "↔ Scroll horizontally to view all",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

