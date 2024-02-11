package com.example.aurtisticsv.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.blogspot.atifsoftwares.firebaseapp.AddPostActivity;
import com.blogspot.atifsoftwares.firebaseapp.PostDetailActivity;
import com.blogspot.atifsoftwares.firebaseapp.PostLikedByActivity;
import com.blogspot.atifsoftwares.firebaseapp.R;
import com.blogspot.atifsoftwares.firebaseapp.ThereProfileActivity;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends Adapter<MyHolder> {
    Context context;
    private DatabaseReference likesRef;
    boolean mProcessLike = false;
    String myUid;
    List<ModelPost> postList;
    private DatabaseReference postsRef;

    class MyHolder extends ViewHolder {
        Button commentBtn;
        Button likeBtn;
        ImageButton moreBtn;
        TextView pCommentsTv;
        TextView pDescriptionTv;
        ImageView pImageIv;
        TextView pLikesTv;
        TextView pTimeTv;
        TextView pTitleTv;
        LinearLayout profileLayout;
        Button shareBtn;
        TextView uNameTv;
        ImageView uPictureIv;

        public MyHolder(View itemView) {
            super(itemView);
            this.uPictureIv = (ImageView) itemView.findViewById(R.id.uPictureIv);
            this.pImageIv = (ImageView) itemView.findViewById(R.id.pImageIv);
            this.uNameTv = (TextView) itemView.findViewById(R.id.uNameTv);
            this.pTimeTv = (TextView) itemView.findViewById(R.id.pTimeTv);
            this.pTitleTv = (TextView) itemView.findViewById(R.id.pTitleTv);
            this.pDescriptionTv = (TextView) itemView.findViewById(R.id.pDescriptionTv);
            this.pLikesTv = (TextView) itemView.findViewById(R.id.pLikesTv);
            this.pCommentsTv = (TextView) itemView.findViewById(R.id.pCommentsTv);
            this.moreBtn = (ImageButton) itemView.findViewById(R.id.moreBtn);
            this.likeBtn = (Button) itemView.findViewById(R.id.likeBtn);
            this.commentBtn = (Button) itemView.findViewById(R.id.commentBtn);
            this.shareBtn = (Button) itemView.findViewById(R.id.shareBtn);
            this.profileLayout = (LinearLayout) itemView.findViewById(R.id.profileLayout);
        }
    }

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        this.myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        this.postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    public MyHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new MyHolder(LayoutInflater.from(this.context).inflate(R.layout.row_posts, viewGroup, false));
    }

    public void onBindViewHolder(MyHolder myHolder, int i) {
        final MyHolder myHolder2 = myHolder;
        final int i2 = i;
        final String uid = ((ModelPost) this.postList.get(i2)).getUid();
        String uEmail = ((ModelPost) this.postList.get(i2)).getuEmail();
        String uName = ((ModelPost) this.postList.get(i2)).getuName();
        String uDp = ((ModelPost) this.postList.get(i2)).getuDp();
        final String pId = ((ModelPost) this.postList.get(i2)).getpId();
        final String pTitle = ((ModelPost) this.postList.get(i2)).getpTitle();
        String pDescription = ((ModelPost) this.postList.get(i2)).getpDescr();
        String pImage = ((ModelPost) this.postList.get(i2)).getpImage();
        String pTimeStamp = ((ModelPost) this.postList.get(i2)).getpTime();
        String pLikes = ((ModelPost) this.postList.get(i2)).getpLikes();
        String pComments = ((ModelPost) this.postList.get(i2)).getpComments();
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        myHolder2.uNameTv.setText(uName);
        myHolder2.pTimeTv.setText(pTime);
        myHolder2.pTitleTv.setText(pTitle);
        myHolder2.pDescriptionTv.setText(pDescription);
        myHolder2.pLikesTv.setText(pLikes + " Likes");
        myHolder2.pCommentsTv.setText(pComments + " Comments");
        setLikes(myHolder2, pId);
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(myHolder2.uPictureIv);
        } catch (Exception e) {
        }
        if (pImage.equals("noImage")) {
            myHolder2.pImageIv.setVisibility(8);
        } else {
            myHolder2.pImageIv.setVisibility(0);
            try {
                Picasso.get().load(pImage).into(myHolder2.pImageIv);
            } catch (Exception e2) {
            }
        }
        ImageButton imageButton = myHolder2.moreBtn;
        AnonymousClass1 anonymousClass1 = r1;
        final MyHolder myHolder3 = myHolder;
        pLikes = uid;
        String pImage2 = pImage;
        pImage = pId;
        uName = pDescription;
        pDescription = pImage2;
        AnonymousClass1 anonymousClass12 = new OnClickListener() {
            public void onClick(View v) {
                AdapterPosts.this.showMoreOptions(myHolder3.moreBtn, pLikes, AdapterPosts.this.myUid, pImage, pDescription);
            }
        };
        imageButton.setOnClickListener(anonymousClass1);
        myHolder2.likeBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                final int pLikes = Integer.parseInt(((ModelPost) AdapterPosts.this.postList.get(i2)).getpLikes());
                AdapterPosts.this.mProcessLike = true;
                final String postIde = ((ModelPost) AdapterPosts.this.postList.get(i2)).getpId();
                AdapterPosts.this.likesRef.addValueEventListener(new ValueEventListener() {
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (AdapterPosts.this.mProcessLike) {
                            String str = "pLikes";
                            String str2 = "";
                            if (dataSnapshot.child(postIde).hasChild(AdapterPosts.this.myUid)) {
                                AdapterPosts.this.postsRef.child(postIde).child(str).setValue(str2 + (pLikes - 1));
                                AdapterPosts.this.likesRef.child(postIde).child(AdapterPosts.this.myUid).removeValue();
                                AdapterPosts.this.mProcessLike = false;
                                return;
                            }
                            AdapterPosts.this.postsRef.child(postIde).child(str).setValue(str2 + (pLikes + 1));
                            AdapterPosts.this.likesRef.child(postIde).child(AdapterPosts.this.myUid).setValue("Liked");
                            AdapterPosts.this.mProcessLike = false;
                            AdapterPosts.this.addToHisNotifications(str2 + uid, str2 + pId, "Liked your post");
                        }
                    }

                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });
        myHolder2.commentBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(AdapterPosts.this.context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                AdapterPosts.this.context.startActivity(intent);
            }
        });
        myHolder2.shareBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) myHolder2.pImageIv.getDrawable();
                if (bitmapDrawable == null) {
                    AdapterPosts.this.shareTextOnly(pTitle, uName);
                    return;
                }
                AdapterPosts.this.shareImageAndText(pTitle, uName, bitmapDrawable.getBitmap());
            }
        });
        myHolder2.profileLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(AdapterPosts.this.context, ThereProfileActivity.class);
                intent.putExtra("uid", uid);
                AdapterPosts.this.context.startActivity(intent);
            }
        });
        myHolder2.pLikesTv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(AdapterPosts.this.context, PostLikedByActivity.class);
                intent.putExtra("postId", pId);
                AdapterPosts.this.context.startActivity(intent);
            }
        });
    }

    private void addToHisNotifications(String hisUid, String pId, String notification) {
        String timestamp = "" + System.currentTimeMillis();
        HashMap<Object, String> hashMap = new HashMap();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", this.myUid);
        FirebaseDatabase.getInstance().getReference("Users").child(hisUid).child("Notifications").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
            }
        });
    }

    private void shareTextOnly(String pTitle, String pDescription) {
        String shareBody = pTitle + "\n" + pDescription;
        Intent sIntent = new Intent("android.intent.action.SEND");
        sIntent.setType("text/plain");
        sIntent.putExtra("android.intent.extra.SUBJECT", "Subject Here");
        sIntent.putExtra("android.intent.extra.TEXT", shareBody);
        this.context.startActivity(Intent.createChooser(sIntent, "Share Via"));
    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        String shareBody = pTitle + "\n" + pDescription;
        Uri uri = saveImageToShare(bitmap);
        Intent sIntent = new Intent("android.intent.action.SEND");
        sIntent.putExtra("android.intent.extra.STREAM", uri);
        sIntent.putExtra("android.intent.extra.TEXT", shareBody);
        sIntent.putExtra("android.intent.extra.SUBJECT", "Subject Here");
        sIntent.setType("image/png");
        this.context.startActivity(Intent.createChooser(sIntent, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(this.context.getCacheDir(), "images");
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            return FileProvider.getUriForFile(this.context, "com.blogspot.atifsoftwares.firebaseapp.fileprovider", file);
        } catch (Exception e) {
            Toast.makeText(this.context, "" + e.getMessage(), 0).show();
            return null;
        }
    }

    private void setLikes(final MyHolder holder, final String postKey) {
        this.likesRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postKey).hasChild(AdapterPosts.this.myUid)) {
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    holder.likeBtn.setText("Liked");
                    return;
                }
                holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                holder.likeBtn.setText("Like");
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {
        PopupMenu popupMenu = new PopupMenu(this.context, moreBtn, 8388613);
        if (uid.equals(myUid)) {
            popupMenu.getMenu().add(0, 0, 0, "Delete");
            popupMenu.getMenu().add(0, 1, 0, "Edit");
        }
        popupMenu.getMenu().add(0, 2, 0, "View Detail");
        popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                Intent intent;
                if (id == 0) {
                    AdapterPosts.this.beginDelete(pId, pImage);
                } else if (id == 1) {
                    intent = new Intent(AdapterPosts.this.context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    AdapterPosts.this.context.startActivity(intent);
                } else if (id == 2) {
                    intent = new Intent(AdapterPosts.this.context, PostDetailActivity.class);
                    intent.putExtra("postId", pId);
                    AdapterPosts.this.context.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        if (pImage.equals("noImage")) {
            deleteWithoutImage(pId);
        } else {
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(final String pId, String pImage) {
        final ProgressDialog pd = new ProgressDialog(this.context);
        pd.setMessage("Deleting...");
        FirebaseStorage.getInstance().getReferenceFromUrl(pImage).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId).addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(AdapterPosts.this.context, "Deleted successfully", 0).show();
                        pd.dismiss();
                    }

                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                pd.dismiss();
                Toast.makeText(AdapterPosts.this.context, "" + e.getMessage(), 0).show();
            }
        });
    }

    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(this.context);
        pd.setMessage("Deleting...");
        FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue();
                }
                Toast.makeText(AdapterPosts.this.context, "Deleted successfully", 0).show();
                pd.dismiss();
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public int getItemCount() {
        return this.postList.size();
    }
}
