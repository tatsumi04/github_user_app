package com.example.github_user_app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubUserSearchResponse(
    @SerialName("total_count") val totalCount: Int = 0,
    @SerialName("incomplete_results") val incompleteResults: Boolean = false,
    @SerialName("items") val items: List<GitHubUserListItem> = emptyList()
)
