package com.stdio.firebasechat;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.stdio.firebasechat.Constants.LOADING_IMAGE_URL;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_IMAGE = 2;
    public static final String ANONYMOUS = "anonymous";
    private String mUsername;
    private String mPhotoUrl;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private EditText mMessageEditText;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> mFirebaseAdapter;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                if (!mMessageEditText.getText().toString().isEmpty()) {
                    FriendlyMessage friendlyMessage = new
                            FriendlyMessage(mMessageEditText.getText().toString(),
                            mUsername,
                            mPhotoUrl,
                            null /* no image */);
                    friendlyMessage.setUid(mFirebaseUser.getUid());
                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(friendlyMessage);
                    mMessageEditText.setText("");
                }
                break;
        }
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
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
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

                                    putImageInStorage(storageReference, uri, key);
                                } else {
                                    Log.w(TAG, "Unable to write message to database.",
                                            databaseError.toException());
                                }
                            }
                        });
            }
        }
    }

    private void showProgressDialog(final String key, final UploadTask uploadTask) {
        dialog = new ProgressDialog(this);
        dialog.setTitle("Загрузка изображения...");
        dialog.setCancelable(false);
        dialog.setButton(Dialog.BUTTON_POSITIVE, "Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(key).removeValue();
                uploadTask.cancel();
            }
        });
        dialog.show();
    }

    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
        final UploadTask uploadTask = storageReference.putFile(uri);
        showProgressDialog(key, uploadTask);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                final double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                Log.i("Load","Upload is " + String.format("%.1f", progress) + "% done");
                Runnable changeMessage = new Runnable() {
                    @Override
                    public void run() {
                        //Log.v(TAG, strCharacters);
                        dialog.setMessage(String.format("%.1f", progress) + "% done");
                    }
                };
                runOnUiThread(changeMessage);
            }
        }).addOnCompleteListener(this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            task.getResult().getMetadata().getReference().getDownloadUrl()
                                    .addOnCompleteListener(MainActivity.this,
                                            new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if (task.isSuccessful()) {
                                                        FriendlyMessage friendlyMessage =
                                                                new FriendlyMessage(null, mUsername, mPhotoUrl,
                                                                        task.getResult().toString());
                                                        friendlyMessage.setUid(mFirebaseUser.getUid());
                                                        mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(key)
                                                                .setValue(friendlyMessage);
                                                        dialog.cancel();
                                                    }
                                                }
                                            });
                        } else {
                            Log.w(TAG, "Image upload task was not successful.",
                                    task.getException());
                        }
                    }
                });
    }
}
