package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Share
import com.example.R
import com.example.ui.auth.GoogleSignInButton
import com.example.ui.components.EthiopianFlagAccent
import com.example.ui.components.ShareAppDialog
import com.example.ui.components.ShareAppLinkCard
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

/**
 * Data structure representing pre-configured role profiles for instant testing & login.
 */
data class DemoRoleProfile(
    val titleAm: String,
    val titleEn: String,
    val identifier: String, // email or phone
    val isEmail: Boolean = false,
    val defaultPass: String = "1234",
    val color: Color,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector
)

val availableDemoRoles = listOf(
    DemoRoleProfile("👑 ሲስተም አድሚን (እኔ)", "System Admin (Me)", "admin@dilijent.edu.et", isEmail = true, defaultPass = "admin123", color = Color(0xFFB45309), Icons.Default.Security),
    DemoRoleProfile("🎓 ርዕሰ መምህር (Principal)", "School Principal", "0911000000", isEmail = false, defaultPass = "1234", color = Color(0xFF1E3A8A), Icons.Default.School),
    DemoRoleProfile("👨‍🏫 መምህር - ሂሳብ (9A)", "Math Teacher (Grade 9A)", "0911223344", isEmail = false, defaultPass = "1234", color = Color(0xFF6D28D9), Icons.Default.MenuBook),
    DemoRoleProfile("👩‍🏫 መምህርት - እንግሊዝኛ (9A)", "English Teacher (Grade 9A)", "0922334455", isEmail = false, defaultPass = "1234", color = Color(0xFF7C3AED), Icons.Default.MenuBook),
    DemoRoleProfile("👨‍🎓 ተማሪ - ዮናስ (9A)", "Student - Jonas (9A)", "0911002233", isEmail = false, defaultPass = "1234", color = Color(0xFF0284C7), Icons.Default.School),
    DemoRoleProfile("✍️ ዋና ጸሐፊ / መዝጋቢ", "Secretary / Registrar", "0912334455", isEmail = false, defaultPass = "1234", color = Color(0xFF0D9488), Icons.Default.People),
    DemoRoleProfile("💰 ፋይናንስ / ሂሳብ ሹም", "Finance Officer", "0913445566", isEmail = false, defaultPass = "1234", color = Color(0xFF047857), Icons.Default.CheckCircle),
    DemoRoleProfile("📦 ንብረት ክፍል ኃላፊ", "Store & Property Head", "0914556677", isEmail = false, defaultPass = "1234", color = Color(0xFFD97706), Icons.Default.Lock),
    DemoRoleProfile("🎓 ም/ርዕሰ መምህር", "Vice Principal", "0918000000", isEmail = false, defaultPass = "1234", color = Color(0xFF4338CA), Icons.Default.Badge)
)

@Composable
fun LoginScreen(
    isAdminMode: Boolean = false,
    onToggleAdminMode: (Boolean) -> Unit = {},
    email: String = "admin@dilijent.edu.et",
    onEmailChange: (String) -> Unit = {},
    phone: String,
    onPhoneChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    isAmharic: Boolean,
    isFullScreen: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleFullScreen: () -> Unit,
    onLoginClick: () -> Unit,
    onQuickDemoLogin: () -> Unit,
    onQuickRoleLogin: (identifier: String, isEmail: Boolean, pass: String) -> Unit = { _, _, _ -> },
    onRegisterNewUser: (name: String, phone: String, pass: String, role: String) -> Unit,
    onGoogleSignInSuccess: (name: String, email: String, uid: String) -> Unit = { _, _, _ -> },
    onGoogleSignInError: (String) -> Unit = {},
    schoolLogoUri: String = "",
    schoolNameAm: String = "ዲሊጀንት ትምህርት ቤት",
    schoolNameEn: String = "Dilijent School",
    schoolGradeLevel: String = "1_8"
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Adaptive Container: Fills 100% of all 4 directions on both phone and computer
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NavyPrimary,
                        NavyPrimary.copy(alpha = 0.94f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .testTag("login_screen")
    ) {
        val isWideScreen = maxWidth >= 720.dp

        if (isWideScreen) {
            // ==============================================================
            // COMPUTER / TABLET DESKTOP MODE (Dual-Pane Edge-to-Edge Canvas)
            // ==============================================================
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Showcase Pane (Branding, Heritage, School Overview)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .weight(0.44f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Top School Crest & Title
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(GoldAccent)
                                ) {
                                    if (schoolLogoUri.isNotEmpty()) {
                                        AsyncImage(
                                            model = schoolLogoUri,
                                            contentDescription = "School Logo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isAmharic) schoolNameAm else schoolNameEn,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = when (schoolGradeLevel) {
                                            "1_8" -> if (isAmharic) "1ኛ - 8ኛ ክፍል • የመረጃ አያያዝ ስርዓት" else "Grade 1-8 • School Management System"
                                            "9_12" -> if (isAmharic) "9ኛ - 12ኛ ክፍል • የመረጃ አያያዝ ስርዓት" else "Grade 9-12 • School Management System"
                                            else -> if (isAmharic) "KG - 12ኛ ክፍል • የመረጃ አያያዝ ስርዓት" else "K-12 • School Management System"
                                        },
                                        fontSize = 11.sp,
                                        color = GoldAccent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            EthiopianFlagAccent()
                            Spacer(modifier = Modifier.height(14.dp))

                            // Hero Classroom Image
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        painter = painterResource(id = R.drawable.school_hero_1792161976345),
                                        contentDescription = "Dilijent Campus",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.85f)
                                                    )
                                                )
                                            )
                                    )
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = GoldAccent
                                        ) {
                                            Text(
                                                text = if (isAmharic) "2017 ዓ.ም የትምህርት ዘመን" else "Academic Year 2017 E.C.",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Text(
                                            text = if (isAmharic) "ትውልድን በእውቀትና በስነ-ምግባር ማነፅ" else "Empowering Generations with Knowledge",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Key Modules Highlights
                            Text(
                                text = if (isAmharic) "✨ በስርዓቱ የተካተቱ አገልግሎቶች፦" else "✨ Included Core Modules:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val highlights = listOf(
                                "📚 " + (if (isAmharic) "የተማሪዎች ምዝገባ እና ሙሉ የግል ማህደር" else "Student Records & Personal Profiles"),
                                "👨‍🏫 " + (if (isAmharic) "የመምህራን እና የክፍለ-ጊዜ ሥራ ክፍፍል" else "Teacher Rosters & Period Workloads"),
                                "💼 " + (if (isAmharic) "የአስተዳደር ሰራተኞች (ፋይናንስ፣ ጸሐፊ፣ ንብረት፣ ፅዳት)" else "Admin Staff (Finance, Secretary, Store, Cleaning)"),
                                "📊 " + (if (isAmharic) "የተማሪዎች የፈተና ውጤት እና የየዕለት ክትትል" else "Grade Assessments & Daily Attendance")
                            )

                            highlights.forEach { itemText ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Text(
                                        text = itemText,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        // Bottom System Status
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EthioGreen.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EthioGreen.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EthioGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isAmharic) "የዳታቤዝ እና የደህንነት ስርዓት ዝግጁ ነው" else "Database & Role Security Active",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Right Sign-In Pane (Interactive Form & Fast Role Selector)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .weight(0.56f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Top bar controls inside Sign-in card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isAmharic) "ወደ ስርዓቱ ይግቡ" else "Sign In to Your Account",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isAmharic) "በስልክ ቁጥርዎ እና በይለፍ ቃልዎ ይግቡ" else "Enter your phone number and password",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Fullscreen Toggle
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isFullScreen) GoldAccent else NavyPrimary.copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onToggleFullScreen() }
                                        .testTag("btn_login_fullscreen_desktop")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                            contentDescription = "Fullscreen",
                                            tint = if (isFullScreen) Color.Black else NavyPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isFullScreen) (if (isAmharic) "ሙሉ" else "Full") else (if (isAmharic) "እስክሪን" else "Fit"),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isFullScreen) Color.Black else NavyPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Language Toggle
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = NavyPrimary.copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onToggleLanguage() }
                                        .testTag("btn_login_language_desktop")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = "Language",
                                            tint = NavyPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isAmharic) "አማ/EN" else "EN/አማ",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Share Web Link Button
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = GoldAccent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { showShareDialog = true }
                                        .testTag("btn_login_share_desktop")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share",
                                            tint = Color.Black,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isAmharic) "ሊንክ አጋራ" else "Share Link",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Error Banner
                        if (errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = errorMessage,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        // Input Form (Email for Admin, Phone for others)
                        LoginFormFields(
                            isAdminMode = isAdminMode,
                            onToggleAdminMode = onToggleAdminMode,
                            email = email,
                            onEmailChange = onEmailChange,
                            phone = phone,
                            onPhoneChange = onPhoneChange,
                            password = password,
                            onPasswordChange = onPasswordChange,
                            passwordVisible = passwordVisible,
                            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                            isLoading = isLoading,
                            isAmharic = isAmharic,
                            onLoginClick = onLoginClick,
                            onRegisterClick = { showRegisterDialog = true },
                            onGoogleSignInSuccess = onGoogleSignInSuccess,
                            onGoogleSignInError = onGoogleSignInError
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Share Web Link Card
                        ShareAppLinkCard(
                            isAmharic = isAmharic,
                            onOpenShareDialog = { showShareDialog = true }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password Hint Card
                        PasswordNoticeCard(isAmharic = isAmharic)
                    }
                }
            }
        } else {
            // ==============================================================
            // PHONE MOBILE MODE (Edge-to-Edge Full Screen In All 4 Directions)
            // ==============================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Action Bar (Full Width, stretched to all edges)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldAccent)
                        ) {
                            if (schoolLogoUri.isNotEmpty()) {
                                AsyncImage(
                                    model = schoolLogoUri,
                                    contentDescription = "School Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isAmharic) schoolNameAm else schoolNameEn,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = when (schoolGradeLevel) {
                                    "1_8" -> if (isAmharic) "1ኛ - 8ኛ ክፍል • መግቢያ" else "Grade 1-8 • Login"
                                    "9_12" -> if (isAmharic) "9ኛ - 12ኛ ክፍል • መግቢያ" else "Grade 9-12 • Login"
                                    else -> if (isAmharic) "KG - 12ኛ ክፍል • መግቢያ" else "K-12 • Login"
                                },
                                fontSize = 10.sp,
                                color = GoldAccent
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Fullscreen Toggle
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isFullScreen) GoldAccent else Color.White.copy(alpha = 0.22f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onToggleFullScreen() }
                                .testTag("btn_login_fullscreen_mobile")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = if (isFullScreen) Color.Black else Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isFullScreen) (if (isAmharic) "ሙሉ" else "Full") else (if (isAmharic) "እስክሪን" else "Fit"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFullScreen) Color.Black else Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Language Toggle
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.22f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onToggleLanguage() }
                                .testTag("btn_login_language_mobile")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Language",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAmharic) "አማ/EN" else "EN/አማ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Mobile Share Web Link Button
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GoldAccent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showShareDialog = true }
                                .testTag("btn_login_share_mobile")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.Black,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAmharic) "ሊንክ" else "Link",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                EthiopianFlagAccent()
                Spacer(modifier = Modifier.height(14.dp))

                // Mobile Sign-In Card (Full Width, No Artificial Limits)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = if (isAmharic) "ወደ ስርዓቱ ይግቡ" else "Sign In to Your Account",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isAmharic) "በስልክ ቁጥርዎ እና በይለፍ ቃልዎ ይግቡ" else "Enter your phone number and password",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = errorMessage,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        // Form fields (Email for Admin, Phone for others)
                        LoginFormFields(
                            isAdminMode = isAdminMode,
                            onToggleAdminMode = onToggleAdminMode,
                            email = email,
                            onEmailChange = onEmailChange,
                            phone = phone,
                            onPhoneChange = onPhoneChange,
                            password = password,
                            onPasswordChange = onPasswordChange,
                            passwordVisible = passwordVisible,
                            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                            isLoading = isLoading,
                            isAmharic = isAmharic,
                            onLoginClick = onLoginClick,
                            onRegisterClick = { showRegisterDialog = true },
                            onGoogleSignInSuccess = onGoogleSignInSuccess,
                            onGoogleSignInError = onGoogleSignInError
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Share Web Link Card
                        ShareAppLinkCard(
                            isAmharic = isAmharic,
                            onOpenShareDialog = { showShareDialog = true }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password Notice Card
                        PasswordNoticeCard(isAmharic = isAmharic)
                    }
                }
            }
        }
    }

    if (showShareDialog) {
        ShareAppDialog(
            isAmharic = isAmharic,
            onDismiss = { showShareDialog = false }
        )
    }

    if (showRegisterDialog) {
        RegisterUserDialog(
            isAmharic = isAmharic,
            onDismiss = { showRegisterDialog = false },
            onRegister = { name, regPhone, regPass, role ->
                onRegisterNewUser(name, regPhone, regPass, role)
                showRegisterDialog = false
            }
        )
    }
}

/**
 * Role definition for the dropdown menu selector
 */
data class LoginRoleItem(
    val titleAm: String,
    val titleEn: String,
    val iconEmoji: String,
    val isAdmin: Boolean,
    val identifierLabelAm: String,
    val identifierLabelEn: String,
    val placeholder: String
)

val loginRoleOptions = listOf(
    LoginRoleItem("መምህር", "Teacher", "👨‍🏫", false, "የመምህር ስልክ ቁጥር", "Teacher Phone Number", "09XXXXXXXX"),
    LoginRoleItem("ሲስተም አድሚን", "System Admin", "👑", true, "የአድሚን ኢሜል አድራሻ", "Admin Email Address", "admin@dilijent.edu.et"),
    LoginRoleItem("ምክትል ር/መምህር", "Vice Principal", "🎖️", false, "የምክትል ር/መ ስልክ ቁጥር", "Vice Principal Phone", "09XXXXXXXX"),
    LoginRoleItem("ርዕሰ መምህር", "Principal", "🎓", false, "የርዕሰ መምህር ስልክ ቁጥር", "Principal Phone", "09XXXXXXXX"),
    LoginRoleItem("ጸሐፊ", "Secretary", "📝", false, "የጸሐፊ ስልክ ቁጥር", "Secretary Phone", "09XXXXXXXX"),
    LoginRoleItem("ፋይናንስ", "Finance", "💰", false, "የፋይናንስ ስልክ ቁጥር", "Finance Phone", "09XXXXXXXX"),
    LoginRoleItem("ፅዳት እና ጥበቃ", "Support Staff", "🧹", false, "የሰራተኛ ስልክ ቁጥር", "Support Staff Phone", "09XXXXXXXX"),
    LoginRoleItem("ተማሪ / ወላጅ", "Student / Parent", "🎒", false, "የተማሪ መለያ ቁጥር ወይም ስልክ", "Student ID or Phone", "DIL-P01 ወይም 09XXXXXXXX")
)

/**
 * Reusable Login Form Input Fields with Role Dropdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginFormFields(
    isAdminMode: Boolean,
    onToggleAdminMode: (Boolean) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    isLoading: Boolean,
    isAmharic: Boolean,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignInSuccess: (name: String, email: String, uid: String) -> Unit,
    onGoogleSignInError: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var selectedRoleIndex by remember { mutableStateOf(if (isAdminMode) 1 else 0) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    val currentRole = loginRoleOptions.getOrElse(selectedRoleIndex) { loginRoleOptions[0] }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 1. Role Selection Dropdown (መምህር፣ አድሚን፣ ምክትል ር/መ፣ ርዕሰ መምህር፣ ጸሐፊ፣ ፋይናንስ፣ ፅዳት፣ ተማሪ)
        Text(
            text = if (isAmharic) "የመግቢያ የስራ ድርሻ (በድሮፕ ዳውን ይምረጡ)፦" else "Select Login Role (Dropdown):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
        )

        ExposedDropdownMenuBox(
            expanded = roleDropdownExpanded,
            onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = "${currentRole.iconEmoji} ${if (isAmharic) currentRole.titleAm else currentRole.titleEn}",
                onValueChange = {},
                readOnly = true,
                label = { Text(if (isAmharic) "የስራ ድርሻ (Role)" else "Designated Role") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = NavyPrimary.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .testTag("dropdown_login_role")
            )
            ExposedDropdownMenu(
                expanded = roleDropdownExpanded,
                onDismissRequest = { roleDropdownExpanded = false }
            ) {
                loginRoleOptions.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = option.iconEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isAmharic) option.titleAm else option.titleEn,
                                        fontWeight = if (selectedRoleIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedRoleIndex == index) NavyPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isAmharic) option.identifierLabelAm else option.identifierLabelEn,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        onClick = {
                            selectedRoleIndex = index
                            onToggleAdminMode(option.isAdmin)
                            roleDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Identifier input: Admin Email vs Phone / Student ID
        if (isAdminMode) {
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text(if (isAmharic) "የአድሚን ኢሜል አድራሻ" else "Admin Email Address") },
                placeholder = { Text("admin@dilijent.edu.et") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = NavyPrimary
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_login_admin_email")
            )
        } else {
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text(if (isAmharic) currentRole.identifierLabelAm else currentRole.identifierLabelEn) },
                placeholder = { Text(currentRole.placeholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = NavyPrimary
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_login_phone")
            )
        }

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(if (isAmharic) "የይለፍ ቃል (Password)" else "Password") },
            placeholder = { Text("••••••••") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = NavyPrimary
                )
            },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    val isValid = if (isAdminMode) email.isNotBlank() && password.isNotBlank()
                                  else phone.isNotBlank() && password.isNotBlank()
                    if (isValid) onLoginClick()
                }
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_login_password")
        )

        // Security requirement notice
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = EthioGreen,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isAmharic) "ደህንነት፦ የይለፍ ቃል ሲቀየር ቢያንስ 8 ፊደላት/ቁጥሮች መሆን አለበት"
                       else "Security: Passwords must be at least 8 characters when updated",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Sign In Button (Stretches Full Width)
        val canSubmit = !isLoading && password.isNotBlank() && (if (isAdminMode) email.isNotBlank() else phone.isNotBlank())
        Button(
            onClick = onLoginClick,
            enabled = canSubmit,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_submit_login")
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (isAdminMode) (if (isAmharic) "እንደ ሲስተም አድሚን ግባ" else "Sign In as Admin")
                           else (if (isAmharic) "ወደ ስርዓቱ ግባ" else "Sign In"),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Google Sign-In with Credential Manager
        GoogleSignInButton(
            onAuthSuccess = onGoogleSignInSuccess,
            onAuthError = onGoogleSignInError
        )

        // Google Sign-In explanation card answering user's question
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = NavyPrimary.copy(alpha = 0.05f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAmharic) "የGoogle መግቢያ ለምን ይጠቅማል?" else "Why Use Google Sign-In?",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isAmharic)
                        "• ከፍተኛ ደህንነት፦ የይለፍ ቃል እንዳይሰረቅ ይጠብቃል።\n• ሚስጥራዊነት፦ እያንዳንዱ መምህር በራሱ ገፅ ብቻ ይገባል።\n• ክላውድ ሲንክ፦ ስልክ ቢቀየር እንኳን መረጃ አይጠፋም።"
                    else
                        "• High Security: 2-Step verified account access.\n• Privacy: Ensures teachers only enter their assigned class marks.\n• Cloud Sync: Restores records anywhere with continuous cloud sync.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }

        // Register Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isAmharic) "አዲስ ሰራተኛ ነዎት? " else "New staff member? ",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isAmharic) "መለያ ይመዝገቡ" else "Register Account",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onRegisterClick() }
                    .padding(4.dp)
                    .testTag("btn_register_new_account")
            )
        }
    }
}

/**
 * Grid of demo roles allowing immediate one-tap testing
 */
@Composable
private fun RoleSelectorGrid(
    roles: List<DemoRoleProfile>,
    columns: Int,
    isAmharic: Boolean,
    onRoleSelected: (identifier: String, isEmail: Boolean, pass: String) -> Unit
) {
    val chunked = roles.chunked(columns)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        chunked.forEach { rowRoles ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowRoles.forEach { roleProfile ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = roleProfile.color.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, roleProfile.color.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onRoleSelected(roleProfile.identifier, roleProfile.isEmail, roleProfile.defaultPass)
                            }
                            .testTag("btn_role_${roleProfile.identifier}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = if (isAmharic) roleProfile.titleAm else roleProfile.titleEn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = roleProfile.color,
                                maxLines = 1
                            )
                            Text(
                                text = roleProfile.identifier,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Password Notice Card explaining role access and default password
 */
@Composable
private fun PasswordNoticeCard(isAmharic: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NavyPrimary.copy(alpha = 0.06f),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = if (isAmharic) "🔒 የይለፍ ቃል እና የስራ ድርሻ ጥበቃ መመሪያ፦" else "🔒 Password & Role Security Guide:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isAmharic) "የይለፍ ቃል ሲቀየር ደህንነቱ የተጠበቀ እንዲሆን ቢያንስ 8 ፊደላት/ቁጥሮች (characters) መሆን አለበት። የአድሚን ገፅ እና የይለፍ ቃል መቆጣጠሪያ በአድሚኑ ብቻ የሚከፈቱ ናቸው።"
                else "Passwords must be at least 8 characters when changed for security. System settings and password controls are strictly restricted to the Admin.",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}

/**
 * Full Registration Dialog allowing new staff to create an account with customized administrative role
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterUserDialog(
    isAmharic: Boolean,
    onDismiss: () -> Unit,
    onRegister: (name: String, phone: String, pass: String, role: String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("መምህር (Teacher)") }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    val roleOptions = listOf(
        "መምህር (Teacher)",
        "ርዕሰ መምህር (Admin)",
        "ምክትል ርዕሰ መምህር",
        "ፋይናንስ / ሂሳብ ሹም",
        "ዋና ጸሐፊ / መዝጋቢ",
        "ንብረት ክፍል ኃላፊ",
        "የፅዳት ሰራተኛ",
        "የጥበቃ ኃላፊ",
        "ሌላ አስተዳደር ሰራተኛ"
    )

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isAmharic) "አዲስ የመግቢያ መለያ መመዝገቢያ" else "Create New Staff Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Text(
                    text = if (isAmharic) "የግል መረጃዎን እና የስራ ድርሻዎን ያስገቡ" else "Enter your profile info and designated role",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(if (isAmharic) "ሙሉ ስም" else "Full Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isAmharic) "ስልክ ቁጥር" else "Phone Number") },
                    placeholder = { Text("09XXXXXXXX") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isAmharic) "የይለፍ ቃል (ቢያንስ 8 ፊደላት/ቁጥሮች)" else "Password (Min 8 chars)") },
                    isError = password.isNotEmpty() && password.trim().length < 8,
                    supportingText = {
                        if (password.isNotEmpty() && password.trim().length < 8) {
                            Text(
                                text = if (isAmharic) "የይለፍ ቃሉ ቢያንስ 8 ፊደላት/ቁጥሮች መሆን አለበት!" else "Must be at least 8 characters!",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp
                            )
                        } else {
                            Text(
                                text = if (isAmharic) "ደህንነቱ የተጠበቀ እንዲሆን ቢያንስ 8 ፊደላት/ቁጥሮች ያስፈልጋል" else "Min 8 characters required",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Role Dropdown
                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isAmharic) "የስራ ድርሻ (Role)" else "Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        roleOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    role = opt
                                    roleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isAmharic) "ይቅር" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (fullName.isNotBlank() && phone.isNotBlank() && password.trim().length >= 8) {
                                onRegister(fullName, phone, password.trim(), role)
                            }
                        },
                        enabled = fullName.isNotBlank() && phone.isNotBlank() && password.trim().length >= 8,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text(if (isAmharic) "ይመዝገቡ" else "Register")
                    }
                }
            }
        }
    }
}
