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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ferpiguardsystem.MainActivity;
import com.example.ferpiguardsystem.Model.KeyHistoryActivityChildModel;
import com.example.ferpiguardsystem.Model.KeyHistoryActivityParentModel;
import com.example.ferpiguardsystem.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class KeyHistoryActivityParentAdapter extends RecyclerView.Adapter<KeyHistoryActivityParentAdapter.ViewHolder> {
    ArrayList<KeyHistoryActivityParentModel> homeModelArrayList;
    KeyHistoryActivityChildAdapter keyHistoryActivityChildAdapter;
    ArrayList<KeyHistoryActivityChildModel> keyHistoryActivityChildModels;
    Context context;

    public KeyHistoryActivityParentAdapter(ArrayList<KeyHistoryActivityParentModel> homeModelArrayList, Context context) {
        this.homeModelArrayList = homeModelArrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public KeyHistoryActivityParentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_key_history_parent_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull KeyHistoryActivityParentAdapter.ViewHolder holder, int position) {
        KeyHistoryActivityParentModel modal = homeModelArrayList.get(position);
        System.out.println(modal.getParentItemTitle());
        holder.parent_item_title.setText(modal.getParentItemTitle().toString());
        keyHistoryActivityChildModels = new ArrayList<>();
        holder.child_recyclerview.setHasFixedSize(true);
        holder.child_recyclerview.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false));
        keyHistoryActivityChildAdapter = new KeyHistoryActivityChildAdapter(keyHistoryActivityChildModels, context);
        holder.child_recyclerview.setAdapter(keyHistoryActivityChildAdapter);
        sendModelData(modal);
    }

    private void sendModelData(KeyHistoryActivityParentModel modal) {
        for(KeyHistoryActivityChildModel entry:modal.getChildItemList()){
            KeyHistoryActivityChildModel dataModal = entry;
            keyHistoryActivityChildModels.add(dataModal);
            keyHistoryActivityChildAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public int getItemCount() {
        return homeModelArrayList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView parent_item_title;
        RecyclerView child_recyclerview;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            parent_item_title = itemView.findViewById(R.id.parent_item_title);
            child_recyclerview = itemView.findViewById(R.id.child_recyclerview);
        }


    }
}
