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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import java.util.HashMap;
import java.util.Map;

import pub.devrel.easygoogle.R;

public class GacFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = GacFragment.class.getSimpleName();

    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";

    private GoogleApiClient mGoogleApiClient;
    private Map<Class<? extends GacModule>, GacModule> mModules = new HashMap<>();

    private boolean mIsResolving = false;
    private boolean mShouldResolve = false;
    private int mResolutionCode;

    private void buildGoogleApiClient() {
        Log.d(TAG, "buildGoogleApiClient: " + mModules);

        // Can't build a GoogleApiClient with no APIs
        if (mModules.size() == 0) {
            Log.w(TAG, "No APIs, not building GoogleApiClient.");
            return;
        }

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this);

        for (GacModule api : mModules.values()) {
            for (Api apiObj : api.getApis()) {
                builder.addApi(apiObj);
            }

            for (Scope scopeObj : api.getScopes()) {
                builder.addScope(scopeObj);
            }
        }

        mGoogleApiClient = builder.build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        // Give each API a chance to handle it
        boolean handled = false;
        for (GacModule module : mModules.values()) {
            if (module.handleActivityResult(requestCode, resultCode, data)) {
                handled = true;
                break;
            }
        }
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

        for (GacModule module : mModules.values()) {
            module.onStart();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        for (GacModule module : mModules.values()) {
            module.onStop();
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

        for (GacModule module : mModules.values()) {
            module.onConnected();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            Log.d(TAG, "onConnectionFailed: resolving with code " + mResolutionCode);
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(getActivity(), maskRequestCode(mResolutionCode));
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve " + connectionResult, e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                Log.e(TAG, "Error: no resolution:" + connectionResult.getErrorCode());
                showErrorDialog(connectionResult);

                for (GacModule module : mModules.values()) {
                    module.onUnresolvableFailure();
                }
            }
        } else {
            Log.w(TAG, String.format("Not resolving (isResolving, shouldResolve) = (%b, %b)",
                    mIsResolving, mShouldResolve));
        }
    }

    private void showErrorDialog(ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();

        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            // Show the default Google Play services error dialog which may still start an intent
            // on our behalf if the user can resolve the issue.
            GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(), mResolutionCode,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mShouldResolve = false;
                        }
                    }).show();
        } else {
            // No default Google Play Services error, display a message to the user.
            String errorString = getString(R.string.play_services_error_fmt, errorCode);
            Toast.makeText(getActivity(), errorString, Toast.LENGTH_SHORT).show();
            mShouldResolve = false;
        }
    }

    /**
     * This is a nasty hack. When calling startActivityForResult from a Fragment, the requestCode
     * is tagged with the Fragment index and then the Activity (when super.onActivityResult is
     * called) forwards the result to the calling fragment.  However this does not happen for
     * IntentSender, so we need to manually mask our request codes so that they come back to
     * onActivityResult in the Fragment.
     *
     * @param requestCode the original request code to mask.
     * @return the masked request code to use instead.
     */
    private int maskRequestCode(int requestCode) {
        if ((requestCode & 0xffff0000) != 0) {
            throw new IllegalArgumentException("Can only use lower 16 bits for requestCode");
        }

        int fragmentIndex = getActivity().getSupportFragmentManager().getFragments().indexOf(this);
        int maskedCode = requestCode + ((fragmentIndex + 1) << 16);

        return maskedCode;
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

    public <T> T getModule(Class<T> clazz) {
        return (T) mModules.get(clazz);
    }

    // TODO(samstern): I can probably generalize this to "enableModule" if GacModule is genericized
    // with the type of the listener for the module.
    public void enableSignIn(SignIn.SignInListener signInListener) {
        SignIn signIn = new SignIn(this);
        signIn.setListener(signInListener);
        mModules.put(SignIn.class, signIn);
    }

    public void enableAppInvites(AppInvites.AppInviteListener appInviteListener) {
        AppInvites appInvites = new AppInvites(this);
        appInvites.setListener(appInviteListener);
        mModules.put(AppInvites.class, appInvites);
    }
}
