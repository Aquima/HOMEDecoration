package ar.com.falabella.decomovil.pantallas;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import ar.com.falabella.decomovil.G;
import ar.com.falabella.decomovil.utils.CameraPreview;
import ar.com.falabella.decomovil.utils.ImageManager;
import ar.com.falabella.decomovil.utils.MyImageView;
import ar.com.falabella.decomovil.utils.MyView;

import ar.com.falabella.decomovil.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore.Images;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.util.FloatMath;
import android.util.Log;

import android.view.MotionEvent;
import android.view.View.OnTouchListener;

public class Camara extends Activity implements OnTouchListener {
	private Activity thisActivity;
	private Camera mCamera;
	private CameraPreview mPreview;
	private int picW;
	private int picH;
	private boolean sacandoFoto;
	private GuardarFotoAsyncTask at1;
	private ProgressBar spinner;
	private boolean clickLock;
	private boolean hasAutoFocus;

	ReceiveMessages myReceiver = null;
	private boolean myReceiverIsRegistered = false;
	public String tagFotoPrenda = "";
	private boolean intentRecibido = false;

	private final String TAG = "TOUCH";

	// These matrices will be used to move and zoom image
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private Matrix preMatrix = new Matrix();
	private PointF start = new PointF();
	private PointF mid = new PointF();
	private float oldDist = 1.0f;
	private float scale = 1f;
	private float scale_acum = 1f;
	private float x, y, h, w, h0, w0;
	private float zmin, zmax;
	private float[] valores = new float[9];

	// We can be in one of these 3 states
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private int mode = NONE;
	private ImageView view = null;

	// rotación
	private float angulo = 0f;
	private float wa = 0;
	private float ha = 0;
	private float xr1 = 0;
	private float yr1 = 0;
	private float xr2 = 0;
	private float yr2 = 0;
	private float xs1 = 0;
	private float ys1 = 0;
	private float xs2 = 0;
	private float ys2 = 0;

	// estado botones
	private boolean btn_zmas;
	private boolean btn_zmenos;
	private boolean btn_rmas;
	private boolean btn_rmenos;
	private boolean btn_fizq;
	private boolean btn_fder;

	private MyView debugView;

	@Override
	public void onResume() {
		super.onResume();
		if (!myReceiverIsRegistered) {
			System.out.println("REGISTRANDO INTENT RECEIVER (resume)");
			registerReceiver(myReceiver, new IntentFilter("foto_cargada"));
			myReceiverIsRegistered = true;
			intentRecibido = false;

			// prenda (acá para que se registre el intent primero)

			// Bitmap bmpPrenda =
			// ImageManager.getInstance(thisActivity.getApplicationContext()).preloadImage(urlProducto,
			// thisActivity);
			G.posicion=0;
			System.out.println("HAY "+G.url_producto.length+" COLORES");
			tagFotoPrenda=G.url_producto[G.posicion];
			prenda.setTag(G.url_producto[G.posicion]);
			System.out.println("lalalallaa");
			ImageManager.getInstance(thisActivity.getApplicationContext()).displayImage(G.url_producto[G.posicion], thisActivity, prenda);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (myReceiverIsRegistered) {
			unregisterReceiver(myReceiver);
			myReceiverIsRegistered = false;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camara);

		int i;
		// String urlProducto = "";

		thisActivity = this;
		G.camActivity = this;
		sacandoFoto = false;

		spinner = (ProgressBar) findViewById(R.id.spinner);
		spinner.setVisibility(ProgressBar.INVISIBLE);
		clickLock = false;
		myReceiver = new ReceiveMessages();

		// si no hay cámara, pa la casa
		if (!checkCameraHardware(this.getApplicationContext())) {
			Toast.makeText(thisActivity.getApplicationContext(), "No hay cámara", Toast.LENGTH_LONG).show();
			return;
		}

		// Create an instance of Camera
		mCamera = getCameraInstance();
		if (mCamera == null) {
			Toast.makeText(thisActivity.getApplicationContext(), "Cámara no disponible", Toast.LENGTH_LONG).show();
			return;
		}

		// revisar autofocus
		hasAutoFocus = checkCameraHardwareAF(thisActivity.getApplicationContext());

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.frame_camera_preview);
		preview.addView(mPreview);

		picW = mPreview.getWidth();
		picH = mPreview.getHeight();
		System.out.println("foto de " + picW + "x" + picH);

		// view debug
		debugView = new MyView(this.getApplicationContext());
		FrameLayout f = (FrameLayout) findViewById(R.id.contenedor);
		f.addView(debugView);

		// buscar prenda
		for (i = 0; i < G.arr_productos_id.length; i++) {
			if (G.arr_productos_id[i].equals(G.id_producto_elegido)) {
				tagFotoPrenda = G.arr_productos_url_grande[i];
			}
		}
		prenda = (ImageView) findViewById(R.id.img_prenda);
		// en el onResume se carga la imagen

		// System.out.println("Descargar " + urlProducto);
		// prenda.setImageBitmap(bmpPrenda);
		prenda.setOnTouchListener(this);
		view = (ImageView) prenda;
		x = 0;
		y = 0;

		// por ahora 100, después se reemplaza por el tamaño del bitmap
		w0 = 100; // bmpPrenda.getWidth();
		h0 = 100; // bmpPrenda.getHeight();
		w = w0;
		h = h0;
		// System.out.println(w+"x"+h);

		mid.x = w0 / 2;
		mid.y = h0 / 2;
		marcar(mid.x, mid.y);

		// botón tomar foto
		Bitmap bmp_tomar_foto = BitmapFactory.decodeResource(getResources(), R.drawable.tomar_foto);
		Bitmap bmp_tomar_foto_pressed = BitmapFactory.decodeResource(getResources(), R.drawable.tomar_foto_pressed);
		MyImageView btn_tomar_foto = new MyImageView(this.getApplicationContext(), bmp_tomar_foto, bmp_tomar_foto_pressed);
		//MyImageView btn_tomarfoto = new MyImageView(this.getApplicationContext(), bmp_tomar_foto);

		// botones zoom
		Bitmap bmp_zoomas = BitmapFactory.decodeResource(getResources(), R.drawable.zmas);
		Bitmap bmp_zoomas_pressed = BitmapFactory.decodeResource(getResources(), R.drawable.zmas_pressed);
		MyImageView btn_zoommas = new MyImageView(this.getApplicationContext(), bmp_zoomas, bmp_zoomas_pressed);

				
		Bitmap bmp_zoomenos = BitmapFactory.decodeResource(getResources(), R.drawable.zmenos);
		Bitmap bmp_zoomenos_pressed = BitmapFactory.decodeResource(getResources(), R.drawable.zmenos_pressed);
		MyImageView btn_zoommenos = new MyImageView(this.getApplicationContext(), bmp_zoomenos, bmp_zoomenos_pressed);
		
		btn_zmas = false; // no están presioandos
		btn_zmenos = false;
		
		

		// botones rotación
		
		Bitmap bmp_rotmas = BitmapFactory.decodeResource(getResources(), R.drawable.rmas);
		Bitmap bmp_rotmas_pressed = BitmapFactory.decodeResource(getResources(), R.drawable.rmas_pressed);
		MyImageView btn_rotmas = new MyImageView(this.getApplicationContext(), bmp_rotmas, bmp_rotmas_pressed);
		
		Bitmap bmp_rotmenos = BitmapFactory.decodeResource(getResources(), R.drawable.rmenos);
		Bitmap bmp_rotmenos_pressed = BitmapFactory.decodeResource(getResources(), R.drawable.rmenos_pressed);
		MyImageView btn_rotmenos = new MyImageView(this.getApplicationContext(), bmp_rotmenos, bmp_rotmenos_pressed);
		
		btn_rmas = false; // no están presioandos
		btn_rmenos = false;
		

		// botones flechas 
		
		Bitmap bmp_flecha_izq = BitmapFactory.decodeResource(getResources(), R.drawable.flecha_iz);
		Bitmap bmp_flecha_izq_pressed = BitmapFactory.decodeResource(getResources(), R.drawable.flecha_iz_pressed);
		MyImageView btn_flecha_izq = new MyImageView(this.getApplicationContext(), bmp_flecha_izq, bmp_flecha_izq_pressed);
		
		
		Bitmap bmp_flecha_der = BitmapFactory.decodeResource(getResources(), R.drawable.flecha_der);
		Bitmap bmp_flecha_der_pressed = BitmapFactory.decodeResource(getResources(), R.drawable.flecha_der_pressed);		
		MyImageView btn_flecha_der = new MyImageView(this.getApplicationContext(), bmp_flecha_der, bmp_flecha_der_pressed);
		
		btn_fizq = false; // no están presioandos
		btn_fder = false;

		LinearLayout layC1 = (LinearLayout) findViewById(R.id.imageView1);
		LinearLayout layC2 = (LinearLayout) findViewById(R.id.imageView2);
		LinearLayout layC3 = (LinearLayout) findViewById(R.id.imageView3);
		LinearLayout layC4 = (LinearLayout) findViewById(R.id.imageView4);
		LinearLayout layC5 = (LinearLayout) findViewById(R.id.imageView5);
		LinearLayout layC6 = (LinearLayout) findViewById(R.id.flecha_izq);
		LinearLayout layC7 = (LinearLayout) findViewById(R.id.flecha_der);
		layC7.addView(btn_flecha_der);
		layC6.addView(btn_flecha_izq);
		layC5.addView(btn_tomar_foto);
		layC4.addView(btn_zoommenos);
		layC3.addView(btn_zoommas);
		layC2.addView(btn_rotmenos);
		layC1.addView(btn_rotmas);

		/**
		 * layC1.setVisibility(View.GONE); layC2.setVisibility(View.GONE);
		 * layC3.setVisibility(View.GONE); layC4.setVisibility(View.GONE);
		 * layC5.setVisibility(View.GONE);
		 **/

		control.start();

		btn_tomar_foto.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					// enfocar y tomar foto
					if (!clickLock) {
						if (!sacandoFoto) {
							sacandoFoto = true;
							clickLock = true;
							if (hasAutoFocus) {
								// enfocar y tomar foto
								mCamera.autoFocus(new AutoFocusCallback() {
									@Override
									public void onAutoFocus(boolean success, Camera camera) {
										System.out.println("Enfoque :" + success);
										mCamera.takePicture(null, null, mPicture);
										sacandoFoto = false;
									}
								});
							} else {
								// tomar foto (sin enfocar)
								System.out.println("Tomar foto SIN autofocus");
								mCamera.takePicture(null, null, mPicture);
								sacandoFoto = false;
							}
						}
					}
					break;

				}
				return false;
			}
		});
		btn_zoommenos.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					if (!clickLock) {
						btn_zmenos = true;
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					// System.out.println("dejar de alejar");
					btn_zmenos = false;
					break;
				}
				return false;
			}
		});
		btn_zoommas.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					// System.out.println("acercar x = "+(x+w/2)+"    y = "+(y+h/2));
					if (!clickLock) {
						btn_zmas = true;
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					// System.out.println("dejar de acercar");
					btn_zmas = false;
					break;
				}
				return false;
			}
		});
		btn_rotmas.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					// System.out.println("acercar x = "+(x+w/2)+"    y = "+(y+h/2));
					if (!clickLock) {
						btn_rmas = true;
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					btn_rmas = false;
					break;
				}
				return false;
			}
		});
		btn_rotmenos.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					if (!clickLock) {
						btn_rmenos = true;
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					btn_rmenos = false;
					break;
				}
				return false;
			}
		});

		// FLECHAS
		if (G.url_producto.length<2){
			// ocultar flechas si hay un solo producto
			btn_flecha_izq.setVisibility(ImageView.INVISIBLE);
			btn_flecha_der.setVisibility(ImageView.INVISIBLE);
		}else{
			btn_flecha_izq.setVisibility(ImageView.VISIBLE);
			btn_flecha_der.setVisibility(ImageView.VISIBLE);
		}
		btn_flecha_izq.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("CLICK FLECHA IZQUIERDA U MAD BRO??");
				if (G.posicion > 0) {
					G.color = String.valueOf(G.posicion - 1);
					//intentRecibido = false;
					tagFotoPrenda=G.url_producto[G.posicion - 1];
					prenda.setTag(G.url_producto[G.posicion - 1]);
					ImageManager.getInstance(thisActivity.getApplicationContext()).displayImage(G.url_producto[G.posicion - 1], thisActivity, prenda);
					G.posicion = G.posicion - 1;
				}
			}
		});

		btn_flecha_der.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("CLICK FLECHA DERECHO U MAD BRO??");
				if (G.posicion < G.url.length - 1) {
					G.color = String.valueOf(G.posicion + 1);
					//intentRecibido = false;
					tagFotoPrenda = G.url_producto[G.posicion + 1];
					prenda.setTag(G.url_producto[G.posicion + 1]);
					ImageManager.getInstance(thisActivity.getApplicationContext()).displayImage(G.url_producto[G.posicion + 1], thisActivity, prenda);
					G.posicion = G.posicion + 1;
				}

			}
		});

	}

	// //////////////////// onTOuchListener
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		// Dump touch event to log
		// dumpEvent(event);
		float px, py;
		float alfa;

		preMatrix.set(matrix); // salvar en caso de rechazar el movimiento
		// Handle touch events here...
		if (!clickLock) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				savedMatrix.set(matrix);
				start.set(event.getX(), event.getY());
				// Log.d(TAG, "mode=DRAG");
				xr1 = event.getX(0); // posición incial dedo 1
				yr1 = event.getY(0);
				mode = DRAG;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				oldDist = spacing(event);
				// Log.d(TAG, "oldDist=" + oldDist);
				if (oldDist > 10f) {
					savedMatrix.set(matrix);
					midPoint(mid, event);
					marcar(mid.x, mid.y);
					xr2 = event.getX(1); // posición inicial dedo 2
					yr2 = event.getY(1);
					mode = ZOOM;
					// Log.d(TAG, "mode=ZOOM");
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				// Log.d(TAG, "mode=NONE");
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG) {
					// ...
					matrix.set(savedMatrix);
					matrix.postTranslate(event.getX(0) - start.x, event.getY(0) - start.y);

					matrix.getValues(valores);
					x = valores[2];
					y = valores[5];
					w = w0 * scale_acum;
					h = h0 * scale_acum;
					float r = angulo * 3.141592654f / 180;
					wa = (float) (Math.abs(w * Math.cos(r)) + Math.abs(h * Math.sin(r)));
					ha = (float) (Math.abs(w * Math.sin(r)) + Math.abs(h * Math.cos(r)));
					switch (regionAngulo(angulo)) {
					case 1:
						mid.set(x - (float) Math.abs(h * Math.sin(r)) + wa / 2, y + ha / 2);
						break;
					case 2:
						mid.set(x - wa / 2, (float) (y - Math.abs(h * Math.cos(r)) + ha / 2));
						break;
					case 3:
						mid.set(x - (float) Math.abs(w * Math.cos(r - 3.141592654f)) + wa / 2, y - ha / 2);
						break;
					case 4:
						mid.set(x + wa / 2, (float) (y - Math.abs(w * Math.sin(r)) + ha / 2));
						break;
					default:
						System.out.println("REGIÓN NO VÁLIDA");
						break;
					}

					marcar(mid.x, mid.y);
					// System.out.println("x, y, w, h, w0, h0, scale_acum=" + x
					// + " " + y + " " + w + " " + h + " " + w0 + " " + h0 + " "
					// + scale_acum);
					// System.out.println("x, y =" + x + " " + y);

				} else if (mode == ZOOM) {
					// //////////// MULTITOUCH //////////////////
					if (G.multitouch) {
						float newDist = spacing(event);
						// Log.d(TAG, "newDist=" + newDist);
						if (newDist > 10f) {
							// System.out.println("newDist = " + newDist);
							matrix.set(savedMatrix);
							scale = newDist / oldDist;
							w = w0 * scale;
							h = h0 * scale;
							scale_acum = scale;

							// rotación
							xs1 = event.getX(0);
							ys1 = event.getY(0);
							xs2 = event.getX(1);
							ys2 = event.getY(1);
							// float mr = (yr2 - yr1) / (xr2 - xr1);
							// float ms = (ys2 - ys1) / (xs2 - xs1);
							debugView.updatePunto1(xr1, yr1, xr2, yr2);
							debugView.updatePunto2(xs1, ys1, xs2, ys2);

							// punto medio
							px = (xs2 + xs1) / 2; // (mr*xr1 - yr1 - ms*xs1 +
													// ys1)/(mr-ms);
							py = (ys2 + ys1) / 2; // (ys1 - ms*xs1 + ms*(mr*xr1
													// - yr1 - ms*xs1 +
													// ys1)/(mr-ms));
							mid.set(px, py);

							// float angulo_delta = (float)
							// (Math.atan(Math.abs((ms - mr) / (1 + mr * ms))));
							System.out.println("scale_acum=" + scale_acum + "  ángulo = " + angulo); // +
																										// "   angulo_delta = "+angulo_delta);

							alfa = (float) Math.atan((ys2 - ys1) / (xs2 - xs1));
							angulo = alfa * 180f / 3.141592654f;
							// System.out.println("wa = "+wa+"  ha = "+ha);

							// float alfa = angulo - 45 * 3.141592654f / 180f;
							// float c =
							// (float)Math.sqrt((w/2)*(w/2)+(h/2)*(h/2));
							// px += c*Math.cos(alfa);
							// py += c*Math.sin(alfa);
							// mid.set(px, py);

							matrix.set(new Matrix());
							// px = (x + w*Math.abs((float)Math.cos(alfa)) +
							// h*Math.abs((float)Math.sin(alfa)));
							// py = (y + w*Math.abs((float)Math.sin(alfa)) +
							// h*Math.abs((float)Math.cos(alfa)));
							marcar(mid.x, mid.y);

							matrix.postTranslate(mid.x, mid.y);
							// matrix.postTranslate(mid.x+c*(float)Math.cos(alfa),
							// mid.y + c*(float)Math.sin(alfa));
							matrix.postRotate(angulo, mid.x, mid.y);
							matrix.postScale(scale, scale, mid.x, mid.y);

						}
					}
				}
				break;
			}
			// System.out.print("MID x, y =" + mid.x + " " + mid.y);

			// aceptar o rechazar el movimiento en base a la nueva posición
			if ((mid.x > 0) && (mid.x < G.screenG) && (mid.y > 0) && (mid.y < G.screenC)) {
				// System.out.println(" mover");
			} else {
				matrix.set(preMatrix);
				// System.out.println(" NO");
			}
			view.setImageMatrix(matrix);
		}
		return true; // indicate event was handled
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if (at1 == null) {
				at1 = new GuardarFotoAsyncTask(thisActivity);
			}
			if (!at1.isRunning()) {
				at1.execute(data);
			} else {
				System.out.println("ya está corriendo");
			}
		}
	};

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Deco_fotos");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	// auxiliares
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			return null;
		}
		return c; // returns null if camera is unavailable
	}

	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	/** Check if this device has autofocus */
	private boolean checkCameraHardwareAF(Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	private void marcar(float x, float y) {
		// System.out.println("punto en " + x + "," + y);
		/*
		 * ImageView img = (ImageView) findViewById(R.id.img_punto);
		 * 
		 * img.setScaleType(ScaleType.MATRIX); Matrix m = new Matrix();
		 * m.postTranslate(x, y); img.setImageMatrix(m);
		 */
	}

	/**
	 * 
	 * @param g
	 *            grados en sexagesimal (-inf, +inf)
	 * @return cuadrante del plano: 1, 2, 3, 4
	 */
	private int regionAngulo(float g) {
		int grados = (int) g;
		if (grados < 0) {
			grados = 360 - Math.abs(grados) % 360;
		} else {
			grados = grados % 360;
		}
		return (int) (Math.floor((grados % 360) / 90) + 1);
	}

	// recibir intent con datos de la imagen cargada (desde el ImageManager)
	private class ReceiveMessages extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intentRecibido) {
				String action = intent.getAction();
				Bundle extras = intent.getExtras();
				System.out.println("recibido intent = " + action);
				if (action.equals("foto_cargada")) {
					if (extras != null) {
						System.out.println("recibido intent = " + extras.getString("tag"));
						if (extras.getString("tag").equals(tagFotoPrenda)) {
							intentRecibido = true;
							w0 = extras.getInt("w");
							h0 = extras.getInt("h");

							ajustarFotoInicial(w0, h0);

						}
					}
				}
			}
		}
	}

	private void ajustarFotoInicial(float w0, float h0) {
		matrix = new Matrix();

		w = w0;
		h = h0;
		System.out.println("AJUSTANDO FOTO A "+w+"x"+h);

		// posición inicial
		int x_ini = (int) (G.screenG / 2 - w / 2);
		int y_ini = (int) (G.screenC / 2 - h / 2);
		matrix.postTranslate(x_ini, y_ini);
		matrix.getValues(valores);
		x = valores[2];
		y = valores[5];
		mid.x = G.screenG / 2;
		mid.y = G.screenC / 2;

		// límites
		zmin = 0.05f;
		zmax = 3.0f;

		// zoom inicial
		scale = G.screenC / h0;
		matrix.postScale(scale, scale, mid.x, mid.y);
		matrix.getValues(valores);
		x = valores[2];
		y = valores[5];
		scale_acum *= scale;

		// rotación inicial
		angulo -= 90;
		matrix.postRotate(-90, mid.x, mid.y);
		matrix.getValues(valores);
		x = valores[2];
		y = valores[5];
		w = w0 * scale_acum;
		h = h0 * scale_acum;

		float r = angulo * 3.141592654f / 180;
		wa = (float) (Math.abs(w * Math.cos(r)) + Math.abs(h * Math.sin(r)));
		ha = (float) (Math.abs(w * Math.sin(r)) + Math.abs(h * Math.cos(r)));
		switch (regionAngulo(angulo)) {
		case 1:
			mid.set(x - (float) Math.abs(h * Math.sin(r)) + wa / 2, y + ha / 2);
			break;
		case 2:
			mid.set(x - wa / 2, (float) (y - Math.abs(h * Math.cos(r)) + ha / 2));
			break;
		case 3:
			mid.set(x - (float) Math.abs(w * Math.cos(r - 3.141592654f)) + wa / 2, y - ha / 2);
			break;
		case 4:
			mid.set(x + wa / 2, (float) (y - Math.abs(w * Math.sin(r)) + ha / 2));
			break;
		default:
			System.out.println("REGIÓN NO VÁLIDA");
			break;
		}

		marcar(mid.x, mid.y);
		view.setImageMatrix(matrix);
	}

	// control botones zoom y rotación
	private Handler handler = new Handler();
	private Thread control = new Thread() {
		public void run() {
			while (true) {
				if (btn_rmenos) {
					// System.out.println("rot menos");

					angulo -= 5;
					matrix.postRotate(-5, mid.x, mid.y);
					matrix.getValues(valores);
					x = valores[2];
					y = valores[5];
					w = w0 * scale_acum;
					h = h0 * scale_acum;

					float r = angulo * 3.141592654f / 180;
					wa = (float) (Math.abs(w * Math.cos(r)) + Math.abs(h * Math.sin(r)));
					ha = (float) (Math.abs(w * Math.sin(r)) + Math.abs(h * Math.cos(r)));
					switch (regionAngulo(angulo)) {
					case 1:
						mid.set(x - (float) Math.abs(h * Math.sin(r)) + wa / 2, y + ha / 2);
						break;
					case 2:
						mid.set(x - wa / 2, (float) (y - Math.abs(h * Math.cos(r)) + ha / 2));
						break;
					case 3:
						mid.set(x - (float) Math.abs(w * Math.cos(r - 3.141592654f)) + wa / 2, y - ha / 2);
						break;
					case 4:
						mid.set(x + wa / 2, (float) (y - Math.abs(w * Math.sin(r)) + ha / 2));
						break;
					default:
						System.out.println("REGIÓN NO VÁLIDA");
						break;
					}

				}
				if (btn_rmas) {
					// System.out.println("rot más");
					angulo += 5;
					matrix.postRotate(5, mid.x, mid.y);
					matrix.getValues(valores);
					x = valores[2];
					y = valores[5];
					w = w0 * scale_acum;
					h = h0 * scale_acum;

					float r = angulo * 3.141592654f / 180;
					wa = (float) (Math.abs(w * Math.cos(r)) + Math.abs(h * Math.sin(r)));
					ha = (float) (Math.abs(w * Math.sin(r)) + Math.abs(h * Math.cos(r)));
					switch (regionAngulo(angulo)) {
					case 1:
						mid.set(x - (float) Math.abs(h * Math.sin(r)) + wa / 2, y + ha / 2);
						break;
					case 2:
						mid.set(x - wa / 2, (float) (y - Math.abs(h * Math.cos(r)) + ha / 2));
						break;
					case 3:
						mid.set(x - (float) Math.abs(w * Math.cos(r - 3.141592654f)) + wa / 2, y - ha / 2);
						break;
					case 4:
						mid.set(x + wa / 2, (float) (y - Math.abs(w * Math.sin(r)) + ha / 2));
						break;
					default:
						System.out.println("REGIÓN NO VÁLIDA");
						break;
					}

				}
				if (btn_zmenos) {
					// System.out.println("zoom menos");
					// System.out.println("alejar x = "+(x+w/2)+"    y = "+(y+h/2));
					if (scale_acum * (1 / 1.1f) > zmin) {
						scale = 1 / 1.1f;
						matrix.postScale(scale, scale, mid.x, mid.y);
						matrix.getValues(valores);
						x = valores[2];
						y = valores[5];
						scale_acum *= scale;
						w = w0 * scale_acum;
						h = h0 * scale_acum;
						float r = angulo * 3.141592654f / 180;
						wa = (float) (Math.abs(w * Math.cos(r)) + Math.abs(h * Math.sin(r)));
						ha = (float) (Math.abs(w * Math.sin(r)) + Math.abs(h * Math.cos(r)));
						switch (regionAngulo(angulo)) {
						case 1:
							mid.set(x - (float) Math.abs(h * Math.sin(r)) + wa / 2, y + ha / 2);
							break;
						case 2:
							mid.set(x - wa / 2, (float) (y - Math.abs(h * Math.cos(r)) + ha / 2));
							break;
						case 3:
							mid.set(x - (float) Math.abs(w * Math.cos(r - 3.141592654f)) + wa / 2, y - ha / 2);
							break;
						case 4:
							mid.set(x + wa / 2, (float) (y - Math.abs(w * Math.sin(r)) + ha / 2));
							break;
						default:
							System.out.println("REGIÓN NO VÁLIDA");
							break;
						}
					}
				}
				if (btn_zmas) {
					// System.out.println("zoom más");
					if (scale_acum * 1.1f < zmax) {
						scale = 1.1f;
						matrix.postScale(scale, scale, mid.x, mid.y);
						matrix.getValues(valores);
						x = valores[2];
						y = valores[5];
						scale_acum *= scale;
						w = w0 * scale_acum;
						h = h0 * scale_acum;

						float r = angulo * 3.141592654f / 180;
						wa = (float) (Math.abs(w * Math.cos(r)) + Math.abs(h * Math.sin(r)));
						ha = (float) (Math.abs(w * Math.sin(r)) + Math.abs(h * Math.cos(r)));
						switch (regionAngulo(angulo)) {
						case 1:
							mid.set(x - (float) Math.abs(h * Math.sin(r)) + wa / 2, y + ha / 2);
							break;
						case 2:
							mid.set(x - wa / 2, (float) (y - Math.abs(h * Math.cos(r)) + ha / 2));
							break;
						case 3:
							mid.set(x - (float) Math.abs(w * Math.cos(r - 3.141592654f)) + wa / 2, y - ha / 2);
							break;
						case 4:
							mid.set(x + wa / 2, (float) (y - Math.abs(w * Math.sin(r)) + ha / 2));
							break;
						default:
							System.out.println("REGIÓN NO VÁLIDA");
							break;
						}
					}

				}
				handler.post(new Runnable() {
					@Override
					public void run() {
						marcar(mid.x, mid.y);
						view.setImageMatrix(matrix);
					}
				});
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	};
	private ImageView prenda;

	private class GuardarFotoAsyncTask extends AsyncTask<byte[], Integer, String> { // <params, progress, result>
		private boolean running;
		// private ProgressDialog dialog;
		private Activity act;

		@Override
		protected void onPreExecute() {
			// antes de llamar a doInBackground
			System.out.println("Comenzado a ejecutar AsyncTask");
			/*
			 * dialog.setOnCancelListener(new OnCancelListener() {
			 * 
			 * @Override public void onCancel(DialogInterface arg0) {
			 * //////////////////////////////////////////////// // Cancelar el
			 * progressDialog ==> cancelar todo
			 * System.out.println("Dialog: cancelado"); if (dialog.isShowing())
			 * { dialog.dismiss(); } cancel(true); // detener la AsyncTask } });
			 * dialog.setMessage(" "); dialog.show();
			 */
			spinner.setVisibility(ProgressBar.VISIBLE);
			clickLock = true;
		}

		@Override
		protected String doInBackground(byte[]... args) {
			// ejecución en hebra
			running = true;

			// ///////////////////////////////////////////////
			// Código que se ejecuta en background
			byte[] data = args[0];
			System.out.println("------------- GUARDANDO IMAGEN " + data.length + " ------------");
			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Log.d(TAG, "Error creating media file, check storage permissions");
				Toast.makeText(thisActivity.getApplicationContext(), "Error creating media file, check storage permissions", Toast.LENGTH_LONG).show();
				return "";
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);

				// redimensionar foto en bm2
				Bitmap bm2_pre = BitmapFactory.decodeByteArray(data, 0, data.length);

				Bitmap bm2 = G.getResizedBitmap(bm2_pre, G.screenG, G.screenC);
				bm2_pre.recycle(); // liberar el bitmap gigante
				bm2_pre = null;

				// obtener prenda en bm1
				BitmapDrawable drawable1 = (BitmapDrawable) view.getDrawable();
				Bitmap bm1_pre = drawable1.getBitmap();
				Bitmap bm1 = bm1_pre; // getResizedBitmap(bm1_pre, _ch, _cw);

				// copia mutable
				Bitmap bm3_pre = bm2.copy(bm2.getConfig(), true);
				Bitmap bm3 = bm3_pre; // getResizedBitmap(bm3_pre,
										// DecoGlobals.screenW,
										// DecoGlobals.screenH);
				Canvas comboImage = new Canvas(bm3);
				comboImage.drawBitmap(bm1, matrix, null);
				// bm3 is now a composite of the two.

				// rotar ANTES DE GUARDAR
				Matrix m = new Matrix();
				m.postRotate(90);
				Bitmap rotatedBitmap = Bitmap.createBitmap(bm3, 0, 0, bm3.getWidth(), bm3.getHeight(), m, true);

				// crear byte[]
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				rotatedBitmap.compress(CompressFormat.JPEG, 80 /*
																 * ignored for
																 * PNG
																 */, bos);
				byte[] bitmapdata = bos.toByteArray();

				G.bmp_foto_final = rotatedBitmap;

				// escribir
				// G.pathFoto = Images.Media.insertImage(getContentResolver(),
				// G.bmp_foto_final, "imagen", null);
				System.out.println("guardados " + G.bmp_foto_final.getWidth() + "x" + G.bmp_foto_final.getHeight() + " en " + G.pathFoto);
				fos.write(bitmapdata);
				fos.close();

				return pictureFile.getAbsolutePath();

			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
				G.bmp_foto_final = null;
				return "ERROR FILE NOT FOUND";
			} catch (Exception e) {
				Log.d(TAG, "Error : " + e.getMessage());
				G.bmp_foto_final = null;
				return "ERROR";
			}

		}

		// -- called from the publish progress
		// -- notice that the datatype of the second param gets passed to this
		// method
		// -- DESDE ACÁ SE PUEDEN LLAMAR A ELEMENTOS DE LA INTERFAZ GRÁFICA
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			// System.out.println("onProgressUpdate(): " +
			// String.valueOf(values[0]) +"/"+String.valueOf(values[1]));
			// dialog.setMax(values[1]);
			// dialog.setProgress(values[0]);
			// dialog.setMessage(String.valueOf(values[0]+" / "+ values[1]));
		}

		// -- called if the cancel button is pressed
		// -- ACTUALIZAR LA PANATALLA CON INFORMACIÓN DE QUE CANCELARON
		@Override
		protected void onCancelled() {
			super.onCancelled();
			running = false;
			spinner.setVisibility(ProgressBar.INVISIBLE);
			clickLock = false;
			/*
			 * if (dialog.isShowing()) { dialog.dismiss(); }
			 */
			System.out.println("CANCELADO");
			at1 = null;
		}

		// al finalizar
		@Override
		protected void onPostExecute(String result) {
			System.out.println("terminado: " + result);

			G.pathFoto = result;
			if (result.startsWith("ERROR")) {
				Toast.makeText(thisActivity.getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(thisActivity.getApplicationContext(), "Imagen guardada en " + result, Toast.LENGTH_LONG).show();
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setClassName(thisActivity, VistaFinal.class.getName());
				thisActivity.startActivity(intent);
				System.out.println("------------- FIN GUARDANDO IMAGEN ------------");
			}

			/*
			 * if (dialog.isShowing()) { dialog.dismiss(); }
			 */
			spinner.setVisibility(ProgressBar.INVISIBLE);
			clickLock = false;
			at1 = null;
		}

		public boolean isRunning() {
			return running;
		}

		public GuardarFotoAsyncTask(Activity a) {
			super();
			act = a;
			running = false;
			// dialog = new ProgressDialog(a);
			// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// OPCIONAL: personalizar la barra de progreso vía XML
			// dialog.setProgressDrawable(a.getResources().getDrawable(R.drawable.progress));
		}
	}
}
