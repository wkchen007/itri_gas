package com.itripatch.itri_gas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WaveformView extends SurfaceView implements SurfaceHolder.Callback{
	private static final String TAG = "WaveformView";
	private SurfaceHolder surfaceHolder = getHolder();
	private Canvas canvas = null;
	private Canvas bmpcanvas = null;
	private Bitmap threeAxisBmp = null;
	private Paint bigcell = null, xAccPaint = null, yAccPaint = null, zAccPaint = null;
	private Paint xGPaint = null, yGPaint = null, zGPaint = null;
	private Paint xMPaint = null, yMPaint = null, zMPaint = null;
	private int densityDpi = 0, CanvasSizeX = 0, CanvasSizeY = 0, bigcell_x = 0, bigcell_y = 0, zero_y = 0, rawdata_count = 0;
	private int pre_x = 0, cur_x = 0;
	private int pre_xg = 0, cur_xg = 0;
	private int pre_xm = 0, cur_xm = 0;
//	private int pre_xacc_y = 0, pre_yacc_y = 0, pre_zacc_y = 0, cur_xacc_y = 0, cur_yacc_y = 0, cur_zacc_y = 0;
	private float pre_xacc_y = 0, pre_yacc_y = 0, pre_zacc_y = 0, cur_xacc_y = 0, cur_yacc_y = 0, cur_zacc_y = 0;
	private float pre_xg_y = 0, pre_yg_y = 0, pre_zg_y = 0, cur_xg_y = 0, cur_yg_y = 0, cur_zg_y = 0;
	private float pre_xm_y = 0, pre_ym_y = 0, pre_zm_y = 0, cur_xm_y = 0, cur_ym_y = 0, cur_zm_y = 0;
	public Bundle saveDataBundle;

	public WaveformView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
		surfaceHolder.addCallback(this);
		setKeepScreenOn(true);
		setFocusable(true);		
	}

	private void initPaint() {
		bigcell = new Paint();
		bigcell.setAntiAlias(true);
		bigcell.setColor(Color.LTGRAY);
		bigcell.setStrokeWidth(3);

		xAccPaint = new Paint();
		xAccPaint.setAntiAlias(true);
		xAccPaint.setColor(Color.BLUE);
		xAccPaint.setStrokeWidth(4);

		yAccPaint = new Paint();
		yAccPaint.setAntiAlias(true);
		yAccPaint.setColor(Color.YELLOW);
		yAccPaint.setStrokeWidth(4);

		zAccPaint = new Paint();
		zAccPaint.setAntiAlias(true);
		zAccPaint.setColor(Color.GREEN);
		zAccPaint.setStrokeWidth(4);

		xGPaint = new Paint();
		xGPaint.setAntiAlias(true);
		xGPaint.setColor(Color.CYAN);
		xGPaint.setStrokeWidth(4);

		yGPaint = new Paint();
		yGPaint.setAntiAlias(true);
		yGPaint.setColor(Color.RED);
		yGPaint.setStrokeWidth(4);

		zGPaint = new Paint();
		zGPaint.setAntiAlias(true);
		zGPaint.setColor(Color.BLACK);
		zGPaint.setStrokeWidth(4);

		xMPaint = new Paint();
		xMPaint.setAntiAlias(true);
		xMPaint.setColor(Color.GRAY);
		xMPaint.setStrokeWidth(4);

		yMPaint = new Paint();
		yMPaint.setAntiAlias(true);
		yMPaint.setColor(Color.MAGENTA);
		yMPaint.setStrokeWidth(4);

		zMPaint = new Paint();
		zMPaint.setAntiAlias(true);
		zMPaint.setColor(Color.DKGRAY);
		zMPaint.setStrokeWidth(4);



	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		rawdata_count = 0;
		getDisplayInfo();	
		threeAxisBmp = Bitmap.createBitmap(CanvasSizeX, CanvasSizeY, Bitmap.Config.ARGB_8888);
		bmpcanvas = new Canvas(threeAxisBmp);
	}

	private void getDisplayInfo() {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		densityDpi = metrics.densityDpi ;
		bigcell_x = (densityDpi * 25 / 25) / 5;
		bigcell_y = (densityDpi * 25 / 25) / 5;
		CanvasSizeX = this.getWidth();
		CanvasSizeY = this.getHeight();
		zero_y = CanvasSizeY / 2;
	}

	public void drawAxis(boolean lockNeeded) throws InterruptedException {
		int uY = 0;
		int dY = 0;
		double df = 0.0;

		if (lockNeeded) canvas = surfaceHolder.lockCanvas();
		bmpcanvas.drawColor(Color.rgb(255, 250, 250));

		// draw Y axis
		uY = zero_y;
		dY = zero_y;
		df = (double) (bigcell_y) / 5.0;
		while (true) {
			// Big cell
			bmpcanvas.drawLine(0, uY - 1, CanvasSizeX, uY - 1, bigcell);
			bmpcanvas.drawLine(0, dY - 1, CanvasSizeX, dY - 1, bigcell);

			uY = uY - bigcell_y;
			dY = dY + bigcell_y; 	
			if (dY > CanvasSizeY)
				break;
		}

		// draw X axis
		dY = 1;
		df = (double) (bigcell_x) / 5.0;
		while (true) {
			// big cell
			bmpcanvas.drawLine(dY - 1, 0, dY - 1, CanvasSizeY, bigcell);

			dY = dY + bigcell_y;
			if (dY > CanvasSizeX) break;
		}

		pre_x = 0;
		pre_xacc_y = zero_y + 210;
		pre_yacc_y = zero_y + 210;
		pre_zacc_y = zero_y + 210;

		pre_xg_y = zero_y + 210;
		pre_yg_y = zero_y + 210;
		pre_zg_y = zero_y + 210;

		pre_xm_y = zero_y + 210;
		pre_ym_y = zero_y + 210;
		pre_zm_y = zero_y + 210;

		if (lockNeeded) {
			canvas.drawBitmap(threeAxisBmp, 0.0f, 0.0f, null);
			surfaceHolder.unlockCanvasAndPost(canvas);
		}
	}
	
//	public void drawACC(int xaccdata, int yaccdata, int zaccdata) throws InterruptedException {
	public void drawACC(float xaccdata, float yaccdata, float zaccdata, float xgdata, float ygdata, float zgdata,
						float xmdata, float ymdata, float zmdata, boolean [] isLine) throws InterruptedException {
		canvas = surfaceHolder.lockCanvas();
		cur_x = rawdata_count;
		cur_xg = rawdata_count;
		cur_xm = rawdata_count;

		xaccdata /= 1;
		yaccdata /= 1;
		zaccdata /= 1;

		xgdata /= 1;
		ygdata /= 1;
		zgdata /= 1;

		xmdata /= 1;
		ymdata /= 1;
		zmdata /= 1;
/*
		cur_xacc_y = (xaccdata*CanvasSizeY+40000)/80000; 
		cur_yacc_y = (yaccdata*CanvasSizeY+40000)/80000; 
		cur_zacc_y = (zaccdata*CanvasSizeY+40000)/80000;


*/
/*
		float temp_x, temp_y, temp_z;
		float conv_real_x, conv_real_y, conv_real_z;

		temp_x = xaccdata;
		temp_y = yaccdata;
		temp_z = zaccdata;

		conv_real_x = (((temp_x-40000)/10000)*40) +zero_y;
		conv_real_y = (((temp_y-40000)/10000)*40) +zero_y;
		conv_real_z = (((temp_z-40000)/10000)*40) +zero_y;

*/


		cur_xacc_y = (xaccdata*-40) + zero_y;   	// *-40是因為drawLine是paint.mNativePaint, 所以需要再乘以
		cur_yacc_y = (yaccdata*-40) + zero_y; 	// *-40是因為drawLine是paint.mNativePaint, 所以需要再乘以負值
		cur_zacc_y = (zaccdata*-40) + zero_y; 	// *-40是因為drawLine是paint.mNativePaint, 所以需要再乘以負值

		cur_xg_y = (xgdata*-40) + zero_y;  	// *-40是因為drawLine是paint.mNativePaint, 所以需要再乘以
		cur_yg_y = (ygdata*-40) + zero_y;	// *-40是因為drawLine是paint.mNativePaint, 所以需要再乘以負值
		cur_zg_y = (zgdata*-40) + zero_y;	// *-40是因為drawLine是paint.mNativePaint, 所以需要再乘以負值

		cur_xm_y = (xmdata*-40) + zero_y;  	// *-40是因為drawLine是paint.mNativePaint, 所以需要再乘以
		cur_ym_y = (ymdata*-40) + zero_y;	// *-40是因為drawLine是paint.mNativePaint, 所以需要再乘以負值
		cur_zm_y = (zmdata*-40) + zero_y;	// *-40是因為drawLine是paint.mNativePaint, 所以需要再乘以負值

//		cur_xacc_y = (2*40) + zero_y;

/*


		cur_xacc_y = ((xaccdata-40000)/10000)*(CanvasSizeY/80000);
		cur_yacc_y = ((yaccdata-40000)/10000)*(CanvasSizeY/80000);
		cur_zacc_y = ((zaccdata-40000)/10000)*(CanvasSizeY/80000);

*/
/*
		bmpcanvas.drawLine(pre_x, pre_xacc_y, cur_x, cur_xacc_y, xAccPaint);
		bmpcanvas.drawLine(pre_x, pre_yacc_y, cur_x, cur_yacc_y, yAccPaint);
		bmpcanvas.drawLine(pre_x, pre_zacc_y, cur_x, cur_zacc_y, zAccPaint);

		pre_x = cur_x;
		pre_xacc_y = cur_xacc_y;
		pre_yacc_y = cur_yacc_y;
		pre_zacc_y = cur_zacc_y;
*/

		if(isLine[0])
			bmpcanvas.drawLine(pre_x, pre_xacc_y, cur_x, cur_xacc_y, xAccPaint);
		if(isLine[1])
			bmpcanvas.drawLine(pre_x, pre_yacc_y, cur_x, cur_yacc_y, yAccPaint);
		if(isLine[2])
			bmpcanvas.drawLine(pre_x, pre_zacc_y, cur_x, cur_zacc_y, zAccPaint);

		if(isLine[3])
			bmpcanvas.drawLine(pre_xg, pre_xg_y, cur_xg, cur_xg_y, xGPaint);
		if(isLine[4])
			bmpcanvas.drawLine(pre_xg, pre_yg_y, cur_xg, cur_yg_y, yGPaint);
		if(isLine[5])
			bmpcanvas.drawLine(pre_xg, pre_zg_y, cur_xg, cur_zg_y, zGPaint);

		if(isLine[6])
			bmpcanvas.drawLine(pre_xm, pre_xm_y, cur_xm, cur_xm_y, xMPaint);
		if(isLine[7])
			bmpcanvas.drawLine(pre_xm, pre_ym_y, cur_xm, cur_ym_y, yMPaint);
		if(isLine[8])
			bmpcanvas.drawLine(pre_xm, pre_zm_y, cur_xm, cur_zm_y, zMPaint);

		pre_x = cur_x;
		pre_xacc_y = cur_xacc_y;
		pre_yacc_y = cur_yacc_y;
		pre_zacc_y = cur_zacc_y;

		pre_xg = cur_xg;
		pre_xg_y = cur_xg_y;
		pre_yg_y = cur_yg_y;
		pre_zg_y = cur_zg_y;

		pre_xm = cur_xm;
		pre_xm_y = cur_xm_y;
		pre_ym_y = cur_ym_y;
		pre_zm_y = cur_zm_y;

//		rawdata_count++;
		rawdata_count += 5;  //線的兩點距離


		if (rawdata_count >= CanvasSizeX) {
			rawdata_count = 0;
			pre_x = 0;
			pre_xacc_y = zero_y +210;
			pre_yacc_y = zero_y +210;
			pre_zacc_y = zero_y +210;

			pre_xg = 0;
			pre_xg_y = zero_y +210;
			pre_yg_y = zero_y +210;
			pre_zg_y = zero_y +210;

			pre_xm = 0;
			pre_xm_y = zero_y + 210;
			pre_ym_y = zero_y + 210;
			pre_zm_y = zero_y + 210;

			drawAxis(false);	
		}

		if (canvas != null) {
			canvas.drawBitmap(threeAxisBmp, 0.0f, 0.0f, null); 
			surfaceHolder.unlockCanvasAndPost(canvas);
		}
	}//end of drawACC

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
		try {
			drawAxis(true);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
	}
}
