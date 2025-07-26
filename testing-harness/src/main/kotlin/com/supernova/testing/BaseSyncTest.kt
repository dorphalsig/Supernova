package com.supernova.testing

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

abstract class BaseSyncTest : JsonFixtureLoader() {
    protected lateinit var server: MockWebServer
    protected lateinit var retrofit: Retrofit
    protected lateinit var okHttpClient: OkHttpClient

    @BeforeEach
    open fun setUp() {
        server = MockWebServer()
        server.start()
        okHttpClient = OkHttpClient.Builder().build()
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @AfterEach
    open fun tearDown() {
        server.shutdown()
    }

    protected fun enqueueJsonResponse(fixture: String, code: Int = 200, headers: Map<String, String> = emptyMap()) {
        val response = MockResponse()
            .setResponseCode(code)
            .setBody(fixture)
        headers.forEach { (k, v) -> response.addHeader(k, v) }
        server.enqueue(response)
    }

    protected fun enqueueNetworkFailure() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
    }

    protected fun assertRequest(path: String, method: String = "GET") {
        val request = server.takeRequest()
        assertEquals(path, request.path)
        assertEquals(method, request.method)
    }
}
