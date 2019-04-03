package com.example.itc;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;

public class ImageCompressHelper {

    public static Uri getImageThumbnailUri(Context context, Bitmap inImageBitmap){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImageBitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),inImageBitmap,"title",null);
        return Uri.parse(path);
    }

    public static Uri getImageUri(Context context,Bitmap inImage){
        Bitmap outImage = Bitmap.createScaledBitmap(inImage,1024,720,true);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),outImage,"Title",null);
        return Uri.parse(path);
    }

    public static String getRealPathFromURI(Context context,Uri uri){
        String path = "";
        if(context.getContentResolver() != null){
            Cursor cursor = context.getContentResolver().query(uri,null,null,null,null);
            if(cursor!=null){
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }
}
