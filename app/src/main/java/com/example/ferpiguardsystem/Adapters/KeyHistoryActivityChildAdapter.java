package com.example.ferpiguardsystem.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ferpiguardsystem.Model.KeyHistoryActivityChildModel;
import com.example.ferpiguardsystem.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class KeyHistoryActivityChildAdapter extends RecyclerView.Adapter<KeyHistoryActivityChildAdapter.ViewHolder> {
    ArrayList<KeyHistoryActivityChildModel> homeModelArrayList;
    Context context;

    public KeyHistoryActivityChildAdapter(ArrayList<KeyHistoryActivityChildModel> homeModelArrayList, Context context) {
        this.homeModelArrayList = homeModelArrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public KeyHistoryActivityChildAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_key_history_child_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull KeyHistoryActivityChildAdapter.ViewHolder holder, int position) {
        KeyHistoryActivityChildModel modal = homeModelArrayList.get(position);
        String myFormat="HH:mm";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        Date takenTime = modal.getTakenTime().toDate();
        String takenTimeString = String.valueOf((dateFormat.format(takenTime)));
        holder.orderSendingListCustomerName.setText(modal.getTeacherName().toString());
        holder.keyTakenTime.setText(takenTimeString);
        if(modal.getGivenTime()!=null){
            Date givenTime = modal.getGivenTime().toDate();
            String givenTimeString = String.valueOf((dateFormat.format(givenTime)));
            holder.keyGivenTime.setText(givenTimeString);
        }else{
            holder.keyGivenTime.setText("Qaytarilmadi");
        }

    }

    @Override
    public int getItemCount() {
        return homeModelArrayList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView keyTakenTime,keyGivenTime, orderSendingListCustomerName, orderSendingListCustomerAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            keyGivenTime = itemView.findViewById(R.id.keyGivenTime);
            keyTakenTime = itemView.findViewById(R.id.keyTakenTime);
            orderSendingListCustomerName = itemView.findViewById(R.id.orderSendingListCustomerName);
            orderSendingListCustomerAddress = itemView.findViewById(R.id.orderSendingListCustomerAddress);
        }


    }
}
