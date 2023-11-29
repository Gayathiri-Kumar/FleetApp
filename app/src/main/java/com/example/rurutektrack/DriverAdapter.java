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

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.ViewHolder> {

    Context context;
    List<Drivermodel> DriverList;

    public DriverAdapter(Context context, @Nullable List<Drivermodel> DriverList) {
        this.context = context;
        this.DriverList = DriverList != null ? DriverList : new ArrayList<>();
    }

    @NonNull
    @Override
    public DriverAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.driver, parent, false);
        return new DriverAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Drivermodel model = DriverList.get(position);
        if (model != null) {
            holder.un.setText(model.getUn());
            holder.emp.setText(model.getEmp());
            holder.pwd.setText(model.getPwd());
        }
    }

    @Override
    public int getItemCount() {
        return DriverList.size();
    }

    public void setData(@NotNull List<? extends Drivermodel> Drivermodel) {
        this.DriverList = (List<Drivermodel>) Drivermodel;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView un, emp, pwd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            un = itemView.findViewById(R.id.un);
            emp = itemView.findViewById(R.id.emp);
            pwd = itemView.findViewById(R.id.pwd);

        }
    }
}
