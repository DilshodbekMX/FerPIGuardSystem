package com.example.ferpiguardsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.ferpiguardsystem.Adapters.UserListAdapter;
import com.example.ferpiguardsystem.Model.TeachersModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class UserListActivity extends AppCompatActivity {
    SearchView searchView;
    ImageView back_button, actions;

    private ArrayList<TeachersModel> adminUserFragmentAddUserModel;

    private UserListAdapter adminUserFragmentUserListAdapter;
    private RecyclerView userLisRecyclerView;

    FirebaseFirestore db;
    CollectionReference users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        userLisRecyclerView = findViewById(R.id.userLisRecyclerView);
        adminUserFragmentAddUserModel = new ArrayList<>();
        userLisRecyclerView.setHasFixedSize(true);
        userLisRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        users = db.collection("Teachers");
        adminUserFragmentUserListAdapter = new UserListAdapter(adminUserFragmentAddUserModel, this);
        userLisRecyclerView.setAdapter(adminUserFragmentUserListAdapter);
        adminUserFragmentUserListAdapter.setOnItemClickListener(position -> new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Eslatma")
                .setContentText("Foydalanuvchiga tegishli barcha ma'lumotlar ham o'chirib yuboriladi. Rozimisiz?")
                .setCancelText("Yo'q")
                .setConfirmText("Ha")
                .setCancelButtonBackgroundColor(Color.CYAN)
                .showCancelButton(true)
                .setCancelClickListener(SweetAlertDialog::cancel)
                .setConfirmButtonBackgroundColor(Color.RED)
                .setConfirmClickListener(sweetAlertDialog1 -> {

                    DocumentReference docRef = users.document(adminUserFragmentAddUserModel.get(position).getTeacherId());
                    docRef.get().addOnSuccessListener(d -> {
                        if(d.exists()){
                            docRef .delete()
                                    .addOnSuccessListener(unused -> {
                                        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                                .setContentText("Muvafaqqiyatli o'chirildi!")
                                                .setConfirmText("OK!")
                                                .setConfirmClickListener(SweetAlertDialog::cancel)
                                                .show();
                                    })
                                    .addOnFailureListener(e -> {
                                        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                                .setContentText("Jarayonda xatolik!")
                                                .setConfirmText("OK!")
                                                .setConfirmClickListener(SweetAlertDialog::cancel)
                                                .show();

                                    });
                        }
                        else{
                            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                    .setContentText("Jarayonda xatolik!")
                                    .setConfirmText("OK!")
                                    .setConfirmClickListener(SweetAlertDialog::cancel)
                                    .show();
                        }
                    });
                    sweetAlertDialog1.cancel();
                })
                .show());

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                filterList(s);
                return false;
            }
        });
        actions = findViewById(R.id.button2);
        actions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserListActivity.this);
                builder.setTitle("Tanlang:");

                // add a checkbox list
                String[] names= {"Aniqlash","Shaxs qo'shish","Shaxslar ro'yxati"};

                builder.setItems(names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which)
                        {
                            case 0:
                                Intent intent = new Intent(UserListActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                break;
                            case 1:
                                Intent intent2 = new Intent(UserListActivity.this, AddUserActivity.class);
                                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent2);
                                break;
                            case 2:

                                Intent intent3 = new Intent(UserListActivity.this, UserListActivity.class);
                                intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent3);
                                break;
                        }

                    }
                });
                builder.setNegativeButton("Cancel", null);

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

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
        loadUsersList();
    }

    private void loadUsersList() {
        users.addSnapshotListener((value, error) -> {
            if (error!=null){
                String Tag = null;
                Log.e(Tag,"onEvent", error);
                return;
            }
            if (value != null && !value.isEmpty()){
                adminUserFragmentAddUserModel.clear();

                for (QueryDocumentSnapshot d:value){
                    TeachersModel dataModal = d.toObject(TeachersModel.class);
                    adminUserFragmentAddUserModel.add(dataModal);
                    adminUserFragmentUserListAdapter.notifyDataSetChanged();
                }
            }else {
                adminUserFragmentAddUserModel.clear();
                String Tag = null;
                Log.e(Tag,"Xatolik bor!!!!");
                adminUserFragmentUserListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void filterList(String s) {
        ArrayList<TeachersModel> filteredList = new ArrayList<>();

        for (TeachersModel model: adminUserFragmentAddUserModel){
            if (model.getTeacherName().toLowerCase().contains(s.toLowerCase().trim()) ||
                    model.getTeacherRoomKey().toLowerCase().contains(s.toLowerCase().trim())){
                filteredList.add(model);
            }

        }
        this.adminUserFragmentUserListAdapter.setFilteredList(filteredList);
        this.userLisRecyclerView.setAdapter(adminUserFragmentUserListAdapter);
    }
}