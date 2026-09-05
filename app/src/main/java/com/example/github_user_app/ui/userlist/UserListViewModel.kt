package com.example.github_user_app.ui.userlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.github_user_app.data.repository.GitHubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * ユーザー一覧画面の UI 状態とユーザー操作イベントを管理する ViewModel。
 * 
 * @property repository ユーザーデータの取得・検索を担うリポジトリ
 */
class UserListViewModel(
    private val repository: GitHubRepository = GitHubRepository()
) : ViewModel() {

    // UI画面の状態（Loading / Success / Error）
    private val _uiState = MutableStateFlow<UserListUiState>(UserListUiState.Loading)
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    // 検索入力：名前・ユーザー名
    private val _nameQuery = MutableStateFlow("")
    val nameQuery: StateFlow<String> = _nameQuery.asStateFlow()

    // 検索選択：複数言語のセット
    private val _selectedLanguages = MutableStateFlow<Set<String>>(emptySet())
    val selectedLanguages: StateFlow<Set<String>> = _selectedLanguages.asStateFlow()

    // 検索入力：自由入力言語テキスト
    private val _customLanguageInput = MutableStateFlow("")
    val customLanguageInput: StateFlow<String> = _customLanguageInput.asStateFlow()

    // モーダル検索シートの表示/非表示フラグ
    private val _isSearchSheetOpen = MutableStateFlow(false)
    val isSearchSheetOpen: StateFlow<Boolean> = _isSearchSheetOpen.asStateFlow()

    // 画面最上部スワイプ更新（Pull-to-Refresh）の読み込み状態
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // リスト最下部スクロール時の追加読み込み（20件追加）状態
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    // さらなる追加読み込みが可能かどうかのフラグ
    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    // 検索APIのページ番号（1始まり）
    private var currentPage = 1

    init {
        loadUsers()
    }

    /**
     * モーダル検索シートを開きます
     */
    fun openSearchSheet() {
        _isSearchSheetOpen.value = true
    }

    /**
     * モーダル検索シートを閉じます
     */
    fun closeSearchSheet() {
        _isSearchSheetOpen.value = false
    }

    /**
     * 名前検索キーワードの更新
     */
    fun onNameQueryChanged(query: String) {
        _nameQuery.value = query
    }

    /**
     * カスタム言語入力テキストの更新
     */
    fun onCustomLanguageInputChanged(input: String) {
        _customLanguageInput.value = input
    }

    /**
     * テキスト入力されたカスタム言語を検索セットに追加
     */
    fun addCustomLanguage() {
        val trimmed = _customLanguageInput.value.trim()
        if (trimmed.isNotEmpty()) {
            _selectedLanguages.value = _selectedLanguages.value + trimmed
            _customLanguageInput.value = ""
        }
    }

    /**
     * 指定された言語の選択状態をトグル（追加/削除）します
     */
    fun toggleLanguage(language: String) {
        val current = _selectedLanguages.value
        if (current.contains(language)) {
            _selectedLanguages.value = current - language
        } else {
            _selectedLanguages.value = current + language
        }
    }

    /**
     * 設定された検索条件でユーザーを検索します（初期20件取得）。検索実行後にモーダルシートを閉じます。
     */
    fun search() {
        viewModelScope.launch {
            _isSearchSheetOpen.value = false
            _uiState.value = UserListUiState.Loading
            currentPage = 1
            _hasMore.value = true

            try {
                val users = fetchInitialPage()
                _hasMore.value = users.size >= 20
                _uiState.value = UserListUiState.Success(users)
            } catch (e: HttpException) {
                handleHttpError(e)
            } catch (e: Exception) {
                _uiState.value = UserListUiState.Error(
                    e.localizedMessage ?: "ユーザーの取得に失敗しました"
                )
            }
        }
    }

    /**
     * 画面最上部をスワイプした際の更新処理（Pull-to-Refresh）。既存データを最新20件に更新します。
     */
    fun refresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            currentPage = 1
            _hasMore.value = true

            try {
                val users = fetchInitialPage()
                _hasMore.value = users.size >= 20
                _uiState.value = UserListUiState.Success(users)
            } catch (e: Exception) {
                // スワイプ更新失敗時は既存の画面を維持
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * リスト最下部に達した際に追加で20件ずつユーザーを取得します。
     */
    fun loadMore() {
        if (_isLoadingMore.value || _isRefreshing.value || !_hasMore.value) return
        val currentUsers = (_uiState.value as? UserListUiState.Success)?.users ?: return

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val isSearchActive = _nameQuery.value.isNotBlank() || _selectedLanguages.value.isNotEmpty()
                val newUsers = if (isSearchActive) {
                    val nextPage = currentPage + 1
                    val fetched = repository.searchUsers(
                        nameQuery = _nameQuery.value,
                        languages = _selectedLanguages.value,
                        page = nextPage,
                        limit = 20
                    )
                    if (fetched.isNotEmpty()) currentPage = nextPage
                    fetched
                } else {
                    val lastUserId = currentUsers.lastOrNull()?.id
                    repository.getUsers(since = lastUserId, limit = 20)
                }

                if (newUsers.size < 20) {
                    _hasMore.value = false
                }

                val existingIds = currentUsers.map { it.id }.toSet()
                val uniqueNewUsers = newUsers.filterNot { existingIds.contains(it.id) }

                if (uniqueNewUsers.isNotEmpty()) {
                    _uiState.value = UserListUiState.Success(currentUsers + uniqueNewUsers)
                }
            } catch (e: Exception) {
                // 追加読み込みエラー時の無駄な連続リクエストを防止
                _hasMore.value = false
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * 検索条件（名前・選択言語）をすべてクリアし、初期一覧を再取得します
     */
    fun clearSearch() {
        _nameQuery.value = ""
        _selectedLanguages.value = emptySet()
        _customLanguageInput.value = ""
        search()
    }

    /**
     * ユーザー一覧をロードします
     */
    fun loadUsers() {
        search()
    }

    private suspend fun fetchInitialPage() = if (_nameQuery.value.isBlank() && _selectedLanguages.value.isEmpty()) {
        repository.getUsers(since = null, limit = 20)
    } else {
        repository.searchUsers(
            nameQuery = _nameQuery.value,
            languages = _selectedLanguages.value,
            page = 1,
            limit = 20
        )
    }

    private fun handleHttpError(e: HttpException) {
        if (e.code() == 403) {
            _uiState.value = UserListUiState.Error(
                "GitHub APIの利用制限（HTTP 403 レートリミット: 1時間あたり60回）に達しました。しばらく時間を置いてから再試行してください。"
            )
        } else {
            _uiState.value = UserListUiState.Error(
                "通信エラーが発生しました (HTTP ${e.code()})"
            )
        }
    }
}
