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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Staff
import com.example.ui.components.SectionHeader
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

val STAFF_ROLES = listOf(
    "ርዕሰ መምህር",
    "ምክትል ርዕሰ መምህር",
    "ዋና ጸሐፊ",
    "ፋይናንስ / ሂሳብ ሹም",
    "ንብረት ክፍል ኃላፊ",
    "የፅዳት ሰራተኛ",
    "የጥበቃ ኃላፊ",
    "መረጃ ቴክኖሎጂ (IT)",
    "ላይብረሪያን",
    "የተማሪዎች መዝጋቢ (Registrar)",
    "ሌላ"
)

val STAFF_DEPARTMENTS = listOf(
    "አስተዳደር",
    "ፋይናንስ እና ንብረት",
    "አገልግሎትና ደህንነት",
    "አካዳሚክ ድጋፍ"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffScreen(
    staffList: List<Staff>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    roleFilter: String,
    onRoleFilterChange: (String) -> Unit,
    departmentFilter: String,
    onDepartmentFilterChange: (String) -> Unit,
    isAmharic: Boolean,
    onStaffClick: (Staff) -> Unit,
    onAddStaffClick: () -> Unit,
    onEditStaffClick: (Staff) -> Unit,
    onDeleteStaffClick: (Staff) -> Unit
) {
    val rolesForFilter = listOf("ሁሉም") + listOf(
        "ርዕሰ መምህር",
        "ምክትል ርዕሰ መምህር",
        "ዋና ጸሐፊ",
        "ፋይናንስ / ሂሳብ ሹም",
        "ንብረት ክፍል ኃላፊ",
        "የፅዳት ሰራተኛ",
        "የጥበቃ ኃላፊ",
        "መረጃ ቴክኖሎጂ (IT)"
    )

    val deptsForFilter = listOf("ሁሉም") + STAFF_DEPARTMENTS

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Screen Title Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isAmharic) "የአስተዳደር ሰራተኞች" else "Administrative Staff",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (isAmharic) "ርዕሰ መምህራን፣ ጸሐፊዎች፣ ፋይናንስ፣ ንብረት፣ ፅዳት እና ጥበቃ" else "Leadership, Admin, Finance, Store, Cleaning & Security",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onAddStaffClick,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_add_staff")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAmharic) "መዝግብ" else "Add",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Summary Metric Badges
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StaffMetricCard(
                        title = if (isAmharic) "ጠቅላላ ሰራተኛ" else "Total Staff",
                        count = staffList.size.toString(),
                        color = NavyPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StaffMetricCard(
                        title = if (isAmharic) "አስተዳደር" else "Leadership",
                        count = staffList.count { it.department == "አስተዳደር" }.toString(),
                        color = GoldAccent,
                        modifier = Modifier.weight(1f)
                    )
                    StaffMetricCard(
                        title = if (isAmharic) "አገልግሎትና ጥበቃ" else "Support & Security",
                        count = staffList.count { it.department == "አገልግሎትና ደህንነት" }.toString(),
                        color = EthioGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_staff_input"),
                    placeholder = {
                        Text(
                            if (isAmharic) "በስም፣ በስራ ድርሻ፣ በመለያ ኮድ ወይም በስልክ ይፈልጉ..." else "Search by name, role, ID, or phone...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = NavyPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Role Filter Chips
            item {
                Column {
                    Text(
                        text = if (isAmharic) "የስራ ድርሻ (Role):" else "Role Filter:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(rolesForFilter) { r ->
                            val isSelected = roleFilter == r
                            FilterChip(
                                selected = isSelected,
                                onClick = { onRoleFilterChange(r) },
                                label = { Text(r, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Department Filter Chips
            item {
                Column {
                    Text(
                        text = if (isAmharic) "የስራ ክፍል (Department):" else "Department Filter:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(deptsForFilter) { d ->
                            val isSelected = departmentFilter == d
                            FilterChip(
                                selected = isSelected,
                                onClick = { onDepartmentFilterChange(d) },
                                label = { Text(d, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldAccent,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Staff Cards List
            if (staffList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isAmharic) "ምንም የተመዘገበ ሰራተኛ አልተገኘም" else "No staff members found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onAddStaffClick,
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                            ) {
                                Text(if (isAmharic) "+ የመጀመሪያ ሰራተኛ መዝግብ" else "+ Add First Staff Member")
                            }
                        }
                    }
                }
            } else {
                items(staffList, key = { it.id }) { staff ->
                    StaffCardItem(
                        staff = staff,
                        isAmharic = isAmharic,
                        onCardClick = { onStaffClick(staff) },
                        onEditClick = { onEditStaffClick(staff) },
                        onDeleteClick = { onDeleteStaffClick(staff) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddStaffClick,
            containerColor = NavyPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_staff")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Staff")
        }
    }
}

@Composable
fun StaffMetricCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StaffCardItem(
    staff: Staff,
    isAmharic: Boolean,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val roleBadgeColor = when {
        staff.role.contains("ርዕሰ") -> NavyPrimary
        staff.role.contains("ጸሐፊ") -> Color(0xFF00796B)
        staff.role.contains("ፋይናንስ") -> EthioGreen
        staff.role.contains("ንብረት") -> Color(0xFFE65100)
        staff.role.contains("ፅዳት") -> Color(0xFF5D4037)
        staff.role.contains("ጥበቃ") -> Color(0xFF37474F)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
            .testTag("staff_card_${staff.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Avatar, Name, Title, and Code
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(roleBadgeColor.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = staff.fullName.take(2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = roleBadgeColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${staff.title} ${staff.fullName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = roleBadgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = staff.role,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = roleBadgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = staff.staffCode,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Department & Employment Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = staff.department,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (staff.employmentType.contains("ቋሚ")) EthioGreen.copy(alpha = 0.15f) else Color(0xFFFFB300).copy(alpha = 0.18f)
                ) {
                    Text(
                        text = staff.employmentType,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (staff.employmentType.contains("ቋሚ")) EthioGreen else Color(0xFFF57F17),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Responsibilities snippet (የስራ ድርሻ)
            if (staff.responsibilities.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = staff.responsibilities,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contact & Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call Phone Action
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EthioGreen.copy(alpha = 0.12f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${staff.phone}"))
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = EthioGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = staff.phone,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EthioGreen
                        )
                    }
                }

                // Edit & Delete Actions
                Row {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = NavyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(if (isAmharic) "ሰራተኛ ይሰረዝ?" else "Delete Staff Member?")
            },
            text = {
                Text(
                    if (isAmharic)
                        "${staff.title} ${staff.fullName} የተባሉትን ሰራተኛ ከስርዓቱ መሰረዝ ይፈልጋሉ?"
                    else
                        "Are you sure you want to delete ${staff.fullName} from staff records?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isAmharic) "ሰርዝ" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(if (isAmharic) "ይቅር" else "Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffFormDialog(
    initialStaff: Staff? = null,
    isAmharic: Boolean,
    onDismiss: () -> Unit,
    onSave: (Staff) -> Unit
) {
    var title by remember { mutableStateOf(initialStaff?.title ?: "አቶ") }
    var fullName by remember { mutableStateOf(initialStaff?.fullName ?: "") }
    var staffCode by remember { mutableStateOf(initialStaff?.staffCode ?: "ADM-${(100..999).random()}") }
    var gender by remember { mutableStateOf(initialStaff?.gender ?: "ወንድ") }
    var phone by remember { mutableStateOf(initialStaff?.phone ?: "") }
    var email by remember { mutableStateOf(initialStaff?.email ?: "") }
    var role by remember { mutableStateOf(initialStaff?.role ?: "ዋና ጸሐፊ") }
    var department by remember { mutableStateOf(initialStaff?.department ?: "አስተዳደር") }
    var qualification by remember { mutableStateOf(initialStaff?.qualification ?: "ዲፕሎማ") }
    var employmentType by remember { mutableStateOf(initialStaff?.employmentType ?: "ቋሚ") }
    var salary by remember { mutableStateOf(initialStaff?.salary ?: "") }
    var hiringDate by remember { mutableStateOf(initialStaff?.hiringDate ?: "2017 ዓ.ም") }
    var responsibilities by remember { mutableStateOf(initialStaff?.responsibilities ?: "") }
    var emergencyContact by remember { mutableStateOf(initialStaff?.emergencyContact ?: "") }

    var roleDropdownExpanded by remember { mutableStateOf(false) }
    var deptDropdownExpanded by remember { mutableStateOf(false) }
    var qualDropdownExpanded by remember { mutableStateOf(false) }
    var empTypeDropdownExpanded by remember { mutableStateOf(false) }

    BackHandler(onBack = onDismiss)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialStaff == null)
                            (if (isAmharic) "አዲስ የአስተዳደር ሰራተኛ መመዝገቢያ" else "Register Administrative Staff")
                        else
                            (if (isAmharic) "የሰራተኛ መረጃ ማስተካከያ" else "Edit Staff Details"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                // Title selection chips
                Text(
                    text = if (isAmharic) "የማዕረግ ስም (Title):" else "Title:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    listOf("አቶ", "ወ/ሮ", "ወ/ሪት", "ዶ/ር", "ኢ/ር").forEach { t ->
                        FilterChip(
                            selected = title == t,
                            onClick = { title = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(if (isAmharic) "ሙሉ ስም (Full Name) *" else "Full Name *") },
                    placeholder = { Text("ለምሳሌ፡ መስከረም በቀለ ከበደ") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_staff_fullname")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Staff Code & Gender Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = staffCode,
                        onValueChange = { staffCode = it },
                        label = { Text(if (isAmharic) "የመለያ ቁጥር" else "Staff ID") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAmharic) "ፆታ:" else "Gender:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = gender == "ወንድ",
                                onClick = { gender = "ወንድ" },
                                label = { Text(if (isAmharic) "ወንድ" else "Male", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = gender == "ሴት",
                                onClick = { gender = "ሴት" },
                                label = { Text(if (isAmharic) "ሴት" else "Female", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Phone Number & Email
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(if (isAmharic) "ስልክ ቁጥር *" else "Phone *") },
                        placeholder = { Text("09XXXXXXXX") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_staff_phone")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(if (isAmharic) "ኢሜይል (አማራጭ)" else "Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Role Dropdown (የስራ መደብ)
                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isAmharic) "የስራ መደብ / ድርሻ (Role) *" else "Role / Position *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        STAFF_ROLES.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    role = r
                                    // Auto-suggest department based on role
                                    if (r.contains("ርዕሰ") || r.contains("ጸሐፊ")) department = "አስተዳደር"
                                    else if (r.contains("ፋይናንስ") || r.contains("ንብረት")) department = "ፋይናንስ እና ንብረት"
                                    else if (r.contains("ፅዳት") || r.contains("ጥበቃ")) department = "አገልግሎትና ደህንነት"
                                    roleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Department Dropdown
                ExposedDropdownMenuBox(
                    expanded = deptDropdownExpanded,
                    onExpandedChange = { deptDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isAmharic) "የስራ ክፍል (Department)" else "Department") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = deptDropdownExpanded,
                        onDismissRequest = { deptDropdownExpanded = false }
                    ) {
                        STAFF_DEPARTMENTS.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = {
                                    department = d
                                    deptDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Qualification & Employment Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = qualDropdownExpanded,
                        onExpandedChange = { qualDropdownExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = qualification,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isAmharic) "የትምህርት ደረጃ" else "Qualification") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = qualDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = qualDropdownExpanded,
                            onDismissRequest = { qualDropdownExpanded = false }
                        ) {
                            listOf("10/12ኛ ማጠቃለያ", "ሰርተፍኬት", "ዲፕሎማ", "የመጀመሪያ ዲግሪ (BA/BSc)", "ሁለተኛ ዲግሪ (MA/MSc)", "ዶክትሬት (PhD)").forEach { q ->
                                DropdownMenuItem(
                                    text = { Text(q) },
                                    onClick = {
                                        qualification = q
                                        qualDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = empTypeDropdownExpanded,
                        onExpandedChange = { empTypeDropdownExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = employmentType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isAmharic) "የቅጥር አይነት" else "Employment") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = empTypeDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = empTypeDropdownExpanded,
                            onDismissRequest = { empTypeDropdownExpanded = false }
                        ) {
                            listOf("ቋሚ (Permanent)", "ኮንትራት (Contract)", "ጊዜያዊ (Temporary)").forEach { et ->
                                DropdownMenuItem(
                                    text = { Text(et) },
                                    onClick = {
                                        employmentType = et
                                        empTypeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Salary & Hiring Year
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = salary,
                        onValueChange = { salary = it },
                        label = { Text(if (isAmharic) "ወርሃዊ ደመወዝ (ብር)" else "Salary (ETB)") },
                        placeholder = { Text("12,500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = hiringDate,
                        onValueChange = { hiringDate = it },
                        label = { Text(if (isAmharic) "የተቀጠሩበት ዓ.ም" else "Hired Year") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Job Responsibilities (የስራ ድርሻ መሙያ)
                Text(
                    text = if (isAmharic) "የስራ ድርሻ እና ኃላፊነት (Job Responsibilities) *" else "Job Responsibilities & Scope of Work *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Quick Duty Templates
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(text = if (isAmharic) "ናሙና ፈጣን ድርሻ፦" else "Templates:", fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterVertically))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NavyPrimary.copy(alpha = 0.1f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                responsibilities = when (role) {
                                    "ርዕሰ መምህር" -> "የትምህርት ቤቱን አጠቃላይ የአስተዳደር፣ የአካዳሚክ እና የልማት ስራዎችን በበላይነት መምራት፤ ከወላጆችና ከትምህርት ቢሮ ጋር ግንኙነት ማድረግ።"
                                    "ምክትል ርዕሰ መምህር" -> "የመምህራን እና የተማሪዎች የዕለት ተዕለት ስነ-ስርዓት፣ የአካዳሚክ መርሃ-ግብር አፈፃፀም እና የመማር ማስተማር ክትትል ማድረግ።"
                                    "ዋና ጸሐፊ" -> "ደብዳቤዎችን መጻፍ፣ የተማሪዎች እና የመምህራን ፋይል አያያዝ፣ የጽህፈት መሳሪያዎች ጥያቄ መቀበል እና የስልክ ጥሪዎችን ማስተናገድ።"
                                    "ፋይናንስ / ሂሳብ ሹም" -> "የወርሃዊ የትምህርት ቤት ክፍያ መሰብሰብ፣ የደመወዝ ክፍያ ማዘጋጀት፣ የበጀት እቅድ እና የፋይናንስ ሪፖርቶችን ማዘጋጀት።"
                                    "ንብረት ክፍል ኃላፊ" -> "የትምህርት ቤቱን ቋሚ እና አላቂ ንብረቶች መመዝገብ፣ ማከፋፈል፣ አመታዊ የንብረት ቆጠራ ማካሄድ እና የጥገና ፍላጎቶችን መከታተል።"
                                    "የፅዳት ሰራተኛ" -> "የመማሪያ ክፍሎችን፣ የቢሮዎችን፣ የመጸዳጃ ቤቶችን እና የግቢውን ንጽህና በየቀኑ መጠበቅ።"
                                    "የጥበቃ ኃላፊ" -> "የትምህርት ቤቱን ግቢ ደህንነት መጠበቅ፣ የተማሪዎችን እና የእንግዶችን መግቢያና መውጫ መቆጣጠር።"
                                    else -> "የትምህርት ቤቱን ህጎችና ደንቦች በማክበር የተመደበበትን የዕለት ተዕለት የስራ ኃላፊነት መወጣት።"
                                }
                            }
                    ) {
                        Text(
                            text = if (isAmharic) "⚡ በስራ መደቡ በራስ-ሰር ሙላ" else "⚡ Auto-fill for Role",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = responsibilities,
                    onValueChange = { responsibilities = it },
                    label = { Text(if (isAmharic) "የስራ ድርሻ ዝርዝር መግለጫ" else "Detailed Responsibilities") },
                    placeholder = { Text(if (isAmharic) "ዋና ዋና የስራ ድርሻዎችን እዚህ ይዘርዝሩ..." else "List main responsibilities and duties here...") },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_staff_responsibilities")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Emergency Contact
                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text(if (isAmharic) "የአደጋ ጊዜ ተጠሪ ስልክ (Emergency Contact)" else "Emergency Contact") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Submit Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isAmharic) "ይቅር" else "Cancel")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (fullName.isNotBlank() && phone.isNotBlank()) {
                                onSave(
                                    Staff(
                                        id = initialStaff?.id ?: 0L,
                                        staffCode = staffCode,
                                        fullName = fullName.trim(),
                                        title = title,
                                        role = role,
                                        department = department,
                                        phone = phone.trim(),
                                        email = email.trim(),
                                        gender = gender,
                                        qualification = qualification,
                                        employmentType = employmentType,
                                        salary = salary.trim(),
                                        hiringDate = hiringDate.trim(),
                                        responsibilities = responsibilities.trim(),
                                        emergencyContact = emergencyContact.trim(),
                                        status = initialStaff?.status ?: "ንቁ"
                                    )
                                )
                            }
                        },
                        enabled = fullName.isNotBlank() && phone.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.testTag("btn_save_staff")
                    ) {
                        Text(
                            text = if (initialStaff == null)
                                (if (isAmharic) "መዝግብ (Save)" else "Save")
                            else
                                (if (isAmharic) "አዘምን (Update)" else "Update"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

@Composable
fun StaffDetailDialog(
    staff: Staff,
    isAmharic: Boolean,
    onDismiss: () -> Unit,
    onEditStaff: (Staff) -> Unit,
    onDeleteStaff: (Staff) -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    BackHandler(onBack = onDismiss)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAmharic) "የሰራተኛው ሙሉ መረጃ" else "Staff Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hero Profile Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(GoldAccent)
                        ) {
                            Text(
                                text = staff.fullName.take(2),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${staff.title} ${staff.fullName}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GoldAccent.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "${staff.role} • ${staff.department}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Job Responsibilities Card (የስራ ድርሻ)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Work, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAmharic) "የስራ ድርሻ እና ኃላፊነት (Job Responsibilities)" else "Scope of Work & Responsibilities",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (staff.responsibilities.isNotBlank()) staff.responsibilities else (if (isAmharic) "ምንም የተገለጸ የስራ ድርሻ የለም" else "No responsibilities specified"),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Personal and Employment Details Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow(label = if (isAmharic) "የመለያ ቁጥር (Staff Code):" else "Staff Code:", value = staff.staffCode)
                        DetailRow(label = if (isAmharic) "ፆታ:" else "Gender:", value = staff.gender)
                        DetailRow(label = if (isAmharic) "ስልክ ቁጥር:" else "Phone:", value = staff.phone)
                        if (staff.email.isNotBlank()) {
                            DetailRow(label = if (isAmharic) "ኢሜይል:" else "Email:", value = staff.email)
                        }
                        DetailRow(label = if (isAmharic) "የትምህርት ደረጃ:" else "Qualification:", value = staff.qualification)
                        DetailRow(label = if (isAmharic) "የቅጥር ሁኔታ:" else "Employment Type:", value = staff.employmentType)
                        if (staff.salary.isNotBlank()) {
                            DetailRow(label = if (isAmharic) "ወርሃዊ ደመወዝ:" else "Salary:", value = "${staff.salary} ብር")
                        }
                        DetailRow(label = if (isAmharic) "የተቀጠሩበት ዓ.ም:" else "Hired Year:", value = staff.hiringDate)
                        if (staff.emergencyContact.isNotBlank()) {
                            DetailRow(label = if (isAmharic) "የአደጋ ጊዜ ተጠሪ ስልክ:" else "Emergency Contact:", value = staff.emergencyContact)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${staff.phone}"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EthioGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isAmharic) "ደውል" else "Call")
                    }

                    OutlinedButton(
                        onClick = {
                            onEditStaff(staff)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isAmharic) "አስተካክል" else "Edit")
                    }

                    Button(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isAmharic) "ሰርዝ" else "Delete")
                    }
                }
            }
        }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (isAmharic) "ሰራተኛውን መሰረዝ ይፈልጋሉ?" else "Delete Staff Record?") },
            text = { Text(if (isAmharic) "${staff.fullName} ከሰራተኞች ዝርዝር ይሰረዛል።" else "This staff member will be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteStaff(staff)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isAmharic) "ሰርዝ" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(if (isAmharic) "ይቅር" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
