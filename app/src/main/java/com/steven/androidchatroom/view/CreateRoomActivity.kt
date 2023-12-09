package com.steven.androidchatroom.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.steven.androidchatroom.databinding.ActivityCreateRoomBinding
import com.steven.androidchatroom.model.WebSocketModel
import com.steven.androidchatroom.model.adapter.ChatAdapter
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.util.ItemDecoration
import com.steven.androidchatroom.util.toast
import com.steven.androidchatroom.viewModel.ChatViewModel
import com.steven.androidchatroom.web.ApiClient
import com.steven.androidchatroom.web.ApiInterface
import com.steven.androidchatroom.web.WebConfig
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Call
import retrofit2.Callback
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CreateRoomActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityCreateRoomBinding
    private val mViewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter
    lateinit var websocket: WebSocket
    private var roomId: String = ""
    private val address = WebConfig.WEBSOCKET_URL //todo 在這邊輸入server端webSocket的url
    private var name: String = ""
    private var memberId: String = ""
    private var friendName: String = ""
    private var friendId: String = ""
//    private val mApiClient = ApiClient()
//    private lateinit var api: ApiInterface

    var chatList: ArrayList<ApiResponse.ChatData> = arrayListOf()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCreateRoomBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
//        api = mApiClient.getRetrofit().create(ApiInterface::class.java)

        name = intent.getStringExtra("name") ?: ""
        memberId = intent.getStringExtra("memberId") ?: ""
        friendName = intent.getStringExtra("friendName") ?: ""
        friendId = intent.getStringExtra("friendId") ?: ""
        roomId = intent.getStringExtra("roomId") ?: ""

        mBinding.tvTitle.text = friendName

        initObserver()
        initListener()
        connectRoom()
        getChatList()
    }

    override fun onDestroy() {
        super.onDestroy()
        websocket.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initObserver(){
        mViewModel.chatListResponse.observe(this){
            chatList = it.data
            chatAdapter.updateData(chatList)
            scrollToBottom(mBinding.rvChat)
        }

        mViewModel.unSendResponse.observe(this){
            getChatList()
        }

        mViewModel.uploadPhotoResponse.observe(this){
            val id = setChatId()
            val message = Gson().toJson(WebSocketModel.MessageModel(roomId, memberId, name, friendId, "1", friendName, it.url ?: "", true, getDate()))
            websocket.send(message.toString())
            runOnUiThread {
                chatList.add(ApiResponse.ChatData(id = id, it.url, getDate(), memberId, true, isUnSend = false))
                chatAdapter.updateData(chatList)
                scrollToBottom(mBinding.rvChat)
            }
        }
    }

    private fun initListener(){
        chatAdapter = ChatAdapter(memberId, friendName, this)
        mBinding.rvChat.addItemDecoration(ItemDecoration(8, 8,8, 8))
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        mBinding.rvChat.layoutManager = layoutManager
        mBinding.rvChat.adapter = chatAdapter
        chatAdapter.setCallbackListener(callbackListener)
        mBinding.btImage.setOnClickListener{
            if(Build.VERSION.SDK_INT > 32){
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        mBinding.toolbar.setNavigationOnClickListener {
            finish()
        }

    }

    private var callbackListener: ChatAdapter.CallbackListener = object : ChatAdapter.CallbackListener{
        override fun onUnSendClick(data: ApiResponse.ChatData) {
            val message = WebSocketModel.MessageModel(roomId, memberId, friendId = friendId, type = "3", isUnsend = true)
            websocket.send(Gson().toJson(message))
            mViewModel.unSendMessage(roomId, memberId, data.id.toString())
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
        mViewModel.getChatList(roomId, friendId)
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
        selectImageLauncher.launch(intent)
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

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if(it){
            selectImage()
        }else{
            toast("需要選取照片的權限，請至設定開啟！")
        }
    }

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        Log.e("onResult", "$it")
        if(it.resultCode == Activity.RESULT_OK && it.data != null){
            val selectedImageUri: Uri = it.data?.data!!
            var path: String? = null

            path = if(Build.VERSION.SDK_INT < 29){
                getImagePath(selectedImageUri)!!
            }else if(Build.VERSION.SDK_INT < 32){
                getImagePath2(selectedImageUri)
            }else{
                getImagePath3(selectedImageUri)
            }

            if(!path.isNullOrEmpty()){
                val file = File(path)

                val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                val part: MultipartBody.Part = MultipartBody.Part.createFormData("photo", file.name, requestBody)

                mViewModel.uploadPhoto(part)
            }else{
                toast("無法傳送照片！")
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

    private fun getImagePath3(uri: Uri): String? {
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
}