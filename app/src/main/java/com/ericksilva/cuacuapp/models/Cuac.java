package com.ericksilva.cuacuapp.models;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ericksilva on 5/15/18.
 */

public class Cuac extends Model {

    private String key;

    public String   name;
    public String   type;
    public long     power;

    public Date     date;

    public GeoPoint point;
    public long     radius = 0;

    public Date     time;
    public String   days;

    public Date     lastCuac;

    public Cuac(){}

    public Cuac(DocumentSnapshot document){
        name    = document.getString("name");
        type    = document.getString("type");
        power   = document.getLong("power") != null ? document.getLong("power") : 0;

        lastCuac = document.getDate("lastCuac");
        date = document.getDate("date");

        point   = document.getGeoPoint("point");
        radius   = document.getLong("radius") != null ? document.getLong("radius") : 0;
        time = document.getDate("time");
        days = document.getString("days");
    }

    @NonNull
    public Map<String,Object> getMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("type", type);
        map.put("power", power);

        if(lastCuac != null) map.put("lastCuac",lastCuac);
        if(date != null) map.put("date", date);
        if(point != null) map.put("point", point);
        if(radius != 0) map.put("radius", radius);
        if(time != null) map.put("time", time);
        if(days != null) map.put("days", days);

        return map;
    }

    public String key(){
        return key;
    }

    public void key(String id){
        key = id;
    }
}
