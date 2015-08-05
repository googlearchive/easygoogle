package pub.devrel.easygoogle.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;


public class IDRegisterService extends IntentService {

    public static String TAG = "IDRegisterService";

    public IDRegisterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "registering iid");
        String senderId = intent.getStringExtra(MessagingFragment.SENDER_ID_ARG);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // [START register_for_gcm]
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                // [START get_token]
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                // [END get_token]
                Log.i(TAG, "GCM Registration Token: " + token);

                // TODO: Implement this method to send any registration to your app's servers.
                //sendRegistrationToServer(token);

                // Subscribe to topic channels
                //subscribeTopics(token);

                // You should store a boolean that indicates whether the generated token has been
                // sent to your server. If the boolean is false, send the token to your server,
                // otherwise your server should have already received the token.
                sharedPreferences.edit().putBoolean(MessagingFragment.SENT_TOKEN_TO_SERVER, true).apply();
                // [END register_for_gcm]
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(MessagingFragment.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(MessagingFragment.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}
