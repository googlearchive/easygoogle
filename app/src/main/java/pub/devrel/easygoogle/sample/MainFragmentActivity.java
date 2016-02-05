package pub.devrel.easygoogle.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import pub.devrel.easygoogle.Google;
import pub.devrel.easygoogle.gac.SignIn;
import pub.devrel.easygoogle.gcm.Messaging;

/**
 * Activity for demonstrating the use of EasyGoogle in Fragments.
 */
public class MainFragmentActivity extends AppCompatActivity implements
        Messaging.MessagingListener {

    private static final String TAG = "MainFragmentActivity";

    private Google mGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        mGoogle = new Google.Builder(this)
                .enableMessaging(this, getString(R.string.gcm_defaultSenderId))
                .build();

        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, new EasyGoogleFragment())
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogle.getMessaging().subscribeTo("/topics/easygoogle-test");
    }

    @Override
    public void onMessageReceived(String from, Bundle message) {
        Log.d(TAG, "onMessageReceived:" + from);
    }

    /**
     * Fragment that hosts Sign In.
     */
    public static class EasyGoogleFragment extends Fragment implements
            SignIn.SignInListener {

        private static final String TAG = "EasyGoogleFragment";
        private Google mGoogle;

        public EasyGoogleFragment() {}

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Log.d(TAG, "onActivityCreated");

            mGoogle = new Google.Builder(getActivity())
                    .enableSignIn(this)
                    .build();

            Log.d(TAG, "onActivityCreated:isSignedIn:" + isSignedIn());
        }

        @Override
        public void onStart() {
            super.onStart();
            Log.d(TAG, "onStart");
            Log.d(TAG, "onStart:isSignedIn:" + isSignedIn());

            if (!isSignedIn()) {
                mGoogle.getSignIn().signIn();
            }
        }

        private boolean isSignedIn() {
            // The GoogleApiClient is only created on onActivityCreated of GacFragment, which is
            // not guaranteed to happen before or after this fragment is created
            if (mGoogle.getGoogleApiClient() == null) {
                Log.e(TAG, "isSignedIn: mGoogle.getGoogleApiClient() == null");
                return false;
            }

            return (mGoogle.getSignIn().isSignedIn());
        }

        @Override
        public void onSignedIn(GoogleSignInAccount account) {
            Log.d(TAG, "onSignedIn:" + account);
        }

        @Override
        public void onSignInFailed() {
            Log.d(TAG, "onSignInFailed");
        }

        @Override
        public void onSignedOut() {
            Log.d(TAG, "onSignedOut");
        }
    }

}
