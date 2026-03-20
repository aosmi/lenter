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
import android.provider.Settings;
import android.graphics.Typeface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFFFFFFF);
        root.setPadding(48, 48, 48, 48);
        root.setGravity(android.view.Gravity.CENTER);

        TextView title = new TextView(this);
        title.setText("Lenter Keyboard");
        title.setTextColor(0xFF000000);
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, 48);
        root.addView(title);

        TextView descRu = new TextView(this);
        descRu.setText("Lenter: Лёгкая клавиатура с мгновенным откликом! Нажмите “Активировать” и нажмите на нашу клавиатуру, потом в настройки → расширенные настройки → язык и ввод → текущая клавиатура, и выберите нашу. Готово!");
        descRu.setTextColor(0xFF000000);
        descRu.setTextSize(14);
        descRu.setPadding(0, 0, 0, 24);
        descRu.setGravity(android.view.Gravity.CENTER);
        root.addView(descRu);

        TextView descEn = new TextView(this);
        descEn.setText("Lenter: Lightweight keyboard with instant response! Press “Activate” and tap our keyboard, then go to settings → advanced → language & input → current keyboard and select ours. Done!");
        descEn.setTextColor(0xFF000000);
        descEn.setTextSize(14);
        descEn.setPadding(0, 0, 0, 32);
        descEn.setGravity(android.view.Gravity.CENTER);
        root.addView(descEn);

        TextView btn = new TextView(this);
        btn.setText("АКТИВИРОВАТЬ");
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundColor(0xFF252525);
        btn.setPadding(80, 40, 80, 40);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            }
        });

        FrameLayout btnWrap = new FrameLayout(this);
        btnWrap.addView(btn);
        root.addView(btnWrap);

        setContentView(root);
    }
}