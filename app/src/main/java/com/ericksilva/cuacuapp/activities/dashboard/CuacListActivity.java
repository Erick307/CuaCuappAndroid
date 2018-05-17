package com.ericksilva.cuacuapp.activities.dashboard;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.ericksilva.cuacuapp.R;
import com.ericksilva.cuacuapp.activities.MainActivity;
import com.ericksilva.cuacuapp.models.Cuac;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CuacListActivity extends AppCompatActivity {

    private List<Cuac> cuacList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CuacsAdapter cuacsAdapter;
    private CollectionReference cuacsRef;
    private FloatingActionButton btnAdd;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static Intent createIntent(Context context){
        Intent intent = new Intent(context,MainActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuac_list);

        recyclerView = findViewById(R.id.recycler_view);

        btnAdd = findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(onClickAddListener);

        cuacsAdapter = new CuacsAdapter(cuacList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cuacsAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cuacsRef = db.collection("users/"+ uid +"/cuacs");
        cuacsRef.addSnapshotListener(this, eventListener);
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
                cuacList.add(cuac);
            }
            cuacsAdapter.notifyDataSetChanged();
        }
    };

    View.OnClickListener onClickAddListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Cuac cuac = new Cuac();
            cuac.name = "Alto cuac";
            cuac.type = "geo";
            cuac.power = 2;
            cuac.point = new GeoPoint(-34.9112362,-58.5719947);
            cuacsRef.add(cuac.getMap());

        }
    };
}