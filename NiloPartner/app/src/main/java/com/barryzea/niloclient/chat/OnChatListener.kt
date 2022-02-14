package com.barryzea.niloclient.chat

import com.barryzea.niloclient.pojo.Message

interface OnChatListener {
    fun deleteMessage(message: Message)
}