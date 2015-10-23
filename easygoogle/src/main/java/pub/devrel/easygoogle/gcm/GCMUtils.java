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
package pub.devrel.easygoogle.gcm;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities used by EasyGoogle GCM classes.
 */
public class GCMUtils {

    private static final String TAG = "GCMUtils";
    public static final String PREF_KEY_GCM_PERMISSION = "gcm_permission";

    /**
     * Find all services in the AndroidManifest with a given permission. This is useful when
     * you want to start a particular service but don't have enough information to build
     * an explicit intent (implicit intents stopped working in Android 5.0).
     * @param context calling Context.
     * @param permission the permission the service should have declared in the
     *                   {@code android:permission} field.
     * @return a list of {@code ComponentName} objects that can be used to create Intents.
     */
    public static List<ComponentName> findServices(Context context, String permission) {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        Log.d(TAG, "Checking package: " + packageName);

        // Find all services in the package
        PackageInfo servicesInfo;
        try {
            servicesInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SERVICES);
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException("Could not get services for package " + packageName);
        }

        // Get components that have the permission
        ArrayList<ComponentName> results = new ArrayList<>();
        ServiceInfo[] services = servicesInfo.services;
        if (services != null) {
            for (ServiceInfo service : services) {
                if (permission.equals(service.permission)) {
                    ComponentName cn = new ComponentName(packageName, service.name);
                    results.add(cn);
                }
            }
        }

        return results;
    }
}
