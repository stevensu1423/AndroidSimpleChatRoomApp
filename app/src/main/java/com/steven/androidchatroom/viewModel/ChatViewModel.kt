package com.steven.androidchatroom.viewModel

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
class ChatViewModel @Inject constructor(private val repository: MainRepository): ViewModel() {

    val chatListResponse: MutableLiveData<ApiResponse.ChatHistoryResponse> = MutableLiveData()
    val errorResponse: MutableLiveData<ApiResponse.ErrorResponse>? = MutableLiveData()
    val dialogLoading: MutableLiveData<Boolean>? = MutableLiveData(false)
    val unSendResponse: MutableLiveData<ApiResponse.AddFriendResponse> = MutableLiveData()
    val uploadPhotoResponse: MutableLiveData<ApiResponse.UploadPhotoResponse> = MutableLiveData()

    fun getChatList(roomId: String, friendId: String){
        viewModelScope.launch {
            repository.getChatList(roomId, friendId).catch {
                errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
            }.onStart {
                dialogLoading?.postValue(true)
            }.onCompletion {
                dialogLoading?.postValue(false)
            }.collect{
                if(it.status == 200) {
                    chatListResponse?.postValue(it)
                }else{
                    errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
                }
            }
        }
    }

    fun unSendMessage(roomId: String, senderId: String, chatId: String){
        viewModelScope.launch {
            repository.unSendMessage(roomId, senderId, chatId).catch {
                errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
            }.onStart {
                dialogLoading?.postValue(true)
            }.onCompletion {
                dialogLoading?.postValue(false)
            }.collect{
                if(it.status == 200) {
                    unSendResponse?.postValue(it)
                }else{
                    errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
                }
            }
        }
    }

    fun uploadPhoto(photo: MultipartBody.Part){
        viewModelScope.launch {
            repository.uploadPhoto(photo).catch {
                errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
            }.onStart {
                dialogLoading?.postValue(true)
            }.onCompletion {
                dialogLoading?.postValue(false)
            }.collect{
                if(it.status == 200) {
                    uploadPhotoResponse.postValue(it)
                }else{
                    errorResponse?.postValue(ApiResponse.ErrorResponse("錯誤 : ${it.message}"))
                }
            }
        }
    }

}