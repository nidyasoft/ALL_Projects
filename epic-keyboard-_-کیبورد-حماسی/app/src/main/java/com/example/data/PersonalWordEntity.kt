package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "personal_dictionary",
    indices = [Index(value = ["languageId", "word"], unique = true)]
)
data class PersonalWordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val languageId: String,
    val word: String,
    val frequency: Int = 5,
    val updatedAt: Long = System.currentTimeMillis()
)
