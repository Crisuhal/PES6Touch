package net.optijuegos.pes6touch;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String GAME_URL = "https://pes6.optijuegos.net/";
    private WebView web;
    private ControllerOverlay controller;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        immersive();

        FrameLayout root = new FrameLayout(this);
        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        web.setFocusable(true);
        web.setFocusableInTouchMode(true);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient());
        root.addView(web, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        controller = new ControllerOverlay();
        root.addView(controller, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(root);
        web.requestFocus();
        web.loadUrl(GAME_URL);
    }

    private void sendKey(int code, boolean down) {
        if (web == null) return;
        web.requestFocus();
        long now = SystemClock.uptimeMillis();
        web.dispatchKeyEvent(new KeyEvent(now, now, down ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP, code, 0));
    }

    private void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) immersive();
    }

    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (controller != null) controller.releaseAll();
        if (web != null) web.destroy();
        super.onDestroy();
    }

    private final class ControllerOverlay extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final List<Button> buttons = new ArrayList<>();
        private final Map<Integer, Integer> pointerButtons = new HashMap<>();
        private final Map<Integer, Set<Integer>> sources = new HashMap<>();
        private final Map<Integer, Integer> keyCounts = new HashMap<>();
        private int stickPointer = -1;
        private float width, height, density, stickX, stickY, stickRadius, stickKnobX, stickKnobY;
        private float faceX, faceY, faceRadius, faceSpacing;
        private static final int STICK_SOURCE = -999;
        private static final int CIRCLE_BLUE = 0xDD2366B5;
        private static final int CIRCLE_RED = 0xDDD34343;
        private static final int CIRCLE_PURPLE = 0xDD843FA2;
        private static final int CIRCLE_GREEN = 0xDD278C60;

        private final class Button {
            final String id, label;
            final int key, color;
            final RectF rect;
            final float cx, cy, radius;
            final boolean round;
            Button(String id, String label, int key, int color, RectF rect) {
                this.id = id; this.label = label; this.key = key; this.color = color; this.rect = rect;
                this.cx = rect.centerX(); this.cy = rect.centerY(); this.radius = rect.width() / 2f; this.round = true;
            }
            Button(String id, String label, int key, RectF rect) {
                this.id = id; this.label = label; this.key = key; this.color = 0xAA18212D; this.rect = rect;
                this.cx = rect.centerX(); this.cy = rect.centerY(); this.radius = 0; this.round = false;
            }
        }

        ControllerOverlay() {
            super(MainActivity.this);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            setClickable(true);
            setFocusable(false);
        }

        private float dp(float value) { return value * density; }

        private void layoutControls() {
            density = getResources().getDisplayMetrics().density;
            width = getWidth(); height = getHeight();
            if (width <= 0 || height <= 0) return;
            buttons.clear();
            float margin = dp(14), top = dp(12), pillW = dp(58), pillH = dp(34), gap = dp(7);
            addPill("L2", "L2", KeyEvent.KEYCODE_Z, margin, top, pillW, pillH);
            addPill("L1", "L1", KeyEvent.KEYCODE_Q, margin + pillW + gap, top, pillW, pillH);
            addPill("R1", "R1", KeyEvent.KEYCODE_E, width - margin - (pillW * 2) - gap, top, pillW, pillH);
            addPill("R2", "R2", KeyEvent.KEYCODE_C, width - margin - pillW, top, pillW, pillH);
            float mid = width / 2f;
            addPill("SELECT", "SELECT", KeyEvent.KEYCODE_DEL, mid - dp(77), top, dp(68), pillH);
            addPill("START", "START", KeyEvent.KEYCODE_SPACE, mid + dp(9), top, dp(68), pillH);

            stickRadius = Math.min(dp(66), height * .19f);
            stickX = dp(22) + stickRadius;
            stickY = height - dp(18) - stickRadius;
            if (stickPointer < 0) { stickKnobX = stickX; stickKnobY = stickY; }

            faceRadius = Math.min(dp(28), height * .078f);
            faceSpacing = faceRadius * 1.75f;
            faceX = width - dp(24) - faceRadius - faceSpacing;
            faceY = height - dp(19) - faceRadius - faceSpacing;
            addFace("TRIANGLE", "△", KeyEvent.KEYCODE_W, CIRCLE_GREEN, faceX, faceY - faceSpacing);
            addFace("SQUARE", "□", KeyEvent.KEYCODE_A, CIRCLE_PURPLE, faceX - faceSpacing, faceY);
            addFace("CIRCLE", "○", KeyEvent.KEYCODE_D, CIRCLE_RED, faceX + faceSpacing, faceY);
            addFace("CROSS", "×", KeyEvent.KEYCODE_X, CIRCLE_BLUE, faceX, faceY + faceSpacing);
        }

        private void addPill(String id, String label, int key, float x, float y, float w, float h) {
            buttons.add(new Button(id, label, key, new RectF(x, y, x + w, y + h)));
        }
        private void addFace(String id, String label, int key, int color, float x, float y) {
            buttons.add(new Button(id, label, key, color, new RectF(x - faceRadius, y - faceRadius, x + faceRadius, y + faceRadius)));
        }

        @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            layoutControls();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            layoutControls();
            if (width <= 0 || height <= 0) return;
            drawStick(canvas);
            for (Button b : buttons) drawButton(canvas, b);
        }

        private void drawStick(Canvas canvas) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x66252D38);
            canvas.drawCircle(stickX, stickY, stickRadius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(0xDDEEF2F7);
            canvas.drawCircle(stickX, stickY, stickRadius, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x22FFFFFF);
            canvas.drawCircle(stickX, stickY, stickRadius * .63f, paint);
            float knobR = stickRadius * .34f;
            paint.setColor(0xDDE7ECF3);
            canvas.drawCircle(stickKnobX, stickKnobY, knobR, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(Color.WHITE);
            canvas.drawCircle(stickKnobX, stickKnobY, knobR, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        private void drawButton(Canvas canvas, Button b) {
            boolean active = keyCounts.containsKey(b.key);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(active ? 0xF0FFFFFF : b.color);
            if (b.round) canvas.drawCircle(b.cx, b.cy, b.radius, paint);
            else canvas.drawRoundRect(b.rect, dp(18), dp(18), paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1.5f));
            paint.setColor(active ? Color.WHITE : 0xDDEEF2F7);
            if (b.round) canvas.drawCircle(b.cx, b.cy, b.radius, paint);
            else canvas.drawRoundRect(b.rect, dp(18), dp(18), paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(active ? 0xFF17202A : Color.WHITE);
            paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(b.round ? faceRadius * 1.15f : dp(b.label.length() > 4 ? 10 : 12));
            Paint.FontMetrics fm = paint.getFontMetrics();
            canvas.drawText(b.label, b.cx, b.cy - (fm.ascent + fm.descent) / 2f, paint);
        }

        private Button hitButton(float x, float y) {
            for (Button b : buttons) {
                if (b.round) {
                    float dx = x - b.cx, dy = y - b.cy;
                    if (dx * dx + dy * dy <= (b.radius + dp(8)) * (b.radius + dp(8))) return b;
                } else if (b.rect.contains(x, y)) return b;
            }
            return null;
        }

        private boolean insideStick(float x, float y) {
            float dx = x - stickX, dy = y - stickY;
            return dx * dx + dy * dy <= (stickRadius * 1.45f) * (stickRadius * 1.45f);
        }

        private void updateStick(float x, float y) {
            float dx = x - stickX, dy = y - stickY;
            float max = stickRadius * .68f;
            float len = (float) Math.hypot(dx, dy);
            if (len > max && len > 0) { dx *= max / len; dy *= max / len; }
            stickKnobX = stickX + dx;
            stickKnobY = stickY + dy;
            float nx = dx / max, ny = dy / max;
            Set<Integer> next = new HashSet<>();
            if (ny < -.28f) next.add(KeyEvent.KEYCODE_I);
            if (ny > .28f) next.add(KeyEvent.KEYCODE_K);
            if (nx < -.28f) next.add(KeyEvent.KEYCODE_J);
            if (nx > .28f) next.add(KeyEvent.KEYCODE_L);
            updateSource(STICK_SOURCE, next);
            invalidate();
        }

        private void updateSource(int source, Set<Integer> next) {
            Set<Integer> old = sources.get(source);
            if (old == null) old = new HashSet<>();
            for (int code : old) if (!next.contains(code)) removeKey(code);
            for (int code : next) if (!old.contains(code)) addKey(code);
            if (next.isEmpty()) sources.remove(source);
            else sources.put(source, new HashSet<>(next));
        }

        private void addKey(int code) {
            int count = keyCounts.containsKey(code) ? keyCounts.get(code) : 0;
            keyCounts.put(code, count + 1);
            if (count == 0) sendKey(code, true);
        }

        private void removeKey(int code) {
            Integer count = keyCounts.get(code);
            if (count == null) return;
            if (count <= 1) { keyCounts.remove(code); sendKey(code, false); }
            else keyCounts.put(code, count - 1);
        }

        private boolean beginPointer(int id, float x, float y) {
            if (insideStick(x, y) && stickPointer < 0) {
                stickPointer = id;
                updateStick(x, y);
                return true;
            }
            Button b = hitButton(x, y);
            if (b != null) {
                pointerButtons.put(id, b.key);
                Set<Integer> one = new HashSet<>(); one.add(b.key);
                updateSource(id, one);
                invalidate();
                return true;
            }
            return !pointerButtons.isEmpty() || stickPointer >= 0;
        }

        private void endPointer(int id) {
            Integer key = pointerButtons.remove(id);
            if (key != null) updateSource(id, new HashSet<Integer>());
            if (stickPointer == id) {
                stickPointer = -1;
                updateSource(STICK_SOURCE, new HashSet<Integer>());
                stickKnobX = stickX; stickKnobY = stickY;
            }
            invalidate();
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            int action = event.getActionMasked();
            int index = event.getActionIndex();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                boolean handled = beginPointer(event.getPointerId(index), event.getX(index), event.getY(index));
                return handled;
            }
            if (action == MotionEvent.ACTION_MOVE) {
                boolean handled = !pointerButtons.isEmpty() || stickPointer >= 0;
                if (stickPointer >= 0) {
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        if (event.getPointerId(i) == stickPointer) {
                            updateStick(event.getX(i), event.getY(i));
                            break;
                        }
                    }
                }
                return handled;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                int id = event.getPointerId(index);
                boolean handled = pointerButtons.containsKey(id) || stickPointer == id || !pointerButtons.isEmpty() || stickPointer >= 0;
                endPointer(id);
                return handled;
            }
            if (action == MotionEvent.ACTION_CANCEL) { releaseAll(); return true; }
            return !pointerButtons.isEmpty() || stickPointer >= 0;
        }

        void releaseAll() {
            for (int code : new HashSet<>(keyCounts.keySet())) sendKey(code, false);
            keyCounts.clear(); sources.clear(); pointerButtons.clear(); stickPointer = -1;
            stickKnobX = stickX; stickKnobY = stickY;
            invalidate();
        }
    }
}
