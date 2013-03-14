package com.bees4honey.vinscanner;

import com.bees4honey.vinscanner.example.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

// Class for dilspaying elements on screen
public class ViewFinder extends View 
{
	private final android.graphics.Rect box;
	private android.graphics.Rect clipRect;		
	private final int darkColor; 
	private android.graphics.Rect frame; 
	private final int laserColor; 
	private final int lightColor; 
	private final int maskColor;
	private final android.graphics.Paint paint; 
	private boolean running;

	// Constructor
	public ViewFinder(Context context, AttributeSet attrs) 
	{
		// Call for parent method
		super(context, attrs);

		clipRect = new Rect();		// create rectangle for cropping PREVIEW
		frame = new Rect();		// create rectangle for PREVIEW
		running = false;		// when creating launch is not done
		paint = new Paint();		// init drawing object
		box = new Rect();		// rectangle for temporary operations

		// get application resources
		Resources res = getResources();
		
		// set colors 
		maskColor = res.getColor(R.color.viewfinder_mask);
		laserColor = res.getColor(R.color.viewfinder_laser);
		darkColor = res.getColor( R.color.viewfinder_dark_line );
		lightColor = res.getColor(R.color.viewfinder_light_line);
	}
	
	// method returns flag of work state
	boolean isRunning()
	{
		return running;
	}
	
	// called when displaying screen's elements
	@Override
	protected void onDraw(Canvas canvas) 
	{
		if( running )	
		{
			int width = getWidth();			// get width
			int height = getHeight();		// get height
			int height3 = height/3;			// calculate height of displayed rectangle

			// set frame size for displaying
			frame.set(0, height3, width, height-height3);

			// get flag of crossing between displayed rectangle and cropped part
			boolean notEmpty = canvas.getClipBounds(clipRect);	
			
			// set color and size of upper dark rectangle
			paint.setColor(maskColor);
			box.set(0, 0, width, frame.top-2);

			// if there are crossings then draw rectangle
			if( !notEmpty || android.graphics.Rect.intersects(box,clipRect) )
					canvas.drawRect(box, paint);
			
			// draw central part
			// set size 
			box.set(0, frame.bottom-2, width, height);
			
			// if there are crossings, then call for drawing
			if( !notEmpty || android.graphics.Rect.intersects(box,clipRect) )
					canvas.drawRect( box, paint);
			
			// draw lines on borders of PREVIEW area
			if( !notEmpty  || clipRect.intersects(0, frame.top-2, width, frame.top))
			{
				paint.setColor(lightColor);
				canvas.drawLine( 0.0f, (float)frame.top-2, (float)width, (float)frame.top-2, paint);
				paint.setColor(darkColor);
				canvas.drawLine( 0.0f, (float)frame.top-1, (float)width, (float)frame.top-1, paint);
			}
			
			if( !notEmpty || clipRect.intersects(0, frame.bottom, width, frame.bottom+2))
			{
					paint.setColor(darkColor);
					canvas.drawLine( 0.0f, (float)frame.bottom, (float)width, (float)frame.bottom, paint);
					paint.setColor(lightColor);
					canvas.drawLine( 0.0f, (float)frame.bottom+1, (float)width, (float)frame.bottom+1, paint);
			}
			
			// set color and size of upper dark rectangle
			int tmp1 = frame.height() / 2 + frame.top;
			box.set(0, tmp1-1, width, tmp1+2);
			
			// if there are crossings then dra rectangle
			if( !notEmpty || android.graphics.Rect.intersects(box, clipRect))
			{ 
				paint.setColor(laserColor);
				canvas.drawRect(box, paint);
			}
			
			// Cause an invalidate of the specified area to happen on a subsequent 
			// cycle through the event loop. Waits for the specified amount of time.
			postInvalidateDelayed( 100, box.left, box.top, box.right, box.bottom);
		}
	}

	// method for changing work state
	void setRunning( boolean flag )
	{
		if( running != flag )	// If flag is changed
		{
			running = flag;		// set flag
			invalidate();		// init redraw of window
		}
	}
}
