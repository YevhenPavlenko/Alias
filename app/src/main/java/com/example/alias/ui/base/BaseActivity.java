package com.example.alias.ui.base;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alias.ui.main.MainActivity;

public abstract class BaseActivity extends AppCompatActivity {

    private long backPressedTime;
    private Toast backToast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleCustomBackPress();
            }
        });
    }

    protected void animateButtons(View... views) {
        com.example.alias.util.ButtonAnimator.applyToViews(this, views);
    }

    protected void handleCustomBackPress() {
        if (this instanceof MainActivity) {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                if (backToast != null) backToast.cancel();
                finish();
            } else {
                backToast = showToast("Натисніть ще раз, щоб вийти");
                backPressedTime = System.currentTimeMillis();
            }
        } else {
            finish();
        }
    }

    protected Toast showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
        return toast;
    }
}
