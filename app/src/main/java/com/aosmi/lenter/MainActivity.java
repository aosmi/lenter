package com.aosmi.lenter;

/*
 * MIT License
 *
 * Copyright (c) 2026 Aosmi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private SharedPreferences prefs;
    private LinearLayout normalLayout;
    private RelativeLayout editLayout;
    private SeekBar seekLeft, seekRight, seekTop, seekBottom;
    private TextView tvLeft, tvRight, tvTop, tvBottom;
    private LenterIME.KeyboardView previewKeyboard;
    private FrameLayout keyboardContainer;
    private LinearLayout rootLayout;

    private int rootPaddingLeft, rootPaddingRight;
    private int editPaddingLeft, editPaddingRight;

    private static final String[] THEME_NAMES = {"Material", "Lenter"};
    private static final int[] THEME_VALUES = {1, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("LenterPrefs", MODE_PRIVATE);

        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(0xFF000000);

        normalLayout = new LinearLayout(this);
        normalLayout.setOrientation(LinearLayout.VERTICAL);
        normalLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        normalLayout.setBackgroundColor(0xFF000000);
        normalLayout.setPadding(dp(16), dp(24), dp(16), dp(24));

        TextView title = new TextView(this);
        title.setText("Lenter Keyboard");
        title.setTextColor(0xFFFF6600);
        title.setTextSize(28);
        title.setTypeface(null, 1);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(32));
        normalLayout.addView(title);

        addButton(normalLayout, "Activate", new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            }
        });

        addButton(normalLayout, "Break screen", new View.OnClickListener() {
            public void onClick(View v) {
                showEditMode();
            }
        });

        addButton(normalLayout, "Theme", new View.OnClickListener() {
            public void onClick(View v) {
                showThemeDialog();
            }
        });

        TextView helpButton = new TextView(this);
        helpButton.setText("?");
        helpButton.setTextColor(0xFFFF6600);
        helpButton.setTextSize(24);
        helpButton.setGravity(Gravity.CENTER);
        helpButton.setPadding(dp(16), dp(12), dp(16), dp(12));
        helpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showHelpDialog();
            }
        });
        normalLayout.addView(helpButton);

        rootLayout.addView(normalLayout);

        editLayout = new RelativeLayout(this);
        editLayout.setBackgroundColor(0xFF000000);
        editLayout.setVisibility(View.GONE);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setPadding(dp(16), dp(16), dp(16), dp(16));
        controls.setBackgroundColor(0xFF111111);

        TextView editTitle = new TextView(this);
        editTitle.setText("Broken screen adjustment");
        editTitle.setTextColor(0xFFFF6600);
        editTitle.setTextSize(18);
        editTitle.setTypeface(null, 1);
        editTitle.setPadding(0, 0, 0, dp(16));
        controls.addView(editTitle);

        LinearLayout leftRow = createSeekRow("Left:", 0);
        seekLeft = (SeekBar) leftRow.getTag();
        tvLeft = (TextView) leftRow.findViewById(android.R.id.text1);
        controls.addView(leftRow);

        LinearLayout rightRow = createSeekRow("Right:", 1);
        seekRight = (SeekBar) rightRow.getTag();
        tvRight = (TextView) rightRow.findViewById(android.R.id.text1);
        controls.addView(rightRow);

        LinearLayout topRow = createSeekRow("Top:", 2);
        seekTop = (SeekBar) topRow.getTag();
        tvTop = (TextView) topRow.findViewById(android.R.id.text1);
        controls.addView(topRow);

        LinearLayout bottomRow = createSeekRow("Bottom:", 3);
        seekBottom = (SeekBar) bottomRow.getTag();
        tvBottom = (TextView) bottomRow.findViewById(android.R.id.text1);
        controls.addView(bottomRow);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setPadding(0, dp(16), 0, 0);

        TextView saveBtn = new TextView(this);
        saveBtn.setText("Save");
        saveBtn.setTextColor(0xFFFF6600);
        saveBtn.setBackgroundColor(0xFF666666);
        saveBtn.setGravity(Gravity.CENTER);
        saveBtn.setPadding(dp(16), dp(12), dp(16), dp(12));
        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveAndExit();
            }
        });
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        saveParams.setMargins(0, 0, dp(8), 0);
        saveBtn.setLayoutParams(saveParams);
        buttonRow.addView(saveBtn);

        TextView cancelBtn = new TextView(this);
        cancelBtn.setText("Cancel");
        cancelBtn.setTextColor(0xFFFF6600);
        cancelBtn.setBackgroundColor(0xFF666666);
        cancelBtn.setGravity(Gravity.CENTER);
        cancelBtn.setPadding(dp(16), dp(12), dp(16), dp(12));
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                exitEditMode();
            }
        });
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        cancelParams.setMargins(dp(8), 0, 0, 0);
        cancelBtn.setLayoutParams(cancelParams);
        buttonRow.addView(cancelBtn);

        controls.addView(buttonRow);

        RelativeLayout.LayoutParams controlsParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        controlsParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        editLayout.addView(controls, controlsParams);

        keyboardContainer = new FrameLayout(this);
        keyboardContainer.setPadding(0, dp(16), 0, 0);
        RelativeLayout.LayoutParams keyboardParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        keyboardParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        editLayout.addView(keyboardContainer, keyboardParams);

        rootLayout.addView(editLayout);
        setContentView(rootLayout);

        loadSeekValues();
    }

    private void addButton(LinearLayout parent, String text, View.OnClickListener listener) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(0xFFFF6600);
        btn.setBackgroundColor(0xFF666666);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(16), dp(14), dp(16), dp(14));
        btn.setTextSize(16);
        btn.setTypeface(null, 1);
        btn.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(12));
        btn.setLayoutParams(params);
        parent.addView(btn);
    }

    private LinearLayout createSeekRow(String label, int id) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(8), 0, dp(8));

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(0xFFFF6600);
        labelView.setWidth(dp(80));
        row.addView(labelView);

        SeekBar seek = new SeekBar(this);
        seek.setMax(200);
        LinearLayout.LayoutParams seekParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        seek.setLayoutParams(seekParams);
        row.addView(seek);

        TextView value = new TextView(this);
        value.setId(android.R.id.text1);
        value.setTextColor(0xFFFF6600);
        value.setText("0");
        value.setWidth(dp(60));
        row.addView(value);

        row.setTag(seek);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                TextView tv = (TextView) ((View) s.getParent()).findViewById(android.R.id.text1);
                tv.setText(String.valueOf(p));
                updatePreviewOffsets();
            }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });

        return row;
    }

    private void loadSeekValues() {
        int left = prefs.getInt("broken_left", 0);
        int right = prefs.getInt("broken_right", 0);
        int top = prefs.getInt("broken_top", 0);
        int bottom = prefs.getInt("broken_bottom", 0);
        if (seekLeft != null) {
            seekLeft.setProgress(left);
            tvLeft.setText(String.valueOf(left));
            seekRight.setProgress(right);
            tvRight.setText(String.valueOf(right));
            seekTop.setProgress(top);
            tvTop.setText(String.valueOf(top));
            seekBottom.setProgress(bottom);
            tvBottom.setText(String.valueOf(bottom));
        }
    }

    private void updatePreviewOffsets() {
        if (previewKeyboard != null) {
            previewKeyboard.setOffsets(seekLeft.getProgress(), seekRight.getProgress(),
                                       seekTop.getProgress(), seekBottom.getProgress());
        }
    }

    private void saveAndExit() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("broken_left", seekLeft.getProgress());
        editor.putInt("broken_right", seekRight.getProgress());
        editor.putInt("broken_top", seekTop.getProgress());
        editor.putInt("broken_bottom", seekBottom.getProgress());
        editor.apply();
        LenterIME.updateOffsets(this);
        exitEditMode();
    }

    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Help / Помощь");
        builder.setMessage(
            "Activate / Активировать\n" +
            "Tap Activate → Select Lenter Keyboard → Switch to it\n" +
            "Нажмите Активировать → выберите Lenter Keyboard → переключитесь на клавиатуру\n\n" +
            "Break screen / Битый экран\n" +
            "Move keyboard to working area if part of screen is broken\n" +
            "Сдвиньте клавиатуру в рабочую зону, если часть экрана не работает\n\n" +
            "Theme / Тема\n" +
            "2 themes available: Material, Lenter\n" +
            "Доступно 2 темы: Material, Lenter"
        );
        builder.setPositiveButton("Got it / Понятно", null);
        builder.show();
    }

    private void showThemeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select theme / Выберите тему");
        builder.setItems(THEME_NAMES, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                int theme = THEME_VALUES[which];
                prefs.edit().putInt("theme", theme).apply();
                LenterIME.updateTheme(MainActivity.this);
                if (previewKeyboard != null) {
                    previewKeyboard.setTheme(theme);
                }
            }
        });
        builder.show();
    }

 private void showEditMode() {
    rootPaddingLeft = rootLayout.getPaddingLeft();
    rootPaddingRight = rootLayout.getPaddingRight();
    editPaddingLeft = editLayout.getPaddingLeft();
    editPaddingRight = editLayout.getPaddingRight();

    rootLayout.setPadding(0, rootLayout.getPaddingTop(), 0, rootLayout.getPaddingBottom());
    editLayout.setPadding(0, editLayout.getPaddingTop(), 0, editLayout.getPaddingBottom());

    normalLayout.setVisibility(View.GONE);
    editLayout.setVisibility(View.VISIBLE);

    int currentTheme = prefs.getInt("theme", 1);
    String lang = prefs.getString("active_lang", "ru");
    float density = getResources().getDisplayMetrics().density;
    int height = (int) (260 * density + 0.5f);

    if (previewKeyboard == null && keyboardContainer != null) {
        previewKeyboard = new LenterIME.KeyboardView(this);
        previewKeyboard.setIsPreviewMode(true);
        previewKeyboard.setParams(height, lang);
        previewKeyboard.setOffsets(seekLeft.getProgress(), seekRight.getProgress(),
                                   seekTop.getProgress(), seekBottom.getProgress());
        previewKeyboard.setTheme(currentTheme);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, height);
        keyboardContainer.addView(previewKeyboard, params);
    } else if (previewKeyboard != null) {
        previewKeyboard.setTheme(currentTheme);
        previewKeyboard.setLanguage(lang);
        previewKeyboard.setParams(height, lang);
        previewKeyboard.setOffsets(seekLeft.getProgress(), seekRight.getProgress(),
                                   seekTop.getProgress(), seekBottom.getProgress());
    }
}

    private void exitEditMode() {
        rootLayout.setPadding(rootPaddingLeft, rootLayout.getPaddingTop(), rootPaddingRight, rootLayout.getPaddingBottom());
        editLayout.setPadding(editPaddingLeft, editLayout.getPaddingTop(), editPaddingRight, editLayout.getPaddingBottom());
        editLayout.setVisibility(View.GONE);
        normalLayout.setVisibility(View.VISIBLE);
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}