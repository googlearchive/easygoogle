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
package pub.devrel.easygoogle.gac;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.util.Arrays;
import java.util.List;

/**
 * Interface to the Google Sign In API, which can be used to determine the users identity. The
 * default scopes "profile" and "email" are used to return basic information about the user
 * (when possible) however this does not grant any authorization to use other Google APIs on
 * behalf of the user. For more information visit:
 * https://developers.google.com/identity/sign-in/android/
 */
public class SignIn extends GacModule {

    /**
     * Listener to be notified of asynchronous Sign In events, like sign in or sign out,
     */
    public interface SignInListener {

        /**
         * The user has been signed successfully.
         * @param person basic information abou the signed-in user like name and email.
         */
        void onSignedIn(Person person);

        /**
         * The sign in process failed, either due to user cancellation or unresolvable errors. The
         * app should display a sign-in button and wait for the user to initiate action before
         * attempting sign-in again.
         */
        void onSignInFailed();

        /**
         * The user has been signed out and access has been revoked.
         */
        void onSignedOut();
    }

    private static final String TAG = SignIn.class.getSimpleName();
    public static final int RC_SIGN_IN = 9001;

    private SignIn.SignInListener mSignInListener;

    public SignIn(GacFragment fragment) {
        super(fragment);
    }

    @Override
    public List<Api> getApis() {
        return Arrays.asList(new Api[]{Plus.API});
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(new Scope("profile"), new Scope("email"));
    }

    @Override
    public void onConnected() {
        Person currentPerson = Plus.PeopleApi.getCurrentPerson(getFragment().getGoogleApiClient());
        mSignInListener.onSignedIn(currentPerson);
    }

    @Override
    public void onUnresolvableFailure() {
        mSignInListener.onSignInFailed();
    }

    @Override
    public void onStart() {}

    @Override
    public void onStop() {}

    @Override
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != Activity.RESULT_OK) {
                getFragment().setShouldResolve(false);
            }

            getFragment().getGoogleApiClient().connect();
            return true;
        }

        return false;
    }

    /**
     * Add a {@link SignInButton} to the current Activity/Fragment. When clicked, the button
     * will automatically make a call to {@link SignIn#signIn()}.
     * @param context the calling context.
     * @param container a ViewGroup into which the SignInButton should be placed as the first child.
     * @return the instantiated SignInButton, which can be customized further.
     */
    public SignInButton createSignInButton(Context context, ViewGroup container) {

        // Create SignInButton and configure style
        SignInButton signInButton = new SignInButton(context);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setColorScheme(SignInButton.COLOR_DARK);

        // Make it start sign-in on click
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        // Add to the layout, return reference to the button for user styling
        container.addView(signInButton, 0);
        return signInButton;
    }

    /**
     * Initiate the sign in process, resolving all possible errors (showing account picker, consent
     * screen, etc). This operation may result in UI being displayed, results are returned to
     * the {@link pub.devrel.easygoogle.gac.SignIn.SignInListener}.
     */
    public void signIn() {
        Log.d(TAG, "signIn");
        getFragment().setShouldResolve(true);
        getFragment().setResolutionCode(RC_SIGN_IN);
        getFragment().getGoogleApiClient().reconnect();
    }


    /**
     * Initiate the sign out and disconnect process. Results are returned to the
     * {@link pub.devrel.easygoogle.gac.SignIn.SignInListener}. If the user is not already signed
     * in or the sign out operation fails, no result will be returned.
     */
    public void signOut() {
        Log.d(TAG, "signOut");
        final GacFragment fragment = getFragment();
        if (!fragment.isConnected()) {
            Log.w(TAG, "Can't sign out, not signed in!");
            return;
        }
        Plus.AccountApi.clearDefaultAccount(fragment.getGoogleApiClient());
        Plus.AccountApi.revokeAccessAndDisconnect(fragment.getGoogleApiClient()).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            mSignInListener.onSignedOut();
                        } else {
                            Log.w(TAG, "Could not sign out: " + status);
                        }
                    }
                });
    }

    public void setListener(SignInListener listener) {
        mSignInListener = listener;
    }
}
