package com.bees4honey.vinscanner;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

/*
Simple text view which can be rotated in four directions.
 */
public class RotatableTextView extends TextView {

    public enum TextOrientations {
        NORMAL, REVERSE, TOP_DOWN, DOWN_TOP
    }

    private TextOrientations textOrientation;

    public RotatableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        textOrientation = TextOrientations.NORMAL;
    }

    public void setTextOrientation(TextOrientations textOrientation) {
        this.textOrientation = textOrientation;
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        switch (textOrientation) {
            case NORMAL:
                break;
            case REVERSE:
                canvas.rotate(180, getWidth()/2, getHeight()/2);
                break;
            case TOP_DOWN:
                canvas.rotate(90, getWidth()/2, getHeight()/2);
                break;
            case DOWN_TOP:
                canvas.rotate(-90, getWidth()/2, getHeight()/2);
        }

        canvas.clipRect(0, 0, getWidth(), getHeight(), android.graphics.Region.Op.REPLACE);
        super.draw(canvas);
    }
}
