package com.steven.androidchatroom.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
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

    var friendChatUnreadCount: MutableLiveData<Int> = MutableLiveData<Int>().apply {
        value = 0
    }
    var friendRequestCount: MutableLiveData<Int> = MutableLiveData<Int>().apply {
        value = 0
    }

    val friendListResponse: MutableLiveData<ApiResponse.FriendListResponse>? = MutableLiveData()
    val errorResponse: MutableLiveData<ApiResponse.ErrorResponse>? = MutableLiveData()
    val friendRequestResponse: MutableLiveData<ApiResponse.FriendDataResponse>? = MutableLiveData()
    val dialogLoading: MutableLiveData<Boolean>? = MutableLiveData(false)
    val addFriendResponse: MutableLiveData<ApiResponse.AddFriendResponse>? = MutableLiveData()
    val friendConfirmResponse: MutableLiveData<ApiResponse.AddFriendResponse>? = MutableLiveData()


    fun getMyFriendList(){
        viewModelScope.launch {
            repository.getMyFriends(memberId.value.toString()).catch {
                errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
            }.onStart {
                dialogLoading?.postValue(true)
            }.onCompletion {
                dialogLoading?.postValue(false)
            }.collect{
                if(it.status == 200) {
                    friendListResponse?.postValue(it)
                }else{
                    errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
                }
            }
        }
    }

    fun sendFriendRequest(friendId: String){
        viewModelScope.launch {
            repository.sendFriendRequest(memberId.value.toString(), userName.value.toString(), friendId).catch {
                errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
            }.onStart {
                dialogLoading?.postValue(true)
            }.onCompletion {
                dialogLoading?.postValue(false)
            }.collect{
                if(it.status == 200) {
                    addFriendResponse?.postValue(it)
                }else{
                    errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
                }
            }
        }
    }

    fun getMyFriendRequest(){
        viewModelScope.launch {
            repository.getMyFriendsRequest(memberId.value.toString()).catch {
                errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
            }.collect{
                if(it.status == 200){
                    friendRequestResponse?.postValue(it)
                }else{
                    errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
                }
            }
        }
    }

    fun friendConfirm(requestId: String, requestName: String){
        viewModelScope.launch {
            repository.friendConfirm(memberId.value.toString(), requestId, userName.value.toString(), requestName).catch {
                errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
            }.collect{
                if(it.status == 200){
                    friendConfirmResponse?.postValue(it)
                }else{
                    errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
                }
            }

        }
    }


}