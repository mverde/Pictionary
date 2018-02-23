package com.martin.pictionary2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "MainActivity";

    public static final int RC_SIGN_IN = 9001;
    public static final int RC_SELECT_PLAYERS = 9002;

    GoogleSignInAccount mGoogleSignInAccount = null;
    GoogleSignInOptions mGoogleSignInOptions = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.invite_players_button).setOnClickListener(this);
        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestProfile()
                .build();
        if (isSignedIn()) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
            findViewById(R.id.invite_players_button).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isSignedIn()) {
            Log.i(TAG, "Could not get last signed in account while resuming");
            signInSilently();
        } else {
            mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
            Log.i(TAG, "Got last signed in account: " + mGoogleSignInAccount.getDisplayName());
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sign_in_button) {
            // start the asynchronous sign in flow
            startSignInIntent();
        } else if (view.getId() == R.id.sign_out_button) {
            // sign out.
            signOut();
            // show sign-in button, hide the sign-out button
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            findViewById(R.id.invite_players_button).setVisibility(View.GONE);
        } else if (view.getId() == R.id.invite_players_button) {
            invitePlayers();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                mGoogleSignInAccount = result.getSignInAccount();
                Log.i(TAG, "Successfully logged in as: " + mGoogleSignInAccount.getDisplayName());
                findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
                findViewById(R.id.invite_players_button).setVisibility(View.VISIBLE);
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = result.getStatus().getStatusCode() + ": "
                            + getString(R.string.signin_other_error);
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        signOut();
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    private void signInSilently() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions);
        signInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            // The signed in account is stored in the task's result.
                            mGoogleSignInAccount = task.getResult();
                            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
                            findViewById(R.id.invite_players_button).setVisibility(View.VISIBLE);
                            Log.i(TAG, "Successfully signed in silently as: " + mGoogleSignInAccount.getDisplayName());
                        } else {
                            // Player will need to sign-in explicitly using via UI
                            startSignInIntent();
                            Log.i(TAG, "Could not sign in silently; using an intent");
                        }
                    }
                });
    }

    private void startSignInIntent() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private void signOut() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // at this point, the user is signed out.
                        Log.i(TAG, "Signing out of Google Play Games");
                    }
                });
    }

    private void invitePlayers() {
        // launch the player selection screen
        // minimum: 1 other player; maximum: 3 other players
        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getSelectOpponentsIntent(1, 3, true)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_SELECT_PLAYERS);
                    }
                });
    }
}
