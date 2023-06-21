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
import com.steven.androidchatroom.R
import com.steven.androidchatroom.model.adapter.ChatAdapter
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.web.ApiClient
import com.steven.androidchatroom.web.ApiInterface
import kotlinx.android.synthetic.main.activity_create_room.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CreateRoomActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 9999
    private val PERMISSON_STORAGE = 9998
    private lateinit var chatAdapter: ChatAdapter
    lateinit var websocket: WebSocket
    private var roomId: String = ""
    private val address = "在這邊輸入server端webSocket的url" //todo 在這邊輸入server端webSocket的url
    private var name: String = ""
    private var memberId: String = ""
    private var friendName: String = ""
    private var friendId: String = ""
    private val mApiClient = ApiClient()
    private lateinit var api: ApiInterface

    var chatList: ArrayList<ApiResponse.ChatData> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)
        api = mApiClient.getRetrofit().create(ApiInterface::class.java)

        name = if(intent.hasExtra("name")){
            intent.getStringExtra("name").toString()
        }else{
            ""
        }

        memberId = if(intent.hasExtra("memberId")){
            intent.getStringExtra("memberId").toString()
        }else{
            ""
        }

        friendName = if(intent.hasExtra("friendName")){
            intent.getStringExtra("friendName").toString()
        }else{
            ""
        }

        friendId = if(intent.hasExtra("friendId")){
            intent.getStringExtra("friendId").toString()
        }else{
            ""
        }

        roomId = if(intent.hasExtra("roomId")){
            intent.getStringExtra("roomId").toString()
        }else{
            ""
        }
        connectRoom()
        initListener()
        getChatList()
    }

    override fun onDestroy() {
        super.onDestroy()
        websocket.cancel()
    }

    private fun initListener(){
        editText.isEnabled = false
        chatAdapter = ChatAdapter(memberId, friendName, this@CreateRoomActivity)
        rv_chat.addItemDecoration(ItemSpacingDecoration(8))
        rv_chat.layoutManager = LinearLayoutManager(this@CreateRoomActivity)
        rv_chat.adapter = chatAdapter

        bt_image.setOnClickListener{
            checkPermission()
        }

    }

    private fun connectRoom(){

        val client = OkHttpClient()
        val request = Request.Builder().url(address).build()
        websocket = client.newWebSocket(request, object: WebSocketListener(){
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                val json = JSONObject(text)

                if(json.get("type") == 2){
                    getChatList()
                }else {
                    val jsonString = JSONObject()
                    jsonString.put("roomId", roomId)
                    jsonString.put("memberId", memberId)
                    jsonString.put("name", name)
                    jsonString.put("friendId", friendId)
                    jsonString.put("type", 2)
                    jsonString.put("friendName", friendName)
                    jsonString.put("message", "read")
                    jsonString.put("isImage", false)
                    jsonString.put("time", getDate())
                    websocket.send(jsonString.toString())

                    runOnUiThread {
                        chatList.add(
                            ApiResponse.ChatData(
                                json.get("message").toString(),
                                getDate(),
                                json.get("senderId").toString(),
                                json.get("isImage") == true
                            )
                        )
                        chatAdapter.updateData(chatList)
                        scrollToBottom(rv_chat)
                    }
                }

            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                val jsonString = JSONObject()
                jsonString.put("roomId", roomId)
                jsonString.put("memberId", memberId)
                jsonString.put("name", name)
                jsonString.put("type", 0)
                jsonString.put("friendId", friendId)
                jsonString.put("friendName", friendName)
                jsonString.put("isImage", false)
                jsonString.put("time", getDate())
                websocket.send(jsonString.toString())

                runOnUiThread {
                    editText.isEnabled = true
                    chatList.add(ApiResponse.ChatData(  "連線成功", getDate(), memberId))
                    chatAdapter.updateData(chatList)
                    scrollToBottom(rv_chat)
                }
                runOnUiThread {
                    bt_send.setOnClickListener {
                        val msg = editText.text.toString()

                        val imm: InputMethodManager =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(editText.windowToken, 0)
                        editText.text.clear()
                        if(msg.isBlank()){
                            Toast.makeText(this@CreateRoomActivity, "請輸入訊息", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        val json = JSONObject()
                        json.put("roomId", roomId)
                        json.put("memberId", memberId)
                        json.put("name", name)
                        json.put("friendId", friendId)
                        json.put("type", 1)
                        json.put("friendName", friendName)
                        json.put("message", msg)
                        json.put("isImage", false)
                        json.put("time", getDate())
                        websocket.send(json.toString())
                        runOnUiThread {
                            chatList.add(ApiResponse.ChatData(msg,getDate(), memberId))
                            chatAdapter.updateData(chatList)
                            scrollToBottom(rv_chat)
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
                    scrollToBottom(rv_chat)
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
        val lastItemPosition = adapter!!.itemCount - 1
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
                getImagePath2(selectedImageUri)!!
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
                            val json = JSONObject()
                            json.put("roomId", roomId)
                            json.put("memberId", memberId)
                            json.put("name", name)
                            json.put("friendId", friendId)
                            json.put("type", 1)
                            json.put("friendName", friendName)
                            json.put("message", response.body()?.url)
                            json.put("isImage", true)
                            json.put("time", getDate())
                            websocket.send(json.toString())
                            runOnUiThread {
                                chatList.add(ApiResponse.ChatData(response.body()?.url,getDate(), memberId, true))
                                chatAdapter.updateData(chatList)
                                scrollToBottom(rv_chat)
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