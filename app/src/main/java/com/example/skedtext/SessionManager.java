package com.example.skedtext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.example.skedtext.activities.LoginActivity;

import java.util.HashMap;

/**
 * Created by solomon on 2/12/17.
 */

public class SessionManager {

    SharedPreferences pref;
    Editor editor;
    Context context;

    private static final String pref_name = "SkedText";
    private static final String isLogin = "isLogin";
    private static final String key_username = "username";
    private static final String key_password = "password";

    public SessionManager(Context context){
        this.context = context;
        pref = context.getSharedPreferences(pref_name, 0);
        editor = pref.edit();
    }

    public void createLoginSession(String username, String password){
        editor.putBoolean(isLogin, true);
        editor.putString(key_username, username);
        editor.putString(key_password, password);
        editor.commit();
    }

    public void checkLogin(){
        if(!this.isLoggedIn()){
            Intent i = new Intent(context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(key_username, pref.getString(key_username, null));
        user.put(key_password, pref.getString(key_password, null));
        return user;
    }

    public void logoutUser(){
        editor.clear();
        editor.commit();

        Intent i = new Intent(context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(isLogin, false);
    }
}
