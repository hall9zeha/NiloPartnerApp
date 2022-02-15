package com.barryzea.nilopartner.interfaces

import com.barryzea.nilopartner.pojo.Message

interface OnChatListener {
    fun deleteMessage(message: Message)
}