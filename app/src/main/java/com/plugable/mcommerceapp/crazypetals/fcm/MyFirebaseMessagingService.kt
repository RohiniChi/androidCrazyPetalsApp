/*
 * Author : Chetan Patil.
 * Module : Push notification module
 * Version : V 1.0
 * Sprint : 13
 * Date of Development : 07/10/2019
 * Date of Modified : 07/10/2019
 * Comments : This is service class which is executed by firebase SDK
 * Output : To get notification from FireBase
 */

package com.plugable.mcommerceapp.crazypetals.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import org.json.JSONException
import org.json.JSONObject
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import com.plugable.mcommerceapp.crazypetals.R
import com.plugable.mcommerceapp.crazypetals.ui.activities.NotificationActivity
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var title: String? = null
    private var message: String? = null
    private var url: String? = null
    private var notifyType: String? = null
    private var request = ""

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d("FCM", "received")
        Log.d("FCM", remoteMessage.data.toString())
        try {
            val json = JSONObject(remoteMessage.data.toString())
            handleDataMessage(json)
        } catch (e: Exception) {
            Log.e("JSONError", e.toString())
        }
    }

    private fun handleDataMessage(json: JSONObject) {

        try {
            val data = json.getJSONObject("data")

            title = data.getString("title")
            message = data.getString("message")
            url = data.getString("url")

            if(url.isNullOrEmpty()){
                showNotification(null)
            }else{
                val bitmap = getBitmapFromUrl(url!!);
                showNotification(bitmap)

            }

        } catch (e: JSONException) {
            Log.e(getString(R.string.app_name), "Json Exception: " + e.message)
        } catch (e: Exception) {
            Log.e(getString(R.string.app_name), "Exception: " + e.message)
        }

    }

    /*
     * This method shows the notification
     */
    private fun showNotification(bitmap: Bitmap?) {
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default",
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = getString(R.string.app_name)
            mNotificationManager.createNotificationChannel(channel)
        }

        val mBuilder = NotificationCompat.Builder(applicationContext, "default")
            .setSmallIcon(R.drawable.ic_notification_icon_cp) // notification icon
            .setContentTitle(title) // title for notification
            .setContentText(message)// message for notification
            .setAutoCancel(true) // clear notification after click
        if(bitmap!=null){
            mBuilder.setStyle(NotificationCompat.BigPictureStyle()
                .bigPicture(bitmap))/*Notification with Image*/
        }

        val intent = Intent(applicationContext, NotificationActivity::class.java)
        // set intent so it does not start a new activity
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(pi)
        val randomPIN = (Math.random() * 9000).toInt() + 1000
        sendLocalBroadcast(randomPIN.toString())
        mNotificationManager.notify(randomPIN, mBuilder.build())
    }

    /*
    *To get a Bitmap image from the URL received
    * */
    fun getBitmapFromUrl(imageUrl: String): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)

        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            null

        }
    }


    /*
     * This method sends the local broadcast to activity
     */
    private fun sendLocalBroadcast(notificationId: String) {

        val intent = Intent("notification")
        val bundle = Bundle()
        bundle.putString("title", title)
        bundle.putString("message", message)
        bundle.putString("notifyType", notifyType)
        bundle.putString("request", request)
        bundle.putString("notificationId", notificationId)
        intent.putExtras(bundle)

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


    // [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d("FCM Token", "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token)
    }

}