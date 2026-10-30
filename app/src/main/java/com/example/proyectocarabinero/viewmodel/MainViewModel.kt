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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.core.content.FileProvider
import java.io.File
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
    fun updateProfile(name: String, email: String, currentPass: String, newPass: String): String? {
        val u = user ?: return "Sesión no válida"
        if (!db.checkPassword(u.id, currentPass)) return "La contraseña actual es incorrecta"
        if (!db.updateUser(u.id, name.trim(), email.trim(), newPass)) return "Ese correo ya está registrado"
        user = db.getUser(u.id)
        message = "Datos actualizados"
        return null
    }
    // ---------- Foto de perfil ----------
    var photoVersion by mutableStateOf(0)
        private set

    private fun avatarFile() = File(app.filesDir, "avatars/${user?.id ?: 0}.jpg").also { it.parentFile?.mkdirs() }
    private fun tempFile() = File(app.filesDir, "avatars/tmp.jpg").also { it.parentFile?.mkdirs() }

    fun newPhotoUri(): Uri =
        FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", tempFile())

    fun onPhotoTaken(ok: Boolean) {
        if (!ok) return
        tempFile().copyTo(avatarFile(), overwrite = true)
        tempFile().delete()
        photoVersion++
        message = "Foto de perfil actualizada"
    }

    fun setPhotoFromGallery(uri: Uri) {
        try {
            app.contentResolver.openInputStream(uri)?.use { input ->
                avatarFile().outputStream().use { input.copyTo(it) }
            } ?: throw java.io.IOException()
            photoVersion++
            message = "Foto de perfil actualizada"
        } catch (e: Exception) {
            message = "No se pudo cargar la imagen"
        }
    }

    fun hasPhoto() = avatarFile().exists()

    fun removePhoto() {
        avatarFile().delete()
        photoVersion++
        message = "Foto eliminada"
    }

    fun loadPhoto(): Bitmap? {
        val f = avatarFile()
        if (!f.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(f.path, bounds)
        var sample = 1
        while (bounds.outWidth / sample > 600) sample *= 2
        val bmp = BitmapFactory.decodeFile(f.path, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return null
        val degrees = when (
            ExifInterface(f.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bmp
        val m = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
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