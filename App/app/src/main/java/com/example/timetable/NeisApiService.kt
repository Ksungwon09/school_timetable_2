package com.example.timetable

import retrofit2.http.GET
import retrofit2.http.Query

interface NeisApiService {
    @GET("schoolInfo")
    suspend fun getSchoolInfo(
        @Query("KEY") apiKey: String,
        @Query("Type") type: String = "json",
        @Query("pIndex") pIndex: Int = 1,
        @Query("pSize") pSize: Int = 100,
        @Query("SCHUL_NM") schoolName: String
    ): SchoolInfoResponse

    @GET("classInfo")
    suspend fun getClassInfo(
        @Query("KEY") apiKey: String,
        @Query("Type") type: String = "json",
        @Query("pIndex") pIndex: Int = 1,
        @Query("pSize") pSize: Int = 1000,
        @Query("ATPT_OFCDC_SC_CODE") ofcdcCode: String,
        @Query("SD_SCHUL_CODE") schoolCode: String
    ): ClassInfoResponse

    @GET("hisTimetable")
    suspend fun getHisTimetable(
        @Query("KEY") apiKey: String,
        @Query("Type") type: String = "json",
        @Query("pIndex") pIndex: Int = 1,
        @Query("pSize") pSize: Int = 1000,
        @Query("ATPT_OFCDC_SC_CODE") ofcdcCode: String,
        @Query("SD_SCHUL_CODE") schoolCode: String,
        @Query("AY") ay: String,
        @Query("SEM") sem: String,
        @Query("DDDEP_NM") dddepNm: String?,
        @Query("GRADE") grade: String,
        @Query("CLASS_NM") classNm: String,
        @Query("TI_FROM_YMD") tiFromYmd: String,
        @Query("TI_TO_YMD") tiToYmd: String
    ): TimetableResponse

    @GET("mealServiceDietInfo")
    suspend fun getMealInfo(
        @Query("KEY") apiKey: String,
        @Query("Type") type: String = "json",
        @Query("pIndex") pIndex: Int = 1,
        @Query("pSize") pSize: Int = 100,
        @Query("ATPT_OFCDC_SC_CODE") ofcdcCode: String,
        @Query("SD_SCHUL_CODE") schoolCode: String,
        @Query("MLSV_YMD") mlsvYmd: String?,
        @Query("MLSV_FROM_YMD") mlsvFromYmd: String? = null,
        @Query("MLSV_TO_YMD") mlsvToYmd: String? = null
    ): MealServiceDietInfoResponse
}
