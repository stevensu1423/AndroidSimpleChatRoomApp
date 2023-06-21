package com.steven.androidchatroom.model.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.steven.androidchatroom.R
import com.steven.androidchatroom.model.response.ApiResponse
import org.w3c.dom.Text

class ChatAdapter(private var memberId: String, val friendName: String, val context: Context): RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    companion object{
        val ITEM_GET = 1
        val ITEM_SEND = 2
    }

    var dataList: ArrayList<ApiResponse.ChatData> ? = arrayListOf()

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         val message: TextView = itemView.findViewById(R.id.tv_msg)
         val time: TextView = itemView.findViewById(R.id.tv_time)
         val image: ImageView = itemView.findViewById(R.id.iv_img)
         val isRead: TextView = itemView.findViewById(R.id.tv_read)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view: View = if(viewType == ITEM_SEND){
            LayoutInflater.from(parent.context).inflate(R.layout.item_send, parent, false)
        }else{
            LayoutInflater.from(parent.context).inflate(R.layout.item_msg, parent, false)
        }
        return ChatViewHolder(view)
    }

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.time.text = dataList?.get(position)?.time.toString()
        if(dataList?.get(position)?.senderId == memberId){
            holder.message.text = "你 : "+dataList?.get(position)?.message.toString()
//            holder.isRead.visibility = if(dataList?.get(position)?.isRead == true){
//                View.VISIBLE
//            }else{
//                View.GONE
//            }
        }else{
//            holder.isRead.visibility = View.GONE
            holder.message.text = "$friendName : "+dataList?.get(position)?.message.toString()
        }
        if(dataList?.get(position)?.isImage == true){
            val url = dataList?.get(position)?.message
            holder.image.visibility = View.VISIBLE
            holder.message.visibility = View.GONE
            Glide.with(context).load(Uri.parse(url)).into(holder.image)
        }else{
            holder.image.visibility = View.GONE
            holder.message.visibility = View.VISIBLE
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(dataList?.get(position)?.senderId == memberId){
            ITEM_SEND
        }else{
            ITEM_GET
        }
    }

    override fun getItemCount(): Int {
        return dataList?.size ?: 0
    }

    fun updateData(data: ArrayList<ApiResponse.ChatData>){
        dataList = data
        notifyDataSetChanged()
    }

}