package com.example.aurtisticsv.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.blogspot.atifsoftwares.firebaseapp.GroupCreateActivity;
import com.blogspot.atifsoftwares.firebaseapp.MainActivity;
import com.blogspot.atifsoftwares.firebaseapp.R;
import com.blogspot.atifsoftwares.firebaseapp.SettingsActivity;
import com.blogspot.atifsoftwares.firebaseapp.adapters.AdapterChatlist;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelChat;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelChatlist;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {
    AdapterChatlist adapterChatlist;
    List<ModelChatlist> chatlistList;
    FirebaseUser currentUser;
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    DatabaseReference reference;
    List<ModelUser> userList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        this.chatlistList = new ArrayList();
        DatabaseReference child = FirebaseDatabase.getInstance().getReference("Chatlist").child(this.currentUser.getUid());
        this.reference = child;
        child.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatListFragment.this.chatlistList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ChatListFragment.this.chatlistList.add((ModelChatlist) ds.getValue(ModelChatlist.class));
                }
                ChatListFragment.this.loadChats();
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return view;
    }

    private void loadChats() {
        this.userList = new ArrayList();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        this.reference = reference;
        reference.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatListFragment.this.userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelUser user = (ModelUser) ds.getValue(ModelUser.class);
                    for (ModelChatlist chatlist : ChatListFragment.this.chatlistList) {
                        if (user.getUid() != null && user.getUid().equals(chatlist.getId())) {
                            ChatListFragment.this.userList.add(user);
                            break;
                        }
                    }
                    ChatListFragment.this.adapterChatlist = new AdapterChatlist(ChatListFragment.this.getContext(), ChatListFragment.this.userList);
                    ChatListFragment.this.recyclerView.setAdapter(ChatListFragment.this.adapterChatlist);
                    for (int i = 0; i < ChatListFragment.this.userList.size(); i++) {
                        ChatListFragment chatListFragment = ChatListFragment.this;
                        chatListFragment.lastMessage(((ModelUser) chatListFragment.userList.get(i)).getUid());
                    }
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void lastMessage(final String userId) {
        FirebaseDatabase.getInstance().getReference("Chats").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = (ModelChat) ds.getValue(ModelChat.class);
                    if (chat != null) {
                        String sender = chat.getSender();
                        String receiver = chat.getReceiver();
                        if (sender != null) {
                            if (receiver != null) {
                                if ((chat.getReceiver().equals(ChatListFragment.this.currentUser.getUid()) && chat.getSender().equals(userId)) || (chat.getReceiver().equals(userId) && chat.getSender().equals(ChatListFragment.this.currentUser.getUid()))) {
                                    if (chat.getType().equals("image")) {
                                        theLastMessage = "Sent a photo";
                                    } else {
                                        theLastMessage = chat.getMessage();
                                    }
                                }
                            }
                        }
                    }
                }
                ChatListFragment.this.adapterChatlist.setLastMessageMap(userId, theLastMessage);
                ChatListFragment.this.adapterChatlist.notifyDataSetChanged();
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
