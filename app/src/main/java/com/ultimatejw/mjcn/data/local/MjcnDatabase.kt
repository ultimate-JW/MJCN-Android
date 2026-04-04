package com.ultimatejw.mjcn.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ultimatejw.mjcn.data.local.dao.ChatDao
import com.ultimatejw.mjcn.data.local.dao.NoticeDao
import com.ultimatejw.mjcn.data.local.dao.UserDao
import com.ultimatejw.mjcn.data.model.ChatSession
import com.ultimatejw.mjcn.data.model.Notice
import com.ultimatejw.mjcn.data.model.User

@Database(
    entities = [User::class, Notice::class, ChatSession::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MjcnDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun noticeDao(): NoticeDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: MjcnDatabase? = null

        fun getInstance(context: Context): MjcnDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MjcnDatabase::class.java,
                    "mjcn_database"
                ).build().also { INSTANCE = it }
            }
    }
}
