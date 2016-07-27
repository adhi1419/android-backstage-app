package com.adhi.backstage.com.adhi.backstage.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.adhi.backstage.com.adhi.backstage.cardlist.InventoryItem;
import com.adhi.backstage.com.adhi.backstage.cardlist.InventoryItemCardArrayAdapter;
import com.adhi.backstage.com.adhi.backstage.cardlist.VerticalSpaceItemDecoration;
import com.adhi.backstage.com.adhi.backstage.zbar.SimpleScannerActivity;
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


public class Inventory extends Fragment {

    private FloatingActionButton fab;
    private RecyclerView listView;
    private DatabaseReference mDatabase;
    private List<InventoryItem> iItemList = new ArrayList<>();
    private InventoryItemCardArrayAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String listSearch = "";
    private int listSort = 0;

    public Inventory() {
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
        View rootView = inflater.inflate(R.layout.fragment_inventory, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Inventory");

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab_add_inv);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SimpleScannerActivity.class);
                startActivity(intent);
            }
        });

        listView = (RecyclerView) rootView.findViewById(R.id.listView_inventory);
        mAdapter = new InventoryItemCardArrayAdapter(iItemList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(mLayoutManager);
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.addItemDecoration(new VerticalSpaceItemDecoration(8));
        listView.setAdapter(mAdapter);
        loadItems(listSearch, listSort);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout_inventory);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadItems(listSearch, listSort);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        return rootView;
    }

    private void loadItems(final String search, final int sort) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                iItemList.clear();
                for (DataSnapshot ds : dataSnapshot.child("Inventory").getChildren()) {
                    InventoryItem iItem = new InventoryItem(ds.child("id").getValue().toString(), ds.child("item").getValue().toString(),
                            ds.child("event").getValue().toString(), ds.child("time").getValue().toString(),
                            ds.child("status").getValue().toString());
                    iItem.key = ds.getKey();
                    if (iItem.item.contains(search)
                            || iItem.event.toLowerCase().contains(search.toLowerCase())
                            || iItem.time.contains(search)
                            || dataSnapshot.child("Users").child(iItem.id).child("name").getValue()
                            .toString().toLowerCase().contains(search.toLowerCase())) {
                        iItemList.add(iItem);
                    }
                }
                if (sort == 0) {
                    Collections.reverse(iItemList);
                } else {
                    Collections.sort(iItemList, new InventoryItemComparator(sort));
                }
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
                dialog.setContentView(R.layout.filter_popup);
                dialog.setTitle("Search & Sort");

                final Spinner sortSpinner = (Spinner) dialog.findViewById(R.id.spinner_sort);
                List<String> sortCat = Arrays.asList(getResources().getStringArray(R.array.sort_categories));
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, sortCat);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sortSpinner.setAdapter(dataAdapter);
                sortSpinner.setSelection(listSort);

                final EditText etSearch = (EditText) dialog.findViewById(R.id.inventory_search);
                etSearch.setText(listSearch);

                Button bOkay = (Button) dialog.findViewById(R.id.btn_popup_okay);
                Button bCancel = (Button) dialog.findViewById(R.id.btn_popup_cancel);

                bOkay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listSort = sortSpinner.getSelectedItemPosition();
                        listSearch = etSearch.getText().toString();
                        dialog.dismiss();
                        loadItems(listSearch, listSort);
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

class InventoryItemComparator implements Comparator<InventoryItem> {

    int sort;

    public InventoryItemComparator(int sort) {
        this.sort = sort;
    }

    @Override
    public int compare(InventoryItem a, InventoryItem b) {
        if (sort == 1) {
            return a.event.compareToIgnoreCase(b.event);
        } else if (sort == 2) {
            if (a.status == "0") {
                return -1;
            } else if (b.status == "0") {
                return 1;
            } else {
                return -a.status.compareToIgnoreCase(b.status);
            }
        } else if (sort == 3) {
            return a.item.compareToIgnoreCase((b.item));
        } else {
            return 0;
        }
    }
}
