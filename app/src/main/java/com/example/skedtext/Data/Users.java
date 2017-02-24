package com.example.skedtext.Data;

/**
 * Created by solomon on 2/23/17.
 */

public class Users {

    public String id;
    public String first_name;
    public String last_name;
    public String username;
    public String password;

    public void setId(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }

    public void setFirst_name(String first_name){
        this.first_name = first_name;
    }

    public String getFirst_name(){
        return first_name;
    }

    public void setLast_name(String last_name){
        this.last_name = last_name;
    }

    public String getLast_name(){
        return last_name;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getUsername(){
        return username;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getPassword(){
        return password;
    }

}
