package com.bees4honey.vinbarscanner;

import android.graphics.Matrix;
import android.view.View;
import android.view.animation.Transformation;

// Class for controlling screen turns
public class Rotator extends android.widget.FrameLayout
{
	private boolean rotated;
	
	// costructor
	public Rotator (android.content.Context context, android.util.AttributeSet attrSet )
	{
		super( context, attrSet);
	}
	
	// method called by framework for turning child objects
	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t)
	{
		Matrix matrix = t.getMatrix();
		matrix.reset();
		matrix.postRotate( 180.0f );	
		
		matrix.postTranslate( (float)child.getWidth(), (float)child.getHeight());
		return true;
	}
	
	// method returns flag of screen turn
	public boolean isRotated()
	{
		return rotated;
	}
	
	// method called by framework when layout
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		// call of parent method
		super.onLayout(changed, left, top, right, bottom);
		
		int _width = right - left;		// calculate width
		int _height = bottom - top;		// calculate height
		
		if( rotated )
		{
			// if screen turn is set
			
			int childCount = getChildCount();	// get amount of child objects
			
			// cycle on all child objects
			for( int i = 0; i<childCount; i++)
			{
				View view = getChildAt( i );	// get link on a child
				
				// get object coordinates
				int _left = view.getLeft();
				int _top = view.getTop();	
				
				// change coordinates of selected object
				view.layout( _width - _left - view.getWidth(), 
						_height - _top - view.getHeight(),
						_width - _left, 
						_height - _top);
			}	
		}
	}
	
	// set screen turn
	public void setRotated( boolean rotate )
	{
		if( rotated )
		{
			rotated = rotate;
			setStaticTransformationsEnabled( rotate );
			requestLayout();
			invalidate();
		}
	}
}
