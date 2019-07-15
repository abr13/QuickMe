package com.abr.quickme.models;

public class Friends {

    public String date, name, status, thumb_image;

    public Friends() {

    }

    public Friends(String date, String name, String status, String thumb_image) {
        this.date = date;
        this.name = name;
        this.status = status;
        this.thumb_image = thumb_image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}
