package com.example.ferpiguardsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.ferpiguardsystem.Adapters.KeyHistoryActivityParentAdapter;
import com.example.ferpiguardsystem.Model.KeyHistoryActivityChildModel;
import com.example.ferpiguardsystem.Model.KeyHistoryActivityParentModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class KeyHistoryActivity  extends AppCompatActivity {
    private ArrayList<KeyHistoryActivityParentModel> orderPendingListFragmentModel;

    private KeyHistoryActivityParentAdapter orderPendingListFragmentAdapter;
    private RecyclerView orderListRecyclerView;
    private ImageView actions, back_button;
    FirebaseFirestore db;
    CollectionReference keyTakenCollection;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_history);
        db = FirebaseFirestore.getInstance();
        keyTakenCollection = db.collection("KeyTakenGiven");
        db = FirebaseFirestore.getInstance();
        back_button=findViewById(R.id.button4);
        back_button.setOnClickListener(v -> {
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setContentText("Dasturdan chiqasizmi?" )
                    .setCancelText("Yo'q")
                    .setConfirmText("Ha")
                    .showCancelButton(true)
                    .setCancelClickListener(SweetAlertDialog::cancel)
                    .setConfirmClickListener(sweetAlertDialog1 -> {
                        sweetAlertDialog1.cancel();
                        this.finishAffinity();
                    })
                    .show();
        });
// CATEGORY!!!!!!!!!!!!!
        orderListRecyclerView = (RecyclerView) findViewById(R.id.userLisRecyclerView);
        orderPendingListFragmentModel = new ArrayList<>();
        orderListRecyclerView.setHasFixedSize(true);
        orderListRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        orderPendingListFragmentAdapter = new KeyHistoryActivityParentAdapter(orderPendingListFragmentModel, this);
        orderListRecyclerView.setAdapter(orderPendingListFragmentAdapter);
        actions = findViewById(R.id.button2);
        actions.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(KeyHistoryActivity.this);
            builder.setTitle("Tanlang:");

            // add a checkbox list
            String[] names= {"Aniqlash","Shaxs qo'shish","Shaxslar ro'yxati"};

            builder.setItems(names, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    switch (which)
                    {
                        case 0:
                            Intent intent3 = new Intent(KeyHistoryActivity.this, MainActivity.class);
                            intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent3);
                            break;
                        case 1:
                            Intent intent = new Intent(KeyHistoryActivity.this, AddUserActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            break;
                        case 2:
                            Intent intent2 = new Intent(KeyHistoryActivity.this, UserListActivity.class);
                            intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent2);
                            break;

                    }

                }
            });
            builder.setNegativeButton("Cancel", null);

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        loadOrdersData();
    }

    private void loadOrdersData() {
        keyTakenCollection.addSnapshotListener((value, error) -> {
            if (error!=null){
                String Tag = null;
                Log.e(Tag,"onEvent", error);
                return;
            }
            if (value != null && !value.isEmpty()){
                orderPendingListFragmentModel.clear();

                for (QueryDocumentSnapshot d:value){
                    KeyHistoryActivityParentModel dataModal = new KeyHistoryActivityParentModel();
                    HashMap<String,Object> dd = (HashMap<String, Object>) d.getData();

                    ArrayList<KeyHistoryActivityChildModel> innerModel = new ArrayList<>();
                    for(Map.Entry<String, Object> entry:dd.entrySet()){
                        KeyHistoryActivityChildModel dataModal2 = new KeyHistoryActivityChildModel();
                        dataModal2.setTeacherName(entry.getKey());
                        Map<String, Timestamp> map2 = (Map<String, Timestamp>) entry.getValue();
                        for(Map.Entry<String, Timestamp> entry2:map2.entrySet()){
                            if(Objects.equals(entry2.getKey().toString(), "givenTime")){
                                dataModal2.setGivenTime((Timestamp) entry2.getValue());
                            }
                            if(Objects.equals(entry2.getKey().toString(), "takenTime")){
                                dataModal2.setTakenTime((Timestamp) entry2.getValue());
                            }
                        }
                        innerModel.add(dataModal2);
                    }
                    dataModal.setParentItemTitle(d.getId());
                    dataModal.setChildItemList(innerModel);
                    orderPendingListFragmentModel.add(dataModal);
                    System.out.println(dd.entrySet());
                    orderPendingListFragmentAdapter.notifyDataSetChanged();
                }
            }else {
                orderPendingListFragmentModel.clear();
                String Tag = null;
                Log.e(Tag,"Xatolik bor!!!!");
                orderPendingListFragmentAdapter.notifyDataSetChanged();
            }

        });
    }
}
