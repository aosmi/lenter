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

// импорты
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/* *** приложение *** */

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

    public static class LenterIME extends InputMethodService {
        private KeyboardView keyboardView;
        private int currentEnterAction = EditorInfo.IME_ACTION_UNSPECIFIED;
        private EditorInfo currentEditorInfo;

        @Override
        public View onCreateInputView() {
            SharedPreferences prefs = getSharedPreferences("LenterPrefs", MODE_PRIVATE);
            String lang = prefs.getString("active_lang", "ru");
            float density = getResources().getDisplayMetrics().density;
            int height = (int) (250 * density + 0.5f);

            keyboardView = new KeyboardView(this);
            keyboardView.setImeService(this);
            keyboardView.setParams(height, lang);

            LinearLayout container = new LinearLayout(this);
            container.setClipChildren(false);
            container.addView(keyboardView, new LinearLayout.LayoutParams(-1, height));
            return container;
        }

        @Override
        public void onStartInputView(EditorInfo info, boolean restarting) {
            super.onStartInputView(info, restarting);
            currentEditorInfo = info;
            SharedPreferences prefs = getSharedPreferences("LenterPrefs", MODE_PRIVATE);
            String lang = prefs.getString("active_lang", "ru");
            currentEnterAction = info.imeOptions & EditorInfo.IME_MASK_ACTION;
            if (keyboardView != null) {
                keyboardView.setLanguage(lang);
            }
        }

        public void saveLanguage(String lang) {
            getSharedPreferences("LenterPrefs", MODE_PRIVATE).edit().putString("active_lang", lang).apply();
        }

        public void commitText(String t) {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) ic.commitText(t, 1);
        }

        public void handleEnter() {
            InputConnection ic = getCurrentInputConnection();
            if (ic == null || currentEditorInfo == null) return;

            boolean shouldInsertNewline = (currentEditorInfo.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0;
            if (shouldInsertNewline) {
                ic.commitText("\n", 1);
                return;
            }

            switch (currentEnterAction) {
                case EditorInfo.IME_ACTION_SEARCH:
                case EditorInfo.IME_ACTION_GO:
                case EditorInfo.IME_ACTION_SEND:
                case EditorInfo.IME_ACTION_NEXT:
                case EditorInfo.IME_ACTION_DONE:
                case EditorInfo.IME_ACTION_PREVIOUS:
                    ic.performEditorAction(currentEnterAction);
                    break;
                default:
                    ic.commitText("\n", 1);
                    break;
            }
        }

        public boolean deleteSelected() {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                CharSequence selected = ic.getSelectedText(0);
                if (selected != null && selected.length() > 0) {
                    ic.commitText("", 1);
                    return true;
                }
            }
            return false;
        }

        public void deleteChar() {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) ic.deleteSurroundingText(1, 0);
        }
    }

/* *** клавиатура *** */

    public static class KeyboardView extends View {

        private final Rect tempRect = new Rect();
        private final RectF tempRectF = new RectF();
        private final Rect tempSrcRect = new Rect();
        private final RectF tempDstRectF = new RectF();

        // переменные
        private static final int BG = Color.BLACK;
        private static final int KEY = 0xFF252525;
        private static final int SPECIAL = 0xFF1A1A1A;
        private static final int ACCENT = 0xFF404040;
        private static final int GAP = 4;

        private static final int T_CHAR = 0, T_SHIFT = 1, T_LANG = 2, T_ENTER = 3, T_SPACE = 4,
                T_DEL = 5, T_SYM = 8, T_SYM_EX = 9, T_ABC = 10;

        private static final int LANG_RU = 0;
        private static final int LANG_EN = 1;
        private static final int LANG_SYM = 2;
        private static final int LANG_EXTRA = 3;

        private static final String SHIFT_NORMAL = "⇧";
        private static final String SHIFT_SINGLE = "⬆";
        private static final String SHIFT_LOCK = "⟰";

        private final Paint p1 = new Paint();
        private final Paint p2 = new Paint();
        private final Paint pH = new Paint();
        private final TextPaint pText = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pPre = new Paint();
        private final TextPaint pPreT = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pAtlas = new Paint();

        private LenterIME ime;
        private int screenW;
        private float keyH;
        private float textBaselineOffset, previewBaselineOffset;
        private float density;
        private int previewHeight;

        private int curLang = LANG_RU;
        private int lastLang = LANG_RU;
        private int shift = 0;

        private static class LayoutData {
            float[] keyCoords;
            String[] labels;
            String[] values;
            int[] types;
            int count;
            int[][] grid = new int[4][32];
            int[] gridCount = new int[4];
            short[] hitmap;
            int hitmapW, hitmapH;
            int[] normalIdx;
            int[] shiftIdx;
            Bitmap atlas;
            int atlasCellW, atlasCellH, atlasCols;
        }

        private LayoutData[] layouts = new LayoutData[4];
        private LayoutData curLayout;

        private int[] pointerIds = new int[10];
        private int[] pointerKeys = new int[10];

        private final Handler h = new Handler(Looper.getMainLooper());
        private boolean isDel = false;
        private final Runnable delRunnable = new Runnable() {
            @Override
            public void run() {
                if (isDel && ime != null) {
                    ime.deleteChar();
                    h.postDelayed(this, 40);
                }
            }
        };

        private final char[] tmpChar = new char[1];

        public KeyboardView(Context ctx) {
            super(ctx);
            if (Build.VERSION.SDK_INT >= 11) {
                setLayerType(LAYER_TYPE_SOFTWARE, null);
            }
            DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
            density = dm.density;
            screenW = dm.widthPixels;

            float fontSizeNormal = 22 * density;
            float fontSizePreview = 32 * density;
            previewHeight = (int) (50 * density);

            p1.setColor(KEY);
            p2.setColor(SPECIAL);
            pH.setColor(ACCENT);
            pText.setColor(Color.WHITE);
            pText.setTextAlign(Paint.Align.CENTER);
            pText.setTextSize(fontSizeNormal);
            pText.setFakeBoldText(true);
            pPre.setColor(0xEE252525);
            pPreT.setColor(Color.WHITE);
            pPreT.setTextAlign(Paint.Align.CENTER);
            pPreT.setTextSize(fontSizePreview);
            pAtlas.setColor(Color.WHITE);
            pAtlas.setAlpha(255);

            textBaselineOffset = (pText.descent() + pText.ascent()) / 2f;
            previewBaselineOffset = (pPreT.descent() + pPreT.ascent()) / 2f;

            Arrays.fill(pointerIds, -1);
            Arrays.fill(pointerKeys, -1);

            for (int i = 0; i < 4; i++) {
                layouts[i] = new LayoutData();
                layouts[i].keyCoords = new float[60 * 4];
                layouts[i].labels = new String[60];
                layouts[i].values = new String[60];
                layouts[i].types = new int[60];
                layouts[i].normalIdx = new int[60];
                layouts[i].shiftIdx = new int[60];
                Arrays.fill(layouts[i].normalIdx, -1);
                Arrays.fill(layouts[i].shiftIdx, -1);
            }
        }

        public void setImeService(LenterIME s) {
            this.ime = s;
        }

        public void setParams(int h, String l) {
            this.keyH = (h - previewHeight) / 4f;
            this.lastLang = (l.equals("ru") ? LANG_RU : LANG_EN);
            if (screenW > 0) {
                gen(l);
            }
        }

        public void setLanguage(String lang) {
            lastLang = (lang.equals("ru") ? LANG_RU : LANG_EN);
            gen(lang);
        }

// раскладка
        private void gen(String type) {
            int langIdx;
            if (type.equals("ru")) langIdx = LANG_RU;
            else if (type.equals("en")) langIdx = LANG_EN;
            else if (type.equals("sym")) langIdx = LANG_SYM;
            else langIdx = LANG_EXTRA;

            curLang = langIdx;
            LayoutData d = layouts[langIdx];
            d.count = 0;
            for (int i = 0; i < 4; i++) d.gridCount[i] = 0;

            float y = 0, w;
            if (langIdx == LANG_RU) {
                w = screenW / 11f;
                row(d, y, w, "йцукенгшщзх");
                y += keyH;
                row(d, y, w, "фывапролджэ");
                y += keyH;
                k(d, SHIFT_NORMAL, "SH", T_SHIFT, 0, y, w * 1.5f);
                rowW(d, y, w * 1.5f, (screenW - w * 3f) / 9f, "ячсмитьбю");
                k(d, "⌫", "DEL", T_DEL, screenW - w * 1.5f, y, w * 1.5f);
                y += keyH;
                drawBottom(d, y);
            } else if (langIdx == LANG_EN) {
                w = screenW / 10f;
                row(d, y, w, "qwertyuiop");
                y += keyH;
                rowW(d, y, w * 0.5f, w, "asdfghjkl");
                y += keyH;
                k(d, SHIFT_NORMAL, "SH", T_SHIFT, 0, y, w * 1.5f);
                rowW(d, y, w * 1.5f, w, "zxcvbnm");
                k(d, "⌫", "DEL", T_DEL, screenW - w * 1.5f, y, w * 1.5f);
                y += keyH;
                drawBottom(d, y);
            } else if (langIdx == LANG_SYM) {
                w = screenW / 10f;
                row(d, y, w, "1234567890");
                y += keyH;
                row(d, y, w, "@#№_&-+()/");
                y += keyH;
                k(d, "=\\<", "EX", T_SYM_EX, 0, y, w * 1.5f);
                rowW(d, y, w * 1.5f, w, "*\"':;!?");
                k(d, "⌫", "DEL", T_DEL, screenW - w * 1.5f, y, w * 1.5f);
                y += keyH;
                drawBottom(d, y);
            } else {
                w = screenW / 11f;
                row(d, y, w, "~`|•√π÷×¶∆●");
                y += keyH;
                row(d, y, w, "€$£¥₸₽^°={}");
                y += keyH;
                k(d, "?12", "SYM", T_SYM, 0, y, w * 1.5f);
                rowW(d, y, w * 1.5f, w, "\\©®™%[]■");
                k(d, "⌫", "DEL", T_DEL, screenW - w * 1.5f, y, w * 1.5f);
                y += keyH;
                drawBottom(d, y);
            }

            buildAtlas(d);
            buildHitmap(d);
            curLayout = d;
            needRedrawBg = true;
            invalidate();
        }

// todo: сделать чтобы клава не вылезала вне закона (исправлено)
        private void drawBottom(LayoutData d, float y) {
            float wSymWeight = 1.5f;
            float wCommaWeight = 1.0f;
            float wLangWeight = 1.3f;
            float wExtraWeight = (curLang == LANG_RU) ? 1.0f : 0f;
            float wSpaceWeight = 3.5f;
            float wExtraRightWeight = (curLang == LANG_RU) ? 1.0f : 0f;
            float wDotWeight = 1.0f;
            float wEnterWeight = 1.7f;

            float totalWeight = wSymWeight + wCommaWeight + wLangWeight +
                    wExtraWeight + wSpaceWeight + wExtraRightWeight +
                    wDotWeight + wEnterWeight;

            float unit = screenW / totalWeight;
            float x = 0;

            float w = wSymWeight * unit;
            String label = (curLang == LANG_RU || curLang == LANG_EN) ? "?12" : "ABC";
            int type = (curLang == LANG_RU || curLang == LANG_EN) ? T_SYM : T_ABC;
            k(d, label, "SYM_TOGGLE", type, x, y, w);
            x += w;

            w = wCommaWeight * unit;
            String comma = (curLang == LANG_EXTRA) ? "<" : ",";
            k(d, comma, comma, T_CHAR, x, y, w);
            x += w;

            w = wLangWeight * unit;
            k(d, (lastLang == LANG_RU) ? "RU" : "EN", "L", T_LANG, x, y, w);
            x += w;

            if (curLang == LANG_RU) {
                w = wExtraWeight * unit;
                k(d, "ё", "ё", T_CHAR, x, y, w);
                x += w;
            }

            w = wSpaceWeight * unit;
            k(d, " ", " ", T_SPACE, x, y, w);
            x += w;

            if (curLang == LANG_RU) {
                w = wExtraRightWeight * unit;
                k(d, "ъ", "ъ", T_CHAR, x, y, w);
                x += w;
            }

            w = wDotWeight * unit;
            String dot = (curLang == LANG_EXTRA) ? ">" : ".";
            k(d, dot, dot, T_CHAR, x, y, w);
            x += w;

            float enterWidth = screenW - x;
            if (enterWidth < 0) enterWidth = 0;
            k(d, "↵", "\n", T_ENTER, x, y, enterWidth);
        }

        private void k(LayoutData d, String l, String v, int t, float x, float y, float w) {
            int base = d.count * 4;
            d.keyCoords[base] = x;
            d.keyCoords[base + 1] = y;
            d.keyCoords[base + 2] = x + w;
            d.keyCoords[base + 3] = y + keyH;

            d.labels[d.count] = l;
            d.values[d.count] = v;
            d.types[d.count] = t;

            int r = (int) (y / keyH);
            if (r >= 0 && r < 4) d.grid[r][d.gridCount[r]++] = d.count;
            d.count++;
        }

        private void row(LayoutData d, float y, float w, String c) {
            int len = c.length();
            for (int i = 0; i < len; i++) {
                char ch = c.charAt(i);
                k(d, String.valueOf(ch), String.valueOf(ch), T_CHAR, i * w, y, w);
            }
        }

        private void rowW(LayoutData d, float y, float sx, float w, String c) {
            int len = c.length();
            for (int i = 0; i < len; i++) {
                char ch = c.charAt(i);
                k(d, String.valueOf(ch), String.valueOf(ch), T_CHAR, sx + i * w, y, w);
            }
        }
// todo: оптимизировать клавиатуру (сделано)
        private void buildAtlas(LayoutData d) {
            Set<Character> chars = new HashSet<>();
            for (int i = 0; i < d.count; i++) {
                if (d.types[i] == T_CHAR) {
                    char c = d.labels[i].charAt(0);
                    chars.add(c);
                    if (Character.isLetter(c)) {
                        chars.add(Character.toUpperCase(c));
                    }
                }
            }
            List<Character> list = new ArrayList<>(chars);
            Collections.sort(list, new Comparator<Character>() {
                public int compare(Character a, Character b) {
                    return a.compareTo(b);
                }
            });

            d.atlasCellW = (int) pText.measureText("W") + 4;
            d.atlasCellH = (int) (pText.getFontSpacing() + 2);
            d.atlasCols = list.size();
            int atlasWidth = d.atlasCols * d.atlasCellW;
            int atlasHeight = d.atlasCellH;

            if (d.atlas == null || d.atlas.getWidth() != atlasWidth || d.atlas.getHeight() != atlasHeight) {
                d.atlas = Bitmap.createBitmap(atlasWidth, atlasHeight, Bitmap.Config.ALPHA_8);
                Canvas c = new Canvas(d.atlas);
                Paint paint = new Paint(pText);
                paint.setColor(Color.WHITE);
                for (int i = 0; i < list.size(); i++) {
                    char ch = list.get(i);
                    float x = i * d.atlasCellW + d.atlasCellW / 2f;
                    float y = (d.atlasCellH - pText.descent() - pText.ascent()) / 2f;
                    c.drawText(String.valueOf(ch), x, y, paint);
                }
            }

            for (int i = 0; i < d.count; i++) {
                if (d.types[i] == T_CHAR) {
                    char c = d.labels[i].charAt(0);
                    char cu = Character.toUpperCase(c);
                    d.normalIdx[i] = list.indexOf(c);
                    d.shiftIdx[i] = list.indexOf(cu);
                } else {
                    d.normalIdx[i] = -1;
                    d.shiftIdx[i] = -1;
                }
            }
        }

        private static final int HITMAP_SCALE = 4;

        private void buildHitmap(LayoutData d) {
            d.hitmapW = screenW / HITMAP_SCALE + 1;
            d.hitmapH = (int) (keyH * 4) / HITMAP_SCALE + 1;
            int size = d.hitmapW * d.hitmapH;
            if (d.hitmap == null || d.hitmap.length != size) {
                d.hitmap = new short[size];
            }
            Arrays.fill(d.hitmap, (short) -1);

            for (int i = 0; i < d.count; i++) {
                int base = i * 4;
                int l = Math.max(0, (int) d.keyCoords[base]) / HITMAP_SCALE;
                int t = Math.max(0, (int) d.keyCoords[base + 1]) / HITMAP_SCALE;
                int r = Math.min(d.hitmapW - 1, (int) Math.ceil(d.keyCoords[base + 2]) / HITMAP_SCALE);
                int b = Math.min(d.hitmapH - 1, (int) Math.ceil(d.keyCoords[base + 3]) / HITMAP_SCALE);
                for (int y = t; y <= b; y++) {
                    int off = y * d.hitmapW;
                    for (int x = l; x <= r; x++) {
                        d.hitmap[off + x] = (short) i;
                    }
                }
            }
        }

        private Bitmap bgBitmap;
        private Canvas bgCanvas;
        private boolean needRedrawBg = true;
        private int prevWidth = -1, prevHeight = -1;

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            screenW = w;
            keyH = (h - previewHeight) / 4f;
            if (curLayout != null) {
                int lang = curLang;
                gen(lang == LANG_RU ? "ru" : lang == LANG_EN ? "en" : lang == LANG_SYM ? "sym" : "extra");
            }
            needRedrawBg = true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();

            if (needRedrawBg || bgBitmap == null || prevWidth != w || prevHeight != h) {
                if (bgBitmap == null || prevWidth != w || prevHeight != h) {
                    bgBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    bgCanvas = new Canvas(bgBitmap);
                    prevWidth = w;
                    prevHeight = h;
                }
                drawStatic(bgCanvas);
                needRedrawBg = false;
            }

            canvas.drawBitmap(bgBitmap, 0, 0, null);

            LayoutData d = curLayout;
            if (d == null) return;

            for (int i = 0; i < pointerKeys.length; i++) {
                int key = pointerKeys[i];
                if (key < 0 || key >= d.count) continue;

                int base = key * 4;
                float l = d.keyCoords[base] + GAP;
                float t = d.keyCoords[base + 1] + GAP + previewHeight;
                float r = d.keyCoords[base + 2] - GAP;
                float b = d.keyCoords[base + 3] - GAP + previewHeight;

                tempRect.set((int) l, (int) t, (int) r, (int) b);
                canvas.drawRect(tempRect, pH);

                if (d.types[key] == T_CHAR && !d.values[key].equals(" ")) {
                    float cx = (l + r) / 2;
                    float pT = t - previewHeight;
                    float pB = t;
                    tempRectF.set(cx - 50, pT, cx + 50, pB);
                    canvas.drawRect(tempRectF, pPre);

                    int idx = (shift > 0) ? d.shiftIdx[key] : d.normalIdx[key];
                    if (idx >= 0) {
                        int col = idx % d.atlasCols;
                        int row = idx / d.atlasCols;
                        tempSrcRect.set(col * d.atlasCellW, row * d.atlasCellH,
                                (col + 1) * d.atlasCellW, (row + 1) * d.atlasCellH);
                        float cyPreview = (pT + pB) * 0.5f;
                        float halfW = d.atlasCellW * 0.5f;
                        float halfH = d.atlasCellH * 0.5f;
                        tempDstRectF.set(cx - halfW, cyPreview - halfH, cx + halfW, cyPreview + halfH);
                        canvas.drawBitmap(d.atlas, tempSrcRect, tempDstRectF, pAtlas);
                    } else {
                        char ch = d.labels[key].charAt(0);
                        if (shift > 0) ch = Character.toUpperCase(ch);
                        tmpChar[0] = ch;
                        canvas.drawText(tmpChar, 0, 1, cx,
                                (pT + pB) / 2f - previewBaselineOffset, pPreT);
                    }
                }
            }
        }

        private void drawStatic(Canvas cv) {
            cv.drawColor(BG);

            LayoutData d = curLayout;
            if (d == null) return;

            for (int i = 0; i < d.count; i++) {
                int base = i * 4;
                float l = d.keyCoords[base] + GAP;
                float t = d.keyCoords[base + 1] + GAP + previewHeight;
                float r = d.keyCoords[base + 2] - GAP;
                float b = d.keyCoords[base + 3] - GAP + previewHeight;

                Paint p;
                if (d.types[i] == T_CHAR || d.types[i] == T_SPACE) {
                    p = p1;
                } else {
                    p = p2;
                }

                if (d.types[i] == T_SHIFT && shift == 2) {
                    cv.drawRect(l, t, r, b, pH);
                } else {
                    cv.drawRect(l, t, r, b, p);
                }

                float cx = (l + r) / 2;
                float cy = (t + b) / 2;

                if (d.types[i] == T_CHAR) {
                    int idx = (shift > 0) ? d.shiftIdx[i] : d.normalIdx[i];
                    if (idx >= 0) {
                        int col = idx % d.atlasCols;
                        int row = idx / d.atlasCols;
                        tempSrcRect.set(col * d.atlasCellW, row * d.atlasCellH,
                                (col + 1) * d.atlasCellW, (row + 1) * d.atlasCellH);
                        float halfW = d.atlasCellW * 0.5f;
                        float halfH = d.atlasCellH * 0.5f;
                        tempDstRectF.set(cx - halfW, cy - halfH, cx + halfW, cy + halfH);
                        cv.drawBitmap(d.atlas, tempSrcRect, tempDstRectF, pAtlas);
                    } else {
                        char ch = d.labels[i].charAt(0);
                        if (shift > 0) ch = Character.toUpperCase(ch);
                        tmpChar[0] = ch;
                        cv.drawText(tmpChar, 0, 1, cx, cy - textBaselineOffset, pText);
                    }
                } else if (d.types[i] == T_SHIFT) {
                    String shiftLabel;
                    if (shift == 1) shiftLabel = SHIFT_SINGLE;
                    else if (shift == 2) shiftLabel = SHIFT_LOCK;
                    else shiftLabel = SHIFT_NORMAL;
                    cv.drawText(shiftLabel, cx, cy - textBaselineOffset, pText);
                } else {
                    cv.drawText(d.labels[i], cx, cy - textBaselineOffset, pText);
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            int action = e.getActionMasked();
            int index = e.getActionIndex();
            int id = e.getPointerId(index);

            LayoutData d = curLayout;
            if (d == null) return true;

            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                int k = getK(d, e.getX(index), e.getY(index) - previewHeight);
                if (k != -1) {
                    performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
                    int slot = findEmptyPointerSlot();
                    if (slot >= 0) {
                        pointerIds[slot] = id;
                        pointerKeys[slot] = k;
                    }
                    if (d.types[k] == T_DEL) {
                        if (ime.deleteSelected()) {
                            isDel = false;
                            h.removeCallbacks(delRunnable);
                        } else {
                            ime.deleteChar();
                            isDel = true;
                            h.postDelayed(delRunnable, 350);
                        }
                    }
                    getDirtyRect(d, k, tempRect);
                    invalidate(tempRect);
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                boolean changed = false;
                Rect dirty = new Rect();
                Rect tmp = new Rect();
                for (int m = 0; m < e.getPointerCount(); m++) {
                    int pid = e.getPointerId(m);
                    int nk = getK(d, e.getX(m), e.getY(m) - previewHeight);
                    int slot = findPointerSlot(pid);
                    if (slot >= 0) {
                        int oldK = pointerKeys[slot];
                        if (oldK != nk) {
                            if (oldK != -1) {
                                getDirtyRect(d, oldK, tmp);
                                dirty.union(tmp);
                            }
                            if (nk != -1) {
                                pointerKeys[slot] = nk;
                                getDirtyRect(d, nk, tmp);
                                dirty.union(tmp);
                            } else {
                                pointerKeys[slot] = -1;
                                pointerIds[slot] = -1;
                            }
                            changed = true;
                        }
                    } else if (nk != -1) {
                        int newSlot = findEmptyPointerSlot();
                        if (newSlot >= 0) {
                            pointerIds[newSlot] = pid;
                            pointerKeys[newSlot] = nk;
                            getDirtyRect(d, nk, tmp);
                            dirty.union(tmp);
                            changed = true;
                        }
                    }
                }
                if (changed) invalidate(dirty);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                int slot = findPointerSlot(id);
                if (slot >= 0) {
                    int k = pointerKeys[slot];
                    if (k != -1) {
                        if (d.types[k] != T_DEL) {
                            handle(k);
                        }
                        pointerKeys[slot] = -1;
                        pointerIds[slot] = -1;
                        if (d.types[k] == T_DEL) {
                            isDel = false;
                            h.removeCallbacks(delRunnable);
                        }
                        getDirtyRect(d, k, tempRect);
                        invalidate(tempRect);
                    }
                }
            } else if (action == MotionEvent.ACTION_CANCEL) {
                Rect dirty = new Rect();
                Rect tmp = new Rect();
                for (int i = 0; i < pointerKeys.length; i++) {
                    int k = pointerKeys[i];
                    if (k != -1) {
                        getDirtyRect(d, k, tmp);
                        dirty.union(tmp);
                        pointerKeys[i] = -1;
                        pointerIds[i] = -1;
                    }
                }
                isDel = false;
                h.removeCallbacks(delRunnable);
                if (!dirty.isEmpty()) invalidate(dirty);
            }
            return true;
        }

        private int getK(LayoutData d, float x, float y) {
            int ix = (int) x / HITMAP_SCALE;
            int iy = (int) y / HITMAP_SCALE;
            if (iy < 0 || iy >= d.hitmapH || ix < 0 || ix >= d.hitmapW) return -1;
            return d.hitmap[iy * d.hitmapW + ix] & 0xffff;
        }

        private void getDirtyRect(LayoutData d, int idx, Rect outRect) {
            int base = idx * 4;
            float left = d.keyCoords[base];
            float top = d.keyCoords[base + 1];
            float right = d.keyCoords[base + 2];
            float bottom = d.keyCoords[base + 3] + previewHeight;

            outRect.left = (int) Math.floor(left);
            outRect.top = (int) Math.floor(top);
            outRect.right = (int) Math.ceil(right);
            outRect.bottom = (int) Math.ceil(bottom);
        }

        private int findEmptyPointerSlot() {
            for (int i = 0; i < pointerIds.length; i++) {
                if (pointerIds[i] == -1) return i;
            }
            return -1;
        }

        private int findPointerSlot(int id) {
            for (int i = 0; i < pointerIds.length; i++) {
                if (pointerIds[i] == id) return i;
            }
            return -1;
        }

/*
private void handle(int i) {
    int t = types[i];
    String v = values[i];

    if (t == T_CHAR) {
        if (shift > 0) {
            v = v.toUpperCase(Locale.ROOT);
            if (shift == 1) {
                shift = 0;
                needRedrawBg = true;
            }
        }
        ime.commitText(v);
    } else if (t == T_SHIFT) {
        shift = (shift + 1) % 3;
        needRedrawBg = true;
        invalidate();
    } else if (t == T_SPACE) {
        ime.commitText(" ");
    } else if (t == T_ENTER) {
        ime.handleEnter();
    } else if (t == T_LANG) {
        lastL = lastL.equals("en") ? "ru" : "en";
        ime.saveLanguage(lastL);
        gen(lastL);
    } else if (t == T_SYM) {
        if (curL.equals("ru") || curL.equals("en")) {
            gen("sym");
        } else if (curL.equals("sym")) {
            gen(lastL);
        } else if (curL.equals("extra")) {
            gen("sym");
        }
    } else if (t == T_SYM_EX) {
        gen("extra");
    } else if (t == T_ABC) {
        gen(lastL);
    }
}
*/
        private void handle(int i) {
            LayoutData d = curLayout;
            int t = d.types[i];
            String v = d.values[i];

            if (t == T_CHAR) {
                if (shift > 0) {
                    v = v.toUpperCase(Locale.ROOT);
                    if (shift == 1) {
                        shift = 0;
                        needRedrawBg = true;
                    }
                }
                ime.commitText(v);
            } else if (t == T_SHIFT) {
                shift = (shift + 1) % 3;
                needRedrawBg = true;
                invalidate();
            } else if (t == T_SPACE) {
                ime.commitText(" ");
            } else if (t == T_ENTER) {
                ime.handleEnter();
            } else if (t == T_LANG) {
                lastLang = (lastLang == LANG_RU) ? LANG_EN : LANG_RU;
                ime.saveLanguage(lastLang == LANG_RU ? "ru" : "en");
                gen(lastLang == LANG_RU ? "ru" : "en");
            } else if (t == T_SYM) {
                if (curLang == LANG_RU || curLang == LANG_EN) {
                    gen("sym");
                } else if (curLang == LANG_SYM) {
                    gen(lastLang == LANG_RU ? "ru" : "en");
                } else if (curLang == LANG_EXTRA) {
                    gen("sym");
                }
            } else if (t == T_SYM_EX) {
                gen("extra");
            } else if (t == T_ABC) {
                gen(lastLang == LANG_RU ? "ru" : "en");
            }
        }
    }
}