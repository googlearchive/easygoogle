package pub.devrel.easygoogle.gac;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import java.util.HashMap;
import java.util.Map;

public class GacFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = GacFragment.class.getSimpleName();

    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";

    private SignIn.SignInListener mSignInListener;

    private GoogleApiClient mGoogleApiClient;
    private Map<Class<? extends GacService>, GacService> mServices = new HashMap<>();

    private boolean mIsResolving = false;
    private boolean mShouldResolve = false;
    private int mResolutionCode;

    private void buildGoogleApiClient() {
        Log.d(TAG, "buildGoogleApiClient: " + mServices);

        // Can't build a GoogleApiClient with no APIs
        if (mServices.size() == 0) {
            Log.w(TAG, "No APIs, not building GoogleApiClient.");
            return;
        }

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this);

        for (GacService api : mServices.values()) {
            for (Api apiObj : api.getApis()) {
                builder.addApi(apiObj);
            }

            for (Scope scopeObj : api.getScopes()) {
                builder.addScope(scopeObj);
            }
        }

        mGoogleApiClient = builder.build();
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "handleActivityResult:" + requestCode + resultCode + data);

        // Give each API a chance to handle it
        for (GacService s : mServices.values()) {
            if (s.handleActivityResult(requestCode, resultCode, data)) {
                return true;
            }
        }

        // TODO(samstern): handling for general GAC errors (install GMS, etc)
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
            mShouldResolve = savedInstanceState.getBoolean(KEY_SHOULD_RESOLVE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

        buildGoogleApiClient();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
        outState.putBoolean(KEY_SHOULD_RESOLVE, mShouldResolve);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected: " + bundle);

        for (GacService gacService : mServices.values()) {
            gacService.onConnected();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);

        // TODO(samstern): reset resolution code at some point
        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(getActivity(), mResolutionCode);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve " + connectionResult, e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // TODO(samstern): No resolution, show error dialog
                Log.e(TAG, "Error: no resolution:" + connectionResult.getErrorCode());
            }
        } else {
            // TODO(samstern): What to do here?
            // TODO(samstern): should I notify GacServices on unresolvable failure?
            Log.w(TAG, String.format("Not resolving (isResolving, shouldResolve) = (%b, %b)",
                    mIsResolving, mShouldResolve));
        }
    }

    public boolean isConnected() {
        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }

    public void setShouldResolve(boolean shouldResolve) {
        Log.d(TAG, "setShouldResolve:" + shouldResolve);
        mShouldResolve = shouldResolve;
    }

    public void setIsResolving(boolean isResolving) {
        Log.d(TAG, "setIsResolving:" + isResolving);
        mIsResolving = isResolving;
    }

    public void setResolutionCode(int resolutionCode) {
        mResolutionCode = resolutionCode;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public SignIn.SignInListener getSignInListener() {
        return mSignInListener;
    }

    public void setSignInListener(SignIn.SignInListener signInListener) {
        mSignInListener = signInListener;

        // Enable/disable SignIn
        if (mSignInListener != null && !mServices.containsKey(SignIn.class)) {
            // Sign-in service needs to be enabled
            mServices.put(SignIn.class, new SignIn(this));
        } else if (mSignInListener == null && mServices.containsKey(SignIn.class)) {
            // Sign-in service needs to be disabled
            mServices.remove(SignIn.class);
        }
    }
}
