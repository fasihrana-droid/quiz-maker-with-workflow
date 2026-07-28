package com.example.data.repository

import com.example.data.dao.QuestionBankDao
import com.example.data.dao.QuestionDao
import com.example.data.dao.QuizAttemptDao
import com.example.data.dao.QuizDao
import com.example.data.dao.UserDao
import com.example.data.model.AttemptAnswerEntity
import com.example.data.model.QuestionBankEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.QuizEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow

class QuizRepository(
    private val userDao: UserDao,
    private val bankDao: QuestionBankDao,
    private val questionDao: QuestionDao,
    private val quizDao: QuizDao,
    private val attemptDao: QuizAttemptDao
) {
    // Auth
    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)
    suspend fun getUserById(id: Long): UserEntity? = userDao.getUserById(id)
    suspend fun registerUser(user: UserEntity): Long = userDao.insertUser(user)
    fun getAllStudents(): Flow<List<UserEntity>> = userDao.getUsersByRole(UserRole.STUDENT)

    // Question Banks
    fun getQuestionBanksByTeacher(teacherId: Long): Flow<List<QuestionBankEntity>> =
        bankDao.getQuestionBanksByTeacher(teacherId)
    fun getAllQuestionBanks(): Flow<List<QuestionBankEntity>> = bankDao.getAllQuestionBanks()
    suspend fun getQuestionBankById(bankId: Long): QuestionBankEntity? = bankDao.getQuestionBankById(bankId)
    suspend fun createQuestionBank(bank: QuestionBankEntity): Long = bankDao.insertQuestionBank(bank)
    suspend fun updateQuestionBank(bank: QuestionBankEntity) = bankDao.updateQuestionBank(bank)
    suspend fun deleteQuestionBank(bank: QuestionBankEntity) = bankDao.deleteQuestionBank(bank)

    // Questions
    fun getQuestionsByBankId(bankId: Long): Flow<List<QuestionEntity>> = questionDao.getQuestionsByBankId(bankId)
    suspend fun getQuestionsByBankIdSync(bankId: Long): List<QuestionEntity> = questionDao.getQuestionsByBankIdSync(bankId)
    fun getQuestionsByQuizId(quizId: Long): Flow<List<QuestionEntity>> = questionDao.getQuestionsByQuizId(quizId)
    suspend fun getQuestionsByQuizIdSync(quizId: Long): List<QuestionEntity> = questionDao.getQuestionsByQuizIdSync(quizId)
    suspend fun insertQuestion(question: QuestionEntity): Long = questionDao.insertQuestion(question)
    suspend fun insertQuestions(questions: List<QuestionEntity>) = questionDao.insertQuestions(questions)
    suspend fun updateQuestion(question: QuestionEntity) = questionDao.updateQuestion(question)
    suspend fun deleteQuestionById(questionId: Long) = questionDao.deleteQuestionById(questionId)

    // Quizzes
    fun getQuizzesByTeacher(teacherId: Long): Flow<List<QuizEntity>> = quizDao.getQuizzesByTeacher(teacherId)
    fun getPublishedQuizzes(): Flow<List<QuizEntity>> = quizDao.getPublishedQuizzes()
    suspend fun getQuizById(quizId: Long): QuizEntity? = quizDao.getQuizById(quizId)
    suspend fun createQuiz(quiz: QuizEntity): Long = quizDao.insertQuiz(quiz)
    suspend fun updateQuiz(quiz: QuizEntity) = quizDao.updateQuiz(quiz)
    suspend fun deleteQuiz(quiz: QuizEntity) = quizDao.deleteQuiz(quiz)
    suspend fun setQuizQuestions(quizId: Long, questions: List<QuestionEntity>) {
        questionDao.deleteQuestionsForQuiz(quizId)
        val updatedQuestions = questions.map { it.copy(quizId = quizId) }
        questionDao.insertQuestions(updatedQuestions)
    }

    // Attempts & Analytics
    fun getAttemptsByStudent(studentId: Long): Flow<List<QuizAttemptEntity>> =
        attemptDao.getAttemptsByStudent(studentId)
    fun getAttemptsForQuiz(quizId: Long): Flow<List<QuizAttemptEntity>> =
        attemptDao.getAttemptsForQuiz(quizId)
    fun getAllAttempts(): Flow<List<QuizAttemptEntity>> = attemptDao.getAllAttempts()
    suspend fun getAttemptById(attemptId: Long): QuizAttemptEntity? = attemptDao.getAttemptById(attemptId)
    fun getAnswersForAttempt(attemptId: Long): Flow<List<AttemptAnswerEntity>> =
        attemptDao.getAnswersForAttempt(attemptId)
    suspend fun getAnswersForAttemptSync(attemptId: Long): List<AttemptAnswerEntity> =
        attemptDao.getAnswersForAttemptSync(attemptId)

    suspend fun submitQuizAttempt(
        attempt: QuizAttemptEntity,
        answers: List<AttemptAnswerEntity>
    ): Long {
        val attemptId = attemptDao.insertAttempt(attempt)
        val answersWithAttemptId = answers.map { it.copy(attemptId = attemptId) }
        attemptDao.insertAnswers(answersWithAttemptId)
        return attemptId
    }
}
