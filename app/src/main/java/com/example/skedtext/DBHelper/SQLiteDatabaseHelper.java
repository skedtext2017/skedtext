package com.example.skedtext.DBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.skedtext.Data.Messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by solomon on 2/12/17.
 */
public class SQLiteDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "DBSked.db";
    private Context context;
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
    public static final String MESSAGES_CREATED = "created_timestamp";
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

    public static final int MESSAGE_SENT = 0;
    public static final int MESSAGE_ACTIVE = 1;
    public static final int MESSAGE_CANCELLED = 2;
    public static final int MESSAGE_NO_LOAD = 3;

    public SQLiteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
        this.context = context;
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "( " +
                USERS_ID + " INTEGER PRIMARY KEY, " +
                USERS_USERNAME + " TEXT UNIQUE, " +
                USERS_PASSWORD + " TEXT, " +
                USERS_FIRST_NAME + " TEXT, " +
                USERS_LAST_NAME + " TEXT, " +
                USERS_LAST_UPDATE + " TIMESTAMP" +
                ");";
        String INSERT_USERS_ADMIN = "INSERT INTO " + TABLE_USERS + "( " +
                USERS_USERNAME + "," + USERS_PASSWORD + "," + USERS_FIRST_NAME + "," +
                USERS_LAST_NAME + ") VALUES('admin', '21232f297a57a5a743894a0e4a801fc3', 'Sked', 'Text'" +
                ");";

        String CREATE_MESSAGES = "CREATE TABLE " + TABLE_MESSAGES + "( " +
                MESSAGES_ID + " INTEGER PRIMARY KEY, " +
                MESSAGES_CONTACT + " INTEGER, " +
                MESSAGES_MESSAGE + " TEXT, " +
                MESSAGES_EVENT + " DATETIME, " +
                MESSAGES_ALARM + " DATETIME, " +
                MESSAGES_CREATED + " BIGINTEGER UNIQUE, " +
                MESSAGES_STATUS + " INTEGER" +
                ");";

        String CREATE_CONTACT_GROUPS = "CREATE TABLE " + TABLE_CONTACT_GROUPS + "( " +
                CGROUPS_ID + " INTEGER PRIMARY KEY, " +
                CGROUPS_NAME + " TEXT" +
                ");";

        String CREATE_CONTACT_USERS = "CREATE TABLE " + TABLE_CONTACT_USERS + "( " +
                CUSERS_ID + " INTEGER PRIMARY KEY, " +
                CUSERS_FIRST_NAME + " TEXT, " +
                CUSERS_MIDDLE_NAME + " TEXT, " +
                CUSERS_LAST_NAME + " TEXT, " +
                CUSERS_PHONE_NUMBER + " TEXT, " +
                CUSERS_CONTACT_GROUPS_FK + " INTEGER" +
                ");";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(INSERT_USERS_ADMIN);
        db.execSQL(CREATE_MESSAGES);
        db.execSQL(CREATE_CONTACT_GROUPS);
        db.execSQL(CREATE_CONTACT_USERS);
        Toast.makeText(context, "Successfully created database.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT_GROUPS);
        onCreate(db);
    }

    public SQLiteDatabase returnDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db;
    }

    public Cursor getUser(String username, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryUser = "SELECT * FROM " + TABLE_USERS + " WHERE username='"+username +
                "' AND password='" + password + "';";
        Cursor result = db.rawQuery(queryUser, null);
        return result;
    }

    public Cursor getMessages(){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryMessages = "SELECT * FROM " + TABLE_MESSAGES + " WHERE status=1 ORDER BY " +
                MESSAGES_ID +" DESC";
        Cursor result = db.rawQuery(queryMessages, null);
        return result;
    }

    public Cursor getContactUsers(){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryContactUsers = "SELECT * FROM " + TABLE_CONTACT_USERS;
        Cursor result = db.rawQuery(queryContactUsers, null);
        return result;
    }

    public Cursor getContactGroups(){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryContactGroups = "SELECT * FROM " + TABLE_CONTACT_GROUPS;
        Cursor result = db.rawQuery(queryContactGroups, null);
        return result;
    }

    public Cursor getContactGroup(String keyword){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryGroup = "SELECT * FROM " + TABLE_CONTACT_GROUPS + " WHERE id='"+keyword+
                "' OR name='"+keyword+"';";
        Cursor result = db.rawQuery(queryGroup, null);
        return result;
    }

    public Cursor getContactPhone(String keyword){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryPhone = "SELECT " + CUSERS_PHONE_NUMBER + " FROM " + TABLE_CONTACT_USERS +
                " WHERE " + CUSERS_CONTACT_GROUPS_FK + "='"+keyword+"';";
        Cursor result = db.rawQuery(queryPhone, null);
        return result;
    }

    public Cursor getUsers(){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryUsers = "SELECT * FROM " + TABLE_USERS;
        Cursor  result = db.rawQuery(queryUsers, null);
        return result;
    }

    public boolean emptyContactUsers(){
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_CONTACT_USERS, null, null);
        if(result == -1){
            Log.d("TAG", "Error on Delete");
            return false;
        }else{
            Log.d("TAG", "Success Delete");
            return true;
        }
    }

    public boolean emptyContactGroups() {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_CONTACT_GROUPS, null, null);
        if(result == -1){
            Log.d("TAG", "Error on Delete");
            return false;
        }else{
            Log.d("TAG", "Success Delete");
            return true;
        }
    }

    public boolean emptyUsers(){
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_USERS, null, null);
        if(result == -1){
            Log.d("TAG", "Error on Delete");
            return false;
        }else{
            Log.d("TAG", "Success Delete");
            return true;
        }
    }

    public boolean saveMessage(String contact, String message, String event, String alarm, String uniqueID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues data = new ContentValues();

        data.put(MESSAGES_MESSAGE, message);
        data.put(MESSAGES_CONTACT, contact);
        data.put(MESSAGES_EVENT, event);
        data.put(MESSAGES_ALARM, alarm);
        data.put(MESSAGES_CREATED, uniqueID);
        data.put(MESSAGES_STATUS, "1");
        long result = db.insert(TABLE_MESSAGES, null, data);
        if(result == -1){
            Log.d("TAG", "ERROR");
            return false;
        }else{
            Log.d("TAG", "Success");
            return true;
        }
    }

    public void messageChangeStatus(int keyword, String id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cStatus = new ContentValues();
        cStatus.put(MESSAGES_STATUS, keyword);
        long aResult = db.update(TABLE_MESSAGES, cStatus, MESSAGES_ID + "=" +id, null);
        if(aResult == -1){
            Log.d("SkedText", "Error on Updating message status");
        }else{
            Log.d("SkedText", "Success on updating message status");
        }
    }

    public boolean messageUpdateInfo(String id, String contact, String message, String event, String alarm, String uniqueID){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MESSAGES_MESSAGE, message);
        contentValues.put(MESSAGES_CONTACT, contact);
        contentValues.put(MESSAGES_EVENT, event);
        contentValues.put(MESSAGES_ALARM, alarm);
        contentValues.put(MESSAGES_CREATED, uniqueID);
        long result = db.update(TABLE_MESSAGES, contentValues, MESSAGES_ID + "=" + id, null);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    /**
     * function md5 encryption for passwords
     *
     * @param password
     * @return passwordEncrypted
     */
    public static final String md5(final String password) {
        try {

            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(password.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
