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

    public static final String URL_AUTHORIZE = "https://www.strava.com/oauth/authorize";
    public static final String URL_TOKEN = "https://www.strava.com/oauth/token";
    public static final String URL_REDIRECT = "http://localhost/Callback";

    public static final String PREFS_NAME = "strava_authenticator";
    public static final String PREFS_KEY_ACCESS_TOKEN = "strava_authenticator_access_token";

    public static final String OAUTH_STORE_ID = "token_strava";

    private StravaConstants() {
    }

}
