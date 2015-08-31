package com.bees4honey.vinscanner;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/*
 * View which for displaying scanning area for both 1D and 2D vin codes
 */
public class ViewFinder extends View {
    public enum ContentOrientation {
        VERTICAL,
        HORIZONTAL
    }

    private static final int CROSS_RADIUS = 20;
    private final android.graphics.Rect box;    // rectangle for temporary operations
    private final int laserColor;
    private final android.graphics.Paint paint;
    private boolean running;
    private ContentOrientation contentOrientation;

    public ViewFinder(Context context, AttributeSet attrs) {
        // Call for parent method
        super(context, attrs);

        running = false;        // when creating launch is not done
        paint = new Paint();        // init drawing object
        box = new Rect();        // rectangle for temporary operations

        // set colors
        laserColor = Color.parseColor("#FF0000");
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

        //draw vertical laser in the center of the scanning area
        int laserCenter = width/2;
        box.set(laserCenter - 2, 0, laserCenter + 2, height);
        paint.setColor(laserColor);
        canvas.drawRect(box, paint);

        // Draw little cross in the middle
        int crossCenter = height / 2;
        box.set(width/2 - CROSS_RADIUS, crossCenter - 2, width/2 + CROSS_RADIUS, crossCenter + 2);
        canvas.drawRect(box, paint);
    }

    private void drawWithHorizontalOrientation(Canvas canvas) {
        int width = getWidth();            // get width
        int height = getHeight();        // get height

        // Draw horizontal laser in the middle of the screen
        int laserCenter = height / 2;
        box.set(0, laserCenter - 2, width, laserCenter + 2);
        paint.setColor(laserColor);
        canvas.drawRect(box, paint);

        // Draw little cross in the middle
        int crossCenter = width / 2;
        box.set(crossCenter - 2, height/2 - CROSS_RADIUS, crossCenter + 2, height/2 + CROSS_RADIUS);
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
