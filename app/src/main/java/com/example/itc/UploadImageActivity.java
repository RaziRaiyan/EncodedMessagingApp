package com.example.itc;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class UploadImageActivity extends AppCompatActivity {
    private static final String TAG = "UploadImageActivity";

    private static final int REQ_GALLERY_INTENT = 0;
    private static final int REQ_CAMERA_INTENT = 1;

    private ImageView mImageView;
    private Button btn_skip,btn_upload;
    private ImageButton btn_gallery, btn_capture, btn_play_pause, btn_cancel_upload;
    private TextView tv_photo_url,tv_upload_desc,tv_bytes_transferred,tv_upload_percent;
    private ProgressBar mProgressBar;
    private LinearLayout uploadLayout;

    private StorageReference mStorageReference;
    private FirebaseAuth mAuth;

    private static Uri imageUri;
    private static String displayName;
    private static StorageTask mStorageTask;

    private static final int STORAGE_READ_PERMISSION_CODE = 1001;
    private static final int CAMERA_PERMISSION_CODE = 1002;
    private static final int STORAGE_WRITE_PERMISSION_CODE =1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        mImageView = findViewById(R.id.imageView_upload);
        btn_upload = findViewById(R.id.buttonUpload);
        btn_capture = findViewById(R.id.btn_capture);
        btn_gallery = findViewById(R.id.btn_gallery);
        btn_skip = findViewById(R.id.buttonSkip);
        tv_photo_url = findViewById(R.id.tv_upload_uri);
        mProgressBar = findViewById(R.id.upload_progress);
        tv_upload_desc = findViewById(R.id.tv_upload_desc);
        tv_bytes_transferred = findViewById(R.id.tv_upload_bytes);
        tv_upload_percent = findViewById(R.id.tv_upload_percent);
        uploadLayout = findViewById(R.id.upload_layout);
        btn_play_pause = findViewById(R.id.btn_play_pause);
        btn_cancel_upload = findViewById(R.id.btn_cancel_upload);
        uploadLayout.setVisibility(View.INVISIBLE);


        btn_upload.setVisibility(View.INVISIBLE);

        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(UploadImageActivity.this,
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(UploadImageActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        openCamera();
                    }else {
                        requestStorageWriePerimission();
                    }
                }else{
                    requestCameraPermission();
                }
            }
        });

        btn_gallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(UploadImageActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    openGallery();
                }else{
                    requestStorageReadPermission();
                }
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_upload.getText().toString().trim().toLowerCase().equals("finish")){
                    openLoginActivity();
                }else {
                    uploadImageToFirebase();
                    uploadLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        btn_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        btn_play_pause.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(null!=mStorageTask){
                    if(mStorageTask.isInProgress()){
                        mStorageTask.pause();
                        btn_play_pause.setImageResource(R.drawable.ic_play_circle_outline_play_24dp);
                    }else if(mStorageTask.isPaused()){
                        mStorageTask.resume();
                        btn_play_pause.setImageResource(R.drawable.ic_pause_circle_outline_white_24dp);
                    }
                }
            }
        });

        btn_cancel_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != mStorageTask){
                    uploadLayout.setVisibility(View.INVISIBLE);
                    btn_upload.setVisibility(View.VISIBLE);
                    btn_skip.setVisibility(View.VISIBLE);
                    mStorageTask.cancel();
                }
            }
        });

        mStorageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

    }

    private void requestCameraPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)){
            showRequestDialog("Camera capture permission needed",
                    "Give permission so that app can read data and upload",
                    Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE);
        }else {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
        }
    }

    private void requestStorageReadPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
            showRequestDialog("Storage read permission needed",
                    "Give permission so that app can read data and upload",
                    Manifest.permission.READ_EXTERNAL_STORAGE,STORAGE_READ_PERMISSION_CODE);
        }else {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_READ_PERMISSION_CODE);
        }
    }

    private void requestStorageWriePerimission(){
        int currentVersion = Build.VERSION.SDK_INT;
        if(currentVersion >= android.os.Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    showRequestDialog("Sorage write permission needed",
                            "Give permission to save the captured image",
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,STORAGE_WRITE_PERMISSION_CODE);
                }else {
                    ActivityCompat.requestPermissions(this,
                                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_WRITE_PERMISSION_CODE);
                }
            }
        }
    }

    private void showRequestDialog(String title, String message, final String permission, final int permissionCode) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(UploadImageActivity.this,new String[] {permission}, permissionCode);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == STORAGE_READ_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Storage Read Permission Granted",Toast.LENGTH_SHORT).show();
                openGallery();
            }else {
                Toast.makeText(this,"Storage Read Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
                requestStorageWriePerimission();
            }else {
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == STORAGE_WRITE_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
                openCamera();
            }else {
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,REQ_GALLERY_INTENT);
    }

    private void openCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,REQ_CAMERA_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: request code:"+requestCode+" result code: "+resultCode);
        if(requestCode == REQ_GALLERY_INTENT && resultCode == RESULT_OK ){
            if(null == data)
                return;
            imageUri = data.getData();
            if(imageUri == null){
                Toast.makeText(this,"Can't load image url",Toast.LENGTH_SHORT).show();
                return;
            }
            String uriString = imageUri.toString();
            File imageFile = new File(uriString);
            if(uriString.startsWith("content://")){
                Cursor cursor = null;
                try{
                    cursor = UploadImageActivity.this.getContentResolver().query(imageUri,null,null,null,null);
                    if(cursor != null && cursor.moveToFirst()){
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                }finally {
                    cursor.close();
                }
            }else if(uriString.startsWith("file://")){
                displayName = imageFile.getName();
            }else {
                Toast.makeText(this,"No matching file path",Toast.LENGTH_SHORT).show();
            }

            if(displayName!= null){
                tv_photo_url.setText(displayName);
                Glide.with(this).load(imageUri).apply(RequestOptions
                        .circleCropTransform())
                        .into(mImageView);
                btn_upload.setVisibility(View.VISIBLE);
                //Upload to Firebase
            }else {
                Toast.makeText(this,"display name is null",Toast.LENGTH_SHORT).show();

            }
        }
        else if(requestCode == REQ_CAMERA_INTENT && resultCode ==RESULT_OK){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            Uri tempUri = ImageCompressHelper.getImageUri(this,bitmap);
            String finalStringUrl = ImageCompressHelper.getRealPathFromURI(this,tempUri);
            File finalFile = new File(finalStringUrl);
            Uri finalUri = Uri.parse(finalStringUrl);
            Glide.with(this).load(finalUri).apply(RequestOptions.circleCropTransform()).into(mImageView);
        }
    }

    private void uploadImageToFirebase(){
        btn_upload.setVisibility(View.INVISIBLE);
        btn_skip.setVisibility(View.INVISIBLE);
        if(mStorageReference!=null && imageUri != null){
            final StorageReference filePath = mStorageReference.child(mAuth.getCurrentUser().getUid())
                    .child("display picture").child(displayName);
            mStorageTask = filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            User user = User.getUserInstance();
                            user.setPhotoUrl(uri.toString());
                            tv_upload_desc.setText("Upload Complete");
                            btn_upload.setText("Finish");
                            btn_upload.setVisibility(View.VISIBLE);
                            saveUserDatabase();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    mProgressBar.setProgress((int)progress);
                    String progressText,totalBytes ;
                    if(taskSnapshot.getTotalByteCount()>1048576){
                        totalBytes = taskSnapshot.getTotalByteCount()/1048576+"MB";
                    }else if(taskSnapshot.getTotalByteCount()>1024){
                        totalBytes = taskSnapshot.getTotalByteCount()/1024+"KB";
                    }else {
                        totalBytes = taskSnapshot.getTotalByteCount()+"Bytes";
                    }
                    if(taskSnapshot.getBytesTransferred()>1048576){
                        progressText = taskSnapshot.getBytesTransferred()/1048576+"MB/";
                    }else if(taskSnapshot.getBytesTransferred()>1024){
                        progressText = taskSnapshot.getBytesTransferred()/1024+"KB/";
                    }else {
                        progressText = taskSnapshot.getBytesTransferred()+"Bytes/";
                    }
                    tv_bytes_transferred.setText(progressText+totalBytes);
                    tv_upload_percent.setText((int)progress+"%");
                }
            });
        }else {
            Toast.makeText(this,"Storage reference or uri is null, can't upload image",Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserDatabase(){
        DatabaseReference mRefence = FirebaseDatabase.getInstance().getReference("users");
        mRefence.child(mAuth.getCurrentUser().getUid()).setValue(User.getUserInstance())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(UploadImageActivity.this,"Data updated, with image Url",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void openLoginActivity(){
        Intent intent = new Intent(this,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
