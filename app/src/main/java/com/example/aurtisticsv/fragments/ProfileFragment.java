package com.example.aurtisticsv.fragments;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blogspot.atifsoftwares.firebaseapp.AddPostActivity;
import com.blogspot.atifsoftwares.firebaseapp.MainActivity;
import com.blogspot.atifsoftwares.firebaseapp.R;
import com.blogspot.atifsoftwares.firebaseapp.SettingsActivity;
import com.blogspot.atifsoftwares.firebaseapp.adapters.AdapterPosts;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask.TaskSnapshot;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int STORAGE_REQUEST_CODE = 200;
    AdapterPosts adapterPosts;
    ImageView avatarIv;
    String[] cameraPermissions;
    ImageView coverIv;
    DatabaseReference databaseReference;
    TextView emailTv;
    FloatingActionButton fab;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    Uri image_uri;
    TextView nameTv;
    ProgressDialog pd;
    TextView phoneTv;
    List<ModelPost> postList;
    RecyclerView postsRecyclerView;
    String profileOrCoverPhoto;
    String storagePath = "Users_Profile_Cover_Imgs/";
    String[] storagePermissions;
    StorageReference storageReference;
    String uid;
    FirebaseUser user;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        FirebaseAuth instance = FirebaseAuth.getInstance();
        this.firebaseAuth = instance;
        this.user = instance.getCurrentUser();
        FirebaseDatabase instance2 = FirebaseDatabase.getInstance();
        this.firebaseDatabase = instance2;
        this.databaseReference = instance2.getReference("Users");
        this.storageReference = FirebaseStorage.getInstance().getReference();
        String str = "android.permission.WRITE_EXTERNAL_STORAGE";
        this.cameraPermissions = new String[]{"android.permission.CAMERA", str};
        this.storagePermissions = new String[]{str};
        this.avatarIv = (ImageView) view.findViewById(R.id.avatarIv);
        this.coverIv = (ImageView) view.findViewById(R.id.coverIv);
        this.nameTv = (TextView) view.findViewById(R.id.nameTv);
        this.emailTv = (TextView) view.findViewById(R.id.emailTv);
        this.phoneTv = (TextView) view.findViewById(R.id.phoneTv);
        this.fab = (FloatingActionButton) view.findViewById(R.id.fab);
        this.postsRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_posts);
        this.pd = new ProgressDialog(getActivity());
        this.databaseReference.orderByChild("email").equalTo(this.user.getEmail()).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String str = "";
                    String name = str + ds.child("name").getValue();
                    String email = str + ds.child("email").getValue();
                    String phone = str + ds.child("phone").getValue();
                    String image = str + ds.child("image").getValue();
                    str = str + ds.child("cover").getValue();
                    ProfileFragment.this.nameTv.setText(name);
                    ProfileFragment.this.emailTv.setText(email);
                    ProfileFragment.this.phoneTv.setText(phone);
                    try {
                        Picasso.get().load(image).into(ProfileFragment.this.avatarIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img_white).into(ProfileFragment.this.avatarIv);
                    }
                    try {
                        Picasso.get().load(str).into(ProfileFragment.this.coverIv);
                    } catch (Exception e2) {
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
        this.fab.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ProfileFragment.this.showEditProfileDialog();
            }
        });
        this.postList = new ArrayList();
        checkUserStatus();
        loadMyPosts();
        return view;
    }

    private void loadMyPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        this.postsRecyclerView.setLayoutManager(layoutManager);
        FirebaseDatabase.getInstance().getReference("Posts").orderByChild("uid").equalTo(this.uid).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProfileFragment.this.postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ProfileFragment.this.postList.add((ModelPost) ds.getValue(ModelPost.class));
                    ProfileFragment.this.adapterPosts = new AdapterPosts(ProfileFragment.this.getActivity(), ProfileFragment.this.postList);
                    ProfileFragment.this.postsRecyclerView.setAdapter(ProfileFragment.this.adapterPosts);
                }
            }

            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileFragment.this.getActivity(), "" + databaseError.getMessage(), 0).show();
            }
        });
    }

    private void searchMyPosts(final String searchQuery) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        this.postsRecyclerView.setLayoutManager(layoutManager);
        FirebaseDatabase.getInstance().getReference("Posts").orderByChild("uid").equalTo(this.uid).addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProfileFragment.this.postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPosts = (ModelPost) ds.getValue(ModelPost.class);
                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) || myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        ProfileFragment.this.postList.add(myPosts);
                    }
                    ProfileFragment.this.adapterPosts = new AdapterPosts(ProfileFragment.this.getActivity(), ProfileFragment.this.postList);
                    ProfileFragment.this.postsRecyclerView.setAdapter(ProfileFragment.this.adapterPosts);
                }
            }

            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileFragment.this.getActivity(), "" + databaseError.getMessage(), 0).show();
            }
        });
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(getActivity(), "android.permission.WRITE_EXTERNAL_STORAGE") == 0;
    }

    private void requestStoragePermission() {
        requestPermissions(this.storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(), "android.permission.CAMERA") == 0;
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), "android.permission.WRITE_EXTERNAL_STORAGE") == 0;
        if (result && result1) {
            return true;
        }
        return false;
    }

    private void requestCameraPermission() {
        requestPermissions(this.cameraPermissions, 100);
    }

    private void showEditProfileDialog() {
        String[] options = new String[]{"Edit Profile Picture", "Edit Cover Photo", "Edit Name", "Edit Phone", "Change Password"};
        Builder builder = new Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    ProfileFragment.this.pd.setMessage("Updating Profile Picture");
                    ProfileFragment.this.profileOrCoverPhoto = "image";
                    ProfileFragment.this.showImagePicDialog();
                } else if (which == 1) {
                    ProfileFragment.this.pd.setMessage("Updating Cover Photo");
                    ProfileFragment.this.profileOrCoverPhoto = "cover";
                    ProfileFragment.this.showImagePicDialog();
                } else if (which == 2) {
                    ProfileFragment.this.pd.setMessage("Updating Name");
                    ProfileFragment.this.showNamePhoneUpdateDialog("name");
                } else if (which == 3) {
                    ProfileFragment.this.pd.setMessage("Updating Phone");
                    ProfileFragment.this.showNamePhoneUpdateDialog("phone");
                } else if (which == 4) {
                    ProfileFragment.this.pd.setMessage("Changing Password");
                    ProfileFragment.this.showChangePasswordDialog();
                }
            }
        });
        builder.create().show();
    }

    private void showChangePasswordDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_password, null);
        final EditText passwordEt = (EditText) view.findViewById(R.id.passwordEt);
        final EditText newPasswordEt = (EditText) view.findViewById(R.id.newPasswordEt);
        Button updatePasswordBtn = (Button) view.findViewById(R.id.updatePasswordBtn);
        Builder builder = new Builder(getActivity());
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();
        updatePasswordBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String oldPassword = passwordEt.getText().toString().trim();
                String newPassword = newPasswordEt.getText().toString().trim();
                if (TextUtils.isEmpty(oldPassword)) {
                    Toast.makeText(ProfileFragment.this.getActivity(), "Enter your current password...", 0).show();
                } else if (newPassword.length() < 6) {
                    Toast.makeText(ProfileFragment.this.getActivity(), "Password length must atleast 6 characters...", 0).show();
                } else {
                    dialog.dismiss();
                    ProfileFragment.this.updatePassword(oldPassword, newPassword);
                }
            }
        });
    }

    private void updatePassword(String oldPassword, final String newPassword) {
        this.pd.show();
        final FirebaseUser user = this.firebaseAuth.getCurrentUser();
        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), oldPassword)).addOnSuccessListener(new OnSuccessListener<Void>() {
            public void onSuccess(Void aVoid) {
                user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                    public void onSuccess(Void aVoid) {
                        ProfileFragment.this.pd.dismiss();
                        Toast.makeText(ProfileFragment.this.getActivity(), "Password Updated...", 0).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    public void onFailure(Exception e) {
                        ProfileFragment.this.pd.dismiss();
                        Toast.makeText(ProfileFragment.this.getActivity(), "" + e.getMessage(), 0).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                ProfileFragment.this.pd.dismiss();
                Toast.makeText(ProfileFragment.this.getActivity(), "" + e.getMessage(), 0).show();
            }
        });
    }

    private void showNamePhoneUpdateDialog(final String key) {
        Builder builder = new Builder(getActivity());
        builder.setTitle("Update " + key);
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(1);
        linearLayout.setPadding(10, 10, 10, 10);
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key);
        linearLayout.addView(editText);
        builder.setView(linearLayout);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final String value = editText.getText().toString().trim();
                if (TextUtils.isEmpty(value)) {
                    Toast.makeText(ProfileFragment.this.getActivity(), "Please enter " + key, 0).show();
                    return;
                }
                ProfileFragment.this.pd.show();
                HashMap<String, Object> result = new HashMap();
                result.put(key, value);
                ProfileFragment.this.databaseReference.child(ProfileFragment.this.user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                    public void onSuccess(Void aVoid) {
                        ProfileFragment.this.pd.dismiss();
                        Toast.makeText(ProfileFragment.this.getActivity(), "Updated...", 0).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    public void onFailure(Exception e) {
                        ProfileFragment.this.pd.dismiss();
                        Toast.makeText(ProfileFragment.this.getActivity(), "" + e.getMessage(), 0).show();
                    }
                });
                if (key.equals("name")) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                    ref.orderByChild("uid").equalTo(ProfileFragment.this.uid).addValueEventListener(new ValueEventListener() {
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                dataSnapshot.getRef().child(ds.getKey()).child("uName").setValue(value);
                            }
                        }

                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                String child = ds.getKey();
                                String str = "Comments";
                                if (dataSnapshot.child(child).hasChild(str)) {
                                    FirebaseDatabase.getInstance().getReference("Posts").child("" + dataSnapshot.child(child).getKey()).child(str).orderByChild("uid").equalTo(ProfileFragment.this.uid).addValueEventListener(new ValueEventListener() {
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                dataSnapshot.getRef().child(ds.getKey()).child("uName").setValue(value);
                                            }
                                        }

                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                                }
                            }
                        }

                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void showImagePicDialog() {
        String[] options = new String[]{"Camera", "Gallery"};
        Builder builder = new Builder(getActivity());
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (ProfileFragment.this.checkCameraPermission()) {
                        ProfileFragment.this.pickFromCamera();
                    } else {
                        ProfileFragment.this.requestCameraPermission();
                    }
                } else if (which != 1) {
                } else {
                    if (ProfileFragment.this.checkStoragePermission()) {
                        ProfileFragment.this.pickFromGallery();
                    } else {
                        ProfileFragment.this.requestStoragePermission();
                    }
                }
            }
        });
        builder.create().show();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean writeStorageAccepted = true;
        if (requestCode != 100) {
            if (requestCode == STORAGE_REQUEST_CODE && grantResults.length > 0) {
                if (grantResults[0] != 0) {
                    writeStorageAccepted = false;
                }
                if (writeStorageAccepted) {
                    pickFromGallery();
                } else {
                    Toast.makeText(getActivity(), "Please enable storage permission", 0).show();
                }
            }
        } else if (grantResults.length > 0) {
            boolean cameraAccepted = grantResults[0] == 0;
            if (grantResults[1] != 0) {
                writeStorageAccepted = false;
            }
            if (cameraAccepted && writeStorageAccepted) {
                pickFromCamera();
            } else {
                Toast.makeText(getActivity(), "Please enable camera & storage permission", 0).show();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                Uri data2 = data.getData();
                this.image_uri = data2;
                uploadProfileCoverPhoto(data2);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                uploadProfileCoverPhoto(this.image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        this.pd.show();
        this.storageReference.child(this.storagePath + "" + this.profileOrCoverPhoto + "_" + this.user.getUid()).putFile(uri).addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {
            public void onSuccess(TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) {
                }
                final Uri downloadUri = (Uri) uriTask.getResult();
                if (uriTask.isSuccessful()) {
                    HashMap<String, Object> results = new HashMap();
                    results.put(ProfileFragment.this.profileOrCoverPhoto, downloadUri.toString());
                    ProfileFragment.this.databaseReference.child(ProfileFragment.this.user.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                        public void onSuccess(Void aVoid) {
                            ProfileFragment.this.pd.dismiss();
                            Toast.makeText(ProfileFragment.this.getActivity(), "Image Updated...", 0).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        public void onFailure(Exception e) {
                            ProfileFragment.this.pd.dismiss();
                            Toast.makeText(ProfileFragment.this.getActivity(), "Erro Updating Image...", 0).show();
                        }
                    });
                    if (ProfileFragment.this.profileOrCoverPhoto.equals("image")) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        ref.orderByChild("uid").equalTo(ProfileFragment.this.uid).addValueEventListener(new ValueEventListener() {
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    dataSnapshot.getRef().child(ds.getKey()).child("uDp").setValue(downloadUri.toString());
                                }
                            }

                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String child = ds.getKey();
                                    String str = "Comments";
                                    if (dataSnapshot.child(child).hasChild(str)) {
                                        FirebaseDatabase.getInstance().getReference("Posts").child("" + dataSnapshot.child(child).getKey()).child(str).orderByChild("uid").equalTo(ProfileFragment.this.uid).addValueEventListener(new ValueEventListener() {
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                    dataSnapshot.getRef().child(ds.getKey()).child("uDp").setValue(downloadUri.toString());
                                                }
                                            }

                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                }
                            }

                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        return;
                    }
                    return;
                }
                ProfileFragment.this.pd.dismiss();
                Toast.makeText(ProfileFragment.this.getActivity(), "Some error occured", 0).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                ProfileFragment.this.pd.dismiss();
                Toast.makeText(ProfileFragment.this.getActivity(), e.getMessage(), 0).show();
            }
        });
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put("title", "Temp Pic");
        values.put("description", "Temp Description");
        this.image_uri = getActivity().getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        cameraIntent.putExtra("output", this.image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent("android.intent.action.PICK");
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void checkUserStatus() {
        FirebaseUser user = this.firebaseAuth.getCurrentUser();
        if (user != null) {
            this.uid = user.getUid();
            return;
        }
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }

    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        ((SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search))).setOnQueryTextListener(new OnQueryTextListener() {
            public boolean onQueryTextSubmit(String s) {
                if (TextUtils.isEmpty(s)) {
                    ProfileFragment.this.loadMyPosts();
                } else {
                    ProfileFragment.this.searchMyPosts(s);
                }
                return false;
            }

            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)) {
                    ProfileFragment.this.loadMyPosts();
                } else {
                    ProfileFragment.this.searchMyPosts(s);
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            this.firebaseAuth.signOut();
            checkUserStatus();
        } else if (id == R.id.action_add_post) {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
