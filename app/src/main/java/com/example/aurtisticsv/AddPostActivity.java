package com.example.aurtisticsv;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class AddPostActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int STORAGE_REQUEST_CODE = 200;
    ActionBar actionBar;
    String[] cameraPermissions;
    EditText descriptionEt;
    String dp;
    String editDescription;
    String editImage;
    String editTitle;
    String email;
    FirebaseAuth firebaseAuth;
    ImageView imageIv;
    Uri image_rui = null;
    String name;
    ProgressDialog pd;
    String[] storagePermissions;
    EditText titleEt;
    String uid;
    Button uploadBtn;
    DatabaseReference userDbRef;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        ActionBar supportActionBar = getSupportActionBar();
        this.actionBar = supportActionBar;
        String str = "Add New Post";
        supportActionBar.setTitle(str);
        this.actionBar.setDisplayShowHomeEnabled(true);
        this.actionBar.setDisplayHomeAsUpEnabled(true);
        String str2 = "android.permission.WRITE_EXTERNAL_STORAGE";
        this.cameraPermissions = new String[]{"android.permission.CAMERA", str2};
        this.storagePermissions = new String[]{str2};
        this.pd = new ProgressDialog(this);
        this.firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();
        this.titleEt = (EditText) findViewById(R.id.pTitleEt);
        this.descriptionEt = (EditText) findViewById(R.id.pDescriptionEt);
        this.imageIv = (ImageView) findViewById(R.id.pImageIv);
        this.uploadBtn = (Button) findViewById(R.id.pUploadBtn);
        Intent intent = getIntent();
        str2 = intent.getAction();
        String type = intent.getType();
        if ("android.intent.action.SEND".equals(str2) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent);
            } else if (type.startsWith("image")) {
                handleSendImage(intent);
            }
        }
        String str3 = "";
        final String isUpdateKey = str3 + intent.getStringExtra("key");
        str3 = str3 + intent.getStringExtra("editPostId");
        if (isUpdateKey.equals("editPost")) {
            this.actionBar.setTitle("Update Post");
            this.uploadBtn.setText("Update");
            loadPostData(str3);
        } else {
            this.actionBar.setTitle(str);
            this.uploadBtn.setText("Upload");
        }
        this.actionBar.setSubtitle(this.email);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        this.userDbRef = reference;
        reference.orderByChild("email").equalTo(this.email).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String str = "";
                    AddPostActivity.this.name = str + ds.child("name").getValue();
                    AddPostActivity.this.email = str + ds.child("email").getValue();
                    AddPostActivity.this.dp = str + ds.child("image").getValue();
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
        this.imageIv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AddPostActivity.this.showImagePickDialog();
            }
        });
        this.uploadBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String title = AddPostActivity.this.titleEt.getText().toString().trim();
                String description = AddPostActivity.this.descriptionEt.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(AddPostActivity.this, "Enter title...", 0).show();
                } else if (TextUtils.isEmpty(description)) {
                    Toast.makeText(AddPostActivity.this, "Enter description...", 0).show();
                } else {
                    if (isUpdateKey.equals("editPost")) {
                        AddPostActivity.this.beginUpdate(title, description, str3);
                    } else {
                        AddPostActivity.this.uploadData(title, description);
                    }
                }
            }
        });
    }

    private void handleSendImage(Intent intent) {
        Uri imageURI = (Uri) intent.getParcelableExtra("android.intent.extra.STREAM");
        if (imageURI != null) {
            this.image_rui = imageURI;
            this.imageIv.setImageURI(imageURI);
        }
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra("android.intent.extra.TEXT");
        if (sharedText != null) {
            this.descriptionEt.setText(sharedText);
        }
    }

    private void beginUpdate(String title, String description, String editPostId) {
        this.pd.setMessage("Updating Post...");
        this.pd.show();
        if (!this.editImage.equals("noImage")) {
            updateWasWithImage(title, description, editPostId);
        } else if (this.imageIv.getDrawable() != null) {
            updateWithNowImage(title, description, editPostId);
        } else {
            updateWithoutImage(title, description, editPostId);
        }
    }

    private void updateWithoutImage(String title, String description, String editPostId) {
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("uid", this.uid);
        hashMap.put("uName", this.name);
        hashMap.put("uEmail", this.email);
        hashMap.put("uDp", this.dp);
        hashMap.put("pTitle", title);
        hashMap.put("pDescr", description);
        hashMap.put("pImage", "noImage");
        FirebaseDatabase.getInstance().getReference("Posts").child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                AddPostActivity.this.pd.dismiss();
                Toast.makeText(AddPostActivity.this, "Updated...", 0).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                AddPostActivity.this.pd.dismiss();
                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), 0).show();
            }
        });
    }

    private void updateWithNowImage(final String title, final String description, final String editPostId) {
        String filePathAndName = "Posts/post_" + String.valueOf(System.currentTimeMillis());
        Bitmap bitmap = ((BitmapDrawable) this.imageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, baos);
        FirebaseStorage.getInstance().getReference().child(filePathAndName).putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {
            public void onSuccess(TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) {
                }
                String downloadUri = ((Uri) uriTask.getResult()).toString();
                if (uriTask.isSuccessful()) {
                    HashMap<String, Object> hashMap = new HashMap();
                    hashMap.put("uid", AddPostActivity.this.uid);
                    hashMap.put("uName", AddPostActivity.this.name);
                    hashMap.put("uEmail", AddPostActivity.this.email);
                    hashMap.put("uDp", AddPostActivity.this.dp);
                    hashMap.put("pTitle", title);
                    hashMap.put("pDescr", description);
                    hashMap.put("pImage", downloadUri);
                    FirebaseDatabase.getInstance().getReference("Posts").child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        public void onSuccess(Void aVoid) {
                            AddPostActivity.this.pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "Updated...", 0).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        public void onFailure(Exception e) {
                            AddPostActivity.this.pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), 0).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                AddPostActivity.this.pd.dismiss();
                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), 0).show();
            }
        });
    }

    private void updateWasWithImage(final String title, final String description, final String editPostId) {
        FirebaseStorage.getInstance().getReferenceFromUrl(this.editImage).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                String filePathAndName = "Posts/post_" + String.valueOf(System.currentTimeMillis());
                Bitmap bitmap = ((BitmapDrawable) AddPostActivity.this.imageIv.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(CompressFormat.PNG, 100, baos);
                FirebaseStorage.getInstance().getReference().child(filePathAndName).putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {
                    public void onSuccess(TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) {
                        }
                        String downloadUri = ((Uri) uriTask.getResult()).toString();
                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> hashMap = new HashMap();
                            hashMap.put("uid", AddPostActivity.this.uid);
                            hashMap.put("uName", AddPostActivity.this.name);
                            hashMap.put("uEmail", AddPostActivity.this.email);
                            hashMap.put("uDp", AddPostActivity.this.dp);
                            hashMap.put("pTitle", title);
                            hashMap.put("pDescr", description);
                            hashMap.put("pImage", downloadUri);
                            FirebaseDatabase.getInstance().getReference("Posts").child(editPostId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                public void onSuccess(Void aVoid) {
                                    AddPostActivity.this.pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Updated...", 0).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                public void onFailure(Exception e) {
                                    AddPostActivity.this.pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), 0).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    public void onFailure(Exception e) {
                        AddPostActivity.this.pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), 0).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                AddPostActivity.this.pd.dismiss();
                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), 0).show();
            }
        });
    }

    private void loadPostData(String editPostId) {
        FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(editPostId).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String str = "";
                    AddPostActivity.this.editTitle = str + ds.child("pTitle").getValue();
                    AddPostActivity.this.editDescription = str + ds.child("pDescr").getValue();
                    AddPostActivity.this.editImage = str + ds.child("pImage").getValue();
                    AddPostActivity.this.titleEt.setText(AddPostActivity.this.editTitle);
                    AddPostActivity.this.descriptionEt.setText(AddPostActivity.this.editDescription);
                    if (!AddPostActivity.this.editImage.equals("noImage")) {
                        try {
                            Picasso.get().load(AddPostActivity.this.editImage).into(AddPostActivity.this.imageIv);
                        } catch (Exception e) {
                        }
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void uploadData(final String title, final String description) {
        this.pd.setMessage("Publishing post...");
        this.pd.show();
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/post_" + timeStamp;
        if (this.imageIv.getDrawable() != null) {
            Bitmap bitmap = ((BitmapDrawable) this.imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.PNG, 100, baos);
            FirebaseStorage.getInstance().getReference().child(filePathAndName).putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {
                public void onSuccess(TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) {
                    }
                    String downloadUri = ((Uri) uriTask.getResult()).toString();
                    if (uriTask.isSuccessful()) {
                        HashMap<Object, String> hashMap = new HashMap();
                        hashMap.put("uid", AddPostActivity.this.uid);
                        hashMap.put("uName", AddPostActivity.this.name);
                        hashMap.put("uEmail", AddPostActivity.this.email);
                        hashMap.put("uDp", AddPostActivity.this.dp);
                        hashMap.put("pId", timeStamp);
                        hashMap.put("pTitle", title);
                        hashMap.put("pDescr", description);
                        hashMap.put("pImage", downloadUri);
                        hashMap.put("pTime", timeStamp);
                        String str = "0";
                        hashMap.put("pLikes", str);
                        hashMap.put("pComments", str);
                        FirebaseDatabase.getInstance().getReference("Posts").child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            public void onSuccess(Void aVoid) {
                                AddPostActivity.this.pd.dismiss();
                                Toast.makeText(AddPostActivity.this, "Post published", 0).show();
                                String str = "";
                                AddPostActivity.this.titleEt.setText(str);
                                AddPostActivity.this.descriptionEt.setText(str);
                                AddPostActivity.this.imageIv.setImageURI(null);
                                AddPostActivity.this.image_rui = null;
                                AddPostActivity.this.prepareNotification(str + timeStamp, str + AddPostActivity.this.name + " added new post", str + title + "\n" + description, "PostNotification", "POST");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            public void onFailure(Exception e) {
                                AddPostActivity.this.pd.dismiss();
                                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), 0).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                public void onFailure(Exception e) {
                    AddPostActivity.this.pd.dismiss();
                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), 0).show();
                }
            });
            return;
        }
        HashMap<Object, String> hashMap = new HashMap();
        hashMap.put("uid", this.uid);
        hashMap.put("uName", this.name);
        hashMap.put("uEmail", this.email);
        hashMap.put("uDp", this.dp);
        hashMap.put("pId", timeStamp);
        hashMap.put("pTitle", title);
        hashMap.put("pDescr", description);
        hashMap.put("pImage", "noImage");
        hashMap.put("pTime", timeStamp);
        String str = "0";
        hashMap.put("pLikes", str);
        hashMap.put("pComments", str);
        FirebaseDatabase.getInstance().getReference("Posts").child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                AddPostActivity.this.pd.dismiss();
                Toast.makeText(AddPostActivity.this, "Post published", 0).show();
                String str = "";
                AddPostActivity.this.titleEt.setText(str);
                AddPostActivity.this.descriptionEt.setText(str);
                AddPostActivity.this.imageIv.setImageURI(null);
                AddPostActivity.this.image_rui = null;
                AddPostActivity.this.prepareNotification(str + timeStamp, str + AddPostActivity.this.name + " added new post", str + title + "\n" + description, "PostNotification", "POST");
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                AddPostActivity.this.pd.dismiss();
                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), 0).show();
            }
        });
    }

    private void prepareNotification(String pId, String title, String description, String notificationType, String notificationTopic) {
        String NOTIFICATION_TOPIC = "/topics/" + notificationTopic;
        String NOTIFICATION_TITLE = title;
        String NOTIFICATION_MESSAGE = description;
        String NOTIFICATION_TYPE = notificationType;
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("sender", this.uid);
            notificationBodyJo.put("pId", pId);
            notificationBodyJo.put("pTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("pDescription", NOTIFICATION_MESSAGE);
            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo);
        } catch (JSONException e) {
            Toast.makeText(this, "" + e.getMessage(), 0).show();
        }
        sendPostNotification(notificationJo);
    }

    private void sendPostNotification(JSONObject notificationJo) {
        Volley.newRequestQueue(this).add(new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
                Log.d("FCM_RESPONSE", "onResponse: " + response.toString());
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AddPostActivity.this, "" + error.toString(), 0).show();
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAA7AuJGz8:APA91bGwtgymO7JGkhboQEjJPR7wdIzOYA4ZeCU0th6udSCABz8VnPcfcwlh8R7hSrYBzX1QQcP8To55cwcRSjIj0YttTGVaaXP2e8u18QGbluxclRlIFBwlExiwqk9AkHPt6cLegJkt");
                return headers;
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
                    if (AddPostActivity.this.checkCameraPermission()) {
                        AddPostActivity.this.pickFromCamera();
                    } else {
                        AddPostActivity.this.requestCameraPermission();
                    }
                }
                if (which != 1) {
                    return;
                }
                if (AddPostActivity.this.checkStoragePermission()) {
                    AddPostActivity.this.pickFromGallery();
                } else {
                    AddPostActivity.this.requestStoragePermission();
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

    /* Access modifiers changed, original: protected */
    public void onStart() {
        super.onStart();
        checkUserStatus();
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus() {
        FirebaseUser user = this.firebaseAuth.getCurrentUser();
        if (user != null) {
            this.email = user.getEmail();
            this.uid = user.getUid();
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
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            this.firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
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
                this.imageIv.setImageURI(data2);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                this.imageIv.setImageURI(this.image_rui);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
