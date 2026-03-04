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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextPaint;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.DisplayMetrics;
import java.util.Locale;


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
      
      // переменные
        private static final int BG = Color.BLACK;
        private static final int KEY = 0xFF252525;
        private static final int SPECIAL = 0xFF1A1A1A;
        private static final int ACCENT = 0xFF404040;
        private static final int GAP = 4;

        private static final int T_CHAR = 0, T_SHIFT = 1, T_LANG = 2, T_ENTER = 3, T_SPACE = 4,
                T_DEL = 5, T_SYM = 8, T_SYM_EX = 9, T_ABC = 10;

        private static final String SHIFT_NORMAL = "⇧";
        private static final String SHIFT_SINGLE = "⬆";
        private static final String SHIFT_LOCK = "⟰";

        private final Paint p1 = new Paint();
        private final Paint p2 = new Paint();
        private final Paint pH = new Paint();
        
        private final TextPaint pText = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pPre = new Paint();
        private final TextPaint pPreT = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        private LenterIME ime;
        private int screenW;
        private float keyH;
        private float textBaselineOffset, previewBaselineOffset;
        private String curL = "ru", lastL = "ru";
        private int shift = 0;

        private final float[] keyCoords;
        private final String[] labels;
        private final String[] values;
        private final int[] types;
        private int count = 0;

        private final int[][] grid = new int[4][32];
        private final int[] gridCount = new int[4];

        private Bitmap bgBitmap;
        private Canvas bgCanvas;
        private boolean needRedrawBg = true;
        private int prevWidth = -1, prevHeight = -1;

        private final SparseArray<Integer> pointers = new SparseArray<>(4);
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

        private float density;
        private int previewHeight;
        private final char[] tmpChar = new char[1];

        public KeyboardView(Context ctx) {
            super(ctx);
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

            textBaselineOffset = (pText.descent() + pText.ascent()) / 2f;
            previewBaselineOffset = (pPreT.descent() + pPreT.ascent()) / 2f;

            int maxKeys = 60;
            keyCoords = new float[maxKeys * 4];
            labels = new String[maxKeys];
            values = new String[maxKeys];
            types = new int[maxKeys];
        }

        public void setImeService(LenterIME s) {
            this.ime = s;
        }

        public void setParams(int h, String l) {
            this.keyH = (h - previewHeight) / 4f;
            this.lastL = (l.equals("sym") || l.equals("extra")) ? "ru" : l;
            if (screenW > 0) {
                gen(l);
            }
        }

        public void setLanguage(String lang) {
            lastL = lang;
            gen(lang);
        }

// раскладка
        private void gen(String type) {
            curL = type;
            count = 0;
            for (int i = 0; i < 4; i++) gridCount[i] = 0;

            float y = 0, w;
            if (type.equals("ru")) {
                w = screenW / 11f;
                row(y, w, "йцукенгшщзх");
                y += keyH;
                row(y, w, "фывапролджэ");
                y += keyH;
                k(SHIFT_NORMAL, "SH", T_SHIFT, 0, y, w * 1.5f);
                rowW(y, w * 1.5f, (screenW - w * 3f) / 9f, "ячсмитьбю");
                k("⌫", "DEL", T_DEL, screenW - w * 1.5f, y, w * 1.5f);
                y += keyH;
                drawBottom(y);
            } else if (type.equals("en")) {
                w = screenW / 10f;
                row(y, w, "qwertyuiop");
                y += keyH;
                rowW(y, w * 0.5f, w, "asdfghjkl");
                y += keyH;
                k(SHIFT_NORMAL, "SH", T_SHIFT, 0, y, w * 1.5f);
                rowW(y, w * 1.5f, w, "zxcvbnm");
                k("⌫", "DEL", T_DEL, screenW - w * 1.5f, y, w * 1.5f);
                y += keyH;
                drawBottom(y);
            } else if (type.equals("sym")) {
                w = screenW / 10f;
                row(y, w, "1234567890");
                y += keyH;
                row(y, w, "@#№_&-+()/");
                y += keyH;
                k("=\\<", "EX", T_SYM_EX, 0, y, w * 1.5f);
                rowW(y, w * 1.5f, w, "*\"':;!?");
                k("⌫", "DEL", T_DEL, screenW - w * 1.5f, y, w * 1.5f);
                y += keyH;
                drawBottom(y);
            } else if (type.equals("extra")) {
                w = screenW / 11f;
                row(y, w, "~`|•√π÷×¶∆●");
                y += keyH;
                row(y, w, "€$£¥₸₽^°={}");
                y += keyH;
                k("?12", "SYM", T_SYM, 0, y, w * 1.5f);
                rowW(y, w * 1.5f, w, "\\©®™%[]■");
                k("⌫", "DEL", T_DEL, screenW - w * 1.5f, y, w * 1.5f);
                y += keyH;
                drawBottom(y);
            }
            needRedrawBg = true;
            invalidate();
        }

// todo: сделать чтобы клава не вылезала вне закона (исправлено)
private void drawBottom(float y) {
    float wSymWeight = 1.5f;
    float wCommaWeight = 1.0f;
    float wLangWeight = 1.3f;
    float wExtraWeight = curL.equals("ru") ? 1.0f : 0f;
    float wSpaceWeight = 3.5f;
    float wExtraRightWeight = curL.equals("ru") ? 1.0f : 0f;
    float wDotWeight = 1.0f;
    float wEnterWeight = 1.7f;

    float totalWeight = wSymWeight + wCommaWeight + wLangWeight + 
                        wExtraWeight + wSpaceWeight + wExtraRightWeight + 
                        wDotWeight + wEnterWeight;

    float unit = screenW / totalWeight;
    float x = 0;

    float w = wSymWeight * unit;
    String label = (curL.equals("ru") || curL.equals("en")) ? "?12" : "ABC";
    int type = (curL.equals("ru") || curL.equals("en")) ? T_SYM : T_ABC;
    k(label, "SYM_TOGGLE", type, x, y, w);
    x += w;

    w = wCommaWeight * unit;
    String comma = curL.equals("extra") ? "<" : ",";
    k(comma, comma, T_CHAR, x, y, w);
    x += w;

    w = wLangWeight * unit;
    k(lastL.equals("ru") ? "RU" : "EN", "L", T_LANG, x, y, w);
    x += w;

    if (curL.equals("ru")) {
        w = wExtraWeight * unit;
        k("ё", "ё", T_CHAR, x, y, w);
        x += w;
    }

    w = wSpaceWeight * unit;
    k(" ", " ", T_SPACE, x, y, w);
    x += w;

    if (curL.equals("ru")) {
        w = wExtraRightWeight * unit;
        k("ъ", "ъ", T_CHAR, x, y, w);
        x += w;
    }

    w = wDotWeight * unit;
    String dot = curL.equals("extra") ? ">" : ".";
    k(dot, dot, T_CHAR, x, y, w);
    x += w;

    float enterWidth = screenW - x;
    if (enterWidth < 0) enterWidth = 0;
    k("↵", "\n", T_ENTER, x, y, enterWidth);
}

        private void k(String l, String v, int t, float x, float y, float w) {
            int base = count * 4;
            keyCoords[base] = x;
            keyCoords[base + 1] = y;
            keyCoords[base + 2] = x + w;
            keyCoords[base + 3] = y + keyH;

            labels[count] = l;
            values[count] = v;
            types[count] = t;

            int r = (int) (y / keyH);
            if (r >= 0 && r < 4) grid[r][gridCount[r]++] = count;
            count++;
        }

        private void row(float y, float w, String c) {
            int len = c.length();
            for (int i = 0; i < len; i++) {
                char ch = c.charAt(i);
                k(String.valueOf(ch), String.valueOf(ch), T_CHAR, i * w, y, w);
            }
        }

        private void rowW(float y, float sx, float w, String c) {
            int len = c.length();
            for (int i = 0; i < len; i++) {
                char ch = c.charAt(i);
                k(String.valueOf(ch), String.valueOf(ch), T_CHAR, sx + i * w, y, w);
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            screenW = w;
            keyH = (h - previewHeight) / 4f;
            if (curL != null) {
                gen(curL);
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

            int pointersSize = pointers.size();
            for (int p = 0; p < pointersSize; p++) {
                int idx = pointers.valueAt(p);
                if (idx >= count) continue;

                int base = idx * 4;
                float l = keyCoords[base] + GAP;
                float t = keyCoords[base + 1] + GAP + previewHeight;
                float r = keyCoords[base + 2] - GAP;
                float b = keyCoords[base + 3] - GAP + previewHeight;

                canvas.drawRect(l, t, r, b, pH);

                if (types[idx] == T_CHAR && !values[idx].equals(" ")) {
                    float cx = (l + r) / 2;
                    float pT = t - previewHeight;
                    float pB = t;
                    canvas.drawRect(cx - 50, pT, cx + 50, pB, pPre);

                    char ch = labels[idx].charAt(0);
                    if (shift > 0) {
                        ch = Character.toUpperCase(ch);
                    }
                    tmpChar[0] = ch;
                    canvas.drawText(tmpChar, 0, 1, cx,
                            (pT + pB) / 2f - previewBaselineOffset, pPreT);
                }
            }
        }

        private void drawStatic(Canvas cv) {
            cv.drawColor(BG);

            for (int i = 0; i < count; i++) {
                int base = i * 4;
                float l = keyCoords[base] + GAP;
                float t = keyCoords[base + 1] + GAP + previewHeight;
                float r = keyCoords[base + 2] - GAP;
                float b = keyCoords[base + 3] - GAP + previewHeight;

                Paint p;
                if (types[i] == T_CHAR || types[i] == T_SPACE) {
                    p = p1;
                } else {
                    p = p2;
                }

                if (types[i] == T_SHIFT && shift == 2) {
                    cv.drawRect(l, t, r, b, pH);
                } else {
                    cv.drawRect(l, t, r, b, p);
                }

                float cx = (l + r) / 2;
                float cy = (t + b) / 2;

                if (types[i] == T_CHAR) {
                    char ch = labels[i].charAt(0);
                    if (shift > 0) {
                        ch = Character.toUpperCase(ch);
                    }
                    tmpChar[0] = ch;
                    cv.drawText(tmpChar, 0, 1, cx, cy - textBaselineOffset, pText);
                } else if (types[i] == T_SHIFT) {
                    String shiftLabel;
                    if (shift == 1) shiftLabel = SHIFT_SINGLE;
                    else if (shift == 2) shiftLabel = SHIFT_LOCK;
                    else shiftLabel = SHIFT_NORMAL;
                    cv.drawText(shiftLabel, cx, cy - textBaselineOffset, pText);
                } else {
                    cv.drawText(labels[i], cx, cy - textBaselineOffset, pText);
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            int action = e.getActionMasked();
            int index = e.getActionIndex();
            int id = e.getPointerId(index);

            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                int k = getK(e.getX(index), e.getY(index) - previewHeight);
                if (k != -1) {
                    performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
                    pointers.put(id, k);
                    if (types[k] == T_DEL) {
                        if (ime.deleteSelected()) {
                            isDel = false;
                            h.removeCallbacks(delRunnable);
                        } else {
                            ime.deleteChar();
                            isDel = true;
                            h.postDelayed(delRunnable, 350);
                        }
                    }
                    Rect dirty = new Rect();
                    getDirtyRect(k, dirty);
                    invalidate(dirty);
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                boolean changed = false;
                Rect dirty = new Rect();
                Rect tmp = new Rect();
                for (int m = 0; m < e.getPointerCount(); m++) {
                    int pid = e.getPointerId(m);
                    int nk = getK(e.getX(m), e.getY(m) - previewHeight);
                    int oldK = pointers.get(pid, -1);
                    if (oldK != nk) {
                        if (oldK != -1) {
                            getDirtyRect(oldK, tmp);
                            dirty.union(tmp);
                        }
                        if (nk != -1) {
                            pointers.put(pid, nk);
                            getDirtyRect(nk, tmp);
                            dirty.union(tmp);
                        } else {
                            pointers.remove(pid);
                        }
                        changed = true;
                    }
                }
                if (changed) invalidate(dirty);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                int k = pointers.get(id, -1);
                if (k != -1) {
                    if (types[k] != T_DEL) {
                        handle(k);
                    }
                    pointers.remove(id);
                    if (types[k] == T_DEL) {
                        isDel = false;
                        h.removeCallbacks(delRunnable);
                    }
                    Rect dirty = new Rect();
                    getDirtyRect(k, dirty);
                    invalidate(dirty);
                }
            } else if (action == MotionEvent.ACTION_CANCEL) {
                Rect dirty = new Rect();
                Rect tmp = new Rect();
                for (int i = 0; i < pointers.size(); i++) {
                    int k = pointers.valueAt(i);
                    getDirtyRect(k, tmp);
                    dirty.union(tmp);
                }
                pointers.clear();
                isDel = false;
                h.removeCallbacks(delRunnable);
                if (!dirty.isEmpty()) invalidate(dirty);
            }
            return true;
        }

        private int getK(float x, float y) {
            if (y < 0 || y >= keyH * 4) return -1;
            int r = (int) (y / keyH);
            if (r >= 0 && r < 4) {
                for (int j = 0; j < gridCount[r]; j++) {
                    int i = grid[r][j];
                    int base = i * 4;
                    if (x >= keyCoords[base] && x <= keyCoords[base + 2]) {
                        return i;
                    }
                }
            }
            return -1;
        }

        private void getDirtyRect(int idx, Rect outRect) {
            int base = idx * 4;
            float left = keyCoords[base];
            float top = keyCoords[base + 1];
            float right = keyCoords[base + 2];
            float bottom = keyCoords[base + 3] + previewHeight;

            outRect.left = (int) Math.floor(left);
            outRect.top = (int) Math.floor(top);
            outRect.right = (int) Math.ceil(right);
            outRect.bottom = (int) Math.ceil(bottom);
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
    }
}