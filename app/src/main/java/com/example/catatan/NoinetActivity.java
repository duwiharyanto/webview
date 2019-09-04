package com.example.catatan;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;

public class NoinetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noinet);
        Button button = (Button) findViewById(R.id.refresh);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent noinet=new Intent(NoinetActivity.this, com.example.catatan.MainActivity.class);
                startActivity(noinet);
                finish();
            }
        });
    }
}
