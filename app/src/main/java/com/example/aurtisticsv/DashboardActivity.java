package com.example.aurtisticsv;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.example.aurtisticsv.fragments.ChatListFragment;
import com.example.aurtisticsv.fragments.HomeFragment;
import com.example.aurtisticsv.fragments.NotificationsFragment;
import com.example.aurtisticsv.fragments.ProfileFragment;
import com.example.aurtisticsv.fragments.UsersFragment;
import com.example.aurtisticsv.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.iid.FirebaseInstanceId;

public class DashboardActivity extends AppCompatActivity {
    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    String mUID;
    private BottomNavigationView navigationView;

    private OnNavigationItemSelectedListener selectedListener = new OnNavigationItemSelectedListener() {
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            String str = "";
            FragmentTransaction ft4;
            switch (menuItem.getItemId()) {
                case R.id.nav_chat /*2131231004*/:
                    DashboardActivity.this.actionBar.setTitle("Chats");
                    ChatListFragment fragment4 = new ChatListFragment();
                    ft4 = DashboardActivity.this.getSupportFragmentManager().beginTransaction();
                    ft4.replace(2131230852, fragment4, str);
                    ft4.commit();
                    return true;
                case R.id.nav_home /*2131231005*/:
                    DashboardActivity.this.actionBar.setTitle("Home");
                    HomeFragment fragment1 = new HomeFragment();
                    ft4 = DashboardActivity.this.getSupportFragmentManager().beginTransaction();
                    ft4.replace(R.id.content, fragment1,str);
                    ft4.commit();
                    return true;
                case R.id.nav_more /*2131231006*/:
                    DashboardActivity.this.showMoreOptions();
                    return true;
                case R.id.nav_profile /*2131231007*/:
                    DashboardActivity.this.actionBar.setTitle("Profile");
                    ProfileFragment fragment2 = new ProfileFragment();
                    ft4 = DashboardActivity.this.getSupportFragmentManager().beginTransaction();
                    ft4.replace(R.id.content, fragment2, str);
                    ft4.commit();
                    return true;
                case R.id.nav_users /*2131231008*/:
                    DashboardActivity.this.actionBar.setTitle("Users");
                    UsersFragment fragment3 = new UsersFragment();
                    ft4 = DashboardActivity.this.getSupportFragmentManager().beginTransaction();
                    ft4.replace(R.id.content, fragment3, str);
                    ft4.commit();
                    return true;
                default:
                    return false;
            }
        }
    };

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        ActionBar supportActionBar = getSupportActionBar();
        this.actionBar = supportActionBar;
        supportActionBar.setTitle("Profile");
        this.firebaseAuth = FirebaseAuth.getInstance();
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        this.navigationView = bottomNavigationView;
        bottomNavigationView.setOnNavigationItemSelectedListener(this.selectedListener);
        this.actionBar.setTitle("Home");
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1,"");
        ft1.commit();
        checkUserStatus();
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        ref.child(this.mUID).setValue(new Token(token));
    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, this.navigationView, 8388613);
        popupMenu.getMenu().add(0, 0, 0, "Notifications");
        popupMenu.getMenu().add(0, 1, 0, "Group Chats");
        popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                String str = "";
                FragmentTransaction ft5;
                if (id == 0) {
                    DashboardActivity.this.actionBar.setTitle("Notifications");
                    NotificationsFragment fragment5 = new NotificationsFragment();
                    ft5 = DashboardActivity.this.getSupportFragmentManager().beginTransaction();
                    ft5.replace(2131230852, fragment5, str);
                    ft5.commit();
                } else if (id == 1) {
                    DashboardActivity.this.actionBar.setTitle("Group Chats");
                    GroupChatsFragment fragment6 = new GroupChatsFragment();
                    ft5 = DashboardActivity.this.getSupportFragmentManager().beginTransaction();
                    ft5.replace(2131230852, fragment6, str);
                    ft5.commit();
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void checkUserStatus() {
        FirebaseUser user = this.firebaseAuth.getCurrentUser();
        if (user != null) {
            this.mUID = user.getUid();
            Editor editor = getSharedPreferences("SP_USER", null).edit();
            editor.putString("Current_USERID", this.mUID);
            editor.apply();
            updateToken(FirebaseInstanceId.getInstance().getToken());
            return;
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /* Access modifiers changed, original: protected */
    public void onStart() {
        checkUserStatus();
        super.onStart();
    }
}
