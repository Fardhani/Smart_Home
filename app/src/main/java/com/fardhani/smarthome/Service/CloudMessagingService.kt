package com.fardhani.smarthome.Service

import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fardhani.smarthome.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CloudMessagingService : FirebaseMessagingService() {
    private val TAG = "ServiceFCM"

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage?.getFrom())

        // Check if message contains a data payload.
        if (remoteMessage?.getData()!!.size > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData())
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(
                TAG,
                "Message Notification Body: " + remoteMessage?.getNotification()!!.getBody()
            )
        }
        sendNotification(remoteMessage)
    }

    fun sendNotification(remoteMessage: RemoteMessage?) {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
            .setContentText(remoteMessage?.notification!!.body)
            .setAutoCancel(true)
            .setSmallIcon(R.mipmap.logo_app)
            .setSound(defaultSoundUri)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1 /* ID of notification */, notificationBuilder.build())
    }
}