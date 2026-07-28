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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionBankEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.QuizEntity
import com.example.ui.common.EmptyStateCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.TertiaryEmerald

@Composable
fun QuizMakerListScreen(
    quizzes: List<QuizEntity>,
    onCreateNewQuiz: () -> Unit,
    onEditQuiz: (QuizEntity) -> Unit,
    onTogglePublish: (QuizEntity) -> Unit,
    onDeleteQuiz: (QuizEntity) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Test / Quiz Maker",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Create, edit, and publish assessment tests for students.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    Button(
                        onClick = onCreateNewQuiz,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier.testTag("create_quiz_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Quiz", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Quiz", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (quizzes.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Assignment,
                        title = "No Quizzes Created",
                        description = "Design interactive timed quizzes with passing scores and questions from your banks or custom entry.",
                        actionLabel = "Create First Quiz",
                        onAction = onCreateNewQuiz
                    )
                }
            } else {
                items(quizzes) { quiz ->
                    QuizCardItem(
                        quiz = quiz,
                        onEdit = { onEditQuiz(quiz) },
                        onTogglePublish = { onTogglePublish(quiz) },
                        onDelete = { onDeleteQuiz(quiz) }
                    )
                }
            }
        }
    }
}

@Composable
fun QuizCardItem(
    quiz: QuizEntity,
    onEdit: () -> Unit,
    onTogglePublish: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quiz_card_${quiz.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = (if (quiz.isPublished) TertiaryEmerald else AccentAmber).copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (quiz.isPublished) TertiaryEmerald else AccentAmber)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (quiz.isPublished) "PUBLISHED" else "DRAFT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (quiz.isPublished) TertiaryEmerald else AccentAmber
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = quiz.isPublished,
                        onCheckedChange = { onTogglePublish() },
                        colors = SwitchDefaults.colors(checkedThumbColor = TertiaryEmerald),
                        modifier = Modifier.testTag("publish_switch_${quiz.id}")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryIndigo)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = quiz.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (quiz.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = quiz.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${quiz.timeLimitMinutes} mins",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TertiaryEmerald, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pass: ${quiz.passingPercentage}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Text(
                        text = quiz.subject,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryIndigo,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuizEditorScreen(
    initialQuiz: QuizEntity? = null,
    initialQuestions: List<QuestionEntity> = emptyList(),
    availableBanks: List<QuestionBankEntity>,
    fetchBankQuestions: suspend (Long) -> List<QuestionEntity>,
    onSaveQuiz: (Long?, String, String, String, Int, Int, Boolean, List<QuestionEntity>) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(initialQuiz?.title ?: "") }
    var subject by remember { mutableStateOf(initialQuiz?.subject ?: "Science") }
    var description by remember { mutableStateOf(initialQuiz?.description ?: "") }
    var timeLimitStr by remember { mutableStateOf((initialQuiz?.timeLimitMinutes ?: 15).toString()) }
    var passPctStr by remember { mutableStateOf((initialQuiz?.passingPercentage ?: 60).toString()) }
    var isPublished by remember { mutableStateOf(initialQuiz?.isPublished ?: false) }

    var quizQuestions by remember { mutableStateOf(initialQuestions) }

    var showBankSelectDialog by remember { mutableStateOf(false) }
    var showAddQuestionDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (initialQuiz == null) "Create New Test / Quiz" else "Edit Quiz",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Configure quiz parameters and attached questions.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Row {
                TextButton(onClick = onCancel) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val tLimit = timeLimitStr.toIntOrNull() ?: 15
                        val pPct = passPctStr.toIntOrNull() ?: 60
                        onSaveQuiz(
                            initialQuiz?.id,
                            title,
                            subject,
                            description,
                            tLimit,
                            pPct,
                            isPublished,
                            quizQuestions
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    modifier = Modifier.testTag("save_quiz_button")
                ) {
                    Text("Save Quiz", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Quiz Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("General Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Quiz Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_quiz_title"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = timeLimitStr,
                        onValueChange = { timeLimitStr = it },
                        label = { Text("Time Limit (mins)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = passPctStr,
                        onValueChange = { passPctStr = it },
                        label = { Text("Passing %") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Instructions / Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Publish State", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = if (isPublished) "Published to Student Portal" else "Draft (Hidden from students)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Switch(
                        checked = isPublished,
                        onCheckedChange = { isPublished = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TertiaryEmerald)
                    )
                }
            }
        }

        // Quiz Questions Header & Actions
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quiz Questions (${quizQuestions.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showBankSelectDialog = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FolderSpecial, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import from Bank", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showAddQuestionDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Custom", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (quizQuestions.isEmpty()) {
                    Text(
                        text = "No questions added to this quiz yet. Import from your question bank or add custom questions above.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        quizQuestions.forEachIndexed { idx, q ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Q${idx + 1}.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryIndigo
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = q.questionText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(
                                    onClick = {
                                        quizQuestions = quizQuestions.toMutableList().also { it.removeAt(idx) }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddQuestionDialog) {
        QuestionEditorDialog(
            defaultCategory = subject,
            onDismiss = { showAddQuestionDialog = false },
            onConfirm = { cat, qText, o1, o2, o3, o4, correctIdx, exp ->
                val newQ = QuestionEntity(
                    category = cat,
                    questionText = qText,
                    option1 = o1,
                    option2 = o2,
                    option3 = o3,
                    option4 = o4,
                    correctAnswerIndex = correctIdx,
                    explanation = exp
                )
                quizQuestions = quizQuestions + newQ
                showAddQuestionDialog = false
            }
        )
    }

    if (showBankSelectDialog) {
        ImportQuestionsFromBankDialog(
            banks = availableBanks,
            fetchBankQuestions = fetchBankQuestions,
            onDismiss = { showBankSelectDialog = false },
            onAddQuestions = { selected ->
                quizQuestions = quizQuestions + selected
                showBankSelectDialog = false
            }
        )
    }
}

@Composable
fun ImportQuestionsFromBankDialog(
    banks: List<QuestionBankEntity>,
    fetchBankQuestions: suspend (Long) -> List<QuestionEntity>,
    onDismiss: () -> Unit,
    onAddQuestions: (List<QuestionEntity>) -> Unit
) {
    var selectedBankId by remember { mutableStateOf<Long?>(banks.firstOrNull()?.id) }
    var loadedQuestions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }
    var selectedQuestionIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    androidx.compose.runtime.LaunchedEffect(selectedBankId) {
        selectedBankId?.let {
            loadedQuestions = fetchBankQuestions(it)
            selectedQuestionIds = loadedQuestions.map { q -> q.id }.toSet()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Questions from Bank", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.height(300.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Bank:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(banks) { b ->
                        Surface(
                            onClick = { selectedBankId = b.id },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedBankId == b.id) PrimaryIndigo.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${b.title} (${b.category})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedBankId == b.id) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedBankId == b.id) PrimaryIndigo else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                Text("Available Questions (${loadedQuestions.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(loadedQuestions) { q ->
                        val isChecked = selectedQuestionIds.contains(q.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    selectedQuestionIds = if (isChecked) selectedQuestionIds - q.id else selectedQuestionIds + q.id
                                }
                            )
                            Text(text = q.questionText, fontSize = 12.sp, maxLines = 1, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val questionsToAdd = loadedQuestions.filter { selectedQuestionIds.contains(it.id) }
                    onAddQuestions(questionsToAdd)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Text("Import (${selectedQuestionIds.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
