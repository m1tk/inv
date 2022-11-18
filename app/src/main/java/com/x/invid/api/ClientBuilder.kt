package com.x.invid.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


data class Thumbnail(
    @JsonProperty("url") val url: String,
    @JsonProperty("width") val width: Int
)

data class Video(
    @JsonProperty("videoId") val id: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("author") val author: String,
    @JsonProperty("lengthSeconds") val len_secs: Long,
    @JsonProperty("publishedText") val time: String,
    @JsonProperty("viewCount") val views: Long,
    @JsonProperty("videoThumbnails") val img_prev: List<Thumbnail>,
)

data class VideoSource(
    @JsonProperty("url") val url: String,
    @JsonProperty("resolution") val resolution: String,
)

data class VideoInfo(
    @JsonProperty("formatStreams") val streams: List<VideoSource>,
)

interface Api {
    @GET("/api/v1/popular?fields=title,videoId,lengthSeconds,viewCount,videoThumbnails,publishedText,author")
    fun get_popular() : Call<List<Video>>

    @GET("/api/v1/trending?fields=title,videoId,lengthSeconds,viewCount,videoThumbnails,publishedText,author")
    fun get_trending() : Call<List<Video>>

    @GET("/api/v1/search/?fields=title,videoId,lengthSeconds,viewCount,videoThumbnails,publishedText,author")
    fun get_search(@Query("q") query: String) : Call<List<Video>>

    @GET("/api/v1/videos/{video_id}?fields=formatStreams")
    fun get_video(@Path("video_id") video_id: String) : Call<VideoInfo>

}

class ClientBuilder {
    companion object {
        const val INV_URL: String = "https://vid.puffyan.us/"
    }

    fun get_client() : Api {
        val intercept = Interceptor { chain ->
            val request = chain.request()
            println("Outgoing request to ${request.url()}")
            val resp = chain.proceed(request)
            println("Outgoing response status ${resp.code()}")
            resp
        }

        val okHttpClient = OkHttpClient()
            .newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .addInterceptor(intercept)
            .build()

        val mapper = ObjectMapper()
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        var retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(INV_URL)
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .build()

        return retrofit.create(Api::class.java)
    }
}