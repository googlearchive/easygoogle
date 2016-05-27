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
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import pub.devrel.easygoogle.Google;
import pub.devrel.easygoogle.gac.AppInvites;
import pub.devrel.easygoogle.gac.SignIn;
import pub.devrel.easygoogle.gac.SmartLock;
import pub.devrel.easygoogle.gcm.Messaging;

/**
 * Simple Activity demonstrating how to use the EasyGoogle library to quickly integrate
 * Sign-In, App Invites, Google Cloud Messaging, and SmartLock for Passwords.
 */
public class MainActivity extends AppCompatActivity implements
        SignIn.SignInListener,
        Messaging.MessagingListener,
        AppInvites.AppInviteListener,
        SmartLock.SmartLockListener,
        View.OnClickListener {

    public static String TAG = "sample.MainActivity";

    private Google mGoogle;

    // SmartLock data/fields
    private Credential mCredential;
    private EditText mUsernameField;
    private EditText mPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the Google object with access to Cloud Messaging, Sign-In, and App Invites.
        // All APIs are accessed through the Google object and the result of asynchronous operations
        // are returned through API-specific listener classes like {@link SignIn.SignInListener}.
        mGoogle = new Google.Builder(this)
                .enableMessaging(this, getString(R.string.gcm_defaultSenderId))
                .enableSignIn(this, null)
                .enableAppInvites(this)
                .enableSmartLock(this)
                .build();

        // Inject sign-in button, automatically configured to initiate sign-in when clicked.
        mGoogle.getSignIn().createSignInButton(this, (ViewGroup) findViewById(R.id.layout_sign_in));

        // Click listeners for buttons
        findViewById(R.id.send_message_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.send_invites_button).setOnClickListener(this);
        findViewById(R.id.button_smartlock_load).setOnClickListener(this);
        findViewById(R.id.button_smartlock_save).setOnClickListener(this);
        findViewById(R.id.button_smartlock_delete).setOnClickListener(this);

        // Other views
        mUsernameField = ((EditText) findViewById(R.id.field_smartlock_username));
        mPasswordField = ((EditText) findViewById(R.id.field_smartlock_password));
    }

    @Override
    public void onStart() {
        super.onStart();

        // Subscribe to the "easygoogle" topic
        mGoogle.getMessaging().subscribeTo("/topics/easygoogle");
    }

    @Override
    public void onSignedIn(GoogleSignInAccount account) {
        Log.d(TAG, "onSignedIn:" + account.getEmail());
        ((TextView) findViewById(R.id.sign_in_status)).setText(
                getString(R.string.status_signed_in_fmt, account.getDisplayName(), account.getEmail()));
    }

    @Override
    public void onSignedOut() {
        ((TextView) findViewById(R.id.sign_in_status)).setText(R.string.status_signed_out);
    }

    @Override
    public void onSignInFailed() {
        ((TextView) findViewById(R.id.sign_in_status)).setText(R.string.status_sign_in_failed);
    }

    @Override
    public void onMessageReceived(String from, Bundle message) {
        ((TextView) findViewById(R.id.messaging_status)).setText(
                getString(R.string.status_message_fmt, from));
    }

    @Override
    public void onInvitationReceived(String invitationId, String deepLink) {
        ((TextView) findViewById(R.id.app_invites_status)).setText(
                getString(R.string.status_invitation_fmt, invitationId, deepLink));
    }

    @Override
    public void onInvitationsSent(String[] ids) {
        ((TextView) findViewById(R.id.app_invites_status)).setText(
                getString(R.string.status_invitation_sent_fmt, ids.length));
    }

    @Override
    public void onInvitationsFailed() {
        ((TextView) findViewById(R.id.app_invites_status)).setText(R.string.status_invitation_failed);
    }

    @Override
    public void onCredentialRetrieved(Credential credential) {
        ((TextView) findViewById(R.id.smartlock_status)).setText(R.string.status_credential_retrieved);
        mCredential = credential;
        mUsernameField.setText(credential.getId());
        mPasswordField.setText(credential.getPassword());
    }

    @Override
    public void onShouldShowCredentialPicker() {
        mGoogle.getSmartLock().showCredentialPicker();
    }

    @Override
    public void onCredentialRetrievalFailed() {
        ((TextView) findViewById(R.id.smartlock_status)).setText(R.string.status_credential_failed);
        mUsernameField.setText(null);
        mPasswordField.setText(null);
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
            case R.id.button_smartlock_load:
                // Begin loading Credentials
                mGoogle.getSmartLock().getCredentials();
                break;
            case R.id.button_smartlock_save:
                // Save Credential
                String id = mUsernameField.getText().toString();
                String password = mPasswordField.getText().toString();
                mGoogle.getSmartLock().save(id, password);

                ((TextView) findViewById(R.id.smartlock_status)).setText(null);
                mUsernameField.setText(null);
                mPasswordField.setText(null);
                break;
            case R.id.button_smartlock_delete:
                // Delete Credential and clear fields
                if (mCredential != null) {
                    mGoogle.getSmartLock().delete(mCredential);

                    mCredential = null;
                    ((TextView) findViewById(R.id.smartlock_status)).setText(null);
                    mUsernameField.setText(null);
                    mPasswordField.setText(null);
                }
                break;
        }
    }
}
