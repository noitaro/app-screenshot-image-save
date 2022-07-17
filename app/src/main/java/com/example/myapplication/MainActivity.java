package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void screenshot(View view) {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) this.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        activityResultLauncher.launch(mediaProjectionManager.createScreenCaptureIntent());
    }

    ActivityResultLauncher<Intent> activityResultLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {

                EditText editText1 = findViewById(R.id.editTextNumber);
                int maxCount = Integer.parseInt(editText1.getText().toString());
                EditText editText2 = findViewById(R.id.editTextNumber2);
                int waitSeconds = Integer.parseInt(editText2.getText().toString());

                Intent intent = new Intent(getApplication(), CaptureService.class);
                intent.putExtra("ResultCode", result.getResultCode());
                intent.putExtra("ResultData", result.getData());
                intent.putExtra("MaxCount", maxCount);
                intent.putExtra("WaitSeconds", waitSeconds);
                startForegroundService(intent);
            }
        });
}