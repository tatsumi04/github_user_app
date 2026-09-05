package com.example.github_user_app.ui.userlist

import com.example.github_user_app.data.model.GitHubUserListItem

sealed interface UserListUiState {
    object Loading : UserListUiState
    data class Success(val users: List<GitHubUserListItem>) : UserListUiState
    data class Error(val message: String) : UserListUiState
}
