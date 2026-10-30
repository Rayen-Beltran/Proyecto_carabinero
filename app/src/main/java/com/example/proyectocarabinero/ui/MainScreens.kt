package com.example.proyectocarabinero.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectocarabinero.viewmodel.MainViewModel
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp

// Color de Carabineros
private val VerdeCarabineros = Color(0xFF00512C)

@Composable
fun HomeScreen(
    vm: MainViewModel,
    onMaterias: () -> Unit = {},
    onAnuncios: () -> Unit = {},
    onCalendario: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val user = vm.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = VerdeCarabineros),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatar(vm, 52.dp, tint = Color.White)

                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = "Bienvenido,",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = user?.name ?: "Usuario",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user?.email ?: "",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Cerrar sesión",
                        tint = Color.White
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //  Anuncios del día
            FeatureCard(
                index = 0,
                icon = Icons.Default.Campaign,
                title = "Anuncios del dia",
                subtitle = "Revisa las instrucciones y avisos vigentes",
                enabled = true,
                onClick = onAnuncios
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    FeatureCard(
                        index = 1,
                        icon = Icons.Default.Book,
                        title = "Materias",
                        subtitle = "Ver ramos",
                        enabled = true,
                        onClick = onMaterias
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    FeatureCard(
                        index = 2,
                        icon = Icons.Default.CalendarMonth,
                        title = "Calendario",
                        subtitle = "Evaluaciones",
                        enabled = true,
                        onClick = onCalendario
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    index: Int,
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    // Animación de las tarjetas del Inicio
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400, delayMillis = index * 100)) +
                slideInVertically(tween(400, delayMillis = index * 100)) { it / 3 }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = if (enabled) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
fun ProfileScreen(vm: MainViewModel, onEdit: () -> Unit, onLogout: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        ProfileAvatar(vm, 120.dp)
        Text(vm.user?.name ?: "", style = MaterialTheme.typography.headlineSmall)
        Text(vm.user?.email ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, null)
            Spacer(Modifier.width(8.dp))
            Text("Editar datos")
        }
        Button(onClick = onLogout) {
            Icon(Icons.Default.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesion")
        }
    }
}
@Composable
fun EditProfileScreen(vm: MainViewModel, onDone: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(vm.user?.name ?: "") }
    var email by rememberSaveable { mutableStateOf(vm.user?.email ?: "") }
    var newPass by rememberSaveable { mutableStateOf("") }
    var current by rememberSaveable { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val nameErr = validateName(name)
    val emailErr = validateEmail(email)
    val newPassErr = if (newPass.isEmpty()) null else validatePassword(newPass)
    val currentErr = if (current.isEmpty()) "Ingresa tu contraseña actual para confirmar" else null

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        vm.onPhotoTaken(ok)
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { vm.setPhotoFromGallery(it) }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("Editar mis datos", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        // Foto de perfil (recurso nativo: cámara y galería)
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileAvatar(vm, 110.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    try {
                        camera.launch(vm.newPhotoUri())
                    } catch (e: ActivityNotFoundException) {
                        vm.showMessage("No hay una app de cámara disponible")
                    }
                }) {
                    Icon(Icons.Default.PhotoCamera, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Tomar foto")
                }
                OutlinedButton(onClick = {
                    gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(Icons.Default.PhotoLibrary, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Galería")
                }
            }
            if (vm.photoVersion >= 0 && vm.hasPhoto()) {
                TextButton(onClick = { vm.removePhoto() }) { Text("Quitar foto") }
            }
        }
        Spacer(Modifier.height(8.dp))

        AppTextField(name, { name = it }, "Nombre", Icons.Default.Person, if (submitted) nameErr else null)
        AppTextField(email, { email = it; error = null }, "Correo", Icons.Default.Email,
            if (submitted) emailErr else null, keyboardType = KeyboardType.Email)
        AppTextField(newPass, { newPass = it }, "Nueva contraseña (opcional)", Icons.Default.Lock,
            if (submitted) newPassErr else null, password = true, keyboardType = KeyboardType.Password)
        AppTextField(current, { current = it; error = null }, "Contraseña actual", Icons.Default.Lock,
            if (submitted) currentErr else null, password = true,
            keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = {
                submitted = true
                if (listOf(nameErr, emailErr, newPassErr, currentErr).all { it == null }) {
                    error = vm.updateProfile(name, email, current, newPass)
                    if (error == null) onDone()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar cambios") }
        TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
    }
}
@Composable
fun ProfileAvatar(
    vm: MainViewModel,
    size: Dp,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val photo = remember(vm.photoVersion, vm.user?.id) { vm.loadPhoto()?.asImageBitmap() }
    if (photo != null) {
        Image(
            bitmap = photo,
            contentDescription = "Foto de perfil",
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(CircleShape)
        )
    } else {
        Icon(Icons.Default.AccountCircle, "Sin foto de perfil", modifier.size(size), tint = tint)
    }
}