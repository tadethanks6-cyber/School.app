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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
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
import com.example.data.model.Subject
import com.example.data.model.Teacher
import com.example.data.model.TeacherWorkload
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.EthioRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

@Composable
fun WorkloadScreen(
    workloads: List<TeacherWorkload>,
    teachers: List<Teacher>,
    subjects: List<Subject>,
    isAmharic: Boolean,
    isAdmin: Boolean = true,
    currentTeacher: Teacher? = null,
    onAddWorkloadClick: () -> Unit,
    onEditWorkloadClick: (TeacherWorkload) -> Unit,
    onDeleteWorkloadClick: (TeacherWorkload) -> Unit,
    modifier: Modifier = Modifier
) {
    val isTeacherRole = !isAdmin && currentTeacher != null
    val effectiveWorkloads = remember(workloads, isTeacherRole, currentTeacher) {
        if (!isTeacherRole) workloads
        else {
            val my = workloads.filter { it.teacherId == currentTeacher?.id || it.teacherName.contains(currentTeacher?.fullName ?: "") || (currentTeacher?.fullName ?: "").contains(it.teacherName) }
            if (my.isEmpty()) workloads else my
        }
    }
    var selectedViewMode by remember { mutableStateOf(if (isTeacherRole) 1 else 0) } // 0: By Teacher, 1: All Allotments
    var workloadToDelete by remember { mutableStateOf<TeacherWorkload?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("workload_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Role Confidential Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isTeacherRole) Color(0xFF6D28D9).copy(alpha = 0.10f) else NavyPrimary.copy(alpha = 0.08f)
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
                        imageVector = if (isTeacherRole) Icons.Default.Lock else Icons.Default.Security,
                        contentDescription = null,
                        tint = if (isTeacherRole) Color(0xFF6D28D9) else NavyPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isTeacherRole)
                                "🔒 የመምህር ${currentTeacher?.fullName ?: ""} የክፍለ-ጊዜ ሰንጠረዥ"
                            else
                                "👑 የአድሚን የሥራ ክፍፍል ማስተዳደሪያ (Workload Manager)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isTeacherRole) Color(0xFF5B21B6) else NavyPrimary
                        )
                        Text(
                            text = if (isTeacherRole)
                                "የእርስዎ ሳምንታዊ የትምህርት ክፍለ-ጊዜዎች ዝርዝር። ምደባውን መቀየር የሚችለው የት/ቤቱ አድሚን ብቻ ነው።"
                            else
                                "ለመምህራን ክፍለ-ጊዜና ክፍል የመመደብ፣ የማሻሻል እና የመቆጣጠር ሙሉ ስልጣን አልዎት።",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tab View Mode
            TabRow(
                selectedTabIndex = selectedViewMode,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedViewMode == 0,
                    onClick = { selectedViewMode = 0 },
                    text = {
                        Text(
                            if (isAmharic) "በመምህራን ማጠቃለያ" else "By Teacher",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedViewMode == 1,
                    onClick = { selectedViewMode = 1 },
                    text = {
                        Text(
                            if (isAmharic) "ዝርዝር ክፍፍል" else "Detailed List",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val totalWeeklyPeriods = workloads.sumOf { it.periodsPerWeek }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            text = if (isAmharic) "ጠቅላላ የተመደበ ክፍለ-ጊዜ" else "Total Allocated Periods",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$totalWeeklyPeriods " + (if (isAmharic) "ክፍለ-ጊዜ በሳምንት" else "periods/week"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "${workloads.size} " + (if (isAmharic) "ክፍሎች" else "assignments"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (workloads.isEmpty()) {
                EmptyStateCard(
                    message = if (isAmharic) "ምንም የተመደበ የሥራ ክፍፍል የለም። ለመመደብ + ይጫኑ" else "No workload allocations found. Press + to assign"
                )
            } else if (selectedViewMode == 0) {
                // Grouped by Teacher
                val groupedByTeacher = workloads.groupBy { it.teacherId }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(teachers, key = { it.id }) { teacher ->
                        val teacherLoads = groupedByTeacher[teacher.id] ?: emptyList()
                        val teacherTotalPeriods = teacherLoads.sumOf { it.periodsPerWeek }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(NavyPrimary.copy(alpha = 0.12f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = NavyPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = teacher.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "${teacher.specialization} (${teacher.teacherCode})",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    // Workload status badge
                                    val statusColor = when {
                                        teacherTotalPeriods in 16..24 -> EthioGreen
                                        teacherTotalPeriods > 24 -> EthioRed
                                        teacherTotalPeriods > 0 -> GoldAccent
                                        else -> Color.Gray
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = statusColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "$teacherTotalPeriods " + (if (isAmharic) "ክ/ጊዜ" else "p/w"),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (teacherLoads.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        teacherLoads.forEach { wl ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${wl.subjectName} • ${wl.grade}-${wl.section}",
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = wl.daysAllotted,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "${wl.periodsPerWeek} " + (if (isAmharic) "ክ/ጊዜ" else "periods"),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    IconButton(
                                                        onClick = { onEditWorkloadClick(wl) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Edit",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { workloadToDelete = wl },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Delete",
                                                            tint = EthioRed,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (isAmharic) "ገና የተመደበ የሥራ ክፍፍል የለም" else "No classes assigned yet",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            } else {
                // Detailed List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(workloads, key = { it.id }) { wl ->
                        WorkloadCardItem(
                            workload = wl,
                            isAmharic = isAmharic,
                            onEdit = { onEditWorkloadClick(wl) },
                            onDelete = { workloadToDelete = wl }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddWorkloadClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_workload")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isAmharic) "ሥራ መድብ" else "Assign Workload"
            )
        }
    }

    // Delete Confirmation
    workloadToDelete?.let { wl ->
        AlertDialog(
            onDismissRequest = { workloadToDelete = null },
            title = {
                Text(if (isAmharic) "የሥራ ክፍፍል ሰርዝ" else "Delete Workload Assignment")
            },
            text = {
                Text(
                    if (isAmharic)
                        "የመምህር '${wl.teacherName}' ለ '${wl.subjectName} (${wl.grade}-${wl.section})' የተመደበው የሥራ ክፍፍል ይሰረዝ?"
                    else
                        "Delete workload '${wl.teacherName} - ${wl.subjectName} (${wl.grade}-${wl.section})'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteWorkloadClick(wl)
                        workloadToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EthioRed)
                ) {
                    Text(if (isAmharic) "ሰርዝ" else "Delete")
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
fun WorkloadCardItem(
    workload: TeacherWorkload,
    isAmharic: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workload.teacherName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${workload.subjectName} • ${workload.grade} - ${workload.section}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${workload.periodsPerWeek} " + (if (isAmharic) "ክ/ጊዜ" else "periods"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = workload.daysAllotted,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = EthioRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkloadFormDialog(
    initialWorkload: TeacherWorkload?,
    teachers: List<Teacher>,
    subjects: List<Subject>,
    availableGrades: List<String>,
    availableSections: List<String>,
    isAmharic: Boolean,
    onDismiss: () -> Unit,
    onSave: (TeacherWorkload) -> Unit
) {
    var selectedTeacher by remember {
        mutableStateOf(
            teachers.find { it.id == initialWorkload?.teacherId } ?: teachers.firstOrNull()
        )
    }
    var selectedSubject by remember {
        mutableStateOf(
            subjects.find { it.id == initialWorkload?.subjectId } ?: subjects.firstOrNull()
        )
    }
    var grade by remember {
        mutableStateOf(
            initialWorkload?.grade ?: availableGrades.firstOrNull { it != "ሁሉም" } ?: "9ኛ ክፍል"
        )
    }
    var section by remember { mutableStateOf(initialWorkload?.section ?: "A") }
    var periodsPerWeek by remember {
        mutableStateOf(
            initialWorkload?.periodsPerWeek?.toString() ?: selectedSubject?.periodsPerWeek?.toString() ?: "4"
        )
    }
    var daysAllotted by remember {
        mutableStateOf(initialWorkload?.daysAllotted ?: "ሰኞ፣ ረቡዕ፣ አርብ")
    }

    var teacherExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var gradeExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }

    var errorMsg by remember { mutableStateOf("") }

    val handleSave = {
        if (selectedTeacher == null) {
            errorMsg = if (isAmharic) "እባክዎ መምህር ይምረጡ" else "Please select a teacher"
        } else if (selectedSubject == null) {
            errorMsg = if (isAmharic) "እባክዎ የትምህርት ዓይነት ይምረጡ" else "Please select a subject"
        } else {
            val periods = periodsPerWeek.toIntOrNull() ?: 4

            val newWorkload = (initialWorkload ?: TeacherWorkload(
                teacherId = selectedTeacher!!.id,
                teacherName = selectedTeacher!!.fullName,
                subjectId = selectedSubject!!.id,
                subjectName = selectedSubject!!.name,
                grade = grade,
                section = section,
                periodsPerWeek = periods,
                daysAllotted = daysAllotted
            )).copy(
                teacherId = selectedTeacher!!.id,
                teacherName = selectedTeacher!!.fullName,
                subjectId = selectedSubject!!.id,
                subjectName = selectedSubject!!.name,
                grade = grade,
                section = section,
                periodsPerWeek = periods,
                daysAllotted = daysAllotted
            )
            onSave(newWorkload)
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
                            text = if (initialWorkload == null)
                                (if (isAmharic) "የመምህራን የሥራ ክፍፍል መደብ" else "Assign Teacher Workload")
                            else
                                (if (isAmharic) "የሥራ ክፍፍል ማስተካከያ" else "Edit Workload Assignment"),
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
                                .testTag("btn_save_workload_top")
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

                // Teacher Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = teacherExpanded,
                    onExpandedChange = { teacherExpanded = !teacherExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedTeacher?.fullName ?: (if (isAmharic) "መምህር ምረጥ" else "Select Teacher"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isAmharic) "የመምህሩ ስም *" else "Teacher *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = teacherExpanded,
                        onDismissRequest = { teacherExpanded = false }
                    ) {
                        teachers.forEach { t ->
                            DropdownMenuItem(
                                text = { Text("${t.fullName} (${t.specialization})") },
                                onClick = {
                                    selectedTeacher = t
                                    teacherExpanded = false
                                }
                            )
                        }
                    }
                }

                // Subject Selector Dropdown
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
                        subjects.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.name} (${s.code})") },
                                onClick = {
                                    selectedSubject = s
                                    periodsPerWeek = s.periodsPerWeek.toString()
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                // Grade Selector Dropdown
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
                        availableGrades.filter { it != "ሁሉም" }.forEach { g ->
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

                // Section Selector Dropdown
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

                // Periods per week
                OutlinedTextField(
                    value = periodsPerWeek,
                    onValueChange = { periodsPerWeek = it },
                    label = { Text(if (isAmharic) "ሳምንታዊ የክፍለ-ጊዜ ብዛት *" else "Weekly Periods *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Teaching Days
                OutlinedTextField(
                    value = daysAllotted,
                    onValueChange = { daysAllotted = it },
                    label = { Text(if (isAmharic) "የሚያስተምርባቸው ቀናት" else "Schedule Days") },
                    placeholder = { Text("ሰኞ፣ ማክሰኞ፣ ረቡዕ...") },
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
                        .testTag("btn_save_workload")
                ) {
                    Text(
                        text = if (isAmharic) "ሥራውን መዝግብ" else "Save Workload",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
