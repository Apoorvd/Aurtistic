package com.blogspot.atifsoftwares.firebaseapp;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.blogspot.atifsoftwares.firebaseapp.adapters.AdapterParticipantAdd;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class GroupParticipantAddActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private AdapterParticipantAdd adapterParticipantAdd;
    private FirebaseAuth firebaseAuth;
    private String groupId;
    private String myGroupRole;
    private ArrayList<ModelUser> userList;
    private RecyclerView usersRv;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participant_add);
        ActionBar supportActionBar = getSupportActionBar();
        this.actionBar = supportActionBar;
        supportActionBar.setTitle("Add Participants");
        this.actionBar.setDisplayShowHomeEnabled(true);
        this.actionBar.setDisplayHomeAsUpEnabled(true);
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.usersRv = (RecyclerView) findViewById(R.id.usersRv);
        this.groupId = getIntent().getStringExtra("groupId");
        loadGroupInfo();
    }

    private void getAllUsers() {
        this.userList = new ArrayList();
        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                GroupParticipantAddActivity.this.userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelUser modelUser = (ModelUser) ds.getValue(ModelUser.class);
                    if (!GroupParticipantAddActivity.this.firebaseAuth.getUid().equals(modelUser.getUid())) {
                        GroupParticipantAddActivity.this.userList.add(modelUser);
                    }
                }
                GroupParticipantAddActivity groupParticipantAddActivity = GroupParticipantAddActivity.this;
                GroupParticipantAddActivity groupParticipantAddActivity2 = GroupParticipantAddActivity.this;
                String str = "";
                groupParticipantAddActivity.adapterParticipantAdd = new AdapterParticipantAdd(groupParticipantAddActivity2, groupParticipantAddActivity2.userList, str + GroupParticipantAddActivity.this.groupId, str + GroupParticipantAddActivity.this.myGroupRole);
                GroupParticipantAddActivity.this.usersRv.setAdapter(GroupParticipantAddActivity.this.adapterParticipantAdd);
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = "Groups";
        final DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference(ref);
        FirebaseDatabase.getInstance().getReference(ref).orderByChild("groupId").equalTo(this.groupId).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String str = "";
                    String groupId = str + ds.child("groupId").getValue();
                    final String groupTitle = str + ds.child("groupTitle").getValue();
                    String groupDescription = str + ds.child("groupDescription").getValue();
                    String groupIcon = str + ds.child("groupIcon").getValue();
                    String createdBy = str + ds.child("createdBy").getValue();
                    str = str + ds.child("timestamp").getValue();
                    GroupParticipantAddActivity.this.actionBar.setSubtitle("Add Participants");
                    ref1.child(groupId).child("Participants").child(GroupParticipantAddActivity.this.firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                GroupParticipantAddActivity.this.myGroupRole = "" + dataSnapshot.child("role").getValue();
                                GroupParticipantAddActivity.this.actionBar.setTitle(groupTitle + "(" + GroupParticipantAddActivity.this.myGroupRole + ")");
                                GroupParticipantAddActivity.this.getAllUsers();
                            }
                        }

                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
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
