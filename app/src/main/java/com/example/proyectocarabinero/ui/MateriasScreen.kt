package com.example.proyectocarabinero.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    var expandido by remember { mutableStateOf(true) }
    var mostrarInputDocumento by remember { mutableStateOf(false) }

    var mensajeInput by remember { mutableStateOf("") }
    var archivoAdjunto by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            archivoAdjunto = uri.lastPathSegment ?: "Documento_adjunto.pdf"
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

                        // Botón verde para activar la zona de adjuntar/subir documento
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

                        // Sección expandible con el cuadro de texto e iconos (clip, eliminar, enviar)
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
                                                // Icono de Clip pa
                                                IconButton(onClick = { filePicker.launch("*/*") }) {
                                                    Icon(
                                                        imageVector = Icons.Default.AttachFile,
                                                        contentDescription = "Adjuntar archivo",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            } else {
                                                //Icono de Basura
                                                IconButton(onClick = { archivoAdjunto = null }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Eliminar archivo",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }

                                            // Icono de Subir
                                            IconButton(onClick = {
                                                mensajeInput = ""
                                                archivoAdjunto = null
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
    }
}