package com.darrencorrea.lab2b;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;

    //Declare objects
    Button takePhoto;
    ImageView photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get objects
        takePhoto = findViewById(R.id.btnTakePhoto);
        photo = findViewById(R.id.imgPhotoTaken);

        //Add click event to takephoto button object
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                dispatchTakePictureIntent();
            }
        });
    }

    //Take photo with camera application
    private void dispatchTakePictureIntent(){
        //Check devices camera availability and number of cameras
        checkCameraHardware(MainActivity.this);

        //A quick way to enable taking pictures or videos in your application without a lot of extra
        // code is to use an Intent to invoke an existing Android camera application
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Ensure that theres a camera activity to handle the intent
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            //Create a file where the photo should go
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch(IOException ex){
                Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            if(photoFile != null){
                //We are using getUriForFile(Context, String, File) which returns a content URI.
                //For more recent apps targeting Android 7.0 (API level 24) and higher.
                //Passing a file: URI across a package boundry causes a FieUriExposedException.
                //Therefore, we now present a more generic way of storing images using a FileProvider.
                Uri photoURI = FileProvider.getUriForFile(this, "com.darrencorrea.android.fireproof", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    //Create a time-based file name
    private File createImageFile() throws IOException {
        //Crete an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        //Save a file; path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        //Toast.makeText(MainActivity.this, currentPhotoPath, Toast.LENGTH_SHORT).show();
        return image;
    }

    //Get thumbnail of image captured and store into photo object
    //The Android Camera application encodes the photo in the return intent
    // delivered to onActivitResult() as a small Bitmap in the extras, under the key "data"
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            photo.setImageBitmap(imageBitmap);
             */

            //Invoke media scanner
            //galleryAddPic();
            setPic();
        }
    }

    //Set multiple full size images to be tricky with limited memory
    private void setPic(){
        //Get the dimensions of the View
        int targetW = photo.getWidth();
        int targetH = photo.getHeight();

        //Get dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        //Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        //Decode the image file into a Bitmap sized to fill the view
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        photo.setImageBitmap(bitmap);
    }

    //check if this device has a camera
    private boolean checkCameraHardware(Context context){
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            //this device has a camera
            Toast.makeText(context, "This device has a camera", Toast.LENGTH_SHORT).show();
            //Check how many cameras it has
            Toast.makeText(context, "This device has" + String.valueOf(Camera.getNumberOfCameras()) + " cameras", Toast.LENGTH_SHORT).show();
            return true;
        } else{
            //no camera on this device
            Toast.makeText(context, "This device has no camera", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}