package pub.devrel.easygoogle.gac;

import android.content.Intent;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Scope;

import java.util.List;

import pub.devrel.easygoogle.Google;

/**
 * Simplified version of a GoogleApiClient "API".  Declares the scopes and APIs it will need,
 * reacts to onConnected events.  Implementers should offer static methods that accept an
 * {@link Google} as a parameter to represent very common API methods.
 */
public abstract class GacBase {

    private GacFragment mFragment;

    protected GacBase(GacFragment fragment) {
        mFragment = fragment;
    }

    protected GacFragment getFragment() {
        return mFragment;
    }

    public abstract boolean handleActivityResult(int requestCode, int resultCode, Intent data);

    public abstract List<Api> getApis();

    public abstract List<Scope> getScopes();

    public abstract void onConnected();
}
