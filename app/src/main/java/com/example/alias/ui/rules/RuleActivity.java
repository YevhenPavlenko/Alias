package com.example.alias.ui.rules;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.example.alias.R;
import com.example.alias.ui.base.BaseActivity;

public class RuleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
        setupHeader(R.string.rules);

        TextView tvRules = findViewById(R.id.tvRules);
        tvRules.setText(Html.fromHtml(getString(R.string.game_rules), Html.FROM_HTML_MODE_LEGACY));
    }
}