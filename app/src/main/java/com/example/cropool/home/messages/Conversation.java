package com.example.cropool.home.messages;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Conversation implements Comparable<Conversation>, Parcelable {

    private final String name;
    private final String lastMessage;
    private final String profilePicture;
    private final boolean lastMessageByOtherUser, unseen;
    private final Long time;
    private String conversationID;
    private final String currentUserUID, otherUserUID;

    public Conversation(String name, String lastMessage, String profilePicture, boolean lastMessageByOtherUser, boolean unseen, Long time, String conversationID, String currentUserUID, String otherUserUID) {
        this.name = name;
        this.lastMessage = lastMessage;
        this.profilePicture = profilePicture;
        this.lastMessageByOtherUser = lastMessageByOtherUser;
        this.unseen = unseen;
        this.time = time;
        this.conversationID = conversationID;
        this.currentUserUID = currentUserUID;
        this.otherUserUID = otherUserUID;
    }


    // Required for implementing Parcelable (auto-generated)
    protected Conversation(Parcel in) {
        name = in.readString();
        lastMessage = in.readString();
        profilePicture = in.readString();
        lastMessageByOtherUser = in.readByte() != 0;
        unseen = in.readByte() != 0;
        if (in.readByte() == 0) {
            time = null;
        } else {
            time = in.readLong();
        }
        conversationID = in.readString();
        currentUserUID = in.readString();
        otherUserUID = in.readString();
    }

    // Required for implementing Parcelable (auto-generated)
    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel in) {
            return new Conversation(in);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getConversationID() {
        return conversationID;
    }

    public String getCurrentUserUID() {
        return currentUserUID;
    }

    public String getOtherUserUID() {
        return otherUserUID;
    }

    public void setConversationID(String conversationID) {
        if (this.isConversationIDValid()) {
            // Conversation ID was already set, no intention to change it
            return;
        }

        // Conversation ID was never set, change it
        this.conversationID = conversationID;
    }

    public boolean isConversationIDValid() {
        try {
            // Conversation ID was already set, therefore valid, no shouldn't be changed
            // Conversation ID was never set, should be changed (set)
            return Long.parseLong(this.conversationID) >= 0L;
        } catch (Exception e) {
            Log.e("ERR/isConvIDValid", conversationID);
            e.printStackTrace();
            return false;
        }
    }

    public boolean isOtherUserUIDValid() {
        try {
            // otherUserUID was already set, therefore valid, no shouldn't be changed
            // otherUserUID was never set, conversation should be INVALID
            return Long.parseLong(this.otherUserUID) >= 0L;
        } catch (Exception e) {
            Log.e("ERR/isOtherUIDValid", otherUserUID);
            e.printStackTrace();
            return false;
        }
    }

    public boolean isCurrentUserUIDValid() {
        try {
            // currentUserUID was already set, therefore valid, no shouldn't be changed
            // currentUserUID was never set, conversation should be INVALID
            return Long.parseLong(this.currentUserUID) >= 0L;
        } catch (Exception e) {
            Log.e("ERR/isCurrUIDValid", currentUserUID);
            e.printStackTrace();
            return false;
        }
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public boolean isLastMessageByOtherUser() {
        return lastMessageByOtherUser;
    }

    public boolean isUnseen() {
        return unseen;
    }

    public Long getTime() {
        return time;
    }

    // Comparator used to sort the conversations in
    // conversationList by last message timestamp
    // (newest first)
    @Override
    public int compareTo(Conversation o) {
        return Long.compare(o.getTime(), time);
    }

    // Required for implementing Parcelable (auto-generated)
    @Override
    public int describeContents() {
        return 0;
    }

    // Required for implementing Parcelable (auto-generated)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(lastMessage);
        dest.writeString(profilePicture);
        dest.writeByte((byte) (lastMessageByOtherUser ? 1 : 0));
        dest.writeByte((byte) (unseen ? 1 : 0));
        if (time == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(time);
        }
        dest.writeString(conversationID);
        dest.writeString(currentUserUID);
        dest.writeString(otherUserUID);
    }
}
