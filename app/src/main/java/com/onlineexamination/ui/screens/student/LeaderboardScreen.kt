package com.onlineexamination.ui.screens.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlineexamination.data.model.LeaderboardEntry
import com.onlineexamination.ui.viewmodel.LeaderboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit,
    leaderboardViewModel: LeaderboardViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Prelim", "Midterm", "Finals")

    val uiState by leaderboardViewModel.uiState.collectAsState()

    LaunchedEffect(selectedTab) {
        leaderboardViewModel.getLeaderboard(tabs[selectedTab])
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaderboards") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(it)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (uiState.leaderboard != null) {
                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        items(uiState.leaderboard!!.entries) { entry ->
                            LeaderboardCard(entry = entry)
                        }
                    }
                } else {
                    Text("No leaderboard data found.", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun LeaderboardCard(entry: LeaderboardEntry) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${entry.rank}", style = MaterialTheme.typography.titleLarge)
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "Rank",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                )
                Column {
                    Text(
                        text = entry.studentName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Score: ${entry.score}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
