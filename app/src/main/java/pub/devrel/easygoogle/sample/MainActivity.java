package pub.devrel.easygoogle.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.plus.model.people.Person;

import pub.devrel.easygoogle.Google;
import pub.devrel.easygoogle.R;
import pub.devrel.easygoogle.gac.SignIn;
import pub.devrel.easygoogle.gcm.Messaging;

public class MainActivity extends AppCompatActivity implements
        SignIn.SignInListener,
        Messaging.MessagingListener {

    private Google mGoogle;
    public static String TAG = "sample.MainActivity";

    // TODO(afshar): I could not resolve R.string.gcm_defaultSenderId, this is a placeholder
    private static final String SENDER_ID = "gcm_defaultSenderId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogle = new Google.Builder(this)
                .enableMessaging(SENDER_ID)
                .setSignInListener(this)
                .build();

        // Send GCM Message
        findViewById(R.id.send_message_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                b.putString("message", "I am a banana!");
                Messaging.send(mGoogle, b);
            }
        });

        // Sign in
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn.signIn(mGoogle);
            }
        });
    }

    @Override
    public void onSignedIn(Person person) {
        // TODO: Implement
    }

    @Override
    public void onSignedOut() {
        // TODO: Implement
    }

    @Override
    public void onSignInFailed() {
        // TODO: Implement
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
