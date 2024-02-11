package com.blogspot.atifsoftwares.firebaseapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.firebaseapp.adapters.AdapterChat;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelChat;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelUser;
import com.blogspot.atifsoftwares.firebaseapp.notifications.Data;
import com.blogspot.atifsoftwares.firebaseapp.notifications.Sender;
import com.blogspot.atifsoftwares.firebaseapp.notifications.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask.TaskSnapshot;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int STORAGE_REQUEST_CODE = 200;
    AdapterChat adapterChat;
    ImageButton attachBtn;
    ImageView blockIv;
    String[] cameraPermissions;
    List<ModelChat> chatList;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    String hisImage;
    String hisUid;
    Uri image_rui = null;
    boolean isBlocked = false;
    EditText messageEt;
    String myUid;
    TextView nameTv;
    private boolean notify = false;
    ImageView profileIv;
    RecyclerView recyclerView;
    private RequestQueue requestQueue;
    ValueEventListener seenListener;
    ImageButton sendBtn;
    String[] storagePermissions;
    Toolbar toolbar;
    DatabaseReference userRefForSeen;
    TextView userStatusTv;
    DatabaseReference usersDbRef;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        this.recyclerView = (RecyclerView) findViewById(R.id.chat_recyclerView);
        this.profileIv = (ImageView) findViewById(R.id.proifleIv);
        this.blockIv = (ImageView) findViewById(R.id.blockIv);
        this.nameTv = (TextView) findViewById(R.id.nameTv);
        this.userStatusTv = (TextView) findViewById(R.id.userStatusTv);
        this.messageEt = (EditText) findViewById(R.id.messageEt);
        this.sendBtn = (ImageButton) findViewById(R.id.sendBtn);
        this.attachBtn = (ImageButton) findViewById(R.id.attachBtn);
        String str = "android.permission.WRITE_EXTERNAL_STORAGE";
        this.cameraPermissions = new String[]{"android.permission.CAMERA", str};
        this.storagePermissions = new String[]{str};
        this.requestQueue = Volley.newRequestQueue(getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setLayoutManager(linearLayoutManager);
        this.hisUid = getIntent().getStringExtra("hisUid");
        this.firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase instance = FirebaseDatabase.getInstance();
        this.firebaseDatabase = instance;
        DatabaseReference reference = instance.getReference("Users");
        this.usersDbRef = reference;
        reference.orderByChild("uid").equalTo(this.hisUid).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String str = "";
                    String name = str + ds.child("name").getValue();
                    ChatActivity.this.hisImage = str + ds.child("image").getValue();
                    if ((str + ds.child("typingTo").getValue()).equals(ChatActivity.this.myUid)) {
                        ChatActivity.this.userStatusTv.setText("typing...");
                    } else {
                        str = str + ds.child("onlineStatus").getValue();
                        if (str.equals("online")) {
                            ChatActivity.this.userStatusTv.setText(str);
                        } else {
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(str));
                            ChatActivity.this.userStatusTv.setText("Last seen at: " + DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString());
                        }
                    }
                    ChatActivity.this.nameTv.setText(name);
                    try {
                        Picasso.get().load(ChatActivity.this.hisImage).placeholder(R.drawable.ic_default_img_white).into(ChatActivity.this.profileIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img_white).into(ChatActivity.this.profileIv);
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
        this.sendBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ChatActivity.this.notify = true;
                String message = ChatActivity.this.messageEt.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(ChatActivity.this, "Cannot send the empty message...", 0).show();
                } else {
                    ChatActivity.this.sendMessage(message);
                }
                ChatActivity.this.messageEt.setText("");
            }
        });
        this.attachBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ChatActivity.this.showImagePickDialog();
            }
        });
        this.messageEt.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    ChatActivity.this.checkTypingStatus("noOne");
                    return;
                }
                ChatActivity chatActivity = ChatActivity.this;
                chatActivity.checkTypingStatus(chatActivity.hisUid);
            }

            public void afterTextChanged(Editable s) {
            }
        });
        this.blockIv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ChatActivity.this.isBlocked) {
                    ChatActivity.this.unBlockUser();
                } else {
                    ChatActivity.this.blockUser();
                }
            }
        });
        readMessages();
        checkIsBlocked();
        seenMessage();
    }

    private void checkIsBlocked() {
        FirebaseDatabase.getInstance().getReference("Users").child(this.firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(this.hisUid).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.exists()) {
                        ChatActivity.this.blockIv.setImageResource(R.drawable.ic_blocked_red);
                        ChatActivity.this.isBlocked = true;
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void blockUser() {
        HashMap<String, String> hashMap = new HashMap();
        hashMap.put("uid", this.hisUid);
        FirebaseDatabase.getInstance().getReference("Users").child(this.myUid).child("BlockedUsers").child(this.hisUid).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                Toast.makeText(ChatActivity.this, "Blocked Successfully...", 0).show();
                ChatActivity.this.blockIv.setImageResource(R.drawable.ic_blocked_red);
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                Toast.makeText(ChatActivity.this, "Failed: " + e.getMessage(), 0).show();
            }
        });
    }

    private void unBlockUser() {
        FirebaseDatabase.getInstance().getReference("Users").child(this.myUid).child("BlockedUsers").orderByChild("uid").equalTo(this.hisUid).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.exists()) {
                        ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ChatActivity.this, "Unbloked Successfully...", 0).show();
                                ChatActivity.this.blockIv.setImageResource(R.drawable.ic_unblocked_green);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            public void onFailure(Exception e) {
                                Toast.makeText(ChatActivity.this, "Failed: " + e.getMessage(), 0).show();
                            }
                        });
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void showImagePickDialog() {
        String[] options = new String[]{"Camera", "Gallery"};
        Builder builder = new Builder(this);
        builder.setTitle("Choose Image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (ChatActivity.this.checkCameraPermission()) {
                        ChatActivity.this.pickFromCamera();
                    } else {
                        ChatActivity.this.requestCameraPermission();
                    }
                }
                if (which != 1) {
                    return;
                }
                if (ChatActivity.this.checkStoragePermission()) {
                    ChatActivity.this.pickFromGallery();
                } else {
                    ChatActivity.this.requestStoragePermission();
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put("title", "Temp Pick");
        cv.put("description", "Temp Descr");
        this.image_rui = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, cv);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra("output", this.image_rui);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, this.storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == 0;
        boolean result1 = ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0;
        if (result && result1) {
            return true;
        }
        return false;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, this.cameraPermissions, 100);
    }

    private void seenMessage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        this.userRefForSeen = reference;
        this.seenListener = reference.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = (ModelChat) ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(ChatActivity.this.myUid) && chat.getSender().equals(ChatActivity.this.hisUid)) {
                        HashMap<String, Object> hasSeenHashMap = new HashMap();
                        hasSeenHashMap.put("isSeen", Boolean.valueOf(true));
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void readMessages() {
        this.chatList = new ArrayList();
        FirebaseDatabase.getInstance().getReference("Chats").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatActivity.this.chatList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = (ModelChat) ds.getValue(ModelChat.class);
                    if ((chat.getReceiver().equals(ChatActivity.this.myUid) && chat.getSender().equals(ChatActivity.this.hisUid)) || (chat.getReceiver().equals(ChatActivity.this.hisUid) && chat.getSender().equals(ChatActivity.this.myUid))) {
                        ChatActivity.this.chatList.add(chat);
                    }
                    ChatActivity chatActivity = ChatActivity.this;
                    ChatActivity chatActivity2 = ChatActivity.this;
                    chatActivity.adapterChat = new AdapterChat(chatActivity2, chatActivity2.chatList, ChatActivity.this.hisImage);
                    ChatActivity.this.adapterChat.notifyDataSetChanged();
                    ChatActivity.this.recyclerView.setAdapter(ChatActivity.this.adapterChat);
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void sendMessage(final String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("sender", this.myUid);
        hashMap.put("receiver", this.hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", Boolean.valueOf(false));
        hashMap.put("type", "text");
        databaseReference.child("Chats").push().setValue(hashMap);
        FirebaseDatabase.getInstance().getReference("Users").child(this.myUid).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                ModelUser user = (ModelUser) dataSnapshot.getValue(ModelUser.class);
                if (ChatActivity.this.notify) {
                    ChatActivity chatActivity = ChatActivity.this;
                    chatActivity.senNotification(chatActivity.hisUid, user.getName(), message);
                }
                ChatActivity.this.notify = false;
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
        String str = "Chatlist";
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference(str).child(this.myUid).child(this.hisUid);
        chatRef1.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef1.child("id").setValue(ChatActivity.this.hisUid);
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference(str).child(this.hisUid).child(this.myUid);
        chatRef2.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef2.child("id").setValue(ChatActivity.this.myUid);
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void sendImageMessage(Uri image_rui) throws IOException {
        this.notify = true;
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending image...");
        progressDialog.show();
        final String timeStamp = "" + System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/post_" + timeStamp;
        Bitmap bitmap = Media.getBitmap(getContentResolver(), image_rui);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, baos);
        FirebaseStorage.getInstance().getReference().child(fileNameAndPath).putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {
            public void onSuccess(TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) {
                }
                String downloadUri = ((Uri) uriTask.getResult()).toString();
                if (uriTask.isSuccessful()) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    HashMap<String, Object> hashMap = new HashMap();
                    hashMap.put("sender", ChatActivity.this.myUid);
                    hashMap.put("receiver", ChatActivity.this.hisUid);
                    hashMap.put("message", downloadUri);
                    hashMap.put("timestamp", timeStamp);
                    hashMap.put("type", "image");
                    hashMap.put("isSeen", Boolean.valueOf(false));
                    databaseReference.child("Chats").push().setValue(hashMap);
                    FirebaseDatabase.getInstance().getReference("Users").child(ChatActivity.this.myUid).addValueEventListener(new ValueEventListener() {
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ModelUser user = (ModelUser) dataSnapshot.getValue(ModelUser.class);
                            if (ChatActivity.this.notify) {
                                ChatActivity.this.senNotification(ChatActivity.this.hisUid, user.getName(), "Sent you a photo...");
                            }
                            ChatActivity.this.notify = false;
                        }

                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    String str = "Chatlist";
                    final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference(str).child(ChatActivity.this.myUid).child(ChatActivity.this.hisUid);
                    chatRef1.addValueEventListener(new ValueEventListener() {
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                chatRef1.child("id").setValue(ChatActivity.this.hisUid);
                            }
                        }

                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference(str).child(ChatActivity.this.hisUid).child(ChatActivity.this.myUid);
                    chatRef2.addValueEventListener(new ValueEventListener() {
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                chatRef2.child("id").setValue(ChatActivity.this.myUid);
                            }
                        }

                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                progressDialog.dismiss();
            }
        });
    }

    private void senNotification(final String hisUid, final String name, final String message) {
        FirebaseDatabase.getInstance().getReference("Tokens").orderByKey().equalTo(hisUid).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Token token = (Token) ds.getValue(Token.class);
                    String str = "";
                    String str2 = str + ChatActivity.this.myUid;
                    String str3 = str + name + ": " + message;
                    Sender sender = new Sender(new Data(str2, str3, "New Message", str + hisUid, "ChatNotification", Integer.valueOf(R.drawable.ic_default_img)), token.getToken());
                    try {
                        ChatActivity.this.requestQueue.add(new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", new JSONObject(new Gson().toJson(sender)), new Listener<JSONObject>() {
                            public void onResponse(JSONObject response) {
                                Log.d("JSON_RESPONSE", "onResponse: " + response.toString());
                            }
                        }, new ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: " + error.toString());
                            }
                        }) {
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAA7AuJGz8:APA91bGwtgymO7JGkhboQEjJPR7wdIzOYA4ZeCU0th6udSCABz8VnPcfcwlh8R7hSrYBzX1QQcP8To55cwcRSjIj0YttTGVaaXP2e8u18QGbluxclRlIFBwlExiwqk9AkHPt6cLegJkt");
                                return headers;
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = this.firebaseAuth.getCurrentUser();
        if (user != null) {
            this.myUid = user.getUid();
            return;
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(this.myUid);
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("onlineStatus", status);
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(this.myUid);
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("typingTo", typing);
        dbRef.updateChildren(hashMap);
    }

    /* Access modifiers changed, original: protected */
    public void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        super.onPause();
        checkOnlineStatus(String.valueOf(System.currentTimeMillis()));
        checkTypingStatus("noOne");
        this.userRefForSeen.removeEventListener(this.seenListener);
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean storageAccepted = true;
        if (requestCode != 100) {
            if (requestCode == STORAGE_REQUEST_CODE && grantResults.length > 0) {
                if (grantResults[0] != 0) {
                    storageAccepted = false;
                }
                if (storageAccepted) {
                    pickFromGallery();
                } else {
                    Toast.makeText(this, "Storage permissions necessary...", 0).show();
                }
            }
        } else if (grantResults.length > 0) {
            boolean cameraAccepted = grantResults[0] == 0;
            if (grantResults[1] != 0) {
                storageAccepted = false;
            }
            if (cameraAccepted && storageAccepted) {
                pickFromCamera();
            } else {
                Toast.makeText(this, "Camera & Storage both permissions are necessary...", 0).show();
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                Uri data2 = data.getData();
                this.image_rui = data2;
                try {
                    sendImageMessage(data2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                try {
                    sendImageMessage(this.image_rui);
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            this.firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
