package com.ericksilva.cuacuapp.activities.dashboard;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ericksilva.cuacuapp.R;
import com.ericksilva.cuacuapp.models.Cuac;

import java.util.List;


/**
 * Created by ericksilva on 5/15/18.
 */

public class CuacsAdapter extends RecyclerView.Adapter<CuacsAdapter.CuacHolder> {

    private List<Cuac>  cuacs;

    public CuacsAdapter(List<Cuac> cuacs) {
        this.cuacs = cuacs;
    }

    //RECYCLE METHODS
    @NonNull
    @Override
    public CuacHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View item = inflater.inflate(R.layout.cuac_item,parent,false);
        return new CuacHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull CuacHolder holder, int position) {
        Cuac cuac = cuacs.get(position);
        holder.lblName.setText(cuac.name);
    }

    @Override
    public int getItemCount() {
        return cuacs.size();
    }

    //VIEW HOLDER
    public class CuacHolder extends RecyclerView.ViewHolder{

        public TextView lblName;

        public CuacHolder(View itemView) {
            super(itemView);
            lblName = itemView.findViewById(R.id.lblname);
        }
    }
}
