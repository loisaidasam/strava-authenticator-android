package com.samsandberg.stravaauthenticator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.wuman.android.auth.AuthorizationDialogController;
import com.wuman.android.auth.AuthorizationFlow;
import com.wuman.android.auth.DialogFragmentController;
import com.wuman.android.auth.OAuthManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

public class StravaAuthenticateActivity extends FragmentActivity {

    /*****************************************
     * Methods override START
     */

    /**
     * Client ID
     */
    protected String getStravaClientId() {
        return null;
    }

    /**
     * Client Secret
     */
    protected String getStravaClientSecret() {
        return null;
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
     * (default true)
     */
    protected boolean getStravaUseCache() {
        return true;
    }

    /**
     * Should we check a token (against Strava's API) or should we just assume it's good?
     * (default true)
     */
    protected boolean getStravaCheckToken() {
        return true;
    }

    /**
     * What intent should we kick off, given OK auth
     */
    protected Intent getStravaActivityIntent() {
        // Example: return new Intent(this, ExampleMainActivity.class);
        return null;
    }

    /**
     * Should we finish this activity after successful auth + kicking off next activity?
     * (default true)
     */
    protected boolean getStravaFinishOnComplete() {
        return true;
    }

    /**
     * Methods override END
     ****************************************/


    private static final Logger LOGGER = Logger.getLogger(StravaConstants.TAG);

    public static final JsonFactory JSON_FACTORY = new JacksonFactory();
    public static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentById(android.R.id.content) == null) {
            OAuthFragment fragment = new OAuthFragment();
            fm.beginTransaction().add(android.R.id.content, fragment).commit();
        }

        handleCachedToken();
    }

    public static String getStravaAccessToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(StravaConstants.PREFS_NAME,
                Context.MODE_PRIVATE);
        return preferences.getString(StravaConstants.PREFS_KEY_ACCESS_TOKEN, null);
    }

    /**
     * Lookup cached token if necessary, check it if necessary, start activity if all good.
     * Return whether activity started.
     *
     * @return
     */
    protected boolean handleCachedToken() {
        if (! getStravaUseCache()) {
            return false;
        }
        String token = getStravaAccessToken(this);
        if (token == null) {
            return false;
        }
        // TODO: Make a CheckTokenTask
//        if (getStravaCheckToken() && ! checkToken(token)) {
//            Toast.makeText(this, "Invalid token! TODO: Something!", Toast.LENGTH_SHORT).show();
//            return false;
//        }
        startMainActivity(token);
        return true;
    }

    protected void setStravaAccessToken(String token) {
        SharedPreferences preferences = getSharedPreferences(StravaConstants.PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(StravaConstants.PREFS_KEY_ACCESS_TOKEN, token);
        editor.commit();
    }

    protected void startMainActivity(String token) {
        Intent intent = getStravaActivityIntent();
        if (intent == null) {
            Toast.makeText(this, "getStravaActivityIntent() returned null! TODO: Something!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
        if (getStravaFinishOnComplete()) {
            finish();
        }
    }

    public static class OAuthFragment extends Fragment {

        private OAuthManager oauth;

        private Button button;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.activity_auth, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            button = (Button) view.findViewById(android.R.id.button1);
            button.setText(R.string.button_login);
            button.setTag(R.string.button_login);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StravaAuthenticateActivity activity = (StravaAuthenticateActivity) getActivity();
                    new AuthTask(activity.getStravaCheckToken()).execute();
                }
            });
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setupOauth();
        }

        private void setupOauth() {
            boolean fullScreen = true;
            StravaAuthenticateActivity activity = (StravaAuthenticateActivity) getActivity();
            String clientId = activity.getStravaClientId();
            if (clientId == null) {
                Toast.makeText(activity, "clientId is null! TODO: Something!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String clientSecret = activity.getStravaClientSecret();
            if (clientSecret == null) {
                Toast.makeText(activity, "clientSecret is null! TODO: Something!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // setup authorization flow
            AuthorizationFlow flow = new AuthorizationFlow.Builder(
                    BearerToken.queryParameterAccessMethod(),
                    activity.HTTP_TRANSPORT,
                    activity.JSON_FACTORY,
                    new GenericUrl(StravaConstants.URL_TOKEN),
                    new ClientParametersAuthentication(clientId, clientSecret),
                    clientId,
                    StravaConstants.URL_AUTHORIZE)
                    .setScopes(activity.getStravaScopes())
                    .setRequestInitializer(new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request) throws IOException {}
                    })
                    .build();
            // setup UI controller
            AuthorizationDialogController controller =
                    new DialogFragmentController(getFragmentManager(), fullScreen) {
                        @Override
                        public String getRedirectUri() throws IOException {
                            return StravaConstants.URL_REDIRECT;
                        }

                        @Override
                        public boolean isJavascriptEnabledForWebView() {
                            return true;
                        }

                        @Override
                        public boolean disableWebViewCache() {
                            return false;
                        }

                        @Override
                        public boolean removePreviousCookie() {
                            return false;
                        }

                    };
            // instantiate an OAuthManager instance
            oauth = new OAuthManager(flow, controller);
        }

        /**
         * Simple result envelope class for AuthTask
         */
        private class AuthResult {
            public Credential credential;
            public String errorMessage;
            public boolean success;

            public AuthResult(Credential credential) {
                this.credential = credential;
                success = true;
            }

            public AuthResult(String errorMessage) {
                this.errorMessage = errorMessage;
                success = false;
            }
        }

        private class AuthTask extends AsyncTask<Void, Void, AuthResult> {

            private boolean stravaCheckToken;

            public AuthTask(boolean stravaCheckToken) {
                this.stravaCheckToken = stravaCheckToken;
            }

            /**
             * UI ish
             */
            protected void onPreExecute () {
                getActivity().setProgressBarIndeterminateVisibility(true);
                button.setEnabled(false);
            }

            /**
             * Actually check access token - maybe call some Strava method with it?
             *
             * @param token
             * @return
             */
            protected boolean checkToken(String token) {
                // TODO: Implement this!
                return true;
            }

            /**
             * Background work (not on UI thread)
             */
            protected AuthResult doInBackground(Void... params) {
                Credential credential;
                try {
                    credential = oauth.authorizeExplicitly(StravaConstants.OAUTH_STORE_ID, null,
                            null).getResult();
                } catch (IOException e) {
                    e.printStackTrace();
                    LOGGER.severe("Auth failed!");
                    return new AuthResult("Auth failed! TODO: Something! " + e.getMessage());
                }
                String token = credential.getAccessToken();
                LOGGER.info("token: " + token);
                if (TextUtils.isEmpty(token)) {
                    return new AuthResult("Auth failed, token is empty! TODO: Something!");
                }
                if (stravaCheckToken && ! checkToken(token)) {
                    return new AuthResult("Token check failed! TODO: Something!");
                }
                return new AuthResult(credential);
            }

            /**
             * UI ish
             */
            protected void onPostExecute(AuthResult result) {
                getActivity().setProgressBarIndeterminateVisibility(false);
                button.setEnabled(true);
                StravaAuthenticateActivity activity = (StravaAuthenticateActivity) getActivity();
                if (result.success) {
                    String token = result.credential.getAccessToken();
                    activity.setStravaAccessToken(token);
                    activity.startMainActivity(token);
                } else {
                    Toast.makeText(activity, "Error during auth: " + result.errorMessage,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}