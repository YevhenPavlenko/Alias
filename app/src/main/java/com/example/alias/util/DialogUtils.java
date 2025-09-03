package com.example.alias.util;

import android.app.AlertDialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alias.R;

import java.util.Objects;

public class DialogUtils {

    public static View inflateDialogView(Context context, int layoutRes) {
        ViewGroup root = ((android.app.Activity) context).findViewById(android.R.id.content);
        return LayoutInflater.from(context).inflate(layoutRes, root, false);
    }

    public static AlertDialog buildDialog(Context context, View dialogView) {
        return new AlertDialog.Builder(context, R.style.CustomDialog)
                .setView(dialogView)
                .setCancelable(true)
                .create();
    }

    public static void setDialogWidth(AlertDialog dialog, Context context, int dpWidth) {
        Objects.requireNonNull(dialog.getWindow()).setLayout(
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, dpWidth, context.getResources().getDisplayMetrics()
                ),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }
}