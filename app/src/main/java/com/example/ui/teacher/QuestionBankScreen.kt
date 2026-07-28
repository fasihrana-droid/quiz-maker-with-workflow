package com.example.ui.teacher

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionBankEntity
import com.example.data.model.QuestionEntity
import com.example.ui.common.EmptyStateCard
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryTeal
import com.example.util.CsvExcelManager

@Composable
fun QuestionBankListScreen(
    banks: List<QuestionBankEntity>,
    onSelectBank: (Long) -> Unit,
    onCreateBank: (String, String, String) -> Unit,
    onDeleteBank: (QuestionBankEntity) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

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
                            text = "Question Banks",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Manage and organize reusable questions by topic.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    Button(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier.testTag("create_bank_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Bank", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Bank", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (banks.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.FolderSpecial,
                        title = "No Question Banks",
                        description = "Create your first question bank to store and categorize questions, or import CSV/Excel spreadsheets.",
                        actionLabel = "Create Question Bank",
                        onAction = { showCreateDialog = true }
                    )
                }
            } else {
                items(banks) { bank ->
                    QuestionBankCard(
                        bank = bank,
                        onSelect = { onSelectBank(bank.id) },
                        onDelete = { onDeleteBank(bank) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateBankDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, cat, desc ->
                onCreateBank(title, cat, desc)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun QuestionBankCard(
    bank: QuestionBankEntity,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bank_card_${bank.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryIndigo.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FolderSpecial,
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SecondaryTeal.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = bank.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryTeal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = bank.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (bank.description.isNotBlank()) {
                    Text(
                        text = bank.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_bank_${bank.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Bank",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun QuestionBankDetailScreen(
    bank: QuestionBankEntity,
    questions: List<QuestionEntity>,
    onAddQuestion: (String, String, String, String, String, String, Int, String) -> Unit,
    onUpdateQuestion: (QuestionEntity) -> Unit,
    onDeleteQuestion: (Long) -> Unit,
    onImportQuestions: (List<QuestionEntity>) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var editingQuestion by remember { mutableStateOf<QuestionEntity?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val imported = CsvExcelManager.readQuestionsFromUri(context, it, bank.id, 0, bank.category)
            if (imported.isNotEmpty()) {
                onImportQuestions(imported)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = bank.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${questions.size} Questions in this Bank | Category: ${bank.category}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Import & Export Toolbar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showImportDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("import_csv_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = "Import", tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import CSV/Excel", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val csv = CsvExcelManager.exportQuestionsToCsv(questions)
                                    CsvExcelManager.shareCsvContent(context, bank.title, csv)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_csv_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Export", tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export CSV", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Questions List
            if (questions.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.HelpOutline,
                        title = "Bank is Empty",
                        description = "Add questions manually or import a CSV/Excel file matching: Category, Question, Option 1, Option 2, Option 3, Option 4, Correct answer, Explanation.",
                        actionLabel = "Add Question",
                        onAction = { showAddQuestionDialog = true }
                    )
                }
            } else {
                items(questions) { q ->
                    QuestionCardItem(
                        question = q,
                        onEdit = { editingQuestion = q },
                        onDelete = { onDeleteQuestion(q.id) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddQuestionDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_question"),
            containerColor = PrimaryIndigo,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Question")
        }
    }

    if (showAddQuestionDialog) {
        QuestionEditorDialog(
            defaultCategory = bank.category,
            onDismiss = { showAddQuestionDialog = false },
            onConfirm = { cat, qText, o1, o2, o3, o4, correctIdx, exp ->
                onAddQuestion(cat, qText, o1, o2, o3, o4, correctIdx, exp)
                showAddQuestionDialog = false
            }
        )
    }

    editingQuestion?.let { q ->
        QuestionEditorDialog(
            initialQuestion = q,
            defaultCategory = bank.category,
            onDismiss = { editingQuestion = null },
            onConfirm = { cat, qText, o1, o2, o3, o4, correctIdx, exp ->
                onUpdateQuestion(
                    q.copy(
                        category = cat,
                        questionText = qText,
                        option1 = o1,
                        option2 = o2,
                        option3 = o3,
                        option4 = o4,
                        correctAnswerIndex = correctIdx,
                        explanation = exp
                    )
                )
                editingQuestion = null
            }
        )
    }

    if (showImportDialog) {
        ImportCsvExcelDialog(
            onDismiss = { showImportDialog = false },
            onOpenFilePicker = {
                filePickerLauncher.launch("*/*")
                showImportDialog = false
            },
            onPasteCsv = { csvText ->
                val parsed = CsvExcelManager.parseCsvQuestions(csvText, bank.id, 0, bank.category)
                if (parsed.isNotEmpty()) {
                    onImportQuestions(parsed)
                }
                showImportDialog = false
            },
            onCopyTemplate = {
                clipboardManager.setText(AnnotatedString(CsvExcelManager.getSampleCsvTemplate()))
            }
        )
    }
}

@Composable
fun QuestionCardItem(
    question: QuestionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryIndigo.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = question.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryIndigo)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = question.questionText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            val options = listOf(question.option1, question.option2, question.option3, question.option4)
            options.forEachIndexed { idx, opt ->
                val isCorrect = idx == question.correctAnswerIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isCorrect) PrimaryIndigo.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.background
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${('A' + idx)}.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isCorrect) PrimaryIndigo else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = opt,
                        fontSize = 13.sp,
                        color = if (isCorrect) PrimaryIndigo else MaterialTheme.colorScheme.onSurface
                    )
                    if (isCorrect) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Correct Answer",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                    }
                }
            }

            if (question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = "Explanation",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Explanation: ${question.explanation}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateBankDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Science") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Question Bank", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bank Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category / Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, category, description) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Text("Create Bank")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun QuestionEditorDialog(
    initialQuestion: QuestionEntity? = null,
    defaultCategory: String = "General",
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, Int, String) -> Unit
) {
    var category by remember { mutableStateOf(initialQuestion?.category ?: defaultCategory) }
    var questionText by remember { mutableStateOf(initialQuestion?.questionText ?: "") }
    var option1 by remember { mutableStateOf(initialQuestion?.option1 ?: "") }
    var option2 by remember { mutableStateOf(initialQuestion?.option2 ?: "") }
    var option3 by remember { mutableStateOf(initialQuestion?.option3 ?: "") }
    var option4 by remember { mutableStateOf(initialQuestion?.option4 ?: "") }
    var correctIndex by remember { mutableIntStateOf(initialQuestion?.correctAnswerIndex ?: 0) }
    var explanation by remember { mutableStateOf(initialQuestion?.explanation ?: "") }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialQuestion == null) "Add Question" else "Edit Question",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question Text") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Options (Select radio button for Correct Answer):", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                val options = listOf(option1 to { s: String -> option1 = s },
                    option2 to { s: String -> option2 = s },
                    option3 to { s: String -> option3 = s },
                    option4 to { s: String -> option4 = s })

                options.forEachIndexed { idx, (optVal, setter) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = correctIndex == idx,
                            onClick = { correctIndex = idx }
                        )
                        OutlinedTextField(
                            value = optVal,
                            onValueChange = setter,
                            label = { Text("Option ${('A' + idx)}") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Explanation (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(category, questionText, option1, option2, option3, option4, correctIndex, explanation) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Text("Save Question")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ImportCsvExcelDialog(
    onDismiss: () -> Unit,
    onOpenFilePicker: () -> Unit,
    onPasteCsv: (String) -> Unit,
    onCopyTemplate: () -> Unit
) {
    var rawCsvInput by remember { mutableStateOf("") }
    var isPasteMode by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FileUpload, contentDescription = null, tint = PrimaryIndigo)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import CSV / Excel", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Import questions from CSV or Excel file matching columns:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryIndigo.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Category | Question | Option 1 | Option 2 | Option 3 | Option 4 | Correct answer | Explanation",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryIndigo,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCopyTemplate,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Copy Sample CSV Template", fontSize = 11.sp)
                    }
                }

                if (!isPasteMode) {
                    Button(
                        onClick = onOpenFilePicker,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("choose_file_button")
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select CSV/Excel File", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { isPasteMode = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Or Paste Raw CSV / Excel Text", fontSize = 12.sp, color = PrimaryIndigo)
                    }
                } else {
                    OutlinedTextField(
                        value = rawCsvInput,
                        onValueChange = { rawCsvInput = it },
                        label = { Text("Paste CSV or Excel Text Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = { onPasteCsv(rawCsvInput) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Parse & Import Questions")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
