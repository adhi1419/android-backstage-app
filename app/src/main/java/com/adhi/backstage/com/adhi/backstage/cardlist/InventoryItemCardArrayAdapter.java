package com.adhi.backstage.com.adhi.backstage.cardlist;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.adhi.backstage.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class InventoryItemCardArrayAdapter extends RecyclerView.Adapter<InventoryItemCardArrayAdapter.MyViewHolder> {
    private List<InventoryItem> cardList = new ArrayList<>();
    private DatabaseReference mDatabase;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView line1;
        TextView line2;
        TextView line3;
        TextView line4;
        TextView line5;
        Button btn1;

        public MyViewHolder(View view) {
            super(view);
            line1 = (TextView) view.findViewById(R.id.list_inventory_item);
            line2 = (TextView) view.findViewById(R.id.list_inventory_name);
            line3 = (TextView) view.findViewById(R.id.list_inventory_time);
            line4 = (TextView) view.findViewById(R.id.list_inventory_event);
            line5 = (TextView) view.findViewById(R.id.list_inventory_return);
            btn1 = (Button) view.findViewById(R.id.list_inventory_return_btn);
        }
    }

    @Override
    public InventoryItemCardArrayAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_card_inventory, parent, false);

        return new InventoryItemCardArrayAdapter.MyViewHolder(itemView);
    }

    public InventoryItemCardArrayAdapter(List<InventoryItem> cardList) {
        this.cardList = cardList;
    }

    @Override
    public void onBindViewHolder(final InventoryItemCardArrayAdapter.MyViewHolder holder, int position) {
        final InventoryItem card = cardList.get(position);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        holder.line1.setText(card.item);
        holder.line3.setText("Issued On: " + card.time);
        mDatabase.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        holder.line2.setText("Issued To: " + dataSnapshot.child("Users").child(card.id)
                                .child("name").getValue().toString());
                        holder.line4.setText("Event: " + dataSnapshot.child("Event").child(card.event)
                                .child("name").getValue().toString() + ", " + dataSnapshot.child("Event").child(card.event)
                                .child("place").getValue().toString());
                        if (!card.status.equals("0")) {
                            holder.btn1.setVisibility(View.GONE);
                            holder.line1.setTextColor(Color.GREEN);
                            holder.line5.setText("Returned On: " + card.status);
                        } else {
                            if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().split("\\@")[0].equals(card.id))
                                holder.btn1.setVisibility(View.GONE);
                            holder.line1.setTextColor(Color.RED);
                            holder.line5.setText("Status: Not Returned\nExpected: " + dataSnapshot.child("Event").child(card.event)
                                    .child("end").getValue().toString());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });


        holder.btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getDefault());
                String currentDateAndTime = sdf.format(new Date());

                DatabaseReference miDatabase = mDatabase.child("Inventory");
                InventoryItem iItem = new InventoryItem(card.id, card.item, card.event, card.time,
                        currentDateAndTime);
                Map<String, Object> postValues = iItem.toMap();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(card.key, postValues);
                miDatabase.updateChildren(childUpdates);

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.line3.getVisibility() == View.GONE) {
                    holder.line3.setVisibility(View.VISIBLE);
                    holder.line4.setVisibility(View.VISIBLE);
                    holder.line5.setVisibility(View.VISIBLE);
                } else {
                    holder.line3.setVisibility(View.GONE);
                    holder.line4.setVisibility(View.GONE);
                    holder.line5.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

}

