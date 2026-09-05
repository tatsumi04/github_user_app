package com.example.github_user_app.ui.userlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * ユーザー一覧画面を表示するメイン Composable。
 * モーダル展開型の検索フォームおよび LazyColumn によるユーザーカード表示を担当します。
 * 
 * @param onUserClick ユーザータップ時に詳細画面へ遷移するためのコールバック
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserListScreen(
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val nameQuery by viewModel.nameQuery.collectAsState()
    val selectedLanguages by viewModel.selectedLanguages.collectAsState()
    val customLanguageInput by viewModel.customLanguageInput.collectAsState()
    val isSearchSheetOpen by viewModel.isSearchSheetOpen.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    // リスト最下部近く（残り2件以下）に達したか検知して自動で追加20件読み込み
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            totalItemsNumber > 0 && lastVisibleItemIndex >= totalItemsNumber - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMore()
        }
    }

    // クイック選択用の主要プログラミング言語リスト
    val popularLanguages = listOf("Kotlin", "Swift", "TypeScript", "Python", "Java", "Go", "Rust", "C++", "JavaScript")

    // 現在何らかの検索フィルタが適用されているか確認
    val hasActiveFilter = nameQuery.isNotBlank() || selectedLanguages.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ユーザー一覧") },
                actions = {
                    // 右上の検索アイコンボタン（タップでモーダルシートが開く）
                    IconButton(onClick = { viewModel.openSearchSheet() }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "検索フォームを開く"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // アクティブな検索条件が存在する場合のバッジ表示バー
            if (hasActiveFilter) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { viewModel.openSearchSheet() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "現在の検索条件:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            if (nameQuery.isNotBlank()) {
                                Text(
                                    text = "名前: $nameQuery",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            if (selectedLanguages.isNotEmpty()) {
                                Text(
                                    text = "言語: ${selectedLanguages.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        // 条件クリアボタン
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "条件をクリア",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // メインコンテンツエリア（スワイプ更新 PullToRefreshBox / 追加読み込み付き LazyColumn / エラー画面）
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = uiState) {
                        is UserListUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        is UserListUiState.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                                    .align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "エラーが発生しました: ${state.message}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.loadUsers() }) {
                                    Text("再試行")
                                }
                            }
                        }

                        is UserListUiState.Success -> {
                            if (state.users.isEmpty()) {
                                Text(
                                    text = "該当するユーザーが見つかりませんでした。",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(24.dp),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                // ユーザー一覧を LazyColumn でスクロール表示（無限スクロール・追加読み込み対応）
                                LazyColumn(
                                    state = listState,
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(
                                        items = state.users,
                                        key = { user -> user.id }
                                    ) { user ->
                                        UserListItem(
                                            user = user,
                                            onUserClick = onUserClick
                                        )
                                    }

                                    // リスト最下部スクロール時の追加読み込みインジケーター
                                    if (isLoadingMore) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // モーダル展開される検索ボトムシート (ModalBottomSheet)
        if (isSearchSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeSearchSheet() },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "ユーザーを検索",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 名前・ユーザー名入力フィールド
                    OutlinedTextField(
                        value = nameQuery,
                        onValueChange = { viewModel.onNameQueryChanged(it) },
                        label = { Text("名前・ユーザー名") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            keyboardController?.hide()
                            viewModel.search()
                        })
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "使用言語（複数選択可）",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 人気言語のマルチセレクトチップ
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        popularLanguages.forEach { lang ->
                            val isSelected = selectedLanguages.contains(lang)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.toggleLanguage(lang) },
                                label = { Text(lang) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // カスタム言語追加フィールド
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customLanguageInput,
                            onValueChange = { viewModel.onCustomLanguageInputChanged(it) },
                            label = { Text("その他の言語を追加") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                viewModel.addCustomLanguage()
                            })
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = { viewModel.addCustomLanguage() }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "言語を追加"
                            )
                        }
                    }

                    // ユーザーが手動追加した追加言語チップ
                    val customSelectedLangs = selectedLanguages.filter { !popularLanguages.contains(it) }
                    if (customSelectedLangs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            customSelectedLangs.forEach { lang ->
                                InputChip(
                                    selected = true,
                                    onClick = { viewModel.toggleLanguage(lang) },
                                    label = { Text(lang) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "削除",
                                            modifier = Modifier.padding(start = 2.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 検索・クリアアクションボタン
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.clearSearch()
                            }
                        ) {
                            Text("クリア")
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.search()
                            }
                        ) {
                            Text("この条件で検索")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
