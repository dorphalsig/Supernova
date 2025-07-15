package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.SeasonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeasonDao {
    @Query("SELECT * FROM season ORDER BY season_number ASC")
    fun getAllSeasons(): Flow<List<SeasonEntity>>

    @Query("SELECT * FROM season WHERE series_id = :seriesId AND season_number = :seasonNumber")
    suspend fun getSeason(seriesId: Int, seasonNumber: Int): SeasonEntity?

    @Query("SELECT * FROM season WHERE series_id = :seriesId ORDER BY season_number ASC")
    fun getSeasonsBySeriesId(seriesId: Int): Flow<List<SeasonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeason(season: SeasonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeasons(seasons: List<SeasonEntity>)

    @Update
    suspend fun updateSeason(season: SeasonEntity)

    @Delete
    suspend fun deleteSeason(season: SeasonEntity)

    @Query("DELETE FROM season WHERE series_id = :seriesId AND season_number = :seasonNumber")
    suspend fun deleteSeasonById(seriesId: Int, seasonNumber: Int)

    @Query("DELETE FROM season")
    suspend fun deleteAllSeasons()
}
