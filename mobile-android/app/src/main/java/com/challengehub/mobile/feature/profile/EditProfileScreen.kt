package com.challengehub.mobile.feature.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.challengehub.mobile.core.badges.earnedBadgeItems
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.ChallengeGradient
import com.challengehub.mobile.core.design.GlassCard
import com.challengehub.mobile.core.design.GradientButton
import com.challengehub.mobile.core.design.Gold
import com.challengehub.mobile.core.design.HotPink
import com.challengehub.mobile.core.design.Surface
import com.challengehub.mobile.core.design.SurfaceSoft
import com.challengehub.mobile.core.design.TextMuted
import com.challengehub.mobile.core.network.BadgeDto
import com.challengehub.mobile.core.network.NetworkModule
import com.challengehub.mobile.core.network.PinBadgesRequestDto
import com.challengehub.mobile.core.network.UpdateProfileRequestDto
import com.challengehub.mobile.core.network.resolveMediaUrl
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private val profileBackgroundKeys = listOf("background_1", "background_2", "background_3", "background_4", "background_5", "background_6", "background_7")

@Composable
fun EditProfileScreen(
    contentPadding: PaddingValues,
    token: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var isPremium by remember { mutableStateOf(false) }
    var selectedBackground by remember { mutableStateOf<String?>(null) }

    var badges by remember { mutableStateOf<List<BadgeDto>>(emptyList()) }
    var selectedBadgeIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    var editNameDialog by remember { mutableStateOf(false) }
    var editUsernameDialog by remember { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val currentToken = token ?: return@rememberLauncherForActivityResult
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val part = avatarToMultipartPart(context, uri)
                NetworkModule.api.uploadAvatar("Bearer $currentToken", part)
            }.onSuccess {
                avatarUrl = it.avatarUrl
                error = null
            }.onFailure {
                error = "Не вдалося завантажити аватар"
            }
        }
    }

    LaunchedEffect(token) {
        val currentToken = token
        if (currentToken.isNullOrBlank()) {
            loading = false
            error = "Потрібна авторизація"
            return@LaunchedEffect
        }
        loading = true
        runCatching { NetworkModule.api.me("Bearer $currentToken") }
            .onSuccess { me ->
                fullName = me.fullName
                username = me.username
                avatarUrl = me.avatarUrl
                isPremium = me.isPremium
                selectedBackground = me.profileBackground
                error = null
            }
            .onFailure { error = "Не вдалося завантажити профіль" }

        runCatching { NetworkModule.api.myBadges("Bearer $currentToken") }
            .onSuccess {
                badges = it
                selectedBadgeIds = it.filter { b -> b.isPinned }.map { b -> b.id }.toSet()
            }
        loading = false
    }

    val previewBackgroundRes = remember(selectedBackground) {
        val key = selectedBackground ?: return@remember 0
        context.resources.getIdentifier(key, "drawable", context.packageName)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(contentPadding),
    ) {
        if (previewBackgroundRes != 0) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = previewBackgroundRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.28f,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
            }
            Text("Редагувати профіль", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
        }

        if (loading) {
            Text("Завантаження...", color = TextMuted)
            return@Column
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(124.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(ChallengeGradient)
                        .clickable { avatarPicker.launch("image/*") },
                    contentAlignment = Alignment.Center,
                ) {
                    val resolved = avatarUrl?.let { resolveMediaUrl(it) }
                    if (!resolved.isNullOrBlank()) {
                        AsyncImage(
                            model = resolved,
                            contentDescription = "Аватар",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(fullName.firstOrNull()?.uppercase() ?: "U", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = -10.dp, y = (-10).dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(HotPink)
                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            PillEdit(text = fullName.ifBlank { "Ім'я" }, onClick = { editNameDialog = true })
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            PillEdit(text = "@$username", onClick = { editUsernameDialog = true })
        }

        Text("🏆 Значки", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Оберіть до 3 значків для показу в профілі", color = TextMuted, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            earnedBadgeItems(badges).forEach { catalogItem ->
                val badgeId = badges.find { it.badgeCode == catalogItem.code }?.id ?: ""
                val isSelected = selectedBadgeIds.contains(badgeId)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF2B102E))
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) HotPink else Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                        .clickable {
                            if (badgeId.isNotEmpty()) {
                                selectedBadgeIds = toggleBadgeSelection(selectedBadgeIds, badgeId, max = 3)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(catalogItem.emoji, fontSize = 20.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(catalogItem.title, color = Color.White, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }

        if (isPremium) {
            Spacer(Modifier.height(6.dp))
            Text("Змінити фон", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            ProfileBackgroundGrid(selectedBackground = selectedBackground) { selectedBackground = it }
        }

        if (!error.isNullOrBlank()) {
            Text(error.orEmpty(), color = Color(0xFFFF8A8A), fontSize = 12.sp)
        }

        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(999.dp)).background(SurfaceSoft),
            ) { Text("Скасувати", color = Color.White, fontWeight = FontWeight.Bold) }

            GradientButton(
                text = if (saving) "Збереження..." else "Зберегти",
                enabled = !saving,
                modifier = Modifier.weight(1f),
            ) {
                val currentToken = token ?: return@GradientButton
                saving = true
                scope.launch {
                    val profileResult = runCatching {
                        NetworkModule.api.updateProfile(
                            authorization = "Bearer $currentToken",
                            body = UpdateProfileRequestDto(
                                username = username.trim(),
                                fullName = fullName.trim(),
                                profileBackground = if (isPremium) selectedBackground else null,
                            ),
                        )
                    }
                    if (profileResult.isFailure) {
                        error = if (profileResult.exceptionOrNull()?.message?.contains("409") == true) {
                            "@username вже зайнятий"
                        } else {
                            "Не вдалося зберегти профіль"
                        }
                        saving = false
                        return@launch
                    }

                    val pinResult = runCatching {
                        NetworkModule.api.pinBadges(
                            "Bearer $currentToken",
                            PinBadgesRequestDto(selectedBadgeIds.toList()),
                        )
                    }
                    if (pinResult.isFailure) {
                        error = "Профіль збережено, але не вдалося оновити значки"
                    } else {
                        error = null
                        onSaved()
                    }
                    saving = false
                }
            }
        }

        Spacer(Modifier.height(36.dp))
        }
    }

    if (editNameDialog) {
        EditTextDialog(
            title = "Змінити ім'я",
            initialValue = fullName,
            prefix = "",
            onDismiss = { editNameDialog = false },
            onConfirm = { value ->
                fullName = value
                editNameDialog = false
            },
        )
    }

    if (editUsernameDialog) {
        EditTextDialog(
            title = "Змінити username",
            initialValue = username,
            prefix = "@",
            onDismiss = { editUsernameDialog = false },
            onConfirm = { value ->
                username = value.replace("@", "").trim()
                editUsernameDialog = false
            },
        )
    }
}

@Composable
private fun PillEdit(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF3B3B3B))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Icon(Icons.Default.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun EditTextDialog(
    title: String,
    initialValue: String,
    prefix: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var field by remember(initialValue) {
        mutableStateOf(TextFieldValue(initialValue, selection = TextRange(0, initialValue.length)))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (prefix.isNotBlank()) {
                    Text(prefix, color = TextMuted)
                    Spacer(Modifier.width(4.dp))
                }
                BasicTextField(
                    value = field,
                    onValueChange = { field = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(field.text.trim()) }) { Text("OK", color = HotPink) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Скасувати") }
        },
    )
}

@Composable
private fun ProfileBackgroundGrid(selectedBackground: String?, onSelect: (String?) -> Unit) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            BackgroundTile(
                modifier = Modifier.weight(1f),
                selected = selectedBackground == null,
                onClick = { onSelect(null) },
                content = {},
            )
            profileBackgroundKeys.take(3).forEach { key ->
                BackgroundTileFromName(
                    modifier = Modifier.weight(1f),
                    key = key,
                    context = context,
                    selected = selectedBackground == key,
                    onClick = { onSelect(key) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            profileBackgroundKeys.drop(3).take(4).forEach { key ->
                BackgroundTileFromName(
                    modifier = Modifier.weight(1f),
                    key = key,
                    context = context,
                    selected = selectedBackground == key,
                    onClick = { onSelect(key) },
                )
            }
        }
    }
}

@Composable
private fun BackgroundTileFromName(
    modifier: Modifier,
    key: String,
    context: Context,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val id = remember(key) { context.resources.getIdentifier(key, "drawable", context.packageName) }
    BackgroundTile(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        content = {
            if (id != 0) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = id),
                    contentDescription = key,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        },
    )
}

@Composable
private fun BackgroundTile(
    modifier: Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .height(106.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1C1C1C))
            .border(if (selected) 2.dp else 1.dp, if (selected) HotPink else Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

private fun toggleBadgeSelection(current: Set<String>, id: String, max: Int): Set<String> {
    return if (current.contains(id)) {
        current - id
    } else {
        if (current.size >= max) current else current + id
    }
}

private fun avatarToMultipartPart(context: Context, uri: Uri): MultipartBody.Part {
    val stream = context.contentResolver.openInputStream(uri) ?: error("Cannot open image")
    val bytes = stream.use { it.readBytes() }
    val dir = File(context.cacheDir, "avatars").apply { mkdirs() }
    val file = File(dir, "avatar_${System.currentTimeMillis()}.jpg")
    file.writeBytes(bytes)
    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("file", file.name, requestBody)
}
