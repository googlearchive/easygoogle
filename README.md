# EasyGoogle
EasyGoogle is a wrapper library to simplify basic integrations with Google Play
Services.  The library wraps the following APIs (for now):

  * [Google Sign-In](https://developers.google.com/identity/sign-in/android/)
  * [Google Cloud Messaging](https://developers.google.com/cloud-messaging/)
  * [Google App Invites](https://developers.google.com/app-invites/)

## Installation
EasyGoogle is installed by adding the following dependency to your 
`build.gradle` file:

    dependencies {
      compile 'pub.devrel:easygoogle:0.1'
    }

## Usage

### Enabling Services
Before you begin, visit [this page](https://developers.google.com/mobile/add) 
to select Google services and add them to your Android app. Once you have
a `google-services.json` file in the proper place you can proceed to use
EasyGoogle.

### Basic
EasyGoogle makes use of `Fragments` to manage the lifecycle of the
`GoogleApiClient`, so any Activity which uses EasyGoogle must extend
`FragmentActivity`.  

All interaction with EasyGoogle is through the `Google` class, which is 
instantiated like this:

    public class MainActivity extends AppCompatActivity {

      private Google mGoogle;

      @Override
      protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogle = new Google.Builder(this)
          .build();
      }

    }

Of course, instantiating a `Google` object like this won't do anything at all,
you need to enable features individually.

### Sign-In
To enable Google Sign-In, call the appropriate method on `Google.Builder` and
implement the `SignIn.SignInListener` interface:

    public class MainActivity extends AppCompatActivity implements
      SignIn.SignInListener {

      private Google mGoogle;

      @Override
      protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogle = new Google.Builder(this)
          .enableSignIn(this)
          .build();
      }

      @Override
      public void onSignedIn(Person person) {
          // Sign in was successful.
      }

      @Override
      public void onSignedOut() {
          // Sign out was successful.
      }

      @Override
      public void onSignInFailed() {
          // Sign in failed for some reason and should not be attempted again
          // unless the user requests it.
      }

    }

Then, use the `SignIn` object from `mGoogle.getSignIn()` to access API methods
like `SignIn#signIn` and `SignIn#signOut`.

### Cloud Messaging
To enable Cloud Messaging, call the appropriate method on `Google.Builder` and
implement the `Messaging.MessagingListener` interface:

    public class MainActivity extends AppCompatActivity implements
      Messaging.MessagingListener {

      private Google mGoogle;

      @Override
      protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogle = new Google.Builder(this)
          .enableMessaging(this, getString(R.string.gcm_defaultSenderId))
          .build();
      }

      @Override
      public void onMessageReceived(String from, Bundle message) {
          // GCM message received.
      }

    }

Then, use the `Messaging` object from `mGoogle.getSignIn()` to access API
methodslike `Messaging#send`.

### App Invites
To enable App Invites, call the appropriate method on `Google.Builder` and
implement the `AppInvites.AppInviteListener` interface:

    public class MainActivity extends AppCompatActivity implements
      AppInvites.AppInviteListener {

      private Google mGoogle;

      @Override
      protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogle = new Google.Builder(this)
          .enableAppInvites(this)
          .build();
      }

      @Override
      public void onInvitationReceived(String invitationId, String deepLink) {
          // Invitation recieved in the app.
      }

      @Override
      public void onInvitationsSent(String[] ids) {
          // The user selected contacts and invitations sent successfully.
      }

      @Override
      public void onInvitationsFailed() {
          // The user either canceled sending invitations or they failed to
          // send due to some configuration error.
      }

    }

Then, use the `AppInvites` object from `mGoogle.getAppInvites()` to access API 
methods like `AppInvites#sendInvitation`.

### Advanced Usage
If you would like to perform some action using one of the enabled Google 
services but it is not properly wrapped by the EasyGoogle library, just call
`Google#getGoogleApiClient()` to get access to the underlying `GoogleApiClient`
held by the `Google` object.
