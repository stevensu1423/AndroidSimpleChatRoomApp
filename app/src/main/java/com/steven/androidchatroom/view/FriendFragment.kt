package com.steven.androidchatroom.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.steven.androidchatroom.R
import com.steven.androidchatroom.databinding.DialogFriendBinding
import com.steven.androidchatroom.databinding.DialogFriendConfirmBinding
import com.steven.androidchatroom.databinding.FragmentFriendBinding
import com.steven.androidchatroom.model.adapter.FriendAdapter
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.web.ApiClient
import com.steven.androidchatroom.web.ApiInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendFragment : Fragment() {
    
    private var _binding: FragmentFriendBinding? = null
    private val mBinding: FragmentFriendBinding get() = _binding!!

    private var friendAdapter = FriendAdapter()
    private var friendRequestAdapter = FriendAdapter()

    private val mApiClient = ApiClient()
    private lateinit var api: ApiInterface
    private var userName = ""
    private var memberId = ""
    private var mContext: Context? = null
    private var mActivity: Activity? = null

    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendBinding.inflate(layoutInflater, container, false)
        // Inflate the layout for this fragment
        return mBinding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as Activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        api = mApiClient.getRetrofit().create(ApiInterface::class.java)

//        userName = mActivity.intent.getStringExtra("userName").toString() ?: ""
//        memberId = intent.getStringExtra("memberId") ?: ""


        mBinding.refresh.setOnClickListener {
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
        mBinding.rvMyFriends.layoutManager = LinearLayoutManager(mActivity)
        mBinding.rvMyFriends.addItemDecoration(ItemSpacingDecoration(4))
        mBinding.rvMyFriends.adapter = friendAdapter

        mBinding.rvMyFriendRequest.layoutManager = LinearLayoutManager(mActivity)
        mBinding.rvMyFriendRequest.addItemDecoration(ItemSpacingDecoration(4))
        mBinding.rvMyFriendRequest.adapter = friendRequestAdapter


        friendAdapter.setCallbackListener(friendAdapterListener)
        friendRequestAdapter.setCallbackListener(friendAdapterListener)

        mBinding.btAddFriend.setOnClickListener{
            showAskFriendDialog(object : DialogCallBack{
                override fun confirm(dialogInterface: DialogInterface, data: String) {
                    api.addFriend(memberId, userName, data).enqueue(object :
                        Callback<ApiResponse.AddFriendResponse> {
                        override fun onResponse(
                            call: Call<ApiResponse.AddFriendResponse>,
                            response: Response<ApiResponse.AddFriendResponse>
                        ) {
                            if(response.body()?.status == 200){
                                Toast.makeText(mActivity, "${response.body()?.message}", Toast.LENGTH_SHORT).show()
                                dialogInterface.dismiss()
                            }else{
                                Toast.makeText(mActivity, "${response.body()?.message}", Toast.LENGTH_SHORT).show()
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
        api.getMyFriends(memberId).enqueue(object : Callback<ApiResponse.FriendDataResponse> {
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
                Toast.makeText(mActivity, "error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getMyFriendRequest() {
        api.getMyFriendRequest(memberId).enqueue(object : Callback<ApiResponse.FriendDataResponse> {
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
                Toast.makeText(mActivity, "error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun showAskFriendDialog(callback: DialogCallBack): AlertDialog {
        val binding = mActivity?.layoutInflater?.let { DialogFriendBinding.inflate(it) }
        val dialog = AlertDialog.Builder(mActivity)
            .setCancelable(false)
            .setView(binding?.root)
            .create()
        binding?.btSend?.setOnClickListener {
            val data = binding.etFriendId.text.toString()
            if(data.isBlank()){
                Toast.makeText(mActivity, "請輸入好友稱呼", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            callback.confirm(dialog, data)
        }
        binding?.btCancel?.setOnClickListener {
            callback.cancel(dialog)
        }
        dialogSize(dialog)
        return dialog
    }

    private fun showFriendConfirmDialog(id: String, callback: DialogCallBack): AlertDialog {
        val binding = mActivity?.layoutInflater?.let { DialogFriendConfirmBinding.inflate(it) }
        val dialog = AlertDialog.Builder(mActivity)
            .setCancelable(false)
            .setView(binding?.root)
            .create()
        binding?.btYes?.setOnClickListener {
            callback.confirm(dialog, id)
        }
        binding?.btNo?.setOnClickListener {
            callback.cancel(dialog)
        }
        dialogSize(dialog)
        return dialog
    }

    private fun dialogSize(dialog: AlertDialog) {
        val displayMetrics = DisplayMetrics()
        mActivity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
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
                                        Toast.makeText(mActivity, "OK", Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        Toast.makeText(
                                            mActivity,
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
                                        mActivity,
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
//                intent.putExtra("memberId", mActivity.memberId)
                intent.putExtra("friendName", friendData.name)
                intent.putExtra("friendId", friendData.memberId!!)
                intent.putExtra("roomId", friendData.roomId)
                mActivity?.let { intent.setClass(it, CreateRoomActivity::class.java) }
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