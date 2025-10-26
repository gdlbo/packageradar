package ru.gdlbo.parcelradar.app.nav.register

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.nav.RootComponent

@Composable
fun RegisterComponentImpl(registerComponent: RegisterComponent) {
    val snackbarHostState = remember { SnackbarHostState() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val state by registerComponent.registerState.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val passwordVisible = remember { mutableStateOf(false) }
    val passwordVisibleConfirm = remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.account_reg),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

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

                        IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                            Icon(painter = image, description)
                        }
                    }
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(id = R.string.confirm_password)) },
                    visualTransformation = if (passwordVisibleConfirm.value) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        val image = if (passwordVisibleConfirm.value) {
                            painterResource(id = R.drawable.baseline_visibility_24)
                        } else {
                            painterResource(id = R.drawable.baseline_visibility_off_24)
                        }

                        val description = if (passwordVisibleConfirm.value) {
                            stringResource(id = R.string.hide_password)
                        } else {
                            stringResource(id = R.string.show_password)
                        }

                        IconButton(onClick = {
                            passwordVisibleConfirm.value = !passwordVisibleConfirm.value
                        }) {
                            Icon(painter = image, description)
                        }
                    }
                )

                if (state is RegisterComponent.RegisterState.Loading) {
                    CircularProgressIndicator()
                }

                Button(
                    onClick = {
                        if (password == confirmPassword) {
                            registerComponent.register(email, password)
                            focusManager.clearFocus()
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.password_not_match),
                                    actionLabel = context.getString(R.string.dismiss),
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(stringResource(id = R.string.register))
                }

                TextButton(
                    onClick = {
                        registerComponent.popBack()
                    }
                ) {
                    Text(stringResource(R.string.already_have_account))
                }
            }
        }
    )

    LaunchedEffect(registerComponent.registerState) {
        registerComponent.registerState.collect { state ->
            when (state) {
                is RegisterComponent.RegisterState.Success -> {
                    registerComponent.navigateTo(RootComponent.TopLevelConfiguration.HomeScreenConfiguration)
                }

                is RegisterComponent.RegisterState.Error -> {
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