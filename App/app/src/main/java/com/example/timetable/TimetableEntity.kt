package com.example.timetable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // e.g. "20240416"
    val period: String, // e.g. "1"
    val subject: String // e.g. "수학"
)
