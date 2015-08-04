package pub.devrel.easygoogle.gcm;

import android.os.Bundle;

import pub.devrel.easygoogle.Google;

public class Messaging {

    public interface MessagingListener {
        public void onMessageReceived(String from, Bundle message);
    }

    public static void send(Google google, Bundle bundle) {
        google.getMessagingFragment().send(bundle);
    }

}
