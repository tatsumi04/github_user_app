package com.example.github_user_app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ユーザー一覧画面で利用するユーザー概要データモデル。
 * 
 * @property id ユーザー固有ID
 * @property login アカウントユーザー名
 * @property avatarUrl アバター画像URL
 * @property htmlUrl プロフィールWebページURL
 * @property topLanguages 主要な使用プログラミング言語（最大2個）
 * @property lastCommitDate 最終コミット日時 (ISO 8601フォーマット文字列)
 */
@Serializable
data class GitHubUserListItem(
    @SerialName("id") val id: Long,
    @SerialName("login") val login: String,
    @SerialName("avatar_url") val avatarUrl: String,
    @SerialName("html_url") val htmlUrl: String? = null,
    val topLanguages: List<String> = emptyList(),
    val lastCommitDate: String? = null
)
