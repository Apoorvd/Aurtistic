package com.example.aurtisticsv.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blogspot.atifsoftwares.firebaseapp.GroupCreateActivity;
import com.blogspot.atifsoftwares.firebaseapp.MainActivity;
import com.blogspot.atifsoftwares.firebaseapp.R;
import com.blogspot.atifsoftwares.firebaseapp.SettingsActivity;
import com.blogspot.atifsoftwares.firebaseapp.adapters.AdapterUsers;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {
    AdapterUsers adapterUsers;
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelUser> userList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        this.firebaseAuth = FirebaseAuth.getInstance();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.users_recyclerView);
        this.recyclerView = recyclerView;
        recyclerView.setHasFixedSize(true);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.userList = new ArrayList();
        getAllUsers();
        return view;
    }

    private void getAllUsers() {
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                UsersFragment.this.userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelUser modelUser = (ModelUser) ds.getValue(ModelUser.class);
                    if (!modelUser.getUid().equals(fUser.getUid())) {
                        UsersFragment.this.userList.add(modelUser);
                    }
                    UsersFragment.this.adapterUsers = new AdapterUsers(UsersFragment.this.getActivity(), UsersFragment.this.userList);
                    UsersFragment.this.recyclerView.setAdapter(UsersFragment.this.adapterUsers);
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void searchUsers(final String query) {
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                UsersFragment.this.userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelUser modelUser = (ModelUser) ds.getValue(ModelUser.class);
                    if (!modelUser.getUid().equals(fUser.getUid()) && (modelUser.getName().toLowerCase().contains(query.toLowerCase()) || modelUser.getEmail().toLowerCase().contains(query.toLowerCase()))) {
                        UsersFragment.this.userList.add(modelUser);
                    }
                    UsersFragment.this.adapterUsers = new AdapterUsers(UsersFragment.this.getActivity(), UsersFragment.this.userList);
                    UsersFragment.this.adapterUsers.notifyDataSetChanged();
                    UsersFragment.this.recyclerView.setAdapter(UsersFragment.this.adapterUsers);
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void checkUserStatus() {
        if (this.firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        ((SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search))).setOnQueryTextListener(new OnQueryTextListener() {
            public boolean onQueryTextSubmit(String s) {
                if (TextUtils.isEmpty(s.trim())) {
                    UsersFragment.this.getAllUsers();
                } else {
                    UsersFragment.this.searchUsers(s);
                }
                return false;
            }

            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s.trim())) {
                    UsersFragment.this.getAllUsers();
                } else {
                    UsersFragment.this.searchUsers(s);
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
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        } else if (id == R.id.action_create_group) {
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
