package com.example.aurtisticsv;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    Button mLoginBtn;
    Button mRegisterBtn;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mRegisterBtn = (Button) findViewById(R.id.register_btn);
        this.mLoginBtn = (Button) findViewById(R.id.loginBtn);
        this.mRegisterBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
        this.mLoginBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }
}
