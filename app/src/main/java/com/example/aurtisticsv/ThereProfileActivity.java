package com.blogspot.atifsoftwares.firebaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blogspot.atifsoftwares.firebaseapp.adapters.AdapterPosts;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {
    AdapterPosts adapterPosts;
    ImageView avatarIv;
    ImageView coverIv;
    TextView emailTv;
    FirebaseAuth firebaseAuth;
    TextView nameTv;
    TextView phoneTv;
    List<ModelPost> postList;
    RecyclerView postsRecyclerView;
    String uid;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        this.avatarIv = (ImageView) findViewById(R.id.avatarIv);
        this.coverIv = (ImageView) findViewById(R.id.coverIv);
        this.nameTv = (TextView) findViewById(R.id.nameTv);
        this.emailTv = (TextView) findViewById(R.id.emailTv);
        this.phoneTv = (TextView) findViewById(R.id.phoneTv);
        this.postsRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_posts);
        this.firebaseAuth = FirebaseAuth.getInstance();
        String str = "uid";
        this.uid = getIntent().getStringExtra(str);
        FirebaseDatabase.getInstance().getReference("Users").orderByChild(str).equalTo(this.uid).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String str = "";
                    String name = str + ds.child("name").getValue();
                    String email = str + ds.child("email").getValue();
                    String phone = str + ds.child("phone").getValue();
                    String image = str + ds.child("image").getValue();
                    str = str + ds.child("cover").getValue();
                    ThereProfileActivity.this.nameTv.setText(name);
                    ThereProfileActivity.this.emailTv.setText(email);
                    ThereProfileActivity.this.phoneTv.setText(phone);
                    try {
                        Picasso.get().load(image).into(ThereProfileActivity.this.avatarIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img_white).into(ThereProfileActivity.this.avatarIv);
                    }
                    try {
                        Picasso.get().load(str).into(ThereProfileActivity.this.coverIv);
                    } catch (Exception e2) {
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
        this.postList = new ArrayList();
        checkUserStatus();
        loadHistPosts();
    }

    private void loadHistPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        this.postsRecyclerView.setLayoutManager(layoutManager);
        FirebaseDatabase.getInstance().getReference("Posts").orderByChild("uid").equalTo(this.uid).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                ThereProfileActivity.this.postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ThereProfileActivity.this.postList.add((ModelPost) ds.getValue(ModelPost.class));
                    ThereProfileActivity thereProfileActivity = ThereProfileActivity.this;
                    ThereProfileActivity thereProfileActivity2 = ThereProfileActivity.this;
                    thereProfileActivity.adapterPosts = new AdapterPosts(thereProfileActivity2, thereProfileActivity2.postList);
                    ThereProfileActivity.this.postsRecyclerView.setAdapter(ThereProfileActivity.this.adapterPosts);
                }
            }

            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ThereProfileActivity.this, "" + databaseError.getMessage(), 0).show();
            }
        });
    }

    private void searchHistPosts(final String searchQuery) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        this.postsRecyclerView.setLayoutManager(layoutManager);
        FirebaseDatabase.getInstance().getReference("Posts").orderByChild("uid").equalTo(this.uid).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                ThereProfileActivity.this.postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPosts = (ModelPost) ds.getValue(ModelPost.class);
                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) || myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        ThereProfileActivity.this.postList.add(myPosts);
                    }
                    ThereProfileActivity thereProfileActivity = ThereProfileActivity.this;
                    ThereProfileActivity thereProfileActivity2 = ThereProfileActivity.this;
                    thereProfileActivity.adapterPosts = new AdapterPosts(thereProfileActivity2, thereProfileActivity2.postList);
                    ThereProfileActivity.this.postsRecyclerView.setAdapter(ThereProfileActivity.this.adapterPosts);
                }
            }

            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ThereProfileActivity.this, "" + databaseError.getMessage(), 0).show();
            }
        });
    }

    private void checkUserStatus() {
        if (this.firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        ((SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search))).setOnQueryTextListener(new OnQueryTextListener() {
            public boolean onQueryTextSubmit(String s) {
                if (TextUtils.isEmpty(s)) {
                    ThereProfileActivity.this.loadHistPosts();
                } else {
                    ThereProfileActivity.this.searchHistPosts(s);
                }
                return false;
            }

            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)) {
                    ThereProfileActivity.this.loadHistPosts();
                } else {
                    ThereProfileActivity.this.searchHistPosts(s);
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            this.firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
