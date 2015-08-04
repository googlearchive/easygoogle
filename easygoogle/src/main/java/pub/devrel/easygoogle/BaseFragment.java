package pub.devrel.easygoogle;


import android.app.Activity;
import android.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {

    Activity mActivity;

    public BaseFragment() {
        // Required empty public constructor
    }

    public static void bind(Activity activity, Fragment fragment, String tag) {
        activity.getFragmentManager().beginTransaction().add(fragment, tag).commit();
    }

    public static void bind(Activity activity, Fragment fragment, int containerId) {
        activity.getFragmentManager().beginTransaction().add(containerId, fragment).commit();
    }

}
