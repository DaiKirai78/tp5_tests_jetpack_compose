package com.example.tests_jetpack_compose

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StepDao {
    @Query("SELECT * FROM steps_table ORDER BY date DESC")
    suspend fun getAllSteps(): List<StepRecord>

    @Query("SELECT * FROM steps_table WHERE date = :date LIMIT 1")
    suspend fun getStepsForDate(date: String): StepRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStep(stepRecord: StepRecord)
}