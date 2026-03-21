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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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

// todo: разработка новой функции: разбитый экран

public class MainActivity extends Activity {

    private SharedPreferences prefs;
    private LinearLayout normalModeLayout;
    private RelativeLayout editModeLayout;
    private LinearLayout root;
    private SeekBar seekLeft, seekRight, seekTop, seekBottom;
    private TextView tvLeft, tvRight, tvTop, tvBottom;
    private LenterIME.KeyboardView previewKeyboard;
    private int maxOffset = 200;

    private int rootPaddingLeft, rootPaddingRight;
    private int editPaddingLeft, editPaddingRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFFFFFFF);
        root.setPadding(48, 48, 48, 48);

        normalModeLayout = new LinearLayout(this);
        normalModeLayout.setOrientation(LinearLayout.VERTICAL);
        normalModeLayout.setGravity(Gravity.CENTER);

        TextView title = new TextView(this);
        title.setText("Lenter Keyboard");
        title.setTextColor(0xFF000000);
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, 48);
        normalModeLayout.addView(title);

        TextView dRU = new TextView(this);
        dRU.setText("Lenter: Лёгкая клавиатура с мгновенным откликом! Нажмите “Активировать” и нажмите на нашу клавиатуру, потом в настройки → расширенные настройки → язык и ввод → текущая клавиатура, и выберите нашу. Готово!");
        dRU.setTextColor(0xFF000000);
        dRU.setTextSize(14);
        dRU.setPadding(0, 0, 0, 24);
        dRU.setGravity(Gravity.CENTER);
        normalModeLayout.addView(dRU);

        TextView dEN = new TextView(this);
        dEN.setText("Lenter: Lightweight keyboard with instant response! Press “Activate” and tap our keyboard, then go to settings → advanced → language & input → current keyboard and select ours. Done!");
        dEN.setTextColor(0xFF000000);
        dEN.setTextSize(14);
        dEN.setPadding(0, 0, 0, 32);
        dEN.setGravity(Gravity.CENTER);
        normalModeLayout.addView(dEN);

        TextView rz = new TextView(this);
        rz.setText("------------------------");
        rz.setTextColor(0xFF000000);
        rz.setTextSize(14);
        rz.setPadding(0, 0, 0, 16);
        rz.setGravity(Gravity.CENTER);
        normalModeLayout.addView(rz);

        TextView bsRU = new TextView(this);
        bsRU.setText("Функция для разбитых экранов: если часть дисплея не работает, вы можете сдвинуть клавиатуру в рабочую зону. Нажмите “Break screen” и настройте отступы.");
        bsRU.setTextColor(0xFF000000);
        bsRU.setTextSize(12);
        bsRU.setPadding(0, 0, 0, 16);
        bsRU.setGravity(Gravity.CENTER);
        normalModeLayout.addView(bsRU);

        TextView bsEN = new TextView(this);
        bsEN.setText("Broken screen feature: if part of the screen is broken, you can shift the keyboard into the working area. Press “Break screen” and adjust the offsets.");
        bsEN.setTextColor(0xFF000000);
        bsEN.setTextSize(12);
        bsEN.setPadding(0, 0, 0, 32);
        bsEN.setGravity(Gravity.CENTER);
        normalModeLayout.addView(bsEN);

        TextView btnActivate = new TextView(this);
        btnActivate.setText("АКТИВИРОВАТЬ");
        btnActivate.setTextColor(0xFFFFFFFF);
        btnActivate.setBackgroundColor(0xFF252525);
        btnActivate.setPadding(80, 40, 80, 40);
        btnActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            }
        });
        FrameLayout btnWrap = new FrameLayout(this);
        btnWrap.addView(btnActivate);
        normalModeLayout.addView(btnWrap);

        TextView krz = new TextView(this);
        krz.setText("------------------------");
        krz.setTextColor(0xFF000000);
        krz.setTextSize(14);
        krz.setPadding(0, 0, 0, 16);
        krz.setGravity(Gravity.CENTER);
        normalModeLayout.addView(krz);

        TextView btnBreak = new TextView(this);
        btnBreak.setText("Break screen");
        btnBreak.setTextColor(0xFFFFFFFF);
        btnBreak.setBackgroundColor(0xFF252525);
        btnBreak.setPadding(80, 40, 80, 40);
        btnBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditMode();
            }
        });
        FrameLayout btnBreakWrap = new FrameLayout(this);
        btnBreakWrap.addView(btnBreak);
        normalModeLayout.addView(btnBreakWrap);

        root.addView(normalModeLayout);

        editModeLayout = new RelativeLayout(this);
        editModeLayout.setBackgroundColor(0xFFFFFFFF);
        editModeLayout.setVisibility(View.GONE);

        LinearLayout controlsPanel = new LinearLayout(this);
        controlsPanel.setOrientation(LinearLayout.VERTICAL);
        controlsPanel.setPadding(24, 24, 24, 24);
        controlsPanel.setBackgroundColor(0xFFEEEEEE);

        TextView editTitle = new TextView(this);
        editTitle.setText("Настройка битых зон экрана / Broken screen adjustment");
        editTitle.setTextSize(18);
        editTitle.setTextColor(0xFF000000);
        editTitle.setPadding(0, 0, 0, 24);
        controlsPanel.addView(editTitle);

        LinearLayout leftRow = createRow("Слева / Left (px):");
        seekLeft = (SeekBar) leftRow.getTag();
        tvLeft = (TextView) leftRow.findViewById(android.R.id.text1);
        controlsPanel.addView(leftRow);

        LinearLayout rightRow = createRow("Справа / Right (px):");
        seekRight = (SeekBar) rightRow.getTag();
        tvRight = (TextView) rightRow.findViewById(android.R.id.text1);
        controlsPanel.addView(rightRow);

        LinearLayout topRow = createRow("Сверху / Top (px):");
        seekTop = (SeekBar) topRow.getTag();
        tvTop = (TextView) topRow.findViewById(android.R.id.text1);
        controlsPanel.addView(topRow);

        LinearLayout bottomRow = createRow("Снизу / Bottom (px):");
        seekBottom = (SeekBar) bottomRow.getTag();
        tvBottom = (TextView) bottomRow.findViewById(android.R.id.text1);
        controlsPanel.addView(bottomRow);

        TextView btnSave = new TextView(this);
        btnSave.setText("Готово / Done");
        btnSave.setTextColor(0xFFFFFFFF);
        btnSave.setBackgroundColor(0xFF252525);
        btnSave.setPadding(80, 40, 80, 40);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettingsAndExit();
            }
        });
        controlsPanel.addView(btnSave);

        RelativeLayout.LayoutParams controlsParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        controlsParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        editModeLayout.addView(controlsPanel, controlsParams);

        FrameLayout keyboardContainer = new FrameLayout(this);
        keyboardContainer.setPadding(0, 16, 0, 0);

        RelativeLayout.LayoutParams keyboardParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        keyboardParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        editModeLayout.addView(keyboardContainer, keyboardParams);

        root.addView(editModeLayout);
        setContentView(root);

        prefs = getSharedPreferences("LenterPrefs", MODE_PRIVATE);
        loadSettingsIntoSeekBars();

        createPreviewKeyboard(keyboardContainer);
    }

    private LinearLayout createRow(String labelText) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        TextView label = new TextView(this);
        label.setText(labelText);
        label.setTextColor(0xFF000000);
        label.setWidth(200);
        row.addView(label);

        SeekBar seek = new SeekBar(this);
        seek.setMax(maxOffset);
        LinearLayout.LayoutParams seekParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        seek.setLayoutParams(seekParams);
        row.addView(seek);

        TextView value = new TextView(this);
        value.setId(android.R.id.text1);
        value.setText("0");
        value.setWidth(80);
        value.setTextColor(0xFF000000);
        row.addView(value);

        row.setTag(seek);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView tv = (TextView) ((View) seekBar.getParent()).findViewById(android.R.id.text1);
                tv.setText(String.valueOf(progress));
                updatePreviewOffsets();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return row;
    }

    private void createPreviewKeyboard(FrameLayout container) {
        float density = getResources().getDisplayMetrics().density;
        int height = (int) (250 * density + 0.5f);
        String lang = prefs.getString("active_lang", "ru");

        previewKeyboard = new LenterIME.KeyboardView(this);
        previewKeyboard.setIsPreviewMode(true);
        previewKeyboard.setParams(height, lang);
        previewKeyboard.setOffsets(seekLeft.getProgress(), seekRight.getProgress(),
                                   seekTop.getProgress(), seekBottom.getProgress());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, height);
        container.addView(previewKeyboard, params);
    }

    private void updatePreviewOffsets() {
        if (previewKeyboard != null) {
            previewKeyboard.setOffsets(seekLeft.getProgress(), seekRight.getProgress(),
                                       seekTop.getProgress(), seekBottom.getProgress());
        }
    }

    private void loadSettingsIntoSeekBars() {
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

    private void saveSettingsAndExit() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("broken_left", seekLeft.getProgress());
        editor.putInt("broken_right", seekRight.getProgress());
        editor.putInt("broken_top", seekTop.getProgress());
        editor.putInt("broken_bottom", seekBottom.getProgress());
        editor.apply();

        LenterIME.updateOffsets(this);
        exitEditMode();
    }

    private void showEditMode() {
        rootPaddingLeft = root.getPaddingLeft();
        rootPaddingRight = root.getPaddingRight();
        editPaddingLeft = editModeLayout.getPaddingLeft();
        editPaddingRight = editModeLayout.getPaddingRight();

        root.setPadding(0, root.getPaddingTop(), 0, root.getPaddingBottom());
        editModeLayout.setPadding(0, editModeLayout.getPaddingTop(), 0, editModeLayout.getPaddingBottom());

        normalModeLayout.setVisibility(View.GONE);
        editModeLayout.setVisibility(View.VISIBLE);

        if (previewKeyboard != null) {
            FrameLayout parent = (FrameLayout) previewKeyboard.getParent();
            parent.removeView(previewKeyboard);
            createPreviewKeyboard(parent);
            updatePreviewOffsets();
        }
    }

    private void exitEditMode() {
        root.setPadding(rootPaddingLeft, root.getPaddingTop(), rootPaddingRight, root.getPaddingBottom());
        editModeLayout.setPadding(editPaddingLeft, editModeLayout.getPaddingTop(), editPaddingRight, editModeLayout.getPaddingBottom());

        editModeLayout.setVisibility(View.GONE);
        normalModeLayout.setVisibility(View.VISIBLE);
    }
}