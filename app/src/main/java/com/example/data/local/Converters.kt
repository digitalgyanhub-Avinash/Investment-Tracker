package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.CommitteeMonth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromCommitteeMonthList(value: List<CommitteeMonth>?): String {
        if (value == null) return "[]"
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCommitteeMonthList(value: String?): List<CommitteeMonth> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<CommitteeMonth>>() {}.type
        return try {
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
