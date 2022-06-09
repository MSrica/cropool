package com.example.cropool.home.messages;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cropool.R;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.MyViewHolder> {
    private final List<Conversation> conversationList;
    private final Context context;
    private final FragmentActivity fragmentActivity;

    public ConversationsAdapter(List<Conversation> conversationList, Context context, FragmentActivity fragmentActivity) {
        this.conversationList = conversationList;
        this.context = context;
        this.fragmentActivity = fragmentActivity;
    }

    @NonNull
    @Override
    public ConversationsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_item_adapter_layout, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationsAdapter.MyViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);

        holder.name.setText(conversation.getName());
        holder.text.setText((conversation.isLastMessageByOtherUser()) ? conversation.getLastMessage() : "You: " + conversation.getLastMessage());

        holder.time.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(conversation.getTime()));

        if (!conversation.getProfilePicture().equals(context.getResources().getString(R.string.FB_RTDB_DEFAULT_PICTURE_VALUE))) {
            // Conversation has a custom profile picture
            Picasso.get().load(conversation.getProfilePicture()).into(holder.profilePicture);
        }

        if (!conversation.isUnseen()) {
            // Conversation is read
            holder.text.setTypeface(null, Typeface.NORMAL);
            holder.time.setTypeface(null, Typeface.NORMAL);
            holder.unseenMessage.setVisibility(View.GONE);
        } else {
            // Conversation has unread messages
            holder.text.setTypeface(null, Typeface.BOLD);
            holder.time.setTypeface(null, Typeface.BOLD);
            holder.unseenMessage.setVisibility(View.VISIBLE);
        }

        holder.rootLayout.setOnClickListener(v -> {
            Intent goToChat = new Intent(fragmentActivity, ChatActivity.class);
            goToChat.putExtra(context.getResources().getString(R.string.CHAT_ACTIVITY_INTENT_CONVERSATION_NAME), conversation);
            fragmentActivity.startActivity(goToChat);
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView profilePicture;
        private final TextView name, text, unseenMessage, time;
        private final LinearLayout rootLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePicture = itemView.findViewById(R.id.profile_picture);
            name = itemView.findViewById(R.id.name);
            text = itemView.findViewById(R.id.text);
            time = itemView.findViewById(R.id.time);
            unseenMessage = itemView.findViewById(R.id.unseen_message);
            rootLayout = itemView.findViewById(R.id.message_list_root_layout);
        }
    }
}
