package pub.devrel.easygoogle.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * An {@link IntentService} for sending GCM upstream messages.
 */
public class MessageSenderService extends IntentService {

    public static final String TAG = "MessageSenderService";

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
            Log.d(TAG, "Sending gcm message. " + senderEmail + "//" + data + "//" + id);
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            try {
                gcm.send(senderEmail, id, data);
                Log.d(TAG, "Sent!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
