package com.zbadev.emotizone;

public class ScheduleItem {
    private String time;
    private String title;
    private String category;
    private String iconResId;

    public ScheduleItem(String time, String title, String category, String iconResId) {
        this.time = time;
        this.title = title;
        this.category = category;
        this.iconResId = iconResId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIconResId() {
        return iconResId;
    }

    public void setEmotionalState(String iconResId) {
        this.iconResId = iconResId;
    }

}
