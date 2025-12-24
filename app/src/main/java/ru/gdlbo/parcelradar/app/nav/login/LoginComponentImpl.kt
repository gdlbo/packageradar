package ru.gdlbo.parcelradar.app.nav.login

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
    var passwordVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    DisposableEffect(Unit) {
        onDispose {
            loginComponent.resetState()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        actionColor = MaterialTheme.colorScheme.inversePrimary,
                        shape = MaterialTheme.shapes.medium
                    )
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrivacyPolicyText(context)
                NotOfficialText()
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppLogo()

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(id = R.string.login),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(id = R.string.email)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(id = R.string.password)) },
                    placeholder = { Text("••••••••") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            loginComponent.login(email, password)
                            focusManager.clearFocus()
                        }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Filled.Visibility
                                } else {
                                    Icons.Filled.VisibilityOff
                                },
                                contentDescription = stringResource(
                                    id = if (passwordVisible) {
                                        R.string.hide_password
                                    } else {
                                        R.string.show_password
                                    }
                                ),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            if (email.isEmpty()) {
                                val message = context.getString(R.string.please_enter_email)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                loginComponent.remindPassword(email)
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.forgot_password),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        loginComponent.login(email, password)
                        focusManager.clearFocus()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    enabled = state !is LoginComponent.LoginState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    AnimatedVisibility(
                        visible = state is LoginComponent.LoginState.Loading,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    }

                    AnimatedVisibility(
                        visible = state !is LoginComponent.LoginState.Loading,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Text(
                            text = stringResource(id = R.string.login),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        text = stringResource(id = R.string.or),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                OutlinedButton(
                    onClick = {
                        loginComponent.navigateTo(
                            RootComponent.TopLevelConfiguration.RegisterScreenConfiguration
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        width = 1.5.dp
                    )
                ) {
                    Text(
                        text = stringResource(R.string.no_have_account),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    LaunchedEffect(loginComponent.loginState) {
        loginComponent.loginState.collect { state ->
            when (state) {
                is LoginComponent.LoginState.RemindPasswordSuccess -> {
                    val message = context.getString(R.string.password_reset_email_sent)
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                is LoginComponent.LoginState.RemindPasswordError -> {
                    val message = state.message.toString()
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Long
                        )
                    }
                }

                is LoginComponent.LoginState.Success -> {
                    loginComponent.navigateToHome()
                }

                is LoginComponent.LoginState.Error -> {
                    snackbarHostState.showSnackbar(
                        message = state.message.toString(),
                        actionLabel = context.getString(R.string.dismiss),
                        duration = SnackbarDuration.Long
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
fun NotOfficialText() {
    Text(
        text = stringResource(id = R.string.unofficial_app),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
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
                fontWeight = FontWeight.Medium,
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

    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.clickable {
            annotatedText.getStringAnnotations(
                tag = "URL",
                start = 0,
                end = annotatedText.length
            ).firstOrNull()?.let { annotation ->
                val intent = Intent(Intent.ACTION_VIEW, annotation.item.toUri())
                context.startActivity(intent)
            }
        },
        textAlign = TextAlign.Center
    )
}
