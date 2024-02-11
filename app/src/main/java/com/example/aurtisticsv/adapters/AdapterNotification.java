package com.example.aurtisticsv.adapters;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.blogspot.atifsoftwares.firebaseapp.PostDetailActivity;
import com.blogspot.atifsoftwares.firebaseapp.R;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelNotification;
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

public class AdapterNotification extends Adapter<HolderNotification> {
    private Context context;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private ArrayList<ModelNotification> notificationsList;

    class HolderNotification extends ViewHolder {
        ImageView avatarIv;
        TextView nameTv;
        TextView notificationTv;
        TextView timeTv;

        public HolderNotification(View itemView) {
            super(itemView);
            this.avatarIv = (ImageView) itemView.findViewById(R.id.avatarIv);
            this.nameTv = (TextView) itemView.findViewById(R.id.nameTv);
            this.notificationTv = (TextView) itemView.findViewById(R.id.notificationTv);
            this.timeTv = (TextView) itemView.findViewById(R.id.timeTv);
        }
    }

    public AdapterNotification(Context context, ArrayList<ModelNotification> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;
    }

    public HolderNotification onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HolderNotification(LayoutInflater.from(this.context).inflate(R.layout.row_notification, parent, false));
    }

    public void onBindViewHolder(final HolderNotification holder, int position) {
        final ModelNotification model = (ModelNotification) this.notificationsList.get(position);
        String name = model.getsName();
        String notification = model.getNotification();
        String image = model.getsImage();
        final String timestamp = model.getTimestamp();
        String senderUid = model.getsUid();
        final String pId = model.getpId();
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(senderUid).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String str = "";
                    String name = str + ds.child("name").getValue();
                    String image = str + ds.child("image").getValue();
                    str = str + ds.child("email").getValue();
                    model.setsName(name);
                    model.setsEmail(str);
                    model.setsImage(image);
                    holder.nameTv.setText(name);
                    try {
                        Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
                    } catch (Exception e) {
                        holder.avatarIv.setImageResource(R.drawable.ic_default_img);
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
        holder.notificationTv.setText(notification);
        holder.timeTv.setText(pTime);
        holder.itemView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(AdapterNotification.this.context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                AdapterNotification.this.context.startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                Builder builder = new Builder(AdapterNotification.this.context);
                String str = "Delete";
                builder.setTitle(str);
                builder.setMessage("Are you sure to delete this notification?");
                builder.setPositiveButton(str, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseDatabase.getInstance().getReference("Users").child(AdapterNotification.this.firebaseAuth.getUid()).child("Notifications").child(timestamp).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(AdapterNotification.this.context, "Notification deleted...", 0).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            public void onFailure(Exception e) {
                                Toast.makeText(AdapterNotification.this.context, "" + e.getMessage(), 0).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });
    }

    public int getItemCount() {
        return this.notificationsList.size();
    }
}
