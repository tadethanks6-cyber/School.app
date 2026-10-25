package com.example.ui.components

import android.content.Context
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.EthioRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary
import java.io.File
import java.io.FileOutputStream

/**
 * Copies picked Uri stream to internal app files directory.
 * Guarantees zero-permission permanent file access across app restarts.
 */
fun copyUriToInternalStorage(context: Context, sourceUri: Uri): String {
    return try {
        val fileName = "avatar_${System.currentTimeMillis()}.jpg"
        val destFile = File(context.filesDir, fileName)
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        destFile.absolutePath
    } catch (e: Exception) {
        sourceUri.toString()
    }
}

/**
 * Interactive Profile Photo Picker & Preview component with Android Photo Picker (zero-permission).
 */
@Composable
fun PhotoPickerAvatar(
    photoUri: String,
    onPhotoSelected: (String) -> Unit,
    name: String = "",
    gender: String = "ወንድ",
    isAmharic: Boolean = true,
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Android 13+ & Jetpack Photo Picker (no runtime storage permission required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val permanentPath = copyUriToInternalStorage(context, uri)
            onPhotoSelected(permanentPath)
        }
    }

    val isMale = gender == "ወንድ" || gender.equals("Male", true)
    val fallbackBgColor = if (isMale) NavyPrimary.copy(alpha = 0.12f) else Color(0xFFEC4899).copy(alpha = 0.12f)
    val fallbackTextColor = if (isMale) NavyPrimary else Color(0xFFBE185D)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(fallbackBgColor)
                .border(2.5.dp, GoldAccent, CircleShape)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                .testTag("avatar_photo_picker_box")
        ) {
            if (photoUri.isNotBlank()) {
                val imageModel = if (photoUri.startsWith("/")) {
                    val file = File(photoUri)
                    if (file.exists()) file else photoUri
                } else {
                    photoUri
                }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = if (isAmharic) "የተማሪ/ሰራተኛ ፎቶ" else "Profile Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                if (name.isNotBlank()) {
                    Text(
                        text = name.trim().take(1),
                        fontSize = (size.value * 0.4f).sp,
                        fontWeight = FontWeight.Bold,
                        color = fallbackTextColor
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = fallbackTextColor,
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
            }

            // Camera Badge indicator
            Surface(
                shape = CircleShape,
                color = NavyPrimary,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Pick Photo",
                        tint = GoldAccent,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons Row
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
                modifier = Modifier.testTag("btn_pick_photo")
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = NavyPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (photoUri.isBlank()) {
                        if (isAmharic) "ከስልክ/ጋለሪ ፎቶ ምረጥ" else "Pick from Storage"
                    } else {
                        if (isAmharic) "ፎቶ ቀይር" else "Change Photo"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (photoUri.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { onPhotoSelected("") },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EthioRed),
                    modifier = Modifier.testTag("btn_remove_photo")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = EthioRed
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAmharic) "አስወግድ" else "Remove",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Reusable Circular Avatar with photo support for cards, rows and headers.
 */
@Composable
fun UserAvatar(
    photoUri: String,
    name: String,
    gender: String = "ወንድ",
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val isMale = gender == "ወንድ" || gender.equals("Male", true)
    val fallbackBgColor = if (isMale) NavyPrimary.copy(alpha = 0.15f) else Color(0xFFEC4899).copy(alpha = 0.15f)
    val fallbackTextColor = if (isMale) NavyPrimary else Color(0xFFBE185D)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(fallbackBgColor)
            .border(1.dp, GoldAccent.copy(alpha = 0.4f), CircleShape)
    ) {
        if (photoUri.isNotBlank()) {
            val imageModel = if (photoUri.startsWith("/")) {
                val file = File(photoUri)
                if (file.exists()) file else photoUri
            } else {
                photoUri
            }

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageModel)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = name.trim().take(1).ifBlank { "?" },
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.42f).sp,
                color = fallbackTextColor
            )
        }
    }
}
