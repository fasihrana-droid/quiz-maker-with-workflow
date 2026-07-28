package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    TEACHER,
    STUDENT
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "question_banks")
data class QuestionBankEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long,
    val title: String,
    val category: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionBankId: Long = 0, // 0 if directly in quiz
    val quizId: Long = 0,         // 0 if unassigned bank question
    val category: String,
    val questionText: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String,
    val correctAnswerIndex: Int, // 0, 1, 2, or 3
    val explanation: String = ""
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long,
    val title: String,
    val description: String,
    val subject: String,
    val timeLimitMinutes: Int = 15,
    val passingPercentage: Int = 60,
    val isPublished: Boolean = false,
    val shuffleQuestions: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: Long,
    val quizTitle: String,
    val studentId: Long,
    val studentName: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Float,
    val passed: Boolean,
    val timeTakenSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "attempt_answers")
data class AttemptAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attemptId: Long,
    val questionId: Long,
    val questionText: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String,
    val selectedOptionIndex: Int, // -1 if skipped
    val correctOptionIndex: Int,
    val isCorrect: Boolean,
    val explanation: String = ""
)
