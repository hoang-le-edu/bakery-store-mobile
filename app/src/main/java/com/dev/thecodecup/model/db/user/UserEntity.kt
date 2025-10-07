package com.dev.thecodecup.model.db.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val email: String,
    val name: String,
    val phone: String,
    val address: String,
    val point: Int,
    val stamp: Int = 0
)
