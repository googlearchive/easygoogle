package pub.devrel.easygoogle.sample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import pub.devrel.easygoogle.gcm.EasyMessageService;

public class MessagingService extends EasyMessageService {

    public static final String TAG = "MessagingService";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "onMessageReceived:" + from + ":" + data);

        // If there is a running Activity that implements MessageListener, it should handle
        // this message.
        if (!forwardToListener(from, data)) {
            // There is no active MessageListener to get this, I should fire a notification with
            // a PendingIntent to an activity that can handle this
            Log.d(TAG, "onMessageReceived: no active listeners");

            PendingIntent pendingIntent = createMessageIntent(from, data, MainActivity.class);
            Notification notif = new NotificationCompat.Builder(this)
                    .setContentTitle("Message from: " + from)
                    .setContentText(data.getString("message"))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notif);
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "onNewToken:" + token);
        String senderId = getString(R.string.gcm_defaultSenderId);
        sendRegistrationMessage(senderId, token);
    }
}
