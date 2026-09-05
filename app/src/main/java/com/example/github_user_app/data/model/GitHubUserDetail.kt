package com.example.github_user_app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ユーザー詳細画面で利用するユーザー詳細データモデル。
 * 各フィールドは null の場合でもUIがクラッシュしないよう null 許容型で安全に定義されています。
 * 
 * @property id ユーザー固有ID
 * @property login アカウントユーザー名
 * @property avatarUrl アバター画像URL
 * @property name 表示名（氏名・ニックネームなど）
 * @property bio 自己紹介文
 * @property location 所在地
 * @property followers フォロワー数
 * @property following フォロー数
 * @property publicRepos 公開リポジトリ数
 * @property htmlUrl GitHubプロフィールページURL
 * @property createdAt アカウント作成日時 (ISO 8601 形式)
 * @property topLanguages 主要な使用プログラミング言語リスト
 * @property languageUsages 円グラフ表示用の各言語の使用比率リスト
 */
@Serializable
data class GitHubUserDetail(
    @SerialName("id") val id: Long,
    @SerialName("login") val login: String,
    @SerialName("avatar_url") val avatarUrl: String,
    @SerialName("name") val name: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("followers") val followers: Int? = null,
    @SerialName("following") val following: Int? = null,
    @SerialName("public_repos") val publicRepos: Int? = null,
    @SerialName("html_url") val htmlUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val topLanguages: List<String> = emptyList(),
    val languageUsages: List<LanguageUsage> = emptyList()
)
