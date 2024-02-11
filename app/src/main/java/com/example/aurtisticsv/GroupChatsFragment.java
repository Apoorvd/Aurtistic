package com.blogspot.atifsoftwares.firebaseapp;

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
import androidx.recyclerview.widget.RecyclerView;
import com.blogspot.atifsoftwares.firebaseapp.adapters.AdapterGroupChatList;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelGroupChatList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class GroupChatsFragment extends Fragment {
    private AdapterGroupChatList adapterGroupChatList;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelGroupChatList> groupChatLists;
    private RecyclerView groupsRv;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_chats, container, false);
        this.groupsRv = (RecyclerView) view.findViewById(R.id.groupsRv);
        this.firebaseAuth = FirebaseAuth.getInstance();
        loadGroupChatsList();
        return view;
    }

    private void loadGroupChatsList() {
        this.groupChatLists = new ArrayList();
        FirebaseDatabase.getInstance().getReference("Groups").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                GroupChatsFragment.this.groupChatLists.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("Participants").child(GroupChatsFragment.this.firebaseAuth.getUid()).exists()) {
                        GroupChatsFragment.this.groupChatLists.add((ModelGroupChatList) ds.getValue(ModelGroupChatList.class));
                    }
                }
                GroupChatsFragment.this.adapterGroupChatList = new AdapterGroupChatList(GroupChatsFragment.this.getActivity(), GroupChatsFragment.this.groupChatLists);
                GroupChatsFragment.this.groupsRv.setAdapter(GroupChatsFragment.this.adapterGroupChatList);
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void searchGroupChatsList(final String query) {
        this.groupChatLists = new ArrayList();
        FirebaseDatabase.getInstance().getReference("Groups").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                GroupChatsFragment.this.groupChatLists.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("Participants").child(GroupChatsFragment.this.firebaseAuth.getUid()).exists() && ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())) {
                        GroupChatsFragment.this.groupChatLists.add((ModelGroupChatList) ds.getValue(ModelGroupChatList.class));
                    }
                }
                GroupChatsFragment.this.adapterGroupChatList = new AdapterGroupChatList(GroupChatsFragment.this.getActivity(), GroupChatsFragment.this.groupChatLists);
                GroupChatsFragment.this.groupsRv.setAdapter(GroupChatsFragment.this.adapterGroupChatList);
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        ((SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search))).setOnQueryTextListener(new OnQueryTextListener() {
            public boolean onQueryTextSubmit(String s) {
                if (TextUtils.isEmpty(s.trim())) {
                    GroupChatsFragment.this.loadGroupChatsList();
                } else {
                    GroupChatsFragment.this.searchGroupChatsList(s);
                }
                return false;
            }

            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s.trim())) {
                    GroupChatsFragment.this.loadGroupChatsList();
                } else {
                    GroupChatsFragment.this.searchGroupChatsList(s);
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
        } else if (id == R.id.action_create_group) {
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        if (this.firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}
