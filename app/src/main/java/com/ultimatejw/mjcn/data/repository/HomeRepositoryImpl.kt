package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.model.ApiResult
import com.ultimatejw.mjcn.data.model.runRemote
import com.ultimatejw.mjcn.data.remote.MjcnApiService
import com.ultimatejw.mjcn.data.remote.dto.InformationListItemDto
import com.ultimatejw.mjcn.data.remote.dto.NoticeListItemDto
import com.ultimatejw.mjcn.data.remote.dto.TodayScheduleDto
import com.ultimatejw.mjcn.domain.model.DashboardData
import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.model.TodayClass
import com.ultimatejw.mjcn.domain.repository.HomeRepository
import com.ultimatejw.mjcn.utils.toRelativeTime
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val api: MjcnApiService
) : HomeRepository {

    override suspend fun getDashboard(): ApiResult<DashboardData> =
        runRemote { api.getDashboard() }.let { result ->
            when (result) {
                is ApiResult.Success -> {
                    val dto = result.body
                    ApiResult.Success(
                        DashboardData(
                            userName = dto.greeting.userName,
                            todayClasses = dto.todaySchedule.map { it.toDomain() },
                            notices = dto.notices.map { it.toDomain() },
                            infoList = dto.information.map { it.toDomain() },
                            unreadNotificationCount = dto.unreadNotificationCount ?: 0,
                            graduationProgressPercent = dto.graduationProgressPercent ?: 0
                        )
                    )
                }
                is ApiResult.Error -> result
            }
        }
}

private fun TodayScheduleDto.toDomain() = TodayClass(
    id = id.toString(),
    name = courseName,
    startTime = startTime.take(5),
    endTime = endTime.take(5),
    building = building ?: "",
    room = room ?: "",
    professor = professor ?: ""
)

private fun NoticeListItemDto.toDomain() = Notice(
    id = id.toString(),
    title = titleWithoutDept.ifBlank { title },
    category = source.toNoticeCategoryLabel(),
    team = departmentDisplay,
    date = publishedAt.toRelativeTime()
)

private fun InformationListItemDto.toDomain() = Info(
    id = id.toString(),
    title = title,
    category = categories?.firstOrNull() ?: "",
    team = organizer ?: "",
    isGroup = false,
    dday = dDay ?: 0
)

private fun String.toNoticeCategoryLabel(): String = when (this) {
    "academic" -> "학사"
    "general" -> "일반"
    "event" -> "일반"
    "scholarship" -> "장학"
    "overseas" -> "해외"
    "student_activity" -> "학생활동"
    "career" -> "진로/취업/창업"
    "contest" -> "공모전"
    "opentalk" -> "일반"
    else -> "일반"
}
