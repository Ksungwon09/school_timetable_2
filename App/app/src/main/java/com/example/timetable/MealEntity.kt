package com.example.timetable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_table")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val mealName: String
)
