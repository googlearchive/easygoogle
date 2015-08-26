package pub.devrel.easygoogle.gcm;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class GCMUtils {

    private static final String TAG = "GCMUtils";

    public static final String PERMISSION_EASY_GCM = "pub.devrel.easygoogle.GCM";

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

        // Get components that have the EASY GCM permission
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
