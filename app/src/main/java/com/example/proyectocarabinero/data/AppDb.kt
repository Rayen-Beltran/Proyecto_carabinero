package com.example.proyectocarabinero.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest
import java.util.UUID

data class User(val id: Long, val name: String, val email: String)
data class Doc(
    val id: Long, val name: String, val uri: String,
    val mime: String, val size: Long, val createdAt: Long
)

class AppDb(context: Context) : SQLiteOpenHelper(context, "carabinero.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, " +
                    "email TEXT NOT NULL UNIQUE, salt TEXT NOT NULL, hash TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE documents(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, uri TEXT NOT NULL, mime TEXT NOT NULL, size INTEGER, created_at INTEGER NOT NULL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        db.execSQL("DROP TABLE IF EXISTS documents")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    private fun hash(pass: String, salt: String): String =
        MessageDigest.getInstance("SHA-256").digest((salt + pass).toByteArray())
            .joinToString("") { "%02x".format(it) }

    /** false si el correo ya existe */
    fun register(name: String, email: String, pass: String): Boolean {
        val salt = UUID.randomUUID().toString()
        val cv = ContentValues().apply {
            put("name", name); put("email", email.lowercase())
            put("salt", salt); put("hash", hash(pass, salt))
        }
        return writableDatabase.insert("users", null, cv) != -1L
    }

    fun login(email: String, pass: String): User? {
        readableDatabase.rawQuery(
            "SELECT id,name,email,salt,hash FROM users WHERE email=?", arrayOf(email.lowercase())
        ).use { c ->
            if (!c.moveToFirst()) return null
            if (hash(pass, c.getString(3)) != c.getString(4)) return null
            return User(c.getLong(0), c.getString(1), c.getString(2))
        }
    }

    fun getUser(id: Long): User? =
        readableDatabase.rawQuery("SELECT id,name,email FROM users WHERE id=?", arrayOf(id.toString()))
            .use { c -> if (c.moveToFirst()) User(c.getLong(0), c.getString(1), c.getString(2)) else null }

    fun addDoc(userId: Long, name: String, uri: String, mime: String, size: Long) {
        val cv = ContentValues().apply {
            put("user_id", userId); put("name", name); put("uri", uri)
            put("mime", mime); put("size", size); put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insert("documents", null, cv)
    }

    fun docs(userId: Long): List<Doc> {
        val list = mutableListOf<Doc>()
        readableDatabase.rawQuery(
            "SELECT id,name,uri,mime,size,created_at FROM documents WHERE user_id=? ORDER BY created_at DESC",
            arrayOf(userId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                list += Doc(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getLong(4), c.getLong(5))
            }
        }
        return list
    }

    fun deleteDoc(id: Long) {
        writableDatabase.delete("documents", "id=?", arrayOf(id.toString()))
    }
}