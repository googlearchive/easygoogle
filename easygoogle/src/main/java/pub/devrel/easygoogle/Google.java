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

import pub.devrel.easygoogle.gac.AppInvites;
import pub.devrel.easygoogle.gac.GacFragment;
import pub.devrel.easygoogle.gac.SignIn;
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

        public Builder(FragmentActivity activity){
            mActivity = activity;
        }

        public Builder enableSignIn(SignIn.SignInListener signInListener) {
            mSignInListener = signInListener;
            return this;
        }

        public Builder enableMessaging(Messaging.MessagingListener listener, String senderId) {
            mSenderId = senderId;
            mMessagingListener = listener;
            return this;
        }

        public Builder enableAppInvites(AppInvites.AppInviteListener listener) {
            mAppInviteListener = listener;
            return this;
        }

        public Google build() {
            Google google = new Google(mActivity);
            if (mSignInListener != null) {
                google.mGacFragment.enableSignIn(mSignInListener);
            }

            if (mSenderId != null) {
                google.mMessagingFragment.setSenderId(mSenderId);
                google.mMessagingFragment.setMessagingListener(mMessagingListener);
            }

            if (mAppInviteListener != null) {
                google.mGacFragment.enableAppInvites(mAppInviteListener);
            }
            return google;
        }
    }

    private Google(FragmentActivity activity) {
        // Create the fragments first, then the shims.
        mGacFragment = FragmentUtils.getOrCreate(activity, TAG_GAC_FRAGMENT, new GacFragment());
        mMessagingFragment =  FragmentUtils.getOrCreate(activity, TAG_MESSAGING_FRAGMENT, MessagingFragment.newInstance());
    }

    public Messaging getMessaging() {
        Messaging messaging = mMessagingFragment.getMessaging();
        if (messaging == null) {
            Log.w(TAG, "Messaging is not enabled, getMessaging() returning null.");
        }

        return messaging;
    }

    public SignIn getSignIn() {
        SignIn signIn = mGacFragment.getModule(SignIn.class);
        if (signIn == null) {
            Log.w(TAG, "SignIn is not enabled, getSignIn() returning null.");
        }

        return signIn;
    }

    public AppInvites getAppInvites() {
        AppInvites appInvites = mGacFragment.getModule(AppInvites.class);
        if (appInvites == null) {
            Log.w(TAG, "AppInvites is not enabled, getAppInvites() returning null.");
        }

        return appInvites;
    }
}
