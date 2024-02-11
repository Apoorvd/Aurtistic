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
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aurtisticsv.AddPostActivity;
import com.example.aurtisticsv.MainActivity;
import com.example.aurtisticsv.R;
import com.blogspot.atifsoftwares.firebaseapp.SettingsActivity;
import com.example.aurtisticsv.adapters.AdapterPosts;
import com.example.aurtisticsv.models.ModelPost;
import com.example.aurtisticsv.adapters.AdapterPosts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    AdapterPosts adapterPosts;
    FirebaseAuth firebaseAuth;
    List<ModelPost> postList;
    RecyclerView recyclerView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.recyclerView = (RecyclerView) view.findViewById(R.id.postsRecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        this.recyclerView.setLayoutManager(layoutManager);
        this.postList = new ArrayList();
        loadPosts();
        return view;
    }

    private void loadPosts() {
        FirebaseDatabase.getInstance().getReference("Posts").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                HomeFragment.this.postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    HomeFragment.this.postList.add((ModelPost) ds.getValue(ModelPost.class));
                    HomeFragment.this.adapterPosts = new AdapterPosts(HomeFragment.this.getActivity(), HomeFragment.this.postList);
                    HomeFragment.this.recyclerView.setAdapter(HomeFragment.this.adapterPosts);
                }
            }

            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomeFragment.this.getActivity(), "" + databaseError.getMessage(), 0).show();
            }
        });
    }

    private void searchPosts(final String searchQuery) {
        FirebaseDatabase.getInstance().getReference("Posts").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                HomeFragment.this.postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost modelPost = (ModelPost) ds.getValue(ModelPost.class);
                    if (modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) || modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        HomeFragment.this.postList.add(modelPost);
                    }
                    HomeFragment.this.adapterPosts = new AdapterPosts(HomeFragment.this.getActivity(), HomeFragment.this.postList);
                    HomeFragment.this.recyclerView.setAdapter(HomeFragment.this.adapterPosts);
                }
            }

            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomeFragment.this.getActivity(), "" + databaseError.getMessage(), 0).show();
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
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        ((SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search))).setOnQueryTextListener(new OnQueryTextListener() {
            public boolean onQueryTextSubmit(String s) {
                if (TextUtils.isEmpty(s)) {
                    HomeFragment.this.loadPosts();
                } else {
                    HomeFragment.this.searchPosts(s);
                }
                return false;
            }

            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)) {
                    HomeFragment.this.loadPosts();
                } else {
                    HomeFragment.this.searchPosts(s);
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
