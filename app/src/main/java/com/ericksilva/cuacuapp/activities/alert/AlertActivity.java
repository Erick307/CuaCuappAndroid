package com.ericksilva.cuacuapp.activities.alert;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Date;

public class AlertActivity extends AppCompatActivity {

    private Vibrator vib;
    private Ringtone ringtone;
    private MediaPlayer mediaPlayer;
    private GoogleMap mMap;
    private TextView lblTitle;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference cuacRef;
    private ListenerRegistration cuacListenerRegistration;
    private Cuac cuac = new Cuac();

    public static Intent createIntent(Context context, Cuac cuac){
        Intent intent = new Intent(context,AlertActivity.class);
        intent.putExtra("cuacId",cuac.key());
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuac_alert);

        MapView mMapView = (MapView) findViewById(R.id.map);
        MapsInitializer.initialize(this);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(onMapReadyCallback);

        lblTitle = findViewById(R.id.lbl_title);
        findViewById(R.id.btn_dismiss).setOnClickListener(onDismissClickListener);

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
        initSoundAndVibrateAlert();
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
        }
    };

    View.OnClickListener onDismissClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            cuac.lastCuac = new Date();
            cuacRef.update(cuac.getMap());

            if (ringtone != null) ringtone.stop();
            if (vib != null) vib.cancel();
            finish();
        }
    };

    private void updateView(){
        if (cuac.name == null) return;

        if(mMap != null){
            LatLng latLng = new LatLng(cuac.point.getLatitude(),cuac.point.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18 - (float)cuac.radius/(float)100));
        }

        lblTitle.setText(cuac.name);
    }

    private void initSoundAndVibrateAlert() {

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING),0);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        ringtone.play();

        if (vib == null) {
            vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
        vib.vibrate(10000);

//        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
//
//        if (mediaPlayer == null){
//            mediaPlayer = MediaPlayer.create(this,R.raw.beep);
//        }
//        if (mediaPlayer == null) return;
//        mediaPlayer.setLooping(true);
//        mediaPlayer.setWakeMode(AlertActivity.this.getBaseContext(), PowerManager.PARTIAL_WAKE_LOCK);
//        mediaPlayer.start();

    }
}
