package com.example.github_user_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.github_user_app.ui.userdetail.UserDetailScreen
import com.example.github_user_app.ui.userlist.UserListScreen

/**
 * アプリの画面ルーティングパスを定義する Sealed Class
 */
sealed class Screen(val route: String) {
    /** ユーザー一覧画面ルート */
    object UserList : Screen("user_list")

    /** ユーザー詳細画面ルート（引数 login） */
    object UserDetail : Screen("user_detail/{login}") {
        fun createRoute(login: String) = "user_detail/$login"
    }
}

/**
 * Navigation Compose を用いたアプリ全体の画面遷移（ルーティング）コンポーネント。
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.UserList.route,
        modifier = modifier
    ) {
        // 1. 一覧画面
        composable(Screen.UserList.route) {
            UserListScreen(
                onUserClick = { login ->
                    // 詳細画面へ遷移（login 名をパラメータとして渡す）
                    navController.navigate(Screen.UserDetail.createRoute(login))
                }
            )
        }

        // 2. 詳細画面
        composable(
            route = Screen.UserDetail.route,
            arguments = listOf(
                navArgument("login") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val login = backStackEntry.arguments?.getString("login") ?: ""
            UserDetailScreen(
                login = login,
                onBackClick = {
                    // 前の画面へ戻る
                    navController.popBackStack()
                }
            )
        }
    }
}
