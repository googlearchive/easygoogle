package pub.devrel.easygoogle.gcm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class MessageListenerService extends GcmListenerService {
    public MessageListenerService() {
    }

    public static final String TAG = "MessageListenerService";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        Intent msg = new Intent(MessagingFragment.MESSAGE_RECEIVED);
        msg.putExtra(MessagingFragment.MESSAGE_ARG, data);
        msg.putExtra(MessagingFragment.MESSAGE_FROM_FIELD, from);
        LocalBroadcastManager.getInstance(this).sendBroadcast(msg);
    }

}