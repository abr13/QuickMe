package com.abr.quickme.models;

public class Messages {
    String type, from, time, seen, to, key, message, message_id;

    public Messages(String type, String from, String time, String seen, String to, String key, String message, String message_id) {
        this.type = type;
        this.from = from;
        this.time = time;
        this.seen = seen;
        this.to = to;
        this.key = key;
        this.message = message;
        this.message_id = message_id;
    }

    public Messages() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }


}
