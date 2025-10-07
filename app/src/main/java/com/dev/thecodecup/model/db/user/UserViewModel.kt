package com.dev.thecodecup.model.db.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository.getInstance(application)

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    init {
        ensureDefaultUserExists()
    }


    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(user)
            loadUser()
        }
    }


    fun ensureDefaultUserExists() {
        viewModelScope.launch {
            repository.ensureDefaultUserExists()
            _user.value = repository.getUser()
        }
    }

    fun loadUser() {
        viewModelScope.launch {
            _user.value = repository.getUser()
        }
    }
}
