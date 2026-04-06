package com.example.timetable

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_table WHERE date = :date LIMIT 1")
    suspend fun getMealByDate(date: String): MealEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: MealEntity)

    @Query("DELETE FROM meal_table WHERE date = :date")
    suspend fun deleteByDate(date: String)
}
