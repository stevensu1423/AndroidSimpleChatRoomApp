package com.steven.androidchatroom.model

enum class ChatRoomStatus(var status: String){
    ENTER_ROOM_FIRST("0"),
    ENTER_ROOM_LAST("1"),
    MESSAGE_READ("2"),
    MESSAGE_UN_SEND("3"),
    MESSAGE_READ_ALL("4")
}