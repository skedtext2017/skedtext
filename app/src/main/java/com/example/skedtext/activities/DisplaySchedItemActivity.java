package com.example.skedtext.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.skedtext.R;

public class DisplaySchedItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_sched_item);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_left_arrow);
    }
}
