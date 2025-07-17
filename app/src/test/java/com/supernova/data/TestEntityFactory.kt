package com.supernova.data

import com.supernova.data.entities.*

object TestEntityFactory {
    fun profile(
        id: Int = 0,
        name: String = "User$id",
        pin: Int? = null,
        avatar: String = "https://example.com/avatar$id.png"
    ) = ProfileEntity(id = id, name = name, pin = pin, avatar = avatar)

    fun category(
        type: String = "movie",
        id: Int = 1,
        name: String = "Category$id",
        parentId: Int = 0
    ) = CategoryEntity(type = type, id = id, name = name, parent_id = parentId)

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
}
