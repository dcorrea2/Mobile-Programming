package com.darrencorrea.lab6facedetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView iw;
    Canvas canvas;
    Bitmap mutableBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .build();

        Bitmap bm=getBitmapFromAssets("faces.png");

        iw = (ImageView)findViewById(R.id.imageView);
        iw.setImageBitmap(mutableBitmap);

        mutableBitmap = bm.copy(Bitmap.Config.ARGB_8888, true);
        canvas = new Canvas(mutableBitmap);

        InputImage image = InputImage.fromBitmap(bm, 0);

        FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);

        Task<List<Face>> result =
                detector.process(image)
                    .addOnSuccessListener(
                            new OnSuccessListener<List<Face>>() {
                                @Override
                                public void onSuccess(List<Face> faces) {
                                    for (Face face : faces){
                                        Rect bounds = face.getBoundingBox();

                                        Paint paint = new Paint();
                                        paint.setAntiAlias(true);
                                        paint.setColor(Color.YELLOW);
                                        paint.setStyle(Paint.Style.STROKE);
                                        paint.setStrokeWidth(8);

                                        canvas.drawRect(bounds, paint);

                                        iw = (ImageView)findViewById(R.id.imageView);
                                        iw.setImageBitmap(mutableBitmap);
                                        Log.d("TAG","recognition succeed");
                                    }
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Task failed with an exception
                                    // ...
                                    Log.d("TAG","recognition failed");
                                    Toast.makeText(getApplicationContext(),(String)e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

    }

    private Bitmap getBitmapFromAssets(String fileName){
        AssetManager am = getAssets();
        InputStream is = null;
        try{
            is = am.open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        return bitmap;
    }
}

