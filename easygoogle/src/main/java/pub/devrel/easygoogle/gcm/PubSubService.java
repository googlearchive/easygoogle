package pub.devrel.easygoogle.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import pub.devrel.easygoogle.R;

public class PubSubService extends IntentService {

    private static final String TAG = "PubSubService";

    private GcmPubSub mGcmPubSub;

    public PubSubService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGcmPubSub = GcmPubSub.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();

        if (getString(R.string.action_subscribe).equals(action)) {
            String topic = intent.getStringExtra(MessagingFragment.TOPIC_ARG);
            String senderId = intent.getStringExtra(MessagingFragment.SENDER_ID_ARG);

            Log.d(TAG, "Subscribing to:" + topic);
            try {
                mGcmPubSub.subscribe(getToken(senderId), topic, null);
            } catch (IOException e) {
                Log.e(TAG, "Failed to subscribe to " + topic, e);
            }
        }

        if (getString(R.string.action_unsubscribe).equals(action)) {
            String topic = intent.getStringExtra(MessagingFragment.TOPIC_ARG);
            String senderId = intent.getStringExtra(MessagingFragment.SENDER_ID_ARG);

            Log.d(TAG, "Unsubscribing from:" + topic);
            try {
                mGcmPubSub.unsubscribe(getToken(senderId), topic);
            } catch (IOException e) {
                Log.e(TAG, "Failed to unsubscribe from " + topic, e);
            }
        }
    }

    private String getToken(String senderId) throws IOException {
        InstanceID instanceID = InstanceID.getInstance(this);
        return instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
    }
}
