package pub.devrel.easygoogle.gac;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.util.Arrays;
import java.util.List;

import pub.devrel.easygoogle.Google;

public class SignIn extends GacService {

    public interface SignInListener {
        void onSignedIn(Person person);
        void onSignedOut();

        // TODO(samstern): when should I call this?
        void onSignInFailed();
    }

    private static final String TAG = SignIn.class.getSimpleName();

    public static final int RC_SIGN_IN = 9001;

    public SignIn(GacFragment fragment) {
        super(fragment);
    }

    @Override
    public List<Api> getApis() {
        return Arrays.asList(new Api[]{Plus.API});
    }

    @Override
    public List<Scope> getScopes() {
        // TODO(samstern): remove login scope
        return Arrays.asList(new Scope("profile"), new Scope("email"), Plus.SCOPE_PLUS_LOGIN);
    }

    @Override
    public void onConnected() {
        Person currentPerson = Plus.PeopleApi.getCurrentPerson(
                getFragment().getGoogleApiClient());
        getFragment().getSignInListener().onSignedIn(currentPerson);
    }

    @Override
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != Activity.RESULT_OK) {
                getFragment().setShouldResolve(false);
            }

            getFragment().setIsResolving(false);
            getFragment().getGoogleApiClient().connect();

            return true;
        }

        return false;
    }

    public static void signIn(Google google) {
        Log.d(TAG, "signIn: " + google);
        GacFragment fragment = google.getGacFragment();

        fragment.setShouldResolve(true);
        fragment.setResolutionCode(RC_SIGN_IN);
        fragment.getGoogleApiClient().reconnect();
    }

    public static void signOut(Google google) {
        Log.d(TAG, "signOut: " + google);
        final GacFragment fragment = google.getGacFragment();

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
                            fragment.getSignInListener().onSignedOut();
                        } else {
                            Log.w(TAG, "Could not sign out: " + status);
                        }
                    }
                });
    }
}
