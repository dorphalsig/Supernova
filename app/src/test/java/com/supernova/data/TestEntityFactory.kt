package com.supernova.data

import com.supernova.data.entities.*

/**
 * Utility factory for creating data layer entities used in tests.
 *
 * Each helper provides sensible defaults so tests only override the fields
 * they care about.
 */
object TestEntityFactory {
    /** Create a [ProfileEntity] with optional PIN and avatar. */
    fun profile(
        id: Int = 0,
        name: String = "User$id",
        pin: Int? = null,
        avatar: String = "https://example.com/avatar$id.png"
    ) = ProfileEntity(id = id, name = name, pin = pin, avatar = avatar)

    /** Create a [CategoryEntity] instance. */
    fun category(
        type: String = "movie",
        id: Int = 1,
        name: String = "Category$id",
        parentId: Int = 0
    ) = CategoryEntity(type = type, id = id, name = name, parent_id = parentId)

    /**
     * Create a [MovieEntity] with minimal required information.
     * Additional TMDB metadata fields default to `null`.
     */
    fun movie(
        id: Int = 1,
        name: String = "Movie$id",
        streamType: String? = "movie",
        added: Long? = null
    ) = MovieEntity(
        movie_id = id,
        num = id,
        name = name,
        title = null,
        year = null,
        stream_type = streamType,
        stream_icon = null,
        rating = null,
        rating_5based = null,
        added = added,
        container_extension = null,
        custom_sid = null,
        direct_source = null,
        backdrop_path = null,
        poster_path = null,
        overview = null,
        genres = null,
        runtime = null,
        spoken_languages = null
    )

    /** Create a [SeriesEntity] with full metadata fields set to `null`. */
    fun series(
        id: Int = 1,
        name: String = "Series$id"
    ) = SeriesEntity(
        series_id = id,
        num = id,
        name = name,
        title = null,
        year = null,
        stream_type = null,
        cover = null,
        plot = null,
        cast = null,
        director = null,
        genre = null,
        release_date = null,
        releaseDate = null,
        last_modified = null,
        rating = null,
        rating_5based = null,
        backdrop_path = null,
        youtube_trailer = null,
        episode_run_time = null,
        poster_path = null,
        overview = null,
        genres = null,
        first_air_date = null,
        last_air_date = null,
        number_of_seasons = null,
        number_of_episodes = null
    )

    /** Create a [LiveTvEntity] representing a channel. */
    fun liveTv(
        id: Int = 1,
        name: String = "Channel$id",
        epgId: String? = "epg$id",
        archive: Int? = 0
    ) = LiveTvEntity(
        channel_id = id,
        num = id,
        name = name,
        stream_type = "live",
        stream_icon = null,
        epg_channel_id = epgId,
        added = null,
        custom_sid = null,
        tv_archive = archive,
        direct_source = null,
        tv_archive_duration = null,
        category_type = null,
        category_id = null,
        thumbnail = null
    )

    /** Create a [StreamEntity] for search tests. */
    fun stream(id: Int = 1, title: String = "Stream$id") = StreamEntity(
        stream_id = id,
        title = title,
        year = null,
        stream_type = "movie",
        thumbnail_url = null,
        banner_url = null,
        tmdb_id = null,
        media_type = null,
        tmdb_synced_at = null,
        provider_id = null,
        container_extension = null,
        epg_channel_id = null,
        tv_archive = null,
        tv_archive_duration = null,
        direct_source = null,
        custom_sid = null,
        rating = null,
        rating_5based = null,
        added = null,
        plot = null,
        cast = null,
        director = null,
        genre = null
    )

    /** Create a [StreamFts] row for FTS queries. */
    fun streamFts(id: Int, title: String = "Stream$id") = StreamFts(
        stream_id = id,
        title = title,
        plot = null,
        director = null,
        cast = null,
        genre = null
    )

    /** Create an [EpgProgrammeEntity] with dummy timing. */
    fun programme(id: Int = 1, channelId: String = "ch$id", title: String = "Prog$id") =
        EpgProgrammeEntity(
            programmeId = id,
            epgChannelId = channelId,
            startAt = 0L,
            endAt = 1L,
            title = title
        )

    /** Create a [EpgProgrammeFts] row for FTS programme queries. */
    fun programmeFts(id: Int, title: String = "Prog$id") = EpgProgrammeFts(
        title = title,
        subTitle = null,
        description = null,
        rowid = id
    )


    /** Create a [WatchHistoryEntity] entry. */
    fun watchHistory(userId: Int = 1, streamId: Int = 1) = WatchHistoryEntity(
        userId = userId,
        streamId = streamId,
        episodeId = null,
        watchedAt = System.currentTimeMillis(),
        durationSec = null,
        progress = null
    )

    /** Create a [ReactionEntity] for simple like/dislike tests. */
    fun reaction(userId: Int = 1, streamId: Int = 1, type: String = "like") = ReactionEntity(
        userId = userId,
        streamId = streamId,
        reactionType = type
    )
}
