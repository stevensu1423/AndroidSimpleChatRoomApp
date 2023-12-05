package com.steven.androidchatroom.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.steven.androidchatroom.R
import com.steven.androidchatroom.databinding.FragmentChatBinding
import com.steven.androidchatroom.model.adapter.FriendAdapter
import com.steven.androidchatroom.model.adapter.FriendListAdapter
import com.steven.androidchatroom.model.response.ApiResponse
import com.steven.androidchatroom.util.ItemDecoration
import com.steven.androidchatroom.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private val mViewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentChatBinding? = null
    private val mBinding: FragmentChatBinding get() = _binding!!
    private val mAdapter: FriendListAdapter by lazy {
        FriendListAdapter()
    }

    private var mActivity: Activity? = null
    private var mContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as Activity
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(layoutInflater, container, false)
        // Inflate the layout for this fragment
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initListener()
        initObserver()
    }

    private fun initView(){
        mBinding.rvFriendList.layoutManager = LinearLayoutManager(mContext)
        mBinding.rvFriendList.addItemDecoration(ItemDecoration())
        mBinding.rvFriendList.adapter = mAdapter
    }
    private fun initListener(){
        mAdapter.setOnClickCallbackListener(onClickCallbackListener)
    }

    private fun initObserver(){
        mViewModel.friendListResponse?.observe(viewLifecycleOwner){
            var unReadCount = 0
            it.data.forEach {
                it.latestChat.forEach {
                    if(it.isRead == false && it.senderId != mViewModel.memberId.value)
                        unReadCount++
                }
            }
            mViewModel.friendChatUnreadCount.postValue(unReadCount)
            mAdapter.updateList(it.data)
        }
    }

    private val onClickCallbackListener =  object: FriendListAdapter.OnClickCallback{
        override fun onClick(data: ApiResponse.FriendListData) {
            val intent = Intent()
            intent.setClass(mActivity!!, CreateRoomActivity::class.java)
            intent.putExtra("name", mViewModel.userName.value.toString())
            intent.putExtra("memberId", mViewModel.memberId.value.toString())
            intent.putExtra("friendName", data.friend?.name)
            intent.putExtra("friendId", data.friend?.memberId)
            intent.putExtra("roomId", data.roomId)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
        if(isAdded)
            mViewModel.getMyFriendList()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}