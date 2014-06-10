package com.bees4honey.vinscanner;

import android.graphics.Color;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/*
 * View which for displaying scanning area and laser
 */
public class ViewFinder extends View {
    public enum ContentOrientation {
        VERTICAL,
        HORIZONTAL
    }

    private final android.graphics.Rect box;    // rectangle for temporary operations
    private final int darkColor;
    private android.graphics.Rect scanningFrame;    //a frame to represent the scanning area.
    private final int laserColor;
    private final int lightColor;
    private final int maskColor;
    private final android.graphics.Paint paint;
    private boolean running;
    private ContentOrientation contentOrientation;

    public ViewFinder(Context context, AttributeSet attrs) {
        // Call for parent method
        super(context, attrs);

        scanningFrame = new Rect();        // create rectangle for PREVIEW
        running = false;        // when creating launch is not done
        paint = new Paint();        // init drawing object
        box = new Rect();        // rectangle for temporary operations

        // get application resources
        Resources res = getResources();

        // set colors
        maskColor = Color.parseColor("#CC444444");
        laserColor = Color.parseColor("#FF0000");
        darkColor = Color.parseColor("#333333");
        lightColor = Color.parseColor("#AAAAAA");
        contentOrientation = ContentOrientation.HORIZONTAL;
    }

    // called when displaying screen's elements
    @Override
    protected void onDraw(Canvas canvas) {
        if (running) {
            switch (contentOrientation) {
                case HORIZONTAL:
                    //draw horizontal scanning area
                    drawWithHorizontalOrientation(canvas);
                    break;
                case VERTICAL:
                    //draw vertical scanning area
                    drawWithVerticalOrientation(canvas);
                    break;
                default:
                    //do nothing
            }

            // Cause an invalidate of the specified area to happen on a subsequent
            // cycle through the event loop. Waits for the specified amount of time.
            postInvalidateDelayed(100, box.left, box.top, box.right, box.bottom);
        }
    }

    private void drawWithVerticalOrientation(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int width3 = width / 3;

        // Set frame for the scanning area
        scanningFrame.set(width3, 0, width - width3, height);

        // set color and size of upper dark rectangle
        paint.setColor(maskColor);
        box.set(0, 0, scanningFrame.left - 2, height);
        // draw left dark rectangle
        canvas.drawRect(box, paint);

        // set color and size of right dark rectangle:
        box.set(scanningFrame.right + 2, 0, width, height);
        canvas.drawRect(box, paint);

        // draw lines on borders of the scanning area
        // draw lines on the left
        paint.setColor(lightColor);
        canvas.drawLine(scanningFrame.left - 2, 0.0f, scanningFrame.left - 2, height, paint);
        paint.setColor(darkColor);
        canvas.drawLine((float) scanningFrame.left - 1, 0.0f, (float) scanningFrame.left - 1, height, paint);
        // draw lines on the right
        paint.setColor(darkColor);
        canvas.drawLine((float) scanningFrame.right, 0.0f, (float) scanningFrame.right, (float) height, paint);
        paint.setColor(lightColor);
        canvas.drawLine((float) scanningFrame.right + 1, 0.0f, (float) scanningFrame.right + 1, height, paint);

        //draw laser in the center of the scanning area
        int tmp1 = scanningFrame.width() / 2 + scanningFrame.left;
        box.set(tmp1 - 2, 0, tmp1 + 2, height);
        paint.setColor(laserColor);
        canvas.drawRect(box, paint);
    }

    private void drawWithHorizontalOrientation(Canvas canvas) {
        int width = getWidth();            // get width
        int height = getHeight();        // get height
        int height3 = height / 3;            // calculate height of displayed rectangle

        // Set frame for the scanning area
        scanningFrame.set(0, height3, width, height - height3);

        // set color and size of upper dark rectangle
        paint.setColor(maskColor);
        box.set(0, 0, width, scanningFrame.top - 2);
        //draw upper dark rectangle
        canvas.drawRect(box, paint);

        // set size of bottom dark rectangle
        box.set(0, scanningFrame.bottom - 2, width, height);
        //draw bottom dark rectangle
        canvas.drawRect(box, paint);

        // draw lines on borders of PREVIEW area
        // draw lines at the top
        paint.setColor(lightColor);
        canvas.drawLine(0.0f, (float) scanningFrame.top - 2, (float) width, (float) scanningFrame.top - 2, paint);
        paint.setColor(darkColor);
        canvas.drawLine(0.0f, (float) scanningFrame.top - 1, (float) width, (float) scanningFrame.top - 1, paint);
        // draw lines at the bottom
        paint.setColor(darkColor);
        canvas.drawLine(0.0f, (float) scanningFrame.bottom, (float) width, (float) scanningFrame.bottom, paint);
        paint.setColor(lightColor);
        canvas.drawLine(0.0f, (float) scanningFrame.bottom + 1, (float) width, (float) scanningFrame.bottom + 1, paint);

        // Draw laser in the middle of the screen
        int tmp1 = scanningFrame.height() / 2 + scanningFrame.top;
        box.set(0, tmp1 - 1, width, tmp1 + 2);
        paint.setColor(laserColor);
        canvas.drawRect(box, paint);
    }

    // method for changing work state
    void setRunning(boolean running) {
        if (this.running != running) {
            this.running = running;
            invalidate();        // init redrawing of window
        }
    }

    public void setContentOrientation(ContentOrientation contentOrientation) {
        if (this.contentOrientation != contentOrientation) {
            this.contentOrientation = contentOrientation;
            invalidate();        // init redrawing of window
        }
    }
}
