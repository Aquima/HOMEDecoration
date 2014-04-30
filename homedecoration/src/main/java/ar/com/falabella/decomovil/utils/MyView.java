package ar.com.falabella.decomovil.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.View;

public class MyView extends View {
	private PointF p11, p12;
	private PointF p21, p22;

	public MyView(Context context) {
		super(context);
		p11 = new PointF(0, 0);
		p12 = new PointF(0, 0);
		p21 = new PointF(0, 0);
		p22 = new PointF(0, 0);
		
		
	}

	public void updatePunto1(float x1, float y1, float x2, float y2) {
		p11.x = x1;
		p11.y = y1;
		p12.x = x2;
		p12.y = y2;
	}
	
	public void updatePunto2(float x1, float y1, float x2, float y2) {
		p21.x = x1;
		p21.y = y1;
		p22.x = x2;
		p22.y = y2;
	}

	protected void onDraw(Canvas canvas) {
		Paint p = new Paint();
		p.setColor(Color.RED);

		canvas.drawLine(p11.x, p11.y, p12.x, p12.y, p);
		canvas.drawLine(p21.x, p21.y, p22.x, p22.y, p);

	}

}
