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

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import pub.devrel.easygoogle.Google;
import pub.devrel.easygoogle.gac.AppInvites;
import pub.devrel.easygoogle.gac.SignIn;
import pub.devrel.easygoogle.gcm.Messaging;

/**
 * Simple Activity demonstrating how to use the EasyGoogle library to quickly integrate
 * Sign-In, App Invites, and Google Cloud Messaging.
 */
public class MainActivity extends AppCompatActivity implements
        SignIn.SignInListener,
        Messaging.MessagingListener,
        AppInvites.AppInviteListener,
        View.OnClickListener {

    public static String TAG = "sample.MainActivity";

    private Google mGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the Google object with access to Cloud Messaging, Sign-In, and App Invites.
        // All APIs are accessed through the Google object and the result of asynchronous operations
        // are returned through API-specific listener classes like {@link SignIn.SignInListener}.
        mGoogle = new Google.Builder(this)
                .enableMessaging(this, getString(R.string.gcm_defaultSenderId))
                .enableSignIn(this)
                .enableAppInvites(this)
                .build();

        // Inject sign-in button, automatically configured to initiate sign-in when clicked.
        mGoogle.getSignIn().createSignInButton(this, (ViewGroup) findViewById(R.id.layout_sign_in));

        // Click listeners for sign-out and message buttons
        findViewById(R.id.send_message_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.send_invites_button).setOnClickListener(this);
    }

    @Override
    public void onSignedIn(GoogleSignInAccount account) {
        Log.d(TAG, "onSignedIn:" + account.getEmail());
        ((TextView) findViewById(R.id.sign_in_status)).setText(
                "Signed in as: " + account.getDisplayName() + " (" + account.getEmail() + ")");
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
    public void onInvitationReceived(String invitationId, String deepLink) {
        ((TextView) findViewById(R.id.app_invites_status)).setText(
                "Received invitation " + invitationId + ":" + deepLink);
    }

    @Override
    public void onInvitationsSent(String[] ids) {
        ((TextView) findViewById(R.id.app_invites_status)).setText(
                "Sent " + ids.length + " invitations.");
    }

    @Override
    public void onInvitationsFailed() {
        ((TextView) findViewById(R.id.app_invites_status)).setText("Sending invitations failed.");
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
            case R.id.send_invites_button:
                // Send App Invites
                mGoogle.getAppInvites().sendInvitation(
                        "Title", "Message", Uri.parse("http://example.com/id/12345"));
                break;
        }
    }
}
