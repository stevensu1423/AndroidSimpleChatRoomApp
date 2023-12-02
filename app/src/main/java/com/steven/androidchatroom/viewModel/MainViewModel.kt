package com.steven.androidchatroom.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: MainRepository): ViewModel() {

    var memberId: MutableLiveData<String> = MutableLiveData<String>().apply {
        value = ""
    }
    var userName: MutableLiveData<String> = MutableLiveData<String>().apply {
        value = ""
    }

    var friendListResponse: MutableLiveData<ApiResponse.FriendListResponse>? = MutableLiveData()
    var errorResponse: MutableLiveData<ApiResponse.ErrorResponse>? = MutableLiveData()

    fun getMyFriendList(){
        viewModelScope.launch {
            repository.getMyFriends(memberId.value.toString()).catch {
                errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
            }.collect{
                if(it.status == 200) {
                    friendListResponse?.postValue(it)
                }else{
                    errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
                }
            }
        }
    }


}