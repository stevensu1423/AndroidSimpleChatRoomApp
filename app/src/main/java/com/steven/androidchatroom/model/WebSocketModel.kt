package com.steven.androidchatroom.model

class WebSocketModel {

    data class MessageModel(
        val roomId: String = "",
        val memberId: String = "",
        val name: String = "",
        val friendId: String = "",
        val type: String = "",
        val friendName: String = "",
        val message: String = "",
        val isImage: Boolean = false,
        val time: String = "",
        val chatId: String = "",
        val isUnsend: Boolean = false,
        val id: String = ""
    )

    data class MessageReceiveModel(
        val id: String = "",
        val type: String = "",
        val message: String = "",
        val senderId: String = "",
        val isImage: Boolean = false,
        val isUnSend: Boolean = false
    )
}