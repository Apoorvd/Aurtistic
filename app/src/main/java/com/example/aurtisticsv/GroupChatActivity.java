package com.blogspot.atifsoftwares.firebaseapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.blogspot.atifsoftwares.firebaseapp.adapters.AdapterGroupChat;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelGroupChat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask.TaskSnapshot;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 2000;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int STORAGE_REQUEST_CODE = 400;
    private AdapterGroupChat adapterGroupChat;
    private ImageButton attachBtn;
    private String[] cameraPermission;
    private RecyclerView chatRv;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelGroupChat> groupChatList;
    private ImageView groupIconIv;
    private String groupId;
    private TextView groupTitleTv;
    private Uri image_uri = null;
    private EditText messageEt;
    private String myGroupRole = "";
    private ImageButton sendBtn;
    private String[] storagePermission;
    private Toolbar toolbar;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.groupIconIv = (ImageView) findViewById(R.id.groupIconIv);
        this.groupTitleTv = (TextView) findViewById(R.id.groupTitleTv);
        this.attachBtn = (ImageButton) findViewById(R.id.attachBtn);
        this.messageEt = (EditText) findViewById(R.id.messageEt);
        this.sendBtn = (ImageButton) findViewById(R.id.sendBtn);
        this.chatRv = (RecyclerView) findViewById(R.id.chatRv);
        setSupportActionBar(this.toolbar);
        this.groupId = getIntent().getStringExtra("groupId");
        String str = "android.permission.WRITE_EXTERNAL_STORAGE";
        this.cameraPermission = new String[]{"android.permission.CAMERA", str};
        this.storagePermission = new String[]{str};
        this.firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadGroupMessages();
        loadMyGroupRole();
        this.sendBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String message = GroupChatActivity.this.messageEt.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(GroupChatActivity.this, "Can't send empty message...", 0).show();
                } else {
                    GroupChatActivity.this.sendMessage(message);
                }
            }
        });
        this.attachBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                GroupChatActivity.this.showImageImportDialog();
            }
        });
    }

    private void showImageImportDialog() {
        new Builder(this).setTitle("Pick Image").setItems(new String[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (GroupChatActivity.this.checkCameraPermission()) {
                        GroupChatActivity.this.pickCamera();
                    } else {
                        GroupChatActivity.this.requestCameraPermission();
                    }
                } else if (GroupChatActivity.this.checkStoragePermission()) {
                    GroupChatActivity.this.pickGallery();
                } else {
                    GroupChatActivity.this.requestStoragePermission();
                }
            }
        }).show();
    }

    private void pickGallery() {
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", "GroupImageTitle");
        contentValues.put("description", "GroupImageDescription");
        this.image_uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra("output", this.image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, this.storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, this.cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == 0;
        boolean result1 = ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0;
        if (result && result1) {
            return true;
        }
        return false;
    }

    private void sendImageMessage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Please wait");
        pd.setMessage("Sending Image...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        FirebaseStorage.getInstance().getReference("ChatImages/" + System.currentTimeMillis()).putFile(this.image_uri).addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {
            public void onSuccess(TaskSnapshot taskSnapshot) {
                Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!p_uriTask.isSuccessful()) {
                }
                Uri p_downloadUri = (Uri) p_uriTask.getResult();
                if (p_uriTask.isSuccessful()) {
                    String str = "";
                    String timestamp = str + System.currentTimeMillis();
                    HashMap<String, Object> hashMap = new HashMap();
                    hashMap.put("sender", str + GroupChatActivity.this.firebaseAuth.getUid());
                    hashMap.put("message", str + p_downloadUri);
                    hashMap.put("timestamp", str + timestamp);
                    hashMap.put("type", "image");
                    FirebaseDatabase.getInstance().getReference("Groups").child(GroupChatActivity.this.groupId).child("Messages").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        public void onSuccess(Void aVoid) {
                            GroupChatActivity.this.messageEt.setText("");
                            pd.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        public void onFailure(Exception e) {
                            pd.dismiss();
                            Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), 0).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), 0).show();
                pd.dismiss();
            }
        });
    }

    private void loadMyGroupRole() {
        FirebaseDatabase.getInstance().getReference("Groups").child(this.groupId).child("Participants").orderByChild("uid").equalTo(this.firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    GroupChatActivity.this.myGroupRole = "" + ds.child("role").getValue();
                    GroupChatActivity.this.invalidateOptionsMenu();
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadGroupMessages() {
        this.groupChatList = new ArrayList();
        FirebaseDatabase.getInstance().getReference("Groups").child(this.groupId).child("Messages").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                GroupChatActivity.this.groupChatList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    GroupChatActivity.this.groupChatList.add((ModelGroupChat) ds.getValue(ModelGroupChat.class));
                }
                GroupChatActivity groupChatActivity = GroupChatActivity.this;
                GroupChatActivity groupChatActivity2 = GroupChatActivity.this;
                groupChatActivity.adapterGroupChat = new AdapterGroupChat(groupChatActivity2, groupChatActivity2.groupChatList);
                GroupChatActivity.this.chatRv.setAdapter(GroupChatActivity.this.adapterGroupChat);
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void sendMessage(String message) {
        String str = "";
        String timestamp = str + System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("sender", str + this.firebaseAuth.getUid());
        hashMap.put("message", str + message);
        hashMap.put("timestamp", str + timestamp);
        hashMap.put("type", "text");
        FirebaseDatabase.getInstance().getReference("Groups").child(this.groupId).child("Messages").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                GroupChatActivity.this.messageEt.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), 0).show();
            }
        });
    }

    private void loadGroupInfo() {
        FirebaseDatabase.getInstance().getReference("Groups").orderByChild("groupId").equalTo(this.groupId).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String str = "";
                    String groupTitle = str + ds.child("groupTitle").getValue();
                    String groupDescription = str + ds.child("groupDescription").getValue();
                    String groupIcon = str + ds.child("groupIcon").getValue();
                    String timestamp = str + ds.child("timestamp").getValue();
                    str = str + ds.child("createdBy").getValue();
                    GroupChatActivity.this.groupTitleTv.setText(groupTitle);
                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_white).into(GroupChatActivity.this.groupIconIv);
                    } catch (Exception e) {
                        GroupChatActivity.this.groupIconIv.setImageResource(R.drawable.ic_group_white);
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        if (this.myGroupRole.equals("creator") || this.myGroupRole.equals("admin")) {
            menu.findItem(R.id.action_add_participant).setVisible(true);
        } else {
            menu.findItem(R.id.action_add_participant).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String str = "groupId";
        Intent intent;
        if (id == R.id.action_add_participant) {
            intent = new Intent(this, GroupParticipantAddActivity.class);
            intent.putExtra(str, this.groupId);
            startActivity(intent);
        } else if (id == R.id.action_groupinfo) {
            intent = new Intent(this, GroupInfoActivity.class);
            intent.putExtra(str, this.groupId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                this.image_uri = data.getData();
                sendImageMessage();
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                sendImageMessage();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean writeStorageAccepted = true;
        if (requestCode != CAMERA_REQUEST_CODE) {
            if (requestCode == STORAGE_REQUEST_CODE && grantResults.length > 0) {
                if (grantResults[0] != 0) {
                    writeStorageAccepted = false;
                }
                if (writeStorageAccepted) {
                    pickGallery();
                } else {
                    Toast.makeText(this, "Storage permission required...", 0).show();
                }
            }
        } else if (grantResults.length > 0) {
            boolean cameraAccepted = grantResults[0] == 0;
            if (grantResults[1] != 0) {
                writeStorageAccepted = false;
            }
            if (cameraAccepted && writeStorageAccepted) {
                pickCamera();
            } else {
                Toast.makeText(this, "Camera & Storage permissions are required...", 0).show();
            }
        }
    }
}
