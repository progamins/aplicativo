package com.example.login.data.remote

import com.example.login.BuildConfig
import com.example.login.data.SessionManager
import com.example.login.data.model.RefreshRequest
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object ApiClient {

    val json = Json { ignoreUnknownKeys = true }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private fun baseClient() = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /** Cliente principal: inyecta el Bearer token y renueva automáticamente en 401. */
    private val okHttp: OkHttpClient by lazy {
        baseClient().newBuilder()
            .addInterceptor(AuthInterceptor())
            .build()
    }

    /** Cliente limpio para renovar el token (evita recursión del interceptor). */
    private val refreshOkHttp: OkHttpClient by lazy { baseClient() }

    private fun retrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    val api: ApiService by lazy { retrofit(okHttp).create(ApiService::class.java) }

    private val refreshApi: RefreshService by lazy {
        retrofit(refreshOkHttp).create(RefreshService::class.java)
    }

    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val token = SessionManager.accessToken
            val authorized = if (!token.isNullOrBlank()) {
                request.newBuilder().header("Authorization", "Bearer $token").build()
            } else {
                request
            }

            val response = chain.proceed(authorized)

            val isAuthRoute = request.url.encodedPath.endsWith("/login") ||
                request.url.encodedPath.endsWith("/register") ||
                request.url.encodedPath.endsWith("/refresh") ||
                request.url.encodedPath.endsWith("/logout")

            if (response.code == 401 && !isAuthRoute && SessionManager.refreshToken != null) {
                response.close()
                if (tryRefresh()) {
                    val retry = request.newBuilder()
                        .header("Authorization", "Bearer ${SessionManager.accessToken}")
                        .build()
                    return chain.proceed(retry)
                }
                SessionManager.clear()
            }
            return response
        }
    }

    @Synchronized
    private fun tryRefresh(): Boolean {
        val refreshToken = SessionManager.refreshToken ?: return false
        return try {
            val response = refreshApi.refresh(RefreshRequest(refreshToken)).execute()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                SessionManager.accessToken = body.accessToken
                SessionManager.refreshToken = body.refreshToken
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
