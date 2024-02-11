package com.blogspot.atifsoftwares.firebaseapp;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {
    private static final String TOPIC_POST_NOTIFICATION = "POST";
    Editor editor;
    SwitchCompat postSwitch;
    SharedPreferences sp;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        this.postSwitch = (SwitchCompat) findViewById(R.id.postSwitch);
        boolean isPostEnabled = getSharedPreferences("Notification_SP", 0);
        this.sp = isPostEnabled;
        if (isPostEnabled.getBoolean(TOPIC_POST_NOTIFICATION, false)) {
            this.postSwitch.setChecked(true);
        } else {
            this.postSwitch.setChecked(false);
        }
        this.postSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsActivity settingsActivity = SettingsActivity.this;
                settingsActivity.editor = settingsActivity.sp.edit();
                SettingsActivity.this.editor.putBoolean(SettingsActivity.TOPIC_POST_NOTIFICATION, isChecked);
                SettingsActivity.this.editor.apply();
                if (isChecked) {
                    SettingsActivity.this.subscribePostNotification();
                } else {
                    SettingsActivity.this.unsubscribePostNotification();
                }
            }
        });
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void unsubscribePostNotification() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_POST_NOTIFICATION).addOnCompleteListener(new OnCompleteListener<Void>() {
            public void onComplete(Task<Void> task) {
                String msg = "You will not receive post notifications";
                if (!task.isSuccessful()) {
                    msg = "UnSubscription failed";
                }
                Toast.makeText(SettingsActivity.this, msg, 0).show();
            }
        });
    }

    private void subscribePostNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_POST_NOTIFICATION).addOnCompleteListener(new OnCompleteListener<Void>() {
            public void onComplete(Task<Void> task) {
                String msg = "You will receive post notifications";
                if (!task.isSuccessful()) {
                    msg = "Subscription failed";
                }
                Toast.makeText(SettingsActivity.this, msg, 0).show();
            }
        });
    }
}
