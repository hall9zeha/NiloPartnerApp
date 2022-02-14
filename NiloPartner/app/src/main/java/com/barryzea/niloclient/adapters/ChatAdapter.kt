package com.barryzea.niloclient.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barryzea.niloclient.R
import com.barryzea.niloclient.chat.OnChatListener
import com.barryzea.niloclient.databinding.ItemChatBinding
import com.barryzea.niloclient.pojo.Message

class ChatAdapter(private val chatList:MutableList<Message>, private val listener:OnChatListener):RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    private lateinit var context:Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context=parent.context
        val view=LayoutInflater.from(context).inflate(R.layout.item_chat, parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int =chatList.size

    fun add(message: Message){
        if(!chatList.contains(message)){
            chatList.add(message)
            notifyItemInserted(chatList.size-1)
        }
    }
    fun update(message: Message){
        val index=chatList.indexOf(message)
        if(index != -1){
            chatList.set(index,message)
            notifyItemChanged(index)
        }
    }
    fun delete(message: Message){
        val index=chatList.indexOf(message)
        if(index != -1){
            chatList.remove(message)
            notifyItemRemoved(index)
        }
    }

    inner class ViewHolder(view:View):RecyclerView.ViewHolder(view){
        private val bind=ItemChatBinding.bind(view)

        fun setListener(message:Message){
            bind.tvMessage.setOnLongClickListener {
                listener.deleteMessage(message)
                true
            }
        }
    }
}