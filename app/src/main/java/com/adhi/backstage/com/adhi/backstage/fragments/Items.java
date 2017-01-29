package com.adhi.backstage.com.adhi.backstage.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.adhi.backstage.R;
import com.adhi.backstage.com.adhi.backstage.cardlist.DatabaseItem;
import com.adhi.backstage.com.adhi.backstage.cardlist.DatabaseItemCardArrayAdapter;
import com.adhi.backstage.com.adhi.backstage.cardlist.VerticalSpaceItemDecoration;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class Items extends Fragment {

    private SectionedRecyclerViewAdapter sectionAdapter;
    private RecyclerView listView;
    private DatabaseReference mDatabase;
    private List<DatabaseItem> iItemList = new ArrayList<>();
    private List<String> typeList = new ArrayList();
    private DatabaseItemCardArrayAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String listSearch = "";
    private int listSort = 0;
    private int listFilter = 0;

    public Items() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_items, container, false);
        sectionAdapter = new SectionedRecyclerViewAdapter();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Items");

        listView = (RecyclerView) rootView.findViewById(R.id.listView_items);
        mAdapter = new DatabaseItemCardArrayAdapter(iItemList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(mLayoutManager);
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.addItemDecoration(new VerticalSpaceItemDecoration(8));
        listView.setAdapter(mAdapter);
        loadItems(listSearch, listSort, listFilter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout_items);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadItems(listSearch, listSort, listFilter);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        return rootView;
    }

    private void loadItems(final String search, final int sort, final int filter) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                iItemList.clear();
                for (DataSnapshot ds : dataSnapshot.child("Items").getChildren()) {
                    if (!typeList.contains("All"))
                        typeList.add("All");
                    if (!typeList.contains(ds.child("type").getValue().toString()))
                        typeList.add(ds.child("type").getValue().toString());
                    DatabaseItem iItem = new DatabaseItem(ds.getKey().toString(), ds.child("details").getValue().toString(),
                            ds.child("type").getValue().toString());
                    if (iItem.name.contains(search)
                            || iItem.type.toLowerCase().contains(search.toLowerCase())
                            || iItem.desc.contains(search)) {
                        if (listFilter == 0 || iItem.type.equals(typeList.get(listFilter)))
                            iItemList.add(iItem);
                    }
                }

                Collections.sort(iItemList, new ItemListComparator(sort));

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.addValueEventListener(postListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inventory, menu);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_inventory_filter:
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.filter_popup_database);
                dialog.setTitle("Search, Sort & Filter");

                final Spinner sortSpinner = (Spinner) dialog.findViewById(R.id.spinner_sort_database);
                final Spinner filterSpinner = (Spinner) dialog.findViewById(R.id.spinner_filter_database);
                List<String> sortCat = Arrays.asList(getResources().getStringArray(R.array.sort_categories_database));
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, sortCat);
                ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, typeList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sortSpinner.setAdapter(dataAdapter);
                filterSpinner.setAdapter(dataAdapter1);
                sortSpinner.setSelection(listSort);
                filterSpinner.setSelection(listFilter);

                final EditText etSearch = (EditText) dialog.findViewById(R.id.database_search);
                etSearch.setText(listSearch);

                Button bOkay = (Button) dialog.findViewById(R.id.btn_popup_okay);
                Button bCancel = (Button) dialog.findViewById(R.id.btn_popup_cancel);

                bOkay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listSort = sortSpinner.getSelectedItemPosition();
                        listSearch = etSearch.getText().toString();
                        listFilter = filterSpinner.getSelectedItemPosition();
                        dialog.dismiss();
                        loadItems(listSearch, listSort, listFilter);
                    }
                });

                bCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

class ItemListComparator implements Comparator<DatabaseItem> {

    int sort;

    public ItemListComparator(int sort) {
        this.sort = sort;
    }

    @Override
    public int compare(DatabaseItem a, DatabaseItem b) {
        if (sort == 1) {
            return a.type.compareToIgnoreCase(b.type);
        } else {
            return 1;
        }
    }
}
