package com.blogspot.atifsoftwares.firebaseapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.text.format.DateFormat;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask.TaskSnapshot;
import com.squareup.picasso.Picasso;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class GroupEditActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int STORAGE_REQUEST_CODE = 200;
    private ActionBar actionBar;
    private String[] cameraPermissions;
    private FirebaseAuth firebaseAuth;
    private EditText groupDescriptionEt;
    private ImageView groupIconIv;
    private String groupId;
    private EditText groupTitleEt;
    private Uri image_uri = null;
    private ProgressDialog progressDialog;
    private String[] storagePermissions;
    private FloatingActionButton updateGroupBtn;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);
        ActionBar supportActionBar = getSupportActionBar();
        this.actionBar = supportActionBar;
        supportActionBar.setTitle("Edit Group");
        this.actionBar.setDisplayHomeAsUpEnabled(true);
        this.actionBar.setDisplayShowHomeEnabled(true);
        this.groupIconIv = (ImageView) findViewById(R.id.groupIconIv);
        this.groupTitleEt = (EditText) findViewById(R.id.groupTitleEt);
        this.groupDescriptionEt = (EditText) findViewById(R.id.groupDescriptionEt);
        this.updateGroupBtn = (FloatingActionButton) findViewById(R.id.updateGroupBtn);
        this.groupId = getIntent().getStringExtra("groupId");
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.progressDialog = progressDialog;
        progressDialog.setTitle("Please wait");
        this.progressDialog.setCanceledOnTouchOutside(false);
        String str = "android.permission.WRITE_EXTERNAL_STORAGE";
        this.cameraPermissions = new String[]{"android.permission.CAMERA", str};
        this.storagePermissions = new String[]{str};
        this.firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadGroupInfo();
        this.groupIconIv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                GroupEditActivity.this.showImagePickDialog();
            }
        });
        this.updateGroupBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                GroupEditActivity.this.startUpdatingGroup();
            }
        });
    }

    private void startUpdatingGroup() {
        final String groupTitle = this.groupTitleEt.getText().toString().trim();
        final String groupDescription = this.groupDescriptionEt.getText().toString().trim();
        if (TextUtils.isEmpty(groupTitle)) {
            Toast.makeText(this, "Group title is required...", 0).show();
            return;
        }
        this.progressDialog.setMessage("Updating Group Info...");
        this.progressDialog.show();
        if (this.image_uri == null) {
            HashMap<String, Object> hashMap = new HashMap();
            hashMap.put("groupTitle", groupTitle);
            hashMap.put("groupDescription", groupDescription);
            FirebaseDatabase.getInstance().getReference("Groups").child(this.groupId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                public void onSuccess(Void aVoid) {
                    GroupEditActivity.this.progressDialog.dismiss();
                    Toast.makeText(GroupEditActivity.this, "Group info updated...", 0).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                public void onFailure(Exception e) {
                    GroupEditActivity.this.progressDialog.dismiss();
                    Toast.makeText(GroupEditActivity.this, "" + e.getMessage(), 0).show();
                }
            });
        } else {
            FirebaseStorage.getInstance().getReference("Group_Imgs/image_" + ("" + System.currentTimeMillis())).putFile(this.image_uri).addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {
                public void onSuccess(TaskSnapshot taskSnapshot) {
                    Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!p_uriTask.isSuccessful()) {
                    }
                    Uri p_downloadUri = (Uri) p_uriTask.getResult();
                    if (p_uriTask.isSuccessful()) {
                        HashMap<String, Object> hashMap = new HashMap();
                        hashMap.put("groupTitle", groupTitle);
                        hashMap.put("groupDescription", groupDescription);
                        hashMap.put("groupIcon", "" + p_downloadUri);
                        FirebaseDatabase.getInstance().getReference("Groups").child(GroupEditActivity.this.groupId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            public void onSuccess(Void aVoid) {
                                GroupEditActivity.this.progressDialog.dismiss();
                                Toast.makeText(GroupEditActivity.this, "Group info updated...", 0).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            public void onFailure(Exception e) {
                                GroupEditActivity.this.progressDialog.dismiss();
                                Toast.makeText(GroupEditActivity.this, "" + e.getMessage(), 0).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                public void onFailure(Exception e) {
                    GroupEditActivity.this.progressDialog.dismiss();
                    Toast.makeText(GroupEditActivity.this, "" + e.getMessage(), 0).show();
                }
            });
        }
    }

    private void loadGroupInfo() {
        FirebaseDatabase.getInstance().getReference("Groups").orderByChild("groupId").equalTo(this.groupId).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String str = "";
                    String groupId = str + ds.child("groupId").getValue();
                    String groupTitle = str + ds.child("groupTitle").getValue();
                    String groupDescription = str + ds.child("groupDescription").getValue();
                    String groupIcon = str + ds.child("groupIcon").getValue();
                    String createdBy = str + ds.child("createdBy").getValue();
                    str = str + ds.child("timestamp").getValue();
                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(str));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                    GroupEditActivity.this.groupTitleEt.setText(groupTitle);
                    GroupEditActivity.this.groupDescriptionEt.setText(groupDescription);
                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary).into(GroupEditActivity.this.groupIconIv);
                    } catch (Exception e) {
                        GroupEditActivity.this.groupIconIv.setImageResource(R.drawable.ic_group_primary);
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void showImagePickDialog() {
        new Builder(this).setTitle("Pick Image:").setItems(new String[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (GroupEditActivity.this.checkCameraPermissions()) {
                        GroupEditActivity.this.pickFromCamera();
                    } else {
                        GroupEditActivity.this.requestCameraPermissions();
                    }
                } else if (GroupEditActivity.this.checkStoragePermissions()) {
                    GroupEditActivity.this.pickFromGallery();
                } else {
                    GroupEditActivity.this.requestStoragePermissions();
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
