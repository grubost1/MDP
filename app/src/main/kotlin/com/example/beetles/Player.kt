package com.example.beetles

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val gender: String,
    val course: String,
    val birthDate: String,
    val zodiacSign: String
)