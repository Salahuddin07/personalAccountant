package com.example.dailyshoppinglist.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dailyshoppinglist.Model.Data;
import com.example.dailyshoppinglist.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton fab_btn;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private RadioButton radioButton,radioButton1;
    private RadioGroup radioGroup,radioGroup1;
    RecyclerView recyclerView;
    TextView textViewExpense, textViewIncome, textViewBalance;

    String amountType;
    private String type;
    private int amount;
    private String note;
    private String post_key;
    private android.app.AlertDialog.Builder alertDialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //initialize recyclerView
        recyclerView = findViewById(R.id.recycleview);

        //initialize home activity textView
        textViewExpense = findViewById(R.id.total_expenseId);
        textViewIncome = findViewById(R.id.totalIncomeId);
        textViewBalance = findViewById(R.id.balanceId);


        //firebase database connection and authentication
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uId = mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Shopping List").child(uId);
        //end

        //create offline database synced method and add to database
        mDatabase.keepSynced(true);

        //create a LinearLayoutManager and set property
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        //set recyclerView property and add into linearLayoutManager
        //fixed size of recyclerView layout
        recyclerView.setHasFixedSize(true);

        //attach linear layout to recyclerView
        recyclerView.setLayoutManager(linearLayoutManager);

        //calculating total amount
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int totalIncome = 0;
                int totalExpense = 0;

                //find every children from database using Datasnapshot and store it into snap
                for (DataSnapshot snap : dataSnapshot.getChildren()) {


                    //create Data model object to get all value using snap.getValue method
                    Data data = snap.getValue(Data.class);
                    String amType = data.getAmountType();

                    if (amType.equals("Income")) {
                        //get all data and store it to totalammount
                        totalIncome += data.getAmount();
                        //convert int to string amount for textView showing
                        String stringIncomeAmount = String.valueOf(totalIncome);

                        textViewIncome.setText(stringIncomeAmount);
                    }
                    if (amType.equals("Expense")) {

                        //get all data and store it to totalammount
                        totalExpense += data.getAmount();
                        //convert int to string amount for textView showing
                        String stringExpenseAmount = String.valueOf(totalExpense);

                        textViewExpense.setText(stringExpenseAmount);

                    }

//                    String income = String.valueOf(totalIncome);
//                    String expense = String.valueOf(totalExpense);
//                    Log.d("Total Income:",income);
//                    Log.d("Total Expense:",income);


                }

                int balance = (totalIncome - totalExpense);
                String stringBalance = String.valueOf(balance);
                textViewBalance.setText(stringBalance);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //find toolbar and floating action button and set toolbar to setSupportActionBar
        toolbar = findViewById(R.id.toolbar_home);
        fab_btn = findViewById(R.id.fab);

        setSupportActionBar(toolbar);

        //check getSupportActionBar is null or not
        if (getSupportActionBar() != null) {
            //if not null then set title
            // getSupportActionBar().setTitle("Daily Shopping List");
        }

        //set OnClickListener to floating action button
        fab_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog();//call customDialog method
            }
        });
    }

    private void customDialog() {

        // alertDialog to showing dialog
        AlertDialog.Builder myDialog = new AlertDialog.Builder(HomeActivity.this);

        //to covert input_data Layout to Java View using LayoutInflater
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        final View myView = inflater.inflate(R.layout.input_data, null);

        //create dialog and set view to the dialog
        final AlertDialog dialog = myDialog.create();
        dialog.setView(myView);

        //find all input data field and button
        final EditText type = myView.findViewById(R.id.shoppingTypeId);
        final EditText amount = myView.findViewById(R.id.amountId);
        final EditText note = myView.findViewById(R.id.noteId);
        Button addData = myView.findViewById(R.id.addButton);
        radioGroup = myView.findViewById(R.id.radioGroupId);


        //set OnClickListener to save button
        addData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get data from from input field layout

                //take data from Radio Button
                int selectedId = radioGroup.getCheckedRadioButtonId();
                radioButton = myView.findViewById(selectedId);
                amountType = radioButton.getText().toString();

                String mType = type.getText().toString().trim();
                String mAmount = amount.getText().toString().trim();
                String mNote = note.getText().toString().trim();


                //parse amount string to integer
                int amountInt = Integer.parseInt(mAmount);

                //checking validity
                if (TextUtils.isEmpty(mType)) {
                    type.setError("Required Field...");
                    return;
                }
                if (TextUtils.isEmpty(mAmount)) {
                    amount.setError("Required Field...");
                    return;
                }
                if (TextUtils.isEmpty(mNote)) {
                    note.setError("Required Field...");
                    return;
                }
                if (TextUtils.isEmpty(amountType)) {
                    radioButton.setError("Required Field...");
                    return;
                }

                //create random id
                String id = mDatabase.push().getKey();

                //create date formet for database
                String mDate = DateFormat.getDateInstance().format(new Date());

                //set data to firebase using model class
                Data data = new Data(mType, amountInt, mNote, mDate, id, amountType);
                mDatabase.child(id).setValue(data);
                Toast.makeText(getApplicationContext(), "Data Added Successfully", Toast.LENGTH_SHORT).show();

                //dialog is dismissing
                dialog.dismiss();

            }
        });

        //showing dialog
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //bind Data model class
        FirebaseRecyclerOptions<Data> options =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mDatabase, Data.class)
                        .build();
        //initialize FireBaseRecyclerAdapter
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @Override
            public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                //convert show_item_data into java View
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.show_item_data, parent, false);
                //return view to MyViewHolder class
                return new MyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(MyViewHolder myViewHolder, final int position, final Data data) {

                myViewHolder.setDate(data.getDate());
                myViewHolder.setType(data.getType());
                myViewHolder.setNote(data.getNote());
                myViewHolder.setAmount(data.getAmount());
                myViewHolder.setAmountType(data.getAmountType());

                myViewHolder.myView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        post_key = getRef(position).getKey();
                        type = data.getType();
                        amount = data.getAmount();
                        note = data.getNote();
                        amountType = data.getAmountType();

                        updateData();
                    }
                });
            }
        };

        //set Adapter into RecyclerView
        recyclerView.setAdapter(adapter);
        //now adapter will startListening means getting value using this method
        adapter.startListening();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        View myView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            myView = itemView;
        }

        //setType
        public void setType(String type) {

            TextView mType = myView.findViewById(R.id.showTypeId);
            mType.setText(type);

        }

        //setNote
        public void setNote(String note) {

            TextView mNote = myView.findViewById(R.id.showDesId);
            mNote.setText(note);

        }

        //set Date
        public void setDate(String date) {

            TextView mDate = myView.findViewById(R.id.showDateId);
            mDate.setText(date);

        }

        //set amount
        public void setAmount(int amount) {

            TextView mAmount = myView.findViewById(R.id.showAmountId);
            String stringAmount = String.valueOf(amount);
            mAmount.setText(stringAmount);

        }

        //set amount type
        public void setAmountType(String amountType1) {

            TextView mAmountType = myView.findViewById(R.id.amountTypeId);
            mAmountType.setText(amountType1);

        }

    }

    public void updateData() {

        AlertDialog.Builder myDialog = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater layoutInflater = LayoutInflater.from(HomeActivity.this);
        final View myView = layoutInflater.inflate(R.layout.update_item, null);

        final AlertDialog dialog = myDialog.create();
        dialog.setView(myView);


        final EditText updateType = myView.findViewById(R.id.update_shoppingTypeId);
        final EditText updateAmount = myView.findViewById(R.id.update_amountId);
        final EditText updateNote = myView.findViewById(R.id.update_noteId);

        updateType.setText(type);
        updateType.setSelection(type.length());

        updateAmount.setText(String.valueOf(amount));
        updateAmount.setSelection(String.valueOf(amount).length());

        updateNote.setText(note);
        updateNote.setSelection(note.length());

        Button deleteButton = myView.findViewById(R.id.delete_Button);
        Button updateButton = myView.findViewById(R.id.update_Button);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDatabase.child(post_key).removeValue();
                dialog.dismiss();

            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                type = updateType.getText().toString().trim();
                String mAmount = String.valueOf(amount);
                mAmount = updateAmount.getText().toString().trim();
                note = updateNote.getText().toString().trim();

               Log.d("Amount Type",amountType);


                int intAmount = Integer.parseInt(mAmount);

                String date = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(type, intAmount, note, date, amountType, post_key);
                mDatabase.child(post_key).setValue(data);

                dialog.dismiss();


            }
        });

        dialog.show();

    }

    ///adding menu items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_layout, menu);
        return true;
    }
    //end

    public boolean onOptionsItemSelected(MenuItem item) {

        //share button task
        if (item.getItemId() == R.id.shareId) {

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");

            String subject = "Human Safety app";
            String body = "This app is very helpful for noting what you buy every day.\n com.example.dailyshoppinglist.Activity;";

            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);

            startActivity(Intent.createChooser(intent, "Share With"));

        } else if (item.getItemId() == R.id.FeedBackId) {

//            Intent intent = new Intent(MainActivity2.this, FeedbackActivity.class);
//            startActivity(intent);
//            finish();

        } else if (item.getItemId() == R.id.aboutId) {

//            Intent intent = new Intent(MainActivity2.this, About.class);
//            startActivity(intent);
//            finish();

        } else if (item.getItemId() == R.id.logOut) {
            mAuth.signOut();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else if (item.getItemId() == R.id.helpId) {

//            Intent intent = new Intent(MainActivity2.this, Help.class);
//            startActivity(intent);
//            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //set alert dialog
    @Override
    public void onBackPressed() {
        Log.e("BackPress", "OnBackPressedCalled: ");

        alertDialogBuilder = new android.app.AlertDialog.Builder(HomeActivity.this);
        alertDialogBuilder.setTitle(R.string.dialogTitle);
        alertDialogBuilder.setMessage(R.string.dialogMessage);
        alertDialogBuilder.setIcon(R.drawable.question);
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}
