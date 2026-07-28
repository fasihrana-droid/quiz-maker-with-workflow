package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AttemptAnswerEntity
import com.example.data.model.QuestionBankEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.QuizEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class Screen {
    data object Auth : Screen()
    data object TeacherDashboard : Screen()
    data object TeacherQuestionBanks : Screen()
    data object TeacherQuizzes : Screen()
    data class TeacherQuizEditor(val quizId: Long? = null) : Screen()
    data class TeacherBankDetail(val bankId: Long) : Screen()
    data object StudentDashboard : Screen()
    data class StudentQuizTaking(val quizId: Long) : Screen()
    data class QuizResult(val attemptId: Long) : Screen()
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuizRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = QuizRepository(
            db.userDao(),
            db.questionBankDao(),
            db.questionDao(),
            db.quizDao(),
            db.quizAttemptDao()
        )
    }

    // Navigation & Auth State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Auth)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Data Flows
    private val _questionBanks = MutableStateFlow<List<QuestionBankEntity>>(emptyList())
    val questionBanks: StateFlow<List<QuestionBankEntity>> = _questionBanks.asStateFlow()

    private val _teacherQuizzes = MutableStateFlow<List<QuizEntity>>(emptyList())
    val teacherQuizzes: StateFlow<List<QuizEntity>> = _teacherQuizzes.asStateFlow()

    private val _publishedQuizzes = MutableStateFlow<List<QuizEntity>>(emptyList())
    val publishedQuizzes: StateFlow<List<QuizEntity>> = _publishedQuizzes.asStateFlow()

    private val _studentAttempts = MutableStateFlow<List<QuizAttemptEntity>>(emptyList())
    val studentAttempts: StateFlow<List<QuizAttemptEntity>> = _studentAttempts.asStateFlow()

    private val _allAttemptsForTeacher = MutableStateFlow<List<QuizAttemptEntity>>(emptyList())
    val allAttemptsForTeacher: StateFlow<List<QuizAttemptEntity>> = _allAttemptsForTeacher.asStateFlow()

    // Bank Detail / Question editing
    private val _activeBank = MutableStateFlow<QuestionBankEntity?>(null)
    val activeBank: StateFlow<QuestionBankEntity?> = _activeBank.asStateFlow()

    private val _bankQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val bankQuestions: StateFlow<List<QuestionEntity>> = _bankQuestions.asStateFlow()

    // Quiz Attempt State
    private val _activeQuiz = MutableStateFlow<QuizEntity?>(null)
    val activeQuiz: StateFlow<QuizEntity?> = _activeQuiz.asStateFlow()

    private val _activeQuizQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val activeQuizQuestions: StateFlow<List<QuestionEntity>> = _activeQuizQuestions.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val selectedAnswers: StateFlow<Map<Long, Int>> = _selectedAnswers.asStateFlow()

    private val _flaggedQuestions = MutableStateFlow<Set<Long>>(emptySet())
    val flaggedQuestions: StateFlow<Set<Long>> = _flaggedQuestions.asStateFlow()

    private val _remainingTimeSeconds = MutableStateFlow(0)
    val remainingTimeSeconds: StateFlow<Int> = _remainingTimeSeconds.asStateFlow()

    private var timerJob: Job? = null
    private var attemptStartTimeMs = 0L

    // Result Detail State
    private val _activeAttempt = MutableStateFlow<QuizAttemptEntity?>(null)
    val activeAttempt: StateFlow<QuizAttemptEntity?> = _activeAttempt.asStateFlow()

    private val _activeAttemptAnswers = MutableStateFlow<List<AttemptAnswerEntity>>(emptyList())
    val activeAttemptAnswers: StateFlow<List<AttemptAnswerEntity>> = _activeAttemptAnswers.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // --- Authentication ---
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authError.value = null
            if (email.isBlank() || pass.isBlank()) {
                _authError.value = "Please enter both email and password."
                return@launch
            }
            val user = repository.getUserByEmail(email.trim())
            if (user == null || user.passwordHash != pass) {
                _authError.value = "Invalid email or password."
            } else {
                setUserAndLoadData(user)
            }
        }
    }

    fun register(name: String, email: String, pass: String, role: UserRole) {
        viewModelScope.launch {
            _authError.value = null
            if (name.isBlank() || email.isBlank() || pass.isBlank()) {
                _authError.value = "Please fill in all registration fields."
                return@launch
            }
            val existing = repository.getUserByEmail(email.trim())
            if (existing != null) {
                _authError.value = "An account with this email already exists."
                return@launch
            }
            val newUser = UserEntity(
                name = name.trim(),
                email = email.trim(),
                passwordHash = pass,
                role = role
            )
            val newId = repository.registerUser(newUser)
            val createdUser = newUser.copy(id = newId)
            setUserAndLoadData(createdUser)
        }
    }

    fun loginDemoTeacher() {
        login("teacher@school.edu", "teacher123")
    }

    fun loginDemoStudent() {
        login("alex@student.edu", "student123")
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = Screen.Auth
        timerJob?.cancel()
    }

    private fun setUserAndLoadData(user: UserEntity) {
        _currentUser.value = user
        if (user.role == UserRole.TEACHER) {
            _currentScreen.value = Screen.TeacherDashboard
            observeTeacherData(user.id)
        } else {
            _currentScreen.value = Screen.StudentDashboard
            observeStudentData(user.id)
        }
    }

    private fun observeTeacherData(teacherId: Long) {
        viewModelScope.launch {
            repository.getQuestionBanksByTeacher(teacherId).catch {}.collect {
                _questionBanks.value = it
            }
        }
        viewModelScope.launch {
            repository.getQuizzesByTeacher(teacherId).catch {}.collect {
                _teacherQuizzes.value = it
            }
        }
        viewModelScope.launch {
            repository.getAllAttempts().catch {}.collect {
                _allAttemptsForTeacher.value = it
            }
        }
    }

    private fun observeStudentData(studentId: Long) {
        viewModelScope.launch {
            repository.getPublishedQuizzes().catch {}.collect {
                _publishedQuizzes.value = it
            }
        }
        viewModelScope.launch {
            repository.getAttemptsByStudent(studentId).catch {}.collect {
                _studentAttempts.value = it
            }
        }
    }

    suspend fun getQuestionsByBankIdSync(bankId: Long): List<QuestionEntity> {
        return repository.getQuestionsByBankIdSync(bankId)
    }

    // --- Question Bank Actions ---
    fun selectBank(bankId: Long) {
        viewModelScope.launch {
            val bank = repository.getQuestionBankById(bankId)
            _activeBank.value = bank
            _currentScreen.value = Screen.TeacherBankDetail(bankId)
            repository.getQuestionsByBankId(bankId).collect {
                _bankQuestions.value = it
            }
        }
    }

    fun createQuestionBank(title: String, category: String, description: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            if (title.isBlank()) {
                _toastMessage.value = "Bank title cannot be empty."
                return@launch
            }
            val bank = QuestionBankEntity(
                teacherId = user.id,
                title = title.trim(),
                category = category.trim().ifBlank { "General" },
                description = description.trim()
            )
            repository.createQuestionBank(bank)
            _toastMessage.value = "Question bank created successfully."
        }
    }

    fun deleteQuestionBank(bank: QuestionBankEntity) {
        viewModelScope.launch {
            repository.deleteQuestionBank(bank)
            _toastMessage.value = "Question bank deleted."
            if (_activeBank.value?.id == bank.id) {
                _activeBank.value = null
                _currentScreen.value = Screen.TeacherQuestionBanks
            }
        }
    }

    fun addQuestionToBank(
        bankId: Long,
        category: String,
        questionText: String,
        opt1: String,
        opt2: String,
        opt3: String,
        opt4: String,
        correctIdx: Int,
        explanation: String
    ) {
        viewModelScope.launch {
            if (questionText.isBlank() || opt1.isBlank() || opt2.isBlank()) {
                _toastMessage.value = "Question text and at least 2 options are required."
                return@launch
            }
            val q = QuestionEntity(
                questionBankId = bankId,
                category = category.trim().ifBlank { _activeBank.value?.category ?: "General" },
                questionText = questionText.trim(),
                option1 = opt1.trim(),
                option2 = opt2.trim(),
                option3 = opt3.trim().ifBlank { "N/A" },
                option4 = opt4.trim().ifBlank { "N/A" },
                correctAnswerIndex = correctIdx,
                explanation = explanation.trim()
            )
            repository.insertQuestion(q)
            _toastMessage.value = "Question added to bank."
        }
    }

    fun updateQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            repository.updateQuestion(question)
            _toastMessage.value = "Question updated."
        }
    }

    fun deleteQuestion(questionId: Long) {
        viewModelScope.launch {
            repository.deleteQuestionById(questionId)
            _toastMessage.value = "Question removed."
        }
    }

    fun importQuestionsToBank(bankId: Long, questions: List<QuestionEntity>) {
        viewModelScope.launch {
            if (questions.isEmpty()) {
                _toastMessage.value = "No valid questions found in import."
                return@launch
            }
            val updated = questions.map { it.copy(questionBankId = bankId) }
            repository.insertQuestions(updated)
            _toastMessage.value = "Imported ${questions.size} questions into bank!"
        }
    }

    // --- Quiz Actions ---
    fun saveQuiz(
        quizId: Long?,
        title: String,
        subject: String,
        description: String,
        timeLimitMin: Int,
        passPct: Int,
        isPublished: Boolean,
        questions: List<QuestionEntity>
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            if (title.isBlank()) {
                _toastMessage.value = "Quiz title is required."
                return@launch
            }
            if (questions.isEmpty()) {
                _toastMessage.value = "Please add at least 1 question to the quiz."
                return@launch
            }
            val quizEntity = QuizEntity(
                id = quizId ?: 0,
                teacherId = user.id,
                title = title.trim(),
                subject = subject.trim().ifBlank { "General" },
                description = description.trim(),
                timeLimitMinutes = timeLimitMin.coerceAtLeast(1),
                passingPercentage = passPct.coerceIn(1, 100),
                isPublished = isPublished
            )

            val savedQuizId = if (quizId == null || quizId == 0L) {
                repository.createQuiz(quizEntity)
            } else {
                repository.updateQuiz(quizEntity)
                quizId
            }

            repository.setQuizQuestions(savedQuizId, questions)
            _toastMessage.value = if (quizId == null) "Quiz created successfully!" else "Quiz updated successfully!"
            _currentScreen.value = Screen.TeacherQuizzes
        }
    }

    fun toggleQuizPublished(quiz: QuizEntity) {
        viewModelScope.launch {
            val updated = quiz.copy(isPublished = !quiz.isPublished)
            repository.updateQuiz(updated)
            val status = if (updated.isPublished) "Published" else "Unpublished (Draft)"
            _toastMessage.value = "Quiz is now $status."
        }
    }

    fun deleteQuiz(quiz: QuizEntity) {
        viewModelScope.launch {
            repository.deleteQuiz(quiz)
            _toastMessage.value = "Quiz deleted."
        }
    }

    // --- Student Attempt Actions ---
    fun startQuizAttempt(quizId: Long) {
        viewModelScope.launch {
            val quiz = repository.getQuizById(quizId)
            if (quiz == null) {
                _toastMessage.value = "Quiz not found."
                return@launch
            }
            val questions = repository.getQuestionsByQuizIdSync(quizId)
            if (questions.isEmpty()) {
                _toastMessage.value = "This quiz has no questions."
                return@launch
            }

            _activeQuiz.value = quiz
            _activeQuizQuestions.value = if (quiz.shuffleQuestions) questions.shuffled() else questions
            _selectedAnswers.value = emptyMap()
            _flaggedQuestions.value = emptySet()
            _remainingTimeSeconds.value = quiz.timeLimitMinutes * 60
            attemptStartTimeMs = System.currentTimeMillis()

            _currentScreen.value = Screen.StudentQuizTaking(quizId)

            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingTimeSeconds.value > 0) {
                delay(1000)
                _remainingTimeSeconds.value -= 1
            }
            // Time expired, auto-submit
            _toastMessage.value = "Time expired! Submitting test..."
            submitQuizAttempt()
        }
    }

    fun selectAnswer(questionId: Long, optionIndex: Int) {
        val current = _selectedAnswers.value.toMutableMap()
        current[questionId] = optionIndex
        _selectedAnswers.value = current
    }

    fun toggleFlagQuestion(questionId: Long) {
        val current = _flaggedQuestions.value.toMutableSet()
        if (current.contains(questionId)) {
            current.remove(questionId)
        } else {
            current.add(questionId)
        }
        _flaggedQuestions.value = current
    }

    fun submitQuizAttempt() {
        timerJob?.cancel()
        val user = _currentUser.value ?: return
        val quiz = _activeQuiz.value ?: return
        val questions = _activeQuizQuestions.value

        viewModelScope.launch {
            val answers = _selectedAnswers.value
            var correctCount = 0

            val answerEntities = questions.map { q ->
                val selectedIdx = answers[q.id] ?: -1
                val isCorrect = selectedIdx == q.correctAnswerIndex
                if (isCorrect) correctCount++

                AttemptAnswerEntity(
                    attemptId = 0, // Will be set by repository
                    questionId = q.id,
                    questionText = q.questionText,
                    option1 = q.option1,
                    option2 = q.option2,
                    option3 = q.option3,
                    option4 = q.option4,
                    selectedOptionIndex = selectedIdx,
                    correctOptionIndex = q.correctAnswerIndex,
                    isCorrect = isCorrect,
                    explanation = q.explanation
                )
            }

            val total = questions.size
            val pct = if (total > 0) (correctCount.toFloat() / total) * 100f else 0f
            val passed = pct >= quiz.passingPercentage
            val timeTakenSec = ((System.currentTimeMillis() - attemptStartTimeMs) / 1000).toInt().coerceAtLeast(1)

            val attemptEntity = QuizAttemptEntity(
                quizId = quiz.id,
                quizTitle = quiz.title,
                studentId = user.id,
                studentName = user.name,
                score = correctCount,
                totalQuestions = total,
                percentage = pct,
                passed = passed,
                timeTakenSeconds = timeTakenSec
            )

            val attemptId = repository.submitQuizAttempt(attemptEntity, answerEntities)
            _toastMessage.value = if (passed) "Congratulations! You passed the quiz." else "Quiz completed. Review your score."
            viewAttemptResult(attemptId)
        }
    }

    fun viewAttemptResult(attemptId: Long) {
        viewModelScope.launch {
            val attempt = repository.getAttemptById(attemptId)
            val answers = repository.getAnswersForAttemptSync(attemptId)
            _activeAttempt.value = attempt
            _activeAttemptAnswers.value = answers
            _currentScreen.value = Screen.QuizResult(attemptId)
        }
    }
}
