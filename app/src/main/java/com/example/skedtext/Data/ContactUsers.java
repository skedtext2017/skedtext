package com.example.skedtext.Data;

/**
 * Created by solomon on 2/12/17.
 */

public class ContactUsers {

    public String id;
    public String first_name;
    public String middle_name;
    public String last_name;
    public String phone_number;
    public String contact_groups_fk;

    public ContactUsers(){
    }

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

    public void setMiddle_name(String middle_name){
        this.middle_name = middle_name;
    }

    public String getMiddle_name(){
        return middle_name;
    }

    public void setLast_name(String last_name){
        this.last_name = last_name;
    }

    public String getLast_name(){
        return last_name;
    }

    public void setPhone_number(String phone_number){
        this.phone_number = phone_number;
    }

    public String getPhone_number(){
        return phone_number;
    }

    public void setContact_groups_fk(String contact_groups_fk){
        this.contact_groups_fk = contact_groups_fk;
    }

    public String getContact_groups_fk(){
        return contact_groups_fk;
    }

}
