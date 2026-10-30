package com.example.proyectocarabinero.ui

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.proyectocarabinero.viewmodel.MainViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.proyectocarabinero.R

// Validaciones
fun validateName(s: String): String? = when {
    s.isBlank() -> "Ingresa tu nombre"
    s.trim().length < 3 -> "Mínimo 3 caracteres"
    else -> null
}

fun validateEmail(s: String): String? = when {
    s.isBlank() -> "Ingresa tu correo"
    !Patterns.EMAIL_ADDRESS.matcher(s.trim()).matches() -> "Correo inválido"
    else -> null
}

fun validatePassword(s: String): String? = when {
    s.length < 6 -> "Mínimo 6 caracteres"
    s.none { it.isLetter() } -> "Debe incluir al menos una letra"
    s.none { it.isDigit() } -> "Debe incluir al menos un número"
    else -> null
}

// Componentes
@Composable
fun AppTextField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String?,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        trailingIcon = if (password) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Mostrar u ocultar contraseña"
                    )
                }
            }
        } else null,
        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        isError = error != null,
        supportingText = { Text(error ?: " ") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AuthHeader(title: String, subtitle: String) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    // Animación de entrada del escudo y el título
    AnimatedVisibility(visible, enter = fadeIn() + slideInVertically { -it / 2 }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.escudo),
                contentDescription = "Escudo",
                modifier = Modifier.height(120.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Text(
                subtitle, style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------- Pantalla de bienvenida ----------
@Composable
fun WelcomeScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AuthHeader("Bienvenido", "Proyecto carabinero: estudio, repaso y autoevaluación, incluso sin conexión")
        Spacer(Modifier.height(32.dp))
        Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("Iniciar sesión") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) { Text("Crear cuenta") }
    }
}

// ---------- Login ----------
@Composable
fun LoginScreen(vm: MainViewModel, onSuccess: () -> Unit, onRegister: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val emailErr = validateEmail(email)
    val passErr = if (pass.isBlank()) "Ingresa tu contraseña" else null

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AuthHeader("Proyecto carabinero", "Estudio, repaso y autoevaluación")
        Spacer(Modifier.height(24.dp))
        AppTextField(email, { email = it; error = null }, "Correo", Icons.Default.Email,
            if (submitted) emailErr else null, keyboardType = KeyboardType.Email)
        AppTextField(pass, { pass = it; error = null }, "Contraseña", Icons.Default.Lock,
            if (submitted) passErr else null, password = true,
            keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                submitted = true
                if (emailErr == null && passErr == null) {
                    if (vm.login(email, pass)) onSuccess() else error = "Correo o contraseña incorrectos"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Iniciar sesión") }
        TextButton(onClick = onRegister) { Text("¿No tienes cuenta? Regístrate") }
    }
}

// ---------- Registro ----------
@Composable
fun RegisterScreen(vm: MainViewModel, onDone: () -> Unit, onBack: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var pass2 by rememberSaveable { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val nameErr = validateName(name)
    val emailErr = validateEmail(email)
    val passErr = validatePassword(pass)
    val pass2Err = if (pass2 != pass) "Las contraseñas no coinciden" else null

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AuthHeader("Crear cuenta", "Completa tus datos para registrarte")
        Spacer(Modifier.height(24.dp))
        AppTextField(name, { name = it }, "Nombre", Icons.Default.Person, if (submitted) nameErr else null)
        AppTextField(email, { email = it; error = null }, "Correo", Icons.Default.Email,
            if (submitted) emailErr else null, keyboardType = KeyboardType.Email)
        AppTextField(pass, { pass = it }, "Contraseña", Icons.Default.Lock,
            if (submitted) passErr else null, password = true, keyboardType = KeyboardType.Password)
        AppTextField(pass2, { pass2 = it }, "Repetir contraseña", Icons.Default.Lock,
            if (submitted) pass2Err else null, password = true,
            keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                submitted = true
                if (listOf(nameErr, emailErr, passErr, pass2Err).all { it == null }) {
                    if (vm.register(name, email, pass)) onDone() else error = "Ese correo ya está registrado"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Registrarme") }
        TextButton(onClick = onBack) { Text("Ya tengo cuenta") }
    }
}