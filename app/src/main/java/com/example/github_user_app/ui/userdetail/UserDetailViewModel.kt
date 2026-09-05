package com.example.github_user_app.ui.userdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.github_user_app.data.repository.GitHubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * ユーザー詳細画面の UI 状態と非同期データ取得を管理する ViewModel。
 * 
 * @property login 取得対象ユーザーのアカウント名
 * @property repository データ通信を担うリポジトリ
 */
class UserDetailViewModel(
    private val login: String,
    private val repository: GitHubRepository = GitHubRepository()
) : ViewModel() {

    // UI画面の状態 (Loading / Success / Error)
    private val _uiState = MutableStateFlow<UserDetailUiState>(UserDetailUiState.Loading)
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    init {
        loadUserDetail()
    }

    /**
     * 指定されたユーザーの詳細情報およびリポジトリ言語情報を取得します
     */
    fun loadUserDetail() {
        viewModelScope.launch {
            _uiState.value = UserDetailUiState.Loading
            try {
                val user = repository.getUser(login)
                _uiState.value = UserDetailUiState.Success(user)
            } catch (e: HttpException) {
                if (e.code() == 403) {
                    _uiState.value = UserDetailUiState.Error(
                        "GitHub APIの利用制限（HTTP 403 レートリミット: 1時間あたり60回）に達しました。しばらく時間を置いてから再試行してください。"
                    )
                } else {
                    _uiState.value = UserDetailUiState.Error(
                        "通信エラーが発生しました (HTTP ${e.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = UserDetailUiState.Error(
                    e.localizedMessage ?: "ユーザー詳細の取得に失敗しました"
                )
            }
        }
    }

    /**
     * ViewModel に引数 (login) を安全に渡すための ViewModelProvider.Factory
     */
    class Factory(
        private val login: String,
        private val repository: GitHubRepository = GitHubRepository()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserDetailViewModel(login, repository) as T
        }
    }
}
