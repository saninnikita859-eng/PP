package com.challengehub.mobile.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import com.challengehub.mobile.BuildConfig
import com.challengehub.mobile.R
import com.challengehub.mobile.core.design.Background
import com.challengehub.mobile.core.design.DarkInput
import com.challengehub.mobile.core.design.GradientButton
import com.challengehub.mobile.core.design.HotPink
import com.challengehub.mobile.core.design.Pink
import com.challengehub.mobile.core.design.Purple
import com.challengehub.mobile.core.design.Surface
import com.challengehub.mobile.core.design.TextMuted
import com.challengehub.mobile.core.network.GoogleLoginRequestDto
import com.challengehub.mobile.core.network.LoginRequestDto
import com.challengehub.mobile.core.network.NetworkModule
import com.challengehub.mobile.core.network.RegisterRequestDto
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.io.IOException
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun AuthScreen(onAuthenticated: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isRegister by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("username") }
    var fullName by remember { mutableStateOf("Full Name") }
    var email by remember { mutableStateOf("user@challengehub.dev") }
    var password by remember { mutableStateOf("12345") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun loginWithGoogle() {
        if (BuildConfig.GOOGLE_CLIENT_ID.isBlank()) {
            error = "Відсутній GOOGLE_CLIENT_ID."
            return
        }
        loading = true
        error = null
        scope.launch {
            runCatching {
                val googleOption = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_CLIENT_ID)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
                NetworkModule.api.googleLogin(GoogleLoginRequestDto(credential.idToken))
            }.onSuccess {
                onAuthenticated(it.accessToken)
            }.onFailure {
                error = googleErrorMessage(it)
            }
            loading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Pink.copy(alpha = 0.45f), Background, Color.Black),
                    radius = 900f,
                )
            )
            .padding(horizontal = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LogoMark()
            Spacer(Modifier.height(18.dp))
            Text("ChallengeHub", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "Перетвори скролінг на дію",
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.weight(1f))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (isRegister) "Створити акаунт" else "З поверненням",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (isRegister) "Приєднуйся до руху коротких челенджів"
                    else "Увійди, щоб продовжити свою подорож",
                    color = TextMuted,
                    fontSize = 13.sp,
                )
                if (isRegister) {
                    DarkInput(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = "Ім'я користувача",
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) },
                    )
                    DarkInput(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "Повне ім'я",
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) },
                    )
                }
                DarkInput(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email",
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted) },
                )
                DarkInput(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Пароль",
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
                )
                if (error != null) {
                    Text(error.orEmpty(), color = Color(0xFFFF6B6B), fontSize = 12.sp)
                }
                GradientButton(
                    text = if (isRegister) "Зареєструватися" else "Увійти",
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (isRegister) {
                            val usernameTrim = username.trim()
                            val fullNameTrim = fullName.trim()
                            val emailTrim = email.trim()
                            val passwordTrim = password.trim()
                            if (usernameTrim.length < 3) {
                                error = "Логін має містити щонайменше 3 символи."
                                return@GradientButton
                            }
                            if (fullNameTrim.length < 2) {
                                error = "Ім'я має містити щонайменше 2 символи."
                                return@GradientButton
                            }
                            if (!emailTrim.contains("@")) {
                                error = "Введіть коректний email."
                                return@GradientButton
                            }
                            if (passwordTrim.length < 6) {
                                error = "Пароль має містити щонайменше 6 символів."
                                return@GradientButton
                            }
                        }
                        loading = true
                        error = null
                        scope.launch {
                            runCatching {
                                if (isRegister) {
                                    NetworkModule.api.register(RegisterRequestDto(username.trim(), fullName.trim(), email.trim(), password.trim()))
                                } else {
                                    NetworkModule.api.login(LoginRequestDto(email.trim(), password))
                                }
                            }.onSuccess {
                                onAuthenticated(it.accessToken)
                            }.onFailure {
                                error = authErrorMessage(it, isRegister)
                            }
                            loading = false
                        }
                    },
                )
                Text(
                    "Або продовжити через",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = { if (!loading) loginWithGoogle() },
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Surface,
                        disabledContainerColor = Surface.copy(alpha = 0.6f),
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("G", color = HotPink, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text(if (loading) "Зачекайте..." else "Google")
                    }
                }
                TextButton(onClick = { isRegister = !isRegister }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (isRegister) "Вже маєте акаунт? Увійти" else "Немає акаунту? Зареєструватися",
                        color = Purple,
                    )
                }
            }
        }
    }
}

@Composable
private fun LogoMark() {
    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_icon),
            contentDescription = "ChallengeHub logo",
            modifier = Modifier
                .fillMaxSize()
                .scale(1.12f),
            contentScale = ContentScale.FillBounds,
        )
    }
}

private fun authErrorMessage(error: Throwable, isRegister: Boolean): String = when (error) {
    is HttpException -> when (error.code()) {
        401, 403 -> if (isRegister) "Недостатньо прав для цієї дії." else "Невірний email або пароль."
        409 -> if (isRegister) "Користувач з таким email або логіном вже існує." else "Конфлікт даних. Спробуйте ще раз."
        422 -> if (isRegister) "Некоректні дані реєстрації. Перевірте email, логін (мін. 3) і пароль (мін. 6)." else "Перевірте формат введених даних."
        in 500..599 -> "Сервер тимчасово недоступний. Спробуйте пізніше."
        else -> if (isRegister) "Не вдалося зареєструватися. Перевірте дані або backend URL." else "Не вдалося увійти. Перевірте дані або backend URL."
    }
    is IOException -> "Немає з'єднання з backend. Перевірте інтернет або API URL."
    else -> if (isRegister) "Помилка реєстрації: ${error.message ?: error::class.java.simpleName}" else "Помилка входу: ${error.message ?: error::class.java.simpleName}"
}

private fun googleErrorMessage(error: Throwable): String = when (error) {
    is GetCredentialCancellationException -> "Google вхід скасовано."
    is NoCredentialException -> "На пристрої немає доступного Google акаунта. Додайте акаунт Google у налаштуваннях емулятора/телефона або оновіть Google Play Services."
    is GetCredentialProviderConfigurationException -> "Google вхід недоступний: перевірте Google Play Services, залежність credentials-play-services-auth і що емулятор має образ з Play Store."
    is GetCredentialException -> "Google вхід недоступний: ${error.type}. Перевірте Web Client ID і Google Play Services."
    is HttpException -> "Google token не прийнятий backend: ${error.code()} ${error.response()?.errorBody()?.string().orEmpty()}"
    is IOException -> "Немає з'єднання з backend для Google входу."
    else -> "Не вдалося увійти через Google: ${error.message ?: error::class.java.simpleName}"
}
