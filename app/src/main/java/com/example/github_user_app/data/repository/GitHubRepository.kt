package com.example.github_user_app.data.repository

import com.example.github_user_app.data.api.GitHubApi
import com.example.github_user_app.data.api.RetrofitClient
import com.example.github_user_app.data.model.GitHubUserDetail
import com.example.github_user_app.data.model.GitHubUserListItem
import com.example.github_user_app.data.model.LanguageUsage
import com.example.github_user_app.data.sampledata.SampleUserData
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * =========================================================================
 * 【テスト用サンプルデータ切り替え設定】
 * 
 * 1. USE_SAMPLE_DATA_ON_API_ERROR = true (デフォルト)
 *    -> API通信がエラー（HTTP 403 レート制限やネットワーク障害など）になった場合、
 *       自動的に sampledata のテスト用サンプルデータを返します。
 * 
 * 2. FORCE_USE_SAMPLE_DATA = true
 *    -> true に設定すると、本番API通信を行わず常時テスト用サンプルデータを返します。
 * 
 * ※ テスト完了後にサンプルデータを削除する際は、
 *    com.example.github_user_app.data.sampledata パッケージを削除し、
 *    本フラグの参照箇所を修正してください。
 * =========================================================================
 */
private const val USE_SAMPLE_DATA_ON_API_ERROR = false
private const val FORCE_USE_SAMPLE_DATA = false

/**
 * GitHub API の通信結果を取得・加工してプレゼンテーション層 (ViewModel) に提供するリポジトリクラス。
 * 責務：API通信の実行、複数エンドポイントからのデータ集約、エラー時のフォールバック処理。
 */
class GitHubRepository(
    private val api: GitHubApi = RetrofitClient.instance
) {
    /**
     * GitHub ユーザー一覧を取得します（ページネーション対応、最大20件）。
     * 各ユーザーの最終コミット日時および主要使用言語（上位2個）を非同期並列で取得して付与し、
     * 最終コミット日時の降順でソートして最新20件を返却します。
     * 
     * @param since このユーザーIDより後を取得（ページネーション用）
     * @param limit 取得件数上限（デフォルト20件）
     * @return ユーザー概要モデルのリスト（最大20件）
     */
    suspend fun getUsers(since: Long? = null, limit: Int = 20): List<GitHubUserListItem> {
        if (FORCE_USE_SAMPLE_DATA) {
            return SampleUserData.getSampleUsers(since, limit)
                .sortedWith(compareByDescending<GitHubUserListItem> { it.lastCommitDate }.thenBy { it.login })
        }

        return try {
            val items = api.getUsers(since = since, perPage = limit).take(limit)
            attachTopLanguagesAndCommitDate(items, limit)
        } catch (e: Exception) {
            if (USE_SAMPLE_DATA_ON_API_ERROR) {
                SampleUserData.getSampleUsers(since, limit)
                    .sortedWith(compareByDescending<GitHubUserListItem> { it.lastCommitDate }.thenBy { it.login })
            } else {
                throw e
            }
        }
    }

    /**
     * 名前と使用言語の検索条件に基づいてユーザーを検索します（ページネーション対応、最大20件）。
     * 最終コミット日時の降順でソートして返却します。
     * 
     * @param nameQuery 検索欄に入力された名前・ユーザー名キーワード
     * @param languages 選択されたプログラミング言語のセット（複数指定可）
     * @param page ページ番号（1始まり）
     * @param limit 取得件数上限（デフォルト20件）
     * @return 検索結果のユーザー概要モデルリスト（最大20件）
     */
    suspend fun searchUsers(
        nameQuery: String,
        languages: Set<String>,
        page: Int = 1,
        limit: Int = 20
    ): List<GitHubUserListItem> {
        if (FORCE_USE_SAMPLE_DATA) {
            return SampleUserData.searchSampleUsers(nameQuery, languages, page, limit)
                .sortedWith(compareByDescending<GitHubUserListItem> { it.lastCommitDate }.thenBy { it.login })
        }

        val cleanName = nameQuery.trim()

        val queryParts = mutableListOf<String>()
        if (cleanName.isNotEmpty()) {
            queryParts.add(cleanName)
        }

        languages
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { lang ->
                queryParts.add("language:$lang")
            }

        if (queryParts.isEmpty()) {
            return getUsers(since = null, limit = limit)
        }

        val query = queryParts.joinToString(" ")

        return try {
            val rawItems = api.searchUsers(query, page = page, perPage = limit).items.take(limit)
            attachTopLanguagesAndCommitDate(rawItems, limit)
        } catch (e: Exception) {
            if (USE_SAMPLE_DATA_ON_API_ERROR) {
                SampleUserData.searchSampleUsers(nameQuery, languages, page, limit)
                    .sortedWith(compareByDescending<GitHubUserListItem> { it.lastCommitDate }.thenBy { it.login })
            } else {
                throw e
            }
        }
    }

    /**
     * 指定されたユーザーの詳細情報と公開リポジトリ一覧を取得し、言語の使用比率 (%) を算出して統合します。
     * 
     * @param login 取得対象のユーザー名
     * @return 言語割合情報を含むユーザー詳細データ
     */
    suspend fun getUser(login: String): GitHubUserDetail {
        if (FORCE_USE_SAMPLE_DATA) {
            return SampleUserData.getSampleUserDetail(login)
        }

        return try {
            val userDetail = api.getUser(login)

            // リポジトリ一覧の取得（エラー時は空リスト）
            val repos = try {
                api.getUserRepos(login)
            } catch (e: Exception) {
                emptyList()
            }

            // 非空のプログラミング言語のみ抽出
            val rawLanguages = repos
                .mapNotNull { it.language }
                .filter { it.isNotBlank() }

            val totalCount = rawLanguages.size.toFloat()

            // 言語ごとの利用件数および全体の割合 (%) を計算
            val languageUsages = if (totalCount > 0f) {
                rawLanguages
                    .groupingBy { it }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .map { (lang, count) ->
                        LanguageUsage(
                            language = lang,
                            count = count,
                            percentage = (count / totalCount) * 100f
                        )
                    }
            } else {
                emptyList()
            }

            val topLanguages = languageUsages.map { it.language }

            userDetail.copy(
                topLanguages = topLanguages,
                languageUsages = languageUsages
            )
        } catch (e: Exception) {
            if (USE_SAMPLE_DATA_ON_API_ERROR) {
                SampleUserData.getSampleUserDetail(login)
            } else {
                throw e
            }
        }
    }

    /**
     * ユーザー一覧の各アイテムに対して、主要使用言語（上位2個）および最終コミット日時を非同期並列で取得・アタッチします。
     * 取得結果は最終コミット日時（lastCommitDate）の降順（最新順）でソートし、最大 limit 件に制限します。
     */
    private suspend fun attachTopLanguagesAndCommitDate(
        users: List<GitHubUserListItem>,
        limit: Int = 20
    ): List<GitHubUserListItem> = coroutineScope {
        val targetUsers = users.take(limit)
        val enrichedUsers = targetUsers.map { user ->
            async {
                try {
                    val repos = api.getUserRepos(user.login, sort = "pushed", direction = "desc", perPage = 20)
                    val topLangs = repos
                        .mapNotNull { it.language }
                        .filter { it.isNotBlank() }
                        .groupingBy { it }
                        .eachCount()
                        .entries
                        .sortedByDescending { it.value }
                        .take(2)
                        .map { it.key }

                    // 最終コミット・プッシュ日時（pushed_at または updated_at）を取得
                    val latestCommitDate = repos.mapNotNull { it.pushedAt ?: it.updatedAt }.maxOrNull()

                    user.copy(
                        topLanguages = topLangs,
                        lastCommitDate = latestCommitDate
                    )
                } catch (e: Exception) {
                    user
                }
            }
        }.awaitAll()

        // 最終コミット日時の降順（最新のコミット順）でソートし、最大 limit 件を返却
        enrichedUsers.sortedWith(
            compareByDescending<GitHubUserListItem> { it.lastCommitDate }
                .thenBy { it.login }
        ).take(limit)
    }
}
