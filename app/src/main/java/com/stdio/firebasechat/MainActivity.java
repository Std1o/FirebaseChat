package com.stdio.firebasechat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.capybaralabs.swipetoreply.ISwipeControllerActions;
import com.capybaralabs.swipetoreply.SwipeController;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stdio.firebasechat.models.FriendlyMessage;
import static com.stdio.firebasechat.Constants.LOADING_IMAGE_URL;
import static com.stdio.firebasechat.Constants.MESSAGES_CHILD;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_IMAGE = 2;
    public static final String ANONYMOUS = "anonymous";
    private String mUsername;
    private String mPhotoUrl;
    public static RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private EditText mMessageEditText;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> mFirebaseAdapter;
    RelativeLayout replyLayout;
    TextView txtQuotedMsg, tvSender;
    ImageView ivQuotedMsg;
    private boolean messageIsReply = false;
    private String forwardedMessage;
    private String forwardedImg;
    private String forwardedMessageMessageSender;
    private int forwardedMessageMessagePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        replyLayout = findViewById(R.id.reply_layout);
        txtQuotedMsg = findViewById(R.id.txtQuotedMsg);
        tvSender = findViewById(R.id.tvSender);
        ivQuotedMsg = findViewById(R.id.ivQuotedMsg);
        // Set default username is anonymous.
        mUsername = ANONYMOUS;

        auth();
        initRV();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        SnapshotParser<FriendlyMessage> parser = new SnapshotParser<FriendlyMessage>() {
            @Override
            public FriendlyMessage parseSnapshot(DataSnapshot dataSnapshot) {
                FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                if (friendlyMessage != null) {
                    friendlyMessage.setId(dataSnapshot.getKey());
                }
                return friendlyMessage;
            }
        };

        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD);
        FirebaseRecyclerOptions<FriendlyMessage> options = new FirebaseRecyclerOptions.Builder<FriendlyMessage>()
                .setQuery(messagesRef, parser)
                .build();
        mFirebaseAdapter = new FirebaseRVAdapter(options, mFirebaseUser);

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        mMessageEditText = findViewById(R.id.messageEditText);
    }

    private void auth() {
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }
    }

    private void initRV() {
        mMessageRecyclerView = findViewById(R.id.rvChat);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        SwipeController controller = new SwipeController(this, new ISwipeControllerActions() {
            @Override
            public void onSwipePerformed(int position) {
                reply(position);
            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(controller);
        itemTouchHelper.attachToRecyclerView(mMessageRecyclerView);
    }

    private void reply(int position) {
        replyLayout.setVisibility(View.VISIBLE);
        forwardedMessage = mFirebaseAdapter.getItem(position).getText();
        forwardedImg = mFirebaseAdapter.getItem(position).getImageUrl();
        forwardedMessageMessageSender = mFirebaseAdapter.getItem(position).getName();
        forwardedMessageMessagePosition = position;

        txtQuotedMsg.setText(forwardedMessage);
        tvSender.setText(forwardedMessageMessageSender);

        if (mFirebaseAdapter.getItem(position).getImageUrl() != null) {
            ivQuotedMsg.setVisibility(View.VISIBLE);
            Glide.with(MainActivity.this)
                    .load(mFirebaseAdapter.getItem(position).getImageUrl())
                    .into(ivQuotedMsg);
        } else {
            ivQuotedMsg.setVisibility(View.GONE);
        }
        messageIsReply = true;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.leftBtn:
            case R.id.ivClose:
                finish();
                break;
            case R.id.addMessageImageView:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
                break;
            case R.id.sendButton:
                String message = mMessageEditText.getText().toString();
                message = message.trim();
                message = message.replaceAll("^\\n+|\\n+$", "");
                if (!message.isEmpty()) {
                    FriendlyMessage friendlyMessage = new
                            FriendlyMessage(message, mUsername, mPhotoUrl, null);
                    friendlyMessage.setUid(mFirebaseUser.getUid());

                    if (messageIsReply) {
                        friendlyMessage.setForwardedMessage(forwardedMessage);
                        friendlyMessage.setForwardedImg(forwardedImg);
                        friendlyMessage.setForwardedMessageSender(forwardedMessageMessageSender);
                        friendlyMessage.setForwardedMessagePosition(forwardedMessageMessagePosition);

                        forwardedMessage = null;
                        forwardedImg = null;
                        forwardedMessageMessageSender = null;
                        forwardedMessageMessagePosition = -1;
                        messageIsReply = false;
                    }

                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(friendlyMessage);
                    mMessageEditText.setText("");
                    replyLayout.setVisibility(View.GONE);
                    ivQuotedMsg.setVisibility(View.GONE);
                }
                break;
        }
    }

    public void cancelReply(View view) {
        replyLayout.setVisibility(View.GONE);
        ivQuotedMsg.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE) {
            if (data != null) {
                final Uri uri = data.getData();
                Log.d(TAG, "Uri: " + uri.toString());

                FriendlyMessage tempMessage = new FriendlyMessage(null, mUsername, mPhotoUrl,
                        LOADING_IMAGE_URL);
                tempMessage.setUid(mFirebaseUser.getUid());
                mFirebaseDatabaseReference.child(MESSAGES_CHILD).push()
                        .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError,
                                                   DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    String key = databaseReference.getKey();
                                    StorageReference storageReference = FirebaseStorage.getInstance()
                                            .getReference(mFirebaseUser.getUid())
                                            .child(key)
                                            .child(uri.getLastPathSegment());
                                    ImageUploader imageUploader = new ImageUploader(mFirebaseDatabaseReference, MainActivity.this, mFirebaseUser);
                                    imageUploader.putImageInStorage(storageReference, uri, key);
                                } else {
                                    Log.w(TAG, "Unable to write message to database.",
                                            databaseError.toException());
                                }
                            }
                        });
            }
        }
    }
}
