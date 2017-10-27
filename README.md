# EasyGoogle

## Project status
![status: inactive](https://img.shields.io/badge/status-inactive-red.svg)

This project is no longer actively maintained, and remains here as an archive of this work.

EasyGoogle was created as an experimental improvement to the developer experience for certain
Google APIs. In the time since its creation, most of these APIs have improved (and many in ways
that resemble EasyGoogle). For this reason it no longer makes sense to maintain this library.
Thank you to everyone who submitted issues or gave feedback, your thoughts influenced API
development at Google.

## Introduction

EasyGoogle is a wrapper library to simplify basic integrations with Google Play
Services.  The library wraps the following APIs (for now):

  * [Google Sign-In](https://developers.google.com/identity/sign-in/android/)
  * [Google Cloud Messaging](https://developers.google.com/cloud-messaging/)
  * [Google App Invites](https://developers.google.com/app-invites/)
  * [Google SmartLock for Passwords](https://developers.google.com/identity/smartlock-passwords/android/)

## Installation
EasyGoogle is installed by adding the following dependency to your
`build.gradle` file:

    dependencies {
      compile 'pub.devrel:easygoogle:0.2.5+'
    }

## Usage

### Enabling Services
Before you begin, visit [this page](https://developers.google.com/mobile/add)
to select Google services and add them to your Android app.  Make sure to enable any services
you plan to use and follow all of the steps, including modifying your `build.gradle` files
to enable the `google-services` plugin.

Once you have a `google-services.json` file in the proper place you can proceed to use
EasyGoogle.

### Basic
EasyGoogle makes use of `Fragments` to manage the lifecycle of the
`GoogleApiClient`, so any Activity which uses EasyGoogle must extend
`FragmentActivity`.

All interaction with EasyGoogle is through the `Google` class, which is
instantiated like this:

```java
public class MainActivity extends AppCompatActivity {

  private Google mGoogle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mGoogle = new Google.Builder(this).build();
  }

}
```

Of course, instantiating a `Google` object like this won't do anything at all,
you need to enable features individually.

### Sign-In
To enable Google Sign-In, call the appropriate method on `Google.Builder` and
implement the `SignIn.SignInListener` interface:

```java
 public class MainActivity extends AppCompatActivity implements
   SignIn.SignInListener {

   private Google mGoogle;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_main);

     // This is optional, pass 'null' if no ID token is required.
     String serverClientId = getString(R.string.default_web_client_id);

     mGoogle = new Google.Builder(this)
       .enableSignIn(this, serverClientId)
       .build();
   }

   @Override
   public void onSignedIn(GoogleSignInAccount account) {
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
 ```

Then, use the `SignIn` object from `mGoogle.getSignIn()` to access API methods
like `SignIn#getCurrentUser()`, `SignIn#signIn`, and `SignIn#signOut`.

### Cloud Messaging
To enable Cloud Messaging, you will have to implement a simple `Service` in your application.

First, pick a unique permission name and make the following string resource in your `strings.xml` file.
It is important to pick a unique name:

```xml
<string name="gcm_permission">your.unique.gcm.permission.name.here</string>
```

Next, add the following to your `AndroidManifest.xml` inside the `application` tag, making sure
that the value of the `android:permission` element is the same value you specified in your
`strings.xml` file above:

```xml
 <!-- This allows the app to receive GCM through EasyGoogle -->
 <service
     android:name=".MessagingService"
     android:enabled="true"
     android:exported="false"
     android:permission="your.unique.gcm.permission.name.here" />
 ```

Next, add the following permission to your `AndroidManifest.xml` file before the `application` tag,
replacing `<your-package-name>` with your Android package name:

```xml
    <permission android:name="<your-package-name>.permission.C2D_MESSAGE"
                android:protectionLevel="signature" />
    <uses-permission android:name="<your-package-name>.permission.C2D_MESSAGE" />
```

Then implement a class called `MessagingService` that extends `EasyMessageService`. Below is
one example of such a class:

```java
public class MessagingService extends EasyMessageService {

  @Override
  public void onMessageReceived(String from, Bundle data) {
      // If there is a running Activity that implements MessageListener, it should handle
      // this message.
      if (!forwardToListener(from, data)) {
          // There is no active MessageListener to get this, I should fire a notification with
          // a PendingIntent to an activity that can handle this.
          PendingIntent pendingIntent = createMessageIntent(from, data, MainActivity.class);
          Notification notif = new NotificationCompat.Builder(this)
                  .setContentTitle("Message from: " + from)
                  .setContentText(data.getString("message"))
                  .setSmallIcon(R.mipmap.ic_launcher)
                  .setContentIntent(pendingIntent)
                  .setAutoCancel(true)
                  .build();

          NotificationManager notificationManager =
                  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
          notificationManager.notify(0, notif);
      }
  }

  @Override
  public void onNewToken(String token) {
      // Send a registration message to the server with our new token
      String senderId = getString(R.string.gcm_defaultSenderId);
      sendRegistrationMessage(senderId, token);
  }
}
```

Note the use of the helper methods `forwardToListener` and `createMessageIntent`, which make
it easier for you to either launch an Activity or create a Notification to handle the message.

The `forwardToListener` method checks to see if there is an Activity that implements
`Messaging.MessagingListener` in the foreground.  If there is, it sends the GCM message to the
Activity to be handled.  To implement `Messaging.MessagingListener`, call the appropriate
method on `Google.Builder` in your `Activity` and implement the interface:

```java
public class MainActivity extends AppCompatActivity implements
  Messaging.MessagingListener {

  private Google mGoogle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mGoogle = new Google.Builder(this)
      .enableMessaging(this, getString(R.string.gcm_defaultSenderId))
      .build();
  }

  @Override
  public void onMessageReceived(String from, Bundle message) {
      // GCM message received.
  }
}
```

Then, use the `Messaging` object from `mGoogle.getMessaging()` to access API
methodslike `Messaging#send`.

### App Invites
To enable App Invites, call the appropriate method on `Google.Builder` and
implement the `AppInvites.AppInviteListener` interface:

```java
public class MainActivity extends AppCompatActivity implements
  AppInvites.AppInviteListener {

  private Google mGoogle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

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
```

Then, use the `AppInvites` object from `mGoogle.getAppInvites()` to access API
methods like `AppInvites#sendInvitation`.

### SmartLock for Passwords
To enable Smart Lock for Passwords, call the appropriate method on `Google.Builder` and
implement the `SmartLock.SmartLockListener` interface:

```java
public class MainActivity extends AppCompatActivity implements
  SmartLock.SmartLockListener {

  private Google mGoogle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mGoogle = new Google.Builder(this)
      .enableSmartLock(this)
      .build();
  }

  @Override
  public void onCredentialRetrieved(Credential credential) {
    // Successfully retrieved a Credential for the current device user.
  }

  @Override
  public void onShouldShowCredentialPicker() {
    // In order to retrieve a Credential, the app must show the picker dialog
    // using the SmartLock#showCredentialPicker() method.
  }

  @Override
  public void onCredentialRetrievalFailed() {
    // The user has no stored credentials, or the retrieval operation failed or
    // was canceled.
  }

}
```

Then, use the `SmartLock` object from `mGoogle.getSmartLock()` to access API
methods like `SmartLock#getCredentials()` and `SmartLock#save()`.

### Advanced Usage
If you would like to perform some action using one of the enabled Google
services but it is not properly wrapped by the EasyGoogle library, just call
`Google#getGoogleApiClient()` to get access to the underlying `GoogleApiClient`
held by the `Google` object.
