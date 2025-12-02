package com.onlineexamination.ui.screens.exam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlineexamination.data.model.QuestionAnalysis
import com.onlineexamination.ui.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemAnalysisScreen(
    examId: String,
    onBack: () -> Unit,
    analyticsViewModel: AnalyticsViewModel = viewModel()
) {
    val uiState by analyticsViewModel.uiState.collectAsState()

    LaunchedEffect(examId) {
        analyticsViewModel.getItemAnalysis(examId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Analysis") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(it)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            } else if (uiState.itemAnalysis != null) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item {
                        Text(
                            text = "Total Submissions: ${uiState.itemAnalysis!!.totalSubmissions}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    items(uiState.itemAnalysis!!.questionAnalyses) { analysis ->
                        QuestionAnalysisCard(analysis = analysis)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionAnalysisCard(analysis: QuestionAnalysis) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = analysis.question.questionText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Correct Answer: ${analysis.question.correctAnswer}")
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("%.1f%%".format(analysis.correctPercentage), style = MaterialTheme.typography.headlineSmall)
                    Text("Correct")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${analysis.correctAttempts}/${analysis.totalAttempts}", style = MaterialTheme.typography.headlineSmall)
                    Text("Responses")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Response Distribution:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            analysis.responseDistribution.forEach { (option, count) ->
                Text(text = "$option: $count (${String.format("%.1f", (count.toDouble() / analysis.totalAttempts) * 100)}%)")
            }
        }
    }
}
