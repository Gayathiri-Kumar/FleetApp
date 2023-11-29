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

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    Context context;
    List<Datamodel> DataList;

    public DataAdapter(Context context, @Nullable List<Datamodel> DataList) {
        this.context = context;
        this.DataList = DataList != null ? DataList : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Datamodel model = DataList.get(position);
        if (model != null) {
            holder.si.setText(model.getSi());
            holder.name.setText(model.getName());
            holder.simg.setText(model.getSimg());
            holder.eimg.setText(model.getEImg());
            holder.plc.setText(model.getPlc());
        }
    }

    @Override
    public int getItemCount() {
        return DataList.size();
    }

    public void setData(@NotNull List<? extends Datamodel> datamodel) {
        this.DataList = (List<Datamodel>) datamodel;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, si, eimg, simg, plc, tkm;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            si = itemView.findViewById(R.id.si);
            eimg = itemView.findViewById(R.id.eimg);
            simg = itemView.findViewById(R.id.simg);
            plc = itemView.findViewById(R.id.plc);
        }
    }
}
