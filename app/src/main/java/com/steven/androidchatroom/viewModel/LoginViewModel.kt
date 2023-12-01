package com.steven.androidchatroom.viewModel

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: MainRepository): ViewModel() {

    var _account: MutableLiveData<String> = MutableLiveData<String>()
    val account: LiveData<String> get() = _account

    var _password: MutableLiveData<String> = MutableLiveData<String>()
    val password: LiveData<String> get() = _password

    var loginiRespose: MutableLiveData<ApiResponse.LoginResponse>? = MutableLiveData()
    var errorResponse: MutableLiveData<ApiResponse.ErrorResponse>? = MutableLiveData()

    fun login(){
        viewModelScope.launch {
            repository.login(password = password.value.toString(), account = account.value.toString()).collect{
                loginiRespose?.postValue(it)
            }
        }
    }




}