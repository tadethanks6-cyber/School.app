package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.EthioRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

@Composable
fun ImportExportCenterDialog(
    initialTarget: String = "students",
    isAmharic: Boolean = true,
    onImportStudents: (String, (Int, String) -> Unit) -> Unit,
    onImportTeachers: (String, (Int, String) -> Unit) -> Unit,
    onExportStudents: () -> String,
    onExportTeachers: () -> String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTarget by remember { mutableStateOf(initialTarget) } // "students" or "teachers"
    var rawInputText by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var parsedPreviewRows by remember { mutableStateOf<List<List<String>>>(emptyList()) }

    // Android System File Picker for CSV / text / excel files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                rawInputText = content
                selectedFileName = uri.lastPathSegment ?: "document.csv"
                statusMessage = if (isAmharic) "ፋይሉ በተሳካ ሁኔታ ተነቧል!" else "File read successfully!"
                
                // Parse rows for preview
                val lines = content.lines().map { it.trim() }.filter { it.isNotBlank() }
                parsedPreviewRows = lines.take(6).map { line ->
                    line.split(Regex("[,;\t]")).map { it.trim().removeSurrounding("\"") }
                }
            } catch (e: Exception) {
                statusMessage = if (isAmharic) "ፋይሉን በማንበብ ወቅት ስህተት ተፈጥሯል: ${e.message}" else "Error reading file: ${e.message}"
            }
        }
    }

    val sampleStudentsTemplate = """
የተማሪ ስም, ጾታ, ክፍል, የአሳዳጊ ስልክ
ዳንኤል ሰለሞን, ወንድ, 12ኛ ክፍል, +251 911 345678
ትዕግስት ታከለ, ሴት, 12ኛ ክፍል, +251 922 456789
በረከት ደሳለኝ, ወንድ, 11ኛ ክፍል, +251 933 567890
ማህሌት ግርማ, ሴት, 10ኛ ክፍል, +251 944 678901
""".trimIndent()

    val sampleTeachersTemplate = """
ሙሉ ስም, ዲፓርትመንት, ስልክ ቁጥር, የትምህርት ደረጃ
መምህር ታምራት አበበ, ሒሳብ (Mathematics), +251 911 889900, ባችለር ዲግሪ
መምህርት ትርሀስ ገብሩ, ፊዚክስ (Physics), +251 922 778899, ማስተርስ
መምህር ኤልያስ ተፈሪ, እንግሊዝኛ (English), +251 933 667788, ባችለር ዲግሪ
""".trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isAmharic) "📥 የፋይል ማስመጫ እና መላኪያ ማዕከል" else "📥 Import & Export Center",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = NavyPrimary
                    )
                    Text(
                        text = if (isAmharic) "Excel / CSV / PDF ሰንጠረዥን በቀጥታ ወደ ሲስተሙ መመዝገብ" else "Directly import Excel / CSV / Table into system",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Target Switcher: Students vs Teachers
                TabRow(
                    selectedTabIndex = if (selectedTarget == "students") 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = NavyPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedTarget == "students",
                        onClick = {
                            selectedTarget = "students"
                            parsedPreviewRows = emptyList()
                        },
                        text = {
                            Text(
                                text = if (isAmharic) "👨‍🎓 የተማሪዎች መዝገብ" else "👨‍🎓 Students",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTarget == "teachers",
                        onClick = {
                            selectedTarget = "teachers"
                            parsedPreviewRows = emptyList()
                        },
                        text = {
                            Text(
                                text = if (isAmharic) "👨‍🏫 የመምህራን መዝገብ" else "👨‍🏫 Teachers",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // File Chooser Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(text = "📊", fontSize = 28.sp)
                        Text(
                            text = if (isAmharic) "ከስልክዎ ፋይል ይምረጡ (Excel / CSV / Text)" else "Choose file from device (Excel / CSV / Text)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NavyPrimary
                        )
                        Text(
                            text = if (isAmharic) "ፋይሉ በራሱ ተነቦ ወደ ሲስተሙ CSV ይቀየራል" else "File is read and converted to CSV automatically",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                filePickerLauncher.launch("*/*")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (isAmharic) "ፋይል ምረጥ (Browse File)" else "Browse File", fontSize = 12.sp)
                        }
                        if (selectedFileName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "✓ $selectedFileName",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EthioGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Raw Input Text Area (Supports direct paste from Excel, Word, or PDF tables)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAmharic) "ወይም ሰንጠረዡን እዚህ ይለጥፉ (Paste Text):" else "Or paste table text here:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = {
                            rawInputText = if (selectedTarget == "students") sampleStudentsTemplate else sampleTeachersTemplate
                            val lines = rawInputText.lines().map { it.trim() }.filter { it.isNotBlank() }
                            parsedPreviewRows = lines.take(6).map { line ->
                                line.split(Regex("[,;\t]")).map { it.trim().removeSurrounding("\"") }
                            }
                        }
                    ) {
                        Text(text = if (isAmharic) "📄 የናሙና ጽሑፍ ሙላ" else "Fill Sample", fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = rawInputText,
                    onValueChange = {
                        rawInputText = it
                        val lines = it.lines().map { l -> l.trim() }.filter { l -> l.isNotBlank() }
                        parsedPreviewRows = lines.take(6).map { line ->
                            line.split(Regex("[,;\t]")).map { c -> c.trim().removeSurrounding("\"") }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    placeholder = {
                        Text(
                            text = if (selectedTarget == "students")
                                "ስም, ጾታ, ክፍል, ስልክ\nአበበ ታደሰ, ወንድ, 12A, 0911223344\nሳራ ከበደ, ሴት, 11B, 0922334455"
                            else
                                "ሙሉ ስም, ዲፓርትመንት, ስልክ, የትምህርት ደረጃ\nመምህር ዳዊት በቀለ, ሒሳብ, 0911223344, ባችለር",
                            fontSize = 11.sp
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                )

                // Status Message Banner
                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = statusMessage ?: "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Table Preview Box
                if (parsedPreviewRows.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isAmharic) "የመረጃ ቅድመ-ዕይታ (Preview):" else "Data Preview:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(6.dp)
                        ) {
                            parsedPreviewRows.forEachIndexed { rowIdx, cols ->
                                Row(
                                    modifier = Modifier
                                        .background(if (rowIdx == 0) NavyPrimary.copy(alpha = 0.1f) else Color.Transparent)
                                        .padding(vertical = 3.dp)
                                ) {
                                    cols.forEach { cell ->
                                        Text(
                                            text = cell,
                                            fontSize = 10.sp,
                                            fontWeight = if (rowIdx == 0) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier
                                                .width(105.dp)
                                                .padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                                if (rowIdx < parsedPreviewRows.lastIndex) {
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Export Button (ወደ CSV ፋይል ላክ)
                OutlinedButton(
                    onClick = {
                        val csv = if (selectedTarget == "students") onExportStudents() else onExportTeachers()
                        val filename = if (selectedTarget == "students") "dilijent_students.csv" else "dilijent_teachers.csv"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csv)
                            putExtra(Intent.EXTRA_TITLE, filename)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, if (isAmharic) "የCSV መረጃውን ላክ" else "Share CSV Data"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAmharic) "📤 አሁን ያለውን የ${if (selectedTarget == "students") "ተማሪዎች" else "መምህራን"} መዝገብ ወደ CSV ላክ"
                        else "📤 Export Current ${if (selectedTarget == "students") "Students" else "Teachers"} to CSV",
                        fontSize = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rawInputText.isBlank()) {
                        Toast.makeText(context, if (isAmharic) "እባክዎ ፋይል ይምረጡ ወይም ጽሁፍ ያስገቡ" else "Please select file or enter text", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isProcessing = true
                    if (selectedTarget == "students") {
                        onImportStudents(rawInputText) { count, msg ->
                            isProcessing = false
                            statusMessage = msg
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (count > 0) {
                                onDismiss()
                            }
                        }
                    } else {
                        onImportTeachers(rawInputText) { count, msg ->
                            isProcessing = false
                            statusMessage = msg
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (count > 0) {
                                onDismiss()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EthioGreen),
                enabled = !isProcessing && rawInputText.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_confirm_import")
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = if (isAmharic) "💾 ወደ ሲስተሙ መዝግብ (Import Now)" else "💾 Save to System",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = if (isAmharic) "ሰርዝ" else "Cancel", fontSize = 12.sp)
            }
        }
    )
}
