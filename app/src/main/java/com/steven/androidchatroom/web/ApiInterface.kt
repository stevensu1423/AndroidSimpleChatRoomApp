package com.steven.androidchatroom.web


import com.steven.androidchatroom.model.response.ApiResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiInterface {


    @Multipart
    @POST("photos")
    suspend fun uploadPhoto(@Part photo: MultipartBody.Part): ApiResponse.UploadPhotoResponse

    @FormUrlEncoded
    @POST("getChatList")
    suspend fun getChatList(@Field("roomId") roomId: String, @Field("senderId") friendId: String): ApiResponse.ChatHistoryResponse

    @FormUrlEncoded
    @POST("friendConfirm")
    suspend fun friendConfirm(@Field("memberId") memberId: String, @Field("requestId") requestId: String, @Field("myName") myName: String, @Field("requestName") requestName: String): ApiResponse.AddFriendResponse

    @FormUrlEncoded
    @POST("myFriends")
    suspend fun getMyFriends(@Field("memberId") memberId: String): ApiResponse.FriendListResponse

    @FormUrlEncoded
    @POST("myFriendRequest")
    suspend fun getMyFriendRequest(@Field("memberId") memberId: String): ApiResponse.FriendDataResponse

    @FormUrlEncoded
    @POST("addFriend")
    suspend fun addFriendRequest(@Field("myMemberId") myMemberId: String, @Field("myName") myName: String, @Field("email") email: String): ApiResponse.AddFriendResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(@Field("email") email: String, @Field("password") password: String): ApiResponse.LoginResponse

//    @FormUrlEncoded
//    @POST("register")
//    fun register(@Field("email") email: String, @Field("password") password: String, @Field("userName") userName: String): Call<ApiResponse.RegisterResponse>

    @FormUrlEncoded
    @POST("register")
    suspend fun register(@Field("email") email: String, @Field("password") password: String, @Field("userName") userName: String): ApiResponse.RegisterResponse

//    @FormUrlEncoded
//    @POST("unSendMessage")
//    fun unSendMessage(@Field("roomId") roomId: String, @Field("senderId") friendId: String, @Field("chatId") chatId: String): Call<ApiResponse.AddFriendResponse?>

    @FormUrlEncoded
    @POST("unSendMessage")
    suspend fun unSendMessage(@Field("roomId") roomId: String, @Field("senderId") senderId: String, @Field("chatId") chatId: String): ApiResponse.AddFriendResponse

    @FormUrlEncoded
    @POST("updateFcmToken")
    suspend fun updateFcmToken(@Field("memberId") memberId: String, @Field("fcmToken") fcmToken: String): ApiResponse.FcmTokenResponse

}