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

import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Scope;

import java.util.List;

import pub.devrel.easygoogle.Google;

/**
 * Simplified version of a GoogleApiClient "API".  Declares the scopes and APIs it will need,
 * reacts to onConnected events.  Implementers should offer static methods that accept an
 * {@link Google} as a parameter to represent very common API methods.
 */
public abstract class GacModule {

    private GacFragment mFragment;

    protected GacModule(GacFragment fragment) {
        mFragment = fragment;
    }

    protected GacFragment getFragment() {
        return mFragment;
    }

    public abstract boolean handleActivityResult(int requestCode, int resultCode, Intent data);

    public abstract List<Api> getApis();

    public Api.ApiOptions.HasOptions getOptionsFor(Api<? extends Api.ApiOptions> api) {
        return null;
    }

    public abstract List<Scope> getScopes();

    public abstract void onConnected();

    public abstract void onResolvableFailure(ConnectionResult connectionResult);

    public abstract void onUnresolvableFailure();

    public abstract void onStart();

    public abstract void onStop();
}
