package com.example.alias.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.DashPathEffect;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DashedZoneDrawable extends Drawable {

    private final Paint borderPaint;
    private final Paint dashPaint;
    private final Paint backgroundPaint;

    public DashedZoneDrawable(int color, float borderWidth) {
        int semiTransparentColor = Color.argb(128, Color.red(color), Color.green(color), Color.blue(color));

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(semiTransparentColor);
        backgroundPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(color);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);

        dashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dashPaint.setColor(color);
        dashPaint.setStyle(Paint.Style.STROKE);
        dashPaint.setStrokeWidth(3f);
        dashPaint.setPathEffect(new DashPathEffect(new float[]{25, 20}, 0));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();

        canvas.drawRoundRect(
                bounds.left, bounds.top,
                bounds.right, bounds.bottom,
                24, 24, backgroundPaint
        );

        int spacing = 60;
        for (int x = -bounds.height(); x < bounds.width(); x += spacing) {
            canvas.drawLine(
                    bounds.left + x, bounds.top,
                    bounds.left + x + bounds.height(), bounds.bottom,
                    dashPaint
            );
        }

        canvas.drawRoundRect(
                bounds.left + 1, bounds.top + 1,
                bounds.right - 1, bounds.bottom - 1,
                24, 24, borderPaint
        );
    }

    @Override
    public void setAlpha(int alpha) {
        borderPaint.setAlpha(alpha);
        dashPaint.setAlpha(alpha);
        backgroundPaint.setAlpha(alpha);
    }

    @Override public void setColorFilter(@Nullable android.graphics.ColorFilter colorFilter) {}
    @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
}