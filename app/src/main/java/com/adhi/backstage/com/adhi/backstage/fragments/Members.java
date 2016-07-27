package com.adhi.backstage.com.adhi.backstage.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adhi.backstage.R;
import com.adhi.backstage.com.adhi.backstage.cardlist.MemberCardArrayAdapter;
import com.adhi.backstage.com.adhi.backstage.cardlist.User;
import com.adhi.backstage.com.adhi.backstage.cardlist.VerticalSpaceItemDecoration;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Members extends Fragment {

    private RecyclerView listView;
    private DatabaseReference mDatabase;
    private MemberCardArrayAdapter mAdapter;
    private List<User> userList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public Members() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_members, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        listView = (RecyclerView) rootView.findViewById(R.id.listView_members);
        mAdapter = new MemberCardArrayAdapter(userList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(mLayoutManager);
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.addItemDecoration(new VerticalSpaceItemDecoration(8));
        listView.setAdapter(mAdapter);
        loadItems();

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout_members);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadItems();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        return rootView;
    }

    private void loadItems() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = new User(ds.getKey().toString(), ds.child("name").getValue().toString(),
                            ds.child("avatar").getValue().toString(),
                            ds.child("role").getValue().toString(),
                            ds.child("phone").getValue().toString());
                    userList.add(user);
                }
                Collections.sort(userList, new MembersComparator());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.addValueEventListener(postListener);
    }

    public static String getRoleByVal(String val) {
        if (Integer.valueOf(val) == 0) {
            return "Faculty";
        } else if (Integer.valueOf(val) == 1) {
            return "Co-ordinator";
        } else if (Integer.valueOf(val) == 2) {
            return "Core Member";
        }
        return "Member";
    }
}

class MembersComparator implements Comparator<User> {
    @Override
    public int compare(User a, User b) {
        int x = Integer.valueOf(a.getRole()) - Integer.valueOf(b.getRole());
        if (x > 0) {
            return 1;
        } else if (x == 0) {
            return a.getName().compareToIgnoreCase(b.getName());
        } else {
            return -1;
        }
    }
}

