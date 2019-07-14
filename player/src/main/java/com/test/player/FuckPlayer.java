package com.test.player;

import android.app.Activity;
import android.content.Intent;

public class FuckPlayer {
    private String title = "";
    private String url = "";
    private Activity activity;
    private boolean isLive = false;
    private int resquestCode = -1;

    private boolean showToast = false;

    public FuckPlayer(Activity activity) {
        this.activity = activity;
    }

    public FuckPlayer setTitle(String title) {
        this.title = title;
        return this;
    }

    public FuckPlayer setUrl(String url) {
        this.url = url;
        return this;
    }

    public FuckPlayer islive(boolean isLive) {
        this.isLive = isLive;
        return this;
    }

    public FuckPlayer setResquestCode(int resquestCode) {
        this.resquestCode = resquestCode;
        return this;
    }

    public FuckPlayer setShowToast(boolean showToast) {
        this.showToast = showToast;
        return this;
    }

    public void start() {
        Intent intent = new Intent(activity, AplayerActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        intent.putExtra("isLive", isLive);
        intent.putExtra("showToast", showToast);
        if (resquestCode == -1) {
            activity.startActivity(intent);
        } else {
            activity.startActivityForResult(intent, resquestCode);
        }
    }

    public interface MyCallBack {
        void onPlayFinish();
    }
}
