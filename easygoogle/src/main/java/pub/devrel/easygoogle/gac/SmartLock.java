package pub.devrel.easygoogle.gac;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Interface to the SmartLock for Passwords API which can be used to save and retrieve
 * id and password combinations on behalf of the user.  SmartLock allows your users to
 * enter their password once in Android or Chrome and be automatically signed in across
 * all of their devices. For more information, visit:
 * https://developers.google.com/identity/smartlock-passwords/android
 */
public class SmartLock extends GacModule<SmartLock.SmartLockListener> {

    /**
     * Listener to be notified of asynchronous SmartLock events, like loading Credentials.
     */
    public interface SmartLockListener {

        /**
         * A {@link Credential} has been successfully retrieved and the application
         * should validate the id/password and attempt to sign the user in.
         * @param credential the Credential chosen by the user.
         */
        void onCredentialRetrieved(Credential credential);

        /**
         * There are Credentials available to load, but the app must display a picker
         * dialog to allow the user to choose. See {@link #showCredentialPicker()}.
         */
        void onShouldShowCredentialPicker();

        /**
         * There are no Credentials available to load, or the load operation failed or was
         * canceled by the user.
         */
        void onCredentialRetrievalFailed();

    }

    private static final String TAG = "SmartLock";
    private static final int RC_READ = 9016;
    private static final int RC_SAVE = 9017;

    protected SmartLock() {}

    @Override
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_READ) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                getListener().onCredentialRetrieved(credential);
            } else {
                getListener().onCredentialRetrievalFailed();
            }

            return true;
        }

        if (requestCode == RC_SAVE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "RC_SAVE: Ok");
            } else {
                Log.d(TAG, "RC_SAVE: Failure");
            }

            return true;
        }

        return false;
    }

    @Override
    public List<Api> getApis() {
        return Arrays.asList(new Api[]{Auth.CREDENTIALS_API});
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.emptyList();
    }

    /**
     * Begin the process of retrieving a {@link Credential} for the device user. This can have
     * a few different results:
     *   1) If the user has auto sign-in enabled and exactly one previously saved credential,
     *      {@link SmartLockListener#onCredentialRetrieved(Credential)} will be called and
     *      you can sign the user in immediately.
     *   2) If the user has multiple saved credentials or one saved credential and has disabled
     *      auto sign-in, you will get the callback {@link SmartLockListener#onShouldShowCredentialPicker()}
     *      at which point you can choose to show the picker dialog to continue.
     *   3) If the user has no saved credentials or cancels the operation, you will receive the
     *      {@link SmartLockListener#onCredentialRetrievalFailed()} callback.
     */
    public void getCredentials() {
        CredentialRequest request = buildCredentialRequest();

        Auth.CredentialsApi.request(getFragment().getGoogleApiClient(), request)
                .setResultCallback(new ResultCallback<CredentialRequestResult>() {
                    @Override
                    public void onResult(CredentialRequestResult result) {
                        if (result.getStatus().isSuccess()) {
                            // Single credential, auto sign-in
                            Credential credential = result.getCredential();
                            getListener().onCredentialRetrieved(credential);
                        } else if (result.getStatus().hasResolution() &&
                                result.getStatus().getStatusCode() != CommonStatusCodes.SIGN_IN_REQUIRED) {
                            // Multiple credentials or auto-sign in disabled.  If the status
                            // code is SIGN_IN_REQUIRED then it is a hint credential, which we
                            // do not want at this point.
                            getListener().onShouldShowCredentialPicker();
                        } else {
                            // Could not retrieve credentials
                            getListener().onCredentialRetrievalFailed();
                        }
                    }
                });
    }

    /**
     * Show the dialog allowing the user to choose a Credential. This method shoud only be called
     * after you receive the {@link SmartLockListener#onShouldShowCredentialPicker()} callback.
     */
    public void showCredentialPicker() {
        CredentialRequest request = buildCredentialRequest();
        Activity activity = getFragment().getActivity();
        int maskedCode = getFragment().maskRequestCode(RC_READ);

        Auth.CredentialsApi.request(getFragment().getGoogleApiClient(), request)
                .setResultCallback(new ResolvingResultCallbacks<CredentialRequestResult>(activity, maskedCode) {
                    @Override
                    public void onSuccess(CredentialRequestResult result) {
                        getListener().onCredentialRetrieved(result.getCredential());
                    }

                    @Override
                    public void onUnresolvableFailure(Status status) {
                        Log.e(TAG, "showCredentialPicker:onUnresolvableFailure:" + status);
                    }
                });
    }

    /**
     * Call this method if the user signs out of your application to disable auto sign-in on the
     * next run. This prevents users from getting stuck in a loop between sign-out and auto sign-in.
     */
    public void signOut() {
        Auth.CredentialsApi.disableAutoSignIn(getFragment().getGoogleApiClient())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d(TAG, "signOut:onResult:" + status);
                    }
                });
    }

    /**
     * Save a new Credential to the user's SmartLock. This will be retrievable by all instances
     * of your application across devices so the user can avoid typing in his/her username and
     * password in the future.
     * @param id the id for the Credential, usually a username or email address.
     * @param password the password for the Credential.
     */
    public void save(@NonNull String id, String password) {
        Credential credential = new Credential.Builder(id)
                .setPassword(password)
                .build();

        // TODO(samstern): Should I notify the calling application on the success/failure of this
        //                 operation. If so, also need to do it in handleActivityResult.
        Activity activity = getFragment().getActivity();
        int maskedCode = getFragment().maskRequestCode(RC_SAVE);
        Auth.CredentialsApi.save(getFragment().getGoogleApiClient(), credential)
                .setResultCallback(new ResolvingResultCallbacks<Status>(activity, maskedCode) {
                    @Override
                    public void onSuccess(Status status) {
                        Log.d(TAG, "save:onSuccess:" + status);
                    }

                    @Override
                    public void onUnresolvableFailure(Status status) {
                        Log.d(TAG, "save:onUnresolvableFailure:" + status);
                    }
                });
    }

    /**
     * Delete a saved Credential object from the SmartLock store.
     * @param credential the Credential to delete.
     */
    public void delete(Credential credential) {
        Auth.CredentialsApi.delete(getFragment().getGoogleApiClient(), credential)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d(TAG, "delete:onResult:" + status);
                    }
                });
    }

    private CredentialRequest buildCredentialRequest() {
        return new CredentialRequest.Builder()
                .setCredentialPickerConfig(new CredentialPickerConfig.Builder()
                    .setShowAddAccountButton(false)
                    .setShowCancelButton(true)
                    .setForNewAccount(false)
                    .build())
                .setPasswordLoginSupported(true)
                .build();
    }
}
