/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pub.devrel.easygoogle.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
                .enableMessaging(this, getString(R.string.gcm_defaultSenderId))
                .enableSignIn(this)
                .build();

        // Inject sign-in button, automatcally configured to initiate sign-in when clicked.
        mGoogle.getSignIn().createSignInButton(this, (ViewGroup) findViewById(R.id.layout_sign_in));

        // Click listeners for sign-out and message buttons
        findViewById(R.id.send_message_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
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
            case R.id.sign_out_button:
                // Sign out with Google
                mGoogle.getSignIn().signOut();
                break;
            case R.id.send_message_button:
                // Send a GCM message
                Bundle b = new Bundle();
                b.putString("message", "I am a banana!");
                mGoogle.getMessaging().send(b);
                break;
        }
    }
}
