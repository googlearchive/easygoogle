package pub.devrel.easygoogle.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.plus.model.people.Person;

import pub.devrel.easygoogle.Google;
import pub.devrel.easygoogle.gac.SignIn;
import pub.devrel.easygoogle.gcm.Messaging;

/**
 * Simple activity with GCM and Sign-In.
 */
public class MainActivity extends AppCompatActivity implements
        SignIn.SignInListener,
        Messaging.MessagingListener,
        View.OnClickListener {

    public static String TAG = "sample.MainActivity";
    private Google mGoogle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogle = new Google.Builder(this)
                .enableMessaging(getString(R.string.gcm_defaultSenderId))
                .setSignInListener(this)
                .build();

        findViewById(R.id.send_message_button).setOnClickListener(this);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!mGoogle.handleActivityResult(requestCode, resultCode, data)) {
            // Do your own handling here..
        }
    }

    @Override
    public void onSignedIn(Person person) {
        ((TextView) findViewById(R.id.sign_in_status)).setText("Signed in as: " + person.getDisplayName());
    }

    @Override
    public void onSignedOut() {
        ((TextView) findViewById(R.id.sign_in_status)).setText(R.string.status_signed_out);
    }

    @Override
    public void onSignInFailed() {
        ((TextView) findViewById(R.id.sign_in_status)).setText("Sign in failed.");
    }

    @Override
    public void onMessageReceived(String from, Bundle message) {
        Log.d(TAG, "onMessageReceived:" + from + ":" + message);
        ((TextView) findViewById(R.id.messaging_status)).setText("Message from " + from);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                // Sign in with Google
                SignIn.signIn(mGoogle);
                break;
            case R.id.sign_out_button:
                // Sign out with Google
                SignIn.signOut(mGoogle);
                break;
            case R.id.send_message_button:
                // Send a GCM message
                Bundle b = new Bundle();
                b.putString("message", "I am a banana!");
                Messaging.send(mGoogle, b);
                break;
        }
    }
}
