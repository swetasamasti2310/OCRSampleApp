package com.example.ocrsampleapp;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private int PERMISSION_REQUEST_CAMERA = 100;

    private CameraSource cameraSource;
    private TextRecognizer textRecognizer;
    private TextView resultText;

    private SurfaceView cameraSurfacePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraSurfacePreview = findViewById(R.id.surface_camera_preview);
        resultText = findViewById(R.id.tv_result);
        resultText.setMovementMethod(new ScrollingMovementMethod());

        startCameraSource();

    }

    private void startCameraSource()
    {
        textRecognizer = new TextRecognizer.Builder(this).build();
        if (!textRecognizer.isOperational()) {
            showToast("Dependencies are not yet downloaded... try after sometime");
            Log.i(LOG_TAG, "dependencies not downloaded");
            return;
        }

        cameraSource = new CameraSource.Builder(this, textRecognizer)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1280, 1024)
            .setAutoFocusEnabled(true)
            .setRequestedFps(2.0f)
            .build();

        cameraSurfacePreview.getHolder().addCallback(new SurfaceHolder.Callback()
        {
            @SuppressLint("MissingPermission")
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder)
            {
                try {
                    if (isCameraPermissionGranted()) {
                        cameraSource.start();
                    }
                    else {
                        requestForCameraPermission();
                    }
                }
                catch (Exception e) {
                    Log.e(LOG_TAG, "Exception in camera start: ", e);
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2)
            {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder)
            {
                cameraSource.stop();
            }
        });

        textRecognizer.setProcessor(new Detector.Processor<TextBlock>()
        {
            @Override
            public void release()
            {

            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections)
            {
                final SparseArray<TextBlock> items = detections.getDetectedItems();
                if (items.size() <= 0) {
                    return;
                }

                StringBuilder sb = new StringBuilder();
                for(int i=0; i<items.size(); i++) {
                    TextBlock item = items.get(i);
                    sb.append(item.getValue());
                    sb.append("\n");
                }
                String currText = String.valueOf(resultText.getText());
                currText = currText + sb.toString();
                resultText.setText(currText);

            }
        });
    }

    private void requestForCameraPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
    }

    private boolean isCameraPermissionGranted()
    {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED;
    }

    private void showToast(String toastText)
    {
        Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
    }

}
