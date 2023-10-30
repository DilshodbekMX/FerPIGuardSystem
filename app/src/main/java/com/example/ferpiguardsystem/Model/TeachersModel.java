package com.example.ferpiguardsystem.Model;

import java.util.ArrayList;

public class TeachersModel {

    private String teacherName, teacherOrganization, teacherRoomKey, teacherPhoneNumber, teacherId;
    private ArrayList<Double> encodedFace;
    public TeachersModel(){}

    public TeachersModel(String teacherId,String teacherName, String teacherOrganization, String teacherRoomKey,
                         String teacherPhoneNumber,  ArrayList<Double> encodedFace) {
        this.teacherName = teacherName;
        this.teacherOrganization = teacherOrganization;
        this.teacherRoomKey = teacherRoomKey;
        this.teacherPhoneNumber = teacherPhoneNumber;
        this.teacherId = teacherId;
        this.encodedFace = encodedFace;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherOrganization() {
        return teacherOrganization;
    }

    public void setTeacherOrganization(String teacherOrganization) {
        this.teacherOrganization = teacherOrganization;
    }

    public String getTeacherRoomKey() {
        return teacherRoomKey;
    }

    public void setTeacherRoomKey(String teacherRoomKey) {
        this.teacherRoomKey = teacherRoomKey;
    }

    public String getTeacherPhoneNumber() {
        return teacherPhoneNumber;
    }

    public void setTeacherPhoneNumber(String teacherPhoneNumber) {
        this.teacherPhoneNumber = teacherPhoneNumber;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public ArrayList<Double> getEncodedFace() {
        return encodedFace;
    }

    public void setEncodedFace(ArrayList<Double> encodedFace) {
        this.encodedFace = encodedFace;
    }
}
