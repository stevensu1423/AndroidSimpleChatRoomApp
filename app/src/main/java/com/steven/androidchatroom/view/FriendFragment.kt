package com.steven.androidchatroom.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.steven.androidchatroom.R
import com.steven.androidchatroom.databinding.DialogFriendBinding
import com.steven.androidchatroom.databinding.DialogFriendConfirmBinding
import com.steven.androidchatroom.databinding.FragmentFriendBinding
import com.steven.androidchatroom.model.adapter.FriendAdapter
import com.steven.androidchatroom.model.adapter.FriendRequestListAdapter
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.util.ItemDecoration
import com.steven.androidchatroom.util.setLayoutSize
import com.steven.androidchatroom.viewModel.MainViewModel
import com.steven.androidchatroom.web.ApiClient
import com.steven.androidchatroom.web.ApiInterface
import dagger.hilt.android.AndroidEntryPoint
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
@AndroidEntryPoint
class FriendFragment : Fragment() {
    
    private var _binding: FragmentFriendBinding? = null
    private val mBinding: FragmentFriendBinding get() = _binding!!

    private val mViewModel: MainViewModel by activityViewModels()

    private var friendRequestAdapter = FriendRequestListAdapter()

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

        initObserver()
        initListener()
    }

    private fun initObserver(){
        mViewModel.friendRequestResponse?.observe(viewLifecycleOwner){
            friendRequestAdapter.updateList(it.data)
            mViewModel.friendRequestCount.postValue(it.data.size)
        }
        mViewModel.friendConfirmResponse?.observe(viewLifecycleOwner){
            it?.let {
                mViewModel.getMyFriendRequest()
                findNavController().popBackStack()
                mViewModel.friendConfirmResponse?.value = null
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun initListener() {
        mBinding.rvMyFriendRequest.layoutManager = LinearLayoutManager(mActivity)
        mBinding.rvMyFriendRequest.addItemDecoration(ItemDecoration(4, 4, 4, 4))
        mBinding.rvMyFriendRequest.adapter = friendRequestAdapter
        friendRequestAdapter.setCallbackListener(friendAdapterListener)
    }

//    private fun getMyFriends() {
//        api.getMyFriends(memberId).enqueue(object : Callback<ApiResponse.FriendDataResponse> {
//            override fun onResponse(
//                call: Call<ApiResponse.FriendDataResponse>,
//                response: Response<ApiResponse.FriendDataResponse>
//            ) {
//                if(response.body()?.status == 200){
//                    if(response.body()?.data != null) {
//                        friendAdapter.updateData(response.body()?.data!!)
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<ApiResponse.FriendDataResponse>, t: Throwable) {
//                Toast.makeText(mActivity, "error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//
//        })
//    }

    private fun showFriendConfirmDialog(id: String, callback: (data: String?) -> Unit): Dialog? {
        val binding = mActivity?.layoutInflater?.let { DialogFriendConfirmBinding.inflate(it) }
        val dialog = mActivity?.let { Dialog(it) }
        dialog?.setContentView(binding?.root!!)

        binding?.btYes?.setOnClickListener {
            dialog?.dismiss()
            callback(id)
        }
        binding?.btNo?.setOnClickListener {
            dialog?.dismiss()
            callback(null)
        }
        dialog?.setLayoutSize(mActivity!!, 0.15F, 0.75F)
        return dialog
    }

    override fun onResume() {
        super.onResume()
        if(isAdded)
            mViewModel.getMyFriendRequest()
    }

    private val friendAdapterListener: FriendRequestListAdapter.CallbackListener = object: FriendRequestListAdapter.CallbackListener{
        override fun onFriendClick(friendData: ApiResponse.FriendData) {
            if(friendData.mType == "request") {
                showFriendConfirmDialog(friendData.memberId!!){
                    mViewModel.friendConfirm(friendData.memberId, friendData.name.toString())
                }?.show()
            }
//            else{
//                val intent = Intent()
//                intent.putExtra("name", userName)
////                intent.putExtra("memberId", mActivity.memberId)
//                intent.putExtra("friendName", friendData.name)
//                intent.putExtra("friendId", friendData.memberId!!)
//                intent.putExtra("roomId", friendData.roomId)
//                mActivity?.let { intent.setClass(it, CreateRoomActivity::class.java) }
//                startActivity(intent)
//            }
        }
    }
}