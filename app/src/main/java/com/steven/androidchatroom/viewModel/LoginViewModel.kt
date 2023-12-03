package com.steven.androidchatroom.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: MainRepository): ViewModel() {

    var _account: MutableLiveData<String> = MutableLiveData<String>()
    val account: LiveData<String> get() = _account

    var _password: MutableLiveData<String> = MutableLiveData<String>()
    val password: LiveData<String> get() = _password

    var loginResponse: MutableLiveData<ApiResponse.LoginResponse>? = MutableLiveData()
    var errorResponse: MutableLiveData<ApiResponse.ErrorResponse>? = MutableLiveData()
    var dialogLoading: MutableLiveData<Boolean>? = MutableLiveData(false)

    fun login(){
        viewModelScope.launch {
            repository.login(password = password.value.toString(), account = account.value.toString()).catch {
                errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
            }.onStart {
                dialogLoading?.postValue(true)
            }.onCompletion {
                dialogLoading?.postValue(false)
            }.collect{
                if(it.status == 200) {
                    loginResponse?.postValue(it)
                }else{
                    errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
                }
            }
        }
    }




}