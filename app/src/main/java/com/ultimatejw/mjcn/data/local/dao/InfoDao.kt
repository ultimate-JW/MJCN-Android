package com.ultimatejw.mjcn.data.local.dao

import androidx.room.*
import com.ultimatejw.mjcn.data.local.entity.InfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InfoDao {
    @Query("SELECT * FROM infos WHERE isBookmarked = 1 ORDER BY id DESC")
    fun getBookmarkedInfos(): Flow<List<InfoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(info: InfoEntity)

    @Query("UPDATE infos SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun updateBookmark(id: String, bookmarked: Boolean)
}
