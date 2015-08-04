package pub.devrel.easygoogle;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.api.GoogleApiClient;

import pub.devrel.easygoogle.gac.GacFragment;
import pub.devrel.easygoogle.gac.SignIn;
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
        private String mSenderId;

        public Builder(FragmentActivity activity){
            mActivity = activity;
        }

        public Builder setSignInListener(SignIn.SignInListener signInListener) {
            mSignInListener = signInListener;
            return this;
        }

        // TODO(samstern): it should not be required that the Activity implement MessagingListener,
        // it should be passed in here
        public Builder enableMessaging(String senderId) {
            mSenderId = senderId;
            return this;
        }

        public Google build() {
            Google google = new Google(mActivity, mSenderId);
            google.getGacFragment().setSignInListener(mSignInListener);

            return google;
        }
    }

    private Google(FragmentActivity activity, String senderId) {
        // TODO(samstern): should I make this conditional?
        mGacFragment = FragmentUtils.getOrCreate(activity, TAG_GAC_FRAGMENT, new GacFragment());

        if (senderId != null) {
            mMessagingFragment = FragmentUtils.getOrCreate(activity, TAG_MESSAGING_FRAGMENT,
                    MessagingFragment.newInstance(senderId));
        }

    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        return mGacFragment.handleActivityResult(requestCode, resultCode, data);
    }

    // TODO(samstern): hide this or discourage it
    public GacFragment getGacFragment() {
        return mGacFragment;
    }

    public MessagingFragment getMessagingFragment() {
        return mMessagingFragment;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGacFragment.getGoogleApiClient();
    }
}
