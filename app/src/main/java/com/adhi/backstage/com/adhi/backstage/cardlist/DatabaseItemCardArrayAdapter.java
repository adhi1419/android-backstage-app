package com.adhi.backstage.com.adhi.backstage.cardlist;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adhi.backstage.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DatabaseItemCardArrayAdapter extends RecyclerView.Adapter<DatabaseItemCardArrayAdapter.MyViewHolder> {
    private List<DatabaseItem> cardList = new ArrayList<>();
    private DatabaseReference mDatabase;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView line1;
        TextView line2;

        public MyViewHolder(View view) {
            super(view);
            line1 = (TextView) view.findViewById(R.id.list_items_name);
            line2 = (TextView) view.findViewById(R.id.list_items_desc);
        }
    }

    @Override
    public DatabaseItemCardArrayAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_card_items, parent, false);

        return new DatabaseItemCardArrayAdapter.MyViewHolder(itemView);
    }

    public DatabaseItemCardArrayAdapter(List<DatabaseItem> cardList) {
        this.cardList = cardList;
    }

    @Override
    public void onBindViewHolder(final DatabaseItemCardArrayAdapter.MyViewHolder holder, int position) {
        final DatabaseItem card = cardList.get(position);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        holder.line1.setText("[" + card.type + "] " + card.name);
        holder.line2.setText("Description: " + card.desc);

        mDatabase.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            holder.line1.setTextColor(Color.GREEN);
                            if (dataSnapshot.child("Items").child(card.name).child("history")
                                    .getValue() != null) {
                                final Query mLastItemIssue = FirebaseDatabase.getInstance().getReference().child("Items")
                                        .child(card.name).child("history").limitToLast(1);
                                ValueEventListener checkItemListener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot) {
                                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                            final String sKeyLastEventIssued = dataSnapshot1.getKey().toString();
                                            FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot ds) {
                                                    try {
                                                        String status = ds.child("Inventory").child(sKeyLastEventIssued)
                                                                .child("status").getValue().toString();
                                                        if (status.equals("0")) {
                                                            holder.line1.setTextColor(Color.RED);
                                                        }
                                                    } catch (NullPointerException e) {
                                                        Log.e("com.adhi.backstage", ds.toString());
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
                            }
                        } catch (NullPointerException npEx) {
                            npEx.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

}

