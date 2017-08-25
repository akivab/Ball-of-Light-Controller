package com.example.akivabamberger.balloflightcontroller;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by akivabam.berger on 8/24/17.
 */

public class ImageManipulator {
    private static final String TAG = "ImageManipulator";
    private static final String kImageName = "ballOfLightController.png";

    public static void rotateImage(ImageView imageView, Activity activity) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        updateImageViewWithMatrix(matrix, imageView, activity);
    }

    public static void mirrorImage(ImageView imageView, Activity activity) {
        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1);
        updateImageViewWithMatrix(matrix, imageView, activity);
    }

    private static void updateImageViewWithMatrix(Matrix m, ImageView imageView, Activity activity) {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap src = drawable.getBitmap();
        Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, true);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        setImageBitmap(dst, imageView, activity);
    }

    public static File getImageFile(Context context) {
        return new File(context.getFilesDir(), kImageName);
    }

    public static void setImageBitmap(Bitmap bitmap, ImageView imageView, Activity activity) {
        imageView.setImageBitmap(bitmap);
    }

    public static void updateImageViewFromSavedFile(ImageView imageView, Activity activity) {
        try {
            InputStream inputStream = new FileInputStream(getImageFile(activity.getApplicationContext()));
            Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(inputStream));
            if (bitmap != null && bitmap.getWidth() != 0) {
                imageView.setImageBitmap(bitmap);
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Couldn't find file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void uploadImage(final ImageView imageView, final Activity activity) {
        final ProgressDialog dialog = new ProgressDialog(activity); // this = YourActivity
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Saving...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Thread th = new Thread() {
            @Override
            public void run() {
                File file = getImageFile(activity.getApplicationContext());
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                try

                {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream fOut = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    dialog.dismiss();

                    WiFiController.getInstance().uploadLatestImage(ImageManipulator.getImageFile(activity.getApplicationContext()), activity);

                } catch(IOException e)

                {
                    e.printStackTrace();
                    dialog.dismiss();

                }
            }
        };
        th.start();
    }


    public static void setSavedImageFromUri(Uri imageUri, Activity activity) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getApplicationContext().getContentResolver(),imageUri);
            try

            {
                File file = getImageFile(activity.getApplicationContext());
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fOut = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
