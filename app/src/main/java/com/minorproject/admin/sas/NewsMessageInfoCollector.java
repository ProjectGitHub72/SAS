package com.minorproject.admin.sas;

public class NewsMessageInfoCollector {

    private String content_txt;
    private String by;
    private String content_url;
    private String day;
    private String date;
    private String time;
    private String title;
    private String type;

    public NewsMessageInfoCollector() {
    }

    public NewsMessageInfoCollector(String title,String news, String name, String photoUrl,String day,String date,String time,String type) {
        this.title = title;
        this.content_txt = news;
        this.by = name;
        this.content_url = photoUrl;
        this.day = day;
        this.date = date;
        this.time = time;
        this.type = type;
    }

    public String getTitle(){
        return title;
    }

    public String getContent_txt() {
        return content_txt;
    }

    public String getBy() {
        return by;
    }

    public String getContent_url() {
        return content_url;
    }

    public String getday(){
        return day;
    }

    public String getdate(){
        return date;
    }

    public String getTime(){
        return time;
    }

    public String getType() {
        return type;
    }

}
