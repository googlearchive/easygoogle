package pub.devrel.easygoogle.gcm;

import android.os.Bundle;

import pub.devrel.easygoogle.Google;

public class Messaging {

    private MessagingFragment mFragment;

    public Messaging(MessagingFragment fragment) {
        mFragment = fragment;
    }

    public interface MessagingListener {
        public void onMessageReceived(String from, Bundle message);
    }

    public void setListener(MessagingListener listener) {
        mFragment.setMessagingListener(listener);
    }

    public void send(Bundle bundle) {
        mFragment.send(bundle);
    }

    public void setSenderId(String senderId) {
        mFragment.setSenderId(senderId);
        mFragment.register();
    }

    public MessagingFragment getFragment() {
        return mFragment;
    }

}
