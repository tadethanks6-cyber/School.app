package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.SchoolViewModel
import com.example.ui.components.EthiopianFlagAccent
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

@Composable
fun AdminSchoolSettingsDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    val isAmharic by viewModel.isAmharic.collectAsStateWithLifecycle()
    val currentLogoUri by viewModel.schoolLogoUri.collectAsStateWithLifecycle()
    val currentGradeLevel by viewModel.schoolGradeLevel.collectAsStateWithLifecycle()
    val currentNameAm by viewModel.schoolName.collectAsStateWithLifecycle()
    val currentNameEn by viewModel.schoolNameEn.collectAsStateWithLifecycle()
    val currentAcademicYear by viewModel.academicYear.collectAsStateWithLifecycle()

    var selectedGradeLevel by remember(currentGradeLevel) { mutableStateOf(currentGradeLevel) }
    var nameAm by remember(currentNameAm) { mutableStateOf(currentNameAm) }
    var nameEn by remember(currentNameEn) { mutableStateOf(currentNameEn) }
    var academicYearState by remember(currentAcademicYear) { mutableStateOf(currentAcademicYear) }
    var logoUriState by remember(currentLogoUri) { mutableStateOf(currentLogoUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedUri = viewModel.saveSchoolLogo(uri)
            if (savedUri.isNotEmpty()) {
                logoUriState = savedUri
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = NavyPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isAmharic) "የትምህርት ቤት ቅንብር" else "School Profile & Settings",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Text(
                                text = if (isAmharic) "አርማ (Logo) እና የትምህርት ደረጃ መቆጣጠሪያ" else "Logo & Grade Level Configuration",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_school_settings")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                EthiopianFlagAccent()
                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Dynamic School Logo
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isAmharic) "1. የትምህርት ቤቱ አርማ (School Logo)" else "1. School Logo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyPrimary
                        )
                        Text(
                            text = if (isAmharic) "ፎቶ በመስቀል በማንኛውም ጊዜ አርማውን ይቀይሩ። ወዲያውኑ በዳሽቦርድ፣ በካርድ እና በርዕሰ መረጃዎች ላይ ይታያል"
                            else "Upload any photo from your device. It updates instantly across headers and report cards.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Logo Preview
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, GoldAccent, CircleShape)
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                if (logoUriState.isNotEmpty()) {
                                    AsyncImage(
                                        model = logoUriState,
                                        contentDescription = "School Logo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = "Default School Cap",
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("btn_pick_school_logo")
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isAmharic) "አዲስ ፎቶ ይምረጡ" else "Select Logo Photo",
                                        fontSize = 12.sp
                                    )
                                }

                                if (logoUriState.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.resetSchoolLogo()
                                            logoUriState = ""
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("btn_reset_school_logo")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isAmharic) "ወደ ነባሪ መልስ" else "Reset to Default",
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: School Grade Level Selection
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isAmharic) "2. የትምህርት ቤቱ ደረጃ (School Grade Level)" else "2. School Grade Level",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyPrimary
                        )
                        Text(
                            text = if (isAmharic) "የትምህርት ቤቱን ደረጃ በመምረጥ የተማሪዎችን፣ የክፍሎችን እና የመምህራንን መዝገብ ይወስኑ፦"
                            else "Select your school level to adjust grade filters, class registration, and lists:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val levels = listOf(
                            Triple("1_8", "1ኛ - 8ኛ ክፍል (Primary & Middle School)", "የ1ኛ እስከ 8ኛ ክፍል ተማሪዎች እና የትምህርት ዓይነቶች"),
                            Triple("9_12", "9ኛ - 12ኛ ክፍል (High School)", "የ9ኛ እስከ 12ኛ ክፍል ተማሪዎች፣ የተፈጥሮ እና ማህበራዊ ሳይንስ"),
                            Triple("ALL", "KG - 12ኛ ክፍል (ሙሉ አጠቃላይ ትምህርት ቤት)", "ከመዋዕለ ህፃናት እስከ 12ኛ ክፍል ሙሉ ደረጃ")
                        )

                        levels.forEach { (key, title, subtitle) ->
                            val isSelected = selectedGradeLevel == key
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) NavyPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) NavyPrimary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedGradeLevel = key }
                                    .testTag("opt_grade_level_$key")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedGradeLevel = key },
                                        colors = RadioButtonDefaults.colors(selectedColor = NavyPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = subtitle,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: School Name
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isAmharic) "3. የትምህርት ቤቱ መጠሪያ ስም (School Name)" else "3. School Name",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = nameAm,
                            onValueChange = { nameAm = it },
                            label = { Text(if (isAmharic) "የትምህርት ቤቱ ስም (በአማርኛ)" else "School Name (Amharic)") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_school_name_am")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = nameEn,
                            onValueChange = { nameEn = it },
                            label = { Text(if (isAmharic) "የትምህርት ቤቱ ስም (በእንግሊዝኛ)" else "School Name (English)") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_school_name_en")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Academic Year (ዓመተ ምህረት)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isAmharic) "4. የትምህርት ዘመን / ዓመተ ምህረት (Academic Year)" else "4. Academic Year",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = NavyPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isAmharic)
                                "በአድሚኑ የሚሞላ ተለዋዋጭ የትምህርት ዘመን (በሪፖርት ካርድ፣ በክፍል መዝገብ እና በአቴንዳንስ ላይ በቀጥታ የሚንጸባረቅ)"
                            else
                                "Configurable school academic year reflected across reports, roster, and attendance records.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick selection chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            listOf("2016 ዓ.ም", "2017 ዓ.ም", "2018 ዓ.ም", "2019 ዓ.ም").forEach { yr ->
                                FilterChip(
                                    selected = academicYearState.trim() == yr,
                                    onClick = { academicYearState = yr },
                                    label = { Text(yr, fontSize = 12.sp) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = academicYearState,
                            onValueChange = { academicYearState = it },
                            label = { Text(if (isAmharic) "ዓመተ ምህረት (Academic Year) *" else "Academic Year *") },
                            placeholder = { Text("ለምሳሌ፡ 2017 ዓ.ም") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_school_academic_year")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_cancel_school_settings")
                    ) {
                        Text(if (isAmharic) "ሰርዝ" else "Cancel")
                    }

                    Button(
                        onClick = {
                            viewModel.updateSchoolSettings(
                                gradeLevel = selectedGradeLevel,
                                nameAm = nameAm.ifBlank { "ዲሊጀንት ትምህርት ቤት" },
                                nameEn = nameEn.ifBlank { "Dilijent School" },
                                year = academicYearState.ifBlank { "2017 ዓ.ም" }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EthioGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_save_school_settings")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAmharic) "አስቀምጥ" else "Save Settings",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
