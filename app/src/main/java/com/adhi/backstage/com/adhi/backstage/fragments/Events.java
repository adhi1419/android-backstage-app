package com.adhi.backstage.com.adhi.backstage.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.adhi.backstage.R;
import com.adhi.backstage.com.adhi.backstage.cardlist.EventsItem;
import com.adhi.backstage.com.adhi.backstage.cardlist.EventsItemCardArrayAdapter;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Events extends Fragment {

    private FloatingActionButton fab;
    private RecyclerView listView;
    private DatabaseReference mDatabase;
    private List<EventsItem> eItemList = new ArrayList<>();
    private EventsItemCardArrayAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public Events() {
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
        View rootView = inflater.inflate(R.layout.fragment_events, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Event");

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab_add_events);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.add_event_popup);
                dialog.setTitle("Add an Event");
                Calendar c = Calendar.getInstance();

                final EditText etName = (EditText) dialog.findViewById(R.id.add_event_name);
                final EditText etPlace = (EditText) dialog.findViewById(R.id.add_event_place);
                final Button bStartDate = (Button) dialog.findViewById(R.id.btn_event_start_date);
                final Button bStartTime = (Button) dialog.findViewById(R.id.btn_event_start_time);
                final Button bEndDate = (Button) dialog.findViewById(R.id.btn_event_end_date);
                final Button bEndTime = (Button) dialog.findViewById(R.id.btn_event_end_time);
                Button bOkay = (Button) dialog.findViewById(R.id.btn_ev_popup_okay);
                Button bCancel = (Button) dialog.findViewById(R.id.btn_ev_popup_cancel);

                String curDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1 < 10 ?
                        "0" + (c.get(Calendar.MONTH) + 1) : (c.get(Calendar.MONTH) + 1)) + "-" +
                        (c.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + c.get(Calendar.DAY_OF_MONTH)
                                : c.get(Calendar.DAY_OF_MONTH));

                String curTime = (c.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + c.get(Calendar.HOUR_OF_DAY)
                        : c.get(Calendar.HOUR_OF_DAY)) + ":" + (c.get(Calendar.MINUTE) < 10 ? "0" +
                        c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE)) + ":00";

                bStartDate.setText(curDate);
                bStartTime.setText(curTime);
                bEndDate.setText(curDate);
                bEndTime.setText(curTime);

                bStartDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickDate(bStartDate);
                    }
                });

                bStartTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickTime(bStartTime);
                    }
                });

                bEndDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickDate(bEndDate);
                    }
                });

                bEndTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickTime(bEndTime);
                    }
                });


                bOkay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String key = mDatabase.push().getKey();
                        EventsItem eItem = new EventsItem(etName.getText().toString(), bStartDate.getText().toString() +
                                " " + bStartTime.getText().toString(), bEndDate.getText().toString() +
                                " " + bEndTime.getText().toString(), etPlace.getText().toString());
                        Map<String, Object> postValues = eItem.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(key, postValues);
                        mDatabase.updateChildren(childUpdates);
                        dialog.dismiss();
                        loadItems();
                    }
                });

                bCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        listView = (RecyclerView) rootView.findViewById(R.id.listView_events);
        mAdapter = new EventsItemCardArrayAdapter(eItemList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(mLayoutManager);
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.addItemDecoration(new VerticalSpaceItemDecoration(8));
        listView.setAdapter(mAdapter);
        loadItems();

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout_events);
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
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eItemList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    EventsItem eItem = new EventsItem(ds.child("name").getValue().toString(), ds.child("start").getValue().toString(),
                            ds.child("end").getValue().toString(), ds.child("place").getValue().toString());
                    eItem.key = ds.getKey();
                    eItemList.add(eItem);
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.addValueEventListener(postListener);
    }

    private void pickDate(final Button b) {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        String date = year + "-" + (monthOfYear + 1 < 10 ? "0" + (monthOfYear + 1) :
                                (monthOfYear + 1)) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth);
                        b.setText(date);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void pickTime(final Button b) {
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        b.setText((hourOfDay < 10 ? "0" + hourOfDay : hourOfDay) + ":" + (minute < 10 ? "0" + minute : minute) + ":00");
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}