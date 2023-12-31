package com.steven.androidchatroom.model.response


class ApiResponse {

    data class UploadPhotoResponse(
        val status: Int?,
        val message: String?,
        val url: String,
    )

    data class ChatHistoryResponse(
        val status: Int?,
        val message: String?,
        val data: ArrayList<ChatData>
    )
    data class ChatData(
        val id: String? = null,
        val message: String? = null,
        val time: String? = null,
        val senderId: String? = null,
        val isImage: Boolean? = false,
        val isRead: Boolean? = false,
        val isUnSend: Boolean? = false
    )


    data class AddFriendResponse(
        val status: Int?,
        val message: String
    )

    data class FriendDataResponse(
        val status: Int?,
        val message: String,
        val data: ArrayList<FriendData>
    )

    data class FriendData(
        val memberId: String?,
        val name: String?,
        val roomId: String,
        val mType: String?, //friend or request
    )

    data class ChatRoomData(
        val roomId: String,
        val hostName: String
    )

    data class ChatRoomResponse(
        val data: ArrayList<ChatRoomData>
    )

    data class CheckRoomResponse(
        val status: Int?,
        val message: String
    )

    data class LoginResponse(
        val status: Int?,
        val message: String,
        val userName: String? = null,
        val memberId: String? = null
    )

    data class RegisterResponse(
        val status: Int?,
        val message: String,
        val userName: String? = null,
        val memberId: String? = null
    )
}