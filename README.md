# AndroidSimpleChatRoomApp
Android簡易聊天室的App端，你可以在[這裡](https://github.com/stevensu1423/AndroidSimpleChatRoomServer)找到server端的程式碼

## 功能介紹
一個簡單的Android聊天室App，能處理即時通訊、圖片上傳、登入、好友

## 修改server連結
至 **web**
加入**WebConfig.kt**
```kotlin
package com.steven.androidchatroom.web

object WebConfig {
    const val API_URL = "在這邊輸入server端的url" //範例: http://xxx.xxx.xxx.xxx:3000/chat
    const val WEBSOCKET_URL = "在這邊輸入server端webSocket的url" //範例: http://xxx.xxx.xxx.xxx:3000
}
```
## 注意事項
- 手機和Server需要連線相同的區域網路
- 圖片上傳需要在目錄中有 **`uploads`** 資料夾
