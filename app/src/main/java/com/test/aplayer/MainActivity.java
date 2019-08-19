package com.test.aplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.test.player.FuckPlayer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText title;
    private EditText url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        title = findViewById(R.id.title);
        url = findViewById(R.id.url);
        findViewById(R.id.start).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                String name = title.getText().toString();
                String link = url.getText().toString();

                new FuckPlayer(this).setTitle(name).setUrl(link).setResquestCode(201).start();

                break;
        }
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
