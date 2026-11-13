package com.filmipaheli.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.Random;

/** A prize wheel. Segment 0 starts at the top and the segments run clockwise. */
public class WheelView extends View {

    /** Coins on each segment, clockwise from the top. */
    public static final int[] PRIZES = {10, 25, 15, 50, 10, 20, 100, 30};
    /** How likely each segment is, out of 100. Averages about 20 coins a spin. */
    private static final int[] WEIGHTS = {22, 14, 18, 8, 22, 10, 2, 4};

    private final int[] colors = {
            0xFFF0386B, 0xFF19C3A6, 0xFFFFB224, 0xFF6B4FD1,
            0xFFF0386B, 0xFF19C3A6, 0xFFFFB224, 0xFF6B4FD1};
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF box = new RectF();
    private final Random random = new Random();

    public WheelView(Context c) {
        this(c, null);
    }

    public WheelView(Context c, AttributeSet a) {
        super(c, a);
        text.setColor(0xFF170E2B);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        rim.setStyle(Paint.Style.STROKE);
        rim.setColor(0xFFFFF0D2);
    }

    /** Picks a segment with the weights above. */
    public int pickIndex() {
        int roll = random.nextInt(100), sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += WEIGHTS[i];
            if (roll < sum) return i;
        }
        return 0;
    }

    /** Spins and stops with segment 'index' under the pointer at the top. */
    public void spinTo(int index, Runnable done) {
        float segment = 360f / PRIZES.length;
        float centre = index * segment + segment / 2f + (random.nextFloat() - 0.5f) * segment * 0.6f;
        float current = getRotation() % 360f;
        float wanted = (360f - centre) % 360f;
        float delta = ((wanted - current) % 360f + 360f) % 360f + 360f * 5;
        animate().rotationBy(delta).setDuration(4200).setInterpolator(new DecelerateInterpolator(1.6f))
                .withEndAction(done).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float size = Math.min(getWidth(), getHeight());
        float pad = size * 0.03f;
        box.set(pad, pad, size - pad, size - pad);
        float segment = 360f / PRIZES.length;
        float radius = (size - 2 * pad) / 2f;
        float cx = size / 2f, cy = size / 2f;
        text.setTextSize(size * 0.075f);
        rim.setStrokeWidth(size * 0.025f);

        for (int i = 0; i < PRIZES.length; i++) {
            fill.setColor(colors[i]);
            canvas.drawArc(box, -90f + i * segment, segment, true, fill);

            canvas.save();
            canvas.rotate(i * segment + segment / 2f, cx, cy);
            canvas.drawText(String.valueOf(PRIZES[i]), cx, cy - radius * 0.68f, text);
            canvas.restore();
        }
        canvas.drawCircle(cx, cy, radius, rim);
        fill.setColor(0xFFFFF0D2);
        canvas.drawCircle(cx, cy, size * 0.06f, fill);
    }

    @Override
    protected void onMeasure(int w, int h) {
        int size = Math.min(MeasureSpec.getSize(w), MeasureSpec.getSize(h) > 0 ? MeasureSpec.getSize(h) : MeasureSpec.getSize(w));
        setMeasuredDimension(size, size);
    }
}
