package com.example.aurtisticsv.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.blogspot.atifsoftwares.firebaseapp.ChatActivity;
import com.blogspot.atifsoftwares.firebaseapp.R;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelUser;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends Adapter<MyHolder> {
    Context context;
    private HashMap<String, String> lastMessageMap = new HashMap();
    List<ModelUser> userList;

    class MyHolder extends ViewHolder {
        TextView lastMessageTv;
        TextView nameTv;
        ImageView onlineStatusIv;
        ImageView profileIv;

        public MyHolder(View itemView) {
            super(itemView);
            this.profileIv = (ImageView) itemView.findViewById(R.id.profileIv);
            this.onlineStatusIv = (ImageView) itemView.findViewById(R.id.onlineStatusIv);
            this.nameTv = (TextView) itemView.findViewById(R.id.nameTv);
            this.lastMessageTv = (TextView) itemView.findViewById(R.id.lastMessageTv);
        }
    }

    public AdapterChatlist(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    public MyHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new MyHolder(LayoutInflater.from(this.context).inflate(R.layout.row_chatlist, viewGroup, false));
    }

    public void onBindViewHolder(MyHolder myHolder, int i) {
        final String hisUid = ((ModelUser) this.userList.get(i)).getUid();
        String userImage = ((ModelUser) this.userList.get(i)).getImage();
        String lastMessage = (String) this.lastMessageMap.get(hisUid);
        myHolder.nameTv.setText(((ModelUser) this.userList.get(i)).getName());
        if (lastMessage == null || lastMessage.equals("default")) {
            myHolder.lastMessageTv.setVisibility(8);
        } else {
            myHolder.lastMessageTv.setVisibility(0);
            myHolder.lastMessageTv.setText(lastMessage);
        }
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img).into(myHolder.profileIv);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.ic_default_img).into(myHolder.profileIv);
        }
        if (((ModelUser) this.userList.get(i)).getOnlineStatus().equals("online")) {
            myHolder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        } else {
            myHolder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }
        myHolder.itemView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(AdapterChatlist.this.context, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                AdapterChatlist.this.context.startActivity(intent);
            }
        });
    }

    public void setLastMessageMap(String userId, String lastMessage) {
        this.lastMessageMap.put(userId, lastMessage);
    }

    public int getItemCount() {
        return this.userList.size();
    }
}
