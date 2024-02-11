package com.example.aurtisticsv.adapters;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.blogspot.atifsoftwares.firebaseapp.R;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;

public class AdapterParticipantAdd extends Adapter<HolderParticipantAdd> {
    private Context context;
    private String groupId;
    private String myGroupRole;
    private ArrayList<ModelUser> userList;

    class HolderParticipantAdd extends ViewHolder {
        private ImageView avatarIv;
        private TextView emailTv;
        private TextView nameTv;
        private TextView statusTv;

        public HolderParticipantAdd(View itemView) {
            super(itemView);
            this.avatarIv = (ImageView) itemView.findViewById(R.id.avatarIv);
            this.nameTv = (TextView) itemView.findViewById(R.id.nameTv);
            this.emailTv = (TextView) itemView.findViewById(R.id.emailTv);
            this.statusTv = (TextView) itemView.findViewById(R.id.statusTv);
        }
    }

    public AdapterParticipantAdd(Context context, ArrayList<ModelUser> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    public HolderParticipantAdd onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HolderParticipantAdd(LayoutInflater.from(this.context).inflate(R.layout.row_participant_add, parent, false));
    }

    public void onBindViewHolder(HolderParticipantAdd holder, int position) {
        final ModelUser modelUser = (ModelUser) this.userList.get(position);
        String name = modelUser.getName();
        String email = modelUser.getEmail();
        String image = modelUser.getImage();
        final String uid = modelUser.getUid();
        holder.nameTv.setText(name);
        holder.emailTv.setText(email);
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
        } catch (Exception e) {
            holder.avatarIv.setImageResource(R.drawable.ic_default_img);
        }
        checkIfAlreadyExists(modelUser, holder);
        holder.itemView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("Groups").child(AdapterParticipantAdd.this.groupId).child("Participants").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String str;
                        if (dataSnapshot.exists()) {
                            String hisPreviousRole = "" + dataSnapshot.child("role").getValue();
                            Builder builder = new Builder(AdapterParticipantAdd.this.context);
                            builder.setTitle("Choose Option");
                            str = "creator";
                            String str2 = "Make Admin";
                            String str3 = "Remove Admin";
                            String str4 = "participant";
                            String str5 = "admin";
                            String str6 = "Remove User";
                            if (AdapterParticipantAdd.this.myGroupRole.equals(str)) {
                                if (hisPreviousRole.equals(str5)) {
                                    builder.setItems(new String[]{str3, str6}, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                AdapterParticipantAdd.this.removeAdmin(modelUser);
                                            } else {
                                                AdapterParticipantAdd.this.removeParticipant(modelUser);
                                            }
                                        }
                                    }).show();
                                    return;
                                } else if (hisPreviousRole.equals(str4)) {
                                    builder.setItems(new String[]{str2, str6}, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                AdapterParticipantAdd.this.makeAdmin(modelUser);
                                            } else {
                                                AdapterParticipantAdd.this.removeParticipant(modelUser);
                                            }
                                        }
                                    }).show();
                                    return;
                                } else {
                                    return;
                                }
                            } else if (!AdapterParticipantAdd.this.myGroupRole.equals(str5)) {
                                return;
                            } else {
                                if (hisPreviousRole.equals(str)) {
                                    Toast.makeText(AdapterParticipantAdd.this.context, "Creator of Group...", 0).show();
                                    return;
                                } else if (hisPreviousRole.equals(str5)) {
                                    builder.setItems(new String[]{str3, str6}, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                AdapterParticipantAdd.this.removeAdmin(modelUser);
                                            } else {
                                                AdapterParticipantAdd.this.removeParticipant(modelUser);
                                            }
                                        }
                                    }).show();
                                    return;
                                } else if (hisPreviousRole.equals(str4)) {
                                    builder.setItems(new String[]{str2, str6}, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                AdapterParticipantAdd.this.makeAdmin(modelUser);
                                            } else {
                                                AdapterParticipantAdd.this.removeParticipant(modelUser);
                                            }
                                        }
                                    }).show();
                                    return;
                                } else {
                                    return;
                                }
                            }
                        }
                        str = "CANCEL";
                        new Builder(AdapterParticipantAdd.this.context).setTitle("Add Participant").setMessage("Add this user in this group?").setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                AdapterParticipantAdd.this.addParticipant(modelUser);
                            }
                        }).setNegativeButton(str, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                    }

                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });
    }

    private void addParticipant(ModelUser modelUser) {
        String str = "";
        String timestamp = str + System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap();
        hashMap.put("uid", modelUser.getUid());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", str + timestamp);
        FirebaseDatabase.getInstance().getReference("Groups").child(this.groupId).child("Participants").child(modelUser.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                Toast.makeText(AdapterParticipantAdd.this.context, "Added successfully...", 0).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                Toast.makeText(AdapterParticipantAdd.this.context, "" + e.getMessage(), 0).show();
            }
        });
    }

    private void makeAdmin(ModelUser modelUser) {
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("role", "admin");
        FirebaseDatabase.getInstance().getReference("Groups").child(this.groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                Toast.makeText(AdapterParticipantAdd.this.context, "The user is now admin...", 0).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                Toast.makeText(AdapterParticipantAdd.this.context, "" + e.getMessage(), 0).show();
            }
        });
    }

    private void removeParticipant(ModelUser modelUser) {
        FirebaseDatabase.getInstance().getReference("Groups").child(this.groupId).child("Participants").child(modelUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
            }
        });
    }

    private void removeAdmin(ModelUser modelUser) {
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("role", "participant");
        FirebaseDatabase.getInstance().getReference("Groups").child(this.groupId).child("Participants").child(modelUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                Toast.makeText(AdapterParticipantAdd.this.context, "The user is no longer admin...", 0).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                Toast.makeText(AdapterParticipantAdd.this.context, "" + e.getMessage(), 0).show();
            }
        });
    }

    private void checkIfAlreadyExists(ModelUser modelUser, final HolderParticipantAdd holder) {
        FirebaseDatabase.getInstance().getReference("Groups").child(this.groupId).child("Participants").child(modelUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                String str = "";
                if (dataSnapshot.exists()) {
                    holder.statusTv.setText(str + dataSnapshot.child("role").getValue());
                    return;
                }
                holder.statusTv.setText(str);
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public int getItemCount() {
        return this.userList.size();
    }
}
