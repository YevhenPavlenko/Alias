package com.example.alias.util;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.alias.R;

public class ButtonAnimator {

    public static void applyTo(View view, Context context) {
        Animation scaleAnim = AnimationUtils.loadAnimation(context, R.anim.button_scale);

        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.startAnimation(scaleAnim);
                    return false;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    return false;
            }
            return false;
        });
    }

    public static void applyToViews(Context context, View... views) {
        for (View view : views) {
            applyTo(view, context);
        }
    }
}
