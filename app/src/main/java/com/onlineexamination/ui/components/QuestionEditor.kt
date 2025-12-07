package com.onlineexamination.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlineexamination.data.model.Question
import com.onlineexamination.data.model.QuestionType

@Composable
fun QuestionEditor(
    question: Question,
    index: Int,
    onQuestionChange: (Question) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question $index",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            OutlinedTextField(
                value = question.questionText,
                onValueChange = { onQuestionChange(question.copy(questionText = it)) },
                label = { Text("Question Text *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Question Type
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = question.type.name.replace("_", " "),
                    onValueChange = {},
                    label = { Text("Question Type") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    QuestionType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name.replace("_", " ")) },
                            onClick = {
                                val newQuestion = when (type) {
                                    QuestionType.TRUE_FALSE -> question.copy(type = type, options = emptyList(), correctAnswer = "True")
                                    QuestionType.SHORT_ANSWER -> question.copy(type = type, options = emptyList())
                                    QuestionType.MULTIPLE_CHOICE -> question.copy(type = type, options = listOf("", "", "", ""))
                                }
                                onQuestionChange(newQuestion)
                                expanded = false
                            }
                        )
                    }
                }
            }

            when (question.type) {
                QuestionType.MULTIPLE_CHOICE -> {
                    Text("Options:", fontWeight = FontWeight.Medium)
                    question.options.forEachIndexed { optIndex, option ->
                        OutlinedTextField(
                            value = option,
                            onValueChange = { newOption ->
                                val newOptions = question.options.toMutableList()
                                newOptions[optIndex] = newOption
                                onQuestionChange(question.copy(options = newOptions))
                            },
                            label = { Text("Option ${optIndex + 1}") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    val options = question.options.filter { it.isNotBlank() }.map { DropdownOption(it, it) }
                    DropdownField(
                        label = "Correct Answer *",
                        selectedKey = question.correctAnswer,
                        options = options,
                        enabled = options.isNotEmpty()
                    ) {
                        onQuestionChange(question.copy(correctAnswer = it.key))
                    }
                }
                QuestionType.TRUE_FALSE -> {
                    val trueFalseOptions = listOf(DropdownOption("True", "True"), DropdownOption("False", "False"))
                    DropdownField(
                        label = "Correct Answer *",
                        selectedKey = question.correctAnswer,
                        options = trueFalseOptions,
                        enabled = true
                    ) {
                        onQuestionChange(question.copy(correctAnswer = it.key))
                    }
                }
                QuestionType.SHORT_ANSWER -> {
                    OutlinedTextField(
                        value = question.correctAnswer,
                        onValueChange = { onQuestionChange(question.copy(correctAnswer = it)) },
                        label = { Text("Correct Answer *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            OutlinedTextField(
                value = question.points.toString(),
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        onQuestionChange(question.copy(points = it.toIntOrNull() ?: 10))
                    }
                },
                label = { Text("Points") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
