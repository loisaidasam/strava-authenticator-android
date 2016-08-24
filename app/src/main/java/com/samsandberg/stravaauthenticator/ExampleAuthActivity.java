package com.samsandberg.stravaauthenticator;

import android.content.Intent;

import java.util.Arrays;
import java.util.Collection;


public class ExampleAuthActivity extends StravaAuthenticateActivity {

    /*****************************************
     * Methods override START
     */

    /**
     * Client ID
     */
    protected String getStravaClientId() {
        return "YOUR-STRAVA-CLIENT-ID";
    }

    /**
     * Client Secret
     */
    protected String getStravaClientSecret() {
        return "YOUR-STRAVA-CLIENT-SECRET";
    }

    /**
     * Scopes to auth for
     * (default public)
     */
    protected Collection<String> getStravaScopes() {
        return Arrays.asList(StravaScopes.SCOPE_PUBLIC);
    }

    /**
     * Should we use the local cache?
     * (default True)
     */
    protected boolean getStravaUseCache() {
        return false;
    }

    /**
     * Should we check a token (against Strava's API) or should we just assume it's good?
     * (default True)
     */
    protected boolean getStravaCheckToken() {
        return true;
    }

    /**
     * What intent should we kick off, given OK auth
     */
    protected Intent getStravaActivityIntent() {
        return new Intent(this, ExampleMainActivity.class);
    }

    /**
     * Methods override END
     ****************************************/
}
