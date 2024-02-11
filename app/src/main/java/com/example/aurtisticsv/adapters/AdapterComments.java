package com.example.aurtisticsv.adapters;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
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
import com.blogspot.atifsoftwares.firebaseapp.models.ModelComment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends Adapter<MyHolder> {
    List<ModelComment> commentList;
    Context context;
    String myUid;
    String postId;

    class MyHolder extends ViewHolder {
        ImageView avatarIv;
        TextView commentTv;
        TextView nameTv;
        TextView timeTv;

        public MyHolder(View itemView) {
            super(itemView);
            this.avatarIv = (ImageView) itemView.findViewById(R.id.avatarIv);
            this.nameTv = (TextView) itemView.findViewById(R.id.nameTv);
            this.commentTv = (TextView) itemView.findViewById(R.id.commentTv);
            this.timeTv = (TextView) itemView.findViewById(R.id.timeTv);
        }
    }

    public AdapterComments(Context context, List<ModelComment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    public MyHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new MyHolder(LayoutInflater.from(this.context).inflate(R.layout.row_comments, viewGroup, false));
    }

    public void onBindViewHolder(MyHolder myHolder, int i) {
        final String uid = ((ModelComment) this.commentList.get(i)).getUid();
        String name = ((ModelComment) this.commentList.get(i)).getuName();
        String email = ((ModelComment) this.commentList.get(i)).getuEmail();
        String image = ((ModelComment) this.commentList.get(i)).getuDp();
        final String cid = ((ModelComment) this.commentList.get(i)).getcId();
        String comment = ((ModelComment) this.commentList.get(i)).getComment();
        String timestamp = ((ModelComment) this.commentList.get(i)).getTimestamp();
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        myHolder.nameTv.setText(name);
        myHolder.commentTv.setText(comment);
        myHolder.timeTv.setText(pTime);
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(myHolder.avatarIv);
        } catch (Exception e) {
        }
        myHolder.itemView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (AdapterComments.this.myUid.equals(uid)) {
                    Builder builder = new Builder(v.getRootView().getContext());
                    String str = "Delete";
                    builder.setTitle(str);
                    builder.setMessage("Are you sure to delete this comment?");
                    builder.setPositiveButton(str, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AdapterComments.this.deleteComment(cid);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    return;
                }
                Toast.makeText(AdapterComments.this.context, "Can't delete other's comment...", 0).show();
            }
        });
    }

    private void deleteComment(String cid) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(this.postId);
        ref.child("Comments").child(cid).removeValue();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                String str = "";
                String str2 = "pComments";
                ref.child(str2).setValue(str + (Integer.parseInt(str + dataSnapshot.child(str2).getValue()) - 1));
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public int getItemCount() {
        return this.commentList.size();
    }
}
