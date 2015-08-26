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
package pub.devrel.easygoogle.gcm;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import pub.devrel.easygoogle.R;


public abstract class EasyMessageService extends IntentService {

    private static final String TAG = "EasyMessageService";

    public static final String EXTRA_TOKEN = "token";
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_FROM = "from";

    public static final String ACTION_REGISTER = "register";

    public EasyMessageService() {
        super(TAG);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (getString(R.string.action_new_token).equals(action)) {
            String token = intent.getStringExtra(EXTRA_TOKEN);

            onNewToken(token);
        }

        if (getString(R.string.action_new_message).equals(action)) {
            String from = intent.getStringExtra(EXTRA_FROM);
            Bundle data = intent.getExtras();
            data.remove(EXTRA_FROM);

            onMessageReceived(from, data);
        }
    }

    public abstract void onMessageReceived(String from, Bundle data);

    public abstract void onNewToken(String token);

    public boolean forwardToListener(String from, Bundle data) {
        Intent msg = new Intent(MessagingFragment.MESSAGE_RECEIVED);
        msg.putExtra(MessagingFragment.MESSAGE_ARG, data);
        msg.putExtra(MessagingFragment.MESSAGE_FROM_FIELD, from);

        return LocalBroadcastManager.getInstance(this).sendBroadcast(msg);
    }

    public PendingIntent createMessageIntent(String from, Bundle data,
                                             Class<? extends Messaging.MessagingListener> target) {

        Intent intent = new Intent(this, target);
        intent.setAction(MessagingFragment.MESSAGE_RECEIVED);
        intent.putExtra(MessagingFragment.MESSAGE_FROM_FIELD, from);
        intent.putExtra(MessagingFragment.MESSAGE_ARG, data);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    /**
     * Send an upstream GCM message with the following message data:
     * {
     *     token: $TOKEN,
     *     action: "register"
     * }
     */
    public void sendRegistrationMessage(String senderId, String token) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TOKEN, token);
        bundle.putString(EXTRA_ACTION, ACTION_REGISTER);
        MessagingFragment.send(this, senderId, bundle);
    }
}
