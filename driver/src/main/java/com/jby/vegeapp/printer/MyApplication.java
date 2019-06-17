package com.jby.vegeapp.printer;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import com.jby.vegeapp.R;
import com.jby.vegeapp.Utils.StaticVar;
import com.jby.vegeapp.printer.Crash.CrashHandler;
import com.jby.vegeapp.printer.Manager.PrintfManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends Application {

    ExecutorService cachedThreadPool = null;

    public ExecutorService getCachedThreadPool() {
        return cachedThreadPool;
    }


    private Handler handler = new Handler();

    public Handler getHandler() {
        return handler;
    }

    static MyApplication instance = null;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        cachedThreadPool = Executors.newCachedThreadPool();
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.basket_icon);
                int left = PrintfManager.getCenterLeft(72, bitmap);
                byte[] bytes = PrintfManager.bitmap2PrinterBytes(bitmap, left);
                StaticVar.bitmap_80 = bytes;
                left = PrintfManager.getCenterLeft(48, bitmap);
                bytes = PrintfManager.bitmap2PrinterBytes(bitmap, left);
                StaticVar.bitmap_58 = bytes;
            }
        });
    }
}
