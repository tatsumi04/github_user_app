package com.example.github_user_app.data.api

import com.example.github_user_app.data.model.GitHubUserDetail
import com.example.github_user_app.data.model.GitHubUserListItem
import com.example.github_user_app.data.model.GitHubUserRepo
import com.example.github_user_app.data.model.GitHubUserSearchResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * GitHub REST API との HTTP 通信を担当する Retrofit インターフェース。
 * 責務：エンドポイントの定義およびリクエストパラメータの設定。
 */
interface GitHubApi {
    
    /**
     * GitHub ユーザーの一覧を取得します (`GET /users`)
     * 
     * @param since このIDより大きいユーザーを取得（ページネーション用）
     * @param perPage 取得件数上限（デフォルト: 20件）
     */
    @GET("users")
    suspend fun getUsers(
        @Query("since") since: Long? = null,
        @Query("per_page") perPage: Int = 20
    ): List<GitHubUserListItem>

    /**
     * 特定の GitHub ユーザーの最新詳細情報を取得します (`GET /users/{login}`)
     * 
     * @param login ユーザーのアカウント識別名
     */
    @GET("users/{login}")
    suspend fun getUser(@Path("login") login: String): GitHubUserDetail

    /**
     * ユーザーを検索条件（名前や使用言語）で検索します (`GET /search/users?q={query}`)
     * 
     * @param query GitHub Search APIの検索クエリ（例: "torvalds language:Kotlin"）
     * @param page ページ番号（1始まり）
     * @param perPage 取得件数上限（デフォルト: 20件）
     */
    @GET("search/users")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): GitHubUserSearchResponse

    /**
     * ユーザーの公開リポジトリ一覧を最終コミット日時順で取得します (`GET /users/{login}/repos`)
     * 
     * @param login ユーザーのアカウント識別名
     * @param sort ソート項目（デフォルト: "pushed" - 最終コミット・プッシュ日時）
     * @param direction 昇順/降順（デフォルト: "desc" - 降順）
     * @param perPage 取得件数上限（デフォルト: 20件）
     */
    @GET("users/{login}/repos")
    suspend fun getUserRepos(
        @Path("login") login: String,
        @Query("sort") sort: String = "pushed",
        @Query("direction") direction: String = "desc",
        @Query("per_page") perPage: Int = 20
    ): List<GitHubUserRepo>
}

/**
 * Retrofit および OkHttp のシングルトンインスタンスを提供するクライアントオブジェクト。
 */
object RetrofitClient {
    private const val BASE_URL = "https://api.github.com/"

    // JSONデシリアライズ設定（定義外キーの無視、デフォルト値補完）
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // OkHttp クライアントの設定
    private val okHttpClient = OkHttpClient.Builder()
        // 必須ヘッダーの自動付与（User-Agent がない場合 GitHub API から HTTP 403 で拒否されるのを防止）
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "GitHubUserApp-Android")
                .header("Accept", "application/vnd.github.v3+json")
                .build()
            chain.proceed(request)
        }
        // HTTP 通信ログの出力インターセプター
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    /**
     * アプリ全体で共有される GitHubApi のシングルトンインスタンス
     */
    val instance: GitHubApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GitHubApi::class.java)
    }
}
