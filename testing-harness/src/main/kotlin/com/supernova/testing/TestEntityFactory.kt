package com.supernova.testing

/** Simple Room-like entity definitions used for testing */
data class Stream(
    val id: Long,
    val title: String,
    val categoryId: Long,
    val streamType: String,
    val isLive: Boolean,
    val number: Int
)

data class Program(
    val id: Long,
    val title: String,
    val start: Long,
    val end: Long,
    val epgChannelId: Long
)

data class Episode(
    val id: Long,
    val title: String,
    val seriesId: Long,
    val seasonNum: Int,
    val episodeNum: Int,
    val vote: Double,
    val poster: String
)

data class Category(
    val id: Long,
    val name: String
)

data class Profile(
    val id: Long,
    val username: String
)

data class Favorite(
    val profileId: Long,
    val contentId: Long
)

data class WatchHistory(
    val profileId: Long,
    val contentId: Long,
    val percentWatched: Int,
    val timestamp: Long
)

data class Recommendation(
    val profileId: Long,
    val contentId: Long,
    val score: Double,
    val sourceTag: String,
    val createdAt: Long
)

data class TmdbMetadata(
    val contentId: Long,
    val tmdbId: Long,
    val genres: List<String>,
    val keywords: List<String>,
    val type: String
)

/**
 * Factory helpers for creating entities with sensible defaults for tests.
 */
open class TestEntityFactory {
    companion object {
    fun stream(
        id: Long = 1L,
        title: String = "Stream $id",
        categoryId: Long = 1L,
        streamType: String = "LIVE",
        isLive: Boolean = true,
        number: Int = 100
    ): Stream = Stream(id, title, categoryId, streamType, isLive, number)

    fun program(
        id: Long = 1L,
        title: String = "Program $id",
        start: Long = 0L,
        end: Long = start + 3_600_000L,
        epgChannelId: Long = 1L
    ): Program = Program(id, title, start, end, epgChannelId)

    fun episode(
        id: Long = 1L,
        title: String = "Episode $id",
        seriesId: Long = 1L,
        seasonNum: Int = 1,
        episodeNum: Int = 1,
        vote: Double = 0.0,
        poster: String = "poster_$id.jpg"
    ): Episode = Episode(id, title, seriesId, seasonNum, episodeNum, vote, poster)

    fun category(
        id: Long = 1L,
        name: String = "Category $id"
    ): Category = Category(id, name)

    fun profile(
        id: Long = 1L,
        username: String = "user$id"
    ): Profile = Profile(id, username)

    fun favorite(
        profileId: Long = 1L,
        contentId: Long = 1L
    ): Favorite = Favorite(profileId, contentId)

    fun watchHistory(
        profileId: Long = 1L,
        contentId: Long = 1L,
        percentWatched: Int = 0,
        timestamp: Long = System.currentTimeMillis()
    ): WatchHistory = WatchHistory(profileId, contentId, percentWatched, timestamp)

    fun recommendation(
        profileId: Long = 1L,
        contentId: Long = 1L,
        score: Double = 1.0,
        sourceTag: String = "popular",
        createdAt: Long = System.currentTimeMillis()
    ): Recommendation = Recommendation(profileId, contentId, score, sourceTag, createdAt)

    fun tmdbMetadata(
        contentId: Long = 1L,
        tmdbId: Long = 100L,
        genres: List<String> = emptyList(),
        keywords: List<String> = emptyList(),
        type: String = "movie"
    ): TmdbMetadata = TmdbMetadata(contentId, tmdbId, genres, keywords, type)
    }
}
