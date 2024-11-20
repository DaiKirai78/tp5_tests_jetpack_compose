package com.example.tests_jetpack_compose

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steps_table")
data class StepRecord(
    @PrimaryKey val date: String, // Format : "YYYY-MM-DD"
    var steps: Int
)