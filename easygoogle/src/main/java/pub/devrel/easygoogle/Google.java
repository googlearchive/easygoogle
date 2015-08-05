package pub.devrel.easygoogle;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.api.GoogleApiClient;

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

    private Messaging mMessaging;
    private SignIn mSignIn;

    public static class Builder {

        private FragmentActivity mActivity;
        private SignIn.SignInListener mSignInListener;
        private Messaging.MessagingListener mMessagingListener;
        private String mSenderId;

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

        public Google build() {
            Google google = new Google(mActivity);
            if (mSignInListener != null) {
                google.getSignIn().setListener(mSignInListener);
            }
            if (mSenderId != null) {
                google.getMessaging().setListener(mMessagingListener);
                google.getMessaging().setSenderId(mSenderId);
            }
            return google;
        }
    }

    private Google(FragmentActivity activity) {
        // Create the fragments first, then the shims.
        mGacFragment = FragmentUtils.getOrCreate(activity, TAG_GAC_FRAGMENT, new GacFragment());
        mMessagingFragment =  FragmentUtils.getOrCreate(activity, TAG_MESSAGING_FRAGMENT, MessagingFragment.newInstance());
        mSignIn = new SignIn(mGacFragment);
        mMessaging = new Messaging(mMessagingFragment);
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        return mSignIn.handleActivityResult(requestCode, resultCode, data);
    }

    public Messaging getMessaging() {
        return mMessaging;
    }

    public SignIn getSignIn() {
        return mSignIn;
    }
}
