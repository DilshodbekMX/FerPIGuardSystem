package com.example.ferpiguardsystem.Model;

import com.google.firebase.Timestamp;

import java.util.List;
import java.util.TimeZone;

public class KeyHistoryActivityChildModel {
    private String teacherName;
    Timestamp givenTime, takenTime;
    public KeyHistoryActivityChildModel(){}

    public KeyHistoryActivityChildModel(String teacherName, Timestamp givenTime, Timestamp takenTime) {
        this.teacherName = teacherName;
        this.givenTime = givenTime;
        this.takenTime = takenTime;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Timestamp getGivenTime() {
        return givenTime;
    }

    public void setGivenTime(Timestamp givenTime) {
        this.givenTime = givenTime;
    }

    public Timestamp getTakenTime() {
        return takenTime;
    }

    public void setTakenTime(Timestamp takenTime) {
        this.takenTime = takenTime;
    }
}
