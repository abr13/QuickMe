package com.abr.quickme.models;

public class Stories {
    String storyImage, storyText, name;

    public Stories(String storyImage, String storyText, String name) {
        this.storyImage = storyImage;
        this.storyText = storyText;
        this.name = name;
    }

    public Stories() {

    }

    public String getStoryImage() {
        return storyImage;
    }

    public void setStoryImage(String storyImage) {
        this.storyImage = storyImage;
    }

    public String getStoryText() {
        return storyText;
    }

    public void setStoryText(String storyText) {
        this.storyText = storyText;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
