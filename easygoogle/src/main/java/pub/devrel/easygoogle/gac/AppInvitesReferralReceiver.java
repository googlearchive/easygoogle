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
package pub.devrel.easygoogle.gac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.appinvite.AppInviteReferral;

import pub.devrel.easygoogle.R;

/**
 * Standard implementation of a Broadcast Receiver that is used to detect installation from
 * the Play Store. When an app is installed from the Play Store, the Play Store broadcasts the
 * install information some time <b>during</b> the app's first run. This receiver catches this
 * broadcast and re-broadcasts the same information to the running application.
 */
public class AppInvitesReferralReceiver extends BroadcastReceiver {

    public AppInvitesReferralReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        // Create deep link intent with correct action and add play store referral information
        Intent deepLinkIntent = AppInviteReferral.addPlayStoreReferrerToIntent(intent,
                new Intent(context.getString(R.string.action_deep_link)));

        // Let any listeners know about the change
        LocalBroadcastManager.getInstance(context).sendBroadcast(deepLinkIntent);
    }

}
