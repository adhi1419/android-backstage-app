package com.adhi.backstage.com.adhi.backstage.cardlist;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.adhi.backstage.R;
import com.adhi.backstage.com.adhi.backstage.fragments.Members;

import java.util.ArrayList;
import java.util.List;

public class MemberCardArrayAdapter extends RecyclerView.Adapter<MemberCardArrayAdapter.MyViewHolder> {
    private static final String TAG = "MemberCardArrayAdapter";
    private List<User> cardList = new ArrayList<>();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView line1;
        TextView line2;
        ImageView avatar;

        public MyViewHolder(View view) {
            super(view);
            line1 = (TextView) view.findViewById(R.id.list_member_name);
            line2 = (TextView) view.findViewById(R.id.list_member_role);
            avatar = (ImageView) view.findViewById(R.id.list_member_avatar);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_card_member, parent, false);

        return new MyViewHolder(itemView);
    }

    public MemberCardArrayAdapter(List<User> cardList) {
        this.cardList = cardList;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final User card = cardList.get(position);
        holder.line1.setText(card.getName());
        holder.line2.setText(Members.getRoleByVal(card.getRole()));
        int avatarResId = R.drawable.avatar_0;
        switch (card.getAvatar()) {
            case "0":
                avatarResId = R.drawable.avatar_0;
                break;
            case "1":
                avatarResId = R.drawable.avatar_1;
                break;
            case "2":
                avatarResId = R.drawable.avatar_2;
                break;
            case "3":
                avatarResId = R.drawable.avatar_3;
                break;
        }
        holder.avatar.setImageResource(avatarResId);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                intent.putExtra(ContactsContract.Intents.Insert.NAME, card.getName() + " (Backstage)")
                        .putExtra(ContactsContract.Intents.Insert.EMAIL, card.getID() + "@goa.bits-pilani.ac.in")
                        .putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                        .putExtra(ContactsContract.Intents.Insert.PHONE, card.getPhone())
                        .putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MAIN);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }
}

