package com.example.timetable

import com.google.gson.annotations.SerializedName

data class SchoolInfoResponse(
    val schoolInfo: List<SchoolInfoWrapper>?
)

data class SchoolInfoWrapper(
    val head: List<Head>? = null,
    val row: List<SchoolRow>? = null
)

data class SchoolRow(
    val ATPT_OFCDC_SC_CODE: String?,
    val SD_SCHUL_CODE: String?,
    val SCHUL_NM: String?,
    val ORG_RDNMA: String?
)

data class ClassInfoResponse(
    val classInfo: List<ClassInfoWrapper>?
)

data class ClassInfoWrapper(
    val head: List<Head>? = null,
    val row: List<ClassRow>? = null
)

data class ClassRow(
    val AY: String?,
    val DDDEP_NM: String?,
    val GRADE: String?,
    val CLASS_NM: String?
)

data class TimetableResponse(
    val hisTimetable: List<TimetableWrapper>?
)

data class TimetableWrapper(
    val head: List<Head>? = null,
    val row: List<TimetableRow>? = null
)

data class TimetableRow(
    val ALL_TI_YMD: String?,
    val PERIO: String?,
    val ITRT_CNTNT: String?
)

data class MealServiceDietInfoResponse(
    val mealServiceDietInfo: List<MealInfoWrapper>?
)

data class MealInfoWrapper(
    val head: List<Head>? = null,
    val row: List<MealRow>? = null
)

data class MealRow(
    val MLSV_YMD: String?,
    val DDISH_NM: String?
)

data class Head(
    val list_total_count: Int?,
    val RESULT: ResultCode?
)

data class ResultCode(
    val CODE: String?,
    val MESSAGE: String?
)
