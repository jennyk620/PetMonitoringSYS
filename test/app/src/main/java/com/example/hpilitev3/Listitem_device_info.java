package com.example.hpilitev3;

// 기기 정보를 얻기 위한 클래스

import android.graphics.drawable.Drawable;

public class Listitem_device_info {

    private Drawable image;
    private int num;
    private String subject, contents;

    public Listitem_device_info(int num, Drawable image, String subject, String contents) {
        this.num = num;
        this.image = image;
        this.subject = subject;
        this.contents = contents;
    }

    public int getNum() {return num;}

    public Drawable getImage() {
        return image;
    }

    public String getSubject() {
        return subject;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
