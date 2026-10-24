package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Timer
import com.example.data.model.Subject
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FilterChipRow
import com.example.ui.theme.EthioRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

@Composable
fun SubjectsScreen(
    subjects: List<Subject>,
    gradeFilter: String,
    onGradeFilterChange: (String) -> Unit,
    availableGrades: List<String>,
    isAmharic: Boolean,
    onAddSubjectClick: () -> Unit,
    onEditSubjectClick: (Subject) -> Unit,
    onDeleteSubjectClick: (Subject) -> Unit,
    modifier: Modifier = Modifier
) {
    var subjectToDelete by remember { mutableStateOf<Subject?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("subjects_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Grade Filter
            Text(
                text = if (isAmharic) "በክፍል ደረጃ ለይተህ እይ:" else "Filter by Grade Level:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilterChipRow(
                options = availableGrades,
                selectedOption = gradeFilter,
                onSelectOption = onGradeFilterChange,
                testTagPrefix = "subject_grade_filter"
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = if (isAmharic) "ጠቅላላ የትምህርት ዓይነቶች: ${subjects.size}" else "Total Subjects: ${subjects.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (subjects.isEmpty()) {
                EmptyStateCard(
                    message = if (isAmharic) "ምንም የትምህርት ዓይነት አልተገኘም" else "No subjects registered for this grade"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(subjects, key = { it.id }) { subject ->
                        SubjectCardItem(
                            subject = subject,
                            isAmharic = isAmharic,
                            onEdit = { onEditSubjectClick(subject) },
                            onDelete = { subjectToDelete = subject }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddSubjectClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_subject")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isAmharic) "የት/ት አይነት መዝግብ" else "Add Subject"
            )
        }
    }

    // Delete Confirmation Dialog
    subjectToDelete?.let { subject ->
        AlertDialog(
            onDismissRequest = { subjectToDelete = null },
            title = {
                Text(if (isAmharic) "የትምህርት ዓይነት ሰርዝ" else "Delete Subject")
            },
            text = {
                Text(
                    if (isAmharic)
                        "የትምህርት ዓይነቱን '${subject.name}' ከስርዓተ-ትምህርቱ መሰረዝ ይፈልጋሉ?"
                    else
                        "Delete subject '${subject.name}' from curriculum?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSubjectClick(subject)
                        subjectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EthioRed)
                ) {
                    Text(if (isAmharic) "ሰርዝ" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToDelete = null }) {
                    Text(if (isAmharic) "ይቅር" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun SubjectCardItem(
    subject: Subject,
    isAmharic: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("subject_card_${subject.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(GoldAccent.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = subject.code,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = subject.gradeLevel,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${subject.periodsPerWeek} " + (if (isAmharic) "ክፍለ-ጊዜ በሳምንት" else "periods/week"),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• " + (if (isAmharic) "ማለፊያ: ${subject.passingScore}%" else "Pass: ${subject.passingScore}%"),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = EthioRed,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectFormDialog(
    initialSubject: Subject?,
    availableGrades: List<String>,
    isAmharic: Boolean,
    onDismiss: () -> Unit,
    onSave: (Subject) -> Unit
) {
    var name by remember { mutableStateOf(initialSubject?.name ?: "") }
    var code by remember { mutableStateOf(initialSubject?.code ?: "") }
    var gradeLevel by remember { mutableStateOf(initialSubject?.gradeLevel ?: availableGrades.firstOrNull() ?: "9ኛ ክፍል") }
    var periodsPerWeek by remember { mutableStateOf(initialSubject?.periodsPerWeek?.toString() ?: "4") }
    var passingScore by remember { mutableStateOf(initialSubject?.passingScore?.toString() ?: "50") }
    var gradeExpanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    val handleSave = {
        if (name.isBlank()) {
            errorMsg = if (isAmharic) "የትምህርት ዓይነት ስም ያስገቡ" else "Please enter subject name"
        } else {
            val periods = periodsPerWeek.toIntOrNull() ?: 4
            val pass = passingScore.toIntOrNull() ?: 50
            val subCode = if (code.isNotBlank()) code else name.take(3).uppercase() + "-01"

            val newSub = (initialSubject ?: Subject(
                name = name,
                code = subCode,
                gradeLevel = gradeLevel,
                periodsPerWeek = periods,
                passingScore = pass
            )).copy(
                name = name,
                code = subCode,
                gradeLevel = gradeLevel,
                periodsPerWeek = periods,
                passingScore = pass
            )
            onSave(newSub)
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
                            text = if (initialSubject == null)
                                (if (isAmharic) "አዲስ የትምህርት ዓይነት መዝግብ" else "Register New Subject")
                            else
                                (if (isAmharic) "የትምህርት ዓይነት ማስተካከያ" else "Edit Subject"),
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
                                .testTag("btn_save_subject_top")
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

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isAmharic) "የትምህርት ዓይነት ስም *" else "Subject Name *") },
                    placeholder = { Text("አማርኛ፣ ሂሳብ፣ ፊዚክስ...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_subject_name")
                )

                // Code
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text(if (isAmharic) "የትምህርት ኮድ *" else "Subject Code *") },
                    placeholder = { Text("MATH-09") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Target Grade Dropdown
                ExposedDropdownMenuBox(
                    expanded = gradeExpanded,
                    onExpandedChange = { gradeExpanded = !gradeExpanded }
                ) {
                    OutlinedTextField(
                        value = gradeLevel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isAmharic) "የክፍል ደረጃ" else "Target Grade") },
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
                                    gradeLevel = g
                                    gradeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Periods per week
                OutlinedTextField(
                    value = periodsPerWeek,
                    onValueChange = { periodsPerWeek = it },
                    label = { Text(if (isAmharic) "በሳምንት ያለው የክፍለ-ጊዜ ብዛት" else "Periods per week") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Passing Score
                OutlinedTextField(
                    value = passingScore,
                    onValueChange = { passingScore = it },
                    label = { Text(if (isAmharic) "ማለፊያ ነጥብ (%)" else "Passing Score (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        .testTag("btn_save_subject")
                ) {
                    Text(
                        text = if (isAmharic) "መዝግብ / አስቀምጥ" else "Save Subject",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
