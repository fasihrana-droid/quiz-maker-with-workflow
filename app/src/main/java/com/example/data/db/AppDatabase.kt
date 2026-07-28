package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        QuestionBankEntity::class,
        QuestionEntity::class,
        QuizEntity::class,
        QuizAttemptEntity::class,
        AttemptAnswerEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun questionBankDao(): QuestionBankDao
    abstract fun questionDao(): QuestionDao
    abstract fun quizDao(): QuizDao
    abstract fun quizAttemptDao(): QuizAttemptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quiz_maker_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val userDao = database.userDao()
            val bankDao = database.questionBankDao()
            val questionDao = database.questionDao()
            val quizDao = database.quizDao()
            val attemptDao = database.quizAttemptDao()

            // 1. Seed Demo Users
            val teacherId = userDao.insertUser(
                UserEntity(
                    id = 1,
                    name = "Prof. Sarah Jenkins",
                    email = "teacher@school.edu",
                    passwordHash = "teacher123",
                    role = UserRole.TEACHER
                )
            )

            val student1Id = userDao.insertUser(
                UserEntity(
                    id = 2,
                    name = "Alex Rivera",
                    email = "alex@student.edu",
                    passwordHash = "student123",
                    role = UserRole.STUDENT
                )
            )

            val student2Id = userDao.insertUser(
                UserEntity(
                    id = 3,
                    name = "Emma Watson",
                    email = "emma@student.edu",
                    passwordHash = "student123",
                    role = UserRole.STUDENT
                )
            )

            // 2. Seed Question Bank
            val bank1Id = bankDao.insertQuestionBank(
                QuestionBankEntity(
                    id = 1,
                    teacherId = teacherId,
                    title = "General Science & Physics",
                    category = "Science",
                    description = "Fundamental questions on physics, chemistry, and astronomy."
                )
            )

            val bank2Id = bankDao.insertQuestionBank(
                QuestionBankEntity(
                    id = 2,
                    teacherId = teacherId,
                    title = "Mathematics & Logic",
                    category = "Mathematics",
                    description = "Algebra, geometry, and numerical logic test questions."
                )
            )

            // Seed Bank Questions
            val bank1Questions = listOf(
                QuestionEntity(
                    questionBankId = bank1Id,
                    category = "Science",
                    questionText = "What is the primary chemical component of Earth's atmosphere?",
                    option1 = "Oxygen",
                    option2 = "Nitrogen",
                    option3 = "Carbon Dioxide",
                    option4 = "Hydrogen",
                    correctAnswerIndex = 1,
                    explanation = "Nitrogen makes up approximately 78% of Earth's atmosphere."
                ),
                QuestionEntity(
                    questionBankId = bank1Id,
                    category = "Science",
                    questionText = "What speed does light travel in a vacuum?",
                    option1 = "150,000 km/s",
                    option2 = "299,792 km/s",
                    option3 = "1,080,000 km/s",
                    option4 = "500,000 km/s",
                    correctAnswerIndex = 1,
                    explanation = "Light travels at roughly 299,792 kilometers per second in a vacuum."
                ),
                QuestionEntity(
                    questionBankId = bank1Id,
                    category = "Science",
                    questionText = "Which planet in the solar system has the highest surface temperature?",
                    option1 = "Mercury",
                    option2 = "Venus",
                    option3 = "Mars",
                    option4 = "Jupiter",
                    correctAnswerIndex = 1,
                    explanation = "Venus has a dense, heat-trapping greenhouse atmosphere reaching 465°C."
                ),
                QuestionEntity(
                    questionBankId = bank1Id,
                    category = "Science",
                    questionText = "What organelle is known as the powerhouse of the eukaryotic cell?",
                    option1 = "Ribosome",
                    option2 = "Nucleus",
                    option3 = "Mitochondria",
                    option4 = "Endoplasmic Reticulum",
                    correctAnswerIndex = 2,
                    explanation = "Mitochondria generate most of the cell's ATP chemical energy."
                )
            )
            questionDao.insertQuestions(bank1Questions)

            val bank2Questions = listOf(
                QuestionEntity(
                    questionBankId = bank2Id,
                    category = "Mathematics",
                    questionText = "What is the square root of 256?",
                    option1 = "14",
                    option2 = "16",
                    option3 = "18",
                    option4 = "32",
                    correctAnswerIndex = 1,
                    explanation = "16 x 16 equals 256."
                ),
                QuestionEntity(
                    questionBankId = bank2Id,
                    category = "Mathematics",
                    questionText = "If 3x + 7 = 22, what is the value of x?",
                    option1 = "3",
                    option2 = "5",
                    option3 = "7",
                    option4 = "8",
                    correctAnswerIndex = 1,
                    explanation = "Subtract 7 to get 3x = 15, then divide by 3 to find x = 5."
                ),
                QuestionEntity(
                    questionBankId = bank2Id,
                    category = "Mathematics",
                    questionText = "What is the sum of angles in a triangle?",
                    option1 = "90 degrees",
                    option2 = "180 degrees",
                    option3 = "270 degrees",
                    option4 = "360 degrees",
                    correctAnswerIndex = 1,
                    explanation = "The interior angles of any planar triangle always sum to 180 degrees."
                )
            )
            questionDao.insertQuestions(bank2Questions)

            // 3. Seed Quizzes
            val quiz1Id = quizDao.insertQuiz(
                QuizEntity(
                    id = 1,
                    teacherId = teacherId,
                    title = "Midterm Science Evaluation",
                    description = "Test your core knowledge in general science and astronomy.",
                    subject = "Science",
                    timeLimitMinutes = 10,
                    passingPercentage = 70,
                    isPublished = true,
                    shuffleQuestions = true
                )
            )

            val quiz2Id = quizDao.insertQuiz(
                QuizEntity(
                    id = 2,
                    teacherId = teacherId,
                    title = "Algebra & Geometry Quiz",
                    description = "Assess foundational math principles and algebra speed.",
                    subject = "Mathematics",
                    timeLimitMinutes = 15,
                    passingPercentage = 60,
                    isPublished = true,
                    shuffleQuestions = false
                )
            )

            // Questions specifically attached to Quiz 1
            val quiz1Questions = listOf(
                QuestionEntity(
                    quizId = quiz1Id,
                    category = "Science",
                    questionText = "What is the chemical symbol for Gold?",
                    option1 = "Ag",
                    option2 = "Au",
                    option3 = "Fe",
                    option4 = "Gd",
                    correctAnswerIndex = 1,
                    explanation = "Au originates from the Latin word for gold, Aurum."
                ),
                QuestionEntity(
                    quizId = quiz1Id,
                    category = "Science",
                    questionText = "Which particle in an atom carries a negative charge?",
                    option1 = "Proton",
                    option2 = "Neutron",
                    option3 = "Electron",
                    option4 = "Photon",
                    correctAnswerIndex = 2,
                    explanation = "Electrons orbit the nucleus and hold a negative electric charge."
                ),
                QuestionEntity(
                    quizId = quiz1Id,
                    category = "Science",
                    questionText = "What is the acceleration due to gravity on Earth's surface?",
                    option1 = "8.2 m/s²",
                    option2 = "9.8 m/s²",
                    option3 = "11.2 m/s²",
                    option4 = "15.0 m/s²",
                    correctAnswerIndex = 1,
                    explanation = "Standard Earth surface gravity is approximately 9.8 m/s²."
                ),
                QuestionEntity(
                    quizId = quiz1Id,
                    category = "Science",
                    questionText = "What process do green plants use to convert light into food energy?",
                    option1 = "Respiration",
                    option2 = "Photosynthesis",
                    option3 = "Fermentation",
                    option4 = "Transpiration",
                    correctAnswerIndex = 1,
                    explanation = "Photosynthesis converts carbon dioxide and water into oxygen and glucose using sunlight."
                )
            )
            questionDao.insertQuestions(quiz1Questions)

            // Questions attached to Quiz 2
            val quiz2Questions = listOf(
                QuestionEntity(
                    quizId = quiz2Id,
                    category = "Mathematics",
                    questionText = "What is the area of a rectangle with length 8 cm and width 5 cm?",
                    option1 = "26 cm²",
                    option2 = "40 cm²",
                    option3 = "13 cm²",
                    option4 = "35 cm²",
                    correctAnswerIndex = 1,
                    explanation = "Area = Length x Width = 8 x 5 = 40 cm²."
                ),
                QuestionEntity(
                    quizId = quiz2Id,
                    category = "Mathematics",
                    questionText = "What is the next prime number after 13?",
                    option1 = "15",
                    option2 = "17",
                    option3 = "19",
                    option4 = "21",
                    correctAnswerIndex = 1,
                    explanation = "17 is divisible only by 1 and 17."
                ),
                QuestionEntity(
                    quizId = quiz2Id,
                    category = "Mathematics",
                    questionText = "Calculate 12% of 250.",
                    option1 = "25",
                    option2 = "30",
                    option3 = "35",
                    option4 = "40",
                    correctAnswerIndex = 1,
                    explanation = "(12 / 100) * 250 = 30."
                )
            )
            questionDao.insertQuestions(quiz2Questions)

            // 4. Seed Demo Student Attempts
            val attempt1Id = attemptDao.insertAttempt(
                QuizAttemptEntity(
                    quizId = quiz1Id,
                    quizTitle = "Midterm Science Evaluation",
                    studentId = student1Id,
                    studentName = "Alex Rivera",
                    score = 4,
                    totalQuestions = 4,
                    percentage = 100f,
                    passed = true,
                    timeTakenSeconds = 240
                )
            )

            val attempt2Id = attemptDao.insertAttempt(
                QuizAttemptEntity(
                    quizId = quiz1Id,
                    quizTitle = "Midterm Science Evaluation",
                    studentId = student2Id,
                    studentName = "Emma Watson",
                    score = 3,
                    totalQuestions = 4,
                    percentage = 75f,
                    passed = true,
                    timeTakenSeconds = 310
                )
            )

            // Answers for Attempt 1
            val attempt1Answers = listOf(
                AttemptAnswerEntity(
                    attemptId = attempt1Id,
                    questionId = 1,
                    questionText = "What is the chemical symbol for Gold?",
                    option1 = "Ag", option2 = "Au", option3 = "Fe", option4 = "Gd",
                    selectedOptionIndex = 1, correctOptionIndex = 1, isCorrect = true,
                    explanation = "Au originates from the Latin word for gold, Aurum."
                ),
                AttemptAnswerEntity(
                    attemptId = attempt1Id,
                    questionId = 2,
                    questionText = "Which particle in an atom carries a negative charge?",
                    option1 = "Proton", option2 = "Neutron", option3 = "Electron", option4 = "Photon",
                    selectedOptionIndex = 2, correctOptionIndex = 2, isCorrect = true,
                    explanation = "Electrons orbit the nucleus and hold a negative electric charge."
                ),
                AttemptAnswerEntity(
                    attemptId = attempt1Id,
                    questionId = 3,
                    questionText = "What is the acceleration due to gravity on Earth's surface?",
                    option1 = "8.2 m/s²", option2 = "9.8 m/s²", option3 = "11.2 m/s²", option4 = "15.0 m/s²",
                    selectedOptionIndex = 1, correctOptionIndex = 1, isCorrect = true,
                    explanation = "Standard Earth surface gravity is approximately 9.8 m/s²."
                ),
                AttemptAnswerEntity(
                    attemptId = attempt1Id,
                    questionId = 4,
                    questionText = "What process do green plants use to convert light into food energy?",
                    option1 = "Respiration", option2 = "Photosynthesis", option3 = "Fermentation", option4 = "Transpiration",
                    selectedOptionIndex = 1, correctOptionIndex = 1, isCorrect = true,
                    explanation = "Photosynthesis converts carbon dioxide and water into oxygen and glucose using sunlight."
                )
            )
            attemptDao.insertAnswers(attempt1Answers)

            // Answers for Attempt 2
            val attempt2Answers = listOf(
                AttemptAnswerEntity(
                    attemptId = attempt2Id,
                    questionId = 1,
                    questionText = "What is the chemical symbol for Gold?",
                    option1 = "Ag", option2 = "Au", option3 = "Fe", option4 = "Gd",
                    selectedOptionIndex = 0, correctOptionIndex = 1, isCorrect = false,
                    explanation = "Au originates from the Latin word for gold, Aurum."
                ),
                AttemptAnswerEntity(
                    attemptId = attempt2Id,
                    questionId = 2,
                    questionText = "Which particle in an atom carries a negative charge?",
                    option1 = "Proton", option2 = "Neutron", option3 = "Electron", option4 = "Photon",
                    selectedOptionIndex = 2, correctOptionIndex = 2, isCorrect = true,
                    explanation = "Electrons orbit the nucleus and hold a negative electric charge."
                ),
                AttemptAnswerEntity(
                    attemptId = attempt2Id,
                    questionId = 3,
                    questionText = "What is the acceleration due to gravity on Earth's surface?",
                    option1 = "8.2 m/s²", option2 = "9.8 m/s²", option3 = "11.2 m/s²", option4 = "15.0 m/s²",
                    selectedOptionIndex = 1, correctOptionIndex = 1, isCorrect = true,
                    explanation = "Standard Earth surface gravity is approximately 9.8 m/s²."
                ),
                AttemptAnswerEntity(
                    attemptId = attempt2Id,
                    questionId = 4,
                    questionText = "What process do green plants use to convert light into food energy?",
                    option1 = "Respiration", option2 = "Photosynthesis", option3 = "Fermentation", option4 = "Transpiration",
                    selectedOptionIndex = 1, correctOptionIndex = 1, isCorrect = true,
                    explanation = "Photosynthesis converts carbon dioxide and water into oxygen and glucose using sunlight."
                )
            )
            attemptDao.insertAnswers(attempt2Answers)
        }
    }
}
