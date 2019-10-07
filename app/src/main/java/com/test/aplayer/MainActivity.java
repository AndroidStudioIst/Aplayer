package com.test.aplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.walixiwa.aplayer.tools.APlayer;
import com.walixiwa.aplayer.tools.Thumbnailer;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView src_over;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        src_over = findViewById(R.id.src_over);
        findViewById(R.id.start).setOnClickListener(v -> {

            new APlayer(MainActivity.this).setTitle("蜘蛛侠：英雄远征").setUrl("/storage/emulated/0/资源鼠/download//8F87D27385AE45B528272B0DBCD25460298F1428/1/[BT乐园·bt606.com]钢铁侠2.Iron.Man.2.BD720P.X264.AAC.国英双语.中英字幕.mkv").setRequestCode(201).start();
           /* Log.e("info", "bitmap: start" );
            new Thumbnailer()
                    .with(this,"/storage/emulated/0/Pictures/ydcs.mkv")
                    .setCount(100)
                    .setOnThumbParseFinishListener(list -> {
                        for (int i = 0; i < list.size(); i++) {
                            Log.e("info", "bitmap: " + list.get(i).getUri());
                        }
                        Log.e("info", "bitmap: ok" );
                    })
                    .setOnInfoParseFinishListener(list -> {
                        Glide.with(this).load(list.getUri()).into(src_over);
                    })
                    .start();*/
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("info", "onActivityResult: requestCode->" + requestCode + "|resultCode->" + resultCode);
        if (requestCode == 201) {
            Toast.makeText(this, "播放完毕: " + requestCode, Toast.LENGTH_SHORT).show();
        }
    }
}
