package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttemptAnswerEntity
import com.example.data.model.QuestionBankEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.QuizEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role ORDER BY name ASC")
    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface QuestionBankDao {
    @Query("SELECT * FROM question_banks WHERE teacherId = :teacherId ORDER BY createdAt DESC")
    fun getQuestionBanksByTeacher(teacherId: Long): Flow<List<QuestionBankEntity>>

    @Query("SELECT * FROM question_banks ORDER BY createdAt DESC")
    fun getAllQuestionBanks(): Flow<List<QuestionBankEntity>>

    @Query("SELECT * FROM question_banks WHERE id = :bankId LIMIT 1")
    suspend fun getQuestionBankById(bankId: Long): QuestionBankEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionBank(bank: QuestionBankEntity): Long

    @Update
    suspend fun updateQuestionBank(bank: QuestionBankEntity)

    @Delete
    suspend fun deleteQuestionBank(bank: QuestionBankEntity)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE questionBankId = :bankId ORDER BY id ASC")
    fun getQuestionsByBankId(bankId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE questionBankId = :bankId ORDER BY id ASC")
    suspend fun getQuestionsByBankIdSync(bankId: Long): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE quizId = :quizId ORDER BY id ASC")
    fun getQuestionsByQuizId(quizId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE quizId = :quizId ORDER BY id ASC")
    suspend fun getQuestionsByQuizIdSync(quizId: Long): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE id = :questionId")
    suspend fun deleteQuestionById(questionId: Long)

    @Query("DELETE FROM questions WHERE quizId = :quizId")
    suspend fun deleteQuestionsForQuiz(quizId: Long)
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes WHERE teacherId = :teacherId ORDER BY createdAt DESC")
    fun getQuizzesByTeacher(teacherId: Long): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE isPublished = 1 ORDER BY createdAt DESC")
    fun getPublishedQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE id = :quizId LIMIT 1")
    suspend fun getQuizById(quizId: Long): QuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Delete
    suspend fun deleteQuiz(quiz: QuizEntity)
}

@Dao
interface QuizAttemptDao {
    @Query("SELECT * FROM quiz_attempts WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getAttemptsByStudent(studentId: Long): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE quizId = :quizId ORDER BY timestamp DESC")
    fun getAttemptsForQuiz(quizId: Long): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC")
    fun getAllAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE id = :attemptId LIMIT 1")
    suspend fun getAttemptById(attemptId: Long): QuizAttemptEntity?

    @Query("SELECT * FROM attempt_answers WHERE attemptId = :attemptId ORDER BY id ASC")
    fun getAnswersForAttempt(attemptId: Long): Flow<List<AttemptAnswerEntity>>

    @Query("SELECT * FROM attempt_answers WHERE attemptId = :attemptId ORDER BY id ASC")
    suspend fun getAnswersForAttemptSync(attemptId: Long): List<AttemptAnswerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<AttemptAnswerEntity>)
}
