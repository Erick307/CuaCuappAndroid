package com.ericksilva.cuacuapp.activities.cuacdetail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ericksilva.cuacuapp.R;
import com.ericksilva.cuacuapp.models.Cuac;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class CuacDetailActivity extends AppCompatActivity {

    private GoogleMap mMap;
    private SeekBar sbZoom;
    private TextView lblTitle;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference cuacRef;
    private ListenerRegistration cuacListenerRegistration;
    private Cuac cuac = new Cuac();

    private ArrayList<View> vDays;
    private TimePicker timePicker;

    public static Intent createIntent(Context context, Cuac cuac){
        Intent intent = new Intent(context,CuacDetailActivity.class);
        intent.putExtra("cuacId",cuac.key());
        intent.putExtra("type",cuac.type);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuac_detail);

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        cuac.key(extras.getString("cuacId"));
        cuac.type = extras.getString("type");

        if (cuac.type.equalsIgnoreCase("geo")){
            initGeoCuac(savedInstanceState);
        }else if(cuac.type.equalsIgnoreCase("recurrent")){
            initRecurrentCuac(savedInstanceState);
        }else if(cuac.type.equalsIgnoreCase("date")){
            initDateCuac(savedInstanceState);
        }

        lblTitle = findViewById(R.id.lbl_title);
        findViewById(R.id.btn_save).setOnClickListener(onSaveClickListener);
        findViewById(R.id.btn_delete).setOnClickListener(onDeleteClickListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cuacRef = db.document("users/"+ uid +"/cuacs/" + cuac.key());
        cuacListenerRegistration = cuacRef.addSnapshotListener(this,cuacListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        cuacListenerRegistration.remove();
    }

    private void initGeoCuac(Bundle savedInstanceState){
        MapView mMapView = (MapView) findViewById(R.id.map);
        MapsInitializer.initialize(this);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(onMapReadyCallback);

        findViewById(R.id.map_container).setVisibility(View.VISIBLE);

        sbZoom = findViewById(R.id.zoom_seek_bar);
        sbZoom.setVisibility(View.VISIBLE);
    }

    private void initRecurrentCuac(Bundle savedInstanceState){

        timePicker = findViewById(R.id.time_picker);
        timePicker.setVisibility(View.VISIBLE);
        timePicker.setIs24HourView(true);

        findViewById(R.id.lbl_days).setVisibility(View.VISIBLE);
        findViewById(R.id.days_container).setVisibility(View.VISIBLE);

        vDays = new ArrayList<>();
        vDays.add(findViewById(R.id.btn_sunday));
        vDays.add(findViewById(R.id.btn_monday));
        vDays.add(findViewById(R.id.btn_tuesday));
        vDays.add(findViewById(R.id.btn_wednesday));
        vDays.add(findViewById(R.id.btn_thursday));
        vDays.add(findViewById(R.id.btn_friday));
        vDays.add(findViewById(R.id.btn_saturday));

        for (View day : vDays){
            day.setOnClickListener(onDayListener);
        }
    }

    private void initDateCuac(Bundle savedInstanceState){

    }

    EventListener<DocumentSnapshot> cuacListener = new EventListener<DocumentSnapshot>() {
        @Override
        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
            if (e!= null){
                Log.e("FireError",e.getLocalizedMessage());
            }
            if (documentSnapshot == null) return;
            cuac = new Cuac(documentSnapshot);
            cuac.key(documentSnapshot.getId());
            updateView();
        }
    };

    OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap map) {
            mMap = map;
            mMap.setOnCameraMoveListener(onCameraMoveListener);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-34.589,-58.4387), 12.0f));
            sbZoom.setOnSeekBarChangeListener(seekBarChangeListene);
        }
    };

    private GoogleMap.OnCameraMoveListener onCameraMoveListener =new GoogleMap.OnCameraMoveListener() {
        @Override
        public void onCameraMove() {

            float zoom = mMap.getCameraPosition().zoom;
            Log.e("ZOOM","" + zoom + "set:"+ (int)(1800 - zoom * 100));
            sbZoom.setProgress((int)(1800 - zoom * 100));
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBarChangeListene = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            Log.e("Progress","" + (18 - (float)i/(float)100));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target,18 - (float)i/(float)100));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private View.OnClickListener onDayListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            String day = (String) view.getTag();
            if (cuac.days != null && cuac.days.contains(day)){
                cuac.days = cuac.days.replace(day,"");
                view.setBackgroundResource(R.drawable.circle_not_selected);
                ((TextView)view).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }else{
                cuac.days = (cuac.days!=null?cuac.days:"") + day;
                view.setBackgroundResource(R.drawable.circle_selected);
                ((TextView)view).setTextColor(getResources().getColor(R.color.textColor));
            }
        }
    };

    View.OnClickListener onSaveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            cuac.power = 2;

            if (cuac.type.equalsIgnoreCase("geo")) {
                cuac.point = new GeoPoint(mMap.getCameraPosition().target.latitude,mMap.getCameraPosition().target.longitude);
                cuac.radius = sbZoom.getProgress();
            }else if(cuac.type.equalsIgnoreCase("recurrent")){

                cuac.minute = timePicker.getCurrentMinute();
                cuac.hour = timePicker.getCurrentHour();
//                cuac.days Este no hace falta porque se actualiza apensa lo tocas

            }else if(cuac.type.equalsIgnoreCase("date")){

            }


            cuacRef.update(cuac.getMap());
            finish();
        }
    };

    View.OnClickListener onDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            cuacRef.delete();
            finish();
        }
    };


    private void updateView(){
        if (cuac.name == null) return;

        if (cuac.type.equalsIgnoreCase("geo")){
            updateGeoCuac();
        }else if(cuac.type.equalsIgnoreCase("recurrent")){
            updateRecurrentCuac();
        }else if(cuac.type.equalsIgnoreCase("date")){
            updateDateCuac();
        }

        lblTitle.setText(cuac.name);
    }

    private void updateGeoCuac(){
        sbZoom.setProgress((int) cuac.radius);
        if(mMap != null){
            LatLng latLng = new LatLng(cuac.point.getLatitude(),cuac.point.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18 - (float)cuac.radius/(float)100));
        }
    }

    private void updateRecurrentCuac(){

        timePicker.setCurrentHour((int)cuac.hour);
        timePicker.setCurrentMinute((int)cuac.minute);

        for (View view : vDays){
            String tag = (String)view.getTag();
            if (cuac.days.contains(tag)){
                view.setBackgroundResource(R.drawable.circle_selected);
                ((TextView)view).setTextColor(getResources().getColor(R.color.textColor));
            }else{
                view.setBackgroundResource(R.drawable.circle_not_selected);
                ((TextView)view).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }

        }
    }

    private void updateDateCuac(){

    }

}
