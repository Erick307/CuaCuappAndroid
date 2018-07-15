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

public class CuacDetailActivity extends AppCompatActivity {

    private GoogleMap mMap;
    private SeekBar sbZoom;
    private TextView lblTitle;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference cuacRef;
    private ListenerRegistration cuacListenerRegistration;
    private Cuac cuac = new Cuac();

    public static Intent createIntent(Context context, Cuac cuac){
        Intent intent = new Intent(context,CuacDetailActivity.class);
        intent.putExtra("cuacId",cuac.key());
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuac_detail);

        MapView mMapView = (MapView) findViewById(R.id.map);
        MapsInitializer.initialize(this);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(onMapReadyCallback);

        sbZoom = findViewById(R.id.zoom_seek_bar);
        lblTitle = findViewById(R.id.lbl_title);
        findViewById(R.id.btn_save).setOnClickListener(onSaveClickListener);
        findViewById(R.id.btn_delete).setOnClickListener(onDeleteClickListener);

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        cuac.key(extras.getString("cuacId"));
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

    View.OnClickListener onSaveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            cuac.power = 2;
            cuac.point = new GeoPoint(mMap.getCameraPosition().target.latitude,mMap.getCameraPosition().target.longitude);
            cuac.radius = sbZoom.getProgress();
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

        sbZoom.setProgress((int) cuac.radius);
        if(mMap != null){
            LatLng latLng = new LatLng(cuac.point.getLatitude(),cuac.point.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18 - (float)cuac.radius/(float)100));
        }

        lblTitle.setText(cuac.name);
    }
}
