package com.supernova.network.models

import com.google.gson.annotations.SerializedName

//@todo refactor this file to use a common base class or interface for all responses
//@todo make name for vod mandatory


// Category Response - Only category_id and category_name are guaranteed
data class CategoryResponse(
    @SerializedName("category_id")
    val categoryId: String = "",
    @SerializedName("category_name")
    val categoryName: String = "",
    @SerializedName("parent_id")
    val parentId: Int = 0
)

// VOD Response - Only stream_id guaranteed, everything else optional
data class VodResponse(
    @SerializedName("stream_id")
    val streamId: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("num")
    val num: Int? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("year")
    val year: Int? = null,
    @SerializedName("stream_type")
    val streamType: String? = null,
    @SerializedName("stream_icon")
    val streamIcon: String? = null,
    @SerializedName("rating")
    val rating: Float? = null,
    @SerializedName("rating_5based")
    val rating5based: Float? = null,
    @SerializedName("added")
    val added: String? = null,
    @SerializedName("category_id")
    val categoryId: String? = null,
    @SerializedName("category_ids")
    val categoryIds: List<Int>? = null,
    @SerializedName("container_extension")
    val containerExtension: String? = null,
    @SerializedName("custom_sid")
    val customSid: String? = null,
    @SerializedName("direct_source")
    val directSource: String? = null
)

// Series Response - Only series_id guaranteed, everything else optional
data class SeriesResponse(
    @SerializedName("series_id")
    val seriesId: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("num")
    val num: Int? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("year")
    val year: String? = null,
    @SerializedName("stream_type")
    val streamType: String? = null,
    @SerializedName("cover")
    val cover: String? = null,
    @SerializedName("plot")
    val plot: String? = null,
    @SerializedName("cast")
    val cast: String? = null,
    @SerializedName("director")
    val director: String? = null,
    @SerializedName("genre")
    val genre: String? = null,
    @SerializedName("release_date")
    val releaseDate: String? = null,
    @SerializedName("releaseDate")
    val releaseDateAlt: String? = null,
    @SerializedName("last_modified")
    val lastModified: String? = null,
    @SerializedName("rating")
    val rating: String? = null,
    @SerializedName("rating_5based")
    val rating5based: Float? = null,
    @SerializedName("backdrop_path")
    val backdropPath: List<String>? = null,
    @SerializedName("youtube_trailer")
    val youtubeTrailer: String? = null,
    @SerializedName("episode_run_time")
    val episodeRunTime: String? = null,
    @SerializedName("category_id")
    val categoryId: String? = null,
    @SerializedName("category_ids")
    val categoryIds: List<Int>? = null
)

// Live TV Response - Only stream_id guaranteed, everything else optional
data class LiveTvResponse(
    @SerializedName("stream_id")
    val streamId: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("num")
    val num: Int? = null,
    @SerializedName("stream_type")
    val streamType: String? = null,
    @SerializedName("stream_icon")
    val streamIcon: String? = null,
    @SerializedName("epg_channel_id")
    val epgChannelId: String? = null,
    @SerializedName("added")
    val added: String? = null,
    @SerializedName("custom_sid")
    val customSid: String? = null,
    @SerializedName("tv_archive")
    val tvArchive: Int? = null,
    @SerializedName("direct_source")
    val directSource: String? = null,
    @SerializedName("tv_archive_duration")
    val tvArchiveDuration: Int? = null,
    @SerializedName("category_id")
    val categoryId: String? = null,
    @SerializedName("category_ids")
    val categoryIds: List<Int>? = null,
    @SerializedName("thumbnail")
    val thumbnail: String? = null
)