package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserAccount
import com.example.ui.components.EthiopianFlagAccent
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

@Composable
fun AdminPasswordManagerDialog(
    users: List<UserAccount>,
    isAmharic: Boolean,
    onDismiss: () -> Unit,
    onUpdatePassword: (userId: Long, newPass: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("ሁሉም") }
    var userToEditPassword by remember { mutableStateOf<UserAccount?>(null) }
    var newPasswordInput by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var passwordValidationError by remember { mutableStateOf<String?>(null) }

    // Map to track password visibility toggle per user card
    val revealedPasswords = remember { mutableStateMapOf<Long, Boolean>() }

    val roleFilters = listOf(
        "ሁሉም",
        "አድሚን",
        "ርዕሰ መምህር",
        "መምህር",
        "ተማሪ",
        "ሰራተኛ"
    )

    val filteredUsers = users.filter { u ->
        val matchesQuery = searchQuery.isBlank() ||
                u.fullName.contains(searchQuery, ignoreCase = true) ||
                u.phone.contains(searchQuery) ||
                u.email.contains(searchQuery, ignoreCase = true) ||
                u.role.contains(searchQuery, ignoreCase = true)

        val matchesRole = when (selectedRoleFilter) {
            "ሁሉም" -> true
            "አድሚን" -> u.role.contains("ሲስተም") || u.role.contains("Admin")
            "ርዕሰ መምህር" -> u.role.contains("ርዕሰ መምህር")
            "መምህር" -> u.role.contains("መምህር") && !u.role.contains("ርዕሰ")
            "ተማሪ" -> u.role.contains("ተማሪ") || u.role.contains("Student")
            "ሰራተኛ" -> !u.role.contains("መምህር") && !u.role.contains("ተማሪ") && !u.role.contains("Admin") && !u.role.contains("ሲስተም")
            else -> true
        }

        matchesQuery && matchesRole
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("dialog_admin_password_manager")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isAmharic) "የይለፍ ቃል እና መለያ መቆጣጠሪያ" else "Account & Password Manager",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Text(
                                text = if (isAmharic) "የአድሚን ማስተር ኮንትሮል (Master Credentials)" else "Admin Master Credentials Control",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_password_manager")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                EthiopianFlagAccent()
                Spacer(modifier = Modifier.height(12.dp))

                // Informational Notice Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldAccent.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👑", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAmharic)
                                "አድሚኑ በኢሜል ብቻ ይገባል። የሌሎች ተጠቃሚዎች (ርዕሰ መምህር፣ መምህራን፣ ተማሪዎች፣ ሰራተኞች) መግቢያ በስልክ ቁጥር ነው። አድሚኑ የእያንዳንዱን ተጠቃሚ የይለፍ ቃል ማየት እና መቀየር ይችላል።"
                            else
                                "Admin logs in via Email. Others log in via Phone Number. Admin can view and reset passwords for all accounts.",
                            fontSize = 11.sp,
                            color = NavyPrimary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isAmharic) "በስም፣ በስልክ፣ በኢሜል ወይም በሚና ፈልግ..." else "Search by name, phone, email...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NavyPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_user_accounts")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role Filter Tabs
                ScrollableTabRow(
                    selectedTabIndex = roleFilters.indexOf(selectedRoleFilter).coerceAtLeast(0),
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    roleFilters.forEach { role ->
                        val isSelected = selectedRoleFilter == role
                        Tab(
                            selected = isSelected,
                            onClick = { selectedRoleFilter = role },
                            text = {
                                Text(
                                    text = role,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Users Accounts List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredUsers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isAmharic) "ምንም የተመዘገበ ተጠቃሚ አልተገኘም" else "No matching user accounts found",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredUsers, key = { it.id }) { user ->
                            val isRevealed = revealedPasswords[user.id] ?: false
                            val isAdminUser = user.role.contains("ሲስተም") || user.email.contains("admin", ignoreCase = true)

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isAdminUser) GoldAccent.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                border = if (isAdminUser) androidx.compose.foundation.BorderStroke(1.dp, GoldAccent) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // User Info
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isAdminUser) GoldAccent
                                                    else if (user.role.contains("ርዕሰ መምህር")) NavyPrimary
                                                    else if (user.role.contains("መምህር")) EthioGreen
                                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                )
                                        ) {
                                            Text(
                                                text = if (isAdminUser) "👑"
                                                else if (user.role.contains("ርዕሰ")) "🎓"
                                                else if (user.role.contains("መምህር")) "👨‍🏫"
                                                else if (user.role.contains("ተማሪ")) "👨‍🎓"
                                                else "👤",
                                                fontSize = 18.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = user.fullName,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isAdminUser) GoldAccent else NavyPrimary.copy(alpha = 0.12f)
                                                ) {
                                                    Text(
                                                        text = user.role,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isAdminUser) Color.Black else NavyPrimary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(3.dp))

                                            // Login Identifier (Email for Admin, Phone for others)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isAdminUser || user.email.isNotBlank()) {
                                                    Icon(
                                                        imageVector = Icons.Default.Email,
                                                        contentDescription = "Email",
                                                        tint = NavyPrimary,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = user.email.ifBlank { "admin@dilijent.edu.et" },
                                                        fontSize = 11.sp,
                                                        color = NavyPrimary,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Phone,
                                                        contentDescription = "Phone",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = user.phone,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                // Password Display with reveal toggle
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.White.copy(alpha = 0.7f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Key,
                                                        contentDescription = null,
                                                        tint = Color(0xFFD97706),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (isRevealed) user.password else "••••••••",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB45309)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        contentDescription = "Show",
                                                        tint = Color.Gray,
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clickable {
                                                                revealedPasswords[user.id] = !isRevealed
                                                            }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Action: Change Password Button
                                    Button(
                                        onClick = {
                                            userToEditPassword = user
                                            newPasswordInput = user.password
                                            newPasswordVisible = false
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NavyPrimary,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("btn_change_pass_${user.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isAmharic) "ቀይር" else "Reset",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Close Button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isAmharic) "ጨርስ እና ዝጋ (Done)" else "Close",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Modal Dialog to edit password for selected user
    if (userToEditPassword != null) {
        val targetUser = userToEditPassword!!
        AlertDialog(
            onDismissRequest = { userToEditPassword = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAmharic) "የይለፍ ቃል መቀየሪያ" else "Change Password",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isAmharic)
                            "ለ «${targetUser.fullName}» (${targetUser.role}) አዲስ የይለፍ ቃል ያስገቡ፦"
                        else
                            "Enter new password for ${targetUser.fullName} (${targetUser.role}):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { 
                            newPasswordInput = it
                            if (passwordValidationError != null && it.trim().length >= 8) {
                                passwordValidationError = null
                            }
                        },
                        label = { Text(if (isAmharic) "አዲስ የይለፍ ቃል (ቢያንስ 8 ፊደላት/ቁጥሮች)" else "New Password (Min 8 chars)") },
                        isError = passwordValidationError != null,
                        supportingText = {
                            if (passwordValidationError != null) {
                                Text(
                                    text = passwordValidationError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = if (isAmharic) "ደህንነቱ የተጠበቀ እንዲሆን ቢያንስ 8 ፊደላት/ቁጥሮች ያስፈልጋል" else "Must be at least 8 characters for security",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = NavyPrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_modal_new_password")
                    )

                    // Strong Password suggestions (8+ characters)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "ጠንካራ ምሳሌ:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        listOf("Admin@2026", "Dilijent#88", "EthioPass1", "Secure@2026").forEach { pin ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { 
                                        newPasswordInput = pin 
                                        passwordValidationError = null
                                    }
                            ) {
                                Text(
                                    text = pin,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newPasswordInput.trim()
                        if (trimmed.length < 8) {
                            passwordValidationError = if (isAmharic)
                                "የይለፍ ቃሉ ጠንካራ እንዲሆን ቢያንስ 8 ፊደላት/ቁጥሮች (characters) መሆን አለበት!"
                            else
                                "Password must be at least 8 characters long for security!"
                        } else {
                            passwordValidationError = null
                            onUpdatePassword(targetUser.id, trimmed)
                            userToEditPassword = null
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.testTag("btn_confirm_password_change")
                ) {
                    Text(text = if (isAmharic) "አረጋግጥና መዝግብ" else "Save Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    userToEditPassword = null
                    passwordValidationError = null
                }) {
                    Text(text = if (isAmharic) "ሰርዝ" else "Cancel")
                }
            }
        )
    }
}
