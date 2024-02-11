package com.example.aurtisticsv.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.blogspot.atifsoftwares.firebaseapp.R;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends Adapter<MyHolder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    List<ModelChat> chatList;
    Context context;
    FirebaseUser fUser;
    String imageUrl;

    class MyHolder extends ViewHolder {
        TextView isSeenTv;
        ImageView messageIv;
        LinearLayout messageLAyout;
        TextView messageTv;
        ImageView profileIv;
        TextView timeTv;

        public MyHolder(View itemView) {
            super(itemView);
            this.profileIv = (ImageView) itemView.findViewById(R.id.profileIv);
            this.messageIv = (ImageView) itemView.findViewById(R.id.messageIv);
            this.messageTv = (TextView) itemView.findViewById(R.id.messageTv);
            this.timeTv = (TextView) itemView.findViewById(R.id.timeTv);
            this.isSeenTv = (TextView) itemView.findViewById(R.id.isSeenTv);
            this.messageLAyout = (LinearLayout) itemView.findViewById(R.id.messageLayout);
        }
    }

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    public MyHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == 1) {
            return new MyHolder(LayoutInflater.from(this.context).inflate(R.layout.row_chat_right, viewGroup, false));
        }
        return new MyHolder(LayoutInflater.from(this.context).inflate(R.layout.row_chat_left, viewGroup, false));
    }

    public void onBindViewHolder(MyHolder myHolder, final int i) {
        String message = ((ModelChat) this.chatList.get(i)).getMessage();
        String timeStamp = ((ModelChat) this.chatList.get(i)).getTimestamp();
        String type = ((ModelChat) this.chatList.get(i)).getType();
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
        if (type.equals("text")) {
            myHolder.messageTv.setVisibility(0);
            myHolder.messageIv.setVisibility(8);
            myHolder.messageTv.setText(message);
        } else {
            myHolder.messageTv.setVisibility(8);
            myHolder.messageIv.setVisibility(0);
            Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(myHolder.messageIv);
        }
        myHolder.messageTv.setText(message);
        myHolder.timeTv.setText(dateTime);
        try {
            Picasso.get().load(this.imageUrl).into(myHolder.profileIv);
        } catch (Exception e) {
        }
        myHolder.messageLAyout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Builder builder = new Builder(AdapterChat.this.context);
                String str = "Delete";
                builder.setTitle(str);
                builder.setMessage("Are you sure to delete this message?");
                builder.setPositiveButton(str, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AdapterChat.this.deleteMessage(i);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        if (i != this.chatList.size() - 1) {
            myHolder.isSeenTv.setVisibility(8);
        } else if (((ModelChat) this.chatList.get(i)).isSeen()) {
            myHolder.isSeenTv.setText("Seen");
        } else {
            myHolder.isSeenTv.setText("Delivered");
        }
    }

    private void deleteMessage(int position) {
        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("Chats").orderByChild("timestamp").equalTo(((ModelChat) this.chatList.get(position)).getTimestamp()).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("sender").getValue().equals(myUID)) {
                        HashMap<String, Object> hashMap = new HashMap();
                        hashMap.put("message", "This message was deleted...");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(AdapterChat.this.context, "message deleted...", 0).show();
                    } else {
                        Toast.makeText(AdapterChat.this.context, "You can delete only your messages...", 0).show();
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public int getItemCount() {
        return this.chatList.size();
    }

    public int getItemViewType(int position) {
        this.fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (((ModelChat) this.chatList.get(position)).getSender().equals(this.fUser.getUid())) {
            return 1;
        }
        return 0;
    }
}
