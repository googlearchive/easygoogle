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
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.util.List;

import pub.devrel.easygoogle.R;

public class IDRegisterService extends IntentService {

    public static final String TAG = "IDRegisterService";

    public IDRegisterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String senderId = intent.getStringExtra(MessagingFragment.SENDER_ID_ARG);
        String gcmPermissionName = intent.getStringExtra(MessagingFragment.GCM_PERMISSION_ARG);

        try {
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "GCM Registration Token: " + token);

            // Find any services that could handle this
            List<ComponentName> services = GCMUtils.findServices(this, gcmPermissionName);

            // Notify the services of a new token
            for (ComponentName cn : services) {
                Log.d(TAG, "Launching service: " + cn);

                Intent newTokenIntent = new Intent();
                newTokenIntent.setComponent(cn);
                newTokenIntent.setAction(getString(R.string.action_new_token));
                newTokenIntent.putExtra(EasyMessageService.EXTRA_TOKEN, token);

                startService(newTokenIntent);
            }
        } catch (Exception e) {
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            Log.e(TAG, "Failed to complete token refresh", e);
        }
    }
}
