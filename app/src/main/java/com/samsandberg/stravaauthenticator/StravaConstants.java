package com.samsandberg.stravaauthenticator;

/**
 * Constants for Strava's OAuth implementation.
 *
 * https://strava.github.io/api/v3/oauth/
 * http://labs.strava.com/developers/
 * https://www.strava.com/settings/api
 */
public class StravaConstants {
    public static final String TAG = "StravaAuthenticator";

    public static final String CREDENTIALS_STORE_PREF_FILE = "oauth";

    public static final String URL_AUTHORIZE = "https://www.strava.com/oauth/authorize";
    public static final String URL_TOKEN = "https://www.strava.com/oauth/token";
    public static final String URL_REDIRECT = "http://localhost/Callback";

    private StravaConstants() {
    }

}
