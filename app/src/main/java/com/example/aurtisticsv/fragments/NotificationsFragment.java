package com.example.aurtisticsv.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.blogspot.atifsoftwares.firebaseapp.R;
import com.blogspot.atifsoftwares.firebaseapp.adapters.AdapterNotification;
import com.blogspot.atifsoftwares.firebaseapp.models.ModelNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class NotificationsFragment extends Fragment {
    private AdapterNotification adapterNotification;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelNotification> notificationsList;
    RecyclerView notificationsRv;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        this.notificationsRv = (RecyclerView) view.findViewById(R.id.notificationsRv);
        this.firebaseAuth = FirebaseAuth.getInstance();
        getAllNotifications();
        return view;
    }

    private void getAllNotifications() {
        this.notificationsList = new ArrayList();
        FirebaseDatabase.getInstance().getReference("Users").child(this.firebaseAuth.getUid()).child("Notifications").addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                NotificationsFragment.this.notificationsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    NotificationsFragment.this.notificationsList.add((ModelNotification) ds.getValue(ModelNotification.class));
                }
                NotificationsFragment.this.adapterNotification = new AdapterNotification(NotificationsFragment.this.getActivity(), NotificationsFragment.this.notificationsList);
                NotificationsFragment.this.notificationsRv.setAdapter(NotificationsFragment.this.adapterNotification);
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
