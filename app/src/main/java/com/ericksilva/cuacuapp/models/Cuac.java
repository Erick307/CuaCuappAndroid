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
    public long     radius;

    public Date     time;
    public String   days;

    public Cuac(){}

    public Cuac(DocumentSnapshot document){
        name    = document.getString("name");
        type    = document.getString("type");
        power   = document.getLong("power") != null ? document.getLong("power") : 0;
    }

    @NonNull
    public Map<String,Object> getMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("type", type);
        return map;
    }

}
