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

    public String   name;
    public String   type;
    public long     power;

    public Date     date;

    public GeoPoint point;
    public long     radius = 0;

    public Date     time;
    public String   days;

    public Cuac(){}

    public Cuac(DocumentSnapshot document){
        name    = document.getString("name");
        type    = document.getString("type");
        power   = document.getLong("power") != null ? document.getLong("power") : 0;

        date = document.getDate("date") != null ? document.getDate("date") : new Date();

        point   = document.getGeoPoint("point") != null ? document.getGeoPoint("point") : new GeoPoint(0,0);
        radius   = document.getLong("radius") != null ? document.getLong("radius") : 0;

        time = document.getDate("time") != null ? document.getDate("time") : new Date();
        days = document.getString("days") != null ? document.getString("days") : "";
    }

    @NonNull
    public Map<String,Object> getMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("type", type);
        map.put("power", power);

        if(date != null) map.put("date", date);
        if(point != null) map.put("point", point);
        if(radius != 0) map.put("radius", radius);
        if(time != null) map.put("time", time);
        if(days != null) map.put("days", days);

        return map;
    }

}
