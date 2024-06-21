package com.example.gptcat;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GPTCatScreenshotObserver extends ContentObserver {

    private static final String TAG = "ScreenshotObserver";
    private final Context context;
    private final List<String> strings = new ArrayList<>();

    public GPTCatScreenshotObserver(Context context, Handler handler) {
        super(handler);
        this.context = context;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);

        if (uri.toString().startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())) {
            String path = getImagePathFromUri(uri);
            if (path != null && path.toLowerCase().endsWith(".png") && !(path.contains(".pending")) && !(strings.contains(path))) {
                strings.add(path);
                Log.d(TAG, "Screenshot captured: " + path);

                CompletableFuture
                        .supplyAsync(() -> GPTCatHttp.sendImageToDiscord(path))
                        .thenComposeAsync(imageUrl -> CompletableFuture.supplyAsync(() -> GPTCatHttp.sendImageToChatGPT4o(imageUrl)))
                        .thenAcceptAsync(answer -> {
                            Log.d("Answer", answer);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, answer, Toast.LENGTH_LONG).show();
                                }
                            });
                        });
            }
        }
    }

    private String getImagePathFromUri(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }
}
