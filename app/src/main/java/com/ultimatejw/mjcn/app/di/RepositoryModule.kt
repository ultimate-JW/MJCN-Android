package com.ultimatejw.mjcn.app.di

import com.ultimatejw.mjcn.data.repository.ChatRepositoryImpl
import com.ultimatejw.mjcn.data.repository.NoticeRepositoryImpl
import com.ultimatejw.mjcn.data.repository.UserRepositoryImpl
import com.ultimatejw.mjcn.domain.repository.ChatRepository
import com.ultimatejw.mjcn.domain.repository.NoticeRepository
import com.ultimatejw.mjcn.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoticeRepository(impl: NoticeRepositoryImpl): NoticeRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository
}
