package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherWithAssignments
import com.example.data.model.TeacherWorkload
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SchoolSearchBar
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.EthioRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

/**
 * TeachersScreen: Manages Teacher records, profiles, and their specific assigned subjects/workloads.
 */
@Composable
fun TeachersScreen(
    teachers: List<Teacher>,
    workloads: List<TeacherWorkload>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    departmentFilter: String,
    onDepartmentFilterChange: (String) -> Unit,
    isAmharic: Boolean,
    onTeacherClick: (Teacher) -> Unit,
    onAddTeacherClick: () -> Unit,
    onEditTeacherClick: (Teacher) -> Unit,
    onDeleteTeacherClick: (Teacher) -> Unit,
    onAssignWorkloadClick: (Teacher) -> Unit,
    modifier: Modifier = Modifier
) {
    var teacherToDelete by remember { mutableStateOf<Teacher?>(null) }

    val departmentOptions = listOf(
        "ሁሉም",
        "የክፍል ተጠሪዎች",
        "የሂሳብ ት/ት ክፍል",
        "የቋንቋዎች ት/ት ክፍል",
        "የተፈጥሮ ሳይንስ",
        "የማህበራዊ ሳይንስ"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("teachers_screen")
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
                placeholder = if (isAmharic) "በመምህር ስም፣ የት/ት ዘርፍ ወይም መታወቂያ ፈልግ..." else "Search by teacher name, major or code...",
                testTag = "teacher_search_input"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Department & Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                departmentOptions.forEach { dept ->
                    val isSelected = departmentFilter == dept
                    val displayLabel = when (dept) {
                        "ሁሉም" -> if (isAmharic) "ሁሉም" else "All"
                        "የክፍል ተጠሪዎች" -> if (isAmharic) "⭐ የክፍል ተጠሪዎች" else "⭐ Homeroom"
                        "የሂሳብ ት/ት ክፍል" -> if (isAmharic) "ሂሳብ" else "Math"
                        "የቋንቋዎች ት/ት ክፍል" -> if (isAmharic) "ቋንቋዎች" else "Languages"
                        "የተፈጥሮ ሳይንስ" -> if (isAmharic) "ተፈጥሮ ሳይንስ" else "Science"
                        "የማህበራዊ ሳይንስ" -> if (isAmharic) "ማህበራዊ" else "Social"
                        else -> dept
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { onDepartmentFilterChange(dept) },
                        label = {
                            Text(
                                text = displayLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Summary Stats Pill Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = if (isAmharic) "ጠቅላላ መምህራን: ${teachers.size}" else "Total Teachers: ${teachers.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                val homeroomCount = teachers.count { it.isHomeroomTeacher }
                Text(
                    text = if (isAmharic) "የክፍል ተጠሪዎች: $homeroomCount" else "Homeroom: $homeroomCount",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (teachers.isEmpty()) {
                EmptyStateCard(
                    message = if (isAmharic) "ምንም የተመዘገበ መምህር አልተገኘም" else "No teachers registered"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(teachers, key = { it.id }) { teacher ->
                        val teacherWorkloads = workloads.filter { it.teacherId == teacher.id }

                        TeacherCardItem(
                            teacher = teacher,
                            workloads = teacherWorkloads,
                            isAmharic = isAmharic,
                            onClick = { onTeacherClick(teacher) },
                            onEdit = { onEditTeacherClick(teacher) },
                            onDelete = { teacherToDelete = teacher },
                            onAssign = { onAssignWorkloadClick(teacher) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddTeacherClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_teacher")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isAmharic) "መምህር መዝግብ" else "Add Teacher"
            )
        }
    }

    // Delete Confirmation Dialog
    teacherToDelete?.let { teacher ->
        AlertDialog(
            onDismissRequest = { teacherToDelete = null },
            title = {
                Text(if (isAmharic) "መምህር ሰርዝ" else "Delete Teacher")
            },
            text = {
                Text(
                    if (isAmharic)
                        "የመምህር '${teacher.fullName}' መረጃ እና የተመደቡባቸው የትምህርት ክፍለ-ጊዜዎች ከሲስተሙ ይሰረዙ?"
                    else
                        "Delete teacher '${teacher.fullName}' and their assigned workloads from system?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTeacherClick(teacher)
                        teacherToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EthioRed)
                ) {
                    Text(if (isAmharic) "ሰርዝ" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { teacherToDelete = null }) {
                    Text(if (isAmharic) "ይቅር" else "Cancel")
                }
            }
        )
    }
}

/**
 * TeacherCardItem: Displays comprehensive teacher summary, contact, status, homeroom,
 * and their specific assigned subjects as interactive visual chips with capacity indicators.
 */
@Composable
fun TeacherCardItem(
    teacher: Teacher,
    workloads: List<TeacherWorkload>,
    isAmharic: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAssign: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedSubjects by remember { mutableStateOf(false) }
    val totalPeriods = workloads.sumOf { it.periodsPerWeek }
    val maxPeriods = teacher.maxWeeklyPeriods.coerceAtLeast(1)
    val progress = (totalPeriods.toFloat() / maxPeriods.toFloat()).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("teacher_card_${teacher.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Avatar, Name, Title, Code, Qualification
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(NavyPrimary.copy(alpha = 0.12f))
                        .border(1.5.dp, NavyPrimary.copy(alpha = 0.3f), CircleShape)
                ) {
                    Text(
                        text = teacher.fullName.replace("አቶ ", "").replace("ወ/ሮ ", "").replace("ወ/ሪት ", "").take(1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = NavyPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = teacher.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${teacher.specialization} • ${teacher.teacherCode}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = teacher.qualification,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contact & Homeroom details
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
                    text = teacher.phone,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (teacher.email.isNotBlank()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = teacher.email,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Homeroom Badge if applicable
            if (teacher.isHomeroomTeacher && teacher.homeroomClass.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GoldAccent.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAmharic) "የክፍል ተጠሪ (Homeroom): ${teacher.homeroomClass}" else "Homeroom: ${teacher.homeroomClass}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // SPECIFIC ASSIGNED SUBJECTS & WORKLOAD SECTION
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isAmharic)
                                    "የተመደቡ የትምህርት ዓይነቶች (${workloads.size})"
                                else
                                    "Assigned Subjects (${workloads.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Workload text & toggle expand
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$totalPeriods/$maxPeriods " + (if (isAmharic) "ክ/ጊዜ" else "periods"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalPeriods > maxPeriods) EthioRed else EthioGreen
                            )
                            if (workloads.isNotEmpty()) {
                                IconButton(
                                    onClick = { expandedSubjects = !expandedSubjects },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (expandedSubjects) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Assigned Subjects",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Workload Capacity Progress Bar
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when {
                            totalPeriods > maxPeriods -> EthioRed
                            totalPeriods >= maxPeriods - 4 -> GoldAccent
                            else -> EthioGreen
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    // Specific Subject Chips
                    if (workloads.isEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isAmharic) "ምንም የተመደበ ትምህርት የለም (ሥራ ለመመደብ + ሥራ መድብ የሚለውን ይጫኑ)" else "No subjects assigned yet",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                        // Summary chips (showing top 3 or all if expanded)
                        val displayedWorkloads = if (expandedSubjects) workloads else workloads.take(3)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            displayedWorkloads.forEach { wl ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Book,
                                                contentDescription = null,
                                                tint = NavyPrimary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${wl.subjectName} (${wl.grade}-${wl.section})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = "${wl.periodsPerWeek} " + (if (isAmharic) "ክ/ጊዜ" else "periods"),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            if (!expandedSubjects && workloads.size > 3) {
                                Text(
                                    text = if (isAmharic) "+ ተጨማሪ ${workloads.size - 3} ክፍሎች..." else "+ ${workloads.size - 3} more classes...",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: View Details, Assign Subject, Edit, Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // View Details Button
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAmharic) "ዝርዝር & ትምህርቶች" else "Details & Subjects",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Assign Subject
                    Button(
                        onClick = onAssign,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isAmharic) "+ ሥራ መድብ" else "+ Assign",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Teacher",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Teacher",
                            tint = EthioRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * TeacherDetailDialog: Comprehensive Profile Sheet and Specific Subject/Assignment Manager.
 * Allows viewing all teacher information, contact details, calling, emailing,
 * viewing all assigned classes/subjects, adding new subject assignments, and deleting specific assignments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDetailDialog(
    teacher: Teacher,
    workloads: List<TeacherWorkload>,
    isAmharic: Boolean,
    onDismiss: () -> Unit,
    onEditTeacher: (Teacher) -> Unit,
    onDeleteTeacher: (Teacher) -> Unit,
    onAddAssignment: (Teacher) -> Unit,
    onEditAssignment: (TeacherWorkload) -> Unit,
    onDeleteAssignment: (TeacherWorkload) -> Unit
) {
    val context = LocalContext.current
    var workloadToDelete by remember { mutableStateOf<TeacherWorkload?>(null) }

    val totalPeriods = workloads.sumOf { it.periodsPerWeek }
    val maxPeriods = teacher.maxWeeklyPeriods.coerceAtLeast(1)
    val progress = (totalPeriods.toFloat() / maxPeriods.toFloat()).coerceIn(0f, 1f)

    BackHandler(onBack = onDismiss)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("teacher_detail_dialog"),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = teacher.fullName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isAmharic) "የመምህር ዝርዝር መረጃ እና የትምህርት ክፍፍል" else "Teacher Profile & Subject Assignments",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
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
                        IconButton(onClick = { onEditTeacher(teacher) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Teacher",
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Card 1: Teacher Bio & Profile Details
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(NavyPrimary.copy(alpha = 0.15f))
                                    .border(2.dp, NavyPrimary, CircleShape)
                            ) {
                                Text(
                                    text = teacher.fullName.replace("አቶ ", "").replace("ወ/ሮ ", "").replace("ወ/ሪት ", "").take(1),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = NavyPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = teacher.fullName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${teacher.specialization} • ${teacher.teacherCode}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = EthioGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = teacher.status,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EthioGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = teacher.qualification,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Details Grid
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow(
                                icon = Icons.Default.School,
                                label = if (isAmharic) "የትምህርት ክፍል (Department)" else "Department",
                                value = teacher.department
                            )
                            DetailRow(
                                icon = Icons.Default.Person,
                                label = if (isAmharic) "ፆታ" else "Gender",
                                value = teacher.gender
                            )
                            DetailRow(
                                icon = Icons.Default.CalendarToday,
                                label = if (isAmharic) "የተቀጠሩበት ዓመት" else "Joined Year",
                                value = teacher.joinedYear
                            )
                            if (teacher.isHomeroomTeacher) {
                                DetailRow(
                                    icon = Icons.Default.Star,
                                    label = if (isAmharic) "የክፍል ተጠሪ (Homeroom)" else "Homeroom Class",
                                    value = teacher.homeroomClass,
                                    valueColor = GoldAccent
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Contact Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${teacher.phone}"))
                                    context.startActivity(dialIntent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EthioGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(teacher.phone, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            if (teacher.email.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        val mailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${teacher.email}"))
                                        context.startActivity(mailIntent)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isAmharic) "ኢሜይል ላክ" else "Email", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Card 2: Workload & Weekly Capacity Progress
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAmharic) "የሳምንት የሥራ ጫና (Workload Capacity)" else "Weekly Teaching Capacity",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "$totalPeriods / $maxPeriods " + (if (isAmharic) "ክ/ጊዜ" else "periods"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (totalPeriods > maxPeriods) EthioRed else NavyPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when {
                                totalPeriods > maxPeriods -> EthioRed
                                totalPeriods >= maxPeriods - 4 -> GoldAccent
                                else -> EthioGreen
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (totalPeriods > maxPeriods)
                                    (if (isAmharic) "⚠️ ከሳምንታዊ አቅም በላይ ነው!" else "⚠️ Exceeds weekly capacity!")
                                else
                                    (if (isAmharic) "${(progress * 100).toInt()}% ተመድቧል" else "${(progress * 100).toInt()}% Allocated"),
                                fontSize = 11.sp,
                                color = if (totalPeriods > maxPeriods) EthioRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = if (isAmharic) "${workloads.size} የተመደቡ ክፍሎች" else "${workloads.size} Assigned classes",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Card 3: Specific Subject Assignments List
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isAmharic) "የተመደቡ የትምህርት ዓይነቶች" else "Assigned Subjects & Classes",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Add Assignment Button
                            Button(
                                onClick = { onAddAssignment(teacher) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAmharic) "+ ትምህርት መድብ" else "+ Assign",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (workloads.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (isAmharic) "ለዚህ መምህር ምንም የተመደበ ትምህርት የለም" else "No subjects assigned to this teacher",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { onAddAssignment(teacher) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (isAmharic) "አሁን የትምህርት ክፍፍል መድብ" else "Assign Subject Now")
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                workloads.forEach { wl ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.padding(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(NavyPrimary.copy(alpha = 0.12f))
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Book,
                                                        contentDescription = null,
                                                        tint = NavyPrimary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column {
                                                    Text(
                                                        text = wl.subjectName,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "${wl.grade} - ${if (isAmharic) "ክፍል" else "Section"} ${wl.section} • ${wl.periodsPerWeek} " + (if (isAmharic) "ክፍለ-ጊዜ/ሳምንት" else "periods/wk"),
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    if (wl.daysAllotted.isNotBlank()) {
                                                        Text(
                                                            text = "📅 ${wl.daysAllotted}",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }

                                            // Action Buttons for this specific assignment
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { onEditAssignment(wl) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit Assignment",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { workloadToDelete = wl },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Assignment",
                                                        tint = EthioRed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Delete Assignment Confirmation Dialog
    workloadToDelete?.let { wl ->
        AlertDialog(
            onDismissRequest = { workloadToDelete = null },
            title = {
                Text(if (isAmharic) "የትምህርት ክፍፍል ሰርዝ" else "Unassign Subject")
            },
            text = {
                Text(
                    if (isAmharic)
                        "የመምህር '${wl.teacherName}' የ'${wl.subjectName} (${wl.grade}-${wl.section})' ትምህርት ምደባ ከሲስተሙ ይሰረዝ?"
                    else
                        "Remove '${wl.subjectName} (${wl.grade}-${wl.section})' assignment from ${wl.teacherName}?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAssignment(wl)
                        workloadToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EthioRed)
                ) {
                    Text(if (isAmharic) "ሰርዝ" else "Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { workloadToDelete = null }) {
                    Text(if (isAmharic) "ይቅር" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (valueColor != Color.Unspecified) valueColor else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * TeacherFormDialog: Full-Screen Form to Add or Edit Teacher information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherFormDialog(
    initialTeacher: Teacher?,
    isAmharic: Boolean,
    onDismiss: () -> Unit,
    onSave: (Teacher) -> Unit
) {
    var title by remember { mutableStateOf(initialTeacher?.title ?: "አቶ") }
    var fullName by remember { mutableStateOf(initialTeacher?.fullName ?: "") }
    var teacherCode by remember {
        mutableStateOf(
            initialTeacher?.teacherCode ?: "T-${(101..999).random()}"
        )
    }
    var gender by remember { mutableStateOf(initialTeacher?.gender ?: "ወንድ") }
    var phone by remember { mutableStateOf(initialTeacher?.phone ?: "09") }
    var email by remember { mutableStateOf(initialTeacher?.email ?: "") }
    var qualification by remember { mutableStateOf(initialTeacher?.qualification ?: "ባችለር ዲግሪ (BA/BSc)") }
    var specialization by remember { mutableStateOf(initialTeacher?.specialization ?: "") }
    var department by remember { mutableStateOf(initialTeacher?.department ?: "የሂሳብ ት/ት ክፍል") }
    var maxWeeklyPeriods by remember { mutableStateOf(initialTeacher?.maxWeeklyPeriods?.toString() ?: "20") }
    var joinedYear by remember { mutableStateOf(initialTeacher?.joinedYear ?: "2015 ዓ.ም") }
    var isHomeroom by remember { mutableStateOf(initialTeacher?.isHomeroomTeacher ?: false) }
    var homeroomClass by remember { mutableStateOf(initialTeacher?.homeroomClass ?: "9ኛ ክፍል-A") }

    val titles = listOf("አቶ", "ወ/ሮ", "ወ/ሪት", "ዶ/ር")
    val qualifications = listOf(
        "ሰርተፊኬት",
        "ዲፕሎማ (Diploma)",
        "ባችለር ዲግሪ (BA/BSc)",
        "ማስተርስ (MA/MSc)",
        "ዶክተሬት (PhD)"
    )
    val departments = listOf(
        "የሂሳብ ት/ት ክፍል",
        "የቋንቋዎች ት/ት ክፍል",
        "የተፈጥሮ ሳይንስ ት/ት ክፍል",
        "የማህበራዊ ሳይንስ ት/ት ክፍል",
        "የኪነ-ጥበብና ስፖርት",
        "አጠቃላይ የትምህርት ክፍል"
    )

    var qualExpanded by remember { mutableStateOf(false) }
    var deptExpanded by remember { mutableStateOf(false) }
    var titleExpanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    val handleSave = {
        if (fullName.isBlank()) {
            errorMsg = if (isAmharic) "እባክዎ የመምህሩን ስም ያስገቡ" else "Please enter teacher name"
        } else if (specialization.isBlank()) {
            errorMsg = if (isAmharic) "የትምህርት ዘርፍ ያስገቡ" else "Please enter specialization"
        } else {
            val maxPeriodsInt = maxWeeklyPeriods.toIntOrNull() ?: 20
            val newTeacher = (initialTeacher ?: Teacher(
                teacherCode = teacherCode,
                fullName = fullName,
                title = title,
                gender = gender,
                phone = phone,
                email = email,
                qualification = qualification,
                specialization = specialization,
                department = department,
                joinedYear = joinedYear,
                maxWeeklyPeriods = maxPeriodsInt,
                isHomeroomTeacher = isHomeroom,
                homeroomClass = if (isHomeroom) homeroomClass else ""
            )).copy(
                teacherCode = teacherCode,
                fullName = fullName,
                title = title,
                gender = gender,
                phone = phone,
                email = email,
                qualification = qualification,
                specialization = specialization,
                department = department,
                joinedYear = joinedYear,
                maxWeeklyPeriods = maxPeriodsInt,
                isHomeroomTeacher = isHomeroom,
                homeroomClass = if (isHomeroom) homeroomClass else ""
            )
            onSave(newTeacher)
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
                            text = if (initialTeacher == null)
                                (if (isAmharic) "አዲስ መምህር ምዝገባ" else "Teacher Registration")
                            else
                                (if (isAmharic) "የመምህር መረጃ ማስተካከያ" else "Edit Teacher Profile"),
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
                                .testTag("btn_save_teacher_top")
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

                // Title and Gender Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Title Dropdown
                    ExposedDropdownMenuBox(
                        expanded = titleExpanded,
                        onExpandedChange = { titleExpanded = !titleExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isAmharic) "ማዕረግ" else "Title") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = titleExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = titleExpanded,
                            onDismissRequest = { titleExpanded = false }
                        ) {
                            titles.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t) },
                                    onClick = {
                                        title = t
                                        titleExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Gender Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        listOf("ወንድ", "ሴት").forEach { g ->
                            FilterChip(
                                selected = gender == g,
                                onClick = { gender = g },
                                label = { Text(if (isAmharic) g else if (g == "ወንድ") "Male" else "Female") },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }

                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(if (isAmharic) "የመምህር ሙሉ ስም *" else "Full Name *") },
                    placeholder = { Text("ሙሉጌታ ታከለ") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_teacher_name")
                )

                // Teacher Code
                OutlinedTextField(
                    value = teacherCode,
                    onValueChange = { teacherCode = it },
                    label = { Text(if (isAmharic) "የመምህር መታወቂያ ኮድ *" else "Teacher ID Code *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Specialization
                OutlinedTextField(
                    value = specialization,
                    onValueChange = { specialization = it },
                    label = { Text(if (isAmharic) "ዋና የትምህርት ዘርፍ (Major/Subject) *" else "Specialization/Subject *") },
                    placeholder = { Text("ሂሳብ፣ እንግሊዝኛ፣ ፊዚክስ...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Department Dropdown
                ExposedDropdownMenuBox(
                    expanded = deptExpanded,
                    onExpandedChange = { deptExpanded = !deptExpanded }
                ) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isAmharic) "የትምህርት ክፍል (Department)" else "Department") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = deptExpanded,
                        onDismissRequest = { deptExpanded = false }
                    ) {
                        departments.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = {
                                    department = d
                                    deptExpanded = false
                                }
                            )
                        }
                    }
                }

                // Qualification Dropdown
                ExposedDropdownMenuBox(
                    expanded = qualExpanded,
                    onExpandedChange = { qualExpanded = !qualExpanded }
                ) {
                    OutlinedTextField(
                        value = qualification,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isAmharic) "የትምህርት ዝግጅት (Qualification)" else "Qualification") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = qualExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = qualExpanded,
                        onDismissRequest = { qualExpanded = false }
                    ) {
                        qualifications.forEach { q ->
                            DropdownMenuItem(
                                text = { Text(q) },
                                onClick = {
                                    qualification = q
                                    qualExpanded = false
                                }
                            )
                        }
                    }
                }

                // Max Weekly Teaching Periods & Joined Year Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = maxWeeklyPeriods,
                        onValueChange = { maxWeeklyPeriods = it },
                        label = { Text(if (isAmharic) "ከፍተኛ ሳምንታዊ ክ/ጊዜ" else "Max Weekly Periods") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = joinedYear,
                        onValueChange = { joinedYear = it },
                        label = { Text(if (isAmharic) "የተቀጠሩበት ዓመት" else "Joined Year") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isAmharic) "ስልክ ቁጥር *" else "Phone Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(if (isAmharic) "ኢሜይል (አማራጭ)" else "Email (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Homeroom Checkbox Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isHomeroom = !isHomeroom }
                        ) {
                            Checkbox(
                                checked = isHomeroom,
                                onCheckedChange = { isHomeroom = it }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAmharic) "የክፍል ተጠሪ (Homeroom) መምህር ነው" else "Is Homeroom Teacher",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (isHomeroom) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = homeroomClass,
                                onValueChange = { homeroomClass = it },
                                label = { Text(if (isAmharic) "የክፍል ተጠሪ የሆነበት ክፍል (ምሳሌ: 9ኛ ክፍል-A)" else "Homeroom Class (e.g. 9-A)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { handleSave() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_save_teacher")
                ) {
                    Text(
                        text = if (isAmharic) "መዝግብ / አስቀምጥ" else "Save Teacher",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
