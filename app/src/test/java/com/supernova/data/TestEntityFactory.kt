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

    /**
     * Create a [SeriesEntity].
     *
     * All parameters default to `null` except for [id], [num] and [name].
     * This mirrors the latest schema so tests can override any field directly
     * without additional copy calls.
     */
    fun series(
        id: Int = 1,
        num: Int = id,
        name: String = "Series$id",
        title: String? = null,
        year: String? = null,
        streamType: String? = null,
        cover: String? = null,
        plot: String? = null,
        cast: String? = null,
        director: String? = null,
        genre: String? = null,
        release_date: String? = null,
        releaseDate: String? = null,
        last_modified: String? = null,
        rating: String? = null,
        rating_5based: Float? = null,
        backdrop_path: String? = null,
        youtube_trailer: String? = null,
        episode_run_time: String? = null,
        poster_path: String? = null,
        overview: String? = null,
        genres: String? = null,
        first_air_date: String? = null,
        last_air_date: String? = null,
        number_of_seasons: Int? = null,
        number_of_episodes: Int? = null
    ) = SeriesEntity(
        series_id = id,
        num = num,
        name = name,
        title = title,
        year = year,
        stream_type = streamType,
        cover = cover,
        plot = plot,
        cast = cast,
        director = director,
        genre = genre,
        release_date = release_date,
        releaseDate = releaseDate,
        last_modified = last_modified,
        rating = rating,
        rating_5based = rating_5based,
        backdrop_path = backdrop_path,
        youtube_trailer = youtube_trailer,
        episode_run_time = episode_run_time,
        poster_path = poster_path,
        overview = overview,
        genres = genres,
        first_air_date = first_air_date,
        last_air_date = last_air_date,
        number_of_seasons = number_of_seasons,
        number_of_episodes = number_of_episodes
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
        streamId = id,
        title = title,
        year = null,
        streamType = "movie",
        thumbnailUrl = null,
        bannerUrl = null,
        tmdbId = null,
        mediaType = null,
        tmdbSyncedAt = null,
        providerId = null,
        containerExtension = null,
        epgChannelId = null,
        tvArchive = null,
        tvArchiveDuration = null,
        directSource = null,
        customSid = null,
        rating = null,
        rating5Based = null,
        added = null,
        plot = null,
        cast = null,
        director = null,
        genre = null
    )

    /** Create a [StreamFts] row for FTS queries. */
    fun streamFts(id: Int, title: String = "Stream$id") = StreamFts(
        streamId = id,
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
