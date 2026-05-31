package com.challengehub.mobile.feature.upload

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.challengehub.mobile.R
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.DarkInput
import com.challengehub.mobile.core.design.GlassCard
import com.challengehub.mobile.core.design.ChallengeGradient
import com.challengehub.mobile.core.design.GradientButton
import com.challengehub.mobile.core.design.Purple
import com.challengehub.mobile.core.design.SurfaceSoft
import com.challengehub.mobile.core.design.TextMuted
import com.challengehub.mobile.core.network.ChallengeDto
import com.challengehub.mobile.core.network.NetworkModule
import com.challengehub.mobile.core.network.resolveMediaUrl
import com.challengehub.mobile.core.network.VideoDto
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

@Composable
fun UploadScreen(contentPadding: PaddingValues, token: String?, initialVideoUri: String? = null, initialChallenge: ChallengeDto? = null, onRecordVideo: () -> Unit = {}, onUploaded: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var challenges by remember { mutableStateOf<List<ChallengeDto>>(emptyList()) }
    var selected by remember { mutableStateOf<ChallengeDto?>(null) }
    var challengeMenuOpen by remember { mutableStateOf(false) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var description by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var exportingYoutube by remember { mutableStateOf(false) }
    var youtubeExported by remember { mutableStateOf(false) }
    var youtubeConnected by remember { mutableStateOf(false) }
    var exportingTiktok by remember { mutableStateOf(false) }
    var tiktokExported by remember { mutableStateOf(false) }
    var tiktokConnected by remember { mutableStateOf(false) }
    var publishedVideo by remember { mutableStateOf<VideoDto?>(null) }
    var exportFeedback by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        videoUri = uri
    }

    LaunchedEffect(initialVideoUri) {
        if (!initialVideoUri.isNullOrBlank()) {
            videoUri = Uri.parse(initialVideoUri)
        }
    }

    LaunchedEffect(initialChallenge) {
        if (initialChallenge != null) selected = initialChallenge
    }

    LaunchedEffect(Unit) {
        runCatching { NetworkModule.api.challenges() }
            .onSuccess {
                challenges = it
            }
            .onFailure { message = "Не вдалося завантажити челенджі" }
    }

    LaunchedEffect(token) {
        val currentToken = token ?: return@LaunchedEffect
        runCatching { NetworkModule.api.youtubeStatus("Bearer $currentToken") }
            .onSuccess { youtubeConnected = it.connected }
        runCatching { NetworkModule.api.tiktokStatus("Bearer $currentToken") }
            .onSuccess { tiktokConnected = it.connected }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Створити", fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text("Завантаж відео-звіт до активного челенджу", color = TextMuted)

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Відео", fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(SurfaceSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    val uri = videoUri
                    if (uri != null) {
                        LocalVideoPreview(uri)
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, tint = Purple)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Оберіть відео з галереї або запишіть з камери",
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
                GradientButton(text = "Завантажити з галереї", modifier = Modifier.fillMaxWidth()) {
                    picker.launch("video/*")
                }
                GradientButton(text = "Записати з камери", modifier = Modifier.fillMaxWidth(), onClick = onRecordVideo)
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Челендж", fontWeight = FontWeight.Bold)
                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(SurfaceSoft).clickable { challengeMenuOpen = true }.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(selected?.title ?: "Оберіть челендж", fontWeight = FontWeight.Bold)
                            Text(selected?.let { "${it.xpReward} XP • ${it.durationDays} дн." } ?: "Без вибору публікація недоступна", color = TextMuted, fontSize = 12.sp)
                        }
                        Text("▾", color = Purple, fontSize = 22.sp)
                    }
                    DropdownMenu(
                        expanded = challengeMenuOpen,
                        onDismissRequest = { challengeMenuOpen = false },
                        containerColor = SurfaceSoft,
                        modifier = Modifier.heightIn(max = 320.dp),
                    ) {
                        challenges.forEach { challenge ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(challenge.title, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("${challenge.xpReward} XP • ${challenge.durationDays} дн.", color = TextMuted, fontSize = 12.sp)
                                    }
                                },
                                onClick = { selected = challenge; challengeMenuOpen = false },
                            )
                        }
                    }
                }
            }
        }

        DarkInput(value = description, onValueChange = { description = it }, placeholder = "Опис відео")
        if (loading) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Purple)
                Text("Завантаження відео на сервер...", color = TextMuted, fontSize = 12.sp)
            }
        }
        GradientButton(
            text = if (loading) "Публікація..." else "Опублікувати",
            enabled = !loading && selected != null && videoUri != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val currentToken = token
            val challenge = selected
            val uri = videoUri
            if (currentToken == null || challenge == null || uri == null) {
                message = "Оберіть челендж і відео"
                return@GradientButton
            }
            loading = true
            scope.launch {
                runCatching {
                    val filePart = uri.toMultipartPart(context)
                    NetworkModule.api.uploadVideo(
                        authorization = "Bearer $currentToken",
                        challengeId = challenge.id.toRequestBody("text/plain".toMediaTypeOrNull()),
                        challengeType = challenge.challengeType.toRequestBody("text/plain".toMediaTypeOrNull()),
                        description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                        file = filePart,
                    )
                }.onSuccess {
                    publishedVideo = it
                    youtubeExported = false
                    tiktokExported = false
                    exportFeedback = null
                }.onFailure {
                    message = "Не вдалося опублікувати відео: ${it.message}"
                }
                loading = false
            }
        }
    }
    message?.let { msg ->
        AlertDialog(
            onDismissRequest = { message = null },
            title = { Text("Повідомлення", fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { message = null }) { Text("Ок") } },
        )
    }
    publishedVideo?.let { video ->
        AlertDialog(
            onDismissRequest = { publishedVideo = null; exportFeedback = null; onUploaded() },
            title = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Відео опубліковано!", fontWeight = FontWeight.Black)
                    IconButton(onClick = { publishedVideo = null; exportFeedback = null; onUploaded() }) { Icon(Icons.Default.Close, contentDescription = "Закрити") }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Експортувати відео до інших платформ?")
                    ExportPlatformRow(
                        target = ExportTarget.TikTok,
                        title = when {
                            tiktokExported -> "TikTok експортовано"
                            tiktokConnected -> "TikTok чернетка"
                            else -> "Підключіть TikTok"
                        },
                        enabled = !tiktokExported && !exportingTiktok,
                    ) {
                        val currentToken = token
                        if (currentToken == null) return@ExportPlatformRow
                        if (tiktokExported) return@ExportPlatformRow
                        if (!tiktokConnected) {
                            scope.launch {
                                runCatching { NetworkModule.api.tiktokConnectUrl("Bearer $currentToken") }
                                    .onSuccess { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.connectUrl))) }
                                    .onFailure { exportFeedback = "Не вдалося відкрити TikTok OAuth" }
                            }
                            return@ExportPlatformRow
                        }
                        exportingTiktok = true
                        scope.launch {
                            runCatching { NetworkModule.api.exportTiktok("Bearer $currentToken", video.id) }
                                .onSuccess {
                                    exportFeedback = it.message
                                    tiktokExported = true
                                }
                                .onFailure { exportFeedback = exportErrorMessage("TikTok", it) }
                            exportingTiktok = false
                        }
                    }
                    ExportPlatformRow(
                        target = ExportTarget.YouTube,
                        title = when {
                            youtubeExported -> "YouTube Shorts експортовано"
                            youtubeConnected -> "YouTube Shorts"
                            else -> "Підключіть YouTube"
                        },
                        enabled = !youtubeExported && !exportingYoutube,
                    ) {
                        val currentToken = token
                        if (currentToken == null) return@ExportPlatformRow
                        if (youtubeExported) return@ExportPlatformRow
                        if (!youtubeConnected) {
                            scope.launch {
                                runCatching { NetworkModule.api.youtubeConnectUrl("Bearer $currentToken") }
                                    .onSuccess { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.connectUrl))) }
                                    .onFailure { exportFeedback = "Не вдалося відкрити YouTube OAuth" }
                            }
                            return@ExportPlatformRow
                        }
                        exportingYoutube = true
                        scope.launch {
                            runCatching { NetworkModule.api.exportYoutube("Bearer $currentToken", video.id) }
                                .onSuccess {
                                    exportFeedback = it.message
                                    youtubeExported = true
                                }
                                .onFailure { exportFeedback = exportErrorMessage("YouTube Shorts", it) }
                            exportingYoutube = false
                        }
                    }
                    exportFeedback?.let { feedback ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceSoft)
                                .verticalScroll(rememberScrollState())
                                .padding(10.dp),
                        ) {
                            Text(
                                feedback,
                                color = if (feedback.startsWith("Не вдалося")) Color(0xFFFF8A8A) else Color(0xFF8AF5A2),
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                            )
                        }
                    }
                    if (exportingYoutube) {
                        Text("Експорт у YouTube Shorts...", color = TextMuted, fontSize = 12.sp)
                    }
                    if (exportingTiktok) {
                        Text("Експорт у TikTok...", color = TextMuted, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { publishedVideo = null; exportFeedback = null; onUploaded() }) { Text("Пропустити", color = TextMuted) }
            },
        )
    }
}

@Composable
private fun ExportPlatformRow(target: ExportTarget, title: String, enabled: Boolean = true, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceSoft.copy(alpha = if (enabled) 1f else 0.58f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BrandPlatformBadge(target = target, enabled = enabled)
        Text(title, fontWeight = FontWeight.Bold, color = if (enabled) Color.White else TextMuted)
    }
}

private enum class ExportTarget { TikTok, YouTube }

@Composable
private fun BrandPlatformBadge(target: ExportTarget, enabled: Boolean) {
    val alpha = if (enabled) 1f else 0.55f
    Box(
        modifier = Modifier.height(38.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF121212)).padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (target) {
            ExportTarget.TikTok -> Image(painter = painterResource(id = R.drawable.tiktok_logo), contentDescription = null, modifier = Modifier.height(20.dp), alpha = alpha)
            ExportTarget.YouTube -> Image(painter = painterResource(id = R.drawable.youtube_logo), contentDescription = null, modifier = Modifier.height(20.dp), alpha = alpha)
        }
    }
}

@Composable
private fun LocalVideoPreview(uri: Uri) {
    val context = LocalContext.current
    val player = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = false
        }
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = true
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { it.player = player },
    )
}

private fun Uri.toMultipartPart(context: Context): MultipartBody.Part {
    val bytes = context.contentResolver.openInputStream(this)?.use { it.readBytes() } ?: ByteArray(0)
    val body = bytes.toRequestBody("video/mp4".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("file", "challenge-video.mp4", body)
}

private fun exportErrorMessage(platform: String, error: Throwable): String = when (error) {
    is HttpException -> {
        val detail = error.response()?.errorBody()?.string().orEmpty()
        "Не вдалося експортувати в $platform (${error.code()}): ${detail.ifBlank { error.message() }}"
    }
    is IOException -> "Не вдалося експортувати в $platform: немає з'єднання з backend."
    else -> "Не вдалося експортувати в $platform: ${error.message ?: error::class.java.simpleName}"
}

private fun Context.shareExport(video: VideoDto, platform: String) {
    val text = "${video.description.orEmpty()}\n\nОпубліковано з ChallengeHub\n${resolveMediaUrl(video.url)}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_TITLE, "Експорт до $platform")
    }
    startActivity(Intent.createChooser(intent, "Експорт до $platform"))
}

