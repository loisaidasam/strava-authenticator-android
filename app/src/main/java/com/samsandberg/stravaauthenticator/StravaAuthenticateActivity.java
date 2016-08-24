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
    private static final Logger LOGGER = Logger.getLogger(StravaConstants.TAG);

    public static final String EXTRA_ACCESS_TOKEN = "access_token";

    public static final JsonFactory JSON_FACTORY = new JacksonFactory();
    public static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();


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
     * (default True)
     */
    protected boolean getStravaUseCache() {
        return true;
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
        // Example: return new Intent(this, ExampleMainActivity.class);
        return null;
    }

    /**
     * Methods override END
     ****************************************/

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
        String token = getCachedAccessToken();
        if (token == null) {
            return false;
        }
        if (getStravaCheckToken() && ! checkToken(token)) {
            Toast.makeText(this, "Invalid token! TODO: Something!", Toast.LENGTH_SHORT).show();
            return false;
        }
        startMainActivity(token);
        return true;
    }

    protected String getCachedAccessToken() {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        return preferences.getString(StravaConstants.PREFS_KEY_ACCESS_TOKEN, null);
    }

    protected void setCachedAccessToken(String token) {
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
    protected boolean checkToken(String token) {
        // TODO: Implement this!
        return true;
    }

    protected void startMainActivity(String token) {
        Intent intent = getStravaActivityIntent();
        if (intent == null) {
            Toast.makeText(this, "getStravaActivityIntent() returned null! TODO: Something!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        intent.putExtra(EXTRA_ACCESS_TOKEN, token);
        startActivity(intent);
        // TODO: finish()?
        finish();
    }

    public static class OAuthFragment extends Fragment implements
            LoaderManager.LoaderCallbacks<AsyncResourceLoader.Result<Credential>> {

        private static final int LOADER_GET_TOKEN = 0;

        private OAuthManager oauth;

        private Button button;

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

        @Override
        public Loader<AsyncResourceLoader.Result<Credential>> onCreateLoader(int id, Bundle args) {
            getActivity().setProgressBarIndeterminateVisibility(true);
            button.setEnabled(false);
            return new GetTokenLoader(getActivity(), oauth);
        }

        @Override
        public void onLoadFinished(Loader<AsyncResourceLoader.Result<Credential>> loader,
                                   AsyncResourceLoader.Result<Credential> result) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            button.setEnabled(true);
            if (! result.success) {
                Toast.makeText(getActivity(), result.errorMessage, Toast.LENGTH_SHORT).show();
                return;
            }
            String token = result.data.getAccessToken();
            StravaAuthenticateActivity activity = (StravaAuthenticateActivity) getActivity();
            if (activity.getStravaCheckToken() && ! activity.checkToken(token)) {
                Toast.makeText(getActivity(), "Token check failed! TODO: Something!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (activity.getStravaUseCache()) {
                activity.setCachedAccessToken(token);
            }
            activity.startMainActivity(token);
        }

        @Override
        public void onLoaderReset(Loader<AsyncResourceLoader.Result<Credential>> loader) {
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