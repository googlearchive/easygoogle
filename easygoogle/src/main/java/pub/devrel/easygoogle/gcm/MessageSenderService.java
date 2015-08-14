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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * An {@link IntentService} for sending GCM upstream messages.
 */
public class MessageSenderService extends IntentService {

    public static final String TAG = "MessageSenderService";

    private static AtomicInteger sMessageId = new AtomicInteger();

    public MessageSenderService() {
        super("MessageSenderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String senderEmail = MessagingFragment.getSenderEmail(intent.getStringExtra(MessagingFragment.SENDER_ID_ARG));
            Bundle data = intent.getBundleExtra(MessagingFragment.MESSAGE_ARG);
            String id = Integer.toString(sMessageId.incrementAndGet());
            Log.d(TAG, "Sending gcm message. " + senderEmail + "//" + data + "//" + id);
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            try {
                gcm.send(senderEmail, id, data);
                Log.d(TAG, "Sent!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
