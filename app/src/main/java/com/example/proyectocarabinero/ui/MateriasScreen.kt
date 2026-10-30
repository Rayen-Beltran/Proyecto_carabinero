package com.example.proyectocarabinero.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File

data class MateriaItem(
    val id: Int,
    val titulo: String,
    val descripcion: String
)

val listaMaterias = listOf(
    MateriaItem(1, "Materia 1", "Derecho Policial y Normativa General"),
    MateriaItem(2, "Materia 2", "Procedimientos Operativos Policiales"),
    MateriaItem(3, "Materia 3", "Criminalística y Manejo de Evidencia"),
    MateriaItem(4, "Materia 4", "Derechos Humanos Aplicados a la Función Policial"),
    MateriaItem(5, "Materia 5", "Primeros Auxilios y Gestión de Emergencias")
)

@Composable
fun MateriasScreen(
    materiaSeleccionada: MateriaItem?,
    onSeleccionarMateria: (MateriaItem?) -> Unit
) {
    if (materiaSeleccionada == null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(listaMaterias) { index, materia ->
                MateriaCard(index = index, materia = materia, onClick = {
                    onSeleccionarMateria(materia)
                })
            }
        }
    } else {
        DetalleMateriaScreen()
    }
}

@Composable
private fun MateriaCard(
    index: Int,
    materia: MateriaItem,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400, delayMillis = index * 80)) +
                slideInVertically(tween(400, delayMillis = index * 80)) { it / 3 }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = materia.titulo,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = materia.descripcion,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DetalleMateriaScreen() {
    val context = LocalContext.current
    var expandido by remember { mutableStateOf(true) }
    var mostrarInputDocumento by remember { mutableStateOf(false) }

    var mensajeInput by remember { mutableStateOf("") }
    var archivoAdjunto by remember { mutableStateOf<String?>(null) }

    var refrescarArchivos by remember { mutableStateOf(0) }

    val listaArchivosGuardados = remember(refrescarArchivos) {
        context.filesDir.listFiles()
            ?.filter { archivo ->
                archivo.isFile &&
                        !archivo.name.contains("profile", ignoreCase = true) &&
                        !archivo.name.startsWith("foto_", ignoreCase = true)
            }
            ?: emptyList()
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            try {
                val nombreArchivo = selectedUri.lastPathSegment?.substringAfterLast("/")
                    ?: "documento_${System.currentTimeMillis()}.pdf"

                val archivoDestino = File(context.filesDir, nombreArchivo)

                context.contentResolver.openInputStream(selectedUri)?.use { inputStream ->
                    archivoDestino.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                archivoAdjunto = nombreArchivo
                refrescarArchivos++
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Contenido del curso",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandido = !expandido }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Información de la Asignatura",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = { expandido = !expandido }) {
                        Icon(
                            imageVector = if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }
                }

                AnimatedVisibility(visible = expandido) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider()

                        ListItem(
                            headlineContent = {
                                Text("Descripción de la Asignatura", fontWeight = FontWeight.Medium)
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.clickable { }
                        )

                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))

                        ListItem(
                            headlineContent = {
                                Text("Evaluación Docente", fontWeight = FontWeight.Medium)
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.clickable { }
                        )

                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = if (mostrarInputDocumento) "Ocultar opción de documento" else "Agregar Documento",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.clickable {
                                mostrarInputDocumento = !mostrarInputDocumento
                            }
                        )

                        AnimatedVisibility(visible = mostrarInputDocumento) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                OutlinedTextField(
                                    value = if (archivoAdjunto != null) "📎 $archivoAdjunto" else mensajeInput,
                                    onValueChange = { if (archivoAdjunto == null) mensajeInput = it },
                                    placeholder = { Text("Escriba un mensaje") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    readOnly = archivoAdjunto != null,
                                    trailingIcon = {
                                        Row {
                                            if (archivoAdjunto == null) {
                                                IconButton(onClick = { filePicker.launch("*/*") }) {
                                                    Icon(
                                                        imageVector = Icons.Default.AttachFile,
                                                        contentDescription = "Adjuntar archivo",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            } else {
                                                IconButton(onClick = { archivoAdjunto = null }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Cancelar",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }

                                            IconButton(onClick = {
                                                if (archivoAdjunto == null && mensajeInput.isNotBlank()) {
                                                    val nombreTxt = "Mensaje_${System.currentTimeMillis()}.txt"
                                                    val archivoTxt = File(context.filesDir, nombreTxt)
                                                    archivoTxt.writeText(mensajeInput)
                                                }

                                                mensajeInput = ""
                                                archivoAdjunto = null
                                                refrescarArchivos++
                                            }) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                                    contentDescription = "Enviar",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (listaArchivosGuardados.isNotEmpty()) {
            Text(
                text = "Documentos Adjuntos",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    listaArchivosGuardados.forEachIndexed { index, archivo ->
                        if (index > 0) {
                            HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        }

                        val textoMostrado = if (archivo.name.endsWith(".txt")) {
                            "💬 ${archivo.readText()}"
                        } else {
                            archivo.name
                        }

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = textoMostrado,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingContent = {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                IconButton(onClick = {
                                    if (archivo.exists()) {
                                        archivo.delete()
                                        refrescarArchivos++
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar documento",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            // AL HACER CLICK EN EL ELEMENTO SE ABRE EL ARCHIVO
                            modifier = Modifier.clickable {
                                abrirDocumento(context, archivo)
                            }
                        )
                    }
                }
            }
        }
    }
}

// FUNCIÓN PARA ABRIR EL ARCHIVO CON APLICACIONES EXTERNAS
private fun abrirDocumento(context: Context, file: File) {
    try {
        if (!file.exists()) {
            Toast.makeText(context, "El archivo no existe", Toast.LENGTH_SHORT).show()
            return
        }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val mimeType = when {
            file.name.endsWith(".pdf", true) -> "application/pdf"
            file.name.endsWith(".txt", true) -> "text/plain"
            file.name.endsWith(".jpg", true) || file.name.endsWith(".png", true) -> "image/*"
            else -> "*/*"
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Abrir con"))
    } catch (e: Exception) {
        Toast.makeText(context, "No se encontró una aplicación para abrir este archivo", Toast.LENGTH_SHORT).show()
    }
}