package home.mockaraokev2.Actitivy;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionSnippet;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import home.mockaraokev2.Class.Constant;
import home.mockaraokev2.MainActivity;
import home.mockaraokev2.R;

public class Act_Login extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {
    //huynh bao quoc...
    private static final String TAG = "Act_Login";
    private static final String KEY_ACCOUNT = "key_account";
    public static final String KEY_SHARE = "InfoEmail";
    public static final String NAME_ACCOUNT = "NAME";
    public static final String MAIL_ACCOUNT = "MAIL";
    public static final String photoEmail = "PHOTO";
    // Request code
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_RECOVERABLE = 9002;

    // Global instance of the HTTP transport
    private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

    // Global instance of the JSON factory
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private GoogleApiClient mGoogleApiClient;

    private Account mAccount;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login);
        if (savedInstanceState != null) {
            mAccount = savedInstanceState.getParcelable(KEY_ACCOUNT);
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);

        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);

                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_ACCOUNT, mAccount);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            handleSignInResult(result);
        }

        // Handling a user-recoverable auth exception
        if (requestCode == RC_RECOVERABLE) {
            if (resultCode == RESULT_OK) {
                getSubscriptions();
            } else {
                Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Get the account from the sign in result
            GoogleSignInAccount acct = result.getSignInAccount();
            String name = acct != null ? acct.getDisplayName() : null;
            String email = acct.getEmail();
            Uri photoUri = acct.getPhotoUrl();
            Intent it = new Intent(this, MainActivity.class);
            SharedPreferences pre = getSharedPreferences(KEY_SHARE, MODE_PRIVATE);
            SharedPreferences.Editor editor = pre.edit();
            editor.putString(NAME_ACCOUNT, name);
            editor.putString(MAIL_ACCOUNT, email);
            //editor.putString(photoEmail, String.valueOf(photoUri));
            editor.commit();
            startActivity(it);
            mAccount = acct.getAccount();
            Log.v(TAG, "account : " + mAccount);
            getSubscriptions();
        } else {
            Log.v(TAG, "fail : " + result.getStatus());
            mAccount = null;
        }
    }

    private void getSubscriptions() {
        if (mAccount == null) {
            Log.w(TAG, "getContacts: null account");
            return;
        }
        GetSubscriptionTask sub = new GetSubscriptionTask();
        sub.execute(mAccount);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("loading");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    public void xxx(View view) {
        startActivity(new Intent(Act_Login.this, MainActivity.class));
    }

    private class GetSubscriptionTask extends AsyncTask<Account, Void, List<Subscription>> {

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected List<Subscription> doInBackground(Account... params) {
            try {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        Act_Login.this,
                        Collections.singleton(Constant.YOUTUBE_SCOPE));
                credential.setSelectedAccount(params[0]);

                YouTube youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName("Google Sign In Quickstart")
                        .build();
                String channelId = "UC6-iiAvpLENHaflu1FGEctA";
                ResourceId resourceId = new ResourceId();
                resourceId.setChannelId(channelId);
                resourceId.setKind("youtube#channel");
                SubscriptionSnippet snippet = new SubscriptionSnippet();
                snippet.setResourceId(resourceId);
                Subscription subscription = new Subscription();
                subscription.setSnippet(snippet);
                YouTube.Subscriptions.Insert subscriptionInsert =
                        youtube.subscriptions().insert("snippet,contentDetails", subscription);
                Subscription returnedSubscription = subscriptionInsert.execute();
                Log.d(TAG, "  - Id: " + returnedSubscription.getId());
                Log.d(TAG, "  - Title: " + returnedSubscription.getSnippet().getTitle());
                return null;
            } catch (UserRecoverableAuthIOException userRecoverableException) {
                userRecoverableException.printStackTrace();
                Log.w(TAG, "getSubscription:recoverable exception", userRecoverableException);
                startActivityForResult(userRecoverableException.getIntent(), RC_RECOVERABLE);
            } catch (IOException e) {
                e.printStackTrace();
                Log.w(TAG, "getSubscription:exception", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Subscription> subscriptions) {
            hideProgressDialog();

            if (subscriptions != null) {
                Log.d(TAG, "subscriptions : size=" + subscriptions.size());

                // Get names of all connections
                StringBuilder msg = new StringBuilder();
                for (int i = 0; i < subscriptions.size(); i++) {
                    Log.v(TAG, "subscription : " + subscriptions.get(i).getId());
                }
                // Display names
            } else {
                Log.d(TAG, "subscriptions: null");
            }
        }
    }

}