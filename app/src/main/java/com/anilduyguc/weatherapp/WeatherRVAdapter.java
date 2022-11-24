package com.anilduyguc.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherRVAdapter extends RecyclerView.Adapter<WeatherRVAdapter.ViewHolder> {
    private Context context;
    private ArrayList<WeatherRVModel> weatherRVModels;

    public WeatherRVAdapter(Context context, ArrayList<WeatherRVModel> weatherRVModels) {
        this.context = context;
        this.weatherRVModels = weatherRVModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeatherRVModel model = weatherRVModels.get(position);
        holder.temperatureTextView.setText(model.getTemperature() + " °C");
        Picasso.get().load("http://".concat(model.getIcon())).into(holder.conditionImageView);
        holder.windTextView.setText(model.getWindSpeed() + " km/h");
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
        try {
            Date date = input.parse(model.getTime());
            holder.timeTextView.setText(output.format(date));
        } catch (ParseException exception){
            exception.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return weatherRVModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView windTextView, temperatureTextView, timeTextView;
        private ImageView conditionImageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            windTextView = itemView.findViewById(R.id.idTVWindSpeed);
            temperatureTextView = itemView.findViewById(R.id.idTVTemperature);
            timeTextView = itemView.findViewById(R.id.idTVTime);
            conditionImageView = itemView.findViewById(R.id.idIVCondition);
        }
    }
}
