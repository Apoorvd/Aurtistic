package com.blogspot.atifsoftwares.firebaseapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask.TaskSnapshot;
import java.util.HashMap;

public class GroupCreateActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int STORAGE_REQUEST_CODE = 200;
    private ActionBar actionBar;
    private String[] cameraPermissions;
    private FloatingActionButton createGroupBtn;
    private FirebaseAuth firebaseAuth;
    private EditText groupDescriptionEt;
    private ImageView groupIconIv;
    private EditText groupTitleEt;
    private Uri image_uri = null;
    private ProgressDialog progressDialog;
    private String[] storagePermissions;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);
        ActionBar supportActionBar = getSupportActionBar();
        this.actionBar = supportActionBar;
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        this.actionBar.setDisplayShowHomeEnabled(true);
        this.actionBar.setTitle("Create Group");
        this.groupIconIv = (ImageView) findViewById(R.id.groupIconIv);
        this.groupTitleEt = (EditText) findViewById(R.id.groupTitleEt);
        this.groupDescriptionEt = (EditText) findViewById(R.id.groupDescriptionEt);
        this.createGroupBtn = (FloatingActionButton) findViewById(R.id.createGroupBtn);
        String str = "android.permission.WRITE_EXTERNAL_STORAGE";
        this.cameraPermissions = new String[]{"android.permission.CAMERA", str};
        this.storagePermissions = new String[]{str};
        this.firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        this.groupIconIv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                GroupCreateActivity.this.showImagePickDialog();
            }
        });
        this.createGroupBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                GroupCreateActivity.this.startCreatingGroup();
            }
        });
    }

    private void startCreatingGroup() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.progressDialog = progressDialog;
        progressDialog.setMessage("Creating Group");
        final String groupTitle = this.groupTitleEt.getText().toString().trim();
        final String groupDescription = this.groupDescriptionEt.getText().toString().trim();
        if (TextUtils.isEmpty(groupTitle)) {
            Toast.makeText(this, "Please enter group title...", 0).show();
            return;
        }
        this.progressDialog.show();
        String str = "";
        final String g_timestamp = str + System.currentTimeMillis();
        if (this.image_uri == null) {
            createGroup(str + g_timestamp, str + groupTitle, str + groupDescription, str);
        } else {
            FirebaseStorage.getInstance().getReference("Group_Imgs/image" + g_timestamp).putFile(this.image_uri).addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {
                public void onSuccess(TaskSnapshot taskSnapshot) {
                    Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!p_uriTask.isSuccessful()) {
                    }
                    Uri p_downloadUri = (Uri) p_uriTask.getResult();
                    if (p_uriTask.isSuccessful()) {
                        String str = "";
                        GroupCreateActivity.this.createGroup(str + g_timestamp, str + groupTitle, str + groupDescription, str + p_downloadUri);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                public void onFailure(Exception e) {
                    GroupCreateActivity.this.progressDialog.dismiss();
                    Toast.makeText(GroupCreateActivity.this, "" + e.getMessage(), 0).show();
                }
            });
        }
    }

    private void createGroup(final String g_timestamp, String groupTitle, String groupDescription, String groupIcon) {
        HashMap<String, String> hashMap = new HashMap();
        String str = "";
        hashMap.put("groupId", str + g_timestamp);
        hashMap.put("groupTitle", str + groupTitle);
        hashMap.put("groupDescription", str + groupDescription);
        hashMap.put("groupIcon", str + groupIcon);
        hashMap.put("timestamp", str + g_timestamp);
        hashMap.put("createdBy", str + this.firebaseAuth.getUid());
        FirebaseDatabase.getInstance().getReference("Groups").child(g_timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                HashMap<String, String> hashMap1 = new HashMap();
                hashMap1.put("uid", GroupCreateActivity.this.firebaseAuth.getUid());
                hashMap1.put("role", "creator");
                hashMap1.put("timestamp", g_timestamp);
                FirebaseDatabase.getInstance().getReference("Groups").child(g_timestamp).child("Participants").child(GroupCreateActivity.this.firebaseAuth.getUid()).setValue(hashMap1).addOnSuccessListener(new OnSuccessListener<Void>() {
                    public void onSuccess(Void aVoid) {
                        GroupCreateActivity.this.progressDialog.dismiss();
                        Toast.makeText(GroupCreateActivity.this, "Group created...", 0).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    public void onFailure(Exception e) {
                        GroupCreateActivity.this.progressDialog.dismiss();
                        Toast.makeText(GroupCreateActivity.this, "" + e.getMessage(), 0).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                GroupCreateActivity.this.progressDialog.dismiss();
                Toast.makeText(GroupCreateActivity.this, "" + e.getMessage(), 0).show();
            }
        });
    }

    private void showImagePickDialog() {
        new Builder(this).setTitle("Pick Image:").setItems(new String[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (GroupCreateActivity.this.checkCameraPermissions()) {
                        GroupCreateActivity.this.pickFromCamera();
                    } else {
                        GroupCreateActivity.this.requestCameraPermissions();
                    }
                } else if (GroupCreateActivity.this.checkStoragePermissions()) {
                    GroupCreateActivity.this.pickFromGallery();
                } else {
                    GroupCreateActivity.this.requestStoragePermissions();
                }
            }
        }).show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put("title", "Group Image Icon Title");
        cv.put("description", "Group Image Icon Description");
        this.image_uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, cv);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra("output", this.image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermissions() {
        return ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0;
    }

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, this.storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions() {
        boolean result = ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == 0;
        boolean result1 = ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0;
        if (result && result1) {
            return true;
        }
        return false;
    }

    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, this.cameraPermissions, 100);
    }

    private void checkUser() {
        FirebaseUser user = this.firebaseAuth.getCurrentUser();
        if (user != null) {
            this.actionBar.setSubtitle(user.getEmail());
        }
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean storageAccepted = true;
        if (requestCode != 100) {
            if (requestCode == STORAGE_REQUEST_CODE && grantResults.length > 0) {
                if (grantResults[0] != 0) {
                    storageAccepted = false;
                }
                if (storageAccepted) {
                    pickFromGallery();
                } else {
                    Toast.makeText(this, "Storage permissions required", 0).show();
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
                Toast.makeText(this, "Camera & Storage permissions are required", 0).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                Uri data2 = data.getData();
                this.image_uri = data2;
                this.groupIconIv.setImageURI(data2);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                this.groupIconIv.setImageURI(this.image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
