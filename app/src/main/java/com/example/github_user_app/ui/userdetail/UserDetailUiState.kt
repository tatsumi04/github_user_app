package com.example.github_user_app.ui.userdetail

import com.example.github_user_app.data.model.GitHubUserDetail

sealed interface UserDetailUiState {
    object Loading : UserDetailUiState
    data class Success(val user: GitHubUserDetail) : UserDetailUiState
    data class Error(val message: String) : UserDetailUiState
}
