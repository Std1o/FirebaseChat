package com.stdio.firebasechat;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    TextView tvMessage;
    TextView tvMessageLeft;
    FrameLayout flMessage, flMessageLeft, flImageLayout, flImageLayoutLeft;
    ImageView messageImageView, messageImageViewLeft;
    RelativeLayout replyLayout;
    TextView txtQuotedMsg, tvSender;
    ImageView ivQuotedMsg;
    RelativeLayout replyLayoutLeft;
    TextView txtQuotedMsgLeft, tvSenderLeft;
    ImageView ivQuotedMsgLeft;

    public MessageViewHolder(View v) {
        super(v);
        tvMessage = itemView.findViewById(R.id.textMessage);
        tvMessageLeft = itemView.findViewById(R.id.textMessageLeft);
        flMessage = itemView.findViewById(R.id.flMessage);
        flMessageLeft = itemView.findViewById(R.id.flMessageLeft);
        flImageLayout = itemView.findViewById(R.id.imageLayout);
        flImageLayoutLeft = itemView.findViewById(R.id.imageLayoutLeft);
        messageImageView = itemView.findViewById(R.id.imageView);
        messageImageViewLeft = itemView.findViewById(R.id.imageViewLeft);

        replyLayout = itemView.findViewById(R.id.replyLayout);
        txtQuotedMsg = itemView.findViewById(R.id.txtQuotedMsg);
        tvSender = itemView.findViewById(R.id.tvSender);
        ivQuotedMsg = itemView.findViewById(R.id.ivQuotedMsg);

        replyLayoutLeft = itemView.findViewById(R.id.replyLayoutLeft);
        txtQuotedMsgLeft = itemView.findViewById(R.id.txtQuotedMsgLeft);
        tvSenderLeft = itemView.findViewById(R.id.tvSenderLeft);
        ivQuotedMsgLeft = itemView.findViewById(R.id.ivQuotedMsgLeft);
    }
}