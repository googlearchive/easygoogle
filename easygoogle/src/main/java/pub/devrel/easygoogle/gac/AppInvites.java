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

public class AppInvites extends GacModule {

    public interface AppInviteListener {
        void onInvitationReceived(String invitationId, String deepLink);
        void onInvitationsSent(String[] ids);
        void onInvitationsFailed();
    }

    private static final String TAG = AppInvites.class.getSimpleName();
    private static final int RC_INVITE = 9003;

    private BroadcastReceiver mDeepLinkReceiver;
    private AppInviteListener mListener;
    private Intent mCachedInvitationIntent;

    protected AppInvites(GacFragment fragment) {
        super(fragment);

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
        mListener.onInvitationReceived(invitationId, deepLink);
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
                mListener.onInvitationsSent(ids);
            } else {
                mListener.onInvitationsFailed();
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
        // If there is an invitation waiting to be updated, do it now
        if (mCachedInvitationIntent != null) {
            updateInvitationStatus(mCachedInvitationIntent);
            mCachedInvitationIntent = null;
        }
    }

    @Override
    public void onUnresolvableFailure() {}

    public void setListener(AppInviteListener listener) {
        mListener = listener;
    }
}
