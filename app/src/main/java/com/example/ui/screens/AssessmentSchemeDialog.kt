package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.example.data.model.AssessmentScheme
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.EthioRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentSchemeDialog(
    currentScheme: AssessmentScheme,
    isAmharic: Boolean,
    onDismiss: () -> Unit,
    onSave: (AssessmentScheme) -> Unit
) {
    var selectedPresetId by remember { mutableStateOf(currentScheme.id) }
    var schoolType by remember { mutableStateOf(currentScheme.schoolType) }
    var titleAmharic by remember { mutableStateOf(currentScheme.titleAmharic) }

    var t1Label by remember { mutableStateOf(currentScheme.test1Label) }
    var t1Weight by remember { mutableStateOf(currentScheme.test1Weight.toString()) }

    var asLabel by remember { mutableStateOf(currentScheme.assignmentLabel) }
    var asWeight by remember { mutableStateOf(currentScheme.assignmentWeight.toString()) }

    var midLabel by remember { mutableStateOf(currentScheme.midExamLabel) }
    var midWeight by remember { mutableStateOf(currentScheme.midExamWeight.toString()) }

    var finalLabel by remember { mutableStateOf(currentScheme.finalExamLabel) }
    var finalWeight by remember { mutableStateOf(currentScheme.finalExamWeight.toString()) }

    var errorMsg by remember { mutableStateOf("") }

    val calculatedTotal = (t1Weight.toFloatOrNull() ?: 0f) +
        (asWeight.toFloatOrNull() ?: 0f) +
        (midWeight.toFloatOrNull() ?: 0f) +
        (finalWeight.toFloatOrNull() ?: 0f)

    val isTotalValid = calculatedTotal in 99.9f..100.1f

    val handlePresetSelect: (AssessmentScheme) -> Unit = { preset ->
        selectedPresetId = preset.id
        schoolType = preset.schoolType
        titleAmharic = preset.titleAmharic
        t1Label = preset.test1Label
        t1Weight = preset.test1Weight.toString()
        asLabel = preset.assignmentLabel
        asWeight = preset.assignmentWeight.toString()
        midLabel = preset.midExamLabel
        midWeight = preset.midExamWeight.toString()
        finalLabel = preset.finalExamLabel
        finalWeight = preset.finalExamWeight.toString()
        errorMsg = ""
    }

    val handleSave = {
        if (!isTotalValid) {
            errorMsg = if (isAmharic)
                "የምዘናዎቹ ድምር በትክክል 100% መሆን አለበት! (አሁን: ${String.format("%.1f", calculatedTotal)}%)"
            else
                "Total weight must equal exactly 100%! (Current: ${String.format("%.1f", calculatedTotal)}%)"
        } else {
            val scheme = AssessmentScheme(
                id = selectedPresetId,
                titleAmharic = titleAmharic,
                titleEnglish = if (schoolType == "የግል") "Private School Scheme" else "Standard MOE Scheme",
                schoolType = schoolType,
                test1Label = t1Label.ifBlank { "ጥያቄ 1" },
                test1Weight = t1Weight.toFloatOrNull() ?: 0f,
                assignmentLabel = asLabel.ifBlank { "የቤት ስራ" },
                assignmentWeight = asWeight.toFloatOrNull() ?: 0f,
                midExamLabel = midLabel.ifBlank { "አጋማሽ ፈተና" },
                midExamWeight = midWeight.toFloatOrNull() ?: 0f,
                finalExamLabel = finalLabel.ifBlank { "ማጠቃለያ ፈተና" },
                finalExamWeight = finalWeight.toFloatOrNull() ?: 0f
            )
            onSave(scheme)
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
                        Column {
                            Text(
                                text = if (isAmharic) "የውጤት አሰጣጥ ማዋቀሪያ (ርዕሰ መ/ር)" else "Grading System Config",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isAmharic) "እንደ ት/ቤቱ ፍላጎት (መንግስት / የግል)" else "For Public & Private Schools",
                                fontSize = 11.sp,
                                color = GoldAccent
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
                        Button(
                            onClick = { handleSave() },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("btn_save_scheme")
                        ) {
                            Text(
                                text = if (isAmharic) "አስቀምጥ" else "Save",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
                )
            }
        ) { innerPadding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Info Banner
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🇪🇹", fontSize = 24.sp, modifier = Modifier.padding(end = 10.dp))
                        Column {
                            Text(
                                text = if (isAmharic) "ተለዋዋጭ የኢትዮጵያ ስርዓተ-ትምህርት ምዘና" else "Flexible Ethiopian Curriculum Evaluation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NavyPrimary
                            )
                            Text(
                                text = if (isAmharic)
                                    "ለግልም ሆነ ለመንግስት ትምህርት ቤቶች እንደፍላጎትዎ የምዘና ዓይነቶችን እና የክብደት መቶኛዎችን (Weights) እዚህ መወሰን ይችላሉ።"
                                else
                                    "Configure custom assessment weights and labels for both private and public schools.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Error Message if any
                if (errorMsg.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EthioRed.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMsg,
                            color = EthioRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // School Type Switcher
                Text(
                    text = if (isAmharic) "1. የትምህርት ቤቱ ዓይነት (School Type)" else "1. School Type",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val isPublic = schoolType == "የመንግስት"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPublic) NavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                schoolType = "የመንግስት"
                                handlePresetSelect(AssessmentScheme.MOE_STANDARD)
                            }
                    ) {
                        Text(
                            text = if (isAmharic) "🏛️ የመንግስት ት/ቤት" else "🏛️ Public School",
                            color = if (isPublic) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                        )
                    }

                    val isPrivate = schoolType == "የግል"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPrivate) NavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                schoolType = "የግል"
                                handlePresetSelect(AssessmentScheme.PRIVATE_MODEL_A)
                            }
                    ) {
                        Text(
                            text = if (isAmharic) "🏫 የግል ትምህርት ቤት" else "🏫 Private School",
                            color = if (isPrivate) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Presets list
                Text(
                    text = if (isAmharic) "2. ዝግጁ ሞዴሎች (Ready Presets)" else "2. Standard Templates",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                AssessmentScheme.PRESETS.forEach { preset ->
                    val isSelected = selectedPresetId == preset.id
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) NavyPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { handlePresetSelect(preset) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { handlePresetSelect(preset) }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAmharic) preset.titleAmharic else preset.titleEnglish,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) NavyPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${preset.test1Label}: ${preset.test1Weight.toInt()}% | ${preset.assignmentLabel}: ${preset.assignmentWeight.toInt()}% | ${preset.midExamLabel}: ${preset.midExamWeight.toInt()}% | ${preset.finalExamLabel}: ${preset.finalExamWeight.toInt()}%",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Custom Option
                val isCustom = selectedPresetId == "custom"
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCustom) NavyPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isCustom) 2.dp else 1.dp,
                        color = if (isCustom) NavyPrimary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedPresetId = "custom"
                            titleAmharic = "ብጁ ስርዓተ-ትምህርት"
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        RadioButton(
                            selected = isCustom,
                            onClick = {
                                selectedPresetId = "custom"
                                titleAmharic = "ብጁ ስርዓተ-ትምህርት"
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAmharic) "✏️ ብጁ ማዋቀሪያ (Custom Configuration)" else "✏️ Custom Configuration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isCustom) NavyPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAmharic)
                                    "የትምህርት ቤቱን የራስዎ ምዘና ስሞች እና የክብደት መቶኛዎችን (Weights) እራስዎ ያስገቡ።"
                                else
                                    "Specify custom assessment component names and weights manually.",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Component Configuration Fields
                Text(
                    text = if (isAmharic) "3. የምዘና ክፍሎች እና ክብደት (Components & Weights)" else "3. Component Breakdown",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                // Component 1
                AssessmentFieldRow(
                    labelNumber = "1",
                    nameValue = t1Label,
                    onNameChange = {
                        t1Label = it
                        selectedPresetId = "custom"
                    },
                    namePlaceholder = if (isAmharic) "ምዘና 1 ስም" else "Component 1 Name",
                    weightValue = t1Weight,
                    onWeightChange = {
                        t1Weight = it
                        selectedPresetId = "custom"
                    }
                )

                // Component 2
                AssessmentFieldRow(
                    labelNumber = "2",
                    nameValue = asLabel,
                    onNameChange = {
                        asLabel = it
                        selectedPresetId = "custom"
                    },
                    namePlaceholder = if (isAmharic) "ምዘና 2 ስም" else "Component 2 Name",
                    weightValue = asWeight,
                    onWeightChange = {
                        asWeight = it
                        selectedPresetId = "custom"
                    }
                )

                // Component 3
                AssessmentFieldRow(
                    labelNumber = "3",
                    nameValue = midLabel,
                    onNameChange = {
                        midLabel = it
                        selectedPresetId = "custom"
                    },
                    namePlaceholder = if (isAmharic) "ምዘና 3 ስም" else "Component 3 Name",
                    weightValue = midWeight,
                    onWeightChange = {
                        midWeight = it
                        selectedPresetId = "custom"
                    }
                )

                // Component 4
                AssessmentFieldRow(
                    labelNumber = "4",
                    nameValue = finalLabel,
                    onNameChange = {
                        finalLabel = it
                        selectedPresetId = "custom"
                    },
                    namePlaceholder = if (isAmharic) "ምዘና 4 ስም" else "Component 4 Name",
                    weightValue = finalWeight,
                    onWeightChange = {
                        finalWeight = it
                        selectedPresetId = "custom"
                    }
                )

                // Total Summary Indicator
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTotalValid) EthioGreen.copy(alpha = 0.12f) else EthioRed.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isAmharic) "ጠቅላላ የክብደት ድምር (Total Weight)" else "Total Weight Sum",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isTotalValid)
                                    (if (isAmharic) "✓ ትክክል ነው (100% ተሟልቷል)" else "✓ Valid (100% complete)")
                                else
                                    (if (isAmharic) "⚠️ ድምሩ 100% መሆን አለበት" else "⚠️ Must equal exactly 100%"),
                                fontSize = 11.sp,
                                color = if (isTotalValid) EthioGreen else EthioRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${String.format("%.1f", calculatedTotal)}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isTotalValid) EthioGreen else EthioRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AssessmentFieldRow(
    labelNumber: String,
    nameValue: String,
    onNameChange: (String) -> Unit,
    namePlaceholder: String,
    weightValue: String,
    onWeightChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = NavyPrimary.copy(alpha = 0.1f),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(labelNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
            }
        }

        OutlinedTextField(
            value = nameValue,
            onValueChange = onNameChange,
            placeholder = { Text(namePlaceholder, fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = weightValue,
            onValueChange = onWeightChange,
            suffix = { Text("%", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.width(86.dp)
        )
    }
}
