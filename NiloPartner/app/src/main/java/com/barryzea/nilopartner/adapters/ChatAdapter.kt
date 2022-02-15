package com.barryzea.nilopartner.adapters

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.barryzea.nilopartner.R
import com.barryzea.nilopartner.databinding.ItemChatBinding
import com.barryzea.nilopartner.interfaces.OnChatListener
import com.barryzea.nilopartner.pojo.Message

class ChatAdapter(private val chatList:MutableList<Message>, private val listener: OnChatListener):
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context=parent.context
        val view= LayoutInflater.from(context).inflate(R.layout.item_chat, parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message=chatList[position]
        holder.setListener(message)

        // var gravity=Gravity.END
        val marginHorizontal=context.resources.getDimensionPixelSize(R.dimen.chat_margin_horizontal)

        var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.gravity= Gravity.END
        params.marginStart=marginHorizontal
        params.marginEnd=0
        params.topMargin=0

        //separación superior entre burbujas de dialog chat
        if(position>0 && message.isSenderByMe() != chatList[position -1].isSenderByMe()){
            params.topMargin=context.resources.getDimensionPixelSize(R.dimen.common_padding_min)
        }

        var background= ContextCompat.getDrawable(context, R.drawable.background_chat_support)
        var textColor= ContextCompat.getColor(context, R.color.colorOnPrimary)

        if(!message.isSenderByMe()){
            //gravity=Gravity.START
            params.gravity= Gravity.START
            background= ContextCompat.getDrawable(context, R.drawable.background_chat_client)
            textColor= ContextCompat.getColor(context, R.color.colorOnSecondary)
            params.marginStart=0
            params.marginEnd=marginHorizontal
        }

        // holder.bind.root.gravity=gravity
        holder.bind.tvMessage.layoutParams=params
        holder.bind.tvMessage.background=background
        holder.bind.tvMessage.setTextColor(textColor)

        holder.bind.tvMessage.text=message.message
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

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val bind= ItemChatBinding.bind(view)

        fun setListener(message:Message){
            bind.tvMessage.setOnLongClickListener {
                listener.deleteMessage(message)
                true
            }
        }
    }
}