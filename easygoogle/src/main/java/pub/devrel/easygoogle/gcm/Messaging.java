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

// TODO(afshar): remove or use unused methods
public class Messaging {

    public interface MessagingListener {
        void onMessageReceived(String from, Bundle message);
    }

    private MessagingFragment mFragment;

    public Messaging(MessagingFragment fragment) {
        mFragment = fragment;
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
}
