package com.blogspot.atifsoftwares.firebaseapp;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.blogspot.atifsoftwares.firebaseapp.adapters.AdapterUsers;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class PostLikedByActivity extends AppCompatActivity {
    private AdapterUsers adapterUsers;
    private FirebaseAuth firebaseAuth;
    String postId;
    private RecyclerView recyclerView;
    private List<ModelUser> userList;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_liked_by);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Liked By");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        FirebaseAuth instance = FirebaseAuth.getInstance();
        this.firebaseAuth = instance;
        actionBar.setSubtitle(instance.getCurrentUser().getEmail());
        this.recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        this.postId = getIntent().getStringExtra("postId");
        this.userList = new ArrayList();
        FirebaseDatabase.getInstance().getReference("Likes").child(this.postId).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                PostLikedByActivity.this.userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    PostLikedByActivity.this.getUsers("" + ds.getRef().getKey());
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getUsers(String hisUid) {
        FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(hisUid).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    PostLikedByActivity.this.userList.add((ModelUser) ds.getValue(ModelUser.class));
                }
                PostLikedByActivity postLikedByActivity = PostLikedByActivity.this;
                PostLikedByActivity postLikedByActivity2 = PostLikedByActivity.this;
                postLikedByActivity.adapterUsers = new AdapterUsers(postLikedByActivity2, postLikedByActivity2.userList);
                PostLikedByActivity.this.recyclerView.setAdapter(PostLikedByActivity.this.adapterUsers);
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
