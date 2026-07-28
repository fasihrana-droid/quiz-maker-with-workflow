package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.QuizViewModel
import com.example.ui.Screen
import com.example.ui.auth.AuthScreen
import com.example.ui.common.QuizTopBar
import com.example.ui.common.StudentBottomNavigation
import com.example.ui.common.TeacherBottomNavigation
import com.example.ui.common.TeacherSideNavigationRail
import com.example.ui.student.QuizAttemptScreen
import com.example.ui.student.QuizResultScreen
import com.example.ui.student.StudentDashboardScreen
import com.example.ui.teacher.QuestionBankDetailScreen
import com.example.ui.teacher.QuestionBankListScreen
import com.example.ui.teacher.QuizEditorScreen
import com.example.ui.teacher.QuizMakerListScreen
import com.example.ui.teacher.TeacherDashboardScreen
import com.example.ui.theme.QuizMakerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizMakerTheme {
                QuizMakerMainApp()
            }
        }
    }
}

@Composable
fun QuizMakerMainApp(quizViewModel: QuizViewModel = viewModel()) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val currentScreen by quizViewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by quizViewModel.currentUser.collectAsStateWithLifecycle()
    val authError by quizViewModel.authError.collectAsStateWithLifecycle()
    val toastMessage by quizViewModel.toastMessage.collectAsStateWithLifecycle()

    val questionBanks by quizViewModel.questionBanks.collectAsStateWithLifecycle()
    val teacherQuizzes by quizViewModel.teacherQuizzes.collectAsStateWithLifecycle()
    val publishedQuizzes by quizViewModel.publishedQuizzes.collectAsStateWithLifecycle()
    val studentAttempts by quizViewModel.studentAttempts.collectAsStateWithLifecycle()
    val allAttemptsForTeacher by quizViewModel.allAttemptsForTeacher.collectAsStateWithLifecycle()

    val activeBank by quizViewModel.activeBank.collectAsStateWithLifecycle()
    val bankQuestions by quizViewModel.bankQuestions.collectAsStateWithLifecycle()

    val activeQuiz by quizViewModel.activeQuiz.collectAsStateWithLifecycle()
    val activeQuizQuestions by quizViewModel.activeQuizQuestions.collectAsStateWithLifecycle()
    val selectedAnswers by quizViewModel.selectedAnswers.collectAsStateWithLifecycle()
    val flaggedQuestions by quizViewModel.flaggedQuestions.collectAsStateWithLifecycle()
    val remainingTimeSeconds by quizViewModel.remainingTimeSeconds.collectAsStateWithLifecycle()

    val activeAttempt by quizViewModel.activeAttempt.collectAsStateWithLifecycle()
    val activeAttemptAnswers by quizViewModel.activeAttemptAnswers.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            quizViewModel.clearToast()
        }
    }

    val isQuizTaking = currentScreen is Screen.StudentQuizTaking
    val isAuthScreen = currentScreen is Screen.Auth

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!isAuthScreen && !isQuizTaking) {
                QuizTopBar(
                    currentUser = currentUser,
                    onLogout = { quizViewModel.logout() }
                )
            }
        },
        bottomBar = {
            if (!isAuthScreen && !isQuizTaking && !isTablet && currentUser != null) {
                if (currentUser?.role == UserRole.TEACHER) {
                    TeacherBottomNavigation(
                        currentScreen = currentScreen,
                        onNavigate = { quizViewModel.navigateTo(it) }
                    )
                } else {
                    StudentBottomNavigation(
                        currentScreen = currentScreen,
                        onNavigate = { quizViewModel.navigateTo(it) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Wide tablet navigation rail for teachers
                if (!isAuthScreen && !isQuizTaking && isTablet && currentUser?.role == UserRole.TEACHER) {
                    TeacherSideNavigationRail(
                        currentScreen = currentScreen,
                        onNavigate = { quizViewModel.navigateTo(it) }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (val screen = currentScreen) {
                        is Screen.Auth -> {
                            AuthScreen(
                                errorMessage = authError,
                                onLogin = { email, pass -> quizViewModel.login(email, pass) },
                                onRegister = { name, email, pass, role -> quizViewModel.register(name, email, pass, role) },
                                onDemoTeacher = { quizViewModel.loginDemoTeacher() },
                                onDemoStudent = { quizViewModel.loginDemoStudent() }
                            )
                        }

                        is Screen.TeacherDashboard -> {
                            TeacherDashboardScreen(
                                quizzes = teacherQuizzes,
                                banks = questionBanks,
                                attempts = allAttemptsForTeacher,
                                onViewAttemptResult = { quizViewModel.viewAttemptResult(it) }
                            )
                        }

                        is Screen.TeacherQuestionBanks -> {
                            QuestionBankListScreen(
                                banks = questionBanks,
                                onSelectBank = { quizViewModel.selectBank(it) },
                                onCreateBank = { title, cat, desc -> quizViewModel.createQuestionBank(title, cat, desc) },
                                onDeleteBank = { quizViewModel.deleteQuestionBank(it) }
                            )
                        }

                        is Screen.TeacherBankDetail -> {
                            activeBank?.let { bank ->
                                QuestionBankDetailScreen(
                                    bank = bank,
                                    questions = bankQuestions,
                                    onAddQuestion = { cat, qText, o1, o2, o3, o4, correctIdx, exp ->
                                        quizViewModel.addQuestionToBank(
                                            bank.id, cat, qText, o1, o2, o3, o4, correctIdx, exp
                                        )
                                    },
                                    onUpdateQuestion = { quizViewModel.updateQuestion(it) },
                                    onDeleteQuestion = { quizViewModel.deleteQuestion(it) },
                                    onImportQuestions = { quizViewModel.importQuestionsToBank(bank.id, it) }
                                )
                            }
                        }

                        is Screen.TeacherQuizzes -> {
                            QuizMakerListScreen(
                                quizzes = teacherQuizzes,
                                onCreateNewQuiz = { quizViewModel.navigateTo(Screen.TeacherQuizEditor()) },
                                onEditQuiz = { quizViewModel.navigateTo(Screen.TeacherQuizEditor(it.id)) },
                                onTogglePublish = { quizViewModel.toggleQuizPublished(it) },
                                onDeleteQuiz = { quizViewModel.deleteQuiz(it) }
                            )
                        }

                        is Screen.TeacherQuizEditor -> {
                            val editingQuiz = teacherQuizzes.find { it.id == screen.quizId }
                            QuizEditorScreen(
                                initialQuiz = editingQuiz,
                                initialQuestions = emptyList(),
                                availableBanks = questionBanks,
                                fetchBankQuestions = { quizViewModel.getQuestionsByBankIdSync(it) },
                                onSaveQuiz = { qId, title, subj, desc, tLimit, pPct, isPub, questions ->
                                    quizViewModel.saveQuiz(qId, title, subj, desc, tLimit, pPct, isPub, questions)
                                },
                                onCancel = { quizViewModel.navigateTo(Screen.TeacherQuizzes) }
                            )
                        }

                        is Screen.StudentDashboard -> {
                            StudentDashboardScreen(
                                publishedQuizzes = publishedQuizzes,
                                pastAttempts = studentAttempts,
                                onStartQuiz = { quizViewModel.startQuizAttempt(it) },
                                onViewAttemptResult = { quizViewModel.viewAttemptResult(it) }
                            )
                        }

                        is Screen.StudentQuizTaking -> {
                            activeQuiz?.let { quiz ->
                                QuizAttemptScreen(
                                    quiz = quiz,
                                    questions = activeQuizQuestions,
                                    selectedAnswers = selectedAnswers,
                                    flaggedQuestions = flaggedQuestions,
                                    remainingTimeSeconds = remainingTimeSeconds,
                                    onSelectAnswer = { qId, optIdx -> quizViewModel.selectAnswer(qId, optIdx) },
                                    onToggleFlag = { quizViewModel.toggleFlagQuestion(it) },
                                    onSubmitAttempt = { quizViewModel.submitQuizAttempt() }
                                )
                            }
                        }

                        is Screen.QuizResult -> {
                            QuizResultScreen(
                                attempt = activeAttempt,
                                answers = activeAttemptAnswers,
                                onBackToDashboard = {
                                    val destination = if (currentUser?.role == UserRole.TEACHER) Screen.TeacherDashboard else Screen.StudentDashboard
                                    quizViewModel.navigateTo(destination)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
