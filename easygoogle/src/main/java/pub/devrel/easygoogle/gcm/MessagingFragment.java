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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Fragment to manage communication with the various GCM services and deliver messages and
 * other events to the host activity automatically.
 */
public class MessagingFragment extends Fragment {
    private static final String TAG = "MessagingFragment";

    public static final String SENDER_ID_ARG = "SENDER_ID";
    public static final String GCM_PERMISSION_ARG = "GCM_PERMISSION";
    public static final String MESSAGE_RECEIVED = "MESSAGE_RECEIVED";
    public static final String MESSAGE_FROM_FIELD = "MESSAGE_FROM";
    public static final String MESSAGE_ARG = "MESSAGE_ARG";

    public static final String GCM_PERMISSION_RES_NAME = "gcm_permission";

    private String mSenderId;
    private Messaging mMessaging;
    private Messaging.MessagingListener mListener;
    private BroadcastReceiver mReceiver;

    public static MessagingFragment newInstance() {
        return new MessagingFragment();
    }

    protected static void send(Context context, String senderId, Bundle data) {
        Intent intent = new Intent(context, MessageSenderService.class);
        intent.putExtra(SENDER_ID_ARG, senderId);
        intent.putExtra(MESSAGE_ARG, data);
        intent.putExtra(GCM_PERMISSION_ARG, getGcmPermissionName(context));
        context.startService(intent);
    }

    /**
     * Get R.string.gcm_permission from the calling app's resources
     * @param context calling app's context.
     * @return the resource value for R.string.gcm_permission, throws IllegalArgumentException
     * if this does not exist.
     */
    private static String getGcmPermissionName(Context context) {
        int gcmPermissionResourceId = context.getResources()
                .getIdentifier(GCM_PERMISSION_RES_NAME, "string", context.getPackageName());
        if (gcmPermissionResourceId == 0) {
            throw new IllegalArgumentException(
                    "Error: must define " + GCM_PERMISSION_RES_NAME + " in strings.xml");
        }

        String gcmPermissionName = context.getString(gcmPermissionResourceId);
        return gcmPermissionName;
    }

    public MessagingFragment() {
        super();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Sender ID
        if (getArguments() != null) {
            mSenderId = getArguments().getString(SENDER_ID_ARG);
        } else {
            Log.w(TAG, "getArguments() returned null, not setting senderId");
        }

        // Messaging permission store
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                .putString(GCMUtils.PREF_KEY_GCM_PERMISSION, getGcmPermissionName(getActivity()))
                .commit();

        mMessaging = new Messaging(this);
        if (mListener != null) {
            // TODO(afshar): how often do we want to do this?
            register();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getActivity().getIntent();
        if (intent != null && MESSAGE_RECEIVED.equals(intent.getAction())) {
            parseMessageIntent(intent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Register the local broadcast receiver
        registerReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister the local broadcast receiver
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    private void registerReceiver() {
        if (mReceiver == null) {
            mReceiver = new MessageBroadcastReceiver();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(MESSAGE_RECEIVED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);
    }

    public void register() {
        Intent intent = new Intent(getActivity(), IDRegisterService.class);
        intent.putExtra(SENDER_ID_ARG, mSenderId);
        intent.putExtra(GCM_PERMISSION_ARG, getGcmPermissionName(getActivity()));
        getActivity().startService(intent);
    }

    public void setMessagingListener(Messaging.MessagingListener messagingListener) {
        mListener = messagingListener;
    }

    public void setSenderId(String senderId) {
        mSenderId = senderId;
    }

    private void parseMessageIntent(Intent intent) {
        Bundle data = intent.getBundleExtra(MESSAGE_ARG);
        String from = intent.getStringExtra(MESSAGE_FROM_FIELD);
        onMessageReceived(from, data);
    }

    public void send(Bundle data) {
        send(getActivity(), mSenderId, data);
    }

    private void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "onMessageReceived:" + from + ":" + data);
        mListener.onMessageReceived(from, data);
    }

    public Messaging getMessaging() {
        return mMessaging;
    }

    private class MessageBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()) {
                case MESSAGE_RECEIVED:
                    parseMessageIntent(intent);
                    break;
            }
        }
    }
}
