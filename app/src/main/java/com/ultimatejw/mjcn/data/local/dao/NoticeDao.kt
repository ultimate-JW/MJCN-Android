package com.ultimatejw.mjcn.data.local.dao

import androidx.room.*
import com.ultimatejw.mjcn.data.model.Notice
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY date DESC")
    fun getAllNotices(): Flow<List<Notice>>

    @Query("SELECT * FROM notices WHERE category = :category ORDER BY date DESC")
    fun getNoticesByCategory(category: String): Flow<List<Notice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notices: List<Notice>)

    @Query("UPDATE notices SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun updateBookmark(id: String, bookmarked: Boolean)

    @Query("DELETE FROM notices")
    suspend fun clearAll()
}
