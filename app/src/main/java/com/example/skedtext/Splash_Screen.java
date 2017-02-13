package com.example.skedtext;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.skedtext.activities.LoginActivity;
import com.example.skedtext.activities.MainActivity;

/**
 * Created by solomon on 2/12/17.
 */

public class Splash_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(isNetworkAvailable(this)){
            onMain(true);
        }else{
            onMain(false);
        }
    }

    private void onMain(Boolean isTrue){
        if(isTrue){
            Intent toMainIntent = new Intent(this, MainActivity.class);
            startActivity(toMainIntent);
            finish();
        }else{
            Intent toMainIntent = new Intent(this, MainActivity.class);
            startActivity(toMainIntent);
            finish();
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conMan.getActiveNetworkInfo() != null && conMan.getActiveNetworkInfo().isConnected()){
            return true;
        }else{
            return false;
        }
    }

}
