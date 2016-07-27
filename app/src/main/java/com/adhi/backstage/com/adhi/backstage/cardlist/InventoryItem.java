package com.adhi.backstage.com.adhi.backstage.cardlist;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class InventoryItem {
    public String id, item, time, event, status;
    public String key;

    public InventoryItem() {

    }

    public InventoryItem(String id, String item, String event, String time, String status) {
        this.id = id;
        this.item = item;
        this.event = event;
        this.time = time;
        this.status = status;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("item", item);
        result.put("event", event);
        result.put("time", time);
        result.put("status", status);
        return result;
    }

}
