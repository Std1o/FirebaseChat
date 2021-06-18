package com.stdio.firebasechat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.stdio.firebasechat.models.FriendlyMessage;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import static com.stdio.firebasechat.Constants.LOADING_IMAGE_URL;

public class FirebaseRVAdapter extends FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> {

    FirebaseRecyclerOptions<FriendlyMessage> options;
    FirebaseUser mFirebaseUser;
    int highlightPosition = -1;
    Activity activity;

    public FirebaseRVAdapter(FirebaseRecyclerOptions<FriendlyMessage> options, FirebaseUser mFirebaseUser, Activity activity) {
        super(options);
        this.options = options;
        this.mFirebaseUser = mFirebaseUser;
        this.activity = activity;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false));
    }

    @Override
    protected void onBindViewHolder(final MessageViewHolder viewHolder, int position, FriendlyMessage friendlyMessage) {
        final RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.progress_animation)
                .dontAnimate()
                .dontTransform();
        hideAllItemLayouts(viewHolder);
        if (highlightPosition == position) {
            viewHolder.selectedView.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    viewHolder.selectedView.setVisibility(View.GONE);
                    viewHolder.selectedView.animate().alpha(0.0f).setDuration(500);
                    highlightPosition = -1;
                }
            }, 1000);
        }
        setReplyLayoutClickListener(viewHolder.replyLayout, friendlyMessage);
        setReplyLayoutClickListener(viewHolder.replyLayoutLeft, friendlyMessage);
        showForwardedMessages(viewHolder, friendlyMessage);
        if (friendlyMessage.getText() != null) {
            if (friendlyMessage.getUid().equals(mFirebaseUser.getUid())) {
                viewHolder.flMessage.setVisibility(TextView.VISIBLE);
                viewHolder.tvMessage.setText(friendlyMessage.getText());
            }
            else {
                viewHolder.flMessageLeft.setVisibility(TextView.VISIBLE);
                viewHolder.tvMessageLeft.setText(friendlyMessage.getText());
            }
        } else if (friendlyMessage.getImageUrl() != null) {
            boolean imageIsNotLoaded = friendlyMessage.getImageUrl().equals(LOADING_IMAGE_URL);
            if (friendlyMessage.getUid().equals(mFirebaseUser.getUid())) {
                Glide.with(activity)
                        .load((imageIsNotLoaded) ? R.drawable.progress_animation : friendlyMessage.getImageUrl())
                        .apply(requestOptions)
                        .into(viewHolder.messageImageView);
                viewHolder.flImageLayout.setVisibility(ImageView.VISIBLE);
                viewHolder.flImageLayout.setOnClickListener(view -> watchImage(friendlyMessage));
            }
            else {
                Glide.with(activity)
                        .load((imageIsNotLoaded) ? R.drawable.progress_animation : friendlyMessage.getImageUrl())
                        .apply(requestOptions)
                        .into(viewHolder.messageImageViewLeft);
                viewHolder.flImageLayoutLeft.setVisibility(ImageView.VISIBLE);
                viewHolder.flImageLayoutLeft.setOnClickListener(view -> watchImage(friendlyMessage));
            }
        }
    }

    private void showForwardedMessages(MessageViewHolder viewHolder, FriendlyMessage friendlyMessage) {
        if (friendlyMessage.getForwardedMessage() != null) {
            showForwardedMessage(viewHolder, friendlyMessage);
        }
        if (friendlyMessage.getForwardedMessage() != null) {
            showForwardedMessageLeft(viewHolder, friendlyMessage);
        }
        if (friendlyMessage.getForwardedImg() != null) {
            showForwardedImg(viewHolder, friendlyMessage);
        }
        if (friendlyMessage.getForwardedImg() != null) {
            showForwardedImgLeft(viewHolder, friendlyMessage);
        }
    }

    private void showForwardedMessage(MessageViewHolder viewHolder, FriendlyMessage friendlyMessage) {
        viewHolder.replyLayout.setVisibility(View.VISIBLE);
        viewHolder.txtQuotedMsg.setText(friendlyMessage.getForwardedMessage());
        viewHolder.tvSender.setText(friendlyMessage.getForwardedMessageSender());
        viewHolder.ivQuotedMsg.setVisibility(View.GONE);
    }

    private void showForwardedMessageLeft(MessageViewHolder viewHolder, FriendlyMessage friendlyMessage) {
        viewHolder.replyLayoutLeft.setVisibility(View.VISIBLE);
        viewHolder.txtQuotedMsgLeft.setText(friendlyMessage.getForwardedMessage());
        viewHolder.tvSenderLeft.setText(friendlyMessage.getForwardedMessageSender());
        viewHolder.ivQuotedMsgLeft.setVisibility(View.GONE);
    }

    private void showForwardedImg(MessageViewHolder viewHolder, FriendlyMessage friendlyMessage) {
        viewHolder.replyLayout.setVisibility(View.VISIBLE);
        viewHolder.tvSender.setText(friendlyMessage.getForwardedMessageSender());
        viewHolder.ivQuotedMsg.setVisibility(View.VISIBLE);
        Glide.with(activity)
                .load(friendlyMessage.getForwardedImg())
                .into(viewHolder.ivQuotedMsg);
    }

    private void showForwardedImgLeft(MessageViewHolder viewHolder, FriendlyMessage friendlyMessage) {
        viewHolder.replyLayoutLeft.setVisibility(View.VISIBLE);
        viewHolder.tvSenderLeft.setText(friendlyMessage.getForwardedMessageSender());
        viewHolder.ivQuotedMsgLeft.setVisibility(View.VISIBLE);
        Glide.with(activity)
                .load(friendlyMessage.getForwardedImg())
                .into(viewHolder.ivQuotedMsgLeft);
    }

    private void setReplyLayoutClickListener(RelativeLayout relativeLayout, FriendlyMessage friendlyMessage) {
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mMessageRecyclerView.smoothScrollToPosition(friendlyMessage.getForwardedMessagePosition());
                highlightPosition = friendlyMessage.getForwardedMessagePosition();
            }
        });
    }

    // override getItemId and getItemViewType to fix "RecyclerView items duplicate and constantly changing"
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void watchImage(FriendlyMessage friendlyMessage) {
        boolean imageIsNotLoaded = friendlyMessage.getImageUrl().equals(LOADING_IMAGE_URL);
        final Dialog dialog = new Dialog(activity, R.style.edit_AlertDialog_style);
        dialog.setContentView(R.layout.image_dialog);


        final ImageViewTouch imageView = dialog.findViewById(R.id.start_img);
        LinearLayout place = dialog.findViewById(R.id.place);
        place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        Glide.with(activity)
                .load((imageIsNotLoaded) ? R.drawable.progress_animation : friendlyMessage.getImageUrl())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.progress_animation))
                .into(imageView);
        dialog.show();

        dialog.setCanceledOnTouchOutside(true); // Sets whether this dialog is
        Window w = dialog.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.x = 0;
        lp.y = 40;
        dialog.onWindowAttributesChanged(lp);
    }

    private void hideAllItemLayouts(MessageViewHolder viewHolder) {
        viewHolder.flImageLayoutLeft.setVisibility(ImageView.GONE);
        viewHolder.flImageLayout.setVisibility(ImageView.GONE);
        viewHolder.flMessage.setVisibility(TextView.GONE);
        viewHolder.flMessageLeft.setVisibility(TextView.GONE);
    }
}
