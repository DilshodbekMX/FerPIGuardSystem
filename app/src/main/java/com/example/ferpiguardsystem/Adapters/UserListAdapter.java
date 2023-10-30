package com.example.ferpiguardsystem.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ferpiguardsystem.Model.TeachersModel;
import com.example.ferpiguardsystem.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {
    private ArrayList<TeachersModel> dataModalArrayList;
    private Context context;
    FirebaseFirestore db;
    private OnItemClickListener listener;
    private TextView orderListTotalPrice;

    // constructor class for our Adapter
    public UserListAdapter(ArrayList<TeachersModel> dataModalArrayList, Context context) {
        this.dataModalArrayList = dataModalArrayList;
        this.context = context;
    }
    public UserListAdapter() {
    }
    public void setFilteredList(ArrayList<TeachersModel> filteredList) {
        this.dataModalArrayList = filteredList;
    }

    @NonNull
    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // passing our layout file for displaying our card item
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.activity_user_list_item, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListAdapter.ViewHolder holder, int position) {
        // setting data to our views in Recycler view items.
        TeachersModel modal = dataModalArrayList.get(position);
        holder.userListName.setText(modal.getTeacherName());
        holder.userListPhone.setText(String.valueOf(modal.getTeacherRoomKey()));
        holder.userListUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserInfo(modal, holder);
            }
        });


    }

    private void updateUserInfo(TeachersModel modal, ViewHolder holder) {
        db = FirebaseFirestore.getInstance();
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.activity_user_list_item_dialog);

        //Initializing the views of the dialog.
        final EditText userListNameUpdateInput = dialog.findViewById(R.id.userListNameUpdateInput);
        userListNameUpdateInput.setText(modal.getTeacherName());
        final EditText userListPhoneUpdateInput = dialog.findViewById(R.id.userListPhoneUpdateInput);
        userListPhoneUpdateInput.setText(modal.getTeacherPhoneNumber());
        final EditText userListUserOrganizationUpdateInput = dialog.findViewById(R.id.userListUserOrganizationUpdateInput);
        userListUserOrganizationUpdateInput.setText(modal.getTeacherOrganization());
        final EditText userListUserRoomKeyUpdateInput = dialog.findViewById(R.id.userListUserRoomKeyUpdateInput);
        userListUserRoomKeyUpdateInput.setText(modal.getTeacherRoomKey());
        final String[] uLNUI = new String[1];
        final String[] uLPUI = new String[1];
        final String[] uLUOUi = new String[1];
        final String[] uLURKUI = new String[1];
        userListNameUpdateInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {uLNUI[0] = charSequence.toString();}
            @Override public void afterTextChanged(Editable editable) { }
        });
        userListPhoneUpdateInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {uLPUI[0] = charSequence.toString();}
            @Override public void afterTextChanged(Editable editable) { }
        });
        userListUserOrganizationUpdateInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {uLUOUi[0] = charSequence.toString();}
            @Override public void afterTextChanged(Editable editable) { }
        });
        userListUserRoomKeyUpdateInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {uLURKUI[0] = charSequence.toString();}
            @Override public void afterTextChanged(Editable editable) { }
        });


        CardView userListAddressUpdateCompleteCard = dialog.findViewById(R.id.userListAddressUpdateCompleteCard);
        userListAddressUpdateCompleteCard.setOnClickListener(v -> {
            String un = modal.getTeacherName(), up = modal.getTeacherPhoneNumber(), uo = modal.getTeacherOrganization(),
                    urk = modal.getTeacherRoomKey();

            DocumentReference usersDoc = db.collection("Teachers").document(modal.getTeacherId());
            if (uLNUI[0]==null && uLPUI[0]==null && uLUOUi[0]==null && uLURKUI[0]==null ){
                new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                        .setContentText("O'zgartirish kiritilmadi!")
                        .setConfirmText("OK!")
                        .setConfirmClickListener(SweetAlertDialog::cancel)
                        .show();
            }
            else {
                if(!Objects.equals(uLNUI[0], modal.getTeacherName())   && uLNUI[0] != null){
                    un = (uLNUI[0]);
                }
                if((!Objects.equals(uLPUI[0], modal.getTeacherPhoneNumber()))   && uLPUI[0] != null){
                    up = (uLPUI[0]);
                }
                if(!Objects.equals(uLUOUi[0], modal.getTeacherOrganization())   && uLUOUi[0] != null){
                    uo = (uLUOUi[0]);
                }
                if(!Objects.equals(uLURKUI[0], modal.getTeacherRoomKey()) && uLURKUI[0] != null){
                    urk = (uLURKUI[0]);
                }
                usersDoc.update("teacherName", un, "teacherPhoneNumber",up, "teacherOrganization", uo,
                        "teacherRoomKey", urk).addOnSuccessListener(unused -> {
                    new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                            .setContentText("Muvafaqqiyatli o'zgartirildi!")
                            .setConfirmText("OK!")
                            .setConfirmClickListener(SweetAlertDialog::cancel)
                            .show();
                }).addOnFailureListener(e -> {
                    new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                            .setContentText("Jarayonda xatolik!")
                            .setConfirmText("OK!")
                            .setConfirmClickListener(SweetAlertDialog::cancel)
                            .show();
                });
            }

            dialog.dismiss();
        });

        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

    }


    @Override
    public int getItemCount() {
        // returning the size of array list.
        return dataModalArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // creating variables for our
        // views of recycler items.
        private TextView userListName, userListPhone;
        CardView userListUpdate, userListDelete;
        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            // initializing the views of recycler views.
            userListName = itemView.findViewById(R.id.userListName);
            userListPhone = itemView.findViewById(R.id.userListPhone);
            userListUpdate = itemView.findViewById(R.id.userListUpdate);
            userListDelete = itemView.findViewById(R.id.userListDelete);

            userListDelete.setOnClickListener(view -> listener.onItemClick(getAdapterPosition()));

        }
    }
    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    public  void setOnItemClickListener(OnItemClickListener clickListener){
        listener = clickListener;
    }
}
