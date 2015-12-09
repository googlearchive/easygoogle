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
package pub.devrel.easygoogle;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import pub.devrel.easygoogle.gac.AppInvites;
import pub.devrel.easygoogle.gac.GacFragment;
import pub.devrel.easygoogle.gac.SignIn;
import pub.devrel.easygoogle.gac.SmartLock;
import pub.devrel.easygoogle.gcm.Messaging;
import pub.devrel.easygoogle.gcm.MessagingFragment;

/**
 * Google is the user-facing interface for all APIs, acts as a controller for a number of
 * sub-fragments that the user should not interact with directly. All user interaction should
 * be driven by listeners given to the Google.Builder.
 */
public class Google {

    private static final String TAG = Google.class.getSimpleName();

    // Tags for fragments that this class will control
    private static final String TAG_GAC_FRAGMENT = "gac_fragment";
    private static final String TAG_MESSAGING_FRAGMENT = "messaging_fragment";

    // Fragment that holds the GoogleApiClient
    private GacFragment mGacFragment;
    private MessagingFragment mMessagingFragment;

    public static class Builder {

        private FragmentActivity mActivity;

        private SignIn.SignInListener mSignInListener;

        private Messaging.MessagingListener mMessagingListener;
        private String mSenderId;

        private AppInvites.AppInviteListener mAppInviteListener;

        private SmartLock.SmartLockListener mSmartLockListener;

        public Builder(FragmentActivity activity){
            mActivity = activity;
        }

        /**
         * Initialize {@link SignIn}.
         * @param signInListener listener for sign in events,
         * @return self, for chaining.
         */
        public Builder enableSignIn(SignIn.SignInListener signInListener) {
            mSignInListener = signInListener;
            return this;
        }

        /**
         * Initialize {@link Messaging}.
         * @param listener listener for GCM events.
         * @param senderId GCM sender Id.
         * @return self, for chaining.
         */
        public Builder enableMessaging(Messaging.MessagingListener listener, String senderId) {
            mSenderId = senderId;
            mMessagingListener = listener;
            return this;
        }

        /**
         * Initialize {@link AppInvites}.
         * @param listener listener for app invites events.
         * @return self, for chaining.
         */
        public Builder enableAppInvites(AppInvites.AppInviteListener listener) {
            mAppInviteListener = listener;
            return this;
        }

        /**
         * Initialize {@link SmartLock}.
         * @param listener listener for SmartLock events.
         * @return self, for chaining.
         */
        public Builder enableSmartLock(SmartLock.SmartLockListener listener) {
            mSmartLockListener = listener;
            return this;
        }

        /**
         * Build the {@link Google} instance for use with all enabled services,
         * @return a Google instance.
         */
        public Google build() {
            Google google = new Google(mActivity);
            if (mSignInListener != null) {
                google.mGacFragment.enableModule(SignIn.class, mSignInListener);
            }

            if (mSenderId != null) {
                google.mMessagingFragment.setSenderId(mSenderId);
                google.mMessagingFragment.setMessagingListener(mMessagingListener);
            }

            if (mAppInviteListener != null) {
                google.mGacFragment.enableModule(AppInvites.class, mAppInviteListener);
            }

            if (mSmartLockListener != null) {
                google.mGacFragment.enableModule(SmartLock.class, mSmartLockListener);
            }
            return google;
        }
    }

    private Google(FragmentActivity activity) {
        // Create the fragments first, then the shims.
        mGacFragment = FragmentUtils.getOrCreate(activity, TAG_GAC_FRAGMENT, new GacFragment());
        mMessagingFragment =  FragmentUtils.getOrCreate(activity, TAG_MESSAGING_FRAGMENT, MessagingFragment.newInstance());
    }

  /**
     * Get the underlying <code>GoogleApiClient</code> instance to access public methods. If GoogleApiClient is not
     * properly created, there will be a warning in logcat.
     * @return the underlying GoogleApiClient instance.
     */
    public GoogleApiClient getGoogleApiClient() {
        GoogleApiClient googleApiClient = mGacFragment.getGoogleApiClient();
        if (googleApiClient == null) {
            Log.w(TAG, "GoogleApiClient is not created, getGoogleApiClient() returning null.");
        }

        return googleApiClient;
    }

    /**
     * Get the local {@link Messaging} instance to access public methods. If Messaging is not
     * properly initialized, there will be a warning in logcat.
     * @return a Messaging instance.
     */
    public Messaging getMessaging() {
        Messaging messaging = mMessagingFragment.getMessaging();
        if (messaging == null) {
            Log.w(TAG, "Messaging is not enabled, getMessaging() returning null.");
        }

        return messaging;
    }

    /**
     * Get the local {@link SignIn} instance to access public methods. If SignIn is not
     * properly initialized, there will be a warning in logcat.
     * @return a SignIn instance.
     */
    public SignIn getSignIn() {
        SignIn signIn = mGacFragment.getModule(SignIn.class);
        if (signIn == null) {
            Log.w(TAG, "SignIn is not enabled, getSignIn() returning null.");
        }

        return signIn;
    }

    /**
     * Get the local {@link AppInvites} instance to access public methods. If AppInvites is not
     * properly initialized, there will be a warning in logcat.
     * @return an AppInvites instance.
     */
    public AppInvites getAppInvites() {
        AppInvites appInvites = mGacFragment.getModule(AppInvites.class);
        if (appInvites == null) {
            Log.w(TAG, "AppInvites is not enabled, getAppInvites() returning null.");
        }

        return appInvites;
    }

    /**
     * Get the local {@link SmartLock} instance to access public methods. If SmartLock is not
     * properly initialized, there will be a warning in logcat.
     * @return a SmartLock instance.
     */
    public SmartLock getSmartLock() {
        SmartLock smartLock = mGacFragment.getModule(SmartLock.class);
        if (smartLock == null) {
            Log.w(TAG, "SmartLock is not enabled, getSmartLock() returning null.");
        }

        return smartLock;
    }
}
