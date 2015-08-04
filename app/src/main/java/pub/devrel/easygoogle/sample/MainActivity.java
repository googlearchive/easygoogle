package pub.devrel.easygoogle.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import pub.devrel.easygoogle.gcm.MessagingFragment;

public class MainActivity extends AppCompatActivity implements MessagingFragment.MessagingListener {

    public static String TAG = "sample.MainActivity";

    private MessagingFragment mMessaging;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMessaging = MessagingFragment.newInstance(this, getString(R.string.gcm_defaultSenderId));

        findViewById(R.id.send_message_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                b.putString("message", "I am a banana!");
                mMessaging.send(b);
            }
        });
    }

    @Override
    public void onMessageReceived(String from, Bundle message) {
        logAndToast("Message received", message);
    }

    private void logAndToast(String message) {
        logAndToast(message, "");
    }

    private void logAndToast(String message, Object value) {
        String text = message + "//" + value;
        Log.d(TAG, text);
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }
}
