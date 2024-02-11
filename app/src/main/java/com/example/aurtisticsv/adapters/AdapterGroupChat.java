package com.example.aurtisticsv.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.blogspot.atifsoftwares.firebaseapp.R;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelGroupChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChat extends Adapter<HolderGroupChat> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_right = 1;
    private Context context;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private ArrayList<ModelGroupChat> modelGroupChatList;

    class HolderGroupChat extends ViewHolder {
        private ImageView messageIv;
        private TextView messageTv;
        private TextView nameTv;
        private TextView timeTv;

        public HolderGroupChat(View itemView) {
            super(itemView);
            this.nameTv = (TextView) itemView.findViewById(R.id.nameTv);
            this.messageTv = (TextView) itemView.findViewById(R.id.messageTv);
            this.timeTv = (TextView) itemView.findViewById(R.id.timeTv);
            this.messageIv = (ImageView) itemView.findViewById(R.id.messageIv);
        }
    }

    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> modelGroupChatList) {
        this.context = context;
        this.modelGroupChatList = modelGroupChatList;
    }

    public HolderGroupChat onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new HolderGroupChat(LayoutInflater.from(this.context).inflate(R.layout.row_groupchat_right, parent, false));
        }
        return new HolderGroupChat(LayoutInflater.from(this.context).inflate(R.layout.row_groupchat_left, parent, false));
    }

    public void onBindViewHolder(HolderGroupChat holder, int position) {
        ModelGroupChat model = (ModelGroupChat) this.modelGroupChatList.get(position);
        String timestamp = model.getTimestamp();
        String message = model.getMessage();
        String senderUid = model.getSender();
        String messageType = model.getType();
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
        if (messageType.equals("text")) {
            holder.messageIv.setVisibility(8);
            holder.messageTv.setVisibility(0);
            holder.messageTv.setText(message);
        } else {
            holder.messageIv.setVisibility(0);
            holder.messageTv.setVisibility(8);
            try {
                Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);
            } catch (Exception e) {
                holder.messageIv.setImageResource(R.drawable.ic_image_black);
            }
        }
        holder.timeTv.setText(dateTime);
        setUserName(model, holder);
    }

    private void setUserName(ModelGroupChat model, final HolderGroupChat holder) {
        FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(model.getSender()).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    holder.nameTv.setText("" + ds.child("name").getValue());
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public int getItemCount() {
        return this.modelGroupChatList.size();
    }

    public int getItemViewType(int position) {
        if (((ModelGroupChat) this.modelGroupChatList.get(position)).getSender().equals(this.firebaseAuth.getUid())) {
            return 1;
        }
        return 0;
    }
}
