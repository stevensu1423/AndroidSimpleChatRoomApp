package com.steven.androidchatroom.viewModel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.repository.MainRepository
import com.steven.androidchatroom.util.datastore.PreferencesConstant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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

    var _isRemember: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    val loginResponse: MutableLiveData<ApiResponse.LoginResponse>? = MutableLiveData()
    val errorResponse: MutableLiveData<ApiResponse.ErrorResponse>? = MutableLiveData()
    val dialogLoading: MutableLiveData<Boolean>? = MutableLiveData(false)

    fun checkIsRememberAccount(dataStore: DataStore<Preferences>){
        viewModelScope.launch(Dispatchers.IO){
            dataStore.edit {
                if(it[PreferencesConstant.KEY_REMEMBER_ACCOUNT] == true){
                    _isRemember.postValue(true)
                    _account.postValue(it[PreferencesConstant.KEY_ACCOUNT])
                    _password.postValue(it[PreferencesConstant.KEY_PASSWORD])
                }
            }
        }
    }

    fun setRememberAccount(dataStore: DataStore<Preferences>){
        viewModelScope.launch(Dispatchers.IO){
            dataStore.edit {
                if(_isRemember.value == true){
                    it[PreferencesConstant.KEY_REMEMBER_ACCOUNT] = true
                    it[PreferencesConstant.KEY_ACCOUNT] = _account.value.toString()
                    it[PreferencesConstant.KEY_PASSWORD] = _password.value.toString()
                }else{
                    it.clear()
                }
            }
        }
    }
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