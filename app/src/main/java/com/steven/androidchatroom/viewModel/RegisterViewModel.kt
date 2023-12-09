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
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val repository: MainRepository): ViewModel() {

    var _name: MutableLiveData<String> = MutableLiveData<String>().apply {
        value = ""
    }

    val name: LiveData<String> get() = _name

    var _account: MutableLiveData<String> = MutableLiveData<String>().apply {
        value = ""
    }

    val account: LiveData<String> get() = _account

    var _password: MutableLiveData<String> = MutableLiveData<String>().apply {
        value = ""
    }

    val password: LiveData<String>  get() = _password

    val registerResponse: MutableLiveData<ApiResponse.RegisterResponse> = MutableLiveData()

    val errorResponse: MutableLiveData<ApiResponse.ErrorResponse> = MutableLiveData()

    val dialogLoading: MutableLiveData<Boolean> = MutableLiveData(false)

    fun register(email: String, password: String, userName: String){
        viewModelScope.launch {
            repository.register(email, password, userName).catch {
                errorResponse.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
            }.onStart {
                dialogLoading.postValue(true)
            }.onCompletion {
                dialogLoading.postValue(false)
            }.collect{
                if(it.status == 200) {
                    registerResponse.postValue(it)
                }else{
                    errorResponse.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
                }
            }
        }
    }
}