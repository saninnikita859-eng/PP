package com.challengehub.mobile.feature.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.ChallengeGradient
import com.challengehub.mobile.core.design.GradientButton
import com.challengehub.mobile.core.design.HotPink
import com.challengehub.mobile.core.design.SurfaceSoft
import com.challengehub.mobile.core.design.TextMuted
import java.io.File
import kotlinx.coroutines.delay

private const val MAX_RECORDING_SECONDS = 60

@Composable
fun CameraScreen(
    contentPadding: PaddingValues,
    onOpenUpload: () -> Unit,
    onRecorded: (Uri) -> Unit = {},
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermissions by remember { mutableStateOf(context.hasCameraPermissions()) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var recording by remember { mutableStateOf<Recording?>(null) }
    var recordingStatus by remember { mutableStateOf("Готово до запису") }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var useFrontCamera by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var pendingStart by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        hasPermissions = result[Manifest.permission.CAMERA] == true && result[Manifest.permission.RECORD_AUDIO] == true
        recordingStatus = if (hasPermissions) "Готово до запису" else "Надайте доступ до камери та мікрофона"
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }

    LaunchedEffect(hasPermissions, previewView, useFrontCamera) {
        val view = previewView ?: return@LaunchedEffect
        if (hasPermissions) {
            bindCamera(context, view, lifecycleOwner, useFrontCamera) { capture -> videoCapture = capture }
        }
    }

    LaunchedEffect(pendingStart, countdown) {
        if (pendingStart && countdown > 0) {
            recordingStatus = "Старт через $countdown..."
            delay(1000)
            countdown -= 1
        } else if (pendingStart && countdown == 0) {
            val capture = videoCapture ?: return@LaunchedEffect
            pendingStart = false
            elapsedSeconds = 0
            recording = startRecording(
                context = context,
                videoCapture = capture,
                onStatus = { recordingStatus = it },
                onSaved = { uri ->
                    recording = null
                    recordingStatus = "Відео записано"
                    onRecorded(uri)
                },
            )
        }
    }

    LaunchedEffect(recording) {
        while (recording != null) {
            delay(1000)
            elapsedSeconds += 1
            recordingStatus = "Запис ${elapsedSeconds}s / ${MAX_RECORDING_SECONDS}s"
            if (elapsedSeconds >= MAX_RECORDING_SECONDS) {
                recording?.stop()
                recording = null
                recordingStatus = "Ліміт 60 секунд. Збереження відео..."
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            recording?.stop()
            recording = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(contentPadding)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White) }
            Column(Modifier.weight(1f)) {
                Text("Запис відео", fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(recordingStatus, color = TextMuted, fontSize = 13.sp)
            }
            IconButton(
                enabled = recording == null && !pendingStart,
                onClick = { useFrontCamera = !useFrontCamera },
            ) {
                Icon(Icons.Default.Cameraswitch, contentDescription = "Змінити камеру", tint = Color.White)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(28.dp))
                .background(SurfaceSoft),
            contentAlignment = Alignment.Center,
        ) {
            if (hasPermissions) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            previewView = this
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                if (recording != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 14.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.Black.copy(alpha = 0.58f))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) { Text("REC ${elapsedSeconds}s", color = HotPink, fontWeight = FontWeight.Black) }
                }
                if (countdown > 0) {
                    Text(countdown.toString(), color = Color.White, fontSize = 76.sp, fontWeight = FontWeight.Black)
                }
            } else {
                Text("Потрібен доступ до камери та мікрофона", color = TextMuted)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(if (recording == null) ChallengeGradient else androidx.compose.ui.graphics.Brush.linearGradient(listOf(HotPink, HotPink))),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    modifier = Modifier.size(76.dp),
                    onClick = {
                        if (!hasPermissions) {
                            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                            return@IconButton
                        }
                        val currentRecording = recording
                        if (currentRecording != null) {
                            currentRecording.stop()
                            recording = null
                            recordingStatus = "Збереження відео..."
                        } else if (!pendingStart) {
                            countdown = 3
                            pendingStart = true
                        }
                    },
                ) {
                    Icon(
                        if (recording == null) Icons.Default.FiberManualRecord else Icons.Default.Stop,
                        contentDescription = if (recording == null) "Почати запис" else "Зупинити запис",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
        }

        GradientButton("Обрати готове відео", Modifier.fillMaxWidth().height(54.dp), onClick = onOpenUpload)
    }
}

private fun Context.hasCameraPermissions(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

private fun bindCamera(
    context: Context,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    useFrontCamera: Boolean,
    onReady: (VideoCapture<Recorder>) -> Unit,
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
        val videoCapture = VideoCapture.withOutput(recorder)
        val selector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        runCatching {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, videoCapture)
            onReady(videoCapture)
        }
    }, ContextCompat.getMainExecutor(context))
}

@SuppressLint("MissingPermission")
private fun startRecording(
    context: Context,
    videoCapture: VideoCapture<Recorder>,
    onStatus: (String) -> Unit,
    onSaved: (Uri) -> Unit,
): Recording {
    val outputDir = File(context.cacheDir, "recorded-videos").apply { mkdirs() }
    val file = File(outputDir, "challenge-${System.currentTimeMillis()}.mp4")
    val outputOptions = FileOutputOptions.Builder(file).build()
    onStatus("Запис триває...")
    return videoCapture.output
        .prepareRecording(context, outputOptions)
        .withAudioEnabled()
        .start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Finalize -> {
                    if (!event.hasError()) onSaved(Uri.fromFile(file)) else onStatus("Не вдалося зберегти відео")
                }
            }
        }
}
