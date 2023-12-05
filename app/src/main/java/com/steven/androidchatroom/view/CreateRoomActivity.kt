package com.steven.androidchatroom.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.steven.androidchatroom.R
import com.steven.androidchatroom.databinding.ActivityCreateRoomBinding
import com.steven.androidchatroom.model.WebSocketModel
import com.steven.androidchatroom.model.adapter.ChatAdapter
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.web.ApiClient
import com.steven.androidchatroom.web.ApiInterface
import com.steven.androidchatroom.web.WebConfig
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

@AndroidEntryPoint
class CreateRoomActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityCreateRoomBinding
    private val PICK_IMAGE_REQUEST = 9999
    private val PERMISSON_STORAGE = 9998
    private lateinit var chatAdapter: ChatAdapter
    lateinit var websocket: WebSocket
    private var roomId: String = ""
    private val address = WebConfig.WEBSOCKET_URL //todo 在這邊輸入server端webSocket的url
    private var name: String = ""
    private var memberId: String = ""
    private var friendName: String = ""
    private var friendId: String = ""
    private val mApiClient = ApiClient()
    private lateinit var api: ApiInterface

    var chatList: ArrayList<ApiResponse.ChatData> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCreateRoomBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        api = mApiClient.getRetrofit().create(ApiInterface::class.java)

        name = intent.getStringExtra("name") ?: ""
        memberId = intent.getStringExtra("memberId") ?: ""
        friendName = intent.getStringExtra("friendName") ?: ""
        friendId = intent.getStringExtra("friendId") ?: ""
        roomId = intent.getStringExtra("roomId") ?: ""

        mBinding.tvTitle.text = friendName

        connectRoom()
        initListener()
        getChatList()
    }

    override fun onDestroy() {
        super.onDestroy()
        websocket.cancel()
    }

    private fun initListener(){
        chatAdapter = ChatAdapter(memberId, friendName, this@CreateRoomActivity)
        mBinding.rvChat.addItemDecoration(ItemSpacingDecoration(8))
        mBinding.rvChat.layoutManager = LinearLayoutManager(this@CreateRoomActivity)
        mBinding.rvChat.adapter = chatAdapter
        chatAdapter.setCallbackListener(callbackListener)
        mBinding.btImage.setOnClickListener{
            checkPermission()
        }
        mBinding.toolbar.setNavigationOnClickListener {
            finish()
        }

    }

    private var callbackListener: ChatAdapter.CallbackListener = object : ChatAdapter.CallbackListener{
        override fun onUnSendClick(data: ApiResponse.ChatData) {
            val message = WebSocketModel.MessageModel(roomId, memberId, friendId = friendId, type = "3", isUnsend = true)
            websocket.send(Gson().toJson(message))

            api.unSendMessage(roomId, memberId, data.id.toString()).enqueue(object : Callback<ApiResponse.AddFriendResponse?>{
                override fun onResponse(
                    call: Call<ApiResponse.AddFriendResponse?>,
                    response: retrofit2.Response<ApiResponse.AddFriendResponse?>
                ) {
                    getChatList()
                }

                override fun onFailure(call: Call<ApiResponse.AddFriendResponse?>, t: Throwable) {
                }
            })
        }
    }

    private fun connectRoom(){

        val client = OkHttpClient()
        val request = Request.Builder().url(address).build()
        websocket = client.newWebSocket(request, object: WebSocketListener(){
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                val json = Gson().fromJson(text, WebSocketModel.MessageReceiveModel::class.java)

                if(json.type == "2" || json.type == "3"){
                    getChatList()
                }else {
                    val data = WebSocketModel.MessageModel(roomId, memberId, name, friendId, "2", friendName, "read", false, getDate())
                    val message = Gson().toJson(data)
                    websocket.send(message)
                    runOnUiThread {
                        chatList.add(
                            ApiResponse.ChatData(
                                json.id,
                                json.message,
                                getDate(),
                                json.senderId,
                                json.isImage,
                                json.isUnSend
                            )
                        )
                        chatAdapter.updateData(chatList)
                        scrollToBottom(mBinding.rvChat)
                    }
                }

            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                val data = WebSocketModel.MessageModel(roomId, memberId, name, friendId, "0", friendName, "", false, getDate())
                val message = Gson().toJson(data)
                websocket.send(message.toString())

                runOnUiThread {
                    mBinding.btSend.setOnClickListener {
                        val msg = mBinding.editText.text.toString()
                        val id = setChatId()
                        val imm: InputMethodManager =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(mBinding.editText.windowToken, 0)
                        mBinding.editText.text?.clear()
                        if(msg.isBlank()){
                            Toast.makeText(this@CreateRoomActivity, "請輸入訊息", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        websocket.send(Gson().toJson(WebSocketModel.MessageModel(roomId, memberId, name, friendId, "1", friendName, msg, false, time = getDate(), id = id, isUnsend = false)))
                        runOnUiThread {
                            chatList.add(ApiResponse.ChatData(id = id, msg,getDate(), memberId, isUnSend = false))
                            chatAdapter.updateData(chatList)
                            scrollToBottom(mBinding.rvChat)
                        }
                    }
                }
            }
        })

    }

    private fun getChatList(){
        api.getChatList(roomId, friendId).enqueue(object : Callback<ApiResponse.ChatHistoryResponse?>{
            override fun onResponse(
                call: Call<ApiResponse.ChatHistoryResponse?>,
                response: retrofit2.Response<ApiResponse.ChatHistoryResponse?>
            ) {
                if(response.body()?.status == 200){
                    chatList = response.body()?.data!!
                    chatAdapter.updateData(chatList)
                    scrollToBottom(mBinding.rvChat)
                }
            }

            override fun onFailure(call: Call<ApiResponse.ChatHistoryResponse?>, t: Throwable) {
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(): String {
        val currentDateTime: LocalDateTime = LocalDateTime.now()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

        return currentDateTime.format(formatter)
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun scrollToBottom(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
        val adapter = chatAdapter
        val lastItemPosition = adapter.itemCount - 1
        layoutManager!!.scrollToPositionWithOffset(lastItemPosition, 0)
        recyclerView.post {
            val target = layoutManager.findViewByPosition(lastItemPosition)
            if (target != null) {
                val offset = recyclerView.measuredHeight - target.measuredHeight
                layoutManager.scrollToPositionWithOffset(lastItemPosition, offset)
            }
        }
    }

    class ItemSpacingDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.left = spacing
            outRect.right = spacing
            outRect.top = spacing
            outRect.bottom = spacing
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSON_STORAGE)
        } else {
            selectImage()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSON_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            selectImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null){
            val selectedImageUri: Uri = data.data!!
            var path: String? = null

            path = if(Build.VERSION.SDK_INT < 29){
                getImagePath(selectedImageUri)!!
            }else{
                getImagePath2(selectedImageUri)
            }

            if(!path.isNullOrEmpty()){
                val file = File(path)

                val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                val part: MultipartBody.Part = MultipartBody.Part.createFormData("photo", file.name, requestBody)

                api.uploadPhoto(part).enqueue(object: Callback<ApiResponse.UploadPhotoResponse>{
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(
                        call: Call<ApiResponse.UploadPhotoResponse>,
                        response: retrofit2.Response<ApiResponse.UploadPhotoResponse>
                    ) {
                        if(response.body()?.status == 200){
                            val id = setChatId()
                            val message = Gson().toJson(WebSocketModel.MessageModel(roomId, memberId, name, friendId, "1", friendName, response.body()?.url ?: "", true, getDate()))
                            websocket.send(message.toString())
                            runOnUiThread {
                                chatList.add(ApiResponse.ChatData(id = id, response.body()?.url,getDate(), memberId, true, isUnSend = false))
                                chatAdapter.updateData(chatList)
                                scrollToBottom(mBinding.rvChat)
                            }
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse.UploadPhotoResponse>, t: Throwable) {
                        Toast.makeText(this@CreateRoomActivity, "error ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    private fun getImagePath(uri: Uri): String? {
        var path: String? = null
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path
    }

    private fun setChatId(): String{
        return "${memberId}${friendId}${System.currentTimeMillis()}"
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun getImagePath2(uri: Uri): String? {
        var path: String? = null
        val cursor: Cursor? = contentResolver.query(
            uri,
            arrayOf(MediaStore.Images.Media.DISPLAY_NAME),
            null,
            null,
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val fileName: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val selection = MediaStore.Images.Media.DISPLAY_NAME + "=?"
                val selectionArgs = arrayOf(fileName)
                val filePathCursor: Cursor? =
                    contentResolver.query(contentUri, null, selection, selectionArgs, null)
                if (filePathCursor != null) {
                    if (filePathCursor.moveToFirst()) {
                        path =
                            filePathCursor.getString(filePathCursor.getColumnIndex(MediaStore.Images.Media.DATA))
                    }
                    filePathCursor.close()
                }
            }
            cursor.close()
        }
        return path
    }
}