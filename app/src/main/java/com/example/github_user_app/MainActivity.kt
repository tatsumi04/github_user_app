package com.example.github_user_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.github_user_app.navigation.AppNavigation
import com.example.github_user_app.ui.theme.GitHub_user_appTheme

/**
 * アプリのエントリーポイントとなるメインアクティビティ。
 * Jetpack Compose を使用して画面UIを初期化し、ナビゲーションを展開します。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 端から端までの画面表示（Edge-to-Edge）を有効化
        enableEdgeToEdge()
        
        setContent {
            // アプリ共通テーマ（Material 3）を適用
            GitHub_user_appTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 全画面のルーティングとナビゲーションを表示
                    AppNavigation()
                }
            }
        }
    }
}