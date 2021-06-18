package com.stdio.firebasechat;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    public FirebaseRVAdapter(FirebaseRecyclerOptions<FriendlyMessage> options, FirebaseUser mFirebaseUser) {
        super(options);
        this.options = options;
        this.mFirebaseUser = mFirebaseUser;
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
        if (friendlyMessage.getText() != null) {
            if (friendlyMessage.getUid().equals(mFirebaseUser.getUid())) {
                viewHolder.flMessage.setVisibility(TextView.VISIBLE);
                viewHolder.tvMessage.setText(friendlyMessage.getText());
                if (friendlyMessage.getForwardedMessage() != null) {
                    showForwardedMessage(viewHolder, friendlyMessage);
                }
            }
            else {
                viewHolder.flMessageLeft.setVisibility(TextView.VISIBLE);
                viewHolder.tvMessageLeft.setText(friendlyMessage.getText());
            }
        } else if (friendlyMessage.getImageUrl() != null) {
            boolean imageIsNotLoaded = friendlyMessage.getImageUrl().equals(LOADING_IMAGE_URL);
            if (friendlyMessage.getUid().equals(mFirebaseUser.getUid())) {
                Glide.with(viewHolder.messageImageView.getContext())
                        .load((imageIsNotLoaded) ? R.drawable.progress_animation : friendlyMessage.getImageUrl())
                        .apply(requestOptions)
                        .into(viewHolder.messageImageView);
                viewHolder.flImageLayout.setVisibility(ImageView.VISIBLE);
                viewHolder.flImageLayout.setOnClickListener(view -> watchImage(friendlyMessage, viewHolder.messageImageView.getContext()));
            }
            else {
                Glide.with(viewHolder.messageImageView.getContext())
                        .load((imageIsNotLoaded) ? R.drawable.progress_animation : friendlyMessage.getImageUrl())
                        .apply(requestOptions)
                        .into(viewHolder.messageImageViewLeft);
                viewHolder.flImageLayoutLeft.setVisibility(ImageView.VISIBLE);
                viewHolder.flImageLayoutLeft.setOnClickListener(view -> watchImage(friendlyMessage, viewHolder.messageImageView.getContext()));
            }
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

    // override getItemId and getItemViewType to fix "RecyclerView items duplicate and constantly changing"
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void watchImage(FriendlyMessage friendlyMessage, Context context) {
        boolean imageIsNotLoaded = friendlyMessage.getImageUrl().equals(LOADING_IMAGE_URL);
        final Dialog dialog = new Dialog(context, R.style.edit_AlertDialog_style);
        dialog.setContentView(R.layout.image_dialog);


        final ImageViewTouch imageView = dialog.findViewById(R.id.start_img);
        LinearLayout place = dialog.findViewById(R.id.place);
        place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        Glide.with(context)
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
