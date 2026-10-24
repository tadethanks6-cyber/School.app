package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EthioGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

/**
 * The permanent Shared Web Browser URL for users to access the app in any browser.
 */
const val SHARED_APP_URL = "https://ais-pre-h5pu5r2hyqqe3gavew6aqz-903205459325.europe-west2.run.app"

/**
 * Copies the shared URL to system clipboard and shows a user confirmation toast.
 */
fun copySharedLinkToClipboard(context: Context, clipboardManager: androidx.compose.ui.platform.ClipboardManager) {
    clipboardManager.setText(AnnotatedString(SHARED_APP_URL))
    Toast.makeText(
        context,
        "የመተግበሪያው ሊንክ ተገልብጧል (Link Copied successfully)!",
        Toast.LENGTH_SHORT
    ).show()
}

/**
 * Launches the native Android Share sheet (Telegram, WhatsApp, SMS, Email, etc.).
 */
fun launchShareIntent(context: Context, isAmharic: Boolean) {
    val shareText = if (isAmharic) {
        """
        🎓 የዲሊጀንት ትምህርት ቤት አስተዳደር መተግበሪያ (School Management App)
        
        ያለ ምንም መጫን (No Installation Required) በማንኛውም ብሮውዘር (Chrome, Safari, Firefox) በቀጥታ በስልክም በኮምፒውተርም ለመጠቀም ይህንን ሊንክ ይጫኑ፦
        🔗 $SHARED_APP_URL
        
        ለርዕሰ መምህር፣ መምህራን፣ ተማሪዎች እና ሰራተኞች የተዘጋጀ ሙሉ ሲስተም!
        """.trimIndent()
    } else {
        """
        🎓 Dilijent School Management System
        
        Access directly from any web browser (Chrome, Safari, Firefox) on mobile or desktop without installation:
        🔗 $SHARED_APP_URL
        
        Complete role-based management for Principals, Teachers, Students, and Staff!
        """.trimIndent()
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, if (isAmharic) "የትምህርት ቤት መተግበሪያ ሊንክ" else "School App Link")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, if (isAmharic) "ሊንኩን አጋራ" else "Share App Link"))
}

/**
 * Opens the shared app URL in the device's default web browser.
 */
fun openInBrowser(context: Context) {
    try {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(SHARED_APP_URL)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(browserIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "ብሮውዘር መክፈት አልተቻለም: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Full Dialog to share the app web link and explain how users can open it seamlessly in any browser.
 */
@Composable
fun ShareAppDialog(
    isAmharic: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NavyPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isAmharic) "መተግበሪያውን በብሮውዘር አጋራ" else "Share Web Browser Link",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = if (isAmharic) "ያለ ምንም መጫን በሊንክ ብቻ ይሰራል" else "Instant access with no install required",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EthiopianFlagAccent()

                // Explanation Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EthioGreen.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EthioGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAmharic)
                                "✓ ተጠቃሚዎች ይህንን ሊንክ በስልካቸው ወይም በኮምፒውተራቸው Chrome፣ Safari ወይም Firefox ላይ በቀጥታ ከፍተው መጠቀም ይችላሉ። ምንም አይነት APK ማውረድ አያስፈልግም!"
                            else
                                "✓ Anyone can open this link directly in Chrome, Safari, or Firefox on phone or PC. Zero download or APK install needed!",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Copyable Link Box
                Text(
                    text = if (isAmharic) "የማጋሪያ ሊንክ (Web Link):" else "Shareable Web Link:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyPrimary
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            copySharedLinkToClipboard(context, clipboardManager)
                            copied = true
                        }
                        .testTag("box_copy_link")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = SHARED_APP_URL,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = NavyPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (copied) EthioGreen else NavyPrimary,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (copied) (if (isAmharic) "ተገልብጧል" else "Copied") else (if (isAmharic) "ቅዳ" else "Copy"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Instructions Checklist
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isAmharic) "📌 እንዴት ማጋራት ይችላሉ?" else "📌 How to share with users:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Text(
                            text = if (isAmharic)
                                "1. «በቴሌግራም/ዋትስአፕ አጋራ» የሚለውን ተጭነው ለት/ቤት ግሩፖች ይላኩ።\n2. ወይም «ሊንክ ቅዳ» ብለው በSMS፣ ኢሜይል ወይም በማህበራዊ ገጽ ይለጥፉ።\n3. መምህራን እና ተማሪዎች ሊንኩን ሲጫኑ በቅጽበት ወደ ሲስተሙ ይገባሉ።"
                            else
                                "1. Tap 'Share via Apps' to send directly to Telegram/WhatsApp groups.\n2. Or tap 'Copy' and paste via SMS, email, or social media.\n3. Teachers and students open the link to access their portal immediately.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    launchShareIntent(context, isAmharic)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_dialog_share_apps")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAmharic) "በቴሌግራም / WhatsApp አጋራ" else "Share via Apps",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    openInBrowser(context)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_dialog_open_browser")
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInBrowser,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isAmharic) "በብሮውዘር ክፈት" else "Open in Browser",
                    fontSize = 12.sp
                )
            }
        }
    )
}

/**
 * Prominent card placed on Dashboard or Login screens encouraging sharing the web link.
 */
@Composable
fun ShareAppLinkCard(
    isAmharic: Boolean,
    onOpenShareDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_share_app_link")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GoldAccent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isAmharic) "🌐 መተግበሪያውን በብሮውዘር አጋሩ" else "🌐 Share App via Web Link",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isAmharic) "ማንኛውም ሰው በChrome/Safari በሊንክ ይከፍተዋል" else "Instant access in any browser without install",
                            fontSize = 11.sp,
                            color = GoldAccent
                        )
                    }
                }
            }

            Text(
                text = if (isAmharic)
                    "መምህራን እና ተማሪዎች ምንም አይነት ፋይል ሳያወርዱ በስልካቸው ወይም በኮምፒውተራቸው ይህንን ሊንክ በመጫን በቀጥታ ገብተው ማርክ መሙላት ወይም ውጤታቸውን ማየት ይችላሉ።"
                else
                    "Teachers and students can access their private portals directly in any web browser without downloading or installing any file.",
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Copy Link Button
                Button(
                    onClick = {
                        copySharedLinkToClipboard(context, clipboardManager)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_card_copy_link")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAmharic) "ሊንክ ቅዳ" else "Copy Link",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Share Button
                Button(
                    onClick = onOpenShareDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_card_share_dialog")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAmharic) "አጋራ / ዝርዝር" else "Share Details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
