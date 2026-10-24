package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SchoolStats
import com.example.data.model.Staff
import com.example.data.model.Student
import com.example.data.model.Teacher
import com.example.data.model.TeacherWorkload
import com.example.data.model.UserAccount
import com.example.ui.SchoolNavTab
import com.example.ui.components.EthiopianFlagAccent
import com.example.ui.components.SectionHeader
import com.example.ui.components.ShareAppDialog
import com.example.ui.components.ShareAppLinkCard
import com.example.ui.components.StatCard
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

@Composable
fun DashboardScreen(
    currentUser: UserAccount?,
    currentTeacher: Teacher? = null,
    currentStaff: Staff? = null,
    currentStudent: Student? = null,
    stats: SchoolStats,
    recentStudents: List<Student>,
    recentTeachers: List<Teacher> = emptyList(),
    recentStaff: List<Staff> = emptyList(),
    recentWorkloads: List<TeacherWorkload>,
    isAmharic: Boolean,
    onNavigateTab: (SchoolNavTab) -> Unit,
    onRegisterStudentClick: () -> Unit,
    onRegisterTeacherClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onAssignWorkloadClick: () -> Unit,
    onAddGradeClick: () -> Unit = {},
    onTakeAttendanceClick: () -> Unit = {},
    onAddStaffClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showShareDialog by remember { mutableStateOf(false) }
    val role = currentUser?.role ?: "ርዕሰ መምህር (Admin)"
    val isTeacher = role.contains("መምህር") && !role.contains("ርዕሰ")
    val isStudent = role.contains("ተማሪ") || role.contains("Student")
    val isFinance = role.contains("ፋይናንስ") || role.contains("ሂሳብ")
    val isProperty = role.contains("ንብረት")
    val isSecretary = role.contains("ጸሐፊ") || role.contains("መዝጋቢ")
    val isCleanerOrSecurity = role.contains("ፅዳት") || role.contains("ጥበቃ") || role.contains("አገልግሎት")
    val isAdmin = role.contains("ርዕሰ መምህር") || role.contains("Admin") || role.contains("ምክትል")

    // Filter workloads for this teacher if applicable - strictly only their allocated workloads!
    val myWorkloads = if (isTeacher) {
        if (currentTeacher != null) {
            recentWorkloads.filter {
                it.teacherId == currentTeacher.id ||
                it.teacherName.trim().equals(currentTeacher.fullName.trim(), ignoreCase = true) ||
                (it.teacherName.isNotBlank() && currentTeacher.fullName.contains(it.teacherName))
            }
        } else {
            recentWorkloads.filter {
                currentUser != null && it.teacherName.contains(currentUser.fullName.split(" ").firstOrNull() ?: "")
            }
        }
    } else {
        recentWorkloads
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 700.dp

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isWideScreen) 20.dp else 14.dp)
                .testTag("dashboard_screen")
        ) {
            // Hero Header Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isWideScreen) 210.dp else 175.dp)
                    ) {
                    Image(
                        painter = painterResource(id = R.drawable.school_hero_1792161976345),
                        contentDescription = "School Classroom",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Dark gradient overlay for text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.Black.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GoldAccent,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = if (isAmharic) "2017 ዓ.ም የትምህርት ዘመን" else "Academic Year 2017 E.C.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = if (isAmharic) "ዲሊጀንት ትምህርት ቤት (Dilijent School)" else "Dilijent School Management System",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isAmharic) "የተጠቃሚ መረጃ እና የስራ ድርሻ ስርዓት" else "Role-Based School Operations Portal",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                EthiopianFlagAccent()
            }
        }

        // ==========================================
        // 1. Personalized User & Role Access Scope Card
        // ==========================================
        item {
            val roleColor = when {
                isStudent -> Color(0xFF0284C7)
                isTeacher -> Color(0xFF6D28D9)
                isFinance -> Color(0xFF047857)
                isProperty -> Color(0xFFD97706)
                isSecretary -> Color(0xFF0284C7)
                isCleanerOrSecurity -> Color(0xFF374151)
                else -> NavyPrimary
            }

            Card(
                shape = RoundedCornerShape(16.dp),
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
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(roleColor.copy(alpha = 0.15f))
                                .border(1.5.dp, roleColor, CircleShape)
                        ) {
                            Icon(
                                imageVector = when {
                                    isStudent -> Icons.Default.School
                                    isTeacher -> Icons.Default.MenuBook
                                    isFinance -> Icons.Default.Payments
                                    isProperty -> Icons.Default.Inventory
                                    isSecretary -> Icons.Default.Badge
                                    isCleanerOrSecurity -> Icons.Default.Security
                                    else -> Icons.Default.VerifiedUser
                                },
                                contentDescription = null,
                                tint = roleColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser?.fullName ?: (if (isAmharic) "ተጠቃሚ" else "User"),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = roleColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = role,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = roleColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Notice showing that only relevant features are granted
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = roleColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    isStudent -> if (isAmharic)
                                        "የተፈቀደልዎ ገፅ፦ የእርስዎ የግል የውጤት ካርድ፣ አማካይ እና የዕለት መገኘት ብቻ። ሚስጥራዊነት የተጠበቀ።"
                                    else "Authorized Access: Your personal academic report card & presence only. Confidential."

                                    isTeacher -> if (isAmharic)
                                        "🔒 ሚስጥራዊ የመምህር አሰራር፦ እርስዎ የሚያስተምሯቸውን የትምህርት ክፍሎች ብቻ ማርክ መሙላት ይችላሉ። ከእርስዎ ውጭ ማንም በገፅዎ ገብቶ ማርክ መቀየር አይችልም።"
                                    else "Authorized Access: Strictly your assigned subjects, sections & grades only. Isolated & protected."

                                    isFinance -> if (isAmharic)
                                        "የተፈቀደልዎ የስራ ድርሻ፦ የሰራተኞች ደመወዝ፣ የተማሪዎች ክፍያ እና የሂሳብ ሪፖርቶች ብቻ።"
                                    else "Authorized Access: Staff payroll, student fee status & finance accounts only."

                                    isProperty -> if (isAmharic)
                                        "የተፈቀደልዎ የስራ ድርሻ፦ የትምህርት ቤቱ ቋሚ እና አላቂ ንብረቶችና የቁሳቁስ መዝገብ ብቻ።"
                                    else "Authorized Access: School physical assets, furniture and lab inventory only."

                                    isSecretary -> if (isAmharic)
                                        "የተፈቀደልዎ የስራ ድርሻ፦ የተማሪዎች ምዝገባ፣ የሰራተኞች ማውጫ እና የዕለት ክትትል ብቻ።"
                                    else "Authorized Access: Student registration, staff directory & attendance records only."

                                    isCleanerOrSecurity -> if (isAmharic)
                                        "የተፈቀደልዎ የስራ ድርሻ፦ የእርስዎ የዕለት መርሃ-ግብር፣ የደህንነት/ንፅህና ተግባር እና የክትትል ፊርማ ብቻ።"
                                    else "Authorized Access: Your daily duty checklist, schedule and attendance sign-in only."

                                    else -> if (isAmharic)
                                        "የተፈቀደልዎ የስራ ድርሻ፦ የትምህርት ቤቱ አጠቃላይ የአስተዳደር፣ የመምህራን ክፍፍል፣ የአካዳሚክ እና የተጠቃሚዎች ቁጥጥር።"
                                    else "Authorized Access: Full executive school administration, workload allocation & system oversight."
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 1.5. Web Browser Link Sharing Card
        // ==========================================
        item {
            ShareAppLinkCard(
                isAmharic = isAmharic,
                onOpenShareDialog = { showShareDialog = true }
            )
        }

        // ==========================================
        // 2. Role-Tailored Statistics Grid (2x2)
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isStudent) {
                    // Student-specific statistics
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = if (isAmharic) "የእኔ ክፍል" else "My Class",
                            value = "${currentStudent?.grade ?: "9ኛ ክፍል"} - ${currentStudent?.section ?: "A"}",
                            icon = Icons.Default.School,
                            accentColor = Color(0xFF0284C7),
                            subtitle = if (isAmharic) "ክፍል እና ሴክሽን" else "Class & Section",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_student_class"
                        )
                        StatCard(
                            title = if (isAmharic) "መታወቂያ ቁጥር" else "Student ID",
                            value = currentStudent?.studentIdNumber ?: "ET-2017-001",
                            icon = Icons.Default.Badge,
                            accentColor = NavyPrimary,
                            subtitle = if (isAmharic) "የተማሪ ኮድ" else "ID Code",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_student_id"
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = if (isAmharic) "የትምህርት ዘመን" else "Academic Year",
                            value = currentStudent?.academicYear ?: "2017 ዓ.ም",
                            icon = Icons.Default.Timer,
                            accentColor = GoldAccent,
                            subtitle = if (isAmharic) "አመታዊ ምዝገባ" else "Active Year",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_student_year"
                        )
                        StatCard(
                            title = if (isAmharic) "የትምህርት ሁኔታ" else "Status",
                            value = currentStudent?.status ?: (if (isAmharic) "ንቁ" else "Active"),
                            icon = Icons.Default.CheckCircle,
                            accentColor = EthioGreen,
                            subtitle = if (isAmharic) "የተማሪ ደረጃ" else "Enrolled",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_student_status"
                        )
                    }
                } else if (isTeacher) {
                    // Teacher-specific statistics
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = if (isAmharic) "የማስተምራቸው ክፍሎች" else "My Classes",
                            value = "${myWorkloads.map { "${it.grade}-${it.section}" }.distinct().size} ክፍሎች",
                            icon = Icons.Default.School,
                            accentColor = NavyPrimary,
                            subtitle = if (isAmharic) "ሳምንታዊ መርሃ-ግብር" else "Weekly Schedule",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_my_classes"
                        )
                        StatCard(
                            title = if (isAmharic) "ሳምንታዊ ክፍለ-ጊዜ" else "My Weekly Periods",
                            value = "${myWorkloads.sumOf { it.periodsPerWeek }}",
                            icon = Icons.Default.Timer,
                            accentColor = EthioGreen,
                            subtitle = if (isAmharic) "ክፍለ-ጊዜ በሳምንት" else "Periods / Week",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_my_periods"
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = if (isAmharic) "የትምህርት ዓይነት" else "Subject Specialization",
                            value = currentTeacher?.specialization ?: (myWorkloads.firstOrNull()?.subjectName?.take(10) ?: "ሂሳብ"),
                            icon = Icons.Default.MenuBook,
                            accentColor = GoldAccent,
                            subtitle = if (isAmharic) "የምመድብበት" else "Assigned Dept",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_my_subject"
                        )
                        StatCard(
                            title = if (isAmharic) "የመደብ ኃላፊነት (Homeroom)" else "Homeroom Class",
                            value = if (currentTeacher?.isHomeroomTeacher == true) (currentTeacher.homeroomClass.ifEmpty { "9ኛ-A" }) else (if (isAmharic) "አልተመደበም" else "None"),
                            icon = Icons.Default.People,
                            accentColor = Color(0xFF7C3AED),
                            subtitle = if (isAmharic) "የክፍል ተቆጣጣሪ" else "Section Master",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_my_homeroom"
                        )
                    }
                } else if (isFinance) {
                    // Finance-specific statistics
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = if (isAmharic) "ወርሃዊ የደመወዝ ወጪ" else "Monthly Payroll",
                            value = "126,500 ብር",
                            icon = Icons.Default.Payments,
                            accentColor = EthioGreen,
                            subtitle = if (isAmharic) "የሰራተኞች ደመወዝ" else "Staff Salaries",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_payroll"
                        )
                        StatCard(
                            title = if (isAmharic) "ጠቅላላ ሰራተኞች" else "Total Employees",
                            value = "${stats.totalStaff + stats.totalTeachers}",
                            icon = Icons.Default.People,
                            accentColor = NavyPrimary,
                            subtitle = if (isAmharic) "በደመወዝ መዝገብ" else "On Active Payroll",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_finance_staff"
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = if (isAmharic) "የትምህርት ቤት ተማሪዎች" else "Student Accounts",
                            value = "${stats.totalStudents}",
                            icon = Icons.Default.School,
                            accentColor = GoldAccent,
                            subtitle = if (isAmharic) "የክፍያ መዝገብ" else "Fee Billing List",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_finance_students"
                        )
                        StatCard(
                            title = if (isAmharic) "የበጀት ዓመት" else "Fiscal Year",
                            value = "2017 ዓ.ም",
                            icon = Icons.Default.Assessment,
                            accentColor = Color(0xFF7C3AED),
                            subtitle = if (isAmharic) "ወቅታዊ በጀት" else "Active Budget",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_fiscal_year"
                        )
                    }
                } else if (isProperty) {
                    // Property-specific statistics
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = if (isAmharic) "የትምህርት ቤት ንብረቶች" else "Total Assets",
                            value = "420+ እቃዎች",
                            icon = Icons.Default.Inventory,
                            accentColor = GoldAccent,
                            subtitle = if (isAmharic) "ቋሚ እና አላቂ" else "Physical Inventory",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_assets"
                        )
                        StatCard(
                            title = if (isAmharic) "የመማሪያ ክፍሎች እቃዎች" else "Classroom Furniture",
                            value = "24 ክፍሎች",
                            icon = Icons.Default.School,
                            accentColor = NavyPrimary,
                            subtitle = if (isAmharic) "ወንበርና ጠረጴዛ" else "Desks & Boards",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_desks"
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = if (isAmharic) "የቤተ-ሙከራ ቁሳቁሶች" else "Lab Equipment",
                            value = "6 ላብራቶሪ",
                            icon = Icons.Default.MenuBook,
                            accentColor = EthioGreen,
                            subtitle = if (isAmharic) "ሳይንስና IT" else "Science & ICT",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_lab_assets"
                        )
                        StatCard(
                            title = if (isAmharic) "የቢሮ ቁሳቁስ" else "Office Supplies",
                            value = "12 ቢሮዎች",
                            icon = Icons.Default.People,
                            accentColor = Color(0xFF7C3AED),
                            subtitle = if (isAmharic) "የጽህፈት መሳሪያ" else "Stationery",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_office_supplies"
                        )
                    }
                } else if (isSecretary) {
                    // Secretary-specific statistics
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = if (isAmharic) "የተመዘገቡ ተማሪዎች" else "Total Students",
                            value = "${stats.totalStudents}",
                            icon = Icons.Default.School,
                            accentColor = NavyPrimary,
                            subtitle = if (isAmharic) "${stats.maleStudents} ወንድ | ${stats.femaleStudents} ሴት" else "${stats.maleStudents} M | ${stats.femaleStudents} F",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_students"
                        )
                        StatCard(
                            title = if (isAmharic) "የሰራተኞች ማውጫ" else "Staff Directory",
                            value = "${stats.totalStaff + stats.totalTeachers}",
                            icon = Icons.Default.People,
                            accentColor = EthioGreen,
                            subtitle = if (isAmharic) "የስልክ አድራሻ" else "Phone Contacts",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_contacts"
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = if (isAmharic) "የዕለት ክትትል" else "Attendance Log",
                            value = if (isAmharic) "ንቁ" else "Active",
                            icon = Icons.Default.CheckCircle,
                            accentColor = GoldAccent,
                            subtitle = if (isAmharic) "ዕለታዊ ሪፖርት" else "Daily Reports",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_attendance_sec"
                        )
                        StatCard(
                            title = if (isAmharic) "የትምህርት ክፍሎች" else "Grades Count",
                            value = "${stats.activeGradesCount}",
                            icon = Icons.Default.MenuBook,
                            accentColor = Color(0xFF7C3AED),
                            subtitle = if (isAmharic) "9ኛ እና 10ኛ" else "Active Levels",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_grades_sec"
                        )
                    }
                } else if (isCleanerOrSecurity) {
                    // Support Staff statistics
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = if (isAmharic) "የስራ መደብ" else "Job Role",
                            value = if (role.contains("ጥበቃ")) (if (isAmharic) "የጥበቃ ኃላፊ" else "Security Guard") else (if (isAmharic) "የፅዳት ሰራተኛ" else "Sanitation Staff"),
                            icon = Icons.Default.Security,
                            accentColor = NavyPrimary,
                            subtitle = if (isAmharic) "ቋሚ ቅጥር" else "Permanent",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_support_role"
                        )
                        StatCard(
                            title = if (isAmharic) "የስራ ሰዓት" else "Duty Shift",
                            value = if (role.contains("ጥበቃ")) "ቀን / ሌሊት" else "7:30 - 11:30",
                            icon = Icons.Default.Timer,
                            accentColor = EthioGreen,
                            subtitle = if (isAmharic) "የዕለት ፈረቃ" else "Daily Shift",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_support_hours"
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = if (isAmharic) "የስራ ክፍል" else "Department",
                            value = if (isAmharic) "አገልግሎትና ደህንነት" else "Services & Safety",
                            icon = Icons.Default.People,
                            accentColor = GoldAccent,
                            subtitle = if (isAmharic) "ዲፓርትመንት" else "Branch",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_support_dept"
                        )
                        StatCard(
                            title = if (isAmharic) "የስራ ሁኔታ" else "Status",
                            value = if (isAmharic) "ንቁ ሰራተኛ" else "Active",
                            icon = Icons.Default.CheckCircle,
                            accentColor = Color(0xFF7C3AED),
                            subtitle = "2017 ዓ.ም",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_support_status"
                        )
                    }
                } else {
                    // Admin executive overview
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = if (isAmharic) "ጠቅላላ ተማሪዎች" else "Total Students",
                            value = "${stats.totalStudents}",
                            icon = Icons.Default.School,
                            accentColor = NavyPrimary,
                            subtitle = if (isAmharic) "${stats.maleStudents} ወንድ | ${stats.femaleStudents} ሴት" else "${stats.maleStudents} M | ${stats.femaleStudents} F",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_students"
                        )
                        StatCard(
                            title = if (isAmharic) "ጠቅላላ መምህራን" else "Total Teachers",
                            value = "${stats.totalTeachers}",
                            icon = Icons.Default.People,
                            accentColor = EthioGreen,
                            subtitle = if (isAmharic) "ንቁ መምህራን" else "Active Staff",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_teachers"
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = if (isAmharic) "የትምህርት ዓይነቶች" else "Total Subjects",
                            value = "${stats.totalSubjects}",
                            icon = Icons.Default.MenuBook,
                            accentColor = GoldAccent,
                            subtitle = if (isAmharic) "ስርዓተ-ትምህርት" else "Curriculum",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_subjects"
                        )
                        StatCard(
                            title = if (isAmharic) "ሳምንታዊ ክፍለ-ጊዜ" else "Allocated Periods",
                            value = "${stats.totalAllocatedPeriods}",
                            icon = Icons.Default.Timer,
                            accentColor = Color(0xFF7C3AED),
                            subtitle = if (isAmharic) "የሥራ ክፍፍል" else "Weekly Hours",
                            modifier = Modifier.weight(1f),
                            testTag = "stat_workloads"
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. Role-Tailored Quick Action Buttons
        // ==========================================
        item {
            SectionHeader(
                title = if (isAmharic) "ለእርስዎ የተፈቀዱ ፈጣን ተግባራት" else "Your Authorized Quick Actions"
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isStudent) {
                    Button(
                        onClick = { onNavigateTab(SchoolNavTab.GRADES) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_student_grades")
                    ) {
                        Icon(imageVector = Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isAmharic) "📜 የውጤት ካርድ" else "📜 View Grades", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { onNavigateTab(SchoolNavTab.ATTENDANCE) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_student_attendance")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isAmharic) "📅 የዕለት መገኘት" else "📅 Attendance", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else if (isTeacher) {
                    Button(
                        onClick = { onNavigateTab(SchoolNavTab.GRADES) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_record_grades")
                    ) {
                        Icon(imageVector = Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isAmharic) "+ ውጤት መዝግብ" else "+ Record Grades", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { onNavigateTab(SchoolNavTab.ATTENDANCE) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_take_attendance")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isAmharic) "✓ የክፍል ክትትል" else "✓ Attendance", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else if (isFinance) {
                    Button(
                        onClick = { onNavigateTab(SchoolNavTab.STAFF) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_staff_payroll")
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isAmharic) "የደመወዝ መዝገብ" else "Staff Payroll", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { onNavigateTab(SchoolNavTab.STUDENTS) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_student_fees")
                    ) {
                        Icon(imageVector = Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isAmharic) "የተማሪዎች ክፍያ" else "Student Fees", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else if (isProperty) {
                    Button(
                        onClick = { onNavigateTab(SchoolNavTab.STAFF) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_property_staff")
                    ) {
                        Icon(imageVector = Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isAmharic) "የክፍሎች ንብረት ድልድል" else "Classroom Allocation", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else if (isSecretary) {
                    Button(
                        onClick = onRegisterStudentClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_register_student")
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isAmharic) "+ አዲስ ተማሪ" else "+ Add Student", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { onNavigateTab(SchoolNavTab.STAFF) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_secretary_staff")
                    ) {
                        Icon(imageVector = Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isAmharic) "የሰራተኞች ማውጫ" else "Staff Directory", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else if (isCleanerOrSecurity) {
                    Button(
                        onClick = { onNavigateTab(SchoolNavTab.ATTENDANCE) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_support_attendance")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isAmharic) "✓ የዕለት ክትትል ፊርማ" else "✓ Sign Attendance", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0911000000"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_call_admin")
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isAmharic) "📞 ለአስተዳደር ደውል" else "📞 Call Admin", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    // Admin options
                    Button(
                        onClick = onRegisterStudentClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_register_student")
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isAmharic) "+ ተማሪ መዝግብ" else "+ Add Student", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = onAssignWorkloadClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_assign_workload")
                    ) {
                        Icon(imageVector = Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isAmharic) "የሥራ ክፍፍል" else "Workload", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // ==========================================
        // 4. Role-Specific Main Interactive Section
        // ==========================================
        item {
            if (isTeacher) {
                // Teacher: My Weekly Teaching Timetable
                SectionHeader(
                    title = if (isAmharic) "የእኔ የሳምንቱ ክፍለ-ጊዜዎች እና የሥራ ድርሻ" else "My Weekly Teaching Schedule & Workload",
                    actionText = if (isAmharic) "ወደ ሥራ ክፍፍል >" else "View All >",
                    onActionClick = { onNavigateTab(SchoolNavTab.WORKLOAD) }
                )

                if (myWorkloads.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isAmharic) "ምንም የተመደበ የሥራ ክፍፍል አልተገኘም" else "No workload assignments found",
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        myWorkloads.forEach { wl ->
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
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF6D28D9).copy(alpha = 0.12f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = Color(0xFF6D28D9),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${wl.grade} - ሴክሽን ${wl.section}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${wl.subjectName} • ${wl.periodsPerWeek} " + (if (isAmharic) "ክፍለ-ጊዜ በሳምንት" else "periods/week"),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Button(
                                        onClick = { onNavigateTab(SchoolNavTab.GRADES) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                                    ) {
                                        Text(
                                            text = if (isAmharic) "ውጤት አስገባ" else "Grade",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (isFinance) {
                // Finance: Staff Payroll Register
                SectionHeader(
                    title = if (isAmharic) "የሰራተኞች ወርሃዊ ደመወዝ መዝገብ (Payroll)" else "Staff Monthly Payroll Register",
                    actionText = if (isAmharic) "የሰራተኞች ዝርዝር >" else "All Staff >",
                    onActionClick = { onNavigateTab(SchoolNavTab.STAFF) }
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val staffToDisplay = if (recentStaff.isNotEmpty()) recentStaff else emptyList()
                    staffToDisplay.take(5).forEach { staff ->
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
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(EthioGreen.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = EthioGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = staff.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = "${staff.role} • ${staff.employmentType}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EthioGreen.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = staff.salary.ifEmpty { "12,000 ብር" },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EthioGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (isProperty) {
                // Property: School Asset Inventory
                SectionHeader(
                    title = if (isAmharic) "የትምህርት ቤቱ የቁሳቁስና የንብረት ዝርዝር መዝገብ" else "School Property & Asset Directory"
                )

                val assetList = listOf(
                    Triple("የተማሪዎች ወንበርና ጠረጴዛ (Combined Desks)", "540 የተሟሉ ወንበሮች", "በሁሉም 24 ክፍሎች"),
                    Triple("የሳይንስ ቤተ-ሙከራ ቁሳቁስ (Lab Kits)", "48 ማይክሮስኮፕና ኬሚካሎች", "ኬሚስትሪና ባዮሎጂ"),
                    Triple("የኮምፒውተር ቤተ-ሙከራ (Desktop PCs)", "45 ኮምፒውተሮች", "IT ላብራቶሪ"),
                    Triple("ስማርት ሰሌዳ እና ፕሮጀክተር", "12 ፕሮጀክተሮች", "የስብሰባና ማሳያ ክፍል")
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    assetList.forEach { (name, qty, loc) ->
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
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(GoldAccent.copy(alpha = 0.15f))
                                ) {
                                    Icon(imageVector = Icons.Default.Inventory, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = loc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(text = qty, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                }
                            }
                        }
                    }
                }
            } else if (isCleanerOrSecurity) {
                // Support Staff: Daily Checklist & Duties
                SectionHeader(
                    title = if (isAmharic) "የእርስዎ የዕለት ተዕለት የስራ መመሪያ እና የኃላፊነት ዝርዝር" else "Daily Duty Checklist & Guidelines"
                )

                val checklist = if (role.contains("ጥበቃ")) {
                    listOf(
                        "የመግቢያና መውጫ በር ደህንነትን ማረጋገጥ እና እንግዶችን መመዝገብ",
                        "ተማሪዎች ያለ ወላጅ/ት/ቤት ፈቃድ ከግቢ እንዳይወጡ መቆጣጠር",
                        "በእረፍት ሰዓት የግቢውን ሰላምና ደህንነት በንቃት መከታተል",
                        "የትምህርት ሰዓት ሲያበቃ ክፍሎች እና በሮች መቆለፋቸውን ማረጋገጥ"
                    )
                } else {
                    listOf(
                        "የመማሪያ ክፍሎችን ጠዋት ተማሪዎች ከመግባታቸው በፊት ማጽዳትና ማዘጋጀት",
                        "የተማሪዎችን እና የመምህራንን መጸዳጃ ቤቶች በየቀኑ በፀረ-ተባይ ማጽዳት",
                        "የግቢውን እና የኮሪደሮችን ንፅህና በተከታታይ መጠበቅ",
                        "የጽዳት እቃዎችና ሳሙና እጥረቶች ሲያጋጥሙ ለንብረት ክፍል ማሳወቅ"
                    )
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        checklist.forEachIndexed { idx, duty ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = EthioGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${idx + 1}. $duty",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // Admin / Secretary view: Teacher workloads & recent student registrations
                SectionHeader(
                    title = if (isAmharic) "የመምህራን የሥራ ክፍፍል ማጠቃለያ" else "Teacher Workload Allocations",
                    actionText = if (isAmharic) "ሁሉንም እይ >" else "View All >",
                    onActionClick = { onNavigateTab(SchoolNavTab.WORKLOAD) }
                )
                if (recentWorkloads.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isAmharic) "ምንም የተመደበ የሥራ ክፍፍል የለም" else "No workload allocations found",
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentWorkloads.take(3).forEach { wl ->
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
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(GoldAccent.copy(alpha = 0.15f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = wl.teacherName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${wl.subjectName} • ${wl.grade} ሴክሽን ${wl.section}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${wl.periodsPerWeek} " + (if (isAmharic) "ክ/ጊዜ" else "periods"),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Recent Registered Students
                SectionHeader(
                    title = if (isAmharic) "የቅርብ ጊዜ የተመዘገቡ ተማሪዎች" else "Recently Registered Students",
                    actionText = if (isAmharic) "ሁሉንም እይ >" else "View All >",
                    onActionClick = { onNavigateTab(SchoolNavTab.STUDENTS) }
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentStudents.take(4).forEach { student ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateTab(SchoolNavTab.STUDENTS) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (student.gender == "ወንድ" || student.gender.equals("Male", true))
                                                NavyPrimary.copy(alpha = 0.15f)
                                            else
                                                Color(0xFFEC4899).copy(alpha = 0.15f)
                                        )
                                ) {
                                    Text(
                                        text = student.fullName.take(1),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (student.gender == "ወንድ" || student.gender.equals("Male", true))
                                            NavyPrimary
                                        else
                                            Color(0xFFBE185D)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${student.studentIdNumber} • ${student.grade} - ${student.section}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showShareDialog) {
        ShareAppDialog(
            isAmharic = isAmharic,
            onDismiss = { showShareDialog = false }
        )
    }
}
}
