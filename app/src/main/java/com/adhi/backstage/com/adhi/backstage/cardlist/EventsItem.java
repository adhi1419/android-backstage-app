package com.adhi.backstage.com.adhi.backstage.cardlist;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class EventsItem {
    public String name, start, end, place;
    public String key;

    public EventsItem() {

    }

    public EventsItem(String name, String start, String end, String place) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.place = place;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("start", start);
        result.put("end", end);
        result.put("place", place);
        return result;
    }

}
