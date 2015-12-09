# 0.2.0

  * Upgraded to Google Play Services version 8.3.0.
  * Upgraded gradle plugin version, target SDK version, and appcompat version.
  * Updated SignIn to use new `GoogleSignIn` API and added the method `SignIn#getCurrentUser`.
  * Removed dependency on `play-services-plus` and replaced all instances of `Person` with
    `GoogleSignInAccount` (breaking interface change).

# 0.1.0

  * Initial Release
