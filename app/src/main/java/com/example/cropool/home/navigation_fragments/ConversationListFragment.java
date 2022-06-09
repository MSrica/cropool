package com.example.cropool.home.navigation_fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cropool.BuildConfig;
import com.example.cropool.R;
import com.example.cropool.api.Tokens;
import com.example.cropool.home.messages.Conversation;
import com.example.cropool.home.messages.ConversationsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationListFragment extends Fragment {
    private final HashMap<String, Conversation> conversationMap = new HashMap<>();
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(BuildConfig.FIREBASE_RTDB_URL);
    private List<String> conversationIDs = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private RecyclerView messageListRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_conversation_list, container, false);

        // Set FB auth/user
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Check if user is signed in (non-null) and update UI accordingly.
        updateUI(v);

        messageListRecyclerView = v.findViewById(R.id.message_list_recycler_view);
        messageListRecyclerView.setHasFixedSize(true);
        messageListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        return v;
    }

    private void updateUI(View v) {
        if (currentUser == null) {
            // User not signed in, sign him in, then update UI again
            customSignIn(v);

            return;
        }

        // Set listener for new/updated conversations
        setConversationIDListener(v);
    }

    // Signs the user in if he has the Firebase token that is set when logging in or registering
    private void customSignIn(View v) {
        if (!Tokens.isFirebaseTokenSet(requireContext())) {
            Log.e("firebaseLogin", "signInWithCustomToken: failed");
            Toast.makeText(getContext(), "Text authentication failed. Please sign in again.", Toast.LENGTH_LONG).show();
            Tokens.loginRequiredProcedure(requireContext(), requireActivity());
        }

        mAuth.signInWithCustomToken(Tokens.getFirebaseToken(requireContext()))
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Sign in successful
                        Log.i("firebaseLogin", "signInWithCustomToken: success");

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        // Set this class' current user to firebaseUser
                        currentUser = firebaseUser;

                        // Check the user's record in RTDB
                        checkUserRTDBRecord(v, firebaseUser);

                        updateUI(v);
                    } else {
                        // Sign in failed
                        Log.e("firebaseLogin", "signInWithCustomToken: failed");

                        Toast.makeText(getContext(), "Text authentication failed.", Toast.LENGTH_LONG).show();
                    }
                });

    }

    // Checks whether the signed in user has a record in the FB RTDB
    // If not, inserts him into the RTDB along with his email, display name and profile picture URL
    private void checkUserRTDBRecord(View v, FirebaseUser firebaseUser) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Toast.makeText(requireContext(), "Deciding whether to create user " + firebaseUser.getUid(), Toast.LENGTH_LONG).show();
                if (!snapshot.child("user").hasChild(firebaseUser.getUid())) {
                    // User hasn't been inserted to FB RTDB yet
                    // Toast.makeText(requireContext(), "Creating user " + firebaseUser.getUid(), Toast.LENGTH_LONG).show();
                    databaseReference.child(v.getResources().getString(R.string.FB_RTDB_USER_TABLE_NAME)).child(firebaseUser.getUid()).child(v.getResources().getString(R.string.FB_RTDB_E_MAIL_KEY)).setValue(firebaseUser.getEmail());
                    databaseReference.child(v.getResources().getString(R.string.FB_RTDB_USER_TABLE_NAME)).child(firebaseUser.getUid()).child(v.getResources().getString(R.string.FB_RTDB_DISPLAY_NAME_KEY)).setValue(firebaseUser.getDisplayName());
                    databaseReference.child(v.getResources().getString(R.string.FB_RTDB_USER_TABLE_NAME)).child(firebaseUser.getUid()).child(v.getResources().getString(R.string.FB_RTDB_PROFILE_PICTURE_KEY)).setValue(firebaseUser.getPhotoUrl() == null ? v.getResources().getString(R.string.FB_RTDB_DEFAULT_PICTURE_VALUE) : firebaseUser.getPhotoUrl().toString());
                    // databaseReference.child(v.getResources().getString(R.string.FB_RTDB_USER_TABLE_NAME)).child(firebaseUser.getUid()).child(v.getResources().getString(R.string.FB_RTDB_CONVERSATIONS_KEY)).setValue("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Sets a RTDB listener that triggers on each conversations/texts change for currentUser in RTDB
    private void setConversationIDListener(View v) {
        if (currentUser == null) {
            Log.e("FIREBASE USER NULL", "setConvIDLstnr currentUser null");
            return;
        }

        Log.e("FIREBASE USER NOT NULL", "setConvIDLstnr currentUser not null");

        // Listener for currentUser (listening for changes in his conversationsList)
        databaseReference.child(v.getResources().getString(R.string.FB_RTDB_USER_TABLE_NAME)).child(currentUser.getUid()).child(v.getResources().getString(R.string.FB_RTDB_CONVERSATIONS_KEY)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clearing current conversation list (something has changed)
                conversationMap.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // dataSnapshot recorded for current user (his conversation field)
                    // Log.e("FB UID LSTNR", uid);

//                    final String email = dataSnapshot.child(v.getResources().getString(R.string.FB_RTDB_E_MAIL_KEY)).getValue(String.class);
//                    final String name = dataSnapshot.child(v.getResources().getString(R.string.FB_RTDB_DISPLAY_NAME_KEY)).getValue(String.class);
//                    final String profilePicture = dataSnapshot.child(v.getResources().getString(R.string.FB_RTDB_PROFILE_PICTURE_KEY)).getValue(String.class);
//                    String lastMessage = new String("");
//                    int unseenMessages = 0;

                    // getMobile = uid
                    // mobile = currentuser.uid

                    // Observing RTDB chat table
//                        databaseReference.child("chat").addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                int chatCount = (int) snapshot.getChildrenCount();
//
//                                if (chatCount > 0) {
//                                    for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
//                                        final String key = dataSnapshot1.getKey();
//                                        final String user1 = dataSnapshot1.child("user_1").getValue(String.class);
//                                        final String user2 = dataSnapshot1.child("user_2").getValue(String.class);
//
//                                        if (user1 != null && user2 != null
//                                                && ((user1.equals(uid) && user2.equals(currentUser.getUid())) || (user1.equals(currentUser.getUid()) && user2.equals(uid)))) {
//                                            for (DataSnapshot chatDataSnapshot : dataSnapshot1.child("messages").getChildren()){
//                                                final long getLastSeenMessage =
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });
                    String conversationID = dataSnapshot.getKey();

                    if (conversationID == null || !("ACTIVE").equals(dataSnapshot.getValue(String.class)))
                        continue;

                    // Log the conversationID to conversationIDs list in order to set the adapter only once
                    conversationIDs.add(conversationID);

                    // Sets a listener for each conversation of currentUser
                    // (forwards conversationID and name and profile picture of the otherUser)
                    setConversationListener(v, conversationID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Listener for conversation "conversationID" (listening for new messages etc.)
    private void setConversationListener(View v, String conversationID) {
        databaseReference.child(v.getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).child(conversationID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Conversation conversationID snapshot (contains texts, user1, user2, user1SeenAt, user2SeenAt)

                Long user1ID = snapshot.child(v.getResources().getString(R.string.FB_RTDB_CHAT_USER_1_KEY)).getValue(Long.class);
                Long user2ID = snapshot.child(v.getResources().getString(R.string.FB_RTDB_CHAT_USER_2_KEY)).getValue(Long.class);
                if (user1ID == null || user2ID == null) {
                    Log.e("FirebaseChatUserIDNull", "user1ID: " + user1ID + ", user2ID: " + user2ID);
                    return;
                }

                Long user1SeenAt = snapshot.child(v.getResources().getString(R.string.FB_RTDB_CHAT_USER_1_SEEN_AT_KEY)).getValue(Long.class);
                Long user2SeenAt = snapshot.child(v.getResources().getString(R.string.FB_RTDB_CHAT_USER_2_SEEN_AT_KEY)).getValue(Long.class);

                // User to which the currentUser is talking to
                String otherUserUID = (!currentUser.getUid().equals(user1ID.toString())) ? user1ID.toString() : user2ID.toString();

                // Timestamp when the currentUser last opened the conversation
                Long currentUserSeenAt = (currentUser.getUid().equals(user1ID.toString())) ? user1SeenAt : user2SeenAt;

                setOtherUserInfoListener(v, conversationID, otherUserUID, currentUserSeenAt);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Listener for otherUser account info (we need his name and profile picture to show the conversation)
    private void setOtherUserInfoListener(View v, String conversationID, String otherUserUID, Long currentUserSeenAt) {
        databaseReference.child(v.getResources().getString(R.string.FB_RTDB_USER_TABLE_NAME)).child(otherUserUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // final String otherUserEmail = snapshot.child(v.getResources().getString(R.string.FB_RTDB_E_MAIL_KEY)).getValue(String.class);
                final String otherUserName = snapshot.child(v.getResources().getString(R.string.FB_RTDB_DISPLAY_NAME_KEY)).getValue(String.class);
                final String otherUserProfilePicture = snapshot.child(v.getResources().getString(R.string.FB_RTDB_PROFILE_PICTURE_KEY)).getValue(String.class);

                // Query in order to download only the last text
                Query lastTextQuery = databaseReference.child(v.getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).child(conversationID).child(v.getResources().getString(R.string.FB_RTDB_CHAT_TEXTS_KEY)).orderByKey().limitToLast(1);

                // Listener for last text in conversation conversationID
                lastTextQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getKey() == null) {
                            // Last text not fetched
                            return;
                        }

                        Long lastTextTS = 0L;
                        String lastMessage = "";
                        Long lastTextSentByUID = 0L;

                        // We move the snapshot to the last text (contains "message" and "sent_by" keys)
                        // There should be just one child in the snapshot (limitToLast(1)) and its key should be a timestamp
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            lastTextTS = (dataSnapshot.getKey() != null) ? Long.parseLong(dataSnapshot.getKey()) : 0;
                            lastMessage = dataSnapshot.child(v.getResources().getString(R.string.FB_RTDB_CHAT_MESSAGE_KEY)).getValue(String.class);
                            lastTextSentByUID = dataSnapshot.child(v.getResources().getString(R.string.FB_RTDB_CHAT_SENT_BY_KEY)).getValue(Long.class);
                        }

                        boolean someTextsUnseen = (lastTextSentByUID == null) || (!lastTextSentByUID.toString().equals(currentUser.getUid()) && currentUserSeenAt < lastTextTS);
                        boolean sentByOtherUser = (lastTextSentByUID == null) || (!currentUser.getUid().equals(lastTextSentByUID.toString()));

                        Conversation conversation = new Conversation(otherUserName, lastMessage, otherUserProfilePicture, sentByOtherUser, someTextsUnseen, lastTextTS, conversationID, currentUser.getUid(), otherUserUID);
                        conversationMap.put(conversationID, conversation);
                        conversationIDs.remove(conversationID);

                        // If conversationIDs list is empty, we got through all chats and can show the list
                        if (conversationIDs.isEmpty()) {
                            // We've updated conversationList so we set the new adapter
                            // (need to get conversations only from the list before that)
                            List<Conversation> conversationList = new ArrayList<>();
                            for (Map.Entry<String, Conversation> e : conversationMap.entrySet()) {
                                conversationList.add(e.getValue());
                            }

                            // Sorting by conversation's last message timestamp
                            Collections.sort(conversationList);

                            messageListRecyclerView.setAdapter(new ConversationsAdapter(conversationList, v.getContext(), requireActivity()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}