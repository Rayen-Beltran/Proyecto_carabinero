package com.example.proyectocarabinero.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.proyectocarabinero.data.Doc
import com.example.proyectocarabinero.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------- Inicio ----------
@Composable
fun HomeScreen(vm: MainViewModel, onDocs: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hola, ${vm.user?.name?.substringBefore(' ') ?: ""}", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Tu herramienta de estudio, repaso y autoevaluación.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FeatureCard(0, Icons.Default.Folder, "Mis documentos",
            "${vm.documents.size} archivo(s) guardado(s) en tu teléfono", true, onDocs)
        FeatureCard(1, Icons.Default.Quiz, "Autoevaluación", "Próximamente", false) {}
        FeatureCard(2, Icons.Default.Policy, "Casos simulados", "Próximamente", false) {}
    }
}

@Composable
private fun FeatureCard(
    index: Int, icon: ImageVector, title: String, subtitle: String,
    enabled: Boolean, onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible,
        enter = fadeIn(tween(400, delayMillis = index * 120)) +
                slideInVertically(tween(400, delayMillis = index * 120)) { it / 3 }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = if (enabled) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, Modifier.size(36.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ---------- Documentos ----------
@Composable
fun DocumentsScreen(vm: MainViewModel) {
    val context = LocalContext.current
    var toDelete by remember { mutableStateOf<Doc?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { vm.addDocument(it) }
    }

    Box(Modifier.fillMaxSize()) {
        if (vm.documents.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Folder, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(8.dp))
                Text("Aún no has cargado documentos", style = MaterialTheme.typography.titleMedium)
                Text("Toca “Cargar” para agregar un PDF o Word", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vm.documents, key = { it.id }) { doc ->
                    DocCard(
                        doc,
                        onOpen = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.parse(doc.uri), doc.mime)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                vm.showMessage("No se pudo abrir. ¿Hay una app instalada para este archivo?")
                            }
                        },
                        onDelete = { toDelete = doc },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { picker.launch(arrayOf(*MainViewModel.ALLOWED_MIME.toTypedArray())) },
            icon = { Icon(Icons.Default.Add, null) },
            text = { Text("Cargar") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    toDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar documento") },
            text = { Text("¿Quitar “${doc.name}” de la aplicación?") },
            confirmButton = {
                TextButton(onClick = { vm.deleteDocument(doc); toDelete = null }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun DocCard(doc: Doc, onOpen: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val isPdf = doc.mime == "application/pdf"
    Card(modifier.fillMaxWidth().animateContentSize()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Description, null,
                Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(doc.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${if (isPdf) "PDF" else "Word"} · ${formatSize(doc.size)} · " +
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(doc.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onOpen) { Icon(Icons.Default.Visibility, "Ver") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun formatSize(b: Long): String = when {
    b <= 0 -> "—"
    b < 1024 -> "$b B"
    b < 1024 * 1024 -> "${b / 1024} KB"
    else -> String.format(Locale.US, "%.1f MB", b / 1024.0 / 1024.0)
}

// ---------- Perfil / cierre de sesión ----------
@Composable
fun ProfileScreen(vm: MainViewModel, onLogout: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Icon(Icons.Default.AccountCircle, null, Modifier.size(96.dp), tint = MaterialTheme.colorScheme.primary)
        Text(vm.user?.name ?: "", style = MaterialTheme.typography.headlineSmall)
        Text(vm.user?.email ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onLogout) {
            Icon(Icons.Default.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }
}