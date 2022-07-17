package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;


public class CaptureService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        NotificationChannel channel = new NotificationChannel("channelId", "channelName", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, "channelId")
            .setContentTitle("notification_title")
            .setContentText("notification_message")
            .build();
        startForeground(1, notification);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        int density = metrics.densityDpi;

        int resultCode = intent.getIntExtra("ResultCode", 0);
        Intent resultData = intent.getParcelableExtra("ResultData");
        int maxCount = intent.getIntExtra("MaxCount", 0);
        int waitSeconds = intent.getIntExtra("WaitSeconds", 0);

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) this.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData);
        ImageReader imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        VirtualDisplay virtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);

        Thread thread = new Thread(() -> {
            try {
                for (int i = 0; i < maxCount; i++) {
                    Image image = imageReader.acquireLatestImage();
                    // 画面に変更がない場合、NULL
                    if (image != null) {

                        // Image -> Bitmap
                        Image.Plane[] planes = image.getPlanes();
                        ByteBuffer buffer = planes[0].getBuffer();
                        int pixelStride = planes[0].getPixelStride();
                        int rowStride = planes[0].getRowStride();
                        int rowPadding = rowStride - pixelStride * width;
                        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(buffer);
                        image.close();

                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/image_" + System.currentTimeMillis() + ".png");
                        FileOutputStream outStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                        outStream.close();
                    }

                    Log.d("CaptureService", "onStartCommand: " + i);
                    Thread.sleep(waitSeconds * 1000);
                }
            } catch (Exception e) {
                Log.e("CaptureService", e.getMessage());
            }

            stopSelf();
        });
        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }
}
