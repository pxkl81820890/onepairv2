package com.iff.onepairv2;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;


import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public MessageAdapter(List<Messages> mMessageList){
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            String current_uid = mAuth.getCurrentUser().getUid();
            Messages c = mMessageList.get(position);
            String from_user = c.getFrom();
            holder.messageText.setText(c.getMessage());

            if(from_user.equals(current_uid)){
                holder.messageText.setBackgroundResource(R.drawable.message_text_bg2);
            }
            else{
                holder.messageText.setBackgroundResource(R.drawable.message_text_bg);
            }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


}
