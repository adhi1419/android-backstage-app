package com.adhi.backstage.com.adhi.backstage.cardlist;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class DatabaseItem {
    public String name, desc, type;

    public DatabaseItem() {

    }

    public DatabaseItem(String name, String desc, String type) {
        this.name = name;
        this.desc = desc;
        this.type = type;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("desc", desc);
        result.put("type", type);
        return result;
    }

}
