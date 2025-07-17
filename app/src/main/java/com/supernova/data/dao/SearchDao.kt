package com.supernova.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.supernova.data.entities.SearchQueryEntity
import com.supernova.data.entities.SearchResult
import com.supernova.utils.SearchUtils

@Dao
interface SearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuery(entity: SearchQueryEntity)

    @Query("SELECT * FROM search_query ORDER BY searchedAt DESC LIMIT :limit")
    fun getRecentQueries(limit: Int): kotlinx.coroutines.flow.Flow<List<SearchQueryEntity>>

    @Query(
        """
        SELECT s.stream_id AS id, s.title AS title, s.stream_type AS type, s.thumbnail_url AS posterUrl, NULL AS startAt,
            CASE WHEN lower(s.title) = :q THEN 0 WHEN lower(s.title) LIKE :q || '%' THEN 1 ELSE 2 END AS relevance
        FROM stream s JOIN stream_fts ON s.stream_id = stream_fts.rowid
        WHERE stream_fts MATCH :q AND length(:q) >= 3
        """
    )
    suspend fun searchStreamsInternal(q: String): List<SearchResult>

    @Query(
        """
        SELECT lt.channel_id AS id, lt.name AS title, 'live' AS type, lt.stream_icon AS posterUrl,
            upcoming.startAt AS startAt,
            CASE WHEN lower(lt.name) = :q THEN 0 WHEN lower(lt.name) LIKE :q || '%' THEN 1 ELSE 2 END -
            CASE WHEN upcoming.startAt IS NOT NULL AND upcoming.startAt - :now < 3600000 THEN 1 ELSE 0 END AS relevance
        FROM live_tv lt JOIN stream_fts ON lt.channel_id = stream_fts.rowid
        LEFT JOIN (
            SELECT epgChannelId, MIN(startAt) AS startAt FROM epg_programme
            WHERE startAt >= :now GROUP BY epgChannelId
        ) upcoming ON upcoming.epgChannelId = lt.epg_channel_id
        WHERE stream_fts MATCH :q AND length(:q) >= 3
        """
    )
    suspend fun searchLiveInternal(q: String, now: Long): List<SearchResult>

    @Query(
        """
        SELECT e.programmeId AS id, e.title AS title, 'episode' AS type, NULL AS posterUrl,
            e.startAt AS startAt,
            CASE WHEN lower(e.title) = :q THEN 0 WHEN lower(e.title) LIKE :q || '%' THEN 1 ELSE 2 END AS relevance
        FROM epg_programme e JOIN epg_programme_fts ON e.rowid = epg_programme_fts.rowid
        WHERE epg_programme_fts MATCH :q AND length(:q) >= 3
        """
    )
    suspend fun searchEpisodesInternal(q: String): List<SearchResult>

    @Transaction
    suspend fun searchAll(rawQuery: String, now: Long): List<SearchResult> {
        val norm = SearchUtils.normalize(rawQuery)
        if (norm.length < 3) return emptyList()
        val results = mutableListOf<SearchResult>()
        results += searchStreamsInternal(norm)
        results += searchLiveInternal(norm, now)
        results += searchEpisodesInternal(norm)
        return results.sortedWith(compareBy<SearchResult> { it.relevance }.thenBy { it.startAt ?: Long.MAX_VALUE })
    }
}
