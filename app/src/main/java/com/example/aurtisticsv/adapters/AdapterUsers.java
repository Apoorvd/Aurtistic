package com.example.aurtisticsv.adapters;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.blogspot.atifsoftwares.firebaseapp.ChatActivity;
import com.blogspot.atifsoftwares.firebaseapp.R;
import com.blogspot.atifsoftwares.firebaseapp.ThereProfileActivity;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.List;

public class AdapterUsers extends Adapter<MyHolder> {
    Context context;
    FirebaseAuth firebaseAuth;
    String myUid;
    List<ModelUser> userList;

    class MyHolder extends ViewHolder {
        ImageView blockIv;
        ImageView mAvatarIv;
        TextView mEmailTv;
        TextView mNameTv;

        public MyHolder(View itemView) {
            super(itemView);
            this.mAvatarIv = (ImageView) itemView.findViewById(R.id.avatarIv);
            this.blockIv = (ImageView) itemView.findViewById(R.id.blockIv);
            this.mNameTv = (TextView) itemView.findViewById(R.id.nameTv);
            this.mEmailTv = (TextView) itemView.findViewById(R.id.emailTv);
        }
    }

    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        FirebaseAuth instance = FirebaseAuth.getInstance();
        this.firebaseAuth = instance;
        this.myUid = instance.getUid();
    }

    public MyHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new MyHolder(LayoutInflater.from(this.context).inflate(R.layout.row_users, viewGroup, false));
    }

    public void onBindViewHolder(MyHolder myHolder, final int i) {
        final String hisUID = ((ModelUser) this.userList.get(i)).getUid();
        String userImage = ((ModelUser) this.userList.get(i)).getImage();
        String userName = ((ModelUser) this.userList.get(i)).getName();
        String userEmail = ((ModelUser) this.userList.get(i)).getEmail();
        myHolder.mNameTv.setText(userName);
        myHolder.mEmailTv.setText(userEmail);
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img).into(myHolder.mAvatarIv);
        } catch (Exception e) {
        }
        myHolder.blockIv.setImageResource(R.drawable.ic_unblocked_green);
        checkIsBlocked(hisUID, myHolder, i);
        myHolder.itemView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Builder builder = new Builder(AdapterUsers.this.context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(AdapterUsers.this.context, ThereProfileActivity.class);
                            intent.putExtra("uid", hisUID);
                            AdapterUsers.this.context.startActivity(intent);
                        }
                        if (which == 1) {
                            AdapterUsers.this.imBlockedORNot(hisUID);
                        }
                    }
                });
                builder.create().show();
            }
        });
        myHolder.blockIv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (((ModelUser) AdapterUsers.this.userList.get(i)).isBlocked()) {
                    AdapterUsers.this.unBlockUser(hisUID);
                } else {
                    AdapterUsers.this.blockUser(hisUID);
                }
            }
        });
    }

    private void imBlockedORNot(final String hisUID) {
        FirebaseDatabase.getInstance().getReference("Users").child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(this.myUid).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.exists()) {
                        Toast.makeText(AdapterUsers.this.context, "You're blocked by that user, can't send message", 0).show();
                        return;
                    }
                }
                Intent intent = new Intent(AdapterUsers.this.context, ChatActivity.class);
                intent.putExtra("hisUid", hisUID);
                AdapterUsers.this.context.startActivity(intent);
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void checkIsBlocked(String hisUID, final MyHolder myHolder, final int i) {
        FirebaseDatabase.getInstance().getReference("Users").child(this.myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.exists()) {
                        myHolder.blockIv.setImageResource(R.drawable.ic_blocked_red);
                        ((ModelUser) AdapterUsers.this.userList.get(i)).setBlocked(true);
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void blockUser(String hisUID) {
        HashMap<String, String> hashMap = new HashMap();
        hashMap.put("uid", hisUID);
        FirebaseDatabase.getInstance().getReference("Users").child(this.myUid).child("BlockedUsers").child(hisUID).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                Toast.makeText(AdapterUsers.this.context, "Blocked Successfully...", 0).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                Toast.makeText(AdapterUsers.this.context, "Failed: " + e.getMessage(), 0).show();
            }
        });
    }

    private void unBlockUser(String hisUID) {
        FirebaseDatabase.getInstance().getReference("Users").child(this.myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.exists()) {
                        ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(AdapterUsers.this.context, "Unbloked Successfully...", 0).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            public void onFailure(Exception e) {
                                Toast.makeText(AdapterUsers.this.context, "Failed: " + e.getMessage(), 0).show();
                            }
                        });
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public int getItemCount() {
        return this.userList.size();
    }
}
