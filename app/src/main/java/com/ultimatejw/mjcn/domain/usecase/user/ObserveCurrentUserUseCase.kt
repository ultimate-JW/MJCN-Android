package com.ultimatejw.mjcn.domain.usecase.user

import com.ultimatejw.mjcn.domain.model.User
import com.ultimatejw.mjcn.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<User?> = repository.currentUser
}
