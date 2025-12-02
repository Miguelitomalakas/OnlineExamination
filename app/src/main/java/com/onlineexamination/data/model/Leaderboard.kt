package com.onlineexamination.data.model

data class Leaderboard(
    val term: String,
    val entries: List<LeaderboardEntry>
)

data class LeaderboardEntry(
    val rank: Int = 0,
    val studentId: String,
    val studentName: String,
    val score: Int
)
