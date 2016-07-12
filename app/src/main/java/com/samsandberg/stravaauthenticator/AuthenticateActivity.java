package com.samsandberg.stravaauthenticator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.wuman.android.auth.AuthorizationDialogController;
import com.wuman.android.auth.AuthorizationFlow;
import com.wuman.android.auth.DialogFragmentController;
import com.wuman.android.auth.OAuthManager;
import com.wuman.android.auth.oauth2.store.SharedPreferencesCredentialStore;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

public class AuthenticateActivity extends FragmentActivity {
    private static final Logger LOGGER = Logger.getLogger(StravaConstants.TAG);

    public static final String EXTRA_ACCESS_TOKEN = "access_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        if (handleCachedToken()) {
            // If cached token handled, exit early
            return;
        }

        FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentById(android.R.id.content) == null) {
            OAuthFragment fragment = new OAuthFragment();
            fm.beginTransaction().add(android.R.id.content, fragment).commit();
        }
    }

    @Override
    protected void onDestroy() {
        //Crouton.cancelAllCroutons();
        super.onDestroy();
    }

    /**
     * Lookup cached token if necessary, check it if necessary, start MainActivity if all good.
     * Return whether MainActivity started
     *
     * @return
     */
    private boolean handleCachedToken() {
        if (! do_use_cache()) {
            return false;
        }
        String token = getCachedAccessToken();
        if (token == null) {
            return false;
        }
        if (do_check_token() && ! checkToken(token)) {
            Toast.makeText(this, "Invalid token! TODO: Something!", Toast.LENGTH_SHORT).show();
            return false;
        }
        startMainActivity(token);
        return true;
    }

    /**
     * Should we use the local cache?
     * @return
     */
    private boolean do_use_cache() {
        return Boolean.valueOf(getString(R.string.strava_auth_use_cache));
    }

    /**
     * Should we check a token (against Strava's API) or should we just assume it's good?
     * @return
     */
    private boolean do_check_token() {
        return Boolean.valueOf(getString(R.string.strava_auth_check_token));
    }

    private String getCachedAccessToken() {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        return preferences.getString(StravaConstants.PREFS_KEY_ACCESS_TOKEN, null);
    }

    private void setCachedAccessToken(String token) {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(StravaConstants.PREFS_KEY_ACCESS_TOKEN, token);
        editor.commit();
    }

    /**
     * Actually check access token - maybe call some strava method with it?
     *
     * @param token
     * @return
     */
    private boolean checkToken(String token) {
        // TODO: Implement this!
        return true;
    }

    private void startMainActivity(String token) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_ACCESS_TOKEN, token);
        startActivity(intent);
        finish();
    }

    public static class OAuthFragment extends Fragment implements
            LoaderManager.LoaderCallbacks<AsyncResourceLoader.Result<Credential>> {

        private static final int LOADER_GET_TOKEN = 0;

        private OAuthManager oauth;

        private Button button;
        //private TextView message;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.oauth_login, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            button = (Button) view.findViewById(android.R.id.button1);
            setButtonText(R.string.button_login);
            //message = (TextView) view.findViewById(android.R.id.text1);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getLoaderManager().getLoader(LOADER_GET_TOKEN) == null) {
                        getLoaderManager().initLoader(LOADER_GET_TOKEN, null, OAuthFragment.this);
                    } else {
                        getLoaderManager().restartLoader(LOADER_GET_TOKEN, null,
                                OAuthFragment.this);
                    }
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
            String clientId = getString(R.string.strava_client_id);
            String clientSecret = getString(R.string.strava_client_secret);
            // setup credential store
            SharedPreferencesCredentialStore credentialStore =
                    new SharedPreferencesCredentialStore(getActivity(),
                            StravaConstants.CREDENTIALS_STORE_PREF_FILE, OAuth.JSON_FACTORY);
            // setup authorization flow
            AuthorizationFlow flow = new AuthorizationFlow.Builder(
                    BearerToken.queryParameterAccessMethod(),
                    OAuth.HTTP_TRANSPORT,
                    OAuth.JSON_FACTORY,
                    new GenericUrl(StravaConstants.URL_TOKEN),
                    new ClientParametersAuthentication(clientId, clientSecret),
                    clientId,
                    StravaConstants.URL_AUTHORIZE)
                    .setScopes(Arrays.asList(StravaScopes.SCOPE_PUBLIC))
                    .setCredentialStore(credentialStore)
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

        @Override
        public Loader<AsyncResourceLoader.Result<Credential>> onCreateLoader(int id, Bundle args) {
            getActivity().setProgressBarIndeterminateVisibility(true);
            button.setEnabled(false);
            //message.setText("");
            return new GetTokenLoader(getActivity(), oauth);
        }

        @Override
        public void onLoadFinished(Loader<AsyncResourceLoader.Result<Credential>> loader,
                                   AsyncResourceLoader.Result<Credential> result) {
            //message.setText(result.success ? result.data.getAccessToken() : "");
            //setButtonText(R.string.button_login);
            getActivity().setProgressBarIndeterminateVisibility(false);
            button.setEnabled(true);
            if (! result.success) {
                //Crouton.makeText(getActivity(), result.errorMessage, Style.ALERT).show();
                // Toast instead of Crouton dependency
                Toast.makeText(getActivity(), result.errorMessage, Toast.LENGTH_SHORT).show();
                return;
            }
            String token = result.data.getAccessToken();
            AuthenticateActivity activity = (AuthenticateActivity) getActivity();
            if (activity.do_check_token() && ! activity.checkToken(token)) {
                Toast.makeText(getActivity(), "Token check failed! TODO: Something!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (activity.do_use_cache()) {
                activity.setCachedAccessToken(token);
            }
            activity.startMainActivity(token);
        }

        @Override
        public void onLoaderReset(Loader<AsyncResourceLoader.Result<Credential>> loader) {
            //message.setText("");
            getActivity().setProgressBarIndeterminateVisibility(false);
            button.setEnabled(true);
        }

        @Override
        public void onDestroy() {
            getLoaderManager().destroyLoader(LOADER_GET_TOKEN);
            super.onDestroy();
        }

        private void setButtonText(int action) {
            button.setText(action);
            button.setTag(action);
        }

        private static class GetTokenLoader extends AsyncResourceLoader<Credential> {

            private final OAuthManager oauth;

            public GetTokenLoader(Context context, OAuthManager oauth) {
                super(context);
                this.oauth = oauth;
            }

            /**
             * TODO: Look into passing handler/callback to `oauth.authorizeExplicitly()`
             * (instead of using this async resource loader stuff)
             *
             * @return
             * @throws Exception
             */
            @Override
            public Credential loadResourceInBackground() throws Exception {
                Credential credential = oauth.authorizeExplicitly(StravaConstants.OAUTH_STORE_ID,
                        null, null).getResult();
                LOGGER.info("token: " + credential.getAccessToken());
                return credential;
            }

            @Override
            public void updateErrorStateIfApplicable(AsyncResourceLoader.Result<Credential> result) {
                Credential data = result.data;
                result.success = !TextUtils.isEmpty(data.getAccessToken());
                result.errorMessage = result.success ? null : "error";
            }
        }
    }
}