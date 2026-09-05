package com.example.github_user_app.data.sampledata

import com.example.github_user_app.data.model.GitHubUserDetail
import com.example.github_user_app.data.model.GitHubUserListItem
import com.example.github_user_app.data.model.LanguageUsage

/**
 * =========================================================================
 * 【テスト用サンプルデータ】
 * API通信が利用できない場合（HTTP 403 レートリミット発生時やオフライン時など）に
 * 使用されるダミーデータです。
 * 
 * 最終的に削除する場合は、この sampledata パッケージごと削除してください。
 * =========================================================================
 */
object SampleUserData {

    val sampleUsers = listOf(
        GitHubUserListItem(
            id = 1,
            login = "torvalds",
            avatarUrl = "https://avatars.githubusercontent.com/u/10240?v=4",
            htmlUrl = "https://github.com/torvalds",
            topLanguages = listOf("C", "Assembly"),
            lastCommitDate = "2026-09-04T18:30:00Z"
        ),
        GitHubUserListItem(
            id = 2,
            login = "jakewharton",
            avatarUrl = "https://avatars.githubusercontent.com/u/66577?v=4",
            htmlUrl = "https://github.com/jakewharton",
            topLanguages = listOf("Kotlin", "Java"),
            lastCommitDate = "2026-09-03T14:15:00Z"
        ),
        GitHubUserListItem(
            id = 3,
            login = "kotlin-lover",
            avatarUrl = "https://avatars.githubusercontent.com/u/1446536?v=4",
            htmlUrl = "https://github.com/kotlin-lover",
            topLanguages = listOf("Kotlin", "Android"),
            lastCommitDate = "2026-09-02T09:45:00Z"
        ),
        GitHubUserListItem(
            id = 4,
            login = "swift-developer",
            avatarUrl = "https://avatars.githubusercontent.com/u/10682057?v=4",
            htmlUrl = "https://github.com/swift-developer",
            topLanguages = listOf("Swift", "Objective-C"),
            lastCommitDate = "2026-09-01T11:20:00Z"
        ),
        GitHubUserListItem(
            id = 5,
            login = "web-creator",
            avatarUrl = "https://avatars.githubusercontent.com/u/4403656?v=4",
            htmlUrl = "https://github.com/web-creator",
            topLanguages = listOf("TypeScript", "JavaScript"),
            lastCommitDate = "2026-08-30T16:00:00Z"
        ),
        GitHubUserListItem(
            id = 6,
            login = "python-expert",
            avatarUrl = "https://avatars.githubusercontent.com/u/150330?v=4",
            htmlUrl = "https://github.com/python-expert",
            topLanguages = listOf("Python", "C++"),
            lastCommitDate = "2026-08-28T08:10:00Z"
        )
    )

    fun getSampleUsers(since: Long? = null, limit: Int = 20): List<GitHubUserListItem> {
        val filtered = if (since != null) sampleUsers.filter { it.id > since } else sampleUsers
        return filtered.take(limit)
    }

    fun searchSampleUsers(
        nameQuery: String,
        languages: Set<String>,
        page: Int = 1,
        limit: Int = 20
    ): List<GitHubUserListItem> {
        val cleanName = nameQuery.trim().lowercase()
        val filtered = sampleUsers.filter { user ->
            val matchesName = cleanName.isEmpty() || user.login.lowercase().contains(cleanName)
            val matchesLang = languages.isEmpty() || user.topLanguages.any { userLang ->
                languages.any { reqLang -> reqLang.equals(userLang, ignoreCase = true) }
            }
            matchesName && matchesLang
        }
        val skip = (page - 1) * limit
        return filtered.drop(skip).take(limit)
    }

    fun getSampleUserDetail(login: String): GitHubUserDetail {
        return when (login.lowercase()) {
            "torvalds" -> GitHubUserDetail(
                id = 1,
                login = "torvalds",
                avatarUrl = "https://avatars.githubusercontent.com/u/10240?v=4",
                name = "Linus Torvalds",
                bio = "Creator of Linux and Git. Software engineer at Linux Foundation.",
                location = "Portland, OR",
                followers = 210000,
                following = 0,
                publicRepos = 7,
                htmlUrl = "https://github.com/torvalds",
                createdAt = "2011-09-03T15:26:22Z",
                topLanguages = listOf("C", "Assembly", "Makefile", "Perl"),
                languageUsages = listOf(
                    LanguageUsage(language = "C", count = 5, percentage = 71.4f),
                    LanguageUsage(language = "Assembly", count = 1, percentage = 14.3f),
                    LanguageUsage(language = "Makefile", count = 1, percentage = 14.3f)
                )
            )

            "jakewharton" -> GitHubUserDetail(
                id = 2,
                login = "jakewharton",
                avatarUrl = "https://avatars.githubusercontent.com/u/66577?v=4",
                name = "Jake Wharton",
                bio = "Android & Kotlin open source developer.",
                location = "Pittsburgh, PA",
                followers = 65000,
                following = 12,
                publicRepos = 124,
                htmlUrl = "https://github.com/jakewharton",
                createdAt = "2009-03-24T14:11:00Z",
                topLanguages = listOf("Kotlin", "Java", "Groovy", "Shell"),
                languageUsages = listOf(
                    LanguageUsage(language = "Kotlin", count = 75, percentage = 60.5f),
                    LanguageUsage(language = "Java", count = 35, percentage = 28.2f),
                    LanguageUsage(language = "Groovy", count = 10, percentage = 8.1f),
                    LanguageUsage(language = "Shell", count = 4, percentage = 3.2f)
                )
            )

            else -> GitHubUserDetail(
                id = 99,
                login = login,
                avatarUrl = "https://avatars.githubusercontent.com/u/10240?v=4",
                name = "$login (サンプルユーザー)",
                bio = "こちらはAPI通信ができない場合に表示されるテスト用サンプルデータです。",
                location = "Tokyo, Japan",
                followers = 1280,
                following = 350,
                publicRepos = 42,
                htmlUrl = "https://github.com/$login",
                createdAt = "2020-01-15T09:00:00Z",
                topLanguages = listOf("Kotlin", "Swift", "Python", "TypeScript"),
                languageUsages = listOf(
                    LanguageUsage(language = "Kotlin", count = 20, percentage = 47.6f),
                    LanguageUsage(language = "Swift", count = 12, percentage = 28.6f),
                    LanguageUsage(language = "Python", count = 6, percentage = 14.3f),
                    LanguageUsage(language = "TypeScript", count = 4, percentage = 9.5f)
                )
            )
        }
    }
}
