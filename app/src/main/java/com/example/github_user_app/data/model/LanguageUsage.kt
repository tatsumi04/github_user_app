package com.example.github_user_app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LanguageUsage(
    val language: String,
    val count: Int,
    val percentage: Float
)
