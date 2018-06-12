package com.ericksilva.cuacuapp.activities.dashboard;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.ericksilva.cuacuapp.R;
import com.ericksilva.cuacuapp.models.Cuac;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

public class AddCuacDialog {

    Dialog dialog;
    private OnAddCuacListener mListener;

    private  GoogleMap mMap;
    private SeekBar sbZoom;

    public  void setAddCuacListener(OnAddCuacListener onAddCuacListener){
        mListener = onAddCuacListener;
    }

    public void showDialog(Activity activity, DialogInterface.OnCancelListener onCancelListener){
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_add_cuac);
        dialog.setOnCancelListener(onCancelListener);

        Window w = dialog.getWindow();
        w.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        MapView mMapView = (MapView) dialog.findViewById(R.id.map);
        MapsInitializer.initialize(activity);
        mMapView.onCreate(dialog.onSaveInstanceState());
        mMapView.onResume();
        mMapView.getMapAsync(onMapReadyCallback);

        sbZoom = dialog.findViewById(R.id.zoom_seek_bar);

        dialog.findViewById(R.id.btn_save).setOnClickListener(onClickListener);

        dialog.show();
    }


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

    private OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
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


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Cuac cuac = new Cuac();
            cuac.name = "geoTest";
            cuac.type = "geo";
            cuac.power = 2;
            cuac.point = new GeoPoint(mMap.getCameraPosition().target.latitude,mMap.getCameraPosition().target.longitude);
            cuac.radius = sbZoom.getProgress();

            mListener.addCuac(cuac);

            dialog.setOnCancelListener(null);
            dialog.cancel();
            dialog.dismiss();
        }
    };


    public interface OnAddCuacListener {
        void addCuac(Cuac cuac);
    }
}
