package com.example.aysenur.messagingsystem.Model;

import java.io.Serializable;

public class Schedule implements Serializable {

    private String id;
    private String date;
    private String time;
    private String note;

    public Schedule() {
    }

    public Schedule(String id, String date, String time, String note) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.note = note;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }

    public String getNote() { return note; }

    public void setNote(String note) { this.note = note; }
}
