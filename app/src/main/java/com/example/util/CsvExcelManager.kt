package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.QuestionEntity
import com.example.data.model.QuizAttemptEntity
import java.io.BufferedReader
import java.io.InputStreamReader

object CsvExcelManager {

    /**
     * Header string matching user format
     */
    const val CSV_HEADER = "Category,Question,Option 1,Option 2,Option 3,Option 4,Correct answer,Explanation"

    /**
     * Parses raw CSV or TSV string into list of QuestionEntity
     */
    fun parseCsvQuestions(
        csvText: String,
        targetBankId: Long = 0,
        targetQuizId: Long = 0,
        defaultCategory: String = "General"
    ): List<QuestionEntity> {
        val questions = mutableListOf<QuestionEntity>()
        val lines = csvText.lines()

        var isHeader = true
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            val tokens = parseCsvLine(trimmed)
            if (tokens.isEmpty()) continue

            // Skip header row if present
            if (isHeader && tokens.firstOrNull()?.contains("Category", ignoreCase = true) == true ||
                isHeader && tokens.firstOrNull()?.contains("Question", ignoreCase = true) == true
            ) {
                isHeader = false
                continue
            }
            isHeader = false

            // Expected columns: Category, Question, Option 1, Option 2, Option 3, Option 4, Correct answer, Explanation
            if (tokens.size >= 6) {
                var category = defaultCategory
                var questionText = ""
                var opt1 = ""
                var opt2 = ""
                var opt3 = ""
                var opt4 = ""
                var correctStr = ""
                var explanation = ""

                if (tokens.size >= 8) {
                    category = tokens[0].ifBlank { defaultCategory }
                    questionText = tokens[1]
                    opt1 = tokens[2]
                    opt2 = tokens[3]
                    opt3 = tokens[4]
                    opt4 = tokens[5]
                    correctStr = tokens[6]
                    explanation = tokens[7]
                } else if (tokens.size >= 7) {
                    category = tokens[0].ifBlank { defaultCategory }
                    questionText = tokens[1]
                    opt1 = tokens[2]
                    opt2 = tokens[3]
                    opt3 = tokens[4]
                    opt4 = tokens[5]
                    correctStr = tokens[6]
                } else {
                    // 6 tokens: Question, Option 1, Option 2, Option 3, Option 4, Correct
                    questionText = tokens[0]
                    opt1 = tokens[1]
                    opt2 = tokens[2]
                    opt3 = tokens[3]
                    opt4 = tokens[4]
                    correctStr = tokens[5]
                }

                val correctIndex = parseCorrectAnswerIndex(correctStr, opt1, opt2, opt3, opt4)

                if (questionText.isNotBlank() && opt1.isNotBlank() && opt2.isNotBlank()) {
                    questions.add(
                        QuestionEntity(
                            questionBankId = targetBankId,
                            quizId = targetQuizId,
                            category = category,
                            questionText = questionText,
                            option1 = opt1,
                            option2 = opt2,
                            option3 = opt3.ifBlank { "N/A" },
                            option4 = opt4.ifBlank { "N/A" },
                            correctAnswerIndex = correctIndex,
                            explanation = explanation
                        )
                    )
                }
            }
        }
        return questions
    }

    /**
     * Reads contents from Android File Uri and parses questions
     */
    fun readQuestionsFromUri(
        context: Context,
        uri: Uri,
        targetBankId: Long = 0,
        targetQuizId: Long = 0,
        defaultCategory: String = "Imported"
    ): List<QuestionEntity> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = reader.readText()
                parseCsvQuestions(content, targetBankId, targetQuizId, defaultCategory)
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Export list of questions to CSV string
     */
    fun exportQuestionsToCsv(questions: List<QuestionEntity>): String {
        val sb = StringBuilder()
        sb.append(CSV_HEADER).append("\n")
        for (q in questions) {
            val correctAnsText = when (q.correctAnswerIndex) {
                0 -> q.option1
                1 -> q.option2
                2 -> q.option3
                3 -> q.option4
                else -> "Option ${q.correctAnswerIndex + 1}"
            }
            sb.append(escapeCsv(q.category)).append(",")
                .append(escapeCsv(q.questionText)).append(",")
                .append(escapeCsv(q.option1)).append(",")
                .append(escapeCsv(q.option2)).append(",")
                .append(escapeCsv(q.option3)).append(",")
                .append(escapeCsv(q.option4)).append(",")
                .append(escapeCsv(correctAnsText)).append(",")
                .append(escapeCsv(q.explanation)).append("\n")
        }
        return sb.toString()
    }

    /**
     * Export student performance analytics report to CSV
     */
    fun exportStudentAnalyticsToCsv(attempts: List<QuizAttemptEntity>): String {
        val sb = StringBuilder()
        sb.append("Student Name,Quiz Title,Score,Total Questions,Percentage,Status,Time Taken (sec),Date\n")
        for (att in attempts) {
            val status = if (att.passed) "PASSED" else "FAILED"
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(att.timestamp)
            sb.append(escapeCsv(att.studentName)).append(",")
                .append(escapeCsv(att.quizTitle)).append(",")
                .append(att.score).append(",")
                .append(att.totalQuestions).append(",")
                .append(String.format(java.util.Locale.US, "%.1f%%", att.percentage)).append(",")
                .append(status).append(",")
                .append(att.timeTakenSeconds).append(",")
                .append(escapeCsv(dateStr)).append("\n")
        }
        return sb.toString()
    }

    /**
     * Share CSV string via Android share sheet intent
     */
    fun shareCsvContent(context: Context, title: String, csvContent: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, csvContent)
        }
        context.startActivity(Intent.createChooser(intent, "Export/Share CSV ($title)"))
    }

    private fun parseCorrectAnswerIndex(
        correctStr: String,
        opt1: String,
        opt2: String,
        opt3: String,
        opt4: String
    ): Int {
        val clean = correctStr.trim().lowercase()
        return when {
            clean == "1" || clean == "option 1" || clean == "a" || clean.equals(opt1.trim(), ignoreCase = true) -> 0
            clean == "2" || clean == "option 2" || clean == "b" || clean.equals(opt2.trim(), ignoreCase = true) -> 1
            clean == "3" || clean == "option 3" || clean == "c" || clean.equals(opt3.trim(), ignoreCase = true) -> 2
            clean == "4" || clean == "option 4" || clean == "d" || clean.equals(opt4.trim(), ignoreCase = true) -> 3
            else -> 0
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        // Detect if line uses tabs instead of commas
        val delimiter = if (line.contains("\t") && !line.contains(",")) '\t' else ','

        for (i in line.indices) {
            val c = line[i]
            if (c == '"') {
                inQuotes = !inQuotes
            } else if (c == delimiter && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.clear()
            } else {
                sb.append(c)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    private fun escapeCsv(value: String): String {
        var clean = value.replace("\n", " ").replace("\r", "")
        if (clean.contains(",") || clean.contains("\"") || clean.contains(";")) {
            clean = "\"" + clean.replace("\"", "\"\"") + "\""
        }
        return clean
    }

    fun getSampleCsvTemplate(): String {
        return """Category,Question,Option 1,Option 2,Option 3,Option 4,Correct answer,Explanation
Science,What is the boiling point of water at sea level?,90°C,100°C,110°C,120°C,100°C,Water boils at 100 degrees Celsius under standard atmospheric pressure.
Mathematics,What is 15 multiplied by 8?,100,110,120,130,120,15 * 8 = 120.
History,Who was the first president of the United States?,Thomas Jefferson,George Washington,Abraham Lincoln,John Adams,George Washington,George Washington served as president from 1789 to 1797."""
    }
}
