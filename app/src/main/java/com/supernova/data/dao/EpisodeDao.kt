package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.EpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episode ORDER BY episode_num ASC")
    fun getAllEpisodes(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episode WHERE episode_id = :episodeId")
    suspend fun getEpisodeById(episodeId: Int): EpisodeEntity?

    @Query("SELECT * FROM episode WHERE series_id = :seriesId AND season_number = :seasonNumber ORDER BY episode_num ASC")
    fun getEpisodesBySeriesAndSeason(seriesId: Int, seasonNumber: Int): Flow<List<EpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: EpisodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    @Update
    suspend fun updateEpisode(episode: EpisodeEntity)

    @Delete
    suspend fun deleteEpisode(episode: EpisodeEntity)

    @Query("DELETE FROM episode WHERE episode_id = :episodeId")
    suspend fun deleteEpisodeById(episodeId: Int)

    @Query("DELETE FROM episode")
    suspend fun deleteAllEpisodes()
}
