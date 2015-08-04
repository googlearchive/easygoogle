package pub.devrel.easygoogle.gcm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class MessagingFragment extends Fragment {
    public static final String SENDER_ID_ARG = "SENDER_ID";
    public static final String SENT_TOKEN_TO_SERVER = "SENT_TOKEN_TO_SERVER";
    public static final String REGISTRATION_COMPLETE = "REGISTRATION COMPLETE";
    public static final String MESSAGE_RECEIVED = "MESSAGE_RECEIVED";
    public static final String MESSAGE_FROM_FIELD = "MESSAGE_FROM";
    private static final String TAG = "gf.MessagingFragment";
    public static final String MESSAGE_ARG = "MESSAGE_ARG";

    private String mSenderId;
    private Messaging.MessagingListener mListener;
    private Activity mContext;
    private BroadcastReceiver mReceiver;


    public static MessagingFragment newInstance(String senderId) {
        MessagingFragment fragment = new MessagingFragment();
        Bundle args = new Bundle();
        args.putString(SENDER_ID_ARG, senderId);
        fragment.setArguments(args);
        return fragment;
    }

    // Required empty public constructor
    public MessagingFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSenderId = getArguments().getString(SENDER_ID_ARG);
        createReceiver();
        register(mSenderId);
    }

    private void createReceiver() {
        BroadcastReceiver rec = new MessageBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(REGISTRATION_COMPLETE);
        filter.addAction(MESSAGE_RECEIVED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(rec, filter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        Log.d(TAG, "I AM AN ATTACHED BANANA");
        try {
            mListener = (Messaging.MessagingListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void register(String senderId) {
        Log.d(TAG, "register");
        Intent intent = new Intent(getActivity(), RegistrationService.class);
        intent.putExtra(SENDER_ID_ARG, mSenderId);
        mContext.startService(intent);
    }

    private class MessageBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "INTENT RECEIVED: " + intent.getAction());
            switch(intent.getAction()) {
                case REGISTRATION_COMPLETE:
                    return;
                case MESSAGE_RECEIVED:
                    Bundle data = intent.getBundleExtra(MESSAGE_ARG);
                    String from = intent.getStringExtra(MESSAGE_FROM_FIELD);
                    onMessageReceived(from, data);
                    return;
            }
        }
    }

    public void send(Bundle data) {
        Intent intent = new Intent(mContext, MessageSender.class);
        intent.putExtra(SENDER_ID_ARG, mSenderId);
        intent.putExtra(MESSAGE_ARG, data);
        getActivity().startService(intent);
    }

    private void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "Message: " + from + data);
        mListener.onMessageReceived(from, data);

    }

    public static String getSenderEmail(String senderId) {
        return senderId + "@gcm.googleapis.com";
    }

}
