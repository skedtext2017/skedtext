package com.example.skedtext.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.StrictMode;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.skedtext.Adapter.RVAdapter;
import com.example.skedtext.DBHelper.SQLiteDatabaseHelper;
import com.example.skedtext.DBHelper.SyncDatabase;
import com.example.skedtext.Data.Messages;
import com.example.skedtext.R;

import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.skedtext.SessionManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    public static final int ADDED_SMS = 1;
    public static final int SELECTED_SMS = 2;

    public static final int RESULT_SMS_CANCELLED = 2;
    public static final int RESULT_SMS_EDIT = 3;

    String Token;
    boolean thread_running = true;
    RecyclerView rv;
    CoordinatorLayout coordinatorLayout;
    List<Messages> messagesList;
    String url;
    private SQLiteDatabaseHelper myDB;
    private SwipeRefreshLayout swipeContainer;
    private SwipeRefreshLayout swipeEmpty;
    private AlertDialog progressDialog;
    Activity activity;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        sessionManager = new SessionManager(this);
        url = "";

        sessionManager.checkLogin();

        FirebaseMessaging.getInstance().subscribeToTopic("test");
        FirebaseInstanceId.getInstance().getToken();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(thread_running){
                    Token = FirebaseInstanceId.getInstance().getToken();
                    if(Token != null){
                        System.out.println("Device token is: " + Token);

                        thread_running = false;
                    }else{
                        System.out.println("Token not loaded");
                    }
                    try{
                        Thread.sleep(1000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();

        myDB = new SQLiteDatabaseHelper(this);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        Cursor cusers = myDB.getContactUsers();
        Cursor cgroups = myDB.getContactGroups();
        Toast.makeText(this, "CUsers: " + String.valueOf(cusers.getCount()), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "CGroups: " + String.valueOf(cgroups.getCount()), Toast.LENGTH_SHORT).show();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeEmpty = (SwipeRefreshLayout) findViewById(R.id.swipeEmpty);
        rv = (RecyclerView) findViewById(R.id.rvMessage);
        progressDialog = new SpotsDialog(activity);

        onPopulateData();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action
                Intent intent = new Intent(MainActivity.this, SchedMessageActivity.class);
                startActivityForResult(intent, ADDED_SMS);
            }
        });

    }

    private void onPopulateData(){
        Cursor res = myDB.getMessages();
        if(res.getCount() > 0){
            swipeEmpty.setVisibility(View.GONE);
            swipeContainer.setVisibility(View.VISIBLE);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            rv.setLayoutManager(llm);
            rv.setHasFixedSize(true);
            messagesList = new ArrayList<>();
            if (res.moveToFirst()){
                do{
                    try{
                        Messages messages = new Messages();
                        String id = res.getString(res.getColumnIndex("id"));
                        messages.setId(id);
                        String contact = res.getString(res.getColumnIndex("contact_fk"));
                        Cursor infoGroup = myDB.getContactGroup(contact);
                        if(infoGroup.moveToFirst()){
                            String nameGroup = infoGroup.getString(infoGroup.getColumnIndex("name"));
                            messages.setContact(nameGroup);
                        }
                        String message = res.getString(res.getColumnIndex("message"));
                        messages.setMessage(message);
                        String event = res.getString(res.getColumnIndex("event_timestamp"));
                        messages.setEventDateTime(event);
                        String alarm = res.getString(res.getColumnIndex("alarm_timestamp"));
                        messages.setAlarmDateTime(alarm);
                        String created = res.getString(res.getColumnIndex("created_timestamp"));
                        messages.setCreatedDateTime(created);
                        String status = res.getString(res.getColumnIndex("status"));
                        messages.setStatus(status);
                        messagesList.add(messages);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }while(res.moveToNext());
            }
            res.close();

            rv.setAdapter(new RVAdapter(messagesList, this, new RVAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Messages item) {
                    Intent toMessage = new Intent(getApplicationContext(), DisplaySchedItemActivity.class);
                    toMessage.putExtra("id", item.getId());
                    toMessage.putExtra("contact", item.getContact());
                    toMessage.putExtra("message", item.getMessage());
                    toMessage.putExtra("eventDateTime", item.getEventDateTime());
                    toMessage.putExtra("alarmDateTime", item.getAlarmDateTime());
                    toMessage.putExtra("createdDateTime", item.getCreatedDateTime());
                    startActivityForResult(toMessage, SELECTED_SMS);
                }
            }));
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

                @Override

                public void onRefresh() {
                    onPopulateData();
                    swipeContainer.setRefreshing(false);
                }

            });

            // Configure the refreshing colors

            swipeContainer.setColorSchemeResources(R.color.colorAccent,
                    R.color.colorPrimaryLight,

                    R.color.colorPrimary,

                    R.color.colorPrimaryDark);
        }else{
            swipeContainer.setVisibility(View.GONE);
            swipeEmpty.setVisibility(View.VISIBLE);
            swipeEmpty.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onPopulateData();
                    swipeEmpty.setRefreshing(false);
                }
            });

            swipeEmpty.setColorSchemeResources(R.color.colorAccent,
                    R.color.colorPrimaryLight,

                    R.color.colorPrimary,

                    R.color.colorPrimaryDark);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.abLogout:
                sessionManager.logoutUser();
                break;
            case R.id.abSync:
                if(isNetworkAvailable(this)){
                    AlertDialog.Builder updateAlert = new AlertDialog.Builder(activity);
                    updateAlert.setTitle("Sync Database");
                    updateAlert.setMessage("Would you like to proceed?");
                    updateAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SyncDatabase syncDB = new SyncDatabase(activity);
                            progressDialog.show();
                            if(syncDB.onStart()){
                                new CountDownTimer(5000, 1000){

                                    @Override
                                    public void onTick(long millisUntilFinished) {

                                    }

                                    @Override
                                    public void onFinish() {
                                        progressDialog.dismiss();
                                        Cursor cCheckDB = myDB.getContactGroups();
                                        if(cCheckDB.moveToFirst()){
                                            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Sync Successfully!", Snackbar.LENGTH_LONG);
                                            snackbar.show();
                                        }else{
                                            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Sync Failed! Check your connection.", Snackbar.LENGTH_LONG);
                                            snackbar.show();
                                        }
                                    }
                                }.start();
                            }else{
                                new CountDownTimer(5000, 1000){

                                    @Override
                                    public void onTick(long millisUntilFinished) {

                                    }

                                    @Override
                                    public void onFinish() {
                                        progressDialog.dismiss();
                                        Toast.makeText(activity, "Sync Failed!", Toast.LENGTH_LONG).show();
                                    }
                                }.start();
                            }
                        }
                    });
                    updateAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    updateAlert.show();
                }else{
                    AlertDialog.Builder updateAlert = new AlertDialog.Builder(activity);
                    updateAlert.setTitle("Internet Error");
                    updateAlert.setMessage("Error: No internet is found");
                    updateAlert.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    updateAlert.show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keycode, event);
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conMan.getActiveNetworkInfo() != null && conMan.getActiveNetworkInfo().isConnected()){
            return true;
        }else{
            return false;
        }
    }

    @Override
    protected void onResume() {
        onPopulateData();
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ADDED_SMS){
            switch (resultCode){
                case RESULT_OK:
                    Snackbar snackAdded = Snackbar.make(coordinatorLayout, "Added SMS Successfully!", Snackbar.LENGTH_LONG);
                    snackAdded.show();
                    break;

                case RESULT_CANCELED:
                    Snackbar snackFailed = Snackbar.make(coordinatorLayout, "Failed while adding SMS!!", Snackbar.LENGTH_LONG);
                    snackFailed.show();
                    break;
            }
        }else if(requestCode == SELECTED_SMS){
            switch (resultCode){
                case RESULT_SMS_CANCELLED:
                    Snackbar snackCancelled = Snackbar.make(coordinatorLayout, "Cancelled Scheduled SMS Successfully!", Snackbar.LENGTH_LONG);
                    snackCancelled.show();
                    break;

                case RESULT_SMS_EDIT:
                    Snackbar snackEdited = Snackbar.make(coordinatorLayout, "Updated Scheduled SMS Successfully!", Snackbar.LENGTH_LONG);
                    snackEdited.show();
                    break;

                case RESULT_CANCELED:
                    Snackbar snackFailed = Snackbar.make(coordinatorLayout, "Failed while updating scheduled SMS!!", Snackbar.LENGTH_LONG);
                    snackFailed.show();
                    break;

                default:
                    break;
            }
        }
    }
}
