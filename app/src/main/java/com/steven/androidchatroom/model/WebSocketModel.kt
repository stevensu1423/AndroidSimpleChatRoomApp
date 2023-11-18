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
        val time: String = ""
    )

    data class MessageReceiveModel(
        val type: String = "",
        val message: String = "",
        val senderId: String = "",
        val isImage: Boolean = false
    )
}