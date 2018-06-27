package com.minorproject.admin.sas;

public class AuthorizeInfoCollector {

    private String userName;
    private String userEmail;
    private String userId;


    public AuthorizeInfoCollector() {
    }

    public AuthorizeInfoCollector(String userName, String userEmail, String userId){

        this.userName = userName;
        this.userEmail = userEmail;
        this.userId = userId;

        }

    public String getUserName(){
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getuserId() {
        return userId;
    }
}
