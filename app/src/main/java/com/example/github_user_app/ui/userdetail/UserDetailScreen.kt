package com.example.github_user_app.ui.userdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.github_user_app.data.model.GitHubUserDetail

/**
 * ユーザー詳細情報を表示するメイン画面 Composable。
 * 
 * @param login ユーザーのアカウント識別名
 * @param onBackClick 前の画面に戻るナビゲーション処理
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    login: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserDetailViewModel = viewModel(factory = UserDetailViewModel.Factory(login))
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ユーザー詳細") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is UserDetailUiState.Loading -> {
                    // ロード中のインジケータ
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UserDetailUiState.Error -> {
                    // 通信失敗時のエラー画面
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
                        Button(onClick = { viewModel.loadUserDetail() }) {
                            Text("再試行")
                        }
                    }
                }

                is UserDetailUiState.Success -> {
                    // 取得成功時のコンテンツ表示
                    UserDetailContent(
                        user = state.user,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * ユーザー詳細情報の成功時コンテンツ。
 * アバター画像、氏名、アカウント名、統計情報（フォロワー/フォロー/リポジトリ）、プロフィール詳細カード、言語割合円グラフを表示。
 */
@Composable
private fun UserDetailContent(
    user: GitHubUserDetail,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 大型アバター画像
        Image(
            painter = rememberAsyncImagePainter(model = user.avatarUrl),
            contentDescription = "${user.login}のアバター画像",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 表示名（存在する場合のみ表示）
        if (!user.name.isNullOrBlank()) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // アカウントユーザー名 (@login)
        Text(
            text = "@${user.login}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 統計カード (フォロワー / フォロー中 / リポジトリ数)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "フォロワー", value = user.followers?.toString() ?: "0")
                StatItem(label = "フォロー中", value = user.following?.toString() ?: "0")
                StatItem(label = "リポジトリ", value = user.publicRepos?.toString() ?: "0")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 自己紹介カード
        if (!user.bio.isNullOrBlank()) {
            InfoCard(title = "自己紹介", content = user.bio)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 所在地カード
        if (!user.location.isNullOrBlank()) {
            InfoCard(title = "所在地", content = user.location)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // GitHub プロフィール URL カード
        if (!user.htmlUrl.isNullOrBlank()) {
            InfoCard(title = "GitHub プロフィール", content = user.htmlUrl)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // アカウント作成日カード
        if (!user.createdAt.isNullOrBlank()) {
            val formattedDate = formatDate(user.createdAt)
            InfoCard(title = "アカウント作成日", content = formattedDate)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 使用言語の割合（円グラフ＋凡例）
        if (user.languageUsages.isNotEmpty()) {
            LanguagePieChart(usages = user.languageUsages)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 統計数値とラベルを表示する小型要素
 */
@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
        )
    }
}

/**
 * プロフィール情報の各項目（タイトル・本文）を美しく囲むカード Composable
 */
@Composable
private fun InfoCard(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * ISO 8601 形式の日付文字列 (例: "2011-01-25T18:44:36Z") を "YYYY年MM月DD日" に整形
 */
private fun formatDate(isoString: String): String {
    return try {
        val datePart = if (isoString.contains("T")) isoString.substringBefore("T") else isoString
        val parts = datePart.split("-")
        if (parts.size == 3) {
            "${parts[0]}年${parts[1]}月${parts[2]}日"
        } else {
            datePart
        }
    } catch (e: Exception) {
        isoString
    }
}
