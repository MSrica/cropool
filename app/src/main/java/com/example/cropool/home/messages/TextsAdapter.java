package com.example.cropool.home.messages;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cropool.R;

import java.text.DateFormat;
import java.util.List;

public class TextsAdapter extends RecyclerView.Adapter<TextsAdapter.MyViewHolder> {
    private final List<Text> texts;
    private final Context context;

    public TextsAdapter(List<Text> texts, Context context) {
        this.texts = texts;
        this.context = context;
    }

    @NonNull
    @Override
    public TextsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_adapter_layout, null));
    }

    @Override
    public void onBindViewHolder(@NonNull TextsAdapter.MyViewHolder holder, int position) {
        Text text = texts.get(position);

        if (text.isSentByOtherUser()) {
            holder.currentUserLayout.setVisibility(View.GONE);
            holder.otherUserLayout.setVisibility(View.VISIBLE);

            holder.otherUserMessage.setText(text.getMessage());

            // holder.otherUserName.setVisibility(View.GONE);
            // holder.otherUserName.setText(text.getName());

            holder.otherUserDateTime.setVisibility(View.GONE);
            holder.otherUserDateTime.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Long.valueOf(text.getTimestamp())));

            holder.otherUserLayout.setOnClickListener(v -> {
                // holder.otherUserName.setVisibility((holder.otherUserName.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE);
                holder.otherUserDateTime.setVisibility((holder.otherUserDateTime.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE);
            });

            // TODO: DOESN'T WORK GOOD WITH HORIZONTAL MARGINS
            // RelativeLayout.LayoutParams otherUserLayoutParams = (RelativeLayout.LayoutParams) holder.otherUserLayout.getLayoutParams();
            // if (position > 0 && texts.get(position - 1).isSentByOtherUser()){
            //      otherUserLayoutParams.setMargins(20, 10, 100, 15); //otherUserLayoutParams.bottomMargin);
            // } else if (position > 0)  {
            //      otherUserLayoutParams.setMargins(20, 35, 100, 15); //otherUserLayoutParams.bottomMargin);
            // }
            // holder.otherUserLayout.setLayoutParams(otherUserLayoutParams);
        } else {
            holder.otherUserLayout.setVisibility(View.GONE);
            holder.currentUserLayout.setVisibility(View.VISIBLE);

            holder.currentUserMessage.setText(text.getMessage());

            // holder.currentUserName.setVisibility(View.GONE);
            // holder.currentUserName.setText(text.getName());

            holder.currentUserDateTime.setVisibility(View.GONE);
            holder.currentUserDateTime.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Long.valueOf(text.getTimestamp())));

            holder.currentUserLayout.setOnClickListener(v -> {
                // holder.currentUserName.setVisibility((holder.currentUserName.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE);
                holder.currentUserDateTime.setVisibility((holder.currentUserDateTime.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE);
            });


            // TODO: DOESN'T WORK GOOD WITH HORIZONTAL MARGINS
            // RelativeLayout.LayoutParams currentUserLayoutParams = (RelativeLayout.LayoutParams) holder.currentUserLayout.getLayoutParams();
            // if (position > 0 && !texts.get(position - 1).isSentByOtherUser()){
            //      currentUserLayoutParams.setMargins(100, 10, 20, 10); //otherUserLayoutParams.bottomMargin);
            // } else if (position > 0) {
            //      currentUserLayoutParams.setMargins(100, 35, 20, 10); //otherUserLayoutParams.bottomMargin);
            // }
            // holder.otherUserLayout.setLayoutParams(currentUserLayoutParams);
        }
    }

    @Override
    public int getItemCount() {
        return texts.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout currentUserLayout, otherUserLayout;
        private final TextView currentUserMessage, otherUserMessage, currentUserDateTime, otherUserDateTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // currentUserName = itemView.findViewById(R.id.current_user_name);
            // otherUserName = itemView.findViewById(R.id.other_user_name);
            currentUserLayout = itemView.findViewById(R.id.current_user_linear_layout);
            otherUserLayout = itemView.findViewById(R.id.other_user_linear_layout);
            currentUserMessage = itemView.findViewById(R.id.current_user_message);
            otherUserMessage = itemView.findViewById(R.id.other_user_message);
            currentUserDateTime = itemView.findViewById(R.id.current_user_date_time);
            otherUserDateTime = itemView.findViewById(R.id.other_user_date_time);
        }
    }
}
