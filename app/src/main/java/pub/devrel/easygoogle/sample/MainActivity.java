package pub.devrel.easygoogle.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import pub.devrel.easygoogle.R;
import pub.devrel.easygoogle.gcm.MessagingFragment;

public class MainActivity extends AppCompatActivity {

    private MessagingFragment mMessaging;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMessaging = MessagingFragment.newInstance(this, getString(R.string.gcm_defaultSenderId));

        findViewById(R.id.send_message_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                b.putString("message", "I am a banana!");
                mMessaging.send(b);
            }
        });
    }

}
