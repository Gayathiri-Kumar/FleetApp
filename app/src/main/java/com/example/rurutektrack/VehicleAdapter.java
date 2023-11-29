package com.example.rurutektrack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.ViewHolder> {

        Context context;
        List<Vehiclemodel> VehicleList;

public VehicleAdapter(Context context, @Nullable List<Vehiclemodel> VehicleList) {
        this.context = context;
        this.VehicleList = VehicleList != null ? VehicleList : new ArrayList<>();
        }

@NonNull
@Override
public VehicleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vehicle, parent, false);
        return new VehicleAdapter.ViewHolder(view);
        }

@Override
public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vehiclemodel model = VehicleList.get(position);
        if (model != null) {
        holder.vehicle.setText(model.getVehicle());
        }
        }

@Override
public int getItemCount() {
        return VehicleList.size();
        }

public void setData(@NotNull List<? extends Vehiclemodel> Vehiclemodel) {
        this.VehicleList = (List<Vehiclemodel>) Vehiclemodel;
        notifyDataSetChanged();
        }

public class ViewHolder extends RecyclerView.ViewHolder {
    TextView vehicle;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        vehicle = itemView.findViewById(R.id.vname);


    }
}
}
