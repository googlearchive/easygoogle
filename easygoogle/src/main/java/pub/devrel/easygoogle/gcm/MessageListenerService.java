package pub.devrel.easygoogle.gcm;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.util.List;

import pub.devrel.easygoogle.R;

public class MessageListenerService extends GcmListenerService {

    private static final String TAG = "MessageListenerService";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "onMessageReceived:" + from + ":" + data);
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
