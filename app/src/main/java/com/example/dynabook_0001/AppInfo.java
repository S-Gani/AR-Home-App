package com.example.dynabook_0001;

import android.graphics.drawable.Drawable;
import android.content.Context;
import android.content.pm.ApplicationInfo;

class AppInfo1 {
    private String appName;
    private Drawable appIcon;
    private String packageName;
    private boolean isSelected;
    private ApplicationInfo appInfo;

    public AppInfo1(Drawable appIcon, String appName, String packageName) {
        this.appName = appName;
        this.appIcon = appIcon;
        this.packageName = packageName;
        this.isSelected = false;
    }

    public String getAppName(Context context) {
        return appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public String getPackageName(Context context) {
        return packageName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }


}
