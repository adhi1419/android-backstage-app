package com.adhi.backstage.com.adhi.backstage.zbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.adhi.backstage.MainActivity;
import com.adhi.backstage.R;
import com.adhi.backstage.com.adhi.backstage.cardlist.InventoryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class SimpleScannerActivity extends BaseScannerActivity implements ZBarScannerView.ResultHandler {
    private ZBarScannerView mScannerView;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private String sEvent = null;
    private Button bAdd;
    private EditText etAdd;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_simple_scanner);
        setupToolbar();
        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZBarScannerView(this);
        contentFrame.addView(mScannerView);

        bAdd = (Button) findViewById(R.id.btn_zbar_add);
        etAdd = (EditText) findViewById(R.id.et_zbar_add);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Inventory");
        auth = FirebaseAuth.getInstance();
        sEvent = getIntent().getStringExtra("event_key");

        bAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = etAdd.getText().toString();
                if (!data.equals("")) {
                    onItemScanned(data);
                } else {
                    Toast.makeText(getApplicationContext(), "Enter a valid Item Number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        onItemScanned(rawResult.getContents());
    }

    private void onItemScanned(final String itemName) {
        final String key = mDatabase.push().getKey();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());
        final String currentDateAndTime = sdf.format(new Date());

        FirebaseDatabase.getInstance().getReference().child("Items").child(itemName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot das) {
                        if (das.getValue() != null) {
                            if (das.child("history").getValue() != null) {
                                final Query mLastItemIssue = FirebaseDatabase.getInstance().getReference().child("Items")
                                        .child(itemName).child("history").limitToLast(1);
                                ValueEventListener checkItemListener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot) {
                                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                            final String sKeyLastEventIssued = dataSnapshot1.getKey().toString();
                                            FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot ds) {
                                                    if (!ds.child("Inventory").child(sKeyLastEventIssued)
                                                            .child("status").getValue().toString().equals("0")) {
                                                        InventoryItem iItem = new InventoryItem(auth.getCurrentUser().getEmail().split("\\@")[0], itemName
                                                                , sEvent, currentDateAndTime, "0");
                                                        Map<String, Object> postValues = iItem.toMap();
                                                        Map<String, Object> childUpdates = new HashMap<>();
                                                        childUpdates.put(key, postValues);
                                                        mDatabase.updateChildren(childUpdates);

                                                        Map<String, Object> mItemHistory = new HashMap<>();
                                                        mItemHistory.put("event", sEvent);
                                                        Map<String, Object> itemHistory = new HashMap<>();
                                                        itemHistory.put(key, mItemHistory);
                                                        FirebaseDatabase.getInstance().getReference()
                                                                .child("Items").child(itemName).child("history")
                                                                .updateChildren(itemHistory);

                                                        Map<String, Object> mEventHistory = new HashMap<>();
                                                        mItemHistory.put("item", itemName);
                                                        Map<String, Object> eventHistory = new HashMap<>();
                                                        eventHistory.put(key, mEventHistory);
                                                        FirebaseDatabase.getInstance().getReference()
                                                                .child("Event").child(sEvent).child("items")
                                                                .updateChildren(eventHistory);

                                                        mAddMoreItemAlertDialog("Add New Item", "Do you want to add more items?");
                                                    } else {
                                                        mAddMoreItemAlertDialog("This item has not been returned yet", "Do you want to add more items?");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }

                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                };
                                mLastItemIssue.addListenerForSingleValueEvent(checkItemListener);
                            } else {
                                InventoryItem iItem = new InventoryItem(auth.getCurrentUser().getEmail().split("\\@")[0], itemName
                                        , sEvent, currentDateAndTime, "0");
                                Map<String, Object> postValues = iItem.toMap();
                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put(key, postValues);
                                mDatabase.updateChildren(childUpdates);

                                Map<String, Object> mItemHistory = new HashMap<>();
                                mItemHistory.put("event", sEvent);
                                Map<String, Object> itemHistory = new HashMap<>();
                                itemHistory.put(key, mItemHistory);
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Items").child(itemName).child("history")
                                        .updateChildren(itemHistory);

                                Map<String, Object> mEventHistory = new HashMap<>();
                                mEventHistory.put("item", itemName);
                                Map<String, Object> eventHistory = new HashMap<>();
                                eventHistory.put(key, mEventHistory);
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Event").child(sEvent).child("items")
                                        .updateChildren(eventHistory);

                                mAddMoreItemAlertDialog("Add New Item", "Do you want to add more items?");
                            }

                        } else {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SimpleScannerActivity.this);
                            alertDialogBuilder.setTitle("Item not found");
                            alertDialogBuilder
                                    .setMessage("Do you still want to issue?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            InventoryItem iItem = new InventoryItem(auth.getCurrentUser().getEmail().split("\\@")[0], itemName
                                                    , sEvent, currentDateAndTime, "0");
                                            Map<String, Object> postValues = iItem.toMap();
                                            Map<String, Object> childUpdates = new HashMap<>();
                                            childUpdates.put(key, postValues);
                                            mDatabase.updateChildren(childUpdates);

                                            Map<String, Object> mEventHistory = new HashMap<>();
                                            mEventHistory.put("item", itemName);
                                            Map<String, Object> eventHistory = new HashMap<>();
                                            eventHistory.put(key, mEventHistory);
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("Event").child(sEvent).child("items")
                                                    .updateChildren(eventHistory);
                                            dialog.cancel();
                                            mAddMoreItemAlertDialog("Add New Item", "Do you want to add more items?");
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            mAddMoreItemAlertDialog("Add New Item", "Do you want to add more items?");
                                        }
                                    });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }

                });

    }


    private void mAddMoreItemAlertDialog(String title, String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SimpleScannerActivity.this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mScannerView.resumeCameraPreview(SimpleScannerActivity.this);
                            }
                        }, 2000);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(SimpleScannerActivity.this, MainActivity.class);
                        intent.putExtra("fragment", R.id.inventory);
                        startActivity(intent);
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}

