package com.barryzea.niloclient.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.barryzea.niloclient.adapters.ChatAdapter
import com.barryzea.niloclient.databinding.FragmentChatBinding
import com.barryzea.niloclient.interfaces.OrderAux
import com.barryzea.niloclient.pojo.Message
import com.barryzea.niloclient.pojo.Order

class ChatFragment: Fragment(), OnChatListener {
    private var bind:FragmentChatBinding?=null
    private lateinit var adapter:ChatAdapter
    private var order: Order?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind= FragmentChatBinding.inflate(inflater, container, false)
        bind?.let{
            return it.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getOrder()
    }

    private fun getOrder() {
        order=(activity as? OrderAux)?.getOrderSelected()
        order?.let{
            setupRealtimeDatabase()
        }
    }

    private fun setupRealtimeDatabase() {

    }

    override fun deleteMessage(message: Message) {

    }

    override fun onDestroy() {
        super.onDestroy()
        bind=null
    }

}