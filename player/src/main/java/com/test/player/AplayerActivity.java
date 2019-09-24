package com.test.player;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aplayer.APlayerAndroid;

import java.lang.ref.WeakReference;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class AplayerActivity extends AppCompatActivity implements View.OnClickListener {
    private APlayerAndroid aPlayer = null;
    private SurfaceView holderView;
    private SeekBar seekBar;
    private TextView tv_position;
    private TextView tv_duration;
    private TextView tv_info;
    private ImageView play;
    private ImageView play_large;
    private TextView v_title;
    private LinearLayout header_bar;
    private LinearLayout ctrl_bar;

    private LinearLayout caching;
    private ImageView lock;
    private ImageView ratate;
    private Handler mainUIHandler;
    private boolean mIsNeedUpdateUIProgress = false;        /* 标志位，是否更新UI */
    private boolean mIsTouchingSeekbar = false;        /* 标志位,是否在滑动进度 */

    private boolean mIsSystemCallPause = false;
    private TextView cachingProgressHint = null;

    private int totalPosition;
    private int currentPosition;
    private int currentMovePosition;
    private float pressedX;
    private float pressedY;
    private float movedY;
    private int currentBrightness;
    private int currentVolume;
    private int pressedBrightness;
    private int pressedVolume;
    private int maxVolume;
    private int action;
    private AudioManager audioManager;
    private boolean locker = false;
    private int width;
    private int height;

    private String url = "";

    private boolean isLive;

    private int resultCode = -1;

    private boolean showToast = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aplayer);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initAPlayer();
        hideBottomUIMenu();
        Intent intent = getIntent();
        resultCode = getIntent().getIntExtra("resultCode", -1);
        v_title.setText(intent.getStringExtra("title"));
        url = intent.getStringExtra("url");
        isLive = intent.getBooleanExtra("isLive", false);
        showToast = intent.getBooleanExtra("showToast", false);

        aPlayer.open(url);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);    /* 强制为横屏 */
    }


    private void initAPlayer() {
        holderView = findViewById(R.id.holderView);
        header_bar = findViewById(R.id.header_bar);
        ctrl_bar = findViewById(R.id.ctrl_bar);
        caching = findViewById(R.id.caching);
        play = findViewById(R.id.v_play);
        play_large = findViewById(R.id.v_play_large);
        play_large.setOnClickListener(this);
        play.setOnClickListener(this);
        findViewById(R.id.v_back).setOnClickListener(this);
        ratate = findViewById(R.id.v_rotate);
        ratate.setOnClickListener(this);
        lock = findViewById(R.id.v_player_lock);
        lock.setOnClickListener(this);
        cachingProgressHint = findViewById(R.id.loading_text);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        this.width = displayMetrics.widthPixels;
        this.height = displayMetrics.heightPixels;

        this.audioManager = ((AudioManager) getSystemService(AUDIO_SERVICE));
        if (audioManager != null) {
            int j = this.audioManager.getStreamMaxVolume(3);
            this.maxVolume = j;
            this.maxVolume *= 6;
        }
        float f = this.currentVolume * 6;
        try {
            int k = Settings.System.getInt(getContentResolver(), "screen_brightness");
            f = 1.0F * k / 255.0F;
        } catch (Settings.SettingNotFoundException localSettingNotFoundException) {
            localSettingNotFoundException.printStackTrace();
        }
        this.currentBrightness = ((int) (f * 100.0F));
        this.pressedBrightness = ((int) (f * 100.0F));

        seekBar = findViewById(R.id.seekbar);
        tv_position = findViewById(R.id.tv_position);
        tv_duration = findViewById(R.id.tv_duration);
        tv_info = findViewById(R.id.tv_info);
        v_title = findViewById(R.id.v_title);
        aPlayer = new APlayerAndroid();
        aPlayer.setView(holderView);
        aPlayer.setOnOpenCompleteListener(b -> {
            if (b) {
                aPlayer.play();
            } else {
                caching.setVisibility(GONE);
            }
        });
        aPlayer.setOnOpenProgressListener(new APlayerAndroid.OnOpenProgressListener(){
            @Override
            public void onOpenProgress(int progress) {
                super.onOpenProgress(progress);
                int visibility = (progress == 100) ? View.INVISIBLE : VISIBLE;
                caching.setVisibility(visibility);
                String buff = "正在打开视频：" + progress + "%";
                cachingProgressHint.setText(buff);
            }
        });
        aPlayer.setOnPlayCompleteListener(s -> {
            switch (s) {
                case APlayerAndroid.PlayCompleteRet.PLAYRE_RESULT_COMPLETE:
                    if (!isLive) {
                        PlayerManger.getInstance(this).deleteInfo(url);
                    }
                    url = null;
                    finishPlay();
                    break;
                case APlayerAndroid.PlayCompleteRet.PLAYRE_RESULT_CLOSE:
                    finishPlay();
                    break;
                case APlayerAndroid.PlayCompleteRet.PLAYRE_RESULT_OPENRROR:
                    if (showToast) {
                        Toast.makeText(AplayerActivity.this, "视频打开失败!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case APlayerAndroid.PlayCompleteRet.PLAYRE_RESULT_DECODEERROR:
                    if (showToast) {
                        Toast.makeText(AplayerActivity.this, "视频解码失败!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case APlayerAndroid.PlayCompleteRet.PLAYRE_RESULT_HARDDECODERROR:
                    if (showToast) {
                        Toast.makeText(AplayerActivity.this, "硬件解码失败!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case APlayerAndroid.PlayCompleteRet.PLAYRE_RESULT_SEEKERROR:
                    if (showToast) {
                        Toast.makeText(AplayerActivity.this, "视频Seek失败!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case APlayerAndroid.PlayCompleteRet.PLAYRE_RESULT_READEFRAMERROR:
                    if (showToast) {
                        Toast.makeText(AplayerActivity.this, "读取内存失败!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case APlayerAndroid.PlayCompleteRet.PLAYRE_RESULT_CREATEGRAPHERROR:
                    if (showToast) {
                        Toast.makeText(AplayerActivity.this, "Create GRAPH Error!", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        });
        aPlayer.setOnBufferListener(i -> {
            Log.e("info", "onBuffer:" + i);
            int visibility = (i == 100) ? View.INVISIBLE : VISIBLE;
            caching.setVisibility(visibility);
            String strProgress = "缓冲: " + i + "%";
            cachingProgressHint.setText(strProgress);
        });
        aPlayer.setOnPlayStateChangeListener((i, i1) -> {
            switch (i) {
                case APlayerAndroid.PlayerState.APLAYER_CLOSEING:
                    caching.setVisibility(VISIBLE);
                    cachingProgressHint.setText("正在关闭...");
                    break;
               /* case APlayerAndroid.PlayerState.APLAYER_OPENING:
                    caching.setVisibility(VISIBLE);
                    cachingProgressHint.setText("正在打开视频...");
                    break;*/
                case APlayerAndroid.PlayerState.APLAYER_PAUSED:
                    caching.setVisibility(GONE);
                    break;
                case APlayerAndroid.PlayerState.APLAYER_PAUSING:
                    caching.setVisibility(VISIBLE);
                    cachingProgressHint.setText("正在暂停...");
                    break;
                case APlayerAndroid.PlayerState.APLAYER_PLAY:
                    break;
                case APlayerAndroid.PlayerState.APLAYER_READ:
                    break;
                case APlayerAndroid.PlayerState.APLAYER_RESET:
                    break;
                case APlayerAndroid.PlayerState.APLAYER_PLAYING:
                    if (!isLive) {
                        int position = PlayerManger.getInstance(this).getPosition(url);
                        if (position > 0) {
                            aPlayer.setPosition(position);
                        }
                    }
                    caching.setVisibility(GONE);
                    break;
            }
            if (i == APlayerAndroid.PlayerState.APLAYER_PLAYING) {
                play_large.setVisibility(GONE);
                play.setImageResource(R.drawable.v_play_pause);
                startUIUpdateThread();
            } else {
                play_large.setVisibility(GONE);
                play.setImageResource(R.drawable.v_play_arrow);
                stopUIUpdateThread();
            }
            Log.e("info", "preState:" + i1 + " >> State:" + i);
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (null == aPlayer || !fromUser) {
                    return;
                }

                mIsTouchingSeekbar = true;
                userSeekPlayProgress(progress, seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                /* Log.e(DEBUG_TAG, "onStartTrackingTouch"); */
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tv_info.setVisibility(GONE);
                mIsTouchingSeekbar = false;
                startUIUpdateThread();
            }
        });
        mainUIHandler = new MyHandler(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.v_play) {
            if (aPlayer != null) {
                if (aPlayer.getState() == APlayerAndroid.PlayerState.APLAYER_PLAYING) {
                    play.setImageResource(R.drawable.v_play_arrow);
                    aPlayer.pause();
                } else {
                    play.setImageResource(R.drawable.v_play_pause);
                    aPlayer.play();
                }
            }
        } else if (v.getId() == R.id.v_back) {
            finishPlay();
        } else if (v.getId() == R.id.v_rotate) {
            Configuration mConfiguration = this.getResources().getConfiguration();               /* 获取设置的配置信息 */
            int ori = mConfiguration.orientation;                           /* 获取屏幕方向 */
            if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                /* 横屏 */
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);            /* 强制为竖屏 */
            } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
                /* 竖屏 */
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);    /* 强制为横屏 */
            }
        } else if (v.getId() == R.id.v_player_lock) {
            if (!locker) {
                locker = true;
                hideCtrlBar();
                lock.setImageResource(R.drawable.v_player_locked);
            } else {
                lock.setImageResource(R.drawable.v_player_unlocked);
                locker = false;
                showBars();
            }
        } else if (v.getId() == R.id.v_play_large) {
            aPlayer.play();
        }
    }

    private void hideCtrlBar() {
        Animation animation = AnimationUtils.loadAnimation(AplayerActivity.this, R.anim.anim_top_out);
        header_bar.startAnimation(animation);
        header_bar.setVisibility(GONE);
        Animation animation2 = AnimationUtils.loadAnimation(AplayerActivity.this, R.anim.anim_bottom_out);
        ctrl_bar.startAnimation(animation2);
        ctrl_bar.setVisibility(GONE);
        Animation animation3 = AnimationUtils.loadAnimation(AplayerActivity.this, R.anim.anim_left_out);
        lock.startAnimation(animation3);
        lock.setVisibility(GONE);

        ratate.startAnimation(animation2);
        ratate.setVisibility(GONE);
    }

    static class MyHandler extends Handler {
        WeakReference<AplayerActivity> mActivity;

        MyHandler(AplayerActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            AplayerActivity activity = mActivity.get();
            if (msg.what == 1) {
                activity.tv_duration.setText(updateTextViewWithTimeFormat(msg.arg2));
                activity.tv_position.setText(updateTextViewWithTimeFormat(msg.arg1));
                if (msg.arg1 > 0 && msg.arg2 >= 0) {
                    activity.seekBar.setMax(msg.arg2);
                    activity.seekBar.setProgress(msg.arg1);
                } else {
                    activity.seekBar.setProgress(0);
                }
            }
        }
    }

    private Thread mUpdateThread = null;

    private void startUIUpdateThread() {
        if (null == mUpdateThread) {
            mIsNeedUpdateUIProgress = true;
            mUpdateThread = new Thread(new UpdatePlayUIProcess());
            mUpdateThread.start();
        } else {
            Log.e("info", "null != mUpdateThread");
        }
    }

    private void stopUIUpdateThread() {
        mIsNeedUpdateUIProgress = false;
        mUpdateThread = null;
    }

    private class UpdatePlayUIProcess implements Runnable {
        @Override
        public void run() {
            while (mIsNeedUpdateUIProgress) {
                if (!mIsTouchingSeekbar) {
                    int currentPlayTime = 0;
                    int durationTime = 0;
                    if (null != aPlayer) {
                        currentPlayTime = aPlayer.getPosition();
                        durationTime = aPlayer.getDuration();
                    }
                    Message msg = mainUIHandler.obtainMessage(1, currentPlayTime / 1000, durationTime / 1000);
                    mainUIHandler.sendMessage(msg);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.e("info", e.toString());
                }
            }
        }
    }

    private static String updateTextViewWithTimeFormat(int i) {
        int i2 = (i % 3600) / 60;
        int i3 = i % 60;
        if (i / 3600 != 0) {
            String format = String.format("%02d:%02d:%02d", Integer.valueOf(i / 3600), Integer.valueOf(i2), Integer.valueOf(i3));
            return format;
        }
        String format = String.format("%02d:%02d", Integer.valueOf(i2), Integer.valueOf(i3));
        return format;
    }

    private boolean isOverSeekGate(int seekBarPositionMs, int currentPlayPosMs) {
        final int SEEK_MIN_GATE_MS = 1000;
        return Math.abs(currentPlayPosMs - seekBarPositionMs) > SEEK_MIN_GATE_MS;
    }

    private void userSeekPlayProgress(int seekPostionMs, int max) {
        int currentPlayPos = aPlayer.getPosition();
        boolean isChangeOverSeekGate = isOverSeekGate(seekPostionMs * 1000, currentPlayPos);
        if (!isChangeOverSeekGate) {
            /* 避免拖动粒度过细，拖动时频繁定位影响体验 */
            return;
        }
        mIsTouchingSeekbar = true;
        stopUIUpdateThread();
        seekBar.setProgress(seekPostionMs);
        tv_info.setText(String.format("%s/%s", updateTextViewWithTimeFormat(seekPostionMs), updateTextViewWithTimeFormat(max)));
        tv_info.setVisibility(VISIBLE);
        aPlayer.setPosition(seekPostionMs * 1000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        float f;
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.totalPosition = this.aPlayer.getDuration() / 1000;
                this.currentPosition = this.aPlayer.getPosition() / 1000;
                this.pressedX = x;
                this.pressedY = y;
                this.action = 1;
                this.currentVolume = this.audioManager.getStreamVolume(3);
                this.pressedVolume = (this.currentVolume * 6);
                this.movedY = y;
                break;
            case MotionEvent.ACTION_UP:
                new Handler().postDelayed(() -> {
                    tv_info.setVisibility(GONE);
                    switch (action) {
                        case 2:
                            if (aPlayer.getDuration() > 5 && !locker) {
                                aPlayer.setPosition(currentMovePosition * 1000);
                            }
                            break;
                        case 3:
                            pressedBrightness = currentBrightness;
                            break;
                        case 4:
                            break;
                        default:
                            onClickEmptyArea();
                            break;
                    }
                }, 100L);

            case MotionEvent.ACTION_MOVE:
                if (!locker) {
                    f = Math.abs(x - this.pressedX);
                    float abs = Math.abs(y - this.pressedY);
                    if (this.action == 1) {
                        if (f > 50.0f && abs < 50.0f) {
                            this.action = 2;
                        }
                        if (f < 50.0f && abs > 50.0f && ((double) this.pressedX) < ((double) this.width) * 0.25d) {
                            this.action = 3;
                        }
                        if (f < 50.0f && abs > 50.0f && ((double) this.pressedX) > ((double) this.width) * 0.75d) {
                            this.action = 4;
                        }
                    }
                    switch (this.action) {
                        case 2:
                            this.currentMovePosition = (int) ((float) ((((double) (((x - this.pressedX) / ((float) this.width)) * ((float) this.totalPosition))) * 0.3d) + ((double) this.currentPosition)));
                            if (this.currentMovePosition < 0) {
                                this.currentMovePosition = 0;
                            }
                            if (this.currentMovePosition > this.totalPosition) {
                                this.currentMovePosition = this.totalPosition;
                            }
                            this.tv_info.setVisibility(VISIBLE);
                            this.tv_info.setText(String.format("%s/%s", updateTextViewWithTimeFormat(this.currentMovePosition), updateTextViewWithTimeFormat(this.totalPosition)));
                            break;
                        case 3:
                            float f6 = (y - this.movedY) * 100.0F / this.height;

                            this.currentBrightness = (this.pressedBrightness - (int) f6);
                            if (this.currentBrightness > 100) {
                                this.currentBrightness = 100;
                            }
                            if (this.currentBrightness < 7) {
                                this.currentBrightness = 7;
                            }
                            this.tv_info.setVisibility(VISIBLE);
                            int j = (this.currentBrightness - 7) * 100 / 93;
                            this.tv_info.setText("亮度：" + j + "%");

                            setBrightness(this.currentBrightness);
                            break;
                        case 4:
                            float f7 = (y - this.movedY) * 100.0F / this.height;

                            int currentVolumeM = (this.pressedVolume - (int) f7);
                            if (currentVolumeM > this.maxVolume) {
                                currentVolumeM = this.maxVolume;
                            }
                            if (currentVolumeM < 0) {
                                currentVolumeM = 0;
                            }
                            this.tv_info.setVisibility(VISIBLE);
                            int k = currentVolumeM * 100 / this.maxVolume;
                            this.tv_info.setText("音量：" + k + "%");
                            int m = currentVolumeM / 6;
                            this.audioManager.setStreamVolume(3, m, 0);
                            break;
                        default:
                            break;
                    }
                }
                break;
        }
        return (true);
    }

    private void onClickEmptyArea() {
        if (locker) {
            if (lock.getVisibility() != VISIBLE) {
                lock.setVisibility(VISIBLE);
                Animation animation3 = AnimationUtils.loadAnimation(AplayerActivity.this, R.anim.anim_left_in);
                lock.startAnimation(animation3);
            } else {
                Animation animation3 = AnimationUtils.loadAnimation(AplayerActivity.this, R.anim.anim_left_out);
                lock.startAnimation(animation3);
                lock.setVisibility(GONE);
            }
            return;
        }
        if (header_bar.getVisibility() == GONE) {
            showBars();
        } else {
            hideBottomUIMenu();
            hideCtrlBar();
        }
    }

    private void showBars() {
        header_bar.setVisibility(VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(AplayerActivity.this, R.anim.anim_top_in);
        header_bar.startAnimation(animation);

        ctrl_bar.setVisibility(VISIBLE);
        Animation animation2 = AnimationUtils.loadAnimation(AplayerActivity.this, R.anim.anim_bottom_in);
        ctrl_bar.startAnimation(animation2);

        lock.setVisibility(VISIBLE);
        Animation animation3 = AnimationUtils.loadAnimation(AplayerActivity.this, R.anim.anim_left_in);
        lock.startAnimation(animation3);

        ratate.setVisibility(VISIBLE);
        ratate.startAnimation(animation2);
    }

    public void setBrightness(int paramInt) {
        if (paramInt < 0) {
            paramInt = 0;
        }
        if (paramInt > 100) {
            paramInt = 100;
        }
        WindowManager.LayoutParams localLayoutParams = this.getWindow().getAttributes();
        localLayoutParams.screenBrightness = (1.0F * paramInt / 100.0F);
        this.getWindow().setAttributes(localLayoutParams);
        this.currentBrightness = paramInt;
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        /* 隐藏虚拟按键，并且全屏 */
        if (Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(GONE);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    protected void onPause() {
        int statue = aPlayer.getState();
        if (isPlay(statue)) {
            aPlayer.pause();
            mIsSystemCallPause = true;
        }
        /* 让播放进度UI更新线程退出 */
        stopUIUpdateThread();
        super.onPause();
    }

    @Override
    protected void onResume() {
        /* 普通播放，后台切换回来，恢复之前的播放状态 */
        if (mIsSystemCallPause) {
            aPlayer.play();
            mIsSystemCallPause = false;
        }
        startUIUpdateThread();
        super.onResume();
    }

    private void finishPlay() {
        if (url != null && !isLive) {
            PlayerManger.getInstance(this).addPlayerInfo(url, aPlayer.getPosition());
        }
        aPlayer.close();
        aPlayer.destroy();
        setResult(resultCode);
        finish();
    }

    private boolean isPlay(int status) {
        return APlayerAndroid.PlayerState.APLAYER_PLAY == status || APlayerAndroid.PlayerState.APLAYER_PLAYING == status;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        this.width = displayMetrics.widthPixels;
        this.height = displayMetrics.heightPixels;
    }
}
