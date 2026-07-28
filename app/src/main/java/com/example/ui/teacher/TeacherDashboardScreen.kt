package com.example.ui.teacher

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionBankEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.QuizEntity
import com.example.ui.common.EmptyStateCard
import com.example.ui.common.MetricStatCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.TertiaryEmerald
import com.example.util.CsvExcelManager
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TeacherDashboardScreen(
    quizzes: List<QuizEntity>,
    banks: List<QuestionBankEntity>,
    attempts: List<QuizAttemptEntity>,
    onViewAttemptResult: (Long) -> Unit
) {
    val context = LocalContext.current

    val totalAttempts = attempts.size
    val avgScorePct = if (totalAttempts > 0) attempts.map { it.percentage }.average().toFloat() else 0f
    val passedAttempts = attempts.count { it.passed }
    val passRatePct = if (totalAttempts > 0) (passedAttempts.toFloat() / totalAttempts) * 100f else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header & Export Analytics Report
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryIndigo),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Teacher Overview & Analytics",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Track student attempts and class performance in real-time.",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Button(
                            onClick = {
                                val csv = CsvExcelManager.exportStudentAnalyticsToCsv(attempts)
                                CsvExcelManager.shareCsvContent(context, "Student Performance Report", csv)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                            modifier = Modifier.testTag("export_analytics_csv_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Export Report", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Report", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Key Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Total Quizzes",
                    value = quizzes.size.toString(),
                    subtitle = "${quizzes.count { it.isPublished }} Published",
                    icon = Icons.Default.Assignment,
                    accentColor = PrimaryIndigo,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Question Banks",
                    value = banks.size.toString(),
                    subtitle = "Central Bank",
                    icon = Icons.Default.FolderSpecial,
                    accentColor = SecondaryTeal,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Avg Class Score",
                    value = String.format(Locale.US, "%.1f%%", avgScorePct),
                    subtitle = if (avgScorePct >= 60f) "Good Progress" else "Needs Attention",
                    icon = Icons.Default.Analytics,
                    accentColor = TertiaryEmerald,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Pass Rate",
                    value = String.format(Locale.US, "%.1f%%", passRatePct),
                    subtitle = "$passedAttempts / $totalAttempts Passed",
                    icon = Icons.Default.CheckCircle,
                    accentColor = AccentAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Student Tracking & Recent Submissions Section
        item {
            Text(
                text = "Real-time Student Submissions (${attempts.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (attempts.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.People,
                    title = "No Student Attempts Yet",
                    description = "When students complete tests in the Student Portal, their scores and detailed question breakdown will appear here in real time."
                )
            }
        } else {
            items(attempts) { att ->
                StudentAttemptTrackerItem(
                    attempt = att,
                    onClick = { onViewAttemptResult(att.id) }
                )
            }
        }
    }
}

@Composable
fun StudentAttemptTrackerItem(
    attempt: QuizAttemptEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("attempt_item_${attempt.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background((if (attempt.passed) TertiaryEmerald else MaterialTheme.colorScheme.error).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (attempt.passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (attempt.passed) TertiaryEmerald else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = attempt.studentName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = attempt.quizTitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = (if (attempt.passed) TertiaryEmerald else MaterialTheme.colorScheme.error).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (attempt.passed) "PASSED" else "FAILED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (attempt.passed) TertiaryEmerald else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Score: ${attempt.score}/${attempt.totalQuestions} (${String.format(Locale.US, "%.1f%%", attempt.percentage)})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val formattedDate = try {
                    SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(attempt.timestamp)
                } catch (e: Exception) { "" }

                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (attempt.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (attempt.passed) TertiaryEmerald else MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        }
    }
}
