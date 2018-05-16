package com.ericksilva.cuacuapp.models;

import android.support.annotation.NonNull;

import java.util.Map;

/**
 * Created by ericksilva on 5/16/18.
 */

public abstract class Model {

    @NonNull
    public abstract Map<String,Object> getMap();

}
