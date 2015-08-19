/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pub.devrel.easygoogle.sample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import pub.devrel.easygoogle.gcm.EasyMessageService;

public class MyMessageService extends EasyMessageService {

    private static final String TAG = "MyMessageService";

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

}
