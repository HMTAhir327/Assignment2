package com;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hm_tahir.assignment2.R;

public class UploadVideo extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST = 3;
    VideoView vv;
    private Uri videoUri;
    private MediaController mc;
    private StorageReference mStorageRef;
    private String videoName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        vv=findViewById(R.id.videoabc);
        mStorageRef = FirebaseStorage.getInstance().getReference("Uploads").child("Videos");
        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                        mc=new MediaController(UploadVideo.this);
                        vv.setMediaController(mc);
                        mc.setAnchorView(vv);
                    }
                });
            }
        });


        vv.start();
    }

    public void Uploadvideo(View view) {
        Intent i =new Intent();
        i.setType("video/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i,"Select a video"),PICK_VIDEO_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_VIDEO_REQUEST && requestCode==RESULT_OK && data != null){
            videoUri = data.getData();
            vv.setVideoURI(videoUri);
            videoName=getFileName(videoUri);
        }
    }

    public void videouploadtoserver(View view) {

        mStorageRef.child(videoName).putFile(videoUri);
        mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Uri downloadUrl = uri;
                Toast.makeText(getBaseContext(), "Upload success! URL - " + downloadUrl.toString() , Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(UploadVideo.this,exception.toString(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public String getFileName(Uri objectUri)
    {
        String result=null;
        if(objectUri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(objectUri, null, null, null);
            try {
                if(cursor != null && cursor.moveToFirst()){
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }finally {
                cursor.close();
            }
        }
        if(result==null){
              result =objectUri.getPath();
              int cut = result.lastIndexOf('/');
              if(cut != -1){
                  result= result.substring(cut+1);
              }
        }

        return result;

    }
}
