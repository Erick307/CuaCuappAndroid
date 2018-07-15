package com.ericksilva.cuacuapp.activities.dashboard;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.ericksilva.cuacuapp.R;
import com.ericksilva.cuacuapp.activities.cuacdetail.CuacDetailActivity;
import com.ericksilva.cuacuapp.models.Cuac;
import com.ericksilva.cuacuapp.services.TrackerService;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CuacListActivity extends AppCompatActivity {

    private static final int REQUEST_FINE_LOCATION = 1;

    private List<Cuac> cuacList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CuacsAdapter cuacsAdapter;
    private CollectionReference cuacsRef;
    private ListenerRegistration cuacListenerRegistration;
    private FloatingActionButton btnAdd;

    private AddCuacDialog dialog;
    private View shadow;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static Intent createIntent(Context context){
        Intent intent = new Intent(context,CuacListActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuac_list);

        recyclerView = findViewById(R.id.recycler_view);

        btnAdd = findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(onClickAddListener);

        cuacsAdapter = new CuacsAdapter(cuacList,onClickSelectCuacListener);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cuacsAdapter);

        shadow = findViewById(R.id.shadow);

        loadPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_FINE_LOCATION);
//        startService(new Intent(CuacListActivity.this, TrackerService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cuacsRef = db.collection("users/"+ uid +"/cuacs");
        cuacListenerRegistration = cuacsRef.addSnapshotListener(this, eventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        cuacListenerRegistration.remove();
    }

    EventListener eventListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

            cuacList.clear();

            if (e!= null){
                Log.e("FireError",e.getLocalizedMessage());
            }

            if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) return;

            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()){
                Cuac cuac = new Cuac(documentSnapshot);
                cuac.key(documentSnapshot.getId());
                cuacList.add(cuac);
            }
            cuacsAdapter.notifyDataSetChanged();
        }
    };

    View.OnClickListener onClickSelectCuacListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int itemPosition = recyclerView.getChildLayoutPosition(view);
            Cuac cuac = cuacList.get(itemPosition);
            Intent intent = CuacDetailActivity.createIntent(CuacListActivity.this,cuac);
            startActivity(intent);
        }
    };

    View.OnClickListener onClickAddListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

//            shadow.setVisibility(View.VISIBLE);
            dialog = new AddCuacDialog();
            dialog.show(CuacListActivity.this);
            dialog.setAddCuacListener(addCuacListener);

        }
    };

    AddCuacDialog.OnAddCuacListener addCuacListener = new AddCuacDialog.OnAddCuacListener() {
        @Override
        public void addCuac(Cuac cuac) {
            cuacsRef.add(cuac.getMap());
//            shadow.setVisibility(View.GONE);
        }
    };

//    DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
//        @Override
//        public void onCancel(DialogInterface dialogInterface) {
//            shadow.setVisibility(View.GONE);
//        }
//    };


    //PERMISSIONS
    private void loadPermissions(String[] perm,int requestCode) {
        if (ContextCompat.checkSelfPermission(this, perm[0]) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, perm[0])) {
                ActivityCompat.requestPermissions(this, perm,requestCode);
                return;
            }
        }
        startService(new Intent(CuacListActivity.this, TrackerService.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService(new Intent(CuacListActivity.this, TrackerService.class));
                } else {
                }
            }
        }
    }
}

