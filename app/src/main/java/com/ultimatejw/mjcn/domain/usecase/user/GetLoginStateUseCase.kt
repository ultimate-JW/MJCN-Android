package com.ultimatejw.mjcn.domain.usecase.user

import com.ultimatejw.mjcn.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLoginStateUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.isLoggedIn
}
