package com.blogspot.atifsoftwares.firebaseapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blogspot.atifsoftwares.firebaseapp.adapters.AdapterComments;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {
    AdapterComments adapterComments;
    ImageView cAvatarIv;
    EditText commentEt;
    List<ModelComment> commentList;
    String hisDp;
    String hisName;
    String hisUid;
    Button likeBtn;
    boolean mProcessComment = false;
    boolean mProcessLike = false;
    ImageButton moreBtn;
    String myDp;
    String myEmail;
    String myName;
    String myUid;
    TextView pCommentsTv;
    TextView pDescriptionTv;
    String pImage;
    ImageView pImageIv;
    String pLikes;
    TextView pLikesTv;
    TextView pTimeTiv;
    TextView pTitleTv;
    ProgressDialog pd;
    String postId;
    LinearLayout profileLayout;
    RecyclerView recyclerView;
    ImageButton sendBtn;
    Button shareBtn;
    TextView uNameTv;
    ImageView uPictureIv;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        this.postId = getIntent().getStringExtra("postId");
        this.uPictureIv = (ImageView) findViewById(R.id.uPictureIv);
        this.pImageIv = (ImageView) findViewById(R.id.pImageIv);
        this.uNameTv = (TextView) findViewById(R.id.uNameTv);
        this.pTimeTiv = (TextView) findViewById(R.id.pTimeTv);
        this.pTitleTv = (TextView) findViewById(R.id.pTitleTv);
        this.pDescriptionTv = (TextView) findViewById(R.id.pDescriptionTv);
        this.pLikesTv = (TextView) findViewById(R.id.pLikesTv);
        this.pCommentsTv = (TextView) findViewById(R.id.pCommentsTv);
        this.moreBtn = (ImageButton) findViewById(R.id.moreBtn);
        this.likeBtn = (Button) findViewById(R.id.likeBtn);
        this.shareBtn = (Button) findViewById(R.id.shareBtn);
        this.profileLayout = (LinearLayout) findViewById(R.id.profileLayout);
        this.recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        this.commentEt = (EditText) findViewById(R.id.commentEt);
        this.sendBtn = (ImageButton) findViewById(R.id.sendBtn);
        this.cAvatarIv = (ImageView) findViewById(R.id.cAvatarIv);
        loadPostInfo();
        checkUserStatus();
        loadUserInfo();
        setLikes();
        actionBar.setSubtitle("SignedIn as: " + this.myEmail);
        loadComments();
        this.sendBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PostDetailActivity.this.postComment();
            }
        });
        this.likeBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PostDetailActivity.this.likePost();
            }
        });
        this.moreBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PostDetailActivity.this.showMoreOptions();
            }
        });
        this.shareBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String pTitle = PostDetailActivity.this.pTitleTv.getText().toString().trim();
                String pDescription = PostDetailActivity.this.pDescriptionTv.getText().toString().trim();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) PostDetailActivity.this.pImageIv.getDrawable();
                if (bitmapDrawable == null) {
                    PostDetailActivity.this.shareTextOnly(pTitle, pDescription);
                    return;
                }
                PostDetailActivity.this.shareImageAndText(pTitle, pDescription, bitmapDrawable.getBitmap());
            }
        });
        this.pLikesTv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(PostDetailActivity.this, PostLikedByActivity.class);
                intent.putExtra("postId", PostDetailActivity.this.postId);
                PostDetailActivity.this.startActivity(intent);
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
        startActivity(Intent.createChooser(sIntent, "Share Via"));
    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        String shareBody = pTitle + "\n" + pDescription;
        Uri uri = saveImageToShare(bitmap);
        Intent sIntent = new Intent("android.intent.action.SEND");
        sIntent.putExtra("android.intent.extra.STREAM", uri);
        sIntent.putExtra("android.intent.extra.TEXT", shareBody);
        sIntent.putExtra("android.intent.extra.SUBJECT", "Subject Here");
        sIntent.setType("image/png");
        startActivity(Intent.createChooser(sIntent, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            return FileProvider.getUriForFile(this, "com.blogspot.atifsoftwares.firebaseapp.fileprovider", file);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), 0).show();
            return null;
        }
    }

    private void loadComments() {
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        this.commentList = new ArrayList();
        FirebaseDatabase.getInstance().getReference("Posts").child(this.postId).child("Comments").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                PostDetailActivity.this.commentList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    PostDetailActivity.this.commentList.add((ModelComment) ds.getValue(ModelComment.class));
                    PostDetailActivity.this.adapterComments = new AdapterComments(PostDetailActivity.this.getApplicationContext(), PostDetailActivity.this.commentList, PostDetailActivity.this.myUid, PostDetailActivity.this.postId);
                    PostDetailActivity.this.recyclerView.setAdapter(PostDetailActivity.this.adapterComments);
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, this.moreBtn, 8388613);
        if (this.hisUid.equals(this.myUid)) {
            popupMenu.getMenu().add(0, 0, 0, "Delete");
            popupMenu.getMenu().add(0, 1, 0, "Edit");
        }
        popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == 0) {
                    PostDetailActivity.this.beginDelete();
                } else if (id == 1) {
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", PostDetailActivity.this.postId);
                    PostDetailActivity.this.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete() {
        if (this.pImage.equals("noImage")) {
            deleteWithoutImage();
        } else {
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");
        FirebaseStorage.getInstance().getReferenceFromUrl(this.pImage).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(PostDetailActivity.this.postId).addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(PostDetailActivity.this, "Deleted successfully", 0).show();
                        pd.dismiss();
                    }

                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), 0).show();
            }
        });
    }

    private void deleteWithoutImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");
        FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(this.postId).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue();
                }
                Toast.makeText(PostDetailActivity.this, "Deleted successfully", 0).show();
                pd.dismiss();
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setLikes() {
        FirebaseDatabase.getInstance().getReference().child("Likes").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(PostDetailActivity.this.postId).hasChild(PostDetailActivity.this.myUid)) {
                    PostDetailActivity.this.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    PostDetailActivity.this.likeBtn.setText("Liked");
                    return;
                }
                PostDetailActivity.this.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                PostDetailActivity.this.likeBtn.setText("Like");
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void likePost() {
        this.mProcessLike = true;
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (PostDetailActivity.this.mProcessLike) {
                    String str = "pLikes";
                    String str2 = "";
                    if (dataSnapshot.child(PostDetailActivity.this.postId).hasChild(PostDetailActivity.this.myUid)) {
                        postsRef.child(PostDetailActivity.this.postId).child(str).setValue(str2 + (Integer.parseInt(PostDetailActivity.this.pLikes) - 1));
                        likesRef.child(PostDetailActivity.this.postId).child(PostDetailActivity.this.myUid).removeValue();
                        PostDetailActivity.this.mProcessLike = false;
                        return;
                    }
                    postsRef.child(PostDetailActivity.this.postId).child(str).setValue(str2 + (Integer.parseInt(PostDetailActivity.this.pLikes) + 1));
                    likesRef.child(PostDetailActivity.this.postId).child(PostDetailActivity.this.myUid).setValue("Liked");
                    PostDetailActivity.this.mProcessLike = false;
                    PostDetailActivity.this.addToHisNotifications(str2 + PostDetailActivity.this.hisUid, str2 + PostDetailActivity.this.postId, "Liked your post");
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void postComment() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.pd = progressDialog;
        progressDialog.setMessage("Adding comment...");
        String comment = this.commentEt.getText().toString().trim();
        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(this, "Comment is empty...", 0).show();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(this.postId).child("Comments");
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", this.myUid);
        hashMap.put("uEmail", this.myEmail);
        hashMap.put("uDp", this.myDp);
        hashMap.put("uName", this.myName);
        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                PostDetailActivity.this.pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "Comment Added...", 0).show();
                String str = "";
                PostDetailActivity.this.commentEt.setText(str);
                PostDetailActivity.this.updateCommentCount();
                PostDetailActivity.this.addToHisNotifications(str + PostDetailActivity.this.hisUid, str + PostDetailActivity.this.postId, "Commented on your post");
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                PostDetailActivity.this.pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), 0).show();
            }
        });
    }

    private void updateCommentCount() {
        this.mProcessComment = true;
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(this.postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (PostDetailActivity.this.mProcessComment) {
                    String str = "";
                    String str2 = "pComments";
                    ref.child(str2).setValue(str + (Integer.parseInt(str + dataSnapshot.child(str2).getValue()) + 1));
                    PostDetailActivity.this.mProcessComment = false;
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadUserInfo() {
        FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(this.myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String str = "";
                    PostDetailActivity.this.myName = str + ds.child("name").getValue();
                    PostDetailActivity.this.myDp = str + ds.child("image").getValue();
                    try {
                        Picasso.get().load(PostDetailActivity.this.myDp).placeholder(R.drawable.ic_default_img).into(PostDetailActivity.this.cAvatarIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img).into(PostDetailActivity.this.cAvatarIv);
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadPostInfo() {
        FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(this.postId).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String str = "";
                    String pTitle = str + ds.child("pTitle").getValue();
                    String pDescr = str + ds.child("pDescr").getValue();
                    PostDetailActivity.this.pLikes = str + ds.child("pLikes").getValue();
                    String pTimeStamp = str + ds.child("pTime").getValue();
                    PostDetailActivity.this.pImage = str + ds.child("pImage").getValue();
                    PostDetailActivity.this.hisDp = str + ds.child("uDp").getValue();
                    PostDetailActivity.this.hisUid = str + ds.child("uid").getValue();
                    String uEmail = str + ds.child("uEmail").getValue();
                    PostDetailActivity.this.hisName = str + ds.child("uName").getValue();
                    str = str + ds.child("pComments").getValue();
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                    PostDetailActivity.this.pTitleTv.setText(pTitle);
                    PostDetailActivity.this.pDescriptionTv.setText(pDescr);
                    PostDetailActivity.this.pLikesTv.setText(PostDetailActivity.this.pLikes + "Likes");
                    PostDetailActivity.this.pTimeTiv.setText(pTime);
                    PostDetailActivity.this.pCommentsTv.setText(str + " Comments");
                    PostDetailActivity.this.uNameTv.setText(PostDetailActivity.this.hisName);
                    if (PostDetailActivity.this.pImage.equals("noImage")) {
                        PostDetailActivity.this.pImageIv.setVisibility(8);
                    } else {
                        PostDetailActivity.this.pImageIv.setVisibility(0);
                        try {
                            Picasso.get().load(PostDetailActivity.this.pImage).into(PostDetailActivity.this.pImageIv);
                        } catch (Exception e) {
                        }
                    }
                    try {
                        Picasso.get().load(PostDetailActivity.this.hisDp).placeholder(R.drawable.ic_default_img).into(PostDetailActivity.this.uPictureIv);
                    } catch (Exception e2) {
                        Picasso.get().load(R.drawable.ic_default_img).into(PostDetailActivity.this.uPictureIv);
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            this.myEmail = user.getEmail();
            this.myUid = user.getUid();
            return;
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
