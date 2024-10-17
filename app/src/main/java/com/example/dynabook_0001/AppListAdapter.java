package com.example.dynabook_0001;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends ArrayAdapter<AppInfo1> {

    private List<AppInfo1> selectedApps = new ArrayList<>();

    public AppListAdapter(Context context, List<AppInfo1> appInfoList) {
        super(context, 0, appInfoList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppInfo1 appInfo = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_checkbox, parent, false);
        }

        TextView appNameTextView = convertView.findViewById(R.id.labelview);
        CheckBox checkBox = convertView.findViewById(R.id.checkBox);
        ImageView iconImageView = convertView.findViewById(R.id.icon);

        if (appInfo != null) {
            appNameTextView.setText(appInfo.getAppName(getContext()));
            iconImageView.setImageDrawable(appInfo.getAppIcon());

            // Ensure the checkbox reflects the current state of the app
            checkBox.setOnCheckedChangeListener(null); // Reset the listener
            checkBox.setChecked(appInfo.isSelected());

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                appInfo.setSelected(isChecked);

                if (isChecked) {
                    if (!selectedApps.contains(appInfo)) {
                        selectedApps.add(appInfo);
                        Log.d("AppListAdapter", "App selected: " + appInfo.getAppName(getContext()));
                    }
                } else {
                    selectedApps.remove(appInfo);
                    Log.d("AppListAdapter", "App deselected: " + appInfo.getAppName(getContext()));
                }
            });
        }

        return convertView;
    }

    public List<AppInfo1> getSelectedApps() {
        return selectedApps;
    }
}
