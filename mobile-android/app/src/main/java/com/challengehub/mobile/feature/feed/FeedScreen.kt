package com.challengehub.mobile.feature.feed

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.challengehub.mobile.R
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.ChallengeGradient
import com.challengehub.mobile.core.design.Gold
import com.challengehub.mobile.core.design.HotPink
import com.challengehub.mobile.core.design.Pink
import com.challengehub.mobile.core.design.Purple
import com.challengehub.mobile.core.design.Surface
import com.challengehub.mobile.core.design.SurfaceSoft
import com.challengehub.mobile.core.design.TextMuted
import com.challengehub.mobile.core.network.CommentDto
import com.challengehub.mobile.core.network.CreateCommentRequestDto
import com.challengehub.mobile.core.network.ChallengeDto
import com.challengehub.mobile.core.network.NetworkModule
import com.challengehub.mobile.core.network.ReportVideoRequestDto
import com.challengehub.mobile.core.network.VideoDto
import com.challengehub.mobile.core.network.resolveMediaUrl
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.Duration
import java.net.URLEncoder

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun FeedScreen(
    contentPadding: PaddingValues,
    token: String?,
    cachedVideos: List<VideoDto>?,
    onCacheUpdate: (List<VideoDto>) -> Unit,
    onOpenChallenge: (ChallengeDto) -> Unit,
    onOpenUser: (String) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var videos by remember(cachedVideos) { mutableStateOf(cachedVideos.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    var commentsVideo by remember { mutableStateOf<VideoDto?>(null) }
    var reportVideo by remember { mutableStateOf<VideoDto?>(null) }
    var shareVideo by remember { mutableStateOf<VideoDto?>(null) }

    LaunchedEffect(token) {
        if (cachedVideos != null) return@LaunchedEffect
        val auth = token?.let { "Bearer $it" }
        runCatching { NetworkModule.api.feed(auth) }
            .onSuccess { videos = it; onCacheUpdate(it) }
            .onFailure { error = "Не вдалося завантажити стрічку" }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(contentPadding)) {
        when {
            error != null -> Text(error.orEmpty(), modifier = Modifier.align(Alignment.Center), color = Color.White)
            videos.isEmpty() -> EmptyFeed()
            else -> {
                val pagerState = rememberPagerState(pageCount = { videos.size })
                LaunchedEffect(pagerState.settledPage, videos.map { it.id }) {
                    val video = videos.getOrNull(pagerState.settledPage) ?: return@LaunchedEffect
                    if (!video.isAd) {
                        runCatching { NetworkModule.api.viewVideo(video.id) }
                            .onSuccess {
                                videos = videos.map { if (it.id == video.id) it.copy(viewsCount = it.viewsCount + 1) else it }
                                onCacheUpdate(videos)
                            }
                    }
                }
                VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    val video = videos[page]
                    ReelCard(
                        video = video,
                        onLike = {
                            val currentToken = token ?: return@ReelCard
                            val wasLiked = video.likedByMe
                            val optimistic = video.copy(likedByMe = !wasLiked, likesCount = (video.likesCount + if (wasLiked) -1 else 1).coerceAtLeast(0))
                            videos = videos.map { if (it.id == video.id) optimistic else it }
                            onCacheUpdate(videos)
                            scope.launch {
                                runCatching {
                                    if (wasLiked) NetworkModule.api.unlikeVideo("Bearer $currentToken", video.id)
                                    else NetworkModule.api.likeVideo("Bearer $currentToken", video.id)
                                }.onSuccess { updated ->
                                    videos = videos.map { if (it.id == updated.id) updated else it }
                                    onCacheUpdate(videos)
                                }.onFailure {
                                    videos = videos.map { if (it.id == video.id) video else it }
                                    onCacheUpdate(videos)
                                }
                            }
                        },
                        onComments = { commentsVideo = video },
                        onShare = { shareVideo = video },
                        onReport = { reportVideo = video },
                        onOpenChallenge = {
                            onOpenChallenge(video.toChallengeDetails())
                        },
                        onOpenUser = { onOpenUser(video.authorUserId) },
                    )
                }
            }
        }
        commentsVideo?.let { video ->
            CommentsSheet(
                video = video,
                token = token,
                onDismiss = { commentsVideo = null },
                onOpenUser = { userId -> commentsVideo = null; onOpenUser(userId) },
                onCommentAdded = {
                    videos = videos.map { if (it.id == video.id) it.copy(commentsCount = it.commentsCount + 1) else it }
                    onCacheUpdate(videos)
                },
            )
        }
        reportVideo?.let { video -> ReportSheet(video = video, token = token, onDismiss = { reportVideo = null }) }
        shareVideo?.let { video ->
            ShareSheet(
                video = video,
                token = token,
                onDismiss = { shareVideo = null },
                onShared = {
                    videos = videos.map { if (it.id == video.id) it.copy(sharesCount = it.sharesCount + 1) else it }
                    onCacheUpdate(videos)
                },
            )
        }
    }
}

@Composable
private fun ReelCard(
    video: VideoDto,
    onLike: () -> Unit,
    onComments: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit,
    onOpenChallenge: () -> Unit,
    onOpenUser: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        VideoVisual(video)
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.08f), Color.Black.copy(alpha = 0.78f)))
            )
        )
        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp, top = 170.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ActionWithCount(if (video.likedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder, video.likesCount.toString(), if (video.likedByMe) HotPink else Color.White, onLike)
            ActionWithCountImage(R.drawable.comment, video.commentsCount.toString(), onComments)
            ActionWithCountImage(R.drawable.share, video.sharesCount.toString(), onShare)
            if (!video.isAd) {
                FeedIcon(Icons.Default.Report, Color.White, onReport)
            }
        }
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 18.dp, end = 84.dp, bottom = 76.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Purple, Pink))), contentAlignment = Alignment.Center) {
                    Text((video.authorDisplayName ?: video.authorUsername ?: "CH").take(1).uppercase(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
                Column(modifier = Modifier.clickable(onClick = onOpenUser)) {
                    Text(video.authorDisplayName ?: video.authorUsername ?: "Користувач", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("@${video.authorUsername ?: video.authorUserId}", color = TextMuted, fontSize = 12.sp)
                }
            }
            if (!video.description.isNullOrBlank()) {
                Text(video.description, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(
                video.challengeTitle ?: if (video.isAd) "Рекламне відео" else "Челендж",
                color = HotPink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(enabled = !video.isAd, onClick = onOpenChallenge),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Visibility, contentDescription = null, tint = TextMuted, modifier = Modifier.size(17.dp))
                Text("${video.viewsCount} переглядів", color = TextMuted, fontSize = 12.sp)
                Text("+${video.challengeXpReward ?: 0} XP", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ActionWithCount(icon: androidx.compose.ui.graphics.vector.ImageVector, count: String, tint: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        FeedIcon(icon, tint, onClick)
        Text(count, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FeedIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(30.dp))
    }
}

@Composable
private fun FeedIconImage(drawableRes: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
        )
    }
}

@Composable
private fun ActionWithCountImage(drawableRes: Int, count: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
        )
        Text(count, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareSheet(video: VideoDto, token: String?, onDismiss: () -> Unit, onShared: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    fun markShared() {
        val currentToken = token
        if (currentToken != null) scope.launch { runCatching { NetworkModule.api.shareVideo("Bearer $currentToken", video.id) } }
        onShared()
    }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Surface) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Поділитися", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Закрити", tint = Color.White) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                ShareLogo(ShareTarget.Telegram, "Telegram") { context.shareToTelegram(video); markShared(); onDismiss() }
                ShareLogo(ShareTarget.X, "X") { context.shareToX(video); markShared(); onDismiss() }
                ShareLogo(ShareTarget.Reddit, "Reddit") { context.shareToReddit(video); markShared(); onDismiss() }
                ShareLogo(ShareTarget.More, "Ще") { context.shareDefault(video); markShared(); onDismiss() }
            }
            ShareRow("Копіювати посилання", Icons.Default.Link) {
                context.copyLink(resolveMediaUrl(video.url)); markShared(); onDismiss()
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

private enum class ShareTarget { Telegram, X, Reddit, More }

@Composable
private fun ShareLogo(target: ShareTarget, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            Modifier
                .size(58.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            when (target) {
                ShareTarget.Telegram -> Image(painter = painterResource(id = R.drawable.telegram_logo), contentDescription = null, modifier = Modifier.size(58.dp))
                ShareTarget.X -> Image(painter = painterResource(id = R.drawable.x_logo), contentDescription = null, modifier = Modifier.size(58.dp))
                ShareTarget.Reddit -> Image(painter = painterResource(id = R.drawable.reddit_logo), contentDescription = null, modifier = Modifier.size(58.dp))
                ShareTarget.More -> Image(painter = painterResource(id = R.drawable.share_logo), contentDescription = null, modifier = Modifier.size(58.dp))
            }
        }
        Text(label, fontSize = 12.sp, color = Color.White)
    }
}

@Composable
private fun ShareRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.IosShare, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceSoft).clickable(onClick = onClick).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = HotPink)
        Text(title, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportSheet(video: VideoDto, token: String?, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var reason by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    fun submitReport() {
        val currentToken = token ?: return
        if (reason.length < 3) {
            message = "Опишіть причину детальніше"
            return
        }
        scope.launch {
            runCatching { NetworkModule.api.reportVideo("Bearer $currentToken", video.id, ReportVideoRequestDto(reason)) }
                .onSuccess { onDismiss() }
                .onFailure { message = "Не вдалося надіслати скаргу" }
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Surface) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Поскаржитися на відео", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = reason, onValueChange = { reason = it }, placeholder = { Text("Причина скарги") }, modifier = Modifier.fillMaxWidth())
            if (message != null) Text(message.orEmpty(), color = TextMuted)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(ChallengeGradient)
                    .clickable { submitReport() }
                    .padding(horizontal = 18.dp, vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) { Text("Надіслати", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun VideoVisual(video: VideoDto) {
    if (!video.url.contains("cdn.challengehub.local")) {
        ReelVideoPlayer(url = resolveMediaUrl(video.url))
        return
    }
    Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF062C38), Color(0xFF0B7A75), Color(0xFF020403)))))
}

@Composable
private fun ReelVideoPlayer(url: String) {
    val context = LocalContext.current
    var paused by remember(url) { mutableStateOf(false) }
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            playWhenReady = true
            prepare()
        }
    }
    LaunchedEffect(paused) { player.playWhenReady = !paused }
    DisposableEffect(player) { onDispose { player.release() } }
    Box(Modifier.fillMaxSize().clickable { paused = !paused }) {
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { PlayerView(it).apply { useController = false; this.player = player } })
        if (paused) {
            Box(Modifier.align(Alignment.Center).size(74.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsSheet(video: VideoDto, token: String?, onDismiss: () -> Unit, onOpenUser: (String) -> Unit, onCommentAdded: () -> Unit) {
    val scope = rememberCoroutineScope()
    var comments by remember { mutableStateOf<List<CommentDto>>(emptyList()) }
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var sortByLikes by remember { mutableStateOf(false) }
    var sortMenuOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    fun addComment() {
        val currentToken = token ?: return
        if (text.isBlank()) return
        scope.launch {
            runCatching { NetworkModule.api.addComment("Bearer $currentToken", video.id, CreateCommentRequestDto(text.trim())) }
                .onSuccess { comments = comments + it; text = ""; onCommentAdded() }
                .onFailure { error = "Не вдалося додати коментар" }
        }
    }

    LaunchedEffect(video.id) {
        runCatching { NetworkModule.api.comments(video.id, token?.let { "Bearer $it" }) }
            .onSuccess { comments = it }
            .onFailure { error = "Не вдалося завантажити коментарі" }
    }

    val sortedComments = if (sortByLikes) {
        comments.sortedByDescending { it.likesCount }
    } else {
        comments.sortedByDescending { runCatching { Instant.parse(it.createdAt) }.getOrElse { Instant.EPOCH } }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Surface, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.65f).padding(horizontal = 18.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Коментарі", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(onClick = { sortMenuOpen = true }) { SortMenuIcon() }
                        DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }, containerColor = SurfaceSoft) {
                            DropdownMenuItem(text = { Text("Нові", color = Color.White) }, onClick = { sortByLikes = false; sortMenuOpen = false })
                            DropdownMenuItem(text = { Text("Популярні", color = Color.White) }, onClick = { sortByLikes = true; sortMenuOpen = false })
                        }
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Закрити", tint = Color.White) }
                }
            }
            if (error != null) Text(error.orEmpty(), color = Color(0xFFFF8A8A))
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sortedComments, key = { it.id }) { comment ->
                    CommentRow(
                        comment = comment,
                        onOpenUser = { onOpenUser(comment.userId) },
                        onLike = {
                            val currentToken = token ?: return@CommentRow
                            val wasLiked = comment.likedByMe
                            val optimistic = comment.copy(likedByMe = !wasLiked, likesCount = (comment.likesCount + if (wasLiked) -1 else 1).coerceAtLeast(0))
                            comments = comments.map { if (it.id == comment.id) optimistic else it }
                            scope.launch {
                                runCatching {
                                    if (wasLiked) NetworkModule.api.unlikeComment("Bearer $currentToken", comment.id)
                                    else NetworkModule.api.likeComment("Bearer $currentToken", comment.id)
                                }.onSuccess { updated ->
                                    comments = comments.map { if (it.id == updated.id) updated else it }
                                }.onFailure {
                                    comments = comments.map { if (it.id == comment.id) comment else it }
                                }
                            }
                        },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Коментувати...") },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = SurfaceSoft, unfocusedContainerColor = SurfaceSoft, focusedBorderColor = HotPink, unfocusedBorderColor = Color.Transparent),
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .height(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(ChallengeGradient)
                        .clickable { addComment() }
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center,
                ) { Text("OK", color = Color.White, fontWeight = FontWeight.Black) }
            }
        }
    }
}

@Composable
private fun SortMenuIcon() {
    Canvas(modifier = Modifier.size(28.dp)) {
        val stroke = 5.6f
        val left = 2f
        val full = size.width - 2f
        val firstY = 5f
        val middleY = size.height / 2f
        val lastY = size.height - 5f
        drawLine(Color.White, androidx.compose.ui.geometry.Offset(left, firstY), androidx.compose.ui.geometry.Offset(full, firstY), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(Color.White, androidx.compose.ui.geometry.Offset(left, middleY), androidx.compose.ui.geometry.Offset(size.width * 0.68f, middleY), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(Color.White, androidx.compose.ui.geometry.Offset(left, lastY), androidx.compose.ui.geometry.Offset(size.width * 0.38f, lastY), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun CommentRow(comment: CommentDto, onOpenUser: () -> Unit, onLike: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.06f)).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Purple, Pink))), contentAlignment = Alignment.Center) {
            Text((comment.displayName ?: comment.username).take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Black)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(comment.displayName ?: comment.username, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onOpenUser))
                Text(timeAgo(comment.createdAt), color = TextMuted, fontSize = 11.sp)
            }
            Text(comment.text, color = TextMuted)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
            IconButton(onClick = onLike, modifier = Modifier.size(34.dp)) {
                Icon(if (comment.likedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Лайк", tint = if (comment.likedByMe) HotPink else Color.White, modifier = Modifier.size(20.dp))
            }
            Text(comment.likesCount.toString(), color = TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun EmptyFeed() {
    Column(modifier = Modifier.fillMaxSize().background(Background).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(96.dp).clip(RoundedCornerShape(28.dp)).background(SurfaceSoft), contentAlignment = Alignment.Center) {
            Text("▶", color = HotPink, fontSize = 34.sp)
        }
        Spacer(Modifier.height(18.dp))
        Text("Стрічка поки порожня", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text("Станьте першим, хто опублікує відео!", color = TextMuted)
    }
}

private fun Context.shareDefault(video: VideoDto) {
    val payload = sharePayload(video)
    startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, payload)
    }, "Поділитися відео"))
}

private fun Context.shareToTelegram(video: VideoDto) {
    val payload = sharePayload(video)
    val url = resolveMediaUrl(video.url)
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("tg://msg_url?url=${url.urlEncode()}&text=${payload.urlEncode()}")))
    }.onFailure {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/share/url?url=${url.urlEncode()}&text=${payload.urlEncode()}")))
        }.onFailure {
            shareDefault(video)
        }
    }
}

private fun Context.shareToX(video: VideoDto) {
    val payload = sharePayload(video)
    val url = resolveMediaUrl(video.url)
    runCatching {
        startActivity(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.twitter.android")
            putExtra(Intent.EXTRA_TEXT, payload)
        })
    }.onFailure {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?text=${payload.urlEncode()}&url=${url.urlEncode()}")))
        }.onFailure {
            shareDefault(video)
        }
    }
}

private fun Context.shareToReddit(video: VideoDto) {
    val payload = sharePayload(video)
    val url = resolveMediaUrl(video.url)
    runCatching {
        startActivity(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.reddit.frontpage")
            putExtra(Intent.EXTRA_TEXT, payload)
        })
    }.onFailure {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.reddit.com/submit?url=${url.urlEncode()}&title=${payload.urlEncode()}")))
        }.onFailure {
            shareDefault(video)
        }
    }
}

private fun exportTags(challengeTitle: String?): String {
    val normalized = challengeTitle.orEmpty()
        .split(Regex("[^A-Za-z0-9]+"))
        .filter { it.isNotBlank() }
        .joinToString("") { it.replaceFirstChar { ch -> ch.uppercase() } }
        .take(40)
    val challengeTag = if (normalized.isBlank()) "" else " #$normalized"
    return "#ChallengeHub$challengeTag"
}

private fun sharePayload(video: VideoDto): String = "ChallengeHub ${exportTags(video.challengeTitle)}: ${resolveMediaUrl(video.url)}"

private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

private fun Context.copyLink(link: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("ChallengeHub video", link))
}

private fun timeAgo(value: String): String {
    val instant = runCatching { Instant.parse(value) }.getOrElse { return "" }
    val duration = Duration.between(instant, Instant.now())
    val minutes = duration.toMinutes()
    val hours = duration.toHours()
    val days = duration.toDays()
    val years = days / 365
    return when {
        minutes < 1 -> "щойно"
        minutes < 60 -> "$minutes хв тому"
        hours < 24 -> "$hours год тому"
        days < 365 -> "$days дн тому"
        else -> "$years р тому"
    }
}

private fun VideoDto.toChallengeDetails(): ChallengeDto = ChallengeDto(
    id = challengeId,
    title = challengeTitle ?: "Челендж",
    description = "Відео-звіт до челенджу ChallengeHub.",
    difficulty = "normal",
    durationDays = 0,
    xpReward = challengeXpReward ?: 0,
    challengeType = challengeType,
    status = "PUBLISHED",
    createdAt = createdAt,
    creatorName = null,
    participantsCount = 0,
    participationStatus = null,
    startedAt = null,
    completedAt = null,
)
