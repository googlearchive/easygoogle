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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easygoogle.R;

/**
 * Interface to the App Invites API, which can be used to send Email and/or SMS invitations
 * to a user's contacts.  For more information visit: https://developers.google.com/app-invites/
 */
public class AppInvites extends GacModule<AppInvites.AppInviteListener> {

    /**
     * Listener to be notified of asynchronous App Invite events, like invitation receipt
     * or sending success.
     */
    public interface AppInviteListener {

        /**
         * Called when the application has received an App Invite, either while running or through
         * a Play Store install.  Before this callback is invoked, the invitation will be marked
         * as completed through the App Invites API.
         * @param invitationId the unique ID of the invitation.
         * @param deepLink the deep link data sent with the invitation,
         */
        void onInvitationReceived(String invitationId, String deepLink);

        /**
         * The user has successfully invited one or more contacts.
         * @param ids an array of unique IDs, one for each invitation sent by the user. The same
         *            id will be given to the recepient upon invitation receipt.
         */
        void onInvitationsSent(String[] ids);

        /**
         * Sending invitations failed or the user canceled the operation.
         */
        void onInvitationsFailed();
    }

    private static final String TAG = AppInvites.class.getSimpleName();
    private static final int RC_INVITE = 9003;

    private BroadcastReceiver mDeepLinkReceiver;
    private Intent mCachedInvitationIntent;

    protected AppInvites() {
        // Instantiate local BroadcastReceiver for receiving broadcasts from
        // AppInvitesReferralReceiver.
        mDeepLinkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // First, check if the Intent contains an AppInvite
                if (AppInviteReferral.hasReferral(intent)) {
                   processReferralIntent(intent);
                }
            }
        };
    }

    /**
     * Launch the UI where the user can choose contacts and send invitations. The UI will be
     * populated with the arguments of this method, however the user can choose to change the
     * final invitation message. Success or failure of this call will be reported to
     * {@link pub.devrel.easygoogle.gac.AppInvites.AppInviteListener}.
     * @param title the title to display at the top of the invitation window. Cannot be
     *              overridden by the user.
     * @param message the message to suggest as the body of the invitation, this will be editable
     *                by the sending user.
     * @param deeplink a URI containing any information the receiving party will need to make use
     *                 of the invitation, such as a coupon code or another identifier.
     */
    public void sendInvitation(String title, String message, Uri deeplink) {
        Intent intent = new AppInviteInvitation.IntentBuilder(title)
                .setMessage(message)
                .setDeepLink(deeplink)
                .build();

        getFragment().startActivityForResult(intent, RC_INVITE);
    }

    private void processReferralIntent(Intent intent) {
        // Confirm receipt of the invitation
        if (getFragment().isConnected()) {
            updateInvitationStatus(intent);
        } else {
            Log.w(TAG, "GoogleAPIClient not connected, can't update invitation.");
            mCachedInvitationIntent = intent;
        }


        // Notify the listener of the received invitation
        String invitationId = AppInviteReferral.getInvitationId(intent);
        String deepLink = AppInviteReferral.getDeepLink(intent);
        getListener().onInvitationReceived(invitationId, deepLink);
    }

    private void updateInvitationStatus(Intent intent) {
        // Extract invitation Id
        String invitationId = AppInviteReferral.getInvitationId(intent);

        // Update invitation installation status and also convert the invitation.
        GoogleApiClient gac = getFragment().getGoogleApiClient();
        if (AppInviteReferral.isOpenedFromPlayStore(intent)) {
            AppInvite.AppInviteApi.updateInvitationOnInstall(gac, invitationId);
        }

        AppInvite.AppInviteApi.convertInvitation(gac, invitationId);
    }

    @Override
    public void onStart() {
        super.onStart();

        // If app is already installed app and launched with deep link that matches
        // DeepLinkActivity filter, then the referral info will be in the intent.
        Intent launchIntent = getFragment().getActivity().getIntent();
        if (AppInviteReferral.hasReferral(launchIntent)) {
            processReferralIntent(launchIntent);
        }

        // Register the local BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(
                getFragment().getString(R.string.action_deep_link));
        LocalBroadcastManager.getInstance(getFragment().getActivity()).registerReceiver(
                mDeepLinkReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mDeepLinkReceiver != null) {
            // Unregister the local BroadcastReceiver
            LocalBroadcastManager.getInstance(getFragment().getActivity()).unregisterReceiver(
                    mDeepLinkReceiver);
        }
    }

    @Override
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_INVITE) {
            if (resultCode == Activity.RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                getListener().onInvitationsSent(ids);
            } else {
                getListener().onInvitationsFailed();
            }

            return true;
        }
        return false;
    }

    @Override
    public List<Api> getApis() {
        return Arrays.asList(new Api[]{
            AppInvite.API
        });
    }

    @Override
    public List<Scope> getScopes() {
        return new ArrayList<>();
    }

    @Override
    public void onConnected() {
        super.onConnected();

        // If there is an invitation waiting to be updated, do it now
        if (mCachedInvitationIntent != null) {
            updateInvitationStatus(mCachedInvitationIntent);
            mCachedInvitationIntent = null;
        }
    }
}
