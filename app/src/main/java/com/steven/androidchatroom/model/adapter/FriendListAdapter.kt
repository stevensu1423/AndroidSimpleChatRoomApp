package com.steven.androidchatroom.model.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.steven.androidchatroom.databinding.ItemFriendListBinding
import com.steven.androidchatroom.model.response.ApiResponse

class FriendListAdapter: RecyclerView.Adapter<FriendListAdapter.VH>() {

    private var mList: ArrayList<ApiResponse.FriendListData>? = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    override fun getItemCount(): Int {
        return mList?.size ?: 0
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val latestChat = if(mList?.get(position)?.latestChat?.isNotEmpty() == true){
            mList?.get(position)?.latestChat?.last()
        }else{
            ApiResponse.ChatData(message = "", time = "")
        }
        val unReadCount = findUnReadCount(mList?.get(position)?.latestChat ?: arrayListOf())
        holder.mBinding.tvName.text = mList?.get(position)?.friend?.name
        holder.mBinding.tvLatestMessage.text = latestChat?.message
        holder.mBinding.tvDate.text = latestChat?.time
        if(unReadCount > 0) {
            holder.mBinding.cardViewCount.visibility = View.VISIBLE
            holder.mBinding.tvUnreadCount.text = unReadCount.toString()
        }else{
            holder.mBinding.cardViewCount.visibility = View.INVISIBLE
        }

    }

    private fun findUnReadCount(data: ArrayList<ApiResponse.ChatData>): Int{
        return data.filter {
            it.isRead == false
        }.size
    }

    fun updateList(data: ArrayList<ApiResponse.FriendListData>){
        mList = data
        notifyDataSetChanged()
    }

    class VH(private val parent: ViewGroup, val mBinding: ItemFriendListBinding =  ItemFriendListBinding.inflate(
        LayoutInflater.from(parent.context), parent, false)): RecyclerView.ViewHolder(mBinding.root)
}