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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

/**
 * Utility class for common Fragment operations.
 */
public class FragmentUtils extends Fragment {

    private static final String TAG = FragmentUtils.class.getSimpleName();

    /**
     * Check if an Activity already has an instance of a particular Fragment/Tag.  If so, return the
     * existing instance.  If it does not have one, add a new instance and return it.
     * @param activity the FragmentActivity to host the Fragment.
     * @param tag the Fragment tag, should be a unique string for each instance.
     * @param instance an instance of the desired Fragment sub-class, to add if necessary.
     * @param <T> a class that extends Fragment.
     * @return an instance of T which is added to the activity.
     */
    public static <T extends Fragment> T getOrCreate(FragmentActivity activity, String tag, T instance) {
        // TODO(samstern): I'd like to avoid having to ask for an instance but I'd also like to avoid
        //                 having to create an instance using reflection...

        T result = null;
        boolean shouldAdd = false;

        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment fragment = fm.findFragmentByTag(tag);
        if (fragment == null) {
            shouldAdd = true;
        } else {
            // TODO(samstern): how to be more confident about this cast?
            Log.d(TAG, "Found fragment instance: " + tag);
            result = (T) fragment;
        }

        if (shouldAdd) {
            Log.d(TAG, "Adding new Fragment: " + tag);

            // Use empty instance
            result = instance;
            ft.add(result, tag).disallowAddToBackStack().commit();
        }

        return result;
    }

}
