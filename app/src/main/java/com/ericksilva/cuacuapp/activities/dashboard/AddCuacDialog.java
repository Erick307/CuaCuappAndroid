package com.ericksilva.cuacuapp.activities.dashboard;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
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
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Date;

public class AddCuacDialog {


    private OnAddCuacListener mListener;

    private Activity mActivity;
    private GoogleMap mMap;
    private SeekBar sbZoom;

    private Dialog dialog;
    private Dialog dialogGeo;
    private Dialog dialogSpecific;
    private Dialog dialogRecurrent;

    private EditText txtTitle;
    private TimePicker timePicker;

    private Cuac mNewCuac = new Cuac();
    private ArrayList<String> mDays = new ArrayList<>();

    public  void setAddCuacListener(OnAddCuacListener onAddCuacListener){
        mListener = onAddCuacListener;
    }

    public void show(Activity activity){
        mActivity = activity;
        showTypeDialog();
    }

    private void showTypeDialog(){
        dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_add_cuac);
        dialog.setOnCancelListener(onCancelListener);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.show();

        dialog.findViewById(R.id.btn_geo).setOnClickListener(onClickTypeListener);
        dialog.findViewById(R.id.btn_specific).setOnClickListener(onClickTypeListener);
        dialog.findViewById(R.id.btn_recurrent).setOnClickListener(onClickTypeListener);

        txtTitle = dialog.findViewById(R.id.txt_title);
    }

    private void showGeoDialog(){
        dialogGeo = new Dialog(mActivity);
        dialogGeo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogGeo.setCancelable(true);
        dialogGeo.setContentView(R.layout.dialog_add_geo_cuac);
        dialogGeo.setOnCancelListener(onCancelListener);

        Window w = dialogGeo.getWindow();
        w.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        MapView mMapView = (MapView) dialogGeo.findViewById(R.id.map);
        MapsInitializer.initialize(mActivity);
        mMapView.onCreate(dialogGeo.onSaveInstanceState());
        mMapView.onResume();
        mMapView.getMapAsync(onMapReadyCallback);

        sbZoom = dialogGeo.findViewById(R.id.zoom_seek_bar);

        dialogGeo.findViewById(R.id.btn_save).setOnClickListener(onAddGeoClickListener);
        dialogGeo.show();
    }

    private void showSpecificDialog(){

        dialogSpecific = new Dialog(mActivity);
        dialogSpecific.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogSpecific.setCancelable(true);
        dialogSpecific.setContentView(R.layout.dialog_add_specific_cuac);
        dialogSpecific.setOnCancelListener(onCancelListener);

        Window w = dialogSpecific.getWindow();
        w.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        dialogSpecific.findViewById(R.id.btn_save).setOnClickListener(onAddSpecificClickListener);
        dialogSpecific.show();
    }

    private void showRecurrentDialog(){

        dialogRecurrent = new Dialog(mActivity);
        dialogRecurrent.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogRecurrent.setCancelable(true);
        dialogRecurrent.setContentView(R.layout.dialog_add_daily_cuac);
        dialogRecurrent.setOnCancelListener(onCancelListener);

        Window w = dialogRecurrent.getWindow();
        w.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        dialogRecurrent.findViewById(R.id.btn_sunday).setOnClickListener(onDayListener);
        dialogRecurrent.findViewById(R.id.btn_monday).setOnClickListener(onDayListener);
        dialogRecurrent.findViewById(R.id.btn_tuesday).setOnClickListener(onDayListener);
        dialogRecurrent.findViewById(R.id.btn_wednesday).setOnClickListener(onDayListener);
        dialogRecurrent.findViewById(R.id.btn_thursday).setOnClickListener(onDayListener);
        dialogRecurrent.findViewById(R.id.btn_friday).setOnClickListener(onDayListener);
        dialogRecurrent.findViewById(R.id.btn_saturday).setOnClickListener(onDayListener);

        timePicker = dialogRecurrent.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);

        dialogRecurrent.findViewById(R.id.btn_save).setOnClickListener(onAddRecurrentClickListener);
        dialogRecurrent.show();
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

    private DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialogInterface) {

        }
    };


    private View.OnClickListener onAddGeoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            mNewCuac.power = 2;
            mNewCuac.point = new GeoPoint(mMap.getCameraPosition().target.latitude,mMap.getCameraPosition().target.longitude);
            mNewCuac.radius = sbZoom.getProgress();
            mListener.addCuac(mNewCuac);

            dialogGeo.setOnCancelListener(null);
            dialogGeo.cancel();
            dialogGeo.dismiss();
        }
    };

    private View.OnClickListener onAddSpecificClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            mNewCuac.power = 2;
            mListener.addCuac(mNewCuac);

            dialogGeo.setOnCancelListener(null);
            dialogGeo.cancel();
            dialogGeo.dismiss();
        }
    };

    private View.OnClickListener onAddRecurrentClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            mNewCuac.power = 2;
            mNewCuac.hour = timePicker.getCurrentHour();
            mNewCuac.minute = timePicker.getCurrentMinute();
            mListener.addCuac(mNewCuac);

            dialogRecurrent.setOnCancelListener(null);
            dialogRecurrent.cancel();
            dialogRecurrent.dismiss();
        }
    };

    private View.OnClickListener onClickTypeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            String type = "";
            switch (view.getId()){
                case R.id.btn_geo:
                    type = "geo";
                    showGeoDialog();
                    break;
                case R.id.btn_recurrent:
                    type = "recurrent";
                    showRecurrentDialog();
                    break;
                case R.id.btn_specific:
                    type = "specific";
                    showSpecificDialog();
                    break;
            }

            mNewCuac.type = type;
            mNewCuac.name = txtTitle.getText().toString();

            dialog.setOnCancelListener(null);
            dialog.cancel();
            dialog.dismiss();
        }
    };

    private View.OnClickListener onDayListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            String day = (String) view.getTag();
            if (mNewCuac.days != null && mNewCuac.days.contains(day)){
                //agregar y seleccionar
                mNewCuac.days = mNewCuac.days.replace(day,"");
                view.setBackgroundResource(R.drawable.circle_not_selected);
                ((TextView)view).setTextColor(mActivity.getResources().getColor(R.color.colorPrimaryDark));
            }else{
                //borrar
                mNewCuac.days = (mNewCuac.days!=null?mNewCuac.days:"") + day;
                view.setBackgroundResource(R.drawable.circle_selected);
                ((TextView)view).setTextColor(mActivity.getResources().getColor(R.color.textColor));
            }
        }
    };

    public interface OnAddCuacListener {
        void addCuac(Cuac cuac);
    }
}
