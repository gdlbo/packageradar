package ru.gdlbo.parcelradar.app.nav.login

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.nav.RootComponent
import ru.gdlbo.parcelradar.app.ui.components.AppLogo
import java.util.*

@Composable
fun LoginComponentImpl(loginComponent: LoginComponent) {
    val snackbarHostState = remember { SnackbarHostState() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by loginComponent.loginState.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val passwordVisible = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            loginComponent.resetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppLogo()

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(id = R.string.email)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(id = R.string.password)) },
                        visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            val image = if (passwordVisible.value) {
                                painterResource(id = R.drawable.baseline_visibility_24)
                            } else {
                                painterResource(id = R.drawable.baseline_visibility_off_24)
                            }

                            val description = if (passwordVisible.value) {
                                stringResource(id = R.string.hide_password)
                            } else {
                                stringResource(id = R.string.show_password)
                            }

                            IconButton(onClick = {
                                passwordVisible.value = !passwordVisible.value
                            }) {
                                Icon(painter = image, contentDescription = description)
                            }
                        }
                    )

                    if (state is LoginComponent.LoginState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                    }

                    Button(
                        onClick = {
                            loginComponent.login(email, password)
                            focusManager.clearFocus()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(stringResource(id = R.string.login))
                    }

                    TextButton(
                        onClick = {
                            loginComponent.navigateTo(RootComponent.TopLevelConfiguration.RegisterScreenConfiguration)
                        }
                    ) {
                        Text(stringResource(R.string.no_have_account))
                    }

                    TextButton(
                        onClick = {
                            if (email.isEmpty()) {
                                val message = context.getString(R.string.please_enter_email)
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message,
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                    }
                                }
                            } else {
                                loginComponent.remindPassword(email)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.forgot_password))
                    }
                }

                PrivacyPolicyText(context)

                NotOfficialText()
            }
        }
    )

    LaunchedEffect(loginComponent.loginState) {
        loginComponent.loginState.collect { state ->
            when (state) {
                is LoginComponent.LoginState.RemindPasswordSuccess -> {
                    val message = context.getString(R.string.password_reset_email_sent)
                    val result = withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
                    }
                    if (result == SnackbarResult.ActionPerformed) {
                        snackbarHostState.currentSnackbarData?.dismiss()
                    }
                }

                is LoginComponent.LoginState.RemindPasswordError -> {
                    val message = state.message.toString()
                    val result = withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
                    }
                    if (result == SnackbarResult.ActionPerformed) {
                        snackbarHostState.currentSnackbarData?.dismiss()
                    }
                }

                is LoginComponent.LoginState.Success -> {
                    loginComponent.navigateToHome()
                }

                is LoginComponent.LoginState.Error -> {
                    val result = snackbarHostState.showSnackbar(
                        message = state.message.toString(),
                        actionLabel = context.getString(R.string.dismiss),
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        snackbarHostState.currentSnackbarData?.dismiss()
                    }
                }

                else -> {

                }
            }
        }
    }
}

@Composable
fun NotOfficialText() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 36.dp, start = 8.dp, end = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = stringResource(id = R.string.unofficial_app),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PrivacyPolicyText(context: Context) {
    val privacyUrl = if (Locale.getDefault().language == "ru") {
        "https://gdeposylka.ru/privacy"
    } else {
        "https://packageradar.com/privacy"
    }

    val annotatedText = buildAnnotatedString {
        append(stringResource(id = R.string.agreement_text))
        append(" ")
        val privacyPolicyStart = length
        append(stringResource(id = R.string.privacy_policy_auth))
        val privacyPolicyEnd = length

        addStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            ),
            start = privacyPolicyStart,
            end = privacyPolicyEnd
        )

        addStringAnnotation(
            tag = "URL",
            annotation = privacyUrl,
            start = privacyPolicyStart,
            end = privacyPolicyEnd
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp, start = 8.dp, end = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable {
                annotatedText.getStringAnnotations(
                    tag = "URL",
                    start = 0,
                    end = annotatedText.length
                )
                    .firstOrNull()?.let { annotation ->
                        val intent = Intent(Intent.ACTION_VIEW, annotation.item.toUri())
                        context.startActivity(intent)
                    }
            },
            textAlign = TextAlign.Center
        )
    }
}