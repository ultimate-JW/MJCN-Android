package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.model.ApiResult
import com.ultimatejw.mjcn.data.model.runRemote
import com.ultimatejw.mjcn.data.remote.MjcnApiService
import com.ultimatejw.mjcn.data.remote.dto.InformationDetailDto
import com.ultimatejw.mjcn.data.remote.dto.InformationListItemDto
import com.ultimatejw.mjcn.data.remote.dto.NoticeDetailDto
import com.ultimatejw.mjcn.data.remote.dto.NoticeListItemDto
import com.ultimatejw.mjcn.data.remote.dto.TodayScheduleDto
import com.ultimatejw.mjcn.domain.model.DashboardData
import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.model.NoticeCard
import com.ultimatejw.mjcn.domain.model.TodayClass
import com.ultimatejw.mjcn.domain.repository.HomeRepository
import com.ultimatejw.mjcn.utils.toRelativeTime
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val api: MjcnApiService
) : HomeRepository {

    override suspend fun getNoticeDetail(id: String): ApiResult<Notice> =
        when (val result = runRemote { api.getNoticeDetail(id) }) {
            is ApiResult.Success -> runCatching {
                ApiResult.Success(result.body.toDomain())
            }.getOrElse { ApiResult.Error(it.message) }
            is ApiResult.Error -> result
        }

    override suspend fun getInfoDetail(id: String): ApiResult<Info> =
        when (val result = runRemote { api.getInformationDetail(id) }) {
            is ApiResult.Success -> runCatching {
                ApiResult.Success(result.body.toDomain())
            }.getOrElse { ApiResult.Error(it.message) }
            is ApiResult.Error -> result
        }

    override suspend fun getDashboard(): ApiResult<DashboardData> =
        when (val result = runRemote { api.getDashboard() }) {
            is ApiResult.Success -> runCatching {
                val dto = result.body
                ApiResult.Success(
                    DashboardData(
                        userName = dto.greeting?.userName ?: "",
                        todayClasses = dto.todaySchedule?.map { it.toDomain() } ?: emptyList(),
                        notices = dto.notices?.map { it.toDomain() } ?: emptyList(),
                        infoList = dto.information?.map { it.toDomain() } ?: emptyList(),
                        unreadNotificationCount = dto.unreadNotificationCount ?: 0,
                        graduationProgressPercent = dto.graduationProgressPercent ?: 0
                    )
                )
            }.getOrElse { ApiResult.Error(it.message) }
            is ApiResult.Error -> result
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
    date = publishedAt.toRelativeTime(),
    url = url,
    summary = summary ?: ""
)

private fun InformationListItemDto.toDomain() = Info(
    id = id.toString(),
    title = title,
    category = categories?.firstOrNull() ?: "",
    team = organizer ?: "",
    isGroup = false,
    dday = dDay ?: 0,
    url = url,
    startDate = startDate,
    endDate = endDate
)

private fun NoticeDetailDto.toDomain() = Notice(
    id = id?.toString() ?: "",
    title = titleWithoutDept?.ifBlank { title ?: "" } ?: title ?: "",
    category = source?.toNoticeCategoryLabel() ?: "",
    team = departmentDisplay ?: "",
    date = publishedAt?.toRelativeTime() ?: "",
    url = url ?: "",
    summary = aiResult?.summary?.takeIf { it.isNotBlank() } ?: summary ?: content ?: "",
    cards = aiResult?.cards?.mapNotNull { card ->
        val t = card.title?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        NoticeCard(title = t, items = card.items ?: emptyList())
    } ?: emptyList()
)

private fun InformationDetailDto.toDomain() = Info(
    id = id?.toString() ?: "",
    title = title ?: "",
    category = categories?.firstOrNull() ?: "",
    team = organizer ?: "",
    isGroup = false,
    dday = dDay ?: 0,
    url = url ?: "",
    startDate = startDate,
    endDate = endDate
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
