package com.example.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File

object ShareAppHelper {

    /**
     * Shares the actual APK file directly so it can be sent via Bluetooth, Nearby Share,
     * Telegram, WhatsApp, ShareMe, Zapya, or other file sharing utilities.
     */
    fun shareApkFile(context: Context) {
        try {
            val appInfo = context.applicationInfo
            val sourceApk = File(appInfo.sourceDir)
            if (!sourceApk.exists()) {
                shareAppDetailsText(context)
                return
            }

            // Create share URI using FileProvider
            val authority = "${context.packageName}.fileprovider"
            val apkUri: Uri = try {
                FileProvider.getUriForFile(context, authority, sourceApk)
            } catch (e: Exception) {
                // Fallback: Copy to cache and share if root provider throws
                val cacheFile = File(context.cacheDir, "EpicKeyboard.apk")
                sourceApk.copyTo(cacheFile, overwrite = true)
                FileProvider.getUriForFile(context, authority, cacheFile)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "اپلیکیشن کیبورد حماسی (Epic Keyboard) - نسخه کامل APK"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "🔥 اپلیکیشن «کیبورد حماسی» (Epic Keyboard) را دریافت کنید!\n" +
                            "• پشتیبانی کامل از زبان‌های فارسی، تورکجه، ترکی استانبولی، آذربایجانی لاتین، عربی و انگلیسی\n" +
                            "• اعراب و حرکه‌ها، نوار پیشنهاد کلمات، حالت‌های شناور و یک‌دست، تم‌های مدرن و فونت‌های زیبا"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "اشتراک‌گذاری فایل نصبی اپلیکیشن (APK) از طریق:").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "اشتراک مستقیم فایل با خطا مواجه شد، اشتراک متنی فراخوانی می‌شود.", Toast.LENGTH_SHORT).show()
            shareAppDetailsText(context)
        }
    }

    /**
     * Shares app details, features, and package link via text for social & messengers.
     */
    fun shareAppDetailsText(context: Context) {
        try {
            val shareText = """
                🌟 کیبورد حماسی (Epic Keyboard)
                یک کیبورد فوق‌العاده مدرن، سریع و هوشمند با امکانات حرفه‌ای:
                ✓ پشتیبانی همزمان از فارسی، تورکجه (عربی و لاتین)، ترکی استانبولی، عربی و انگلیسی
                ✓ دو ردیف کامل علائم، حرکه‌ها و اعراب با چیدمان استاندارد و معکوس
                ✓ چیدمان بدون قرینگی برای تمامی زبان‌های راست‌به‌چپ (ISIRI استاندارد)
                ✓ حالت شناور معلق، حالت یک‌دست چپ و راست، تم‌های مدرن و پالت رنگی دلخواه
                ✓ فونت‌های اختصاصی وزیرمتن، نوتو سنس، نوتو نسخ، پاپینز و تنظیم اندازه فونت هر بخش
                
                نام پکیج: ${context.packageName}
            """.trimIndent()

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "کیبورد حماسی (Epic Keyboard)")
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, "ارسال اطلاعات کیبورد حماسی به دوستان:")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "خطا در ارسال: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * A prominent, distinctive share button displayed prominently at the top of screens.
 * Features a glowing gradient, icon, and animated press state.
 */
@Composable
fun DistinctiveShareButton(
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    text: String = "اشتراک‌گذاری برنامه",
    onClick: () -> Unit
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF6366F1), // Indigo
            Color(0xFF8B5CF6), // Purple
            Color(0xFFEC4899)  // Pink accent
        )
    )

    Surface(
        modifier = modifier
            .testTag("share_app_button")
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .shadow(6.dp, RoundedCornerShape(14.dp)),
        color = Color.Transparent,
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(
                    horizontal = if (isCompact) 10.dp else 14.dp,
                    vertical = if (isCompact) 6.dp else 9.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "اشتراک‌گذاری اپلیکیشن",
                    tint = Color.White,
                    modifier = Modifier.size(if (isCompact) 16.dp else 18.dp)
                )
                if (!isCompact) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = text,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Interactive BottomSheet / Dialog giving user friendly options to share:
 * 1. Send the APK installation file directly (via Bluetooth, Nearby Share, Zapya, Telegram, etc.)
 * 2. Send app description and introduction text.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareAppBottomSheet(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "اشتراک‌گذاری کیبورد حماسی",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismissRequest) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "کیبورد حماسی را با دوستان و همکاران خود به اشتراک بگذارید. شما می‌توانید فایل نصبی کامل (APK) را مستقیماً از طریق بلوتوث، نرم‌افزارهای انتقال فایل یا پیام‌رسان‌ها ارسال کنید:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                textAlign = TextAlign.Right
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Option 1: Send APK directly (Bluetooth, Telegram, Nearby Share, ShareMe)
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        onDismissRequest()
                        ShareAppHelper.shareApkFile(context)
                    },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                border = CardDefaults.outlinedCardBorder(true)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ارسال فایل نصبی برنامه (APK)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "ارسال مستقیم فایل از طریق بلوتوث، Nearby Share، شریت، زاپیا، تلگرام و واتساپ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2: Share details and intro text
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        onDismissRequest()
                        ShareAppHelper.shareAppDetailsText(context)
                    },
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "اشتراک‌گذاری مشخصات و معرفی متنی",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ارسال معرفی کامل امکانات و مشخصات کیبورد حماسی به شبکه‌های اجتماعی",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
