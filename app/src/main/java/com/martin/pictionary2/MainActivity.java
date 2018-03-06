package com.martin.pictionary2;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.martin.pictionary2.messages.GuessMessage;
import com.martin.pictionary2.messages.Message;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "MainActivity";

    private static final int RC_SIGN_IN = 9001;
    private static final int RC_SELECT_PLAYERS = 9002;
    private static final int RC_WAITING_ROOM = 9007;
    private static final int RC_INVITATION_INBOX = 9008;

    // at least 2 players required for our game
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 8;

    private GoogleSignInAccount mGoogleSignInAccount = null;
    private GoogleSignInOptions mGoogleSignInOptions = null;

    private RoomConfig mJoinedRoomConfig;
    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {
        @Override
        public void onRoomCreated(int code, @Nullable Room room) {
            // Update UI and internal state based on room updates.
            if (code == GamesCallbackStatusCodes.OK && room != null) {
                Log.d(TAG, "Room " + room.getRoomId() + " created.");
            } else {
                Log.w(TAG, "Error creating room: " + code);
                // let screen go to sleep
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            }
        }

        @Override
        public void onJoinedRoom(int code, @Nullable Room room) {
            // Update UI and internal state based on room updates.
            if (code == GamesCallbackStatusCodes.OK && room != null) {
                Log.d(TAG, "Room " + room.getRoomId() + " joined.");
                showWaitingRoom(room, MAX_PLAYERS);
            } else {
                Log.w(TAG, "Error joining room: " + code);
                // let screen go to sleep
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            }
        }

        @Override
        public void onLeftRoom(int code, @NonNull String roomId) {
            Log.d(TAG, "Left room" + roomId);
        }

        @Override
        public void onRoomConnected(int code, @Nullable Room room) {
            if (code == GamesCallbackStatusCodes.OK && room != null) {
                Log.d(TAG, "Room " + room.getRoomId() + " connected.");
                showWaitingRoom(room, MAX_PLAYERS);
            } else {
                Log.w(TAG, "Error connecting to room: " + code);
                // let screen go to sleep
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            }
        }
    };
    // are we already playing?
    boolean mPlaying = false;
    private Activity thisActivity = this;
    private Room mRoom;
    private String mMyParticipantId;

    private RoomStatusUpdateCallback mRoomStatusCallbackHandler = new RoomStatusUpdateCallback() {
        @Override
        public void onRoomConnecting(@Nullable Room room) {
            // Update the UI status since we are in the process of connecting to a specific room.
            mRoom = room;
            Log.i(TAG, "Room " + room.getRoomId() + " connecting");
        }

        @Override
        public void onRoomAutoMatching(@Nullable Room room) {
            // Update the UI status since we are in the process of matching other players.
            mRoom = room;
        }

        @Override
        public void onPeerInvitedToRoom(@Nullable Room room, @NonNull List<String> list) {
            // Update the UI status since we are in the process of matching other players.
            mRoom = room;
            Log.i(TAG, "Peer invited to room " + room.getRoomId());
        }

        @Override
        public void onPeerDeclined(@Nullable Room room, @NonNull List<String> list) {
            // Peer declined invitation, see if game should be canceled
            mRoom = room;
            Log.i(TAG, "Peer declined connecting to room " + room.getRoomId());
            if (!mPlaying && shouldCancelGame(room)) {
                Games.getRealTimeMultiplayerClient(thisActivity, mGoogleSignInAccount)
                        .leave(mJoinedRoomConfig, room.getRoomId());
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        @Override
        public void onPeerJoined(@Nullable Room room, @NonNull List<String> list) {
            // Update UI status indicating new players have joined!
            mRoom = room;
            Log.i(TAG, "Peer joined room " + room.getRoomId());
        }

        @Override
        public void onPeerLeft(@Nullable Room room, @NonNull List<String> list) {
            // Peer left, see if game should be canceled.
            mRoom = room;
            Log.i(TAG, "Peer left room " + room.getRoomId());
            if (!mPlaying && shouldCancelGame(room)) {
                Games.getRealTimeMultiplayerClient(thisActivity, mGoogleSignInAccount)
                        .leave(mJoinedRoomConfig, room.getRoomId());
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        @Override
        public void onConnectedToRoom(@Nullable Room room) {
            // Connected to room, record the room Id.
            mRoom = room;
            Log.i(TAG, "Connected to room " + room.getRoomId());
            Games.getPlayersClient(thisActivity, mGoogleSignInAccount)
                    .getCurrentPlayerId().addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String playerId) {
                    mMyParticipantId = mRoom.getParticipantId(playerId);
                }
            });
        }

        @Override
        public void onDisconnectedFromRoom(@Nullable Room room) {
            // This usually happens due to a network error, leave the game.
            Log.i(TAG, "Disconnected from room " + room.getRoomId());
            Games.getRealTimeMultiplayerClient(thisActivity, mGoogleSignInAccount)
                    .leave(mJoinedRoomConfig, room.getRoomId());
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            // show error message and return to main screen
            mRoom = null;
            mJoinedRoomConfig = null;
        }

        @Override
        public void onPeersConnected(@Nullable Room room, @NonNull List<String> list) {
            mRoom = room;
            Log.i(TAG, "Peers connected to room " + room.getRoomId());
            if (mPlaying) {
                // add new player to an ongoing game
            } else if (shouldStartGame(room)) {
                // set initial game state
                mPlaying = true;
            }
        }

        @Override
        public void onPeersDisconnected(@Nullable Room room, @NonNull List<String> list) {
            mRoom = room;
            Log.i(TAG, "Peers disconnected from room " + room.getRoomId());
            if (mPlaying) {
                // do game-specific handling of this -- remove player's avatar
                // from the screen, etc. If not enough players are left for
                // the game to go on, end the game and leave the room.
            } else if (shouldCancelGame(room)) {
                // cancel the game
                Games.getRealTimeMultiplayerClient(thisActivity, mGoogleSignInAccount)
                        .leave(mJoinedRoomConfig, room.getRoomId());
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        @Override
        public void onP2PConnected(@NonNull String participantId) {
            // Update status due to new peer to peer connection.
            Log.i(TAG, "P2P " + participantId + " connected");
        }

        @Override
        public void onP2PDisconnected(@NonNull String participantId) {
            // Update status due to  peer to peer connection being disconnected.
            Log.i(TAG, "P2P " + participantId + " disconnected");
        }
    };

    // called when a message is received
    private OnRealTimeMessageReceivedListener mMessageReceivedHandler =
            new OnRealTimeMessageReceivedListener() {
                @Override
                public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
                    // Handle messages received here.
                    byte[] message = realTimeMessage.getMessageData();
                    // process message contents...
                }
            };

    private RealTimeMultiplayerClient.ReliableMessageSentCallback mReliableMessageSentHandler =
            new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                @Override
                public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientId) {
                    // handle the message being sent.
                    /*synchronized (this) {
                        pendingMessageSet.remove(tokenId);
                    }*/
                }
            };

    private InvitationCallback mInvitationCallbackHandler = new InvitationCallback() {
        @Override
        public void onInvitationReceived(@NonNull Invitation invitation) {
            RoomConfig.Builder builder = RoomConfig.builder(mRoomUpdateCallback)
                    .setOnMessageReceivedListener(mMessageReceivedHandler)
                    .setInvitationIdToAccept(invitation.getInvitationId());
            mJoinedRoomConfig = builder.build();
            Games.getRealTimeMultiplayerClient(thisActivity, mGoogleSignInAccount)
                    .join(mJoinedRoomConfig);
            Log.i(TAG, "joined from oninvitationreceived");
            // prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        @Override
        public void onInvitationRemoved(@NonNull String invitationId) {
            // Invitation removed.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.invite_players_button).setOnClickListener(this);
        findViewById(R.id.invitations_button).setOnClickListener(this);
        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestProfile()
                .build();
        if (isSignedIn()) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
            findViewById(R.id.invite_players_button).setVisibility(View.VISIBLE);
            findViewById(R.id.invitations_button).setVisibility(View.VISIBLE);
            findViewById(R.id.guessText).setVisibility(View.VISIBLE);
            findViewById(R.id.guessesFeed).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            findViewById(R.id.invite_players_button).setVisibility(View.GONE);
            findViewById(R.id.invitations_button).setVisibility(View.GONE);
            findViewById(R.id.guessText).setVisibility(View.GONE);
            findViewById(R.id.guessesFeed).setVisibility(View.GONE);
        }
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        EditText editText = (EditText) findViewById(R.id.guessText);
        final LinearLayout guessesFeed = (LinearLayout) findViewById(R.id.guessesFeed);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    // set guess text in list view
                    TextView guessContent = new TextView(thisActivity);
                    guessContent.setText(v.getText().toString());
                    guessesFeed.addView(guessContent);
//                    GuessMessage guess = new GuessMessage(v.getText().toString(), mMyParticipantId);
//                    sendMessage(guess);
                    handled = true;
                }
                return handled;
            }
        });

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
            findViewById(R.id.invitations_button).setVisibility(View.GONE);
            findViewById(R.id.guessText).setVisibility(View.GONE);
            findViewById(R.id.guessesFeed).setVisibility(View.GONE);
        } else if (view.getId() == R.id.invite_players_button) {
            invitePlayers();
        } else if (view.getId() == R.id.invitations_button) {
            showInvitationInbox();
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
                findViewById(R.id.invitations_button).setVisibility(View.VISIBLE);
                findViewById(R.id.guessText).setVisibility(View.VISIBLE);
                findViewById(R.id.guessesFeed).setVisibility(View.VISIBLE);
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = result.getStatus().getStatusCode() + ": "
                            + getString(R.string.signin_other_error);
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        } else if (requestCode == RC_SELECT_PLAYERS) {
            if (resultCode != Activity.RESULT_OK) {
                // Canceled or some other error.
                return;
            }

            // Get the invitee list.
            final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            // Get Automatch criteria.
            int minAutoPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            // Create the room configuration.
            RoomConfig.Builder roomBuilder = RoomConfig.builder(mRoomUpdateCallback)
                    .setOnMessageReceivedListener(mMessageReceivedHandler)
                    .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                    .addPlayersToInvite(invitees);
            if (minAutoPlayers > 0) {
                roomBuilder.setAutoMatchCriteria(
                        RoomConfig.createAutoMatchCriteria(minAutoPlayers, maxAutoPlayers, 0));
            }

            // Save the roomConfig so we can use it if we call leave().
            mJoinedRoomConfig = roomBuilder.build();
            Games.getRealTimeMultiplayerClient(this, mGoogleSignInAccount)
                    .create(mJoinedRoomConfig);
        } else if (requestCode == RC_WAITING_ROOM) {

            // Look for finishing the waiting room from code, for example if a
            // "start game" message is received.  In this case, ignore the result.
            /*if (mWaitingRoomFinishedFromCode) {
                return;
            }*/

            if (resultCode == Activity.RESULT_OK) {
                // Start the game!
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Waiting room was dismissed with the back button. The meaning of this
                // action is up to the game. You may choose to leave the room and cancel the
                // match, or do something else like minimize the waiting room and
                // continue to connect in the background.

                // in this example, we take the simple approach and just leave the room:
                Games.getRealTimeMultiplayerClient(thisActivity, mGoogleSignInAccount)
                        .leave(mJoinedRoomConfig, mRoom.getRoomId());
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player wants to leave the room.
                Games.getRealTimeMultiplayerClient(thisActivity, mGoogleSignInAccount)
                        .leave(mJoinedRoomConfig, mRoom.getRoomId());
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        } else if (requestCode == RC_INVITATION_INBOX) {
            if (resultCode != Activity.RESULT_OK) {
                // Canceled or some error.
                return;
            }
            Invitation invitation = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);
            if (invitation != null) {
                RoomConfig.Builder builder = RoomConfig.builder(mRoomUpdateCallback)
                        .setOnMessageReceivedListener(mMessageReceivedHandler)
                        .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                        .setInvitationIdToAccept(invitation.getInvitationId());
                mJoinedRoomConfig = builder.build();
                Games.getRealTimeMultiplayerClient(thisActivity, mGoogleSignInAccount)
                        .join(mJoinedRoomConfig);
                Log.i(TAG, "joined from onactivityresult");
                // prevent screen from sleeping during handshake
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        signOut();
        Games.getRealTimeMultiplayerClient(thisActivity, mGoogleSignInAccount)
                .leave(mJoinedRoomConfig, mRoom.getRoomId());
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
                            findViewById(R.id.invitations_button).setVisibility(View.VISIBLE);
                            findViewById(R.id.guessText).setVisibility(View.VISIBLE);
                            findViewById(R.id.guessesFeed).setVisibility(View.VISIBLE);
                            Log.i(TAG, "Successfully signed in silently as: " + mGoogleSignInAccount.getDisplayName());
                            checkForInvitation();
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
        Games.getRealTimeMultiplayerClient(this, mGoogleSignInAccount)
                .getSelectOpponentsIntent(1, 3, true)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_SELECT_PLAYERS);
                    }
                });
    }

    // returns whether there are enough players to start the game
    boolean shouldStartGame(Room room) {
        int connectedPlayers = 0;
        for (Participant p : room.getParticipants()) {
            if (p.isConnectedToRoom()) {
                ++connectedPlayers;
            }
        }
        return connectedPlayers >= MIN_PLAYERS;
    }

    // Returns whether the room is in a state where the game should be canceled.
    boolean shouldCancelGame(Room room) {
        // TODO: Your game-specific cancellation logic here. For example, you might decide to
        // cancel the game if enough people have declined the invitation or left the room.
        // You can check a participant's status with Participant.getStatus().
        // (Also, your UI should have a Cancel button that cancels the game too)
        return false;
    }

    private void showWaitingRoom(Room room, int maxPlayersToStartGame) {
        Games.getRealTimeMultiplayerClient(this, mGoogleSignInAccount)
                .getWaitingRoomIntent(room, maxPlayersToStartGame)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_WAITING_ROOM);
                    }
                });
    }

    private void checkForInvitation() {
        Games.getGamesClient(this, mGoogleSignInAccount)
                .getActivationHint()
                .addOnSuccessListener(
                        new OnSuccessListener<Bundle>() {
                            @Override
                            public void onSuccess(Bundle bundle) {
                                Invitation invitation = bundle.getParcelable(Multiplayer.EXTRA_INVITATION);
                                if (invitation != null) {
                                    RoomConfig.Builder builder = RoomConfig.builder(mRoomUpdateCallback)
                                            .setOnMessageReceivedListener(mMessageReceivedHandler)
                                            .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                                            .setInvitationIdToAccept(invitation.getInvitationId());
                                    mJoinedRoomConfig = builder.build();
                                    Games.getRealTimeMultiplayerClient(thisActivity, mGoogleSignInAccount)
                                            .join(mJoinedRoomConfig);
                                    Log.i(TAG, "joined from checkforinvitations");
                                    // prevent screen from sleeping during handshake
                                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                }
                            }
                        }
                );

    }

    private void showInvitationInbox() {
        Games.getInvitationsClient(this, mGoogleSignInAccount)
                .getInvitationInboxIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_INVITATION_INBOX);
                    }
                });
    }

    void sendMessage(Message message) {
        // set contents of listview

    }

    // sends a byte array to all other players
    void sendToAllReliably(byte[] message) {
        for (String participantId : mRoom.getParticipantIds()) {
            if (!participantId.equals(mMyParticipantId)) {
                Task<Integer> task = Games.
                        getRealTimeMultiplayerClient(this, mGoogleSignInAccount)
                        .sendReliableMessage(message, mRoom.getRoomId(), participantId,
                                mReliableMessageSentHandler).addOnCompleteListener(new OnCompleteListener<Integer>() {
                            @Override
                            public void onComplete(@NonNull Task<Integer> task) {
                                // Keep track of which messages are sent, if desired.
                                // recordMessageToken(task.getResult());
                            }
                        });
            }
        }
    }
}
