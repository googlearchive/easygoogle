package pub.devrel.easygoogle.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class MessageSenderService extends IntentService {

    public static final String TAG = "gf.MessageSenderService";

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
            Log.d(TAG, senderEmail + "//" + data + "//" + id);
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            try {
                gcm.send(senderEmail, id, data);
                Log.d(TAG, "Sent!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
