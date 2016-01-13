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

import android.os.Bundle;


/**
 * Interface to the Google Cloud Messaging API, which can be used to deliver push notifications
 * and send messages back to a server. For more information visit:
 * https://developers.google.com/cloud-messaging/
 */
public class Messaging {

    /**
     * Listener to be notified of asynchronous GCM events, like message receipt.
     */
    public interface MessagingListener {

        /**
         * Called when a downstream GCM message is received.
         * @param from the sender's ID.
         * @param message arbitrary message data included by the sender.
         */
        void onMessageReceived(String from, Bundle message);
    }

    private MessagingFragment mFragment;

    public Messaging(MessagingFragment fragment) {
        mFragment = fragment;
    }

    /**
     * Send an upstream GCM message with some data.
     * @param bundle arbitrary key-value data to include in the message.
     */
    public void send(Bundle bundle) {
        mFragment.send(bundle);
    }

    /**
     * Subscribe to a GCM topic.
     * @see com.google.android.gms.gcm.GcmPubSub#subscribe(String, String, Bundle)
     * @param topic topic to subscribe to.
     */
    public void subscribeTo(String topic) {
        mFragment.subscribeTo(topic);
    }

    /**
     * Unsubscribe from a GCM topic.
     * @see com.google.android.gms.gcm.GcmPubSub#unsubscribe(String, String)
     * @param topic topic to unsubscribe from.
     */
    public void unsubscribeFrom(String topic) {
        mFragment.unsubscribeFrom(topic);
    }

    // TODO(afshar): remove or use unused methods
    public void setSenderId(String senderId) {
        mFragment.setSenderId(senderId);
        mFragment.register();
    }
}
