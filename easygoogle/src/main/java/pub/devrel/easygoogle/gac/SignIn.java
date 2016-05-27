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

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Interface to the Google Sign In API, which can be used to determine the users identity. The
 * default scopes "profile" and "email" are used to return basic information about the user
 * (when possible) however this does not grant any authorization to use other Google APIs on
 * behalf of the user. For more information visit:
 * https://developers.google.com/identity/sign-in/android/
 *
 * When registering OAuth clients for Google Sign-In, Web server OAuth client registration is needed
 * with android by requesting an ID token in your GoogleSignInOptions, and supplying the web client
 * ID for your server. For more information visit:
 * https://android-developers.blogspot.com/2016/03/registering-oauth-clients-for-google.html
 */
public class SignIn extends GacModule<SignIn.SignInListener> {

    /**
     * Listener to be notified of asynchronous Sign In events, like sign in or sign out,
     */
    public interface SignInListener {

        /**
         * The user has been signed successfully.
         * @param account basic information about the signed-in user like name and email.
         */
        void onSignedIn(GoogleSignInAccount account);

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

    public SignIn() {}

    @Override
    public List<Api> getApis() {
        return Arrays.asList(new Api[]{Auth.GOOGLE_SIGN_IN_API});
    }

    @Override
    public Api.ApiOptions.HasOptions getOptionsFor(Api<? extends Api.ApiOptions> api) {
        if (Auth.GOOGLE_SIGN_IN_API.equals(api)) {
            GoogleSignInOptions.Builder googleSignInOptions =  new GoogleSignInOptions.Builder(
                    GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail();

            // Check server client id for OAuth, so GoogleSignInAccount.getIdToken(); is non-null
            String serverClientId = getFragment().getServerClientId();
            if(serverClientId != null){
                googleSignInOptions.requestIdToken(serverClientId);
            }
            return googleSignInOptions.build();
        } else {
            return super.getOptionsFor(api);
        }
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.emptyList();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Kick off silent sign-in process
        Auth.GoogleSignInApi.silentSignIn(getFragment().getGoogleApiClient())
                .setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        if (googleSignInResult.isSuccess()) {
                            getListener().onSignedIn(googleSignInResult.getSignInAccount());
                        } else {
                            getListener().onSignInFailed();
                        }
                    }
                });
    }

    @Override
    public void onStop() {}

    @Override
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            if (data != null) {
                GoogleSignInResult gsr = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (gsr != null && gsr.isSuccess()) {
                    getListener().onSignedIn(gsr.getSignInAccount());
                } else {
                    getListener().onSignInFailed();
                }
            }

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
     * Get the currently signed in user as a GoogleSignInAccount.
     * @return a {@link GoogleSignInAccount} or null.
     */
    public GoogleSignInAccount getCurrentUser() {
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(
                getFragment().getGoogleApiClient());
        if (opr.isDone()) {
            return opr.get().getSignInAccount();
        } else {
            return null;
        }
    }

    /**
     * Convenience method to determine if a user is signed in (current user is not null).
     * @return true if signed in, false otherwise.
     */
    public boolean isSignedIn() {
        return (getCurrentUser() != null);
    }

    /**
     * Initiate the sign in process, resolving all possible errors (showing account picker, consent
     * screen, etc). This operation may result in UI being displayed, results are returned to
     * the {@link pub.devrel.easygoogle.gac.SignIn.SignInListener}.
     */
    public void signIn() {
        Log.d(TAG, "signIn");
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(getFragment().getGoogleApiClient());
        getFragment().startActivityForResult(intent, RC_SIGN_IN);
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

        Auth.GoogleSignInApi.revokeAccess(fragment.getGoogleApiClient()).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            getListener().onSignedOut();
                        } else {
                            Log.w(TAG, "Could not sign out: " + status);
                        }
                    }
                });
    }
}
