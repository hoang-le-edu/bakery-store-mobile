package com.dev.thecodecup.model.db.user

import android.content.Context
import androidx.room.Room
import com.dev.thecodecup.model.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository private constructor(context: Context) {
    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "user-db"
    ).build()
    private val userDao = db.userDao()

    private val defaultUser = UserEntity(
        id = 1,
        email = "khoa@gmail.com",
        name = "Khoa Ho Nguyen Dang",
        phone = "0123456789",
        address = "123 Main St, HCM City",
        point = 5000,
        stamp = 0
    )

    suspend fun getUser(): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUser()
    }


    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user.copy(id = 1))
    }


    suspend fun ensureDefaultUserExists() = withContext(Dispatchers.IO) {
        val user = userDao.getUser()
        if (user == null) {
            userDao.insertUser(defaultUser)
        }
    }

    companion object {
        @Volatile private var instance: UserRepository? = null
        fun getInstance(context: Context): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(context).also { instance = it }
            }
    }
}
