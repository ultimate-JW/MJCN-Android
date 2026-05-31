package com.ultimatejw.mjcn.app.di

import com.ultimatejw.mjcn.data.repository.AuthRepositoryImpl
import com.ultimatejw.mjcn.data.repository.BookmarkRepositoryImpl
import com.ultimatejw.mjcn.data.repository.HomeRepositoryImpl
import com.ultimatejw.mjcn.data.repository.ChatRepositoryImpl
import com.ultimatejw.mjcn.data.repository.CourseHistoryRepositoryImpl
import com.ultimatejw.mjcn.data.repository.CurrentCourseRepositoryImpl
import com.ultimatejw.mjcn.data.repository.InfoRepositoryImpl
import com.ultimatejw.mjcn.data.repository.InterestRepositoryImpl
import com.ultimatejw.mjcn.data.repository.NoticeRepositoryImpl
import com.ultimatejw.mjcn.data.repository.NotificationRepositoryImpl
import com.ultimatejw.mjcn.data.repository.ProfileRepositoryImpl
import com.ultimatejw.mjcn.data.repository.ThemeRepositoryImpl
import com.ultimatejw.mjcn.data.repository.UserRepositoryImpl
import com.ultimatejw.mjcn.domain.repository.AuthRepository
import com.ultimatejw.mjcn.domain.repository.BookmarkRepository
import com.ultimatejw.mjcn.domain.repository.HomeRepository
import com.ultimatejw.mjcn.domain.repository.ChatRepository
import com.ultimatejw.mjcn.domain.repository.CourseHistoryRepository
import com.ultimatejw.mjcn.domain.repository.CurrentCourseRepository
import com.ultimatejw.mjcn.domain.repository.InfoRepository
import com.ultimatejw.mjcn.domain.repository.InterestRepository
import com.ultimatejw.mjcn.domain.repository.NoticeRepository
import com.ultimatejw.mjcn.domain.repository.NotificationRepository
import com.ultimatejw.mjcn.domain.repository.ProfileRepository
import com.ultimatejw.mjcn.domain.repository.ThemeRepository
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
    abstract fun bindBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindInfoRepository(impl: InfoRepositoryImpl): InfoRepository

    @Binds
    @Singleton
    abstract fun bindNoticeRepository(impl: NoticeRepositoryImpl): NoticeRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindInterestRepository(impl: InterestRepositoryImpl): InterestRepository

    @Binds
    @Singleton
    abstract fun bindCourseHistoryRepository(impl: CourseHistoryRepositoryImpl): CourseHistoryRepository

    @Binds
    @Singleton
    abstract fun bindCurrentCourseRepository(impl: CurrentCourseRepositoryImpl): CurrentCourseRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository
}
