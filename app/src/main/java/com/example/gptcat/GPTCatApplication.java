package com.example.gptcat;

import java.io.File;
import android.app.Application;

public class GPTCatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        File dexOutputDir = getCodeCacheDir();
        boolean success = dexOutputDir.setReadOnly();

        if (!success) {
            System.err.println("Failed to set dexOutputDir to read-only");
        }
    }
}