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

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GcmListenerService;

public abstract class EasyMessageService extends GcmListenerService {

    public EasyMessageService() {}

    @Override
    public abstract void onMessageReceived(String from, Bundle data);

    public boolean forwardToListener(String from, Bundle data) {
        Intent msg = new Intent(MessagingFragment.MESSAGE_RECEIVED);
        msg.putExtra(MessagingFragment.MESSAGE_ARG, data);
        msg.putExtra(MessagingFragment.MESSAGE_FROM_FIELD, from);

        return LocalBroadcastManager.getInstance(this).sendBroadcast(msg);
    }

    public PendingIntent createMessageIntent(String from, Bundle data,
                                             Class<? extends Messaging.MessagingListener> target) {

        // TODO(samstern): should I set flags on the PendingIntent?
        Intent intent = new Intent(this, target);
        intent.setAction(MessagingFragment.MESSAGE_RECEIVED);
        intent.putExtra(MessagingFragment.MESSAGE_FROM_FIELD, from);
        intent.putExtra(MessagingFragment.MESSAGE_ARG, data);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, 0);

        return pendingIntent;
    }
}
