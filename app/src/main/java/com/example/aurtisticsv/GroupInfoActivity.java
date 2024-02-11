package com.blogspot.atifsoftwares.firebaseapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.blogspot.atifsoftwares.firebaseapp.adapters.AdapterParticipantAdd;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private AdapterParticipantAdd adapterParticipantAdd;
    private TextView addParticipantTv;
    private TextView createdByTv;
    private TextView descriptionTv;
    private TextView editGroupTv;
    private FirebaseAuth firebaseAuth;
    private ImageView groupIconIv;
    private String groupId;
    private TextView leaveGroupTv;
    private String myGroupRole = "";
    private RecyclerView participantsRv;
    private TextView participantsTv;
    private ArrayList<ModelUser> userList;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        ActionBar supportActionBar = getSupportActionBar();
        this.actionBar = supportActionBar;
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        this.actionBar.setDisplayShowHomeEnabled(true);
        this.groupIconIv = (ImageView) findViewById(R.id.groupIconIv);
        this.descriptionTv = (TextView) findViewById(R.id.descriptionTv);
        this.createdByTv = (TextView) findViewById(R.id.createdByTv);
        this.editGroupTv = (TextView) findViewById(R.id.editGroupTv);
        this.addParticipantTv = (TextView) findViewById(R.id.addParticipantTv);
        this.leaveGroupTv = (TextView) findViewById(R.id.leaveGroupTv);
        this.participantsTv = (TextView) findViewById(R.id.participantsTv);
        this.participantsRv = (RecyclerView) findViewById(R.id.participantsRv);
        this.groupId = getIntent().getStringExtra("groupId");
        this.firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadMyGroupRole();
        this.addParticipantTv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupParticipantAddActivity.class);
                intent.putExtra("groupId", GroupInfoActivity.this.groupId);
                GroupInfoActivity.this.startActivity(intent);
            }
        });
        this.editGroupTv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupEditActivity.class);
                intent.putExtra("groupId", GroupInfoActivity.this.groupId);
                GroupInfoActivity.this.startActivity(intent);
            }
        });
        this.leaveGroupTv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String dialogTitle = "";
                String dialogDescription = "";
                String positiveButtonTitle = "";
                if (GroupInfoActivity.this.myGroupRole.equals("creator")) {
                    dialogTitle = "Delete Group";
                    dialogDescription = "Are you sure you want to Delete group permanently?";
                    positiveButtonTitle = "DELETE";
                } else {
                    dialogTitle = "Leave Group";
                    dialogDescription = "Are you sure you want to Leave group permanently?";
                    positiveButtonTitle = "LEAVE";
                }
                new Builder(GroupInfoActivity.this).setTitle(dialogTitle).setMessage(dialogDescription).setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (GroupInfoActivity.this.myGroupRole.equals("creator")) {
                            GroupInfoActivity.this.deleteGroup();
                        } else {
                            GroupInfoActivity.this.leaveGroup();
                        }
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });
    }

    private void leaveGroup() {
        FirebaseDatabase.getInstance().getReference("Groups").child(this.groupId).child("Participants").child(this.firebaseAuth.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                Toast.makeText(GroupInfoActivity.this, "Group left successfully...", 0).show();
                GroupInfoActivity.this.startActivity(new Intent(GroupInfoActivity.this, DashboardActivity.class));
                GroupInfoActivity.this.finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                Toast.makeText(GroupInfoActivity.this, "" + e.getMessage(), 0).show();
            }
        });
    }

    private void deleteGroup() {
        FirebaseDatabase.getInstance().getReference("Groups").child(this.groupId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                Toast.makeText(GroupInfoActivity.this, "Group successfully deleted...", 0).show();
                GroupInfoActivity.this.startActivity(new Intent(GroupInfoActivity.this, DashboardActivity.class));
                GroupInfoActivity.this.finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                Toast.makeText(GroupInfoActivity.this, "" + e.getMessage(), 0).show();
            }
        });
    }

    private void loadGroupInfo() {
        FirebaseDatabase.getInstance().getReference("Groups").orderByChild("groupId").equalTo(this.groupId).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String str = "";
                    String groupId = str + ds.child("groupId").getValue();
                    String groupTitle = str + ds.child("groupTitle").getValue();
                    String groupDescription = str + ds.child("groupDescription").getValue();
                    String groupIcon = str + ds.child("groupIcon").getValue();
                    String createdBy = str + ds.child("createdBy").getValue();
                    str = str + ds.child("timestamp").getValue();
                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(str));
                    GroupInfoActivity.this.loadCreatorInfo(DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString(), createdBy);
                    GroupInfoActivity.this.actionBar.setTitle(groupTitle);
                    GroupInfoActivity.this.descriptionTv.setText(groupDescription);
                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary).into(GroupInfoActivity.this.groupIconIv);
                    } catch (Exception e) {
                        GroupInfoActivity.this.groupIconIv.setImageResource(R.drawable.ic_group_primary);
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadCreatorInfo(final String dateTime, String createBy) {
        FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(createBy).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    GroupInfoActivity.this.createdByTv.setText("Created by " + ("" + ds.child("name").getValue()) + " on " + dateTime);
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadMyGroupRole() {
        FirebaseDatabase.getInstance().getReference("Groups").child(this.groupId).child("Participants").orderByChild("uid").equalTo(this.firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    GroupInfoActivity.this.myGroupRole = "" + ds.child("role").getValue();
                    GroupInfoActivity.this.actionBar.setSubtitle(GroupInfoActivity.this.firebaseAuth.getCurrentUser().getEmail() + " (" + GroupInfoActivity.this.myGroupRole + ")");
                    String str = "Leave Group";
                    if (GroupInfoActivity.this.myGroupRole.equals("participant")) {
                        GroupInfoActivity.this.editGroupTv.setVisibility(8);
                        GroupInfoActivity.this.addParticipantTv.setVisibility(8);
                        GroupInfoActivity.this.leaveGroupTv.setText(str);
                    } else if (GroupInfoActivity.this.myGroupRole.equals("admin")) {
                        GroupInfoActivity.this.editGroupTv.setVisibility(8);
                        GroupInfoActivity.this.addParticipantTv.setVisibility(0);
                        GroupInfoActivity.this.leaveGroupTv.setText(str);
                    } else if (GroupInfoActivity.this.myGroupRole.equals("creator")) {
                        GroupInfoActivity.this.editGroupTv.setVisibility(0);
                        GroupInfoActivity.this.addParticipantTv.setVisibility(0);
                        GroupInfoActivity.this.leaveGroupTv.setText("Delete Group");
                    }
                }
                GroupInfoActivity.this.loadParticipants();
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadParticipants() {
        this.userList = new ArrayList();
        FirebaseDatabase.getInstance().getReference("Groups").child(this.groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                GroupInfoActivity.this.userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String str = "uid";
                    FirebaseDatabase.getInstance().getReference("Users").orderByChild(str).equalTo("" + ds.child(str).getValue()).addValueEventListener(new ValueEventListener() {
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                GroupInfoActivity.this.userList.add((ModelUser) ds.getValue(ModelUser.class));
                            }
                            GroupInfoActivity.this.adapterParticipantAdd = new AdapterParticipantAdd(GroupInfoActivity.this, GroupInfoActivity.this.userList, GroupInfoActivity.this.groupId, GroupInfoActivity.this.myGroupRole);
                            GroupInfoActivity.this.participantsRv.setAdapter(GroupInfoActivity.this.adapterParticipantAdd);
                            GroupInfoActivity.this.participantsTv.setText("Participants (" + GroupInfoActivity.this.userList.size() + ")");
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
