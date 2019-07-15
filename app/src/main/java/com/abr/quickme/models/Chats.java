package com.abr.quickme.models;

public class Chats {

    public String name, thumb;

    public Chats() {

    }

    public Chats(String name, String thumb) {
        this.name = name;
        this.thumb = thumb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
}
