/*
* Copyright (C) 2016 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.itc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.itc.database.Message;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class TextMessageAdapter extends RecyclerView.Adapter<TextMessageAdapter.TextMessageViewHolder> {
    private static final String TAG = "TextMessageAdapter";

    private static final String DATE_FORMAT = "dd/MM/yyy";

    private List<Message> mMessages;
    private Context mContext;
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    public TextMessageAdapter(Context context) {
        mContext = context;
    }

    @Override
    public TextMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_message, parent, false);

        return new TextMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TextMessageViewHolder holder, int position) {
        Message textMessage = mMessages.get(position);
        if(textMessage.getSenderUid().equals(App.getThisUserUid())){
            holder.senderText.setText(textMessage.getMessage());
            holder.senderImage.setVisibility(View.GONE);
            holder.receiverText.setVisibility(View.GONE);
        }else {
            holder.receiverText.setText(textMessage.getMessage());
            holder.senderText.setVisibility(View.GONE);
            holder.senderImage.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return (mMessages ==null)||(mMessages.size()==0)?0: mMessages.size();
    }

    public List<Message> getMessages() {
        return mMessages;
    }

    public void setMessages(List<Message> messageList) {
        mMessages = messageList;
        notifyDataSetChanged();
    }


    class TextMessageViewHolder extends RecyclerView.ViewHolder{
        CircleImageView senderImage;
        TextView receiverText,senderText;

        public TextMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderImage = itemView.findViewById(R.id.message_profile_image);
            receiverText = itemView.findViewById(R.id.receiver_message_text);
            senderText = itemView.findViewById(R.id.sender_message_text);
        }
    }
}