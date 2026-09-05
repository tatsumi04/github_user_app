package com.example.github_user_app.ui.userdetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.github_user_app.data.model.LanguageUsage
import kotlin.math.abs

/**
 * ユーザーが使用しているプログラミング言語の割合 (%) を円グラフ（ドーナツチャート）および凡例で表示する Composable。
 * Compose の `Canvas` (`drawArc`) を使用して描画します。
 * 
 * @param usages 各プログラミング言語の使用割合データリスト
 */
@Composable
fun LanguagePieChart(
    usages: List<LanguageUsage>,
    modifier: Modifier = Modifier
) {
    if (usages.isEmpty()) return

    // 言語ごとに識別カラーを取得
    val colors = usages.map { getLanguageColor(it.language) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "使用言語の割合",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ドーナツ円グラフの描画エリア (Canvas)
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(120.dp)) {
                        var startAngle = -90f // 真上（12時方向）から開始
                        val strokeWidth = 28.dp.toPx()

                        usages.forEachIndexed { index, usage ->
                            val sweepAngle = (usage.percentage / 100f) * 360f
                            drawArc(
                                color = colors[index],
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )
                            startAngle += sweepAngle
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 右側の凡例リスト（カラーポイント、言語名、割合、リポジトリ数）
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    usages.forEachIndexed { index, usage ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 言語カラーインジケータ
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(colors[index])
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = usage.language,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val formattedPercentage = "%.1f".format(usage.percentage)
                                Text(
                                    text = "$formattedPercentage% (${usage.count}リポジトリ)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * プログラミング言語名に応じた固定テーマカラーを決定します。
 * 定義外の言語については、文字列ハッシュからHSL色空間でユニークなカラーを生成します。
 */
private fun getLanguageColor(language: String): Color {
    return when (language.lowercase()) {
        "kotlin" -> Color(0xFF7F52FF)
        "swift" -> Color(0xFFF05138)
        "python" -> Color(0xFF3572A5)
        "typescript" -> Color(0xFF3178C6)
        "javascript" -> Color(0xFFF1E05A)
        "java" -> Color(0xFFB07219)
        "go" -> Color(0xFF00ADD8)
        "c++", "cpp" -> Color(0xFFF34B7D)
        "rust" -> Color(0xFFDEA584)
        "ruby" -> Color(0xFF701516)
        "php" -> Color(0xFF4F5D95)
        "html" -> Color(0xFFE34C26)
        "css" -> Color(0xFF563D7C)
        "c#" -> Color(0xFF178600)
        "shell" -> Color(0xFF89E051)
        else -> {
            val hash = abs(language.hashCode())
            val hue = (hash % 360).toFloat()
            Color.hsl(hue = hue, saturation = 0.65f, lightness = 0.55f)
        }
    }
}
