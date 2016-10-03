package com.adhi.backstage.com.adhi.backstage.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.PasswordTransformationMethod;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.adhi.backstage.R;
import com.adhi.backstage.com.adhi.backstage.cardlist.EventsItem;
import com.adhi.backstage.com.adhi.backstage.cardlist.InventoryItem;
import com.adhi.backstage.com.adhi.backstage.cardlist.InventoryItemCardArrayAdapter;
import com.adhi.backstage.com.adhi.backstage.cardlist.VerticalSpaceItemDecoration;
import com.adhi.backstage.com.adhi.backstage.zbar.SimpleScannerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class Inventory extends Fragment {

    private FloatingActionButton fab;
    private RecyclerView listView;
    private DatabaseReference mDatabase;
    private List<InventoryItem> iItemList = new ArrayList<>();
    private List<EventsItem> eItemList = new ArrayList<>();
    private List<String> eventList = new ArrayList<String>();
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

        ValueEventListener getEvents = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eItemList.clear();
                eventList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    eventList.add(ds.child("name").getValue().toString());
                    EventsItem eItem = new EventsItem(ds.child("name").getValue().toString(), ds.child("start").getValue().toString(),
                            ds.child("end").getValue().toString(), ds.child("place").getValue().toString());
                    eItem.key = ds.getKey();
                    eItemList.add(eItem);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        FirebaseDatabase.getInstance().getReference().child("Event").addValueEventListener(getEvents);


        fab = (FloatingActionButton) rootView.findViewById(R.id.fab_add_inv);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Select Event");
                final LinearLayout LL = new LinearLayout(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                LL.setLayoutParams(params);
                LL.setOrientation(LinearLayout.HORIZONTAL);
                final TextView tName = new TextView(getContext());
                final Spinner sName = new Spinner(getContext());
                tName.setText("Event:  ");
                tName.setTextColor(Color.BLACK);
                tName.setTextSize(18.0f);
                LL.setPadding(64, 32, 0, 0);
                LL.addView(tName, LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LL.addView(sName, LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                alert.setView(LL);
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, eventList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sName.setAdapter(dataAdapter);
                sName.setSelection(0);
                alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent(getContext(), SimpleScannerActivity.class);
                        intent.putExtra("event_key", eItemList.get(sName.getSelectedItemPosition()).key);
                        startActivity(intent);
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                alert.show();
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
            if (a.status.equals("0") || b.status.equals("0")) {
                return a.status.compareToIgnoreCase(b.status);
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
