package com.example.dynabook_0001;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;

import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Button;

import android.widget.GridLayout;

import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private AppListAdapter adapter;
    private List<AppInfo1> selectedApps;
    private GridLayout appIconsLayout;
    private ExternalDisplayPresentation externalDisplayPresentation;
    private static final int REQUEST_OVERLAY_PERMISSION = 1000;
    private static final int REQUEST_ACCESSIBILITY_PERMISSION = 1001;
    private WindowManager secondaryWindowManager;
    private CursorView cursorRenderer;
    private Display secondaryDisplay;
    private float previousX, previousY;
    private boolean isMoving;
    private boolean isCursorInitialized = false;
    private boolean isTouchpadInitialized = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
        } else {
            checkAndRequestAccessibilityPermission();
        }
     //<---- NAVIGATION AND TOOLBAR  BEGINS------------------>

        // Initialize toolbar and navigation

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize dynamic ListView
        List<AppInfo1> installedApps = getInstalledApps();
        adapter = new AppListAdapter(this, installedApps);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
        selectedApps = adapter.getSelectedApps();

        // Initialize buttons in navigation menu
        Button btnOk = findViewById(R.id.btnOk);
        Button btnCancel = findViewById(R.id.btnCancel);

        // Set button listeners
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        // Set button background tint
        ColorStateList tintList = ColorStateList.valueOf(Color.parseColor("#dcf8d2"));
        btnOk.setBackgroundTintList(tintList);
        btnCancel.setBackgroundTintList(tintList);

        Button activateSecondaryDisplayButton = findViewById(R.id.Home);
        activateSecondaryDisplayButton.setBackgroundResource(R.drawable.homeicon);
        activateSecondaryDisplayButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#000000")));
        activateSecondaryDisplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Detect external display
                detectExternalDisplay();
            }
        });

    }

    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
    }

    private void checkAndRequestAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            requestAccessibilityPermission();
        }
    }

    private void requestAccessibilityPermission() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivityForResult(intent, REQUEST_ACCESSIBILITY_PERMISSION);
        Toast.makeText(this, "Please enable accessibility service for this app.", Toast.LENGTH_SHORT).show();
    }

    private boolean isAccessibilityServiceEnabled() {
        String enabledServices = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        ComponentName componentName = new ComponentName(this, MyAccessibilityService.class);
        String flatComponentName = componentName.flattenToString();

        return enabledServices != null && (enabledServices.contains(flatComponentName) || enabledServices.contains(getPackageName() + ":" + flatComponentName));
    }

    private void showSecondaryDisplayNotFoundDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Secondary Display Not Found")
                .setMessage("A secondary display is not available. Please connect an external display to launch the app.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private List<AppInfo1> getInstalledApps() {
        List<AppInfo1> appInfoList = new ArrayList<>();

        PackageManager packageManager = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appList = packageManager.queryIntentActivities(mainIntent, 0);

        for (ResolveInfo resolveInfo : appList) {
            AppInfo1 appInfo = new AppInfo1(
                    resolveInfo.loadIcon(packageManager),
                    resolveInfo.loadLabel(packageManager).toString(),
                    resolveInfo.activityInfo.packageName
            );
            appInfoList.add(appInfo);
        }


        return appInfoList;
    }

    private void detectExternalDisplay() {
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays();
        if (displays.length > 1) {
            secondaryDisplay = displays[1];
            showPresentation(secondaryDisplay);

            if (!isTouchpadInitialized) {
                setupTouchpadView();
            }

            if (!isCursorInitialized) {
                setupCursorOverlay();
            }

        } else {
            showSecondaryDisplayNotFoundDialog();
        }
    }

    private void showPresentation(Display display) {
        if (externalDisplayPresentation != null) {
            externalDisplayPresentation.dismiss();
            externalDisplayPresentation = null;
        }
        externalDisplayPresentation = new ExternalDisplayPresentation(this, display,selectedApps);
        externalDisplayPresentation.show();
    }

    private void setupCursorOverlay() {
        if (isCursorInitialized) return;

        secondaryWindowManager = (WindowManager) createDisplayContext(secondaryDisplay).getSystemService(WINDOW_SERVICE);
        cursorRenderer = new CursorView(this);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                50,
                50,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.LEFT;

        secondaryWindowManager.addView(cursorRenderer, params);
        isCursorInitialized = true; // Mark as initialized
    }

    private void setupTouchpadView() {
        if (isTouchpadInitialized) return;

        findViewById(R.id.touchableView).setOnTouchListener(this::handleTouchEvent);
        isTouchpadInitialized = true; // Mark as initialized
    }

    private static final int DOUBLE_TAP_TIMEOUT = 300; // 300ms
    private long lastTapTime;
    private boolean isDoubleTap;

    private boolean handleTouchEvent(View v, MotionEvent event) {
        handleCursorMovement(event);
        return true;
    }

    private void handleCursorMovement(MotionEvent event) {
        float currentX = event.getX();
        float currentY = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                initializeTouch(currentX, currentY);
                break;

            case MotionEvent.ACTION_MOVE:
                if (isMoving) {
                    handleMovement(event, currentX, currentY);
                }
                break;

            case MotionEvent.ACTION_UP:
                finalizeTouch(event);
                break;
        }
    }

    private void initializeTouch(float x, float y) {
        previousX = x;
        previousY = y;
        isMoving = true;
        isDoubleTap = false;
    }

    private void handleMovement(MotionEvent event, float currentX, float currentY) {
        if (event.getPointerCount() == 2) {
            handleTwoFingerSwipe(event);
        } else {
            moveCursor(currentX - previousX, currentY - previousY);
            previousX = currentX;
            previousY = currentY;
        }
    }

    private void finalizeTouch(MotionEvent event) {
        isMoving = false;
        if (event.getEventTime() - lastTapTime < DOUBLE_TAP_TIMEOUT) {
            isDoubleTap = true;
        } else {
            lastTapTime = event.getEventTime();
        }

        triggerClickableElementCheck();
    }

    private void handleTwoFingerSwipe(MotionEvent event) {
        float[] positions = calculateAverageMovement(event);

        // Determine if it's a scroll up/down or left/right gesture
        if (Math.abs(positions[1]) > Math.abs(positions[0])) {
            int scrollAction = positions[1] > 0
                    ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                    : AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD;
            triggerScrollAction(scrollAction);
        }

        updatePreviousPosition(event);
    }

    private float[] calculateAverageMovement(MotionEvent event) {
        float deltaX1 = event.getX(0) - previousX;
        float deltaY1 = event.getY(0) - previousY;
        float deltaX2 = event.getX(1) - previousX;
        float deltaY2 = event.getY(1) - previousY;

        return new float[]{(deltaX1 + deltaX2) / 2, (deltaY1 + deltaY2) / 2};
    }

    private void updatePreviousPosition(MotionEvent event) {
        previousX = (event.getX(0) + event.getX(1)) / 2;
        previousY = (event.getY(0) + event.getY(1)) / 2;
    }

    private void triggerScrollAction(int scrollAction) {
        MyAccessibilityService service = MyAccessibilityService.getInstance();
        if (service == null || service.getRootInActiveWindow() == null) {
            showToast(service == null ? "Accessibility service not available." : "Root node is null.");
            return;
        }

        AccessibilityNodeInfo scrollableNode = findScrollableNode(service.getRootInActiveWindow());
        if (scrollableNode != null) {
            scrollableNode.performAction(scrollAction);
        }
    }

    private AccessibilityNodeInfo findScrollableNode(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) return null;

        if (rootNode.isScrollable()) {
            return rootNode;
        }

        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo result = findScrollableNode(rootNode.getChild(i));
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private void moveCursor(float deltaX, float deltaY) {
        if (cursorRenderer != null && secondaryDisplay != null) {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) cursorRenderer.getLayoutParams();

            Point screenSize = new Point();
            secondaryDisplay.getRealSize(screenSize);

            params.x = (int) Math.max(0, Math.min(params.x + deltaX, screenSize.x - cursorRenderer.getWidth()));
            params.y = (int) Math.max(0, Math.min(params.y + deltaY, screenSize.y - cursorRenderer.getHeight()));
            secondaryWindowManager.updateViewLayout(cursorRenderer, params);
        }
    }

    private void triggerClickableElementCheck() {
        if (cursorRenderer == null) return;

        WindowManager.LayoutParams params = (WindowManager.LayoutParams) cursorRenderer.getLayoutParams();
        int centerX = params.x + cursorRenderer.getWidth() / 2;
        int centerY = params.y + cursorRenderer.getHeight() / 2;

        // Only handle double-tap if the external display presentation is active
        if (isDoubleTap && externalDisplayPresentation != null && externalDisplayPresentation.isShowing()) {
            externalDisplayPresentation.checkAndHandleDoubleTap(centerX, centerY);
        } else {
            // Existing functionality or fallback behavior
            checkClickableElementAtPosition(centerX, centerY);
        }
    }

    private void checkClickableElementAtPosition(int x, int y) {
        Toast.makeText(this, "1", Toast.LENGTH_SHORT).show();
        MyAccessibilityService service = MyAccessibilityService.getInstance();
        if (service == null || service.getRootInActiveWindow() == null) {
            showToast(service == null ? "Accessibility service not available." : "Root node is null.");
            return;
        }

        AccessibilityNodeInfo nodeAtPosition = findClickableNodeAtPosition(service.getRootInActiveWindow(), x, y);
        if (nodeAtPosition != null) {
            if (isDoubleTap) {
                nodeAtPosition.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } else {
                nodeAtPosition.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            }
        }
    }

    private AccessibilityNodeInfo findClickableNodeAtPosition(AccessibilityNodeInfo node, int x, int y) {
        if (node == null) return null;

        Rect nodeBounds = new Rect();
        node.getBoundsInScreen(nodeBounds);

        if (nodeBounds.contains(x, y) && node.isClickable()) {
             return node;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo foundNode = findClickableNodeAtPosition(node.getChild(i), x, y);
            if (foundNode != null) return foundNode;
        }

        return null;
    }
    private void showToast(String message) {
       // Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dismiss the presentation if it is active
        if (externalDisplayPresentation != null) {
            externalDisplayPresentation.dismiss();
            externalDisplayPresentation = null;
        }

        // Remove the cursor overlay if it exists
        if (cursorRenderer != null && secondaryWindowManager != null) {
            secondaryWindowManager.removeView(cursorRenderer);
            cursorRenderer = null;
            isCursorInitialized = false; // Reset the cursor initialization flag
        }

        // Reset the touchpad initialization flag
        isTouchpadInitialized = false;
    }


}