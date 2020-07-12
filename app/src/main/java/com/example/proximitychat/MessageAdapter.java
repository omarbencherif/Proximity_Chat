package com.example.proximitychat;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessageAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;


    ArrayList<AMessage> mAMessages = new ArrayList<AMessage>();
    Context context;

    public MessageAdapter(Context contextm, ArrayList<AMessage> AMessages) {
        this.context = context;
        this.mAMessages = AMessages;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_bubble, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bmessage, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        AMessage AMessage = (AMessage) mAMessages.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(AMessage);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(AMessage);
        }

    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return mAMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        AMessage AMessage = (AMessage) mAMessages.get(position);

        if (AMessage.isMine()) {
            // If the current user is the sender of the AMessage
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the AMessage
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }

}

class ReceivedMessageHolder extends RecyclerView.ViewHolder {
    TextView messageText, timeText, nameText;
    ImageView profileImage;
    DateUtils mDate;

    ReceivedMessageHolder(View itemView) {
        super(itemView);
        messageText = (TextView) itemView.findViewById(R.id.message_body);
        //timeText = (TextView) itemView.findViewById(R.id.text_message_time);
        nameText = (TextView) itemView.findViewById(R.id.name);
        //profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
    }

    void bind(AMessage AMessage) {
        messageText.setText(AMessage.getText());

        // Format the stored timestamp into a readable String using method.
//        timeText.setText(Calendar.getInstance().getTime().toString());
        nameText.setText(AMessage.getUserData().getName());

        // Insert the profile image from the URL into the ImageView.
        //Utils.displayRoundImageFromUrl(mContext, AMessage.getSender().getProfileUrl(), profileImage);
    }
}

class SentMessageHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public TextView messageBody;


    TextView messageText, timeText, nameText;
    ImageView profileImage;
    DateUtils mDate;

    SentMessageHolder(View itemView) {
        super(itemView);
        messageText = (TextView) itemView.findViewById(R.id.message_body);
        //timeText = (TextView) itemView.findViewById(R.id.text_message_time);
        nameText = (TextView) itemView.findViewById(R.id.name);
        //profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
    }

    void bind(AMessage AMessage) {
        messageText.setText(AMessage.getText());

        // Format the stored timestamp into a readable String using method.
        timeText.setText(Calendar.getInstance().getTime().toString());
        nameText.setText(AMessage.getUserData().getName());

        // Insert the profile image from the URL into the ImageView.
        //Utils.displayRoundImageFromUrl(mContext, AMessage.getSender().getProfileUrl(), profileImage);
    }
}}



