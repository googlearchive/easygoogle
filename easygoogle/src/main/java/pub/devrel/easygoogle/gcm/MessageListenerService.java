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

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.util.List;

import pub.devrel.easygoogle.R;

/**
 * Service to listen for incoming GCM messages.
 */
public class MessageListenerService extends GcmListenerService {

    private static final String TAG = "MessageListenerService";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "onMessageReceived:" + from + ":" + data);

        // Notify all services with the PERMISSION_EASY_GCM permission about this message
        List<ComponentName> components = GCMUtils.findServices(this, GCMUtils.PERMISSION_EASY_GCM);
        for (ComponentName cn : components) {
            Log.d(TAG, "Launching: " + cn.toString());

            Intent newMessageIntent = new Intent();
            newMessageIntent.setComponent(cn);
            newMessageIntent.setAction(getString(R.string.action_new_message));
            newMessageIntent.putExtra(EasyMessageService.EXTRA_FROM, from);
            newMessageIntent.putExtras(data);

            startService(newMessageIntent);
        }
    }
}
