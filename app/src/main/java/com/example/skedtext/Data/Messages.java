package com.example.skedtext.Data;

/**
 * Created by solomon on 2/7/17.
 */

public class Messages {

    public String id;
    public String contact;
    public String message;
    public String eventDateTime;
    public String alarmDateTime;
    public String createdDateTime;
    public String status;

    public void setId(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }

    public void setContact(String contact){
        this.contact = contact;
    }

    public String getContact(){
        return contact;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return message;
    }

    public void setEventDateTime(String eventDateTime){
        this.eventDateTime = eventDateTime;
    }

    public String getEventDateTime(){
        return eventDateTime;
    }

    public void setAlarmDateTime(String alarmDateTime){
        this.alarmDateTime = alarmDateTime;
    }

    public String getAlarmDateTime(){
        return alarmDateTime;
    }

    public void setCreatedDateTime(String createdDateTime){
        this.createdDateTime = createdDateTime;
    }

    public String getCreatedDateTime(){
        return createdDateTime;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String getStatus(){
        return status;
    }
}
