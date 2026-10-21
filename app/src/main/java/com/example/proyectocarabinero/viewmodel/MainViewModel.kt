package com.example.proyectocarabinero.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.proyectocarabinero.data.AppDb
import com.example.proyectocarabinero.data.Doc
import com.example.proyectocarabinero.data.User

class MainViewModel(private val app: Application) : AndroidViewModel(app) {

    companion object {
        val ALLOWED_MIME = listOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
    }

    private val db = AppDb(app)
    private val prefs = app.getSharedPreferences("session", Context.MODE_PRIVATE)

    var user by mutableStateOf(prefs.getLong("user_id", -1L).let { if (it > 0) db.getUser(it) else null })
        private set
    var documents by mutableStateOf<List<Doc>>(emptyList())
        private set
    var message by mutableStateOf<String?>(null)
        private set

    init { loadDocs() }

    private fun loadDocs() {
        documents = user?.let { db.docs(it.id) } ?: emptyList()
    }

    fun showMessage(m: String) { message = m }
    fun consumeMessage() { message = null }

    fun register(name: String, email: String, pass: String): Boolean {
        val ok = db.register(name.trim(), email.trim(), pass)
        if (ok) message = "Cuenta creada. Ahora inicia sesión"
        return ok
    }

    fun login(email: String, pass: String): Boolean {
        val u = db.login(email.trim(), pass) ?: return false
        prefs.edit().putLong("user_id", u.id).apply()
        user = u
        loadDocs()
        return true
    }

    fun logout() {
        prefs.edit().remove("user_id").apply()
        user = null
        documents = emptyList()
    }

    fun addDocument(uri: Uri) {
        val u = user ?: return
        val resolver = app.contentResolver
        val mime = resolver.getType(uri)
        if (mime == null || mime !in ALLOWED_MIME) {
            message = "Solo se permiten archivos PDF o Word (.doc, .docx)"
            return
        }
        var name = "documento"
        var size = 0L
        resolver.query(uri, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val n = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val s = c.getColumnIndex(OpenableColumns.SIZE)
                if (n >= 0) name = c.getString(n)
                if (s >= 0 && !c.isNull(s)) size = c.getLong(s)
            }
        }
        try {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: Exception) { }
        db.addDoc(u.id, name, uri.toString(), mime, size)
        loadDocs()
        message = "Documento cargado"
    }

    fun deleteDocument(doc: Doc) {
        try {
            app.contentResolver.releasePersistableUriPermission(
                Uri.parse(doc.uri), Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) { }
        db.deleteDoc(doc.id)
        loadDocs()
        message = "Documento eliminado"
    }
}