package com.example.skedtext.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.skedtext.DBHelper.SQLiteDatabaseHelper;
import com.example.skedtext.R;
import com.example.skedtext.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername;
    private EditText edtPass;
    private Button btnLogin;
    private Activity activity;

    private SQLiteDatabaseHelper myDB;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        edtUsername = (EditText) findViewById(R.id.edtUsername);
        edtPass = (EditText) findViewById(R.id.edtPassword);
        activity = this;

        myDB = new SQLiteDatabaseHelper(this);
        sessionManager = new SessionManager(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = edtUsername.getText().toString();
                String pass = edtPass.getText().toString();

                if(username.trim().equals("") || pass.trim().equals("")){
                    final AlertDialog.Builder loginAlert = new AlertDialog.Builder(activity);
                    loginAlert.setTitle("Incomplete Credentials");
                    loginAlert.setMessage("Please input the username and password accordingly.");
                    loginAlert.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    loginAlert.show();
                }else{
                    Cursor res = myDB.getUser(username, pass);
                    if(res.getCount() > 0){
                        sessionManager.createLoginSession(username, pass);
                        //Toast.makeText(getApplicationContext(), "Database: " + String.valueOf(db.getCount()), Toast.LENGTH_LONG).show();
                        Intent toMain = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(toMain);
                        finish();
                    }else{
                        Toast.makeText(activity, "No user is found", Toast.LENGTH_LONG).show();
                    }

                }

            }
        });

    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keycode, event);
    }
}
