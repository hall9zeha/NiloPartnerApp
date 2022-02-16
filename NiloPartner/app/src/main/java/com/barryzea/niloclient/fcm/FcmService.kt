package com.barryzea.niloclient.fcm

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.barryzea.niloclient.commons.Constants
import com.google.firebase.messaging.FirebaseMessagingService

class FcmService: FirebaseMessagingService() {
    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        registerNewToken(newToken)
    }
    private fun registerNewToken(newToken:String){
        val preferences=PreferenceManager.getDefaultSharedPreferences(this)
        preferences.edit {
            putString(Constants.PROPERTY_TOKEN, newToken)
                .apply()
        }
    }
}