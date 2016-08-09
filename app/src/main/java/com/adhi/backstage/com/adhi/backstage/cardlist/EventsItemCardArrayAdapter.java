package com.adhi.backstage.com.adhi.backstage.cardlist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adhi.backstage.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class EventsItemCardArrayAdapter extends RecyclerView.Adapter<EventsItemCardArrayAdapter.MyViewHolder> {
    private List<EventsItem> cardList = new ArrayList<>();
    private DatabaseReference mDatabase;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView line1;
        TextView line2;
        TextView line3;
        TextView line4;

        public MyViewHolder(View view) {
            super(view);
            line1 = (TextView) view.findViewById(R.id.list_events_name);
            line2 = (TextView) view.findViewById(R.id.list_events_start);
            line3 = (TextView) view.findViewById(R.id.list_events_end);
            line4 = (TextView) view.findViewById(R.id.list_events_place);
        }
    }

    @Override
    public EventsItemCardArrayAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_card_events, parent, false);

        return new EventsItemCardArrayAdapter.MyViewHolder(itemView);
    }

    public EventsItemCardArrayAdapter(List<EventsItem> cardList) {
        this.cardList = cardList;
    }

    @Override
    public void onBindViewHolder(final EventsItemCardArrayAdapter.MyViewHolder holder, int position) {
        final EventsItem card = cardList.get(position);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        holder.line1.setText(card.name);
        holder.line2.setText("Scheduled Start: " + card.start);
        holder.line3.setText("Scheduled End: " + card.end);
        holder.line4.setText("Venue: " + card.place);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.line3.getVisibility() == View.GONE) {
                    holder.line3.setVisibility(View.VISIBLE);
                    holder.line4.setVisibility(View.VISIBLE);
                } else {
                    holder.line3.setVisibility(View.GONE);
                    holder.line4.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

}

