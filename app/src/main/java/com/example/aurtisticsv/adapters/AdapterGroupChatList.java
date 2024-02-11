package com.example.aurtisticsv.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.blogspot.atifsoftwares.firebaseapp.GroupChatActivity;
import com.blogspot.atifsoftwares.firebaseapp.R;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelGroupChatList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChatList extends Adapter<HolderGroupChatList> {
    private Context context;
    private ArrayList<ModelGroupChatList> groupChatLists;

    class HolderGroupChatList extends ViewHolder {
        private ImageView groupIconIv;
        private TextView groupTitleTv;
        private TextView messageTv;
        private TextView nameTv;
        private TextView timeTv;

        public HolderGroupChatList(View itemView) {
            super(itemView);
            this.groupIconIv = (ImageView) itemView.findViewById(R.id.groupIconIv);
            this.groupTitleTv = (TextView) itemView.findViewById(R.id.groupTitleTv);
            this.nameTv = (TextView) itemView.findViewById(R.id.nameTv);
            this.messageTv = (TextView) itemView.findViewById(R.id.messageTv);
            this.timeTv = (TextView) itemView.findViewById(R.id.timeTv);
        }
    }

    public AdapterGroupChatList(Context context, ArrayList<ModelGroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    public HolderGroupChatList onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HolderGroupChatList(LayoutInflater.from(this.context).inflate(R.layout.row_groupchats_list, parent, false));
    }

    public void onBindViewHolder(HolderGroupChatList holder, int position) {
        ModelGroupChatList model = (ModelGroupChatList) this.groupChatLists.get(position);
        final String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();
        String str = "";
        holder.nameTv.setText(str);
        holder.timeTv.setText(str);
        holder.messageTv.setText(str);
        loadLastMessage(model, holder);
        holder.groupTitleTv.setText(groupTitle);
        try {
            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary).into(holder.groupIconIv);
        } catch (Exception e) {
            holder.groupIconIv.setImageResource(R.drawable.ic_group_primary);
        }
        holder.itemView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(AdapterGroupChatList.this.context, GroupChatActivity.class);
                intent.putExtra("groupId", groupId);
                AdapterGroupChatList.this.context.startActivity(intent);
            }
        });
    }

    private void loadLastMessage(ModelGroupChatList model, final HolderGroupChatList holder) {
        FirebaseDatabase.getInstance().getReference("Groups").child(model.getGroupId()).child("Messages").limitToLast(1).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String str = "";
                    String message = str + ds.child("message").getValue();
                    String timestamp = str + ds.child("timestamp").getValue();
                    String sender = str + ds.child("sender").getValue();
                    str = str + ds.child("type").getValue();
                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                    if (str.equals("image")) {
                        holder.messageTv.setText("Sent Photo");
                    } else {
                        holder.messageTv.setText(message);
                    }
                    holder.timeTv.setText(dateTime);
                    FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(sender).addValueEventListener(new ValueEventListener() {
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                holder.nameTv.setText("" + ds.child("name").getValue());
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

    public int getItemCount() {
        return this.groupChatLists.size();
    }
}
