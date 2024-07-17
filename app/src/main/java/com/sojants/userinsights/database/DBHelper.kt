package com.sojants.userinsights.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.sojants.userinsights.models.User
import java.util.*

class SqliteDatabase(context: Context?) :
    SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    ) {

    override fun onCreate(db: SQLiteDatabase) {
        val createContactTable = ("CREATE TABLE "
                + TABLE_User + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT,"
                + COLUMN_GENDER + " TEXT,"
                + COLUMN_AGE + " INTEGER,"
                + COLUMN_PHOTO + " BLOB" + ")")
        db.execSQL(createContactTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_User")
        onCreate(db)
    }

    fun listUser(): ArrayList<User> {
        val sql = "SELECT * FROM $TABLE_User"
        val db = this.readableDatabase
        val storeUser = ArrayList<User>()
        val cursor: Cursor = db.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
                val gender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER))
                val age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE))
                val photo = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_PHOTO))
                storeUser.add(User(id, name, email, gender, age, photo))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return storeUser
    }

    fun addUser(User: User) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, User.name)
        values.put(COLUMN_EMAIL, User.email)
        values.put(COLUMN_GENDER, User.gender)
        values.put(COLUMN_AGE, User.age)
        values.put(COLUMN_PHOTO, User.photo)
        db.insert(TABLE_User, null, values)
    }

    fun updateUser(User: User) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, User.name)
        values.put(COLUMN_EMAIL, User.email)
        values.put(COLUMN_GENDER, User.gender)
        values.put(COLUMN_AGE, User.age)
        values.put(COLUMN_PHOTO, User.photo)
        db.update(
            TABLE_User,
            values,
            "$COLUMN_ID = ?",
            arrayOf(User.id.toString())
        )
    }

    fun updateUserPhoto(userId: Int, photo: ByteArray) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PHOTO, photo)
        }
        db.update(
            TABLE_User,
            values,
            "$COLUMN_ID = ?",
            arrayOf(userId.toString())
        )
    }


    fun deleteContact(id: Int) {
        val db = this.writableDatabase
        db.delete(
            TABLE_User,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    fun getCountByAgeRange(minAge: Int, maxAge: Int): Int {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_User WHERE $COLUMN_AGE BETWEEN ? AND ?",
            arrayOf(minAge.toString(), maxAge.toString())
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "UserDB"
        private const val TABLE_User = "User"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_GENDER = "gender"
        private const val COLUMN_AGE = "age"
        private const val COLUMN_PHOTO = "photo"
    }
}