package com.example.dynabook_0001;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class CursorView extends View {
    private Paint paint;
    private Bitmap cursorBitmap;

    public CursorView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        cursorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.circle);

        // Resize the bitmap to match the desired cursor size
        int desiredWidth = dpToPx(10); // Example size in dp
        int desiredHeight = dpToPx(10); // Example size in dp
        cursorBitmap = Bitmap.createScaledBitmap(cursorBitmap, desiredWidth, desiredHeight, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(cursorBitmap, 0, 0, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Use the resized bitmap dimensions
        int width = cursorBitmap.getWidth();
        int height = cursorBitmap.getHeight();
        setMeasuredDimension(width, height);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}

