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

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;

import java.util.*;

// todo: разработка новой функции: разбитый экран

public class LenterIME extends InputMethodService {
    private KeyboardView keyboardView;
    private int currentEnterAction = EditorInfo.IME_ACTION_UNSPECIFIED;
    private EditorInfo currentEditorInfo;

    private static int sOffsetLeft = 0;
    private static int sOffsetRight = 0;
    private static int sOffsetTop = 0;
    private static int sOffsetBottom = 0;
    private static boolean sOffsetsChanged = false;

    public static void updateOffsets(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("LenterPrefs", MODE_PRIVATE);
        sOffsetLeft = prefs.getInt("broken_left", 0);
        sOffsetRight = prefs.getInt("broken_right", 0);
        sOffsetTop = prefs.getInt("broken_top", 0);
        sOffsetBottom = prefs.getInt("broken_bottom", 0);
        sOffsetsChanged = true;
    }

    @Override
    public View onCreateInputView() {
        SharedPreferences prefs = getSharedPreferences("LenterPrefs", MODE_PRIVATE);
        String lang = prefs.getString("active_lang", "ru");
        float density = getResources().getDisplayMetrics().density;
        int height = (int) (250 * density + 0.5f);

        if (!sOffsetsChanged) {
            sOffsetLeft = prefs.getInt("broken_left", 0);
            sOffsetRight = prefs.getInt("broken_right", 0);
            sOffsetTop = prefs.getInt("broken_top", 0);
            sOffsetBottom = prefs.getInt("broken_bottom", 0);
            sOffsetsChanged = true;
        }

        keyboardView = new KeyboardView(this);
        keyboardView.setImeService(this);
        keyboardView.setParams(height, lang);
        keyboardView.setOffsets(sOffsetLeft, sOffsetRight, sOffsetTop, sOffsetBottom);

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
            if (isNumericInput()) {
                keyboardView.setNumericMode(isDecimalInput());
            } else {
                keyboardView.setLanguage(lang);
            }
            keyboardView.setEnterIcon(getEnterIcon());
            keyboardView.setOffsets(sOffsetLeft, sOffsetRight, sOffsetTop, sOffsetBottom);
        }
    }

    public boolean isNumericInput() {
        if (currentEditorInfo == null) return false;
        int inputClass = currentEditorInfo.inputType & EditorInfo.TYPE_MASK_CLASS;
        return inputClass == EditorInfo.TYPE_CLASS_NUMBER ||
               inputClass == EditorInfo.TYPE_CLASS_PHONE;
    }

    public boolean isDecimalInput() {
        if (currentEditorInfo == null) return false;
        int inputClass = currentEditorInfo.inputType & EditorInfo.TYPE_MASK_CLASS;
        if (inputClass == EditorInfo.TYPE_CLASS_PHONE) return true;
        if (inputClass == EditorInfo.TYPE_CLASS_NUMBER) {
            return (currentEditorInfo.inputType & EditorInfo.TYPE_NUMBER_FLAG_DECIMAL) != 0;
        }
        return false;
    }

    public String getEnterIcon() {
        if (currentEditorInfo == null) return "↵";
        int imeOptions = currentEditorInfo.imeOptions;
        if ((imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
            return "↵";
        }
        int action = imeOptions & EditorInfo.IME_MASK_ACTION;
        switch (action) {
            case EditorInfo.IME_ACTION_SEARCH: return "⌕";
            case EditorInfo.IME_ACTION_SEND:   return "➤";
            case EditorInfo.IME_ACTION_NEXT:    return "→";
            case EditorInfo.IME_ACTION_DONE:    return "✓";
            case EditorInfo.IME_ACTION_GO:      return "➤";
            case EditorInfo.IME_ACTION_PREVIOUS:return "←";
            default: return "↵";
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

    public static class KeyboardView extends View {

        private final Rect tempRect = new Rect();
        private final RectF tempRectF = new RectF();
        private final Rect tempSrcRect = new Rect();
        private final RectF tempDstRectF = new RectF();

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
        private static final int LANG_NUMERIC = 4;
        private static final int LANG_DECIMAL = 5;
        private static final int TOTAL_LAYOUTS = 6;

        private static final char[] SHIFT_NORMAL = "⇧".toCharArray();
        private static final char[] SHIFT_SINGLE = "⬆".toCharArray();
        private static final char[] SHIFT_LOCK = "⟰".toCharArray();

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
        private boolean numericMode = false;
        private boolean isPreviewMode = false;

        private int offsetLeft = 0;
        private int offsetRight = 0;
        private int offsetTop = 0;
        private int offsetBottom = 0;
        private float scale = 1f;
        private float translateX = 0;
        private float translateY = 0;

        private static class LayoutData {
            float[] keyCoords;
            char[][] labels;
            int[] labelsLen;
            char[][] values;
            int[] valuesLen;
            byte[] types;
            int count;
            byte[][] grid = new byte[4][32];
            int[] gridCount = new int[4];
            byte[] hitmap;
            int hitmapW, hitmapH;
            byte[] normalIdx;
            byte[] shiftIdx;
            boolean isBuilt = false;
        }

        private LayoutData[] layouts = new LayoutData[TOTAL_LAYOUTS];
        private LayoutData curLayout;

        private static Bitmap sGlobalAtlas;
        private static int[] sCharToAtlasIndex;
        private static int sAtlasCols;
        private static int sAtlasCellW;
        private static int sAtlasCellH;
        private static boolean sGlobalAtlasBuilt = false;

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
        private char[] currentEnterIcon = "↵".toCharArray();

        private static class RectPool {
            private Rect rect1 = new Rect();
            private Rect rect2 = new Rect();
            private Rect rect3 = new Rect();
            private int index = 0;
            Rect obtain() {
                index = (index + 1) % 3;
                switch (index) {
                    case 0: return rect1;
                    case 1: return rect2;
                    default: return rect3;
                }
            }
        }
        private RectPool rectPool = new RectPool();

        private boolean lowEndMode;

        private boolean isLowEndDevice() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                return true;
            }
            ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            int cores = Runtime.getRuntime().availableProcessors();
            long totalMem = mi.totalMem / (1024 * 1024);
            return cores <= 2 || totalMem < 512;
        }

        public KeyboardView(Context ctx) {
            super(ctx);
            lowEndMode = isLowEndDevice();

            if (Build.VERSION.SDK_INT >= 11) {
                if (!lowEndMode) {
                    setLayerType(LAYER_TYPE_HARDWARE, null);
                } else {
                    setLayerType(LAYER_TYPE_SOFTWARE, null);
                }
            }
            DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
            density = dm.density;
            screenW = dm.widthPixels;

            float fontSizeNormal = 22 * density;
            float fontSizePreview = 32 * density;
            previewHeight = lowEndMode ? 0 : (int) (50 * density);

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

            for (int i = 0; i < TOTAL_LAYOUTS; i++) {
                layouts[i] = new LayoutData();
                layouts[i].keyCoords = new float[60 * 4];
                layouts[i].labels = new char[60][];
                layouts[i].labelsLen = new int[60];
                layouts[i].values = new char[60][];
                layouts[i].valuesLen = new int[60];
                layouts[i].types = new byte[60];
                layouts[i].normalIdx = new byte[60];
                layouts[i].shiftIdx = new byte[60];
                Arrays.fill(layouts[i].normalIdx, (byte) -1);
                Arrays.fill(layouts[i].shiftIdx, (byte) -1);
            }

            ensureGlobalAtlas();
        }

        public void setIsPreviewMode(boolean preview) {
            this.isPreviewMode = preview;
        }

        public void setImeService(LenterIME s) {
            this.ime = s;
        }

        public void setParams(int h, String l) {
            this.keyH = (h - previewHeight) / 4f;
            this.lastLang = (l.equals("ru") ? LANG_RU : LANG_EN);
            if (screenW > 0) {
                ensureLayoutBuilt(lastLang);
                curLang = lastLang;
                curLayout = layouts[lastLang];
                needRedrawBg = true;
                invalidate();
            }
        }

        public void setLanguage(String lang) {
            numericMode = false;
            lastLang = lang.equals("ru") ? LANG_RU : LANG_EN;
            curLang = lastLang;
            ensureLayoutBuilt(curLang);
            curLayout = layouts[curLang];
            setEnterIcon(currentEnterIcon);
            needRedrawBg = true;
            invalidate();
        }

        public void setNumericMode(boolean decimal) {
            numericMode = true;
            int targetLang = LANG_NUMERIC;
            ensureLayoutBuilt(targetLang);
            curLang = targetLang;
            curLayout = layouts[targetLang];
            setEnterIcon(currentEnterIcon);
            needRedrawBg = true;
            invalidate();
        }

        public void setEnterIcon(char[] icon) {
            if (!Arrays.equals(currentEnterIcon, icon)) {
                currentEnterIcon = icon;
                if (curLayout != null) {
                    for (int i = 0; i < curLayout.count; i++) {
                        if (curLayout.types[i] == T_ENTER) {
                            curLayout.labels[i] = icon;
                            curLayout.labelsLen[i] = icon.length;
                            break;
                        }
                    }
                }
                needRedrawBg = true;
                invalidate();
            }
        }

        public void setEnterIcon(String icon) {
            setEnterIcon(icon.toCharArray());
        }

        public void setOffsets(int left, int right, int top, int bottom) {
            offsetLeft = left;
            offsetRight = right;
            offsetTop = top;
            offsetBottom = bottom;

            int viewW = getWidth();
            int viewH = getHeight();
            float contentW = screenW;
            float contentH = previewHeight + keyH * 4;

            float availableW = viewW - offsetLeft - offsetRight;
            float availableH = viewH - offsetTop - offsetBottom;

            if (availableW > 0 && availableH > 0) {
                float scaleX = availableW / contentW;
                float scaleY = availableH / contentH;
                scale = Math.min(scaleX, scaleY);
                translateX = offsetLeft + (availableW - contentW * scale) / 2f;
                translateY = offsetTop + (availableH - contentH * scale) / 2f;
            } else {
                scale = 1f;
                translateX = 0;
                translateY = 0;
            }

            needRedrawBg = true;
            invalidate();
        }

        private void ensureLayoutBuilt(int lang) {
            if (!layouts[lang].isBuilt) {
                buildLayout(lang);
            }
        }

        private void buildLayout(int langIdx) {
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
                k(d, SHIFT_NORMAL, "SH".toCharArray(), T_SHIFT, 0, y, w * 1.5f);
                rowW(d, y, w * 1.5f, (screenW - w * 3f) / 9f, "ячсмитьбю");
                k(d, "⌫".toCharArray(), "DEL".toCharArray(), T_DEL, screenW - w * 1.5f, y, w * 1.5f);
                y += keyH;
                drawBottom(d, y);
            } else if (langIdx == LANG_EN) {
                w = screenW / 10f;
                row(d, y, w, "qwertyuiop");
                y += keyH;
                rowW(d, y, w * 0.5f, w, "asdfghjkl");
                y += keyH;
                k(d, SHIFT_NORMAL, "SH".toCharArray(), T_SHIFT, 0, y, w * 1.5f);
                rowW(d, y, w * 1.5f, w, "zxcvbnm");
                k(d, "⌫".toCharArray(), "DEL".toCharArray(), T_DEL, screenW - w * 1.5f, y, w * 1.5f);
                y += keyH;
                drawBottom(d, y);
            } else if (langIdx == LANG_SYM) {
                w = screenW / 10f;
                row(d, y, w, "1234567890");
                y += keyH;
                row(d, y, w, "@#№_&-+()/");
                y += keyH;
                k(d, "=\\<".toCharArray(), "EX".toCharArray(), T_SYM_EX, 0, y, w * 1.5f);
                rowW(d, y, w * 1.5f, w, "*\"':;!?");
                k(d, "⌫".toCharArray(), "DEL".toCharArray(), T_DEL, screenW - w * 1.5f, y, w * 1.5f);
                y += keyH;
                drawBottom(d, y);
            } else if (langIdx == LANG_EXTRA) {
                w = screenW / 11f;
                row(d, y, w, "~`|•√π÷×¶∆●");
                y += keyH;
                row(d, y, w, "€$£¥₸₽^°={}");
                y += keyH;
                k(d, "?12".toCharArray(), "SYM".toCharArray(), T_SYM, 0, y, w * 1.5f);
                rowW(d, y, w * 1.5f, w, "\\©®™%[]■");
                k(d, "⌫".toCharArray(), "DEL".toCharArray(), T_DEL, screenW - w * 1.5f, y, w * 1.5f);
                y += keyH;
                drawBottom(d, y);
            } else if (langIdx == LANG_NUMERIC) {
                float keyW = screenW / 4f;
                y = 0;
                rowNumeric(d, y, keyW, "123(");
                y += keyH;
                rowNumeric(d, y, keyW, "456)");
                y += keyH;
                float x = 0;
                k(d, "7".toCharArray(), "7".toCharArray(), T_CHAR, x, y, keyW);
                x += keyW;
                k(d, "8".toCharArray(), "8".toCharArray(), T_CHAR, x, y, keyW);
                x += keyW;
                k(d, "9".toCharArray(), "9".toCharArray(), T_CHAR, x, y, keyW);
                x += keyW;
                k(d, "ABC".toCharArray(), "ABC".toCharArray(), T_ABC, x, y, keyW);
                y += keyH;
                float keyW5 = screenW / 5f;
                x = 0;
                k(d, ".".toCharArray(), ".".toCharArray(), T_CHAR, x, y, keyW5);
                x += keyW5;
                k(d, "0".toCharArray(), "0".toCharArray(), T_CHAR, x, y, keyW5);
                x += keyW5;
                k(d, "+".toCharArray(), "+".toCharArray(), T_CHAR, x, y, keyW5);
                x += keyW5;
                k(d, "⌫".toCharArray(), "DEL".toCharArray(), T_DEL, x, y, keyW5);
                x += keyW5;
                k(d, currentEnterIcon, "\n".toCharArray(), T_ENTER, x, y, keyW5);
            } else if (langIdx == LANG_DECIMAL) {
            }

            buildAtlas(d);
            buildHitmap(d);
            d.isBuilt = true;
        }

        private void drawBottom(LayoutData d, float y) {
            float wSymWeight = 1.5f;
            float wCommaWeight = 1.2f;
            float wLangWeight = 1.3f;
            float wExtraWeight = (curLang == LANG_RU) ? 1.0f : 0f;
            float wSpaceWeight = 3.5f;
            float wExtraRightWeight = (curLang == LANG_RU) ? 1.0f : 0f;
            float wDotWeight = 1.2f;
            float wEnterWeight = 1.7f;

            boolean isSymbolLayout = (curLang == LANG_SYM || curLang == LANG_EXTRA);
            float totalWeight;
            if (isSymbolLayout) {
                totalWeight = wSymWeight + wCommaWeight + wExtraWeight + wSpaceWeight +
                             wExtraRightWeight + wDotWeight + wEnterWeight;
            } else {
                totalWeight = wSymWeight + wCommaWeight + wLangWeight + wExtraWeight +
                             wSpaceWeight + wExtraRightWeight + wDotWeight + wEnterWeight;
            }

            float unit = screenW / totalWeight;
            float x = 0;

            float w = wSymWeight * unit;
            char[] label = (curLang == LANG_RU || curLang == LANG_EN) ? "?12".toCharArray() : "ABC".toCharArray();
            int type = (curLang == LANG_RU || curLang == LANG_EN) ? T_SYM : T_ABC;
            k(d, label, "SYM_TOGGLE".toCharArray(), type, x, y, w);
            x += w;

            w = wCommaWeight * unit;
            char[] comma = (curLang == LANG_EXTRA) ? "<".toCharArray() : ",".toCharArray();
            k(d, comma, comma, T_CHAR, x, y, w);
            x += w;

            if (!isSymbolLayout) {
                w = wLangWeight * unit;
                char[] langLabel = (lastLang == LANG_RU) ? "RU".toCharArray() : "EN".toCharArray();
                k(d, langLabel, "L".toCharArray(), T_LANG, x, y, w);
                x += w;
            }

            if (curLang == LANG_RU) {
                w = wExtraWeight * unit;
                k(d, "ё".toCharArray(), "ё".toCharArray(), T_CHAR, x, y, w);
                x += w;
            }

            w = wSpaceWeight * unit;
            k(d, " ".toCharArray(), " ".toCharArray(), T_SPACE, x, y, w);
            x += w;

            if (curLang == LANG_RU) {
                w = wExtraRightWeight * unit;
                k(d, "ъ".toCharArray(), "ъ".toCharArray(), T_CHAR, x, y, w);
                x += w;
            }

            w = wDotWeight * unit;
            char[] dot = (curLang == LANG_EXTRA) ? ">".toCharArray() : ".".toCharArray();
            k(d, dot, dot, T_CHAR, x, y, w);
            x += w;

            float enterWidth = screenW - x;
            if (enterWidth < 0) enterWidth = 0;
            k(d, currentEnterIcon, "\n".toCharArray(), T_ENTER, x, y, enterWidth);
        }

        private void k(LayoutData d, char[] l, char[] v, int t, float x, float y, float w) {
            int base = d.count * 4;
            d.keyCoords[base] = x;
            d.keyCoords[base + 1] = y;
            d.keyCoords[base + 2] = x + w;
            d.keyCoords[base + 3] = y + keyH;

            d.labels[d.count] = l;
            d.labelsLen[d.count] = l.length;
            d.values[d.count] = v;
            d.valuesLen[d.count] = v.length;
            d.types[d.count] = (byte) t;

            int r = (int) (y / keyH);
            if (r >= 0 && r < 4) d.grid[r][d.gridCount[r]++] = (byte) d.count;
            d.count++;
        }

        private void row(LayoutData d, float y, float w, String c) {
            int len = c.length();
            for (int i = 0; i < len; i++) {
                char ch = c.charAt(i);
                k(d, new char[]{ch}, new char[]{ch}, T_CHAR, i * w, y, w);
            }
        }

        private void rowW(LayoutData d, float y, float sx, float w, String c) {
            int len = c.length();
            for (int i = 0; i < len; i++) {
                char ch = c.charAt(i);
                k(d, new char[]{ch}, new char[]{ch}, T_CHAR, sx + i * w, y, w);
            }
        }

        private void rowNumeric(LayoutData d, float y, float w, String c) {
            int len = c.length();
            for (int i = 0; i < len; i++) {
                char ch = c.charAt(i);
                k(d, new char[]{ch}, new char[]{ch}, T_CHAR, i * w, y, w);
            }
        }

        private void ensureGlobalAtlas() {
            if (sGlobalAtlasBuilt) return;

            Set<Character> allChars = new HashSet<>();

            String ru = "йцукенгшщзхфывапролджэячсмитьбюё";
            for (int i = 0; i < ru.length(); i++) {
                char c = ru.charAt(i);
                allChars.add(c);
                allChars.add(Character.toUpperCase(c));
            }
            String en = "qwertyuiopasdfghjklzxcvbnm";
            for (int i = 0; i < en.length(); i++) {
                char c = en.charAt(i);
                allChars.add(c);
                allChars.add(Character.toUpperCase(c));
            }
            String symbols = "1234567890@#№_&-+()/*\"':;!?~`|•√π÷×¶∆●€$£¥₸₽^°={}\\©®™%[]■";
            for (int i = 0; i < symbols.length(); i++) {
                allChars.add(symbols.charAt(i));
            }
            allChars.add('↵');
            allChars.add('⌕');
            allChars.add('➤');
            allChars.add('→');
            allChars.add('✓');
            allChars.add('←');
            allChars.add('⌫');

            List<Character> list = new ArrayList<>(allChars);
            Collections.sort(list);

            float maxWidth = 0;
            for (char ch : list) {
                float w = pText.measureText(String.valueOf(ch));
                if (w > maxWidth) maxWidth = w;
            }
            sAtlasCellW = (int) maxWidth + 4;
            sAtlasCellH = (int) (pText.getFontSpacing() + 2);
            sAtlasCols = (int) Math.sqrt(list.size()) + 1;
            int rows = (list.size() + sAtlasCols - 1) / sAtlasCols;
            int atlasWidth = sAtlasCols * sAtlasCellW;
            int atlasHeight = rows * sAtlasCellH;

            if (sGlobalAtlas != null) {
                sGlobalAtlas.recycle();
            }
            sGlobalAtlas = Bitmap.createBitmap(atlasWidth, atlasHeight, Bitmap.Config.ALPHA_8);

            sCharToAtlasIndex = new int[Character.MAX_VALUE + 1];
            Arrays.fill(sCharToAtlasIndex, -1);

            Canvas c = new Canvas(sGlobalAtlas);
            Paint paint = new Paint(pText);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);

            for (int i = 0; i < list.size(); i++) {
                char ch = list.get(i);
                sCharToAtlasIndex[ch] = i;
                int col = i % sAtlasCols;
                int row = i / sAtlasCols;
                float x = col * sAtlasCellW + sAtlasCellW / 2f;
                float y = row * sAtlasCellH + (sAtlasCellH - pText.descent() - pText.ascent()) / 2f;
                c.drawText(String.valueOf(ch), x, y, paint);
            }

            sGlobalAtlasBuilt = true;
        }

        private void buildAtlas(LayoutData d) {
            ensureGlobalAtlas();
            for (int i = 0; i < d.count; i++) {
                if (d.types[i] == T_CHAR) {
                    char c = d.labels[i][0];
                    char cu = Character.toUpperCase(c);
                    if (c < sCharToAtlasIndex.length) {
                        d.normalIdx[i] = (byte) sCharToAtlasIndex[c];
                    }
                    if (cu < sCharToAtlasIndex.length) {
                        d.shiftIdx[i] = (byte) sCharToAtlasIndex[cu];
                    }
                }
            }
        }

        private static final int HITMAP_SCALE = 4;

        private void buildHitmap(LayoutData d) {
            d.hitmapW = screenW / HITMAP_SCALE + 1;
            d.hitmapH = (int) (keyH * 4) / HITMAP_SCALE + 1;
            int size = d.hitmapW * d.hitmapH;
            if (d.hitmap == null || d.hitmap.length != size) {
                d.hitmap = new byte[size];
            }
            Arrays.fill(d.hitmap, (byte) -1);

            for (int i = 0; i < d.count; i++) {
                int base = i * 4;
                int l = Math.max(0, (int) d.keyCoords[base]) / HITMAP_SCALE;
                int t = Math.max(0, (int) d.keyCoords[base + 1]) / HITMAP_SCALE;
                int r = Math.min(d.hitmapW - 1, (int) Math.ceil(d.keyCoords[base + 2]) / HITMAP_SCALE);
                int b = Math.min(d.hitmapH - 1, (int) Math.ceil(d.keyCoords[base + 3]) / HITMAP_SCALE);
                for (int y = t; y <= b; y++) {
                    int off = y * d.hitmapW;
                    for (int x = l; x <= r; x++) {
                        d.hitmap[off + x] = (byte) i;
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
            for (int i = 0; i < TOTAL_LAYOUTS; i++) {
                layouts[i].isBuilt = false;
            }
            ensureLayoutBuilt(curLang);
            curLayout = layouts[curLang];
            setOffsets(offsetLeft, offsetRight, offsetTop, offsetBottom);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(BG);

            canvas.save();
            canvas.translate(translateX, translateY);
            canvas.scale(scale, scale);

            if (lowEndMode) {
                drawStatic(canvas);
                drawPressedKeys(canvas);
            } else {
                if (needRedrawBg || bgBitmap == null || prevWidth != getWidth() || prevHeight != getHeight()) {
                    if (bgBitmap == null || prevWidth != getWidth() || prevHeight != getHeight()) {
                        bgBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
                        bgCanvas = new Canvas(bgBitmap);
                        prevWidth = getWidth();
                        prevHeight = getHeight();
                    }
                    bgCanvas.drawColor(BG);
                    drawStatic(bgCanvas);
                    needRedrawBg = false;
                }
                canvas.drawBitmap(bgBitmap, 0, 0, null);
                drawPressedKeys(canvas);
            }

            canvas.restore();

            if (isPreviewMode) {
                Paint borderPaint = new Paint();
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setStrokeWidth(2 * density);
                borderPaint.setColor(0xFF00FF00);
                canvas.drawRect(0, 0, getWidth(), getHeight(), borderPaint);
                borderPaint.setColor(0xFFFFA500);
                float left = translateX;
                float top = translateY;
                float right = translateX + screenW * scale;
                float bottom = translateY + (previewHeight + keyH * 4) * scale;
                canvas.drawRect(left, top, right, bottom, borderPaint);
            }
        }

        private void drawPressedKeys(Canvas canvas) {
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

                if (d.types[key] == T_CHAR && !(d.valuesLen[key] == 1 && d.values[key][0] == ' ') && previewHeight > 0) {
                    float cx = (l + r) / 2;
                    float pT = t - previewHeight;
                    float pB = t;
                    tempRectF.set(cx - 50, pT, cx + 50, pB);
                    canvas.drawRect(tempRectF, pPre);

                    int idx = (shift > 0) ? d.shiftIdx[key] : d.normalIdx[key];
                    if (idx >= 0) {
                        int col = idx % sAtlasCols;
                        int row = idx / sAtlasCols;
                        tempSrcRect.set(col * sAtlasCellW, row * sAtlasCellH,
                                (col + 1) * sAtlasCellW, (row + 1) * sAtlasCellH);
                        float cyPreview = (pT + pB) * 0.5f;
                        float halfW = sAtlasCellW * 0.5f;
                        float halfH = sAtlasCellH * 0.5f;
                        tempDstRectF.set(cx - halfW, cyPreview - halfH, cx + halfW, cyPreview + halfH);
                        canvas.drawBitmap(sGlobalAtlas, tempSrcRect, tempDstRectF, pAtlas);
                    } else {
                        char ch = d.labels[key][0];
                        if (shift > 0) ch = Character.toUpperCase(ch);
                        tmpChar[0] = ch;
                        canvas.drawText(tmpChar, 0, 1, cx,
                                (pT + pB) / 2f - previewBaselineOffset, pPreT);
                    }
                }
            }
        }

        private void drawStatic(Canvas cv) {
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
                        int col = idx % sAtlasCols;
                        int row = idx / sAtlasCols;
                        tempSrcRect.set(col * sAtlasCellW, row * sAtlasCellH,
                                (col + 1) * sAtlasCellW, (row + 1) * sAtlasCellH);
                        float halfW = sAtlasCellW * 0.5f;
                        float halfH = sAtlasCellH * 0.5f;
                        tempDstRectF.set(cx - halfW, cy - halfH, cx + halfW, cy + halfH);
                        cv.drawBitmap(sGlobalAtlas, tempSrcRect, tempDstRectF, pAtlas);
                    } else {
                        char ch = d.labels[i][0];
                        if (shift > 0) ch = Character.toUpperCase(ch);
                        tmpChar[0] = ch;
                        cv.drawText(tmpChar, 0, 1, cx, cy - textBaselineOffset, pText);
                    }
                } else if (d.types[i] == T_SHIFT) {
                    char[] shiftLabel;
                    if (shift == 1) shiftLabel = SHIFT_SINGLE;
                    else if (shift == 2) shiftLabel = SHIFT_LOCK;
                    else shiftLabel = SHIFT_NORMAL;
                    cv.drawText(shiftLabel, 0, shiftLabel.length, cx, cy - textBaselineOffset, pText);
                } else {
                    cv.drawText(d.labels[i], 0, d.labelsLen[i], cx, cy - textBaselineOffset, pText);
                }
            }
        }

        private int getK(LayoutData d, float x, float y) {
            int ix = (int) x / HITMAP_SCALE;
            int iy = (int) y / HITMAP_SCALE;
            if (iy < 0 || iy >= d.hitmapH || ix < 0 || ix >= d.hitmapW) return -1;
            return d.hitmap[iy * d.hitmapW + ix];
        }

        private void getDirtyRect(LayoutData d, int idx, Rect outRect) {
            int base = idx * 4;
            float left = d.keyCoords[base];
            float top = d.keyCoords[base + 1];
            float right = d.keyCoords[base + 2];
            float bottom = d.keyCoords[base + 3] + previewHeight;

            left = left * scale + translateX;
            top = top * scale + translateY;
            right = right * scale + translateX;
            bottom = bottom * scale + translateY;

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

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            int action = e.getActionMasked();
            int index = e.getActionIndex();
            int id = e.getPointerId(index);

            LayoutData d = curLayout;
            if (d == null) return true;

            float rawX = e.getX(index);
            float rawY = e.getY(index);
            float x = (rawX - translateX) / scale;
            float y = (rawY - translateY) / scale;

            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                int k = getK(d, x, y - previewHeight);
                if (k != -1) {
                    performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
                    int slot = findEmptyPointerSlot();
                    if (slot >= 0) {
                        pointerIds[slot] = id;
                        pointerKeys[slot] = k;
                    }
                    if (!isPreviewMode && d.types[k] == T_DEL) {
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
                Rect dirty = rectPool.obtain();
                Rect tmp = rectPool.obtain();
                dirty.setEmpty();

                for (int m = 0; m < e.getPointerCount(); m++) {
                    int pid = e.getPointerId(m);
                    float px = (e.getX(m) - translateX) / scale;
                    float py = (e.getY(m) - translateY) / scale;
                    int nk = getK(d, px, py - previewHeight);
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
                        if (!isPreviewMode && d.types[k] != T_DEL) {
                            handle(k);
                        }
                        pointerKeys[slot] = -1;
                        pointerIds[slot] = -1;
                        if (!isPreviewMode && d.types[k] == T_DEL) {
                            isDel = false;
                            h.removeCallbacks(delRunnable);
                        }
                        getDirtyRect(d, k, tempRect);
                        invalidate(tempRect);
                    }
                }
            } else if (action == MotionEvent.ACTION_CANCEL) {
                Rect dirty = rectPool.obtain();
                Rect tmp = rectPool.obtain();
                dirty.setEmpty();
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

        private void handle(int i) {
            LayoutData d = curLayout;
            int t = d.types[i];
            char[] v = d.values[i];
            int vLen = d.valuesLen[i];

            if (t == T_CHAR) {
                String sv = new String(v, 0, vLen);
                if (shift > 0) {
                    sv = sv.toUpperCase(Locale.ROOT);
                    if (shift == 1) {
                        shift = 0;
                        needRedrawBg = true;
                    }
                }
                ime.commitText(sv);
            } else if (t == T_SHIFT) {
                shift = (shift + 1) % 3;
                needRedrawBg = true;
                invalidate();
            } else if (t == T_SPACE) {
                ime.commitText(" ");
            } else if (t == T_ENTER) {
                ime.handleEnter();
            } else if (t == T_LANG) {
                if (isPreviewMode) {
                    lastLang = (lastLang == LANG_RU) ? LANG_EN : LANG_RU;
                    curLang = lastLang;
                    ensureLayoutBuilt(curLang);
                    curLayout = layouts[curLang];
                    setEnterIcon(currentEnterIcon);
                    needRedrawBg = true;
                    invalidate();
                } else {
                    lastLang = (lastLang == LANG_RU) ? LANG_EN : LANG_RU;
                    ime.saveLanguage(lastLang == LANG_RU ? "ru" : "en");
                    setLanguage(lastLang == LANG_RU ? "ru" : "en");
                    needRedrawBg = true;
                    invalidate();
                }
            } else if (t == T_SYM) {
                if (curLang == LANG_RU || curLang == LANG_EN) {
                    curLang = LANG_SYM;
                    ensureLayoutBuilt(LANG_SYM);
                    curLayout = layouts[LANG_SYM];
                } else if (curLang == LANG_SYM) {
                    curLang = lastLang;
                    ensureLayoutBuilt(curLang);
                    curLayout = layouts[curLang];
                } else if (curLang == LANG_EXTRA) {
                    curLang = LANG_SYM;
                    ensureLayoutBuilt(LANG_SYM);
                    curLayout = layouts[LANG_SYM];
                } else if (curLang == LANG_NUMERIC || curLang == LANG_DECIMAL) {
                    curLang = lastLang;
                    ensureLayoutBuilt(curLang);
                    curLayout = layouts[curLang];
                    numericMode = false;
                }
                setEnterIcon(currentEnterIcon);
                needRedrawBg = true;
                invalidate();
            } else if (t == T_SYM_EX) {
                curLang = LANG_EXTRA;
                ensureLayoutBuilt(LANG_EXTRA);
                curLayout = layouts[LANG_EXTRA];
                numericMode = false;
                setEnterIcon(currentEnterIcon);
                needRedrawBg = true;
                invalidate();
            } else if (t == T_ABC) {
                curLang = lastLang;
                ensureLayoutBuilt(curLang);
                curLayout = layouts[curLang];
                numericMode = false;
                setEnterIcon(currentEnterIcon);
                needRedrawBg = true;
                invalidate();
            }
        }
    }
}