package com.steven.androidchatroom.model.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.steven.androidchatroom.R
import com.steven.androidchatroom.model.response.ApiResponse

class FriendAdapter: RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    var dataList: ArrayList<ApiResponse.FriendData> ? = arrayListOf()

    private lateinit var listener: CallbackListener

    fun setCallbackListener(listener: CallbackListener){
        this.listener = listener
    }

    interface CallbackListener{
        fun onFriendClick(friendData: ApiResponse.FriendData)
    }

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         val message: TextView = itemView.findViewById(R.id.tv_msg)
         val time: TextView = itemView.findViewById(R.id.tv_time)
    }

    fun updateData(data: ArrayList<ApiResponse.FriendData>){
        dataList = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_msg, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.message.text = dataList?.get(position)?.name.toString()
        holder.time.visibility = View.INVISIBLE
        holder.itemView.setOnClickListener{
            listener.onFriendClick(dataList?.get(position)!!)
        }
    }

    override fun getItemCount(): Int {
        return dataList?.size ?: 0
    }

}