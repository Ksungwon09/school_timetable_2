package com.example.timetable

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, period ASC")
    suspend fun getTimetableByDateRange(startDate: String, endDate: String): List<TimetableEntity>

    @Query("SELECT * FROM timetable WHERE date = :date ORDER BY period ASC")
    suspend fun getTimetableByDate(date: String): List<TimetableEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(timetables: List<TimetableEntity>)

    @Query("DELETE FROM timetable WHERE date BETWEEN :startDate AND :endDate")
    suspend fun deleteByDateRange(startDate: String, endDate: String)

    @Query("DELETE FROM timetable")
    suspend fun clearAll()
}
