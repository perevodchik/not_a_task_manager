package com.perevodchik.not_a_task_manager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context?) :
    SQLiteOpenHelper(
        context,
        Values.DATABASE_NAME,
        null,
        DATABASE_VERSION
    ) {

    companion object {
        var liteDatabase: SQLiteDatabase? = null
        private const val DATABASE_VERSION = 2

        fun update(id: Long, values: ContentValues): Long {
            return liteDatabase!!.update(Values.TABLE_NAME, values, "${Values.columnId} = ?", arrayOf("$id")).toLong()
        }

        fun create(values: ContentValues): Long {
            return liteDatabase!!.insert(Values.TABLE_NAME, null, values)
        }

        fun delete(id: Long) {
            liteDatabase!!.delete(Values.TABLE_NAME, "${Values.columnId} = ?", arrayOf("$id"))
        }
    }

    init {
        liteDatabase = this.readableDatabase
    }

    override fun onCreate(db: SQLiteDatabase) { // Строка для создания таблицы
        val sqlCreateTable =
            ("CREATE TABLE IF NOT EXISTS " + Values.TABLE_NAME + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "title TEXT NOT NULL, "
                    + "text TEXT NOT NULL, "
                    + "tags TEXT NOT NULL, "
                    + "isFavorite INTEGER NOT NULL DEFAULT(0),"
                    + "updateTime INTEGER NOT NULL,"
                    + "createTime INTEGER NOT NULL);")
        // Запускаем создание таблицы
        db.execSQL(sqlCreateTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS ${Values.TABLE_NAME}")
        onCreate(db)
    }
}