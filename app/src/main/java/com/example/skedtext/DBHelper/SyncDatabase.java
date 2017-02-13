package com.example.skedtext.DBHelper;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.skedtext.Data.ContactGroups;
import com.example.skedtext.Data.ContactUsers;
import com.example.skedtext.Data.Messages;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by solomon on 2/12/17.
 */

public class SyncDatabase {

    private static final String getContactUsers = "contact=1";
    private static final String getContactGroups = "group=1";
    private static final String getUsers = "users=1";
    private static final String getMessages = "messages=1";

    private static final String json_url = "http://skedtext.hol.es/json.php?";

    public static final String TABLE_USERS = "users";
    public static final String USERS_ID = "id";
    public static final String USERS_USERNAME = "username";
    public static final String USERS_PASSWORD = "password";
    public static final String USERS_FIRST_NAME = "first_name";
    public static final String USERS_LAST_NAME = "last_name";
    public static final String USERS_LAST_UPDATE = "last_update";

    public static final String TABLE_MESSAGES = "messages";
    public static final String MESSAGES_ID = "id";
    public static final String MESSAGES_MESSAGE = "message";
    public static final String MESSAGES_CONTACT = "contact_fk";
    public static final String MESSAGES_EVENT = "event_timestamp";
    public static final String MESSAGES_ALARM = "alarm_timestamp";
    public static final String MESSAGES_STATUS = "status";

    public static final String TABLE_CONTACT_GROUPS = "contact_groups";
    public static final String CGROUPS_ID = "id";
    public static final String CGROUPS_NAME = "name";

    public static final String TABLE_CONTACT_USERS = "contact_users";
    public static final String CUSERS_ID = "id";
    public static final String CUSERS_FIRST_NAME = "first_name";
    public static final String CUSERS_MIDDLE_NAME = "middle_name";
    public static final String CUSERS_LAST_NAME = "last_name";
    public static final String CUSERS_PHONE_NUMBER = "phone_number";
    public static final String CUSERS_CONTACT_GROUPS_FK = "contact_groups_fk";

    Context context;
    SQLiteDatabaseHelper myDB;
    SQLiteDatabase db;

    public SyncDatabase(Context context){
        this.context = context;
        myDB = new SQLiteDatabaseHelper(context);
        db = myDB.returnDB();
    }

    public boolean onStart() {
        if(onUpdate()){
            return true;
        }else{
            return false;
        }
    }

    public boolean onUpdate() {
        getJsonCGroups();
        getJsonCUsers();
        getJsonMessages();
        return true;
    }

    private void insertWebMessages() {
        String json = messageJSON();
        Log.d("TAG", json);
    }

    private void getJsonMessages(){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(json_url+getMessages,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Dismissing progress dialog
                        //calling method to parse json array
                        parseMessages(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TAG", error.toString());
                    }
                });

        //Creating request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        //Adding request to the queue
        requestQueue.add(jsonArrayRequest);
    }

    private void parseMessages(JSONArray array){
        for (int i = 0; i < array.length(); i++) {
            Messages messages = new Messages();
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                messages.setId(json.getString("id"));
                messages.setContact(json.getString("contact_fk"));
                messages.setMessage(json.getString("message"));
                messages.setEventDateTime(json.getString("event_timestamp"));
                messages.setAlarmDateTime(json.getString("alarm_timestamp"));
                messages.setStatus(json.getString("status"));
                ContentValues data = new ContentValues();
                data.put(MESSAGES_ID, Integer.parseInt(messages.getId()));
                data.put(MESSAGES_MESSAGE, messages.getMessage());
                data.put(MESSAGES_CONTACT, messages.getContact());
                data.put(MESSAGES_EVENT, messages.getEventDateTime());
                data.put(MESSAGES_ALARM, messages.getAlarmDateTime());
                data.put(MESSAGES_STATUS, Integer.parseInt(messages.getStatus()));
                long result = -1;
                try{
                    Cursor cursor = db.rawQuery("select id from " + TABLE_MESSAGES + " WHERE " +
                            MESSAGES_ID + "=" + messages.getId() + ";", null);
                    if(cursor.moveToFirst()){
                        Cursor selectU = db.rawQuery("select id from " + TABLE_MESSAGES + " WHERE " +
                                MESSAGES_MESSAGE + "='" + messages.getMessage() + "' AND " +
                                MESSAGES_CONTACT + "=" + messages.getContact() + " AND " +
                                MESSAGES_EVENT + "='" + messages.getEventDateTime() + "' AND " +
                                MESSAGES_ALARM + "='" + messages.getAlarmDateTime() + "' AND " +
                                MESSAGES_STATUS + "=" + messages.getStatus() + ";", null);
                        if(selectU.moveToFirst()){
                            Log.d("TAG", "Message Exist");
                        }else{
                            Log.d("TAG", "Message does not exist");
                            Log.d("TAG", "Message: " + messages.getMessage());
                            ContentValues dUpdate = new ContentValues();
                            dUpdate.put(MESSAGES_MESSAGE, messages.getMessage());
                            dUpdate.put(MESSAGES_CONTACT, messages.getContact());
                            dUpdate.put(MESSAGES_EVENT, messages.getEventDateTime());
                            dUpdate.put(MESSAGES_ALARM, messages.getAlarmDateTime());
                            dUpdate.put(MESSAGES_STATUS, Integer.parseInt(messages.getStatus()));
                            result = db.insertWithOnConflict(TABLE_MESSAGES, null, dUpdate, SQLiteDatabase.CONFLICT_IGNORE);
                        }
                    }else{
                        result = db.insertWithOnConflict(TABLE_MESSAGES, null, data, SQLiteDatabase.CONFLICT_IGNORE);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(result == -1){
                    Log.d("TAG", "ERROR messages");
                }else{
                    Log.d("TAG", "Success messages");
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void getJsonCUsers(){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(json_url+getContactUsers,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Dismissing progress dialog
                        //calling method to parse json array
                        parseCUsers(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TAG", error.toString());
                    }
                });

        //Creating request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        //Adding request to the queue
        requestQueue.add(jsonArrayRequest);
    }

    private void parseCUsers(JSONArray array){
        for (int i = 0; i < array.length(); i++) {
            ContactUsers cusers = new ContactUsers();
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                cusers.setId(json.getString("id"));
                cusers.setFirst_name(json.getString("first_name"));
                cusers.setMiddle_name(json.getString("middle_name"));
                cusers.setLast_name(json.getString("last_name"));
                cusers.setPhone_number(json.getString("phone_number"));
                cusers.setContact_groups_fk(json.getString("contact_groups_fk"));
                ContentValues data = new ContentValues();
                data.put(CUSERS_ID, Integer.parseInt(cusers.getId()));
                data.put(CUSERS_FIRST_NAME, cusers.getFirst_name());
                data.put(CUSERS_MIDDLE_NAME, cusers.getMiddle_name());
                data.put(CUSERS_LAST_NAME, cusers.getLast_name());
                data.put(CUSERS_PHONE_NUMBER, cusers.getPhone_number());
                data.put(CUSERS_CONTACT_GROUPS_FK, Integer.parseInt(cusers.getContact_groups_fk()));
                long result = -1;
                try{
                    Cursor cursor = db.rawQuery("select id from " + TABLE_CONTACT_USERS + " WHERE " +
                            CUSERS_PHONE_NUMBER + "='" + cusers.getPhone_number() + "';", null);
                    if(cursor.moveToFirst()){
                    }else{
                        result = db.insertWithOnConflict(TABLE_CONTACT_USERS, null, data, SQLiteDatabase.CONFLICT_IGNORE);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(result == -1){
                    Log.d("TAG", "ERROR cusers");
                }else{
                    Log.d("TAG", "Success cusers");
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void getJsonCGroups(){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(json_url+getContactGroups,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Dismissing progress dialog
                        //calling method to parse json array
                        parseCGroups(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("TAG", error.toString());
                    }
                });

        //Creating request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        //Adding request to the queue
        requestQueue.add(jsonArrayRequest);
    }

    private void parseCGroups(JSONArray array){
        for (int i = 0; i < array.length(); i++) {
            ContactGroups cgroups = new ContactGroups();
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                cgroups.setId(json.getString("id"));
                cgroups.setName(json.getString("name"));
                ContentValues data = new ContentValues();
                data.put(CGROUPS_ID, Integer.parseInt(cgroups.getId()));
                data.put(CGROUPS_NAME, cgroups.getName());
                long result = -1;
                try{
                    Cursor cursor = db.rawQuery("select id from " + TABLE_CONTACT_GROUPS + " WHERE " +
                            CGROUPS_NAME + "='" + cgroups.getName() + "';", null);
                    if(cursor.moveToFirst()){
                    }else{
                        result = db.insertWithOnConflict(TABLE_CONTACT_GROUPS, null, data, SQLiteDatabase.CONFLICT_IGNORE);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(result == -1){
                    Log.d("TAG", "ERROR cgroups");
                }else{
                    Log.d("TAG", "Success cgroups");
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public String messageJSON() {
        ArrayList<HashMap<String, String>> offlineList;
        offlineList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM " + TABLE_MESSAGES;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id", cursor.getString(cursor.getColumnIndex("id")));
                map.put("message", cursor.getString(cursor.getColumnIndex("message")));
                map.put("contact_fk", cursor.getString(cursor.getColumnIndex("contact_fk")));
                map.put("event_timestamp", cursor.getString(cursor.getColumnIndex("event_timestamp")));
                map.put("alarm_timestamp", cursor.getString(cursor.getColumnIndex("alarm_timestamp")));
                map.put("status", cursor.getString(cursor.getColumnIndex("status")));
                offlineList.add(map);

            } while (cursor.moveToNext());
        }
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(offlineList);
    }

    public void syncSQLiteMySQLDB() {

        //i get my json string from sqlite, see the code i posted above about this
        final String json = messageJSON();

        new Thread() {
            public void run() {
                makeRequest("http://skedtext.hol.es/apiJson.php", json);
            }
        }.start();

    }

    public void makeRequest(String uri, String json) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(new StringEntity(json));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(httpPost);
            if (response != null) {

                String responseBody = EntityUtils.toString(response.getEntity());
                Log.d("response to sync", responseBody);
                Object jsonObj = new JSONTokener(responseBody).nextValue();
                if (jsonObj instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) jsonObj;
                    //further actions on jsonObjects

                } else if (jsonObj instanceof JSONArray) {
                    //further actions on jsonArray
                    JSONArray jsonArray = (JSONArray) jsonObj;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



}
