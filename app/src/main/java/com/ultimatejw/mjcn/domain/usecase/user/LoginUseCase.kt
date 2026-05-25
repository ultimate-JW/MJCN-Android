package com.ultimatejw.mjcn.domain.usecase.user

import com.ultimatejw.mjcn.data.model.ApiResult
import com.ultimatejw.mjcn.data.model.runRemote
import com.ultimatejw.mjcn.data.remote.MjcnApiService
import com.ultimatejw.mjcn.data.remote.dto.LoginRequestDto
import com.ultimatejw.mjcn.domain.repository.UserRepository
import org.json.JSONObject
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val api: MjcnApiService,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): ApiResult<Unit> {
        return when (val result = runRemote { api.login(LoginRequestDto(email, password)) }) {
            is ApiResult.Success -> {
                userRepository.saveTokens(result.body.access, result.body.refresh)
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> ApiResult.Error(
                parseErrorMessage(result.message, result.code),
                result.code
            )
        }
    }

    private fun parseErrorMessage(errorBody: String?, code: Int?): String {
        if (!errorBody.isNullOrBlank()) {
            try {
                val json = JSONObject(errorBody)
                json.optString("detail").takeIf { it.isNotBlank() }?.let { return it }
                json.optJSONArray("non_field_errors")?.takeIf { it.length() > 0 }
                    ?.let { return it.getString(0) }
                json.optJSONArray("email")?.takeIf { it.length() > 0 }
                    ?.let { return "이메일: ${it.getString(0)}" }
                json.optJSONArray("password")?.takeIf { it.length() > 0 }
                    ?.let { return "비밀번호: ${it.getString(0)}" }
            } catch (_: Exception) {}
        }
        return when (code) {
            400 -> "입력 정보를 다시 확인해주세요."
            401 -> "이메일 또는 비밀번호가 일치하지 않습니다."
            429 -> "너무 많은 시도입니다. 잠시 후 다시 시도해주세요."
            in 500..599 -> "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            null -> "네트워크 연결을 확인해주세요."
            else -> "로그인에 실패했습니다."
        }
    }
}
