package com.steven.androidchatroom.web


import com.steven.androidchatroom.model.response.ApiResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {


    @Multipart
    @POST("photos")
    fun uploadPhoto(@Part photo: MultipartBody.Part): Call<ApiResponse.UploadPhotoResponse>

    @FormUrlEncoded
    @POST("getChatList")
    fun getChatList(@Field("roomId") roomId: String, @Field("senderId") friendId: String): Call<ApiResponse.ChatHistoryResponse?>

    @FormUrlEncoded
    @POST("friendConfirm")
    fun friendConfirm(@Field("memberId") memberId: String, @Field("requestId") requestId: String, @Field("myName") myName: String, @Field("requestName") requestName: String): Call<ApiResponse.FriendDataResponse>

    @FormUrlEncoded
    @POST("myFriends")
    fun getMyFriends(@Field("memberId") memberId: String): Call<ApiResponse.FriendDataResponse>

    @FormUrlEncoded
    @POST("myFriendRequest")
    fun getMyFriendRequest(@Field("memberId") memberId: String): Call<ApiResponse.FriendDataResponse>

    @FormUrlEncoded
    @POST("addFriend")
    fun addFriend(@Field("myMemberId") myMemberId: String, @Field("myName") myName: String, @Field("email") email: String): Call<ApiResponse.AddFriendResponse>

    @FormUrlEncoded
    @POST("login")
    fun login(@Field("email") email: String, @Field("password") password: String): Flow<ApiResponse.LoginResponse>

    @FormUrlEncoded
    @POST("register")
    fun register(@Field("email") email: String, @Field("password") password: String, @Field("userName") userName: String): Call<ApiResponse.RegisterResponse>

    @FormUrlEncoded
    @POST("unSendMessage")
    fun unSendMessage(@Field("roomId") roomId: String, @Field("senderId") friendId: String, @Field("chatId") chatId: String): Call<ApiResponse.AddFriendResponse?>



}