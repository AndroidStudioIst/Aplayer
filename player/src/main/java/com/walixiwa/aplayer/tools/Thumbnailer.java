package com.walixiwa.aplayer.tools;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.aplayer.APlayerAndroid;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Thumbnailer {
    private int count = 1;
    private String mediaPath;
    private OnThumbParseFinishListener onThumbParseFinishListener;
    private OnInfoParseFinishListener onInfoParseFinishListener;

    public Thumbnailer with(String mediaPath) {
        this.mediaPath = mediaPath;
        return this;
    }

    public Thumbnailer setCount(int count) {
        this.count = count;
        return this;
    }

    public Thumbnailer setOnThumbParseFinishListener(OnThumbParseFinishListener onThumbParseFinishListener) {
        this.onThumbParseFinishListener = onThumbParseFinishListener;
        return this;
    }

    public void start() {
        new Thread(() -> {
            List<APlayerAndroid.MediaInfo> list = new ArrayList<>();
            try {
                APlayerAndroid.MediaInfo mediaInfo = APlayerAndroid.parseThumbnail(mediaPath);
                double fix = mediaInfo.duration_ms / count;
                Log.e("thumber", "start: " + mediaInfo.duration_ms);
                for (int i = 0; i < count; i++) {
                    long timeMs = (long) (i * fix);
                    APlayerAndroid.MediaInfo info = APlayerAndroid.parseThumbnail(mediaPath, timeMs, -1, -1);
                    Log.e("thumber", "get -> timeMs: " + timeMs + " position: " + i);
                    if (onInfoParseFinishListener != null) {
                        new Handler(Looper.getMainLooper()).post(() -> onInfoParseFinishListener.onThumbParseFinish(info));
                    }
                    list.add(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.e("thumber", "ok");
            if (onThumbParseFinishListener != null) {
                new Handler(Looper.getMainLooper()).post(() -> onThumbParseFinishListener.onThumbParseFinish(list));
            }
        }).start();
    }

    public interface OnThumbParseFinishListener {
        void onThumbParseFinish(List<APlayerAndroid.MediaInfo> list);
    }

    public Thumbnailer setOnInfoParseFinishListener(OnInfoParseFinishListener onInfoParseFinishListener) {
        this.onInfoParseFinishListener = onInfoParseFinishListener;
        return this;
    }

    public interface OnInfoParseFinishListener {
        void onThumbParseFinish(APlayerAndroid.MediaInfo list);
    }
}
