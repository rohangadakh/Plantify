// WeatherAdapter.java

package com.dev.plantify.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.plantify.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {
    private ArrayList<WeatherRvModel> weatherList;

    public WeatherAdapter(ArrayList<WeatherRvModel> weatherList) {
        this.weatherList = weatherList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weather, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeatherRvModel weather = weatherList.get(position);
        holder.timeTextView.setText(weather.getTime());
        holder.tempTextView.setText(weather.getTemperature());
        holder.chanceRainTextView.setText("Chances of Rain "+weather.getChanceRain());
        Picasso.get().load(weather.getIcon()).into(holder.weatherImageView);

        // Set other properties as needed
    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView, tempTextView, chanceRainTextView;
        CircleImageView weatherImageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.timeTextView); // Replace with your actual TextView IDs
            tempTextView = itemView.findViewById(R.id.tempTextView);
            chanceRainTextView = itemView.findViewById(R.id.chanceRainTextView);
            weatherImageView = itemView.findViewById(R.id.iv_image);
            // Initialize other views as needed
        }
    }
}
