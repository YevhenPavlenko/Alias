package com.example.alias.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.example.alias.R;

public class DialogUtils {

    public static View inflateDialogView(Context context, int layoutRes) {
        ViewGroup root = ((Activity) context).findViewById(android.R.id.content);
        return LayoutInflater.from(context).inflate(layoutRes, root, false);
    }

    public static AlertDialog buildDialog(Context context, View dialogView) {
        AlertDialog dialog = new AlertDialog.Builder(context, R.style.CustomDialog)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.setOnShowListener(d -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setGravity(Gravity.CENTER);

                WindowManager.LayoutParams params = window.getAttributes();
                params.gravity = Gravity.CENTER;
                window.setAttributes(params);
            }
        });

        return dialog;
    }

    public static void setDialogWidth(AlertDialog dialog, Context context, int dpWidth) {
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        int width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpWidth,
                context.getResources().getDisplayMetrics()
        );

        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }
}