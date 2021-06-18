package com.stdio.firebasechat;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stdio.firebasechat.models.FriendlyMessage;

import static com.stdio.firebasechat.Constants.MESSAGES_CHILD;

public class ImageUploader {

    private ProgressDialog dialog;
    private String mUsername;
    private String mPhotoUrl;

    private String TAG = "Uploader";
    private DatabaseReference ref;
    private Activity activity;
    private FirebaseUser mFirebaseUser;

    public ImageUploader(DatabaseReference ref, Activity activity, FirebaseUser mFirebaseUser) {
        this.ref = ref;
        this.activity = activity;
        this.mFirebaseUser = mFirebaseUser;
        mUsername = mFirebaseUser.getDisplayName();
        if (mFirebaseUser.getPhotoUrl() != null) {
            mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }
    }

    public void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
        final UploadTask uploadTask = storageReference.putFile(uri);
        showProgressDialog(key, uploadTask);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                final double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                Runnable changeMessage = new Runnable() {
                    @Override
                    public void run() {
                        dialog.setMessage(String.format("%.1f", progress) + "% done");
                    }
                };
                activity.runOnUiThread(changeMessage);
            }
        }).addOnCompleteListener(activity,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            task.getResult().getMetadata().getReference().getDownloadUrl()
                                    .addOnCompleteListener(activity,
                                            new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if (task.isSuccessful()) {
                                                        FriendlyMessage friendlyMessage =
                                                                new FriendlyMessage(null, mUsername, mPhotoUrl,
                                                                        task.getResult().toString());
                                                        friendlyMessage.setUid(mFirebaseUser.getUid());
                                                        ref.child(MESSAGES_CHILD).child(key)
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

    private void showProgressDialog(final String key, final UploadTask uploadTask) {
        dialog = new ProgressDialog(activity);
        dialog.setTitle("Загрузка изображения...");
        dialog.setCancelable(false);
        dialog.setButton(Dialog.BUTTON_POSITIVE, "Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ref.child(MESSAGES_CHILD).child(key).removeValue();
                uploadTask.cancel();
            }
        });
        dialog.show();
    }
}
