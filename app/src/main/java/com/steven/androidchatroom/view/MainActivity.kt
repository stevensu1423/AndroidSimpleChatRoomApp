package com.steven.androidchatroom.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.steven.androidchatroom.R
import com.steven.androidchatroom.web.ApiClient
import com.steven.androidchatroom.databinding.ActivityMainBinding
import com.steven.androidchatroom.model.adapter.FriendAdapter
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.web.ApiInterface
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_friend.view.*
import kotlinx.android.synthetic.main.dialog_friend_confirm.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private var friendAdapter = FriendAdapter()
    private var friendRequestAdapter = FriendAdapter()
    private val mApiClient = ApiClient()
    private lateinit var api: ApiInterface
    private var userName = ""
    private var memberId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        api = mApiClient.getRetrofit().create(ApiInterface::class.java)

        userName = intent.getStringExtra("userName").toString() ?: ""
        memberId = intent.getStringExtra("memberId") ?: ""


        refresh.setOnClickListener {
            refresh()
        }
        initListener()
        refresh()

    }

    private fun refresh(){
        getMyFriends()
        getMyFriendRequest()

    }

    @SuppressLint("SetTextI18n")
    private fun initListener() {
        mBinding.rvMyFriends.layoutManager = LinearLayoutManager(this@MainActivity)
        mBinding.rvMyFriends.addItemDecoration(ItemSpacingDecoration(4))
        mBinding.rvMyFriends.adapter = friendAdapter

        mBinding.rvMyFriendRequest.layoutManager = LinearLayoutManager(this@MainActivity)
        mBinding.rvMyFriendRequest.addItemDecoration(ItemSpacingDecoration(4))
        mBinding.rvMyFriendRequest.adapter = friendRequestAdapter


        friendAdapter.setCallbackListener(friendAdapterListener)
        friendRequestAdapter.setCallbackListener(friendAdapterListener)

        mBinding.btAddFriend.setOnClickListener{
            showAskFriendDialog(object : DialogCallBack{
                override fun confirm(dialogInterface: DialogInterface, data: String) {
                    api.addFriend(memberId, userName, data).enqueue(object : Callback<ApiResponse.AddFriendResponse>{
                        override fun onResponse(
                            call: Call<ApiResponse.AddFriendResponse>,
                            response: Response<ApiResponse.AddFriendResponse>
                        ) {
                            if(response.body()?.status == 200){
                                Toast.makeText(this@MainActivity, "${response.body()?.message}", Toast.LENGTH_SHORT).show()
                                dialogInterface.dismiss()
                            }else{
                                Toast.makeText(this@MainActivity, "${response.body()?.message}", Toast.LENGTH_SHORT).show()
                            }
                            refresh()
                        }

                        override fun onFailure(
                            call: Call<ApiResponse.AddFriendResponse>,
                            t: Throwable
                        ) {
                        }

                    })
                }

                override fun cancel(dialogInterface: DialogInterface) {
                    dialogInterface.dismiss()
                }
            }).show()
        }


    }

    private fun getMyFriends() {
        api.getMyFriends(memberId).enqueue(object : Callback<ApiResponse.FriendDataResponse>{
            override fun onResponse(
                call: Call<ApiResponse.FriendDataResponse>,
                response: Response<ApiResponse.FriendDataResponse>
            ) {
                if(response.body()?.status == 200){
                    if(response.body()?.data != null) {
                        friendAdapter.updateData(response.body()?.data!!)
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse.FriendDataResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getMyFriendRequest() {
        api.getMyFriendRequest(memberId).enqueue(object : Callback<ApiResponse.FriendDataResponse>{
            override fun onResponse(
                call: Call<ApiResponse.FriendDataResponse>,
                response: Response<ApiResponse.FriendDataResponse>
            ) {
                if(response.body()?.status == 200){
                    if(response.body()?.data != null)
                        friendRequestAdapter.updateData(response.body()?.data!!)
                }
            }

            override fun onFailure(call: Call<ApiResponse.FriendDataResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun showAskFriendDialog(callback: DialogCallBack): AlertDialog{
        val view = LayoutInflater.from(this@MainActivity).inflate(R.layout.dialog_friend, null)
        val dialog = AlertDialog.Builder(this@MainActivity)
            .setCancelable(false)
            .setView(view)
            .create()
        view.run {
            this.bt_send.setOnClickListener{
                val data = this.et_friend_id.text.toString()
                if(data.isBlank()){
                    Toast.makeText(this@MainActivity, "請輸入好友稱呼", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                callback.confirm(dialog, data)
            }
            this.bt_cancel.setOnClickListener{
                callback.cancel(dialog)
            }
        }
        dialogSize(dialog)
        return dialog
    }

    private fun showFriendConfirmDialog(id: String, callback: DialogCallBack): AlertDialog{
        val view = LayoutInflater.from(this@MainActivity).inflate(R.layout.dialog_friend_confirm, null)
        val dialog = AlertDialog.Builder(this@MainActivity)
            .setCancelable(false)
            .setView(view)
            .create()
        view.run {
            this.bt_yes.setOnClickListener{
                callback.confirm(dialog, id)
            }
            this.bt_no.setOnClickListener{
                callback.cancel(dialog)
            }
        }
        dialogSize(dialog)
        return dialog
    }

    private fun dialogSize(dialog: AlertDialog) {
        val displayMetrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth: Int = displayMetrics.widthPixels
        val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window!!.attributes)
        val dialogWindowWidth = (displayWidth * 0.85f).toInt()
        layoutParams.width = dialogWindowWidth
        dialog.window!!.attributes = layoutParams
    }

    interface DialogCallBack{
        fun confirm(dialogInterface: DialogInterface, data: String)
        fun cancel(dialogInterface: DialogInterface)
    }

    private val friendAdapterListener: FriendAdapter.CallbackListener = object: FriendAdapter.CallbackListener{
        override fun onFriendClick(friendData: ApiResponse.FriendData) {
            if(friendData.mType == "request") {
                showFriendConfirmDialog(friendData.memberId!!, object : DialogCallBack {
                    override fun confirm(dialogInterface: DialogInterface, data: String) {
                        api.friendConfirm(memberId, friendData.memberId, userName, friendData.name.toString())
                            .enqueue(object : Callback<ApiResponse.FriendDataResponse> {
                                override fun onResponse(
                                    call: Call<ApiResponse.FriendDataResponse>,
                                    response: Response<ApiResponse.FriendDataResponse>
                                ) {
                                    if (response.body()?.status == 200) {
                                        Toast.makeText(this@MainActivity, "OK", Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "error",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    refresh()
                                    dialogInterface.dismiss()
                                }

                                override fun onFailure(
                                    call: Call<ApiResponse.FriendDataResponse>,
                                    t: Throwable
                                ) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "error : ${t.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dialogInterface.dismiss()
                                }
                            })
                    }

                    override fun cancel(dialogInterface: DialogInterface) {
                        dialogInterface.dismiss()
                    }
                }).show()
            }else{
                val intent = Intent()
                intent.putExtra("name", userName)
                intent.putExtra("memberId", this@MainActivity.memberId)
                intent.putExtra("friendName", friendData.name)
                intent.putExtra("friendId", friendData.memberId!!)
                intent.putExtra("roomId", friendData.roomId)
                intent.setClass(this@MainActivity, CreateRoomActivity::class.java)
                startActivity(intent)
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
}