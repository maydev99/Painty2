package com.droidloft.painty2;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Locale;

import top.defaults.colorpicker.ColorObserver;
import top.defaults.colorpicker.ColorPickerView;


public class ColorPicker extends AppCompatActivity {
    private ColorPickerView colorPickerView;
    private TextView hexCodeTextView;
    private static final int INITIAL_COLOR = 0xFFFF8000;
    private static final String SAVED_STATE_KEY_COLOR = "saved_state_key_color";
    private String colorHex, colorStr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_picker);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        loadColorPrefs();

        //int colorInt = (Integer.parseInt(colorStr));
        //int myColor = Color.parseColor(colorStr);
        //long myColor = Long.parseLong(colorStr, 16);

        hexCodeTextView = findViewById(R.id.hexCodeTextView);

        colorPickerView = findViewById(R.id.colorPicker);
        colorPickerView.setInitialColor((int) Long.parseLong(colorStr, 16));

        colorPickerView.subscribe(new ColorObserver() {
            @Override
            public void onColor(int color, boolean fromUser, boolean shouldPropagate) {
                //pickedColor.setBackgroundColor(color);
                colorHex = ColorPicker.this.colorHex(color);
                hexCodeTextView.setText(colorHex);

                saveColor();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ColorPicker.this.getWindow().setStatusBarColor(color);
                }
                ActionBar actionBar = ColorPicker.this.getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setBackgroundDrawable(new ColorDrawable(color));
                }
            }
        });

        //int color = INITIAL_COLOR;
        int color = (int) Long.parseLong(colorStr, 16);
        if (savedInstanceState != null) {
            color = savedInstanceState.getInt(SAVED_STATE_KEY_COLOR, INITIAL_COLOR);
        }
        colorPickerView.setInitialColor(color);









    }

    private void saveColor() {
        SharedPreferences colorPrefs = getSharedPreferences("color_key", MODE_PRIVATE);
        SharedPreferences.Editor colorEditor = colorPrefs.edit();
        colorEditor.putString("color_key", colorHex);
        colorEditor.apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_STATE_KEY_COLOR, colorPickerView.getColor());
    }

    private String colorHex(int color) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return String.format(Locale.getDefault(), "0x%02X%02X%02X%02X", a, r, g, b);
    }

    private void loadColorPrefs() {
        SharedPreferences colorPrefs = getSharedPreferences("color_key", MODE_PRIVATE);
        colorStr = colorPrefs.getString("color_key", "0xffffffff");
        //makeAToast(colorStr);
        colorStr = colorStr.substring(2, 10);
        //colorStr = "0x" + colorStr;

    }


}
