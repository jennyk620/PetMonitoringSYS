package com.example.hpilitev3;

// 기기 정보를 얻기 위한 클래스

import android.graphics.drawable.Drawable;

public class Listitem_collect_env {

    private Drawable image;
    private String subject;
    private boolean check;

    public Listitem_collect_env(Drawable image, String subject, boolean check) {
        this.image = image;
        this.subject = subject;
        this.check = check;
    }

    public Drawable getImage() {
        return image;
    }

    public String getSubject() {
        return subject;
    }

    public boolean get_check() { return check;}

    public void set_check(boolean check) { this.check = check;}

}
