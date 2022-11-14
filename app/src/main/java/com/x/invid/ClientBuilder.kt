package com.x.invid

import android.graphics.Bitmap
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.Arrays


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

interface Api {
    @GET("/api/v1/popular?fields=title,videoId,lengthSeconds,viewCount,videoThumbnails,publishedText,author")
    fun get_popular() : Call<List<Video>>

    @GET("/api/v1/trending?fields=title,videoId,lengthSeconds,viewCount,videoThumbnails,publishedText,author")
    fun get_trending() : Call<List<Video>>

    @GET
    fun get_img(@Url url: String) : Call<ResponseBody>
}

class ClientBuilder {
    companion object {
        const val INV_URL: String = "https://vid.puffyan.us/"
    }

    fun get_client() : Api {
        val intercept = object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                println("Outgoing request to ${request.url()}")
                return chain.proceed(request)
            }
        }

        val okHttpClient = OkHttpClient()
            .newBuilder()
            .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
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