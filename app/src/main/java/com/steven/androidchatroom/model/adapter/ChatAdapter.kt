package com.steven.androidchatroom.model.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.steven.androidchatroom.R
import com.steven.androidchatroom.model.ChatRoomStatus
import com.steven.androidchatroom.model.response.ApiResponse
import org.w3c.dom.Text

class ChatAdapter(private var memberId: String, val friendName: String, val context: Context): RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    companion object{
        val ITEM_GET = 1
        val ITEM_SEND = 2
    }

    var dataList: ArrayList<ApiResponse.ChatData> ? = arrayListOf()

    private lateinit var listener: CallbackListener

    interface CallbackListener{
        fun onUnSendClick(data: ApiResponse.ChatData)
    }

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

        if(dataList?.get(position)?.isUnSend == true){
            holder.isRead.visibility = View.GONE
            holder.time.visibility = View.GONE
            holder.message.text = "此訊息已被收回"
            holder.itemView.setOnLongClickListener(null)
        }else {
            holder.time.visibility = View.VISIBLE

            holder.time.text = dataList?.get(position)?.time.toString()
            if (dataList?.get(position)?.senderId == memberId) {
                holder.message.text = "你 : " + dataList?.get(position)?.message.toString()
                holder.isRead.visibility = if (dataList?.get(position)?.isRead == true) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                holder.itemView.setOnLongClickListener {
                    val popupMenu = PopupMenu(holder.itemView.context, holder.itemView)
                    val inflater = popupMenu.menuInflater
                    inflater.inflate(R.menu.menu, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.unSend -> {
                                listener.onUnSendClick(dataList?.get(position)!!)
                                true
                            }
                            else -> {
                                false
                            }
                        }
                    }
                    popupMenu.show()
                    true
                }
            } else {
                holder.isRead.visibility = View.GONE
                holder.message.text = "$friendName : " + dataList?.get(position)?.message.toString()
            }
            if (dataList?.get(position)?.isImage == true) {
                val url = dataList?.get(position)?.message
                holder.image.visibility = View.VISIBLE
                holder.message.visibility = View.GONE
                Glide.with(context).load(Uri.parse(url)).into(holder.image)
            } else {
                holder.image.visibility = View.GONE
                holder.message.visibility = View.VISIBLE
            }
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

    fun updateSendData(data: ApiResponse.ChatData){
        dataList?.add(data)
        notifyItemInserted(dataList?.lastIndex ?: 0)
    }

    fun updateSingleData(chatId: String?, type: String){
        if(type == ChatRoomStatus.MESSAGE_READ_ALL.status){
            dataList?.forEachIndexed { index, chatData ->
                if(chatData.isRead == false){
                    chatData.isRead = true
                }
            }
        }
        dataList.apply {
            this?.indexOfLast {
                it.id == chatId
            }.let {
                when(type){
                    ChatRoomStatus.MESSAGE_UN_SEND.status -> {
                        this?.get(it ?: 0)?.isUnSend = true
                        notifyItemChanged(it ?: 0)
                    }
                    ChatRoomStatus.MESSAGE_READ.status -> {
                        this?.get(it ?: 0)?.isRead = true
                        notifyItemChanged(it ?: 0)
                    }
                }
            }
        }
    }

    fun setCallbackListener(callbackListener: CallbackListener){
        this.listener = callbackListener
    }
}