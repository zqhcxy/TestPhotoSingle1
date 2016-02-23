package com.example.testphoto.views;


import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;


/**
 * Created by yangj on 2015/12/11.
 */
public class StrokeTextView extends TextView {
    public static final int MINHEIGHT = 56;
    public StrokeTextView(Context context) {
        super(context);
        init();
    }

    public StrokeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public StrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float mDensity = getContext().getResources().getDisplayMetrics().density;
        setMinimumWidth((int) (0.5F + MINHEIGHT * mDensity));
        setGravity(Gravity.CENTER);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            ViewHelper.setAlpha(this, 1);
        } else {
            ViewHelper.setAlpha(this, 0.5f);
        }
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        setBackgroundDrawable(createRoundCornerShapeDrawable(3, 2, color));
    }

    /**
     * @param borderLength: 一般取较小的值，比如10以内
     */
    private ShapeDrawable createRoundCornerShapeDrawable(float radius, float borderLength, int borderColor) {
        float[] outerRadii = new float[8];
        float[] innerRadii = new float[8];
        for (int i = 0; i < 8; i++) {
            outerRadii[i] = radius + borderLength;
            innerRadii[i] = radius;
        }

        ShapeDrawable sd = new ShapeDrawable(new RoundRectShape(outerRadii, new RectF(borderLength, borderLength,
                borderLength, borderLength), innerRadii));
        sd.getPaint().setColor(borderColor);

        return sd;
    }

}
