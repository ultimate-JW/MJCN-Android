package com.ultimatejw.mjcn.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ultimatejw.mjcn.app.dataStore
import com.ultimatejw.mjcn.data.local.MjcnDatabase
import com.ultimatejw.mjcn.data.local.dao.ChatDao
import com.ultimatejw.mjcn.data.local.dao.NoticeDao
import com.ultimatejw.mjcn.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): MjcnDatabase =
        MjcnDatabase.getInstance(context)

    @Provides
    fun provideUserDao(db: MjcnDatabase): UserDao = db.userDao()

    @Provides
    fun provideNoticeDao(db: MjcnDatabase): NoticeDao = db.noticeDao()

    @Provides
    fun provideChatDao(db: MjcnDatabase): ChatDao = db.chatDao()

    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}
