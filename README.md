Strava Authenticator for Android
===============================================================

[![ghit.me](https://ghit.me/badge.svg?repo=loisaidasam/strava-authenticator-android)](https://ghit.me/repo/loisaidasam/strava-authenticator-android)


A shell for building Android apps with Strava authentication.

![https://raw.githubusercontent.com/loisaidasam/strava-authenticator-android/master/assets/logo.jpg](https://raw.githubusercontent.com/loisaidasam/strava-authenticator-android/master/assets/logo.jpg)

Installation
============

[ ![Download](https://api.bintray.com/packages/loisaidasam/maven/strava-authenticator-android/images/download.svg) ](https://bintray.com/loisaidasam/maven/strava-authenticator-android/_latestVersion)

Available via `jcenter()`:

Gradle:

Add the following to your Android application's `build.gradle` file:

```gradle
dependencies {
  compile 'com.samsandberg.stravaauthenticator:strava-authenticator-android:1.1.0'
}
```

OR

Maven:

```maven
<dependency>
  <groupId>com.samsandberg.stravaauthenticator</groupId>
  <artifactId>strava-authenticator-android</artifactId>
  <version>1.1.0</version>
  <type>pom</type>
</dependency>
```

Setup
=====

In your Android app, create an activity that extends [StravaAuthenticateActivity](https://github.com/loisaidasam/strava-authenticator-android/blob/master/strava-authenticator-android/src/main/java/com/samsandberg/stravaauthenticator/StravaAuthenticateActivity.java), and override a few of the protected methods, in the very least: `getStravaClientId()`, `getStravaClientSecret()`, and `getStravaActivityIntent()`.

Included is an example app with an [ExampleAuthActivity](https://github.com/loisaidasam/strava-authenticator-android/blob/master/example/src/main/java/com/samsandberg/stravaauthenticatorexample/ExampleAuthActivity.java) that redirects to [ExampleMainActivity](https://github.com/loisaidasam/strava-authenticator-android/blob/master/example/src/main/java/com/samsandberg/stravaauthenticatorexample/ExampleMainActivity.java) on successful auth.


Attribution
===========

Some boilerplate OAuth code used from Android OAuth Client Library (android-oauth-client)

https://github.com/wuman/android-oauth-client

Copyright 2013 David Wu
Copyright (C) 2010 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


Logo
====

By Source (WP:NFCC#4), Fair use, https://en.wikipedia.org/w/index.php?curid=48247714


Resources
=========

- [https://github.com/wuman/android-oauth-client](https://github.com/wuman/android-oauth-client)
- [http://search.maven.org/#search|gav|1|g:"com.wu-man" AND a:"android-oauth-client"](http://search.maven.org/#search|gav|1|g:"com.wu-man" AND a:"android-oauth-client")
- [http://strava.github.io/api/v3/oauth/](http://strava.github.io/api/v3/oauth/)
- [https://www.strava.com/settings/api](https://www.strava.com/settings/api)
