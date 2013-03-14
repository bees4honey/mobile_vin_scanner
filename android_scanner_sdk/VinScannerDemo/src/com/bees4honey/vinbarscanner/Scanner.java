package com.bees4honey.vinbarscanner;

import java.io.FileDescriptor;
import java.util.List;
import java.util.concurrent.CountDownLatch;
 
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.bees4honey.vinscanner.B4HScanner;
import com.bees4honey.vinscanner.example.R;

public class Scanner 
	extends Activity implements SurfaceHolder.Callback, SensorEventListener
 {
	/**************************************************************************/	
	private static final int AUTOFOCUS_DELAY = 500;
	private static final int AUTOFOCUS_TIMEOUT = 1500;
	private static final int IDX_BEEP = 0;
	private static final int IDX_VIBRATE = 1;

	private volatile Camera camera;
	private final Camera.AutoFocusCallback cameraAutoFocusCallback;
	private CountDownLatch cameraLatch;
	CameraThread cameraThread; 
	private final B4HScanner d;
	Handler handler;
	private SurfaceHolder holder;
	private long lastAutofocus;
	private MediaPlayer mediaPlayer;
	private Camera.Size previewSize;
	private boolean previewing;
	private double quality;
	private Rotator rotator;
	private boolean scanning;
	private SensorManager sensorManager;
	private boolean settings[] = { true, true} ;
	private boolean surfaceChangedDelayed;
	private ViewFinder viewfinder_view;
	private final Runnable watchdog;
	private Button buttonTorchOnOff;
	
	private TorchControl torchControl;
	
	public Integer frameCount = 0;

	private final Camera.PreviewCallback cameraPreviewCallback;
	Toast vincode;
	
	/**************************************************************************/	

	// class of camera work thread
	class CameraThread extends Thread
	{
		android.os.Handler inner_handler;
		Scanner _this;
		
		// constructor
		CameraThread( Scanner scanner )
		{
			super();
			_this = scanner;
		}

		// constructor 
		CameraThread( Scanner scanner, CameraThread camThread )
		{
			this( scanner );
		}

		// method for finishing thread
		public void finish()
		{
			handler.sendEmptyMessage( 0 );
			try
			{
				join( getId());
			}
			catch (Exception e) {
			}
		}
		
		@Override
		public void run() 
		{
			// prepare looper for camera thread
			android.os.Looper.prepare();
			
			// start camera
			Scanner.this.camera = android.hardware.Camera.open();
			
			// class for processing messages
			class ThreadHandler extends Handler  
			{
				private final Camera val;
				
				// constructor
				ThreadHandler( Camera cam )
				{
					super();
					val = cam;					
				}
			
				// handler for messages queue
				@Override
				public void handleMessage(Message msg) 
				{
					if( msg.what == R.id.quit )
					{	
						Looper.myLooper().quit();
						Scanner.this.camera = null;
						val.release();
					}
				}
			}
			
			inner_handler = new ThreadHandler( camera );
			Scanner.this.cameraLatch.countDown();
			android.os.Looper.loop();
		}
	}

	// constructor
	public Scanner()
	{
		super();
		
		// initialize class objects
		previewing = false;
		scanning = false;
		lastAutofocus = 0;
		surfaceChangedDelayed = false;
		
		// object for handling messages
		handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				
				switch( msg.what )
				{
					case R.id.decode:
						Log.d("tag", "decode");
						// message called when getting image from camera
						Scanner.this.decode( (byte[]) msg.obj );
						break;

					case R.id.focused:
						Log.d("tag", "focused");
						// message called when autofocusing
						Scanner.this.lastAutofocus = System.currentTimeMillis() - AUTOFOCUS_TIMEOUT + AUTOFOCUS_DELAY;
						break;
					
					case R.id.TorchOnOff:
						Log.d("tag", "TorchOnOff");
						Scanner.this.camera.setPreviewCallback( null );
						
						if( Scanner.this.torchControl != null )
						{
							Scanner.this.torchControl.torch( !Scanner.this.torchControl.isEnabled());
							break;
						}

						Camera.Parameters CameraParameters = Scanner.this.camera.getParameters();
			    	
						if( CameraParameters.getFlashMode().compareTo( android.hardware.Camera.Parameters.FLASH_MODE_TORCH ) == 0)
						{
							Log.i("VinScanner", "Torch Off");
							buttonTorchOnOff.setBackgroundDrawable(	getResources().getDrawable( R.drawable.light));
							CameraParameters.setFlashMode( android.hardware.Camera.Parameters.FLASH_MODE_OFF);
						}
						else
						{
							Log.i("VinScanner", "Torch On");
							buttonTorchOnOff.setBackgroundDrawable(	getResources().getDrawable( R.drawable.light_on));
							CameraParameters.setFlashMode( android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
						}
	
						// refresh camera settings
						try
						{	
							Scanner.this.camera.setParameters(CameraParameters);		
						}
						catch (Exception e) 
						{
							Log.e("VinScanner", "torch On/Off Exception", e);
					    	Message nextmsg = handler.obtainMessage( R.id.TorchOnOff );
							sendMessageDelayed(nextmsg, 100);
							Log.e("VinScanner", "send message");
						}

						break;
						
					case R.id.decoded:
						// message called when VIN code is obtained successfully						
						// continue scanning
						setScanning(true);
				}
				
				// call parent method to process any other messages
				super.handleMessage(msg);
			}
		};

		// object of processing event when camera is autofocused
		cameraAutoFocusCallback = new android.hardware.Camera.AutoFocusCallback()
		{
			public void onAutoFocus(boolean success, Camera camera) 
			{
				// message is sent when autofocused
				Scanner.this.handler.sendEmptyMessageDelayed( R.id.focused, 500);
			}
		};

		// object for processing event when image is obtained from camera
		cameraPreviewCallback = new Camera.PreviewCallback()
		{
			public void onPreviewFrame(byte[] data, Camera camera) 
			{
				// if scanning is on
				if( isScanning() )
				{
					// then message is sent containing picture
					Message msg = Scanner.this.handler.obtainMessage( R.id.decode, data );
					Scanner.this.handler.sendMessage( msg );
				}
			}
		};

		// object which calls itself in defined time and intended
		// for restarting autofocus and getting image for processing
		watchdog = new Runnable()
		{
			public void run() 
			{
				handler.postDelayed( watchdog,  100);
				
				// get current time
				long currentTime = java.lang.System.currentTimeMillis();

				if( previewing )    
				{
					if( isScanning() )
					{
						// programm is in PREVIEW and SCANNING mode
						// If enough time is gone since last autofocus
						if( currentTime - lastAutofocus > AUTOFOCUS_TIMEOUT )
						{	// then restart autofocus
							lastAutofocus = currentTime;
							camera.autoFocus( cameraAutoFocusCallback );
						}
						
						// if there is no message about processing image in queue
						if( !handler.hasMessages( R.id.decode ))
						{
							// then register handler on getting image
							camera.setOneShotPreviewCallback(cameraPreviewCallback);
						}
					}
				}
			};
		};
		
		// create and initialize object with native code
		d = new B4HScanner();
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	// restore app's settings

//		Helper.SetContext(this);
    	//Helper.SetSharedPreferences( PreferenceManager.getDefaultSharedPreferences(this));
        
		// change parameters of displayed window
		// app is shown on full screen
		requestWindowFeature( 1 );
		getWindow().addFlags( 1024 );
        
		// bind displaying context
		setContentView( R.layout.scan );
	
		// init local variables for fast access to displayed elements
		//fps_view = (TextView) findViewById(R.id.fps_view);
		//result_view = (TextView) findViewById(R.id.result_view);
		viewfinder_view = (ViewFinder)findViewById( R.id.viewfinder_view );
		rotator = (Rotator) findViewById(R.id.rotate_view);
		
		try { torchControl = new TorchControl(); } 
		catch (Exception e) { torchControl = null; }
				
		buttonTorchOnOff = (Button)this.findViewById(R.id.TorchButton);
		buttonTorchOnOff.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) 
		    {
		    	Message msg = handler.obtainMessage( R.id.TorchOnOff );
				handler.sendMessage( msg );
		    }
		  });
		
		sensorManager = (SensorManager) getSystemService("sensor");

		// init player for sound and vibrate when VIN scan is successful
		initBeep();
    }
    
	// Play sound and vibrate when VIN scan is successful
    public void beepAndVibrate()
    {
    	if( mediaPlayer != null )		// check player
    	{	
    		if( settings[IDX_BEEP] )
    		{
    			mediaPlayer.start();	// play sound
    		}
    	}
    	if( settings[IDX_VIBRATE] )
    	{
    		Vibrator vibr = (Vibrator)getSystemService("vibrator");
    		vibr.vibrate(200);			// vibrate
    	}
    }
    
    // decode obtained image
    private void decode( byte[] data )
    {
    	int w, h;
    	Log.d("tag", "call decode");
    	if( previewing )
    	{
    		if( isScanning() )
    		{	// when program is in preview and scanning, then CallBack is set to receive one frame
    			camera.setOneShotPreviewCallback( cameraPreviewCallback );
    		}
    	}
    	
    	if( data != null )
    	{
    		if( isScanning() )
    		{
        		// there is data on input and program is in scanning mode
    			
    			setScanning(false);				// temporary turn off scanning
    			h = previewSize.height / 3;		// calculate height of image for processing
    			w = previewSize.width;			// get image width for processing

    			// call native procedure to detect VIN
    			String decodedVIN = d.parse( data, h * w, w, h, h >> 6, this);
    			
    			quality = (quality * 3.0 + (double)d.acuracy ) / 4.0; 
    			
    			if( decodedVIN != null )
    			{
    				// VIN code was successfully detected
    				
    				beepAndVibrate();	// play sound and vibrate

    				// show decoded vin and continue scanning
    				vincode = Toast.makeText(Scanner.this.getApplicationContext(), "VIN: " + decodedVIN.subSequence(0, decodedVIN.length()), Toast.LENGTH_LONG);
					vincode.show();
    				
    				// send message to another thread that code is obtained
    				Message msg = handler.obtainMessage( R.id.decoded, decodedVIN );
    				handler.sendMessageDelayed(msg, 4600);
    			}
    			else
    				// restore scanning mode 
    				setScanning(true);		
    		}
    	}
    }
    
    // initialize mediaPlayer and set sound file
    private void initBeep()
    {
    	if( mediaPlayer == null )
    	{
    		setVolumeControlStream( 3 );		  
    		mediaPlayer = new MediaPlayer();	// create mediaPlayer object
    		mediaPlayer.setAudioStreamType( 3 );// set type of played file

    		// played file is in resources
    		Resources res = getResources();		
    		
    		try {
        		AssetFileDescriptor assetFileDescriptor = res.openRawResourceFd( R.raw.scanned ); 
    			// get file descriptor
    			FileDescriptor  fileDescriptor = assetFileDescriptor.getFileDescriptor();
    			
    			// get beginning of file
    			long startOffset = assetFileDescriptor.getStartOffset();
    			
    			// get length of file
    			long lenght = assetFileDescriptor.getLength();
    			
    			// set played data in mediaPlayer
    			mediaPlayer.setDataSource(fileDescriptor, startOffset, lenght);
    			
    			// close descriptor of file
    			assetFileDescriptor.close();
    			
    			// set volume of playback
    			mediaPlayer.setVolume( 1.0f, 1.0f);
    			mediaPlayer.prepare();
			} catch (Exception e) {
				// in case of any error release the object
				Log.i("VinScanner", "Error of media player init");
				mediaPlayer = null;
			}
    	}
    }
    
    // returns SCANNING flag
    private boolean isScanning()
    {
    	return scanning;
    }

    // Set scanning mode
    private void setScanning( boolean scanFlag )
    {
    	scanning = scanFlag;
    
    	// In dependance of transfered parameter, mode of displaying is switched
    	viewfinder_view.setRunning(scanning);
    }

    // Starts Preview 
    void cameraStartPreview( android.view.SurfaceHolder pholder, int width, int height)
    {
    	Log.i("VinScanner", "Camera Start Preview");
    	try
    	{
	    	String str = "flash-value";
	    	
	    	// get object with camera parameters
	    	Camera.Parameters CameraParameters = camera.getParameters();

	    	try
	    	{
	    		List<String> supportedFlash = CameraParameters.getSupportedFlashModes();
	    		if( torchControl == null && !supportedFlash.contains(android.hardware.Camera.Parameters.FLASH_MODE_TORCH) )
	    			throw new Exception();
	    	}
	    	catch (Exception e) {
    			buttonTorchOnOff.setVisibility(4);
			}

	    	// set size of captured image
	    	String s = "Start preview: width= " + width + "height= " + height;
	    	Log.i("VinScanner", s);
	    	
    		List<Size> sizes = CameraParameters.getSupportedPreviewSizes();
    		Size optimalSize = getOptimalPreviewSize(sizes, width, height);
    		CameraParameters.setPreviewSize(optimalSize.width, optimalSize.height);
	    	
	    	// set YUV data format. 
		    CameraParameters.setPreviewFormat( ImageFormat.NV21 );
		    
		    // set frequency of capture
	    	CameraParameters.setPreviewFrameRate( 15 );
	    	
	    	if( CameraParameters.get(str) != null )
	    	{
	    		CameraParameters.set(str, 2);
	    	}

	    	// turn off flash
	    	if( torchControl != null )
	    		torchControl.torch( false );
	    	else
	    		CameraParameters.set("flash-mode", android.hardware.Camera.Parameters.FLASH_MODE_OFF);

	    	// set focus mode
	    	CameraParameters.set("focus-mode", android.hardware.Camera.Parameters.FOCUS_MODE_AUTO);

	    	// refresh camera settings
	    	camera.setParameters(CameraParameters);
	    	
	    	previewSize = CameraParameters.getPreviewSize();

	    	// turn off handler when getting image, as far as image
	    	// should only be get when autofocus 
    		camera.setPreviewCallback( null );	
    		
    		// define where to show preview
    		camera.setPreviewDisplay( pholder );
    		
    		// Starts Preview
    		camera.startPreview();    	
    		previewing = true;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
		}
    }
    
    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		for (Size size : sizes) {
			//Log.i(TAG, String.format("width=%d height=%d", size.width, size.height));
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}
    
    // Stops Preview
    void cameraStopPreview()
    {
    	if( previewing )
    	{
    		previewing = false;					// erase flag
    		camera.setPreviewCallback(null);	// erase all handlers
    		camera.stopPreview();				// stop preview in camera
    	}
    }

    // stub
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) 
    {
    }

    // stub 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	super.onActivityResult(requestCode, resultCode, data);
    } 

    // handler of buttons pushes
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	Log.i("VinScanner", "OnKeyDown");
    	if( keyCode == 80 || keyCode == 27 )
    		return true;
    	if( keyCode == 82 )
    		openOptionsMenu();	// if "Menu" is pushed then open menu
    	else
    		return super.onKeyDown(keyCode, event);		// call of parent handler
    	return true;
    }
    
    // method called on stop/pause (e.g. when launching browser)
    @Override
    protected void onPause() 
    {
    	Log.i("VinScanner", "onPause start"); 
    	
    	previewing = false;		// erase flag for PREVIEW mode
		camera.setPreviewCallback(null);	// erase all handlers
		camera.stopPreview();				// stop preview in camera

    	if( cameraThread != null )
    	{	// if camera object is initialized
    		cameraThread.finish();		// finish camera thread
    		cameraThread = null;		// reset thread object
    		try
    		{
    			Thread.sleep( 200 );		// to avoid thread race
    		}
    		catch (Exception e) {
			}
    	}

    	Log.i("VinScanner", "torch mode control");
    	
    	// turn off torch mode
    	if( torchControl != null )
    		torchControl.torch(false);
    	else
    	{
			try
			{	
				Camera.Parameters CameraParameters = camera.getParameters();
				if( CameraParameters.getFlashMode().compareTo( android.hardware.Camera.Parameters.FLASH_MODE_TORCH ) == 0)
				{
					Log.i("VinScanner", "Torch Off");
					buttonTorchOnOff.setBackgroundDrawable(	getResources().getDrawable( R.drawable.light));
					CameraParameters.setFlashMode( android.hardware.Camera.Parameters.FLASH_MODE_OFF);
				
					// refresh camera settings
					camera.setParameters(CameraParameters);		
				}
			}
			catch (Exception e) 
			{
				Log.e("VinScanner", "torch On/Off Exception", e);
			}
    	}

    	// remove object from schedule
    	handler.removeCallbacks(watchdog);
    	
    	// turn off sensor evnts handler
    	sensorManager.unregisterListener( this );
    	
    	setScanning(false);		// stop scanning mode
    	camera.release();		// release camera for another apps
    	
    	if(vincode!=null)
    		vincode.cancel();
    	
    	// call handler of parent class
    	super.onPause();
    	Log.i("VinScanner", "onPause end"); 
    }
    
    // handler called at start/restore of the app 
    // E.g. when closing browser window or start
    @Override
    protected void onResume() 
    {
    	Log.i("VinScanner", "onResume start");
    	super.onResume();	// call method of parent class
    	getWindow().addFlags(128);
    	
    	Sensor sensor = sensorManager.getDefaultSensor(3);
    	
    	sensorManager.registerListener( this, sensor, 3);

    	// start camera thread
    	cameraLatch = new CountDownLatch(1);
    	cameraThread = new CameraThread( this, null );
    	cameraThread.start();
    	
    	try {
			cameraLatch.await(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		lastAutofocus = 0;			// reset last autofocus time
		previewing = false;			// turn off preview(with camera)
		setScanning(true);			// app goes to scanning mode immediately
		watchdog.run();				// start task on schedule
		
		buttonTorchOnOff.setBackgroundDrawable(	getResources().getDrawable( R.drawable.light));
	
		if( holder == null )
		{
			// if app is just launched
			// get holder to display PREVIEW
			SurfaceView view = (SurfaceView) findViewById( R.id.camera_view );
			SurfaceHolder surfaceHolder = view.getHolder();
			
			// set type of displaying image
			surfaceHolder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
			
			// set event handler
			surfaceHolder.addCallback(this);
		}
		else
		{
			// If app is getting restored
			if( !surfaceChangedDelayed )
			{
				// if there is a delayed event
				surfaceChangedDelayed = false;	// reset flag of delayed event
				
				// restore holder work
				SurfaceView view = (SurfaceView)findViewById( R.id.camera_view );
				surfaceChanged( holder, PixelFormat.OPAQUE, view.getWidth(), view.getHeight());
			}
		}
    	Log.i("VinScanner", "onResume end");
    }
    
    // handler of event of changing sensor location
//    @Override
    public void onSensorChanged(SensorEvent event) 
    {    	
    	rotator.setRotated(true);
    }
 
    // called when creating/restoring app window
//    @Override
    public void surfaceChanged(SurfaceHolder pholder, int format, int width,
    		int height) {
    	
    	if( camera != null )
    	{
    		try
    		{
	    		camera.stopPreview();	// stop camera preview
	    		
	    		// restart camera preview
	    		String s = "width = " + width + " height = " + height;
	    		if( pholder == null )
	    			Log.i("VinScanner", "!!! Pholder = NULL");
	    		Log.i("VinScanner", s);
	    		cameraStartPreview( pholder, width, height);
    		}
    		catch (Exception e) {
				// TODO: handle exception
    			Log.i("VinScanner", "!!!!! Exception !!!!!");
        		surfaceChangedDelayed = true;
			}
    	}
    	else
    	{
    		// if camera object is not created, then processing is delayed
    		surfaceChangedDelayed = true;
    	}
    }
    
    // called at creating app window
    @Override
    public void surfaceCreated(SurfaceHolder pholder) {
    	this.holder = pholder;
    	surfaceChangedDelayed = false;
    }
    
    // called at delete app window
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	holder = null;
    	cameraStopPreview();	// stop preview
    }
}
