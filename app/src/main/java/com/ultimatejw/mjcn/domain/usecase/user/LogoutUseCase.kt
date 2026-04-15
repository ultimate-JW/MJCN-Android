package com.ultimatejw.mjcn.domain.usecase.user

import com.ultimatejw.mjcn.domain.repository.UserRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke() = repository.logout()
}
