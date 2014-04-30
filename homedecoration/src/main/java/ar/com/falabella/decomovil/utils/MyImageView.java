package ar.com.falabella.decomovil.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.widget.ImageView;

////////////////////////////////////////////////////////////////////////////////
// imageview con imagen para normal y presionado
public class MyImageView extends ImageView {
	Bitmap bmp_normal, bmp_hover;
	String TAG = "MyImageView";

	/**
	 * Crear MyImageView usando dos bitmaps (uno para normal, otro para presionado)
	 * @param context thisActivity.getApplicationContext()
	 * @param bmp1 Bitmap para normal
	 * @param bmp2 Bitmap para presionado
	 */
	public MyImageView(Context context, Bitmap bmp1, Bitmap bmp2) {
		super(context);
		bmp_normal = bmp1;
		bmp_hover = bmp2;
		this.setImageBitmap(bmp_normal);
	}

	/**
	 * Crear MyImageView usando un bitmap (el otro se crea en el constructor)
	 * @param context thisActivity.getApplicationContext()
	 * @param bmp1 Bitmap para normal
	 */
	public MyImageView(Context context, Bitmap bmp1) {
		super(context);
		bmp_normal = bmp1;
		bmp_hover = bmp1; // por mientras
		//Log.d(TAG, "bitmap hover...");
		new ImageTask().execute(bmp_normal); // generar imagen "hover" y asignar
		this.setImageBitmap(bmp_normal);
	}

	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			this.setImageBitmap(bmp_hover);
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			this.setImageBitmap(bmp_normal);
		}
		return true;
	}

	// fuente: http://xjaphx.wordpress.com/2011/06/22/image-processing-brightness-over-image/
	private Bitmap doBrightness(Bitmap src, int value) {
		// image size
		int width = src.getWidth();
		int height = src.getHeight();
		// create output bitmap
		Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
		// color information
		int A, R, G, B;
		int pixel;

		// scan through all pixels
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				// get pixel color
				pixel = src.getPixel(x, y);
				A = Color.alpha(pixel);
				R = Color.red(pixel);
				G = Color.green(pixel);
				B = Color.blue(pixel);

				// increase/decrease each channel
				R += value;
				if (R > 255) {
					R = 255;
				} else if (R < 0) {
					R = 0;
				}

				G += value;
				if (G > 255) {
					G = 255;
				} else if (G < 0) {
					G = 0;
				}

				B += value;
				if (B > 255) {
					B = 255;
				} else if (B < 0) {
					B = 0;
				}

				// apply new pixel color to output bitmap
				bmOut.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}

		// return final image
		return bmOut;
	}

	private class ImageTask extends AsyncTask<Bitmap, Void, Bitmap> {
		protected Bitmap doInBackground(Bitmap... args) {
			return doBrightness(args[0], -80);
		}

		protected void onPostExecute(Bitmap result) {
			//Log.d(TAG, "bitmap hover listo");
			bmp_hover = result;
		}
	}

}
