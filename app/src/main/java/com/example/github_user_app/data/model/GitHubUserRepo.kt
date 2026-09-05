package com.example.github_user_app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubUserRepo(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("language") val language: String? = null,
    @SerialName("pushed_at") val pushedAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
