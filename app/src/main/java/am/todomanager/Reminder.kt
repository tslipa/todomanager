package am.todomanager

import am.todomanager.activities.ActivityPreview
import am.todomanager.activities.MainActivity
import am.todomanager.recyclers.ListElement
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class Reminder : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val channelID = "am.todo.reminder"

        val id = intent?.getIntExtra("id", 0)!!
        val title = intent.getStringExtra("title")!!

        val notification = NotificationCompat.Builder(context!!, channelID)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(title)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(PendingIntent.getActivity(context, id, Intent(context, MainActivity::class.java), 0))
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)

        notificationManager.notify(id, notification)
    }

}