package com.example.bustrackerpassenger.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bustrackerpassenger.R;
import com.example.bustrackerpassenger.models.Bus;

import java.util.ArrayList;
import java.util.List;

public class BusInfoAdapter extends RecyclerView.Adapter<BusInfoAdapter.BusViewHolder> {

    private List<Bus> busList;
    private OnBusClickListener listener;

    public interface OnBusClickListener {
        void onBusClick(Bus bus);
    }

    public BusInfoAdapter(OnBusClickListener listener) {
        this.busList = new ArrayList<>();
        this.listener = listener;
    }

    public void setBusList(List<Bus> busList) {
        this.busList = busList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new BusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BusViewHolder holder, int position) {
        Bus bus = busList.get(position);
        holder.bind(bus, listener);
    }

    @Override
    public int getItemCount() {
        return busList.size();
    }

    static class BusViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public BusViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }

        public void bind(Bus bus, OnBusClickListener listener) {
            text1.setText(bus.getPlateNumber() + " - " + bus.getBusClass());
            text2.setText(bus.getRoute());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBusClick(bus);
                }
            });
        }
    }
}