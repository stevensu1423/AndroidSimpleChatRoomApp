package com.steven.androidchatroom.model.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.steven.androidchatroom.databinding.ItemFriendListBinding
import com.steven.androidchatroom.databinding.ItemFriendRequwstBinding
import com.steven.androidchatroom.model.response.ApiResponse

class FriendRequestListAdapter: RecyclerView.Adapter<FriendRequestListAdapter.VH>() {

    interface CallbackListener{
        fun onFriendClick(friendData: ApiResponse.FriendData)
    }

    private var mList: ArrayList<ApiResponse.FriendData>? = arrayListOf()
    private lateinit var listener: CallbackListener


    fun setCallbackListener(listener: CallbackListener){
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    override fun getItemCount(): Int {
        return mList?.size ?: 0
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.mBinding.tvName.text = mList?.get(position)?.name
        holder.itemView.setOnClickListener{
            listener.onFriendClick(mList?.get(position)!!)
        }
    }

    fun updateList(data: ArrayList<ApiResponse.FriendData>){
        mList = data
        notifyDataSetChanged()
    }

    class VH(private val parent: ViewGroup, val mBinding: ItemFriendRequwstBinding =  ItemFriendRequwstBinding.inflate(
        LayoutInflater.from(parent.context), parent, false)): RecyclerView.ViewHolder(mBinding.root)
}