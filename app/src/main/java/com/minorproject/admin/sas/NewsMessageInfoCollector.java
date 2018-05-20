/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minorproject.admin.sas;

public class NewsMessageInfoCollector {

    private String news;
    private String sender;
    private String photoUrl;
    private String day;
    private long date;

    public NewsMessageInfoCollector() {
    }

    public NewsMessageInfoCollector(String news, String name, String photoUrl,String day,Long date) {
        this.news = news;
        this.sender = name;
        this.photoUrl = photoUrl;
        this.day = day;
        this.date = date;
    }

    public String getNews() {
        return news;
    }

    public String getSender() {
        return sender;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getday(){
        return day;
    }

    public long getdate(){
        return date;
    }

}
