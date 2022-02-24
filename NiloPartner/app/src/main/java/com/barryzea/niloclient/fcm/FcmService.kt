package com.barryzea.niloclient.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.barryzea.niloclient.MainActivity
import com.barryzea.niloclient.R
import com.barryzea.niloclient.commons.Constants
import com.barryzea.niloclient.order.OrderActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.time.format.TextStyle

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
    if(remoteMessage.data.isNotEmpty()){
        sendNotificationByData(remoteMessage.data)
    }
        remoteMessage.notification?.let {
            //poniendo imagen de manera dinámica
            val imgUrl =
                it.imageUrl//"https://cms-assets.tutsplus.com/uploads/users/798/posts/27376/preview_image/firebase@2x.png"

            if (imgUrl == null) {
                sendNotification(it)
            } else {
                Glide.with(applicationContext)
                    .asBitmap()
                    .load(imgUrl)
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            sendNotification(it, resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })

            }
        }
    }
    private fun sendNotification(remoteMessage: RemoteMessage.Notification,bitmap:Bitmap?=null){
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
            .setStyle(NotificationCompat.BigTextStyle().bigText(remoteMessage.body))

        bitmap?.let{
         notificationBuilder
             .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                 .bigLargeIcon(null))
        }

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
    private fun sendNotificationByData(data:Map<String, String>){
        val intent=Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent= PendingIntent.getActivity(this, 0,intent,PendingIntent.FLAG_ONE_SHOT)
        val channelId=getString(R.string.notification_channel_id)
        val defaultSoundUri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder=NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(data["title"])
            .setContentText(data["body"])
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.yellow_a400))
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(data["body"]))

        //agregando boton de acción a la notificación
        val actionIntent= data[Constants.ACTION_INTENT]?.toInt()
        val orderId= data[Constants.PROP_ID]
        val status= data[Constants.STATUS]?.toInt()

        val trackIntent=Intent(this, OrderActivity::class.java).apply {
            putExtra(Constants.ACTION_INTENT,actionIntent)
            putExtra(Constants.PROP_ID,orderId)
            putExtra(Constants.STATUS, status)
        }

        val trackPendingIntent=PendingIntent.getActivity(this, System.currentTimeMillis().toInt(),trackIntent,
        0)
        val action=NotificationCompat.Action.Builder(R.drawable.ic_shipping, "Rastrear ahora",
            trackPendingIntent).build()
        notificationBuilder.addAction(action)
        //***************************************
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