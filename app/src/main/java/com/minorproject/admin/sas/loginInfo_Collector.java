package com.minorproject.admin.sas;


public class loginInfo_Collector {

    private String name;
    private String roll_no;
    private String photo_url;
    private int priority_level;
    private String faculty_symbol;
    private String year;

    public loginInfo_Collector() {
    }

    public loginInfo_Collector(String name, String roll_no, String faculty_symbol,String year, String photo_url, int priority_level){

        this.name = name;
        this.roll_no = roll_no;
        this.faculty_symbol = faculty_symbol;
        this.year = year;
        this.photo_url = photo_url;
        this.priority_level = priority_level;

    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoll_no() {
        return roll_no;
    }

    public void setRoll_no(String roll_no) {
        this.roll_no = roll_no;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public int getPriority_level() {
        return priority_level;
    }

    public void setPriority_level(int priority_level) {
        this.priority_level = priority_level;
    }

    public String getFaculty_symbol() {
        return faculty_symbol;
    }

    public void setFaculty_symbol(String faculty_symbol) {
        this.faculty_symbol = faculty_symbol;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
