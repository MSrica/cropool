package com.example.cropool.home.messages;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cropool.BuildConfig;
import com.example.cropool.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(BuildConfig.FIREBASE_RTDB_URL);
    private final List<Text> textList = new ArrayList<>();
    private final int LAST_SEEN_AT_UPDATE_INTERVAL = 3000;
    private Conversation conversation;
    private RecyclerView chatRecyclerView;
    // Will be used for updating last_seen_at field in user table
    // Not local because of cancel feature
    private Timer t;
    private String currentUserUID;

    // Time in seconds that separates "Online" status from "last seen at" status
    private int ONLINE_STATUS_THRESHOLD = 120;
    // Not local because it is used in listener for otherUser status
    private TextView onlineStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Fetch conversation in question from intent extras
        conversation = getIntent().hasExtra(getResources().getString(R.string.CHAT_ACTIVITY_INTENT_CONVERSATION_NAME))
                ? getIntent().getParcelableExtra(getResources().getString(R.string.CHAT_ACTIVITY_INTENT_CONVERSATION_NAME))
                : new Conversation(getResources().getString(R.string.name_surname), getResources().getString(R.string.message_content), getResources().getString(R.string.FB_RTDB_DEFAULT_PICTURE_VALUE), false, false, 0L, "-1", "-1", "-1");

        ImageView backButton = findViewById(R.id.back);
        ImageView sendButton = findViewById(R.id.send);
        TextView name = findViewById(R.id.chat_name);
        onlineStatus = findViewById(R.id.online_status);
        EditText messageText = findViewById(R.id.message_text);
        CircleImageView profilePicture = findViewById(R.id.chat_profile_picture);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);

        name.setText(conversation.getName());

        // Loading profile picture, default person icon if set to DEF_PIC_VAL
        if (!conversation.getProfilePicture().equals(getResources().getString(R.string.FB_RTDB_DEFAULT_PICTURE_VALUE))) {
            Picasso.get().load(conversation.getProfilePicture()).into(profilePicture);
        }

        backButton.setOnClickListener(v -> this.onBackPressed());

        chatRecyclerView.setHasFixedSize(true);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
        chatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    // Keyboard appeared - scroll to bottom again
                    chatRecyclerView.postDelayed(() -> chatRecyclerView.scrollToPosition(textList.size() - 1), 0);
                }
            }
        });

        // Checking conversationID and initializing if needed
        if (!conversation.isConversationIDValid()) {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Default createdConversationID is 0 (if no chats are created yet in the RTDB)
                    long createdConversationID = (snapshot.hasChild(getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME))) ? snapshot.child(getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).getChildrenCount() : 0L;
                    conversation.setConversationID(Long.toString(createdConversationID));

                    // conversationID is created and we can start the listener
                    startTextsListener();

                    // conversationID is created and we can update user_seen_at timestamp
                    updateCurrentUserLastSeenChatTable(System.currentTimeMillis());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            // conversationID is valid, start the listener for (new) texts
            startTextsListener();

            // conversationID is valid, start the listener for otherUserLastSeenAt
            startOtherUserStatusListener();

            // conversationID is valid, update user_seen_at timestamp
            updateCurrentUserLastSeenChatTable(System.currentTimeMillis());
        }

        sendButton.setOnClickListener(v -> sendText(messageText));
    }

    // (New) texts listener
    private void startTextsListener() {
        currentUserUID = conversation.getCurrentUserUID();
        setRecurrentUpdateLastSeen(LAST_SEEN_AT_UPDATE_INTERVAL, currentUserUID);

        databaseReference.child(getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).child(conversation.getConversationID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(getResources().getString(R.string.FB_RTDB_CHAT_TEXTS_KEY))) {
                    return;
                }

                textList.clear();

                for (DataSnapshot textSnapshot : snapshot.child(getResources().getString(R.string.FB_RTDB_CHAT_TEXTS_KEY)).getChildren()) {
                    if (textSnapshot.getKey() == null) {
                        // Text doesn't have a key so we skip it (shouldn't happen)
                        continue;
                    }

                    String textTS = textSnapshot.getKey();
                    String message = textSnapshot.child(getResources().getString(R.string.FB_RTDB_CHAT_MESSAGE_KEY)).getValue(String.class);
                    Long sentByUID = textSnapshot.child(getResources().getString(R.string.FB_RTDB_CHAT_SENT_BY_KEY)).getValue(Long.class);

                    boolean sentByOtherUser = sentByUID != null && !sentByUID.toString().equals(conversation.getCurrentUserUID());

                    textList.add(new Text(message, textTS, sentByOtherUser));
                }

                chatRecyclerView.setAdapter(new TextsAdapter(textList, getApplicationContext()));
                chatRecyclerView.scrollToPosition(textList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendText(EditText messageText) {
        if (!conversation.isConversationIDValid() || !conversation.isCurrentUserUIDValid()) {
            Toast.makeText(getApplicationContext(), "Text parameters invalid. Please sign in again.", Toast.LENGTH_LONG).show();
            return;
        }

        String message = messageText.getText().toString();

        // Initialize chat if needed, then send a message
        databaseReference.child(getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).child(conversation.getConversationID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear message EditText after message is sent
                // (that's how other chat apps act)
                messageText.setText("");

                Long currentTS = System.currentTimeMillis();

                // Initialization process (if needed)
                if (!snapshot.hasChild(getResources().getString(R.string.FB_RTDB_CHAT_USER_1_KEY)) || !snapshot.hasChild(getResources().getString(R.string.FB_RTDB_CHAT_USER_2_KEY))) {
                    // Conversation record in RTDB has none or one users - we initialize them (order is not important)
                    databaseReference.child(getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).child(conversation.getConversationID()).child(getResources().getString(R.string.FB_RTDB_CHAT_USER_1_KEY)).setValue(conversation.getCurrentUserUID());
                    databaseReference.child(getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).child(conversation.getConversationID()).child(getResources().getString(R.string.FB_RTDB_CHAT_USER_1_SEEN_AT_KEY)).setValue(currentTS);
                    databaseReference.child(getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).child(conversation.getConversationID()).child(getResources().getString(R.string.FB_RTDB_CHAT_USER_2_KEY)).setValue(conversation.getOtherUserUID());
                    databaseReference.child(getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).child(conversation.getConversationID()).child(getResources().getString(R.string.FB_RTDB_CHAT_USER_2_SEEN_AT_KEY)).setValue(0L);
                }


                // Conversation users and their last_seen fields are initialized if needed
                // Now we create a new child in text field of the current conversation, set its message and sent_by fields
                databaseReference.child(getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).child(conversation.getConversationID()).child(getResources().getString(R.string.FB_RTDB_CHAT_TEXTS_KEY)).child(currentTS.toString()).child(getResources().getString(R.string.FB_RTDB_CHAT_MESSAGE_KEY)).setValue(message);
                try {
                    databaseReference.child(getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).child(conversation.getConversationID()).child(getResources().getString(R.string.FB_RTDB_CHAT_TEXTS_KEY)).child(currentTS.toString()).child(getResources().getString(R.string.FB_RTDB_CHAT_SENT_BY_KEY)).setValue(Long.parseLong(conversation.getCurrentUserUID()));
                } catch (Exception ex) {
                    Log.e("ERR/sendText", "Setting sent_by field.");
                    ex.printStackTrace();
                }

                // Updating current_user_seen_at field
                // First we need to check whether current user is user_1 or user_2
                Long user1UID = snapshot.child(getResources().getString(R.string.FB_RTDB_CHAT_USER_1_KEY)).getValue(Long.class);
                Long user2UID = snapshot.child(getResources().getString(R.string.FB_RTDB_CHAT_USER_2_KEY)).getValue(Long.class);
                if (user1UID != null && user1UID.toString().equals(conversation.getCurrentUserUID())) {
                    // Current user's key corresponds to user_1's key
                    databaseReference.child(getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).child(conversation.getConversationID()).child(getResources().getString(R.string.FB_RTDB_CHAT_USER_1_SEEN_AT_KEY)).setValue(currentTS);
                } else if (user2UID != null && user2UID.toString().equals(conversation.getCurrentUserUID())) {
                    // Current user's key corresponds to user_2's key
                    databaseReference.child(getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).child(conversation.getConversationID()).child(getResources().getString(R.string.FB_RTDB_CHAT_USER_2_SEEN_AT_KEY)).setValue(currentTS);
                }

                // Updating current_user_seen at for conversationList purposes
                updateCurrentUserLastSeenChatTable(currentTS);

                // Update current user's last_seen_at user table field
                updateCurrentUserLastSeenUserTable(currentTS, conversation.getCurrentUserUID());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Listener for other user's chat status
    // Needs to be run manually (can't rely on onDataChange because if the user is offline, onDataChange won't be triggered)
    private void startOtherUserStatusListener() {
        if (conversation.getOtherUserUID() == null || conversation.getOtherUserUID().isEmpty()) {
            return;
        }

        databaseReference.child(getResources().getString(R.string.FB_RTDB_USER_TABLE_NAME)).child(conversation.getOtherUserUID()).child(getResources().getString(R.string.FB_RTDB_LAST_SEEN_AT_KEY)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // last_seen_at field in user table shouldn't have any children
                Long otherUserLastSeenAt = snapshot.getValue(Long.class);

                if (otherUserLastSeenAt != null) {
                    long differenceSeconds = (System.currentTimeMillis() - otherUserLastSeenAt) / 1000;

                    if (differenceSeconds < ONLINE_STATUS_THRESHOLD) {
                        // We can say that other user is now online
                        onlineStatus.setText(getResources().getString(R.string.STATUS_ONLINE));
                        onlineStatus.setTextColor(getResources().getColor(R.color.USER_ONLINE_COLOR));
                    } else {
                        // User is currently not online (2 or more minutes ago)
                        String textToDisplay = getResources().getString(R.string.STATUS_LAST_SEEN_PREFIX) + " " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(otherUserLastSeenAt);
                        onlineStatus.setText(textToDisplay);
                        onlineStatus.setTextColor(getResources().getColor(R.color.USER_LAST_SEEN_COLOR));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Updating user_last_seen timestamp in conversation conversationID
    private void updateCurrentUserLastSeenChatTable(long currentTimeMillis) {
        databaseReference.child(getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).child(conversation.getConversationID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long user1UID = snapshot.child(getResources().getString(R.string.FB_RTDB_CHAT_USER_1_KEY)).getValue(Long.class);
                Long user2UID = snapshot.child(getResources().getString(R.string.FB_RTDB_CHAT_USER_2_KEY)).getValue(Long.class);

                if (user1UID != null && user1UID.toString().equals(conversation.getCurrentUserUID())) {
                    databaseReference.child(getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).child(conversation.getConversationID()).child(getResources().getString(R.string.FB_RTDB_CHAT_USER_1_SEEN_AT_KEY)).setValue(currentTimeMillis);
                } else if (user2UID != null && user2UID.toString().equals(conversation.getCurrentUserUID())) {
                    databaseReference.child(getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).child(conversation.getConversationID()).child(getResources().getString(R.string.FB_RTDB_CHAT_USER_2_SEEN_AT_KEY)).setValue(currentTimeMillis);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Updating last_seen_at timestamp in user's user table record
    // Could be redundant in this activity since user will probably always go back
    // to conversationList activity which will also automatically update his last_seen_at timestamp
    // Could be useful when user terminates the app from his system's task view
    // after sending a text and before returning to conversationList activity, so we'll leave it here
    private void updateCurrentUserLastSeenUserTable(long currentTimeMillis, String userUID) {
        databaseReference.child(getResources().getString(R.string.FB_RTDB_USER_TABLE_NAME)).child(userUID).child(getResources().getString(R.string.FB_RTDB_LAST_SEEN_AT_KEY)).setValue(currentTimeMillis);
    }

    // Recurrent updating of the user's last_seen_at field in user table and other user's online status
    // (if the user is in the chat activity, but not sending any texts)
    private void setRecurrentUpdateLastSeen(int interval, String userID) {
        // In case there is already an updater running
        cancelRecurrentUpdateLastSeen();

        if (interval < 0 || userID.isEmpty())
            return;

        // Update last_seen_at field in user table at some interval
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Updating current user's last_seen_at field in the user table
                databaseReference.child(getResources().getString(R.string.FB_RTDB_USER_TABLE_NAME)).child(userID).child(getResources().getString(R.string.FB_RTDB_LAST_SEEN_AT_KEY)).setValue(System.currentTimeMillis());

                // Updating other user's online/offline status
                startOtherUserStatusListener();
            }
        }, 0, interval);
    }

    // Cancel recurrent updating of the user's last_seen_at field in user table
    // If the activity is running, but not shown
    private void cancelRecurrentUpdateLastSeen() {
        if (t != null)
            t.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Re-start the recurrent update
        setRecurrentUpdateLastSeen(LAST_SEEN_AT_UPDATE_INTERVAL, currentUserUID);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Cancel updating last_seen_at field in user table if it is scheduled
        cancelRecurrentUpdateLastSeen();
    }
}