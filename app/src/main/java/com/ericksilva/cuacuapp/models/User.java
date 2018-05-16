package com.ericksilva.cuacuapp.models;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ericksilva on 5/15/18.
 */

public class User extends Model {

    public String name;
    public String email;
    public long level;

    public User(String name, String email, long level){
        this.name = name;
        this.email = email;
        this.level = level;
    }

    public User(DocumentSnapshot document){
        name  = document.getString("name");
        email = document.getString("email");
        level = document.getLong("level") != null ? document.getLong("level") : 0;
    }


    @NonNull
    public Map<String,Object> getMap(){
        Map<String, Object> map = new HashMap<>();
        if (name  != null) map.put("name", name);
        if (email != null) map.put("email", email);
        if (level != 0) map.put("level", level);
        return map;
    }

}
