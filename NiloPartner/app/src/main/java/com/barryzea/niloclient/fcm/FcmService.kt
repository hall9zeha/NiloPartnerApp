package com.barryzea.niloclient.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.barryzea.niloclient.MainActivity
import com.barryzea.niloclient.R
import com.barryzea.niloclient.commons.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

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
//manejando las notificaciones en primer plano
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.notification?.let{
            sendNotification(it)
        }
    }
    private fun sendNotification(remoteMessage: RemoteMessage.Notification){
        val intent=Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent= PendingIntent.getActivity(this, 0,intent,PendingIntent.FLAG_ONE_SHOT)
        val channelId=getString(R.string.notification_channel_id)
        val defaultSoundUri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder=NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(remoteMessage.title)
            .setContentText(remoteMessage.body)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.yellow_a400))
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //creamos el canal de la notificación ya ques obligatoria a artir de android 8
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val channelName=getString(R.string.channel_name_nilo)
            val description=getString(R.string.description_channel)
            val importance =NotificationManager.IMPORTANCE_HIGH
            val mChannel=NotificationChannel(channelId, channelName,importance)
            mChannel.description=description
            notificationManager.createNotificationChannel(mChannel)
        }

        notificationManager.notify(0, notificationBuilder.build())

    }
}