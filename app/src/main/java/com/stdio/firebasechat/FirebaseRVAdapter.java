package com.stdio.firebasechat;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;

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
            }
            else {
                Glide.with(viewHolder.messageImageView.getContext())
                        .load((imageIsNotLoaded) ? R.drawable.progress_animation : friendlyMessage.getImageUrl())
                        .apply(requestOptions)
                        .into(viewHolder.messageImageViewLeft);
                viewHolder.flImageLayoutLeft.setVisibility(ImageView.VISIBLE);
            }
        }
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

    private void hideAllItemLayouts(MessageViewHolder viewHolder) {
        viewHolder.flImageLayoutLeft.setVisibility(ImageView.GONE);
        viewHolder.flImageLayout.setVisibility(ImageView.GONE);
        viewHolder.flMessage.setVisibility(TextView.GONE);
        viewHolder.flMessageLeft.setVisibility(TextView.GONE);
    }
}
