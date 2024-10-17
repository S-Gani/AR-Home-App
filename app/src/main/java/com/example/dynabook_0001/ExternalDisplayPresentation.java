package com.example.dynabook_0001;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityOptions;
import android.app.Presentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;


import java.util.List;

public class ExternalDisplayPresentation extends Presentation {
    private Context context;
    private  List<AppInfo1> selectedApps;
    private GridLayout appIconsLayout;
    private CursorView cursorView;
    private Handler handler;


    public ExternalDisplayPresentation(Context outerContext, Display display, List<AppInfo1> selectedAppss) {
        super(outerContext, display);
        handler = new Handler();
        this.selectedApps=selectedAppss;
        this.context = outerContext;


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//            setContentView(R.layout.presentation_layout);
        setPrimaryLayout();
    }
    // Method to set the primary layout
    private void setPrimaryLayout() {
        setContentView(R.layout.presentation_layout);

        appIconsLayout = findViewById(R.id.appIconsLayout);

        addAppIcons(selectedApps);

    }


    private void addAppIcons(List<AppInfo1> appList) {
        for (int i = 0; i < appList.size(); i++) {
            AppInfo1 appInfo = appList.get(i);
            ImageView imageView = new ImageView(getContext());
            imageView.setClickable(true); // Ensure that the ImageView is clickable
            Drawable appIcon = appInfo.getAppIcon(); // Assuming you have a getter for the app icon
            imageView.setImageDrawable(appIcon);
            int iconSize = getResources().getDimensionPixelSize(R.dimen.app_icon_size);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = iconSize;
            params.height = iconSize;
            params.setMargins(10, 10, 10, 10);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle click action
//                        Toast.makeText(getContext(), appInfo.loadLabel(getContext().getPackageManager()) + " clicked.....*", Toast.LENGTH_SHORT).show();
                }
            });

            imageView.setTag(appInfo);
            appIconsLayout.addView(imageView, params);
        }
    }
    public void checkAndHandleDoubleTap(float x, float y) {
        for (int i = 0; i < appIconsLayout.getChildCount(); i++) {
            ImageView imageView = (ImageView) appIconsLayout.getChildAt(i);
            if (isTouched(imageView, x, y)) {
                AppInfo1 appInfo = (AppInfo1) imageView.getTag();
                if (appInfo != null) {
                    String packageName = appInfo.getPackageName(getContext());

                    Toast.makeText(getContext(), "Package: " + packageName, Toast.LENGTH_SHORT).show();

                    DisplayManager displayManager = (DisplayManager) getContext().getSystemService(Context.DISPLAY_SERVICE);
                    Display[] displays = displayManager.getDisplays();

                    if (displays.length > 1) {
                        // Get the secondary display (index 1 is typically the secondary display)
                        Display secondaryDisplay = displays[1];

                        //close the presentation
                        dismiss();



                        // Create an intent to launch the app
                        Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(packageName);

                        if (intent != null) {
                            MyAccessibilityService service = MyAccessibilityService.getInstance();
                            if (service != null) {
                                AccessibilityServiceInfo info = new AccessibilityServiceInfo();
                                info.packageNames = new String[]{packageName};
                                service.setServiceInfo(info);  // Update service info at runtime
                            }


                            // Use ActivityOptions to specify the display
                            ActivityOptions options = ActivityOptions.makeBasic();
                            options.setLaunchDisplayId(secondaryDisplay.getDisplayId());


                            // Launch the app on the secondary display
                            getContext().startActivity(intent, options.toBundle());
                        }
                    }
                }
                break;  // Break after handling the first touched icon
            }
        }
    }

    private boolean isTouched(View view, float x, float y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        Rect rect = new Rect(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
        return rect.contains((int) x, (int) y);
    }

}