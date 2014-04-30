package ar.com.falabella.decomovil.pantallas;

import java.io.File;
import java.io.FileOutputStream;

import ar.com.falabella.decomovil.G;
//import com.facebook.FacebookConnector;
//import com.facebook.SessionEvents;
import ar.com.falabella.decomovil.utils.ImageManager;
import ar.com.falabella.decomovil.utils.MyImageView;

import ar.com.falabella.decomovil.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;

public class VistaFinal extends Activity {
	private Activity thisActivity;
	private BackToCameraAsyncTask at1;
	private CompartirAsyncTask at2;
	private Bitmap fotoFinal;

	/** Called when the thisActivity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.vista_final);
		ImageView preview = (ImageView) findViewById(R.id.fotoFinal);
		System.out.println("FINAL   !!!!!!!!!!!!");
		thisActivity = this;
		at1 = null;

		
		// TEXTO PARA EL t�tulo
		System.out.println("texto t�tulo = " + G.descripcion_producto_elegido);

		// finalizar thisActivity de la c�mara
		if (G.camActivity != null) {
			System.out.println("finalizar c�mara");
			G.camActivity.finish();
			System.out.println("c�mara finalizada");
		}

		/** Movimiento de Preview **/
		LinearLayout lay = (LinearLayout) findViewById(R.id.l2);
		LayoutParams params = (LayoutParams) lay.getLayoutParams();
		//params.topMargin = (int) (G.screenH / 2.5 );
		params.topMargin = (int) (410.0 * G.screenG / 960.0);
		params.leftMargin = (int) (G.screenW / 50);
		lay.setLayoutParams(params);

		/** Escalamiento de Imagen **/

		preview.setScaleType(ScaleType.MATRIX);
		Matrix m = new Matrix();
		m.postScale(0.4f, 0.4f);
		preview.setImageMatrix(m);
		fotoFinal = G.bmp_foto_final;
		preview.setImageBitmap(fotoFinal);
		///////// POPUP CON LA IMAGEN EN GRANDE
		
		preview.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Dialog dialog = new Dialog(thisActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.popupfoto);
				dialog.setCancelable(true);

				// set up image view
				ImageView imageView = (ImageView) dialog.findViewById(R.id.popupImage);
				imageView.setScaleType(ScaleType.CENTER_INSIDE);
				imageView.setImageBitmap(fotoFinal);
				dialog.show();		
			}
		});
		
		
		
		
		/************************* Codificaci�n IMGs *********************************/
		//boton otra
		LinearLayout img_otra = (LinearLayout) findViewById(R.id.fake_otra3);
		Bitmap bmp_otra = BitmapFactory.decodeResource(getResources(), R.drawable.otra);
		//Bitmap bmp_otra_pressed = BitmapFactory.decodeResource(getResources(), R.drawable.otra_pressed);
		MyImageView btn_otra = new MyImageView(this.getApplicationContext(), bmp_otra);
		img_otra.addView(btn_otra);

		//boton volver
		LinearLayout img_volver = (LinearLayout) findViewById(R.id.fake_volver3); 
		Bitmap bmp_volver = BitmapFactory.decodeResource(getResources(), R.drawable.volver);
		//Bitmap bmp_volver_pressed = BitmapFactory.decodeResource(getResources(), R.drawable.volver_pressed);
		MyImageView btn_volver = new MyImageView(this.getApplicationContext(), bmp_volver);
		img_volver.addView(btn_volver);
		
		 
		
		Bitmap bmp_boton = BitmapFactory.decodeResource(getResources(), R.drawable.fb);
		int altura_boton = 92 * G.screenG / 800; //50 
		int ancho_boton = bmp_boton.getWidth() * altura_boton / bmp_boton.getHeight(); //72
				
		//boton facebook
		LinearLayout img_fb = (LinearLayout) findViewById(R.id.imageView1); 
		Bitmap bmp_fb = BitmapFactory.decodeResource(getResources(), R.drawable.fb);
		Bitmap bmp_fb_a = G.getResizedBitmap(bmp_fb, ancho_boton , altura_boton);		
		//Bitmap bmp_fb_pressed = BitmapFactory.decodeResource(getResources(), R.drawable.fb_pressed);
		//Bitmap bmp_fb_pressed_a = G.getResizedBitmap(bmp_fb_pressed, ancho_boton , altura_boton);
		MyImageView btn_fb = new MyImageView(this.getApplicationContext(), bmp_fb_a);
		img_fb.addView(btn_fb);
		//boton email
		LinearLayout img_email = (LinearLayout) findViewById(R.id.imageView2); 
		Bitmap bmp_email = BitmapFactory.decodeResource(getResources(), R.drawable.email);
		Bitmap bmp_email_a = G.getResizedBitmap(bmp_email, ancho_boton , altura_boton);	
		//Bitmap bmp_email_pressed = BitmapFactory.decodeResource(getResources(), R.drawable.email_pressed);
		//Bitmap bmp_email_pressed_a = G.getResizedBitmap(bmp_email_pressed, ancho_boton , altura_boton);	
		MyImageView btn_email = new MyImageView(this.getApplicationContext(), bmp_email_a);
		img_email.addView(btn_email);
		//boton tw
		LinearLayout img_tw = (LinearLayout) findViewById(R.id.imageView3); 
		Bitmap bmp_tw = BitmapFactory.decodeResource(getResources(), R.drawable.tw);
		Bitmap bmp_tw_a = G.getResizedBitmap(bmp_tw, ancho_boton , altura_boton);	
		//Bitmap bmp_tw_pressed = BitmapFactory.decodeResource(getResources(), R.drawable.tw_pressed);
		//Bitmap bmp_tw_pressed_a = G.getResizedBitmap(bmp_tw_pressed, ancho_boton , altura_boton);	
		MyImageView btn_tw = new MyImageView(this.getApplicationContext(), bmp_tw_a);
		img_tw.addView(btn_tw);
		//boton comprar
		LinearLayout img_comprar = (LinearLayout) findViewById(R.id.imageView4); 
		Bitmap bmp_comprar = BitmapFactory.decodeResource(getResources(), R.drawable.comprar);
		Bitmap bmp_comprar_a = G.getResizedBitmap(bmp_comprar, ancho_boton , altura_boton);	
		//Bitmap bmp_comprar_pressed = BitmapFactory.decodeResource(getResources(), R.drawable.comprar_pressed);
		//Bitmap bmp_comprar_pressed_a = G.getResizedBitmap(bmp_comprar_pressed, ancho_boton , altura_boton);	
		MyImageView btn_comprar = new MyImageView(this.getApplicationContext(), bmp_comprar_a);
		img_comprar.addView(btn_comprar);
		/************************** Fin configuraci�n de Imagenes ******************************/
			
		
		
		/** Movement and Rezise TextView Titulo Categoria **/
		// ---------MOVEMENT TITULO CATEGORIA (TEXTO)---------
		LinearLayout lTxt = (LinearLayout) findViewById(R.id.lTxt2);
		LayoutParams params_txt = (LayoutParams) lTxt.getLayoutParams();
		params_txt.topMargin = (int) (G.screenG/3);
		params_txt.leftMargin = (int) (G.screenW/32);
		lTxt.setLayoutParams(params_txt);

		TextView tv = (TextView) findViewById(R.id.txt_titulo);
		Typeface tf = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/VistaSanSCBol.ttf");
		tv.setTypeface(tf);
		
		/** Movimiento Boton FAKE Volver (IMG) **/
		LinearLayout fake_volver = (LinearLayout) findViewById(R.id.fake_volver2);
		LayoutParams params_fake_volver = (LayoutParams) fake_volver.getLayoutParams();
		params_fake_volver.topMargin = (int) (G.screenG/3.2);
		params_fake_volver.rightMargin = (int) (G.screenW/64);
		fake_volver.setLayoutParams(params_fake_volver);
		
		/** Movimiento Boton  Volver (IMG) **/
		LinearLayout volver = (LinearLayout) findViewById(R.id.Volver2);
		LayoutParams params_volver = (LayoutParams) volver.getLayoutParams();
		params_volver.topMargin = (int) (G.screenG/3.2);
		params_volver.rightMargin = (int) (G.screenW/64);
		volver.setLayoutParams(params_volver);		
		
		/** Movimiento Boton FAKE Otra (IMG)**/
		LinearLayout fake_otra = (LinearLayout) findViewById(R.id.fake_otra2);
		LayoutParams params_fake_otra = (LayoutParams) fake_otra.getLayoutParams();
		params_fake_otra.topMargin = (int) (G.screenG/3.2);
		params_fake_otra.rightMargin = (int) (G.screenW/4);
		fake_otra.setLayoutParams(params_fake_otra);
		
		/** Movimiento Boton Otra (IMG)**/
		LinearLayout otra = (LinearLayout) findViewById(R.id.Otra2);
		LayoutParams params_otra = (LayoutParams) otra.getLayoutParams();
		params_otra.topMargin = (int) (G.screenG/3.2);
		params_otra.rightMargin = (int) (G.screenW/4);
		otra.setLayoutParams(params_otra);
		
		/** Movimiento Txt Descripcion Producto **/
		LinearLayout desc_prod = (LinearLayout) findViewById(R.id.lDesc2);
		LayoutParams params_prod = (LayoutParams) desc_prod.getLayoutParams();
		params_prod.bottomMargin = (int) (G.screenG/8.5);
	    params_prod.leftMargin = (int) (G.screenW/32);
		desc_prod.setLayoutParams(params_prod);	
		
		//Configurando txtView footer descripcin
		
		TextView desc = (TextView) findViewById(R.id.txt_descripcion_producto);
		desc.setText(G.descripcion_producto_elegido);
		
		
		
		/**************************** Botones Pantalla ****************************/
		
		//Invisibility
		//btn_tw.setVisibility(View.GONE);
		//btn_email.setVisibility(View.GONE);
		//btn_fb.setVisibility(View.GONE);
		
		//boton twitter
		btn_tw.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					if (at2 == null) {
						at2 = new CompartirAsyncTask(thisActivity);
					}
					if (!at2.isRunning()) {
						at2.execute("twitter");
					} else {
						System.out.println("ya est� corriendo");
					}
					//G.urlFoto = G.ConexionWS(G.base + "guardarFotoGenerada.php", "Id_movil=" + G.cod + "&ID_producto=" + G.id_producto_elegido + "&tipo_compartir=twitter", G.bmp_foto_final, "imagen");
 
					// CONEXION A TWITTER
					String url = G.base + "compartirTwitterAndroid.php?url=" + G.urlFoto;
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					startActivity(intent);
					break;
				}
				return false;
			}
		});

		//boton email
		btn_email.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					if (at2 == null) {
						at2 = new CompartirAsyncTask(thisActivity);
					}
					if (!at2.isRunning()) {
						at2.execute("mail");
					} else {
						System.out.println("ya est� corriendo");
					}
					//G.urlFoto = G.ConexionWS(G.base + "guardarFotoGenerada.php", "Id_movil=" + G.cod + "&ID_producto=" + G.id_producto_elegido + "&tipo_compartir=mail", G.bmp_foto_final, "imagen");

					break;
				}
				return false;
			}
		});

		//boton facebook
		btn_fb.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:

					 /**CONEXION A FACEBOOK**/

					// /////// iniciar el thread que trabaja con FB, haciendo login primero
					// si fuera necesario
		/*			G.facebookConnector.setActivityContext(VistaFinal.this, getApplicationContext());
					if (G.facebookConnector.getFacebook().isSessionValid()) {
						funcionFacebook();
					} else {
						System.out.println("-- sin sesi�n, loguear");
						SessionEvents.AuthListener listener = new SessionEvents.AuthListener() {
							@Override
							public void onAuthSucceed() {
								funcionFacebook();
							}

							@Override
							public void onAuthFail(String error) {
								Toast.makeText(VistaFinal.this, "Facebook no conectado", Toast.LENGTH_LONG).show();
							}
						};
						SessionEvents.addAuthListener(listener);
						G.facebookConnector.login();
					}
*/
					break;
				}
				return false;
			}
		});
		
		

		//boton otra
		btn_otra.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					if (at1 == null) {
						at1 = new BackToCameraAsyncTask(thisActivity);
					}
					if (!at1.isRunning()) {
						at1.execute();
					} else {
						System.out.println("ya est� corriendo");
					}
					/*Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setClassName(thisActivity, Camara.class.getName());
					thisActivity.startActivity(intent);*/
					break;
				}
				return false;
			}
		});

		//boton volver
		btn_volver.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setClassName(thisActivity, Categorias.class.getName());
					thisActivity.startActivity(intent);
					break;
				}
				return false;
			}
		});
		
		
		//boton detalle
		btn_comprar.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
				String url = G.link_falabella;				
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
					break;
				}
				return false;
			}
		});
		/*********************************** boo ya ! *****************************************/

		// FACEBOOK
	//	G.facebookConnector = new FacebookConnector(G.FACEBOOK_APPID, this, getApplicationContext(), new String[] { G.FACEBOOK_PERMISSION });

		// subir foto a hidra
		//String resWS;
		// resWS=G.ConexionWS("http://hidra.advante.cl/mobile/falabella_hot/WS/guardarFotoGenerada.php", "Id_movil=" + G.cod + "&ID_producto=" + G.id_producto_elegido, G.bmp_foto_final);
		//System.out.println("res WS = " + resWS);
	}

	private void funcionFacebook() {
		if (at2 == null) {
			at2 = new CompartirAsyncTask(thisActivity);
		}
		if (!at2.isRunning()) {
			at2.execute("facebook");
		} else {
			System.out.println("ya est� corriendo");
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	//	G.facebookConnector.getFacebook().authorizeCallback(requestCode, resultCode, data);
	}

	///////////////////////////// asynctask precargar foto antes de volver a la c�mara ////////////////////////
	private class BackToCameraAsyncTask extends AsyncTask<Integer, Integer, String> { // <params, progress, result>
		private boolean running;
		private ProgressDialog dialog;
		private Activity act;
		private String id_producto;

		@Override
		protected void onPreExecute() {
			// antes de llamar a doInBackground
			System.out.println("Comenzado a ejecutar AsyncTask");
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface arg0) {
					////////////////////////////////////////////////
					// Cancelar el progressDialog ==> cancelar todo
					System.out.println("Dialog: cancelado");
					if (dialog.isShowing()) {
						dialog.dismiss();
					}
					cancel(true); // detener la AsyncTask
				}
			});
			dialog.setMessage("Descargando Imagen");
			dialog.show();
		}

		@Override
		protected String doInBackground(Integer... args) {
			// ejecuci�n en hebra
			running = true;

			/////////////////////////////////////////////////
			// C�digo que se ejecuta en background
			String response = "";

			// descargar la foto grande
			String url = "";
			int i; 
			for (i = 0; i < G.arr_productos_id.length; i++) {
				if (G.arr_productos_id[i].equals(G.id_producto_elegido)) {
					url = G.arr_productos_url_grande[i];
					id_producto = G.arr_productos_id[i];
				}
			}
			// descargar la imagen
			//Bitmap bmp = ImageManager.getInstance(act).preloadImage(url, act); // descarga

			////////////////
			// revisar si lo cancelaron, NO QUITAR
			if (!running) {
				return response;
			}

			return response;
		}

		// -- called from the publish progress
		// -- notice that the datatype of the second param gets passed to this method
		// -- DESDE AC� SE PUEDEN LLAMAR A ELEMENTOS DE LA INTERFAZ GR�FICA
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			//System.out.println("onProgressUpdate(): " + String.valueOf(values[0]) +"/"+String.valueOf(values[1]));
			//dialog.setMax(values[1]);
			//dialog.setProgress(values[0]);
			//dialog.setMessage(String.valueOf(values[0]+" / "+ values[1]));
		}

		// -- called if the cancel button is pressed
		// -- ACTUALIZAR LA PANATALLA CON INFORMACI�N DE QUE CANCELARON
		@Override
		protected void onCancelled() {
			super.onCancelled();
			running = false;
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			System.out.println("CANCELADO");
			at1 = null;
		}

		// al finalizar
		@Override
		protected void onPostExecute(String result) {
			System.out.println("terminado: " + result);
			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			// ir a la c�mara
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClassName(thisActivity, Camara.class.getName());
			thisActivity.startActivity(intent);
			at1 = null;

		}

		public boolean isRunning() {
			return running;
		}

		public BackToCameraAsyncTask(Activity a) {
			super();
			act = a;
			running = false;
			dialog = new ProgressDialog(a);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// OPCIONAL: personalizar la barra de progreso v�a XML
			//dialog.setProgressDrawable(a.getResources().getDrawable(R.drawable.my_progress));
		}
	}

	///////////////////////////// asynctask subir foto a facebook  ////////////////////////
	private class CompartirAsyncTask extends AsyncTask<String, Integer, String> { // <params, progress, result>
		private boolean running;
		private ProgressDialog dialog;
		private Activity act;
		private String tipo_compartir;
		private Bitmap fotoFinal;

		@Override
		protected void onPreExecute() {
			// antes de llamar a doInBackground
			System.out.println("Comenzado a ejecutar AsyncTask");
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface arg0) {
					////////////////////////////////////////////////
					// Cancelar el progressDialog ==> cancelar todo
					System.out.println("Dialog: cancelado");
					if (dialog.isShowing()) {
						dialog.dismiss();
					}
					cancel(true); // detener la AsyncTask
				}
			});
			dialog.setMessage("Preparando Imagen...");
			dialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			// ejecuci�n en hebra
			running = true;
			tipo_compartir = args[0];
			/////////////////////////////////////////////////
			// C�digo que se ejecuta en background
			System.out.println("compartir por " + tipo_compartir);
			String response = "";

			if (tipo_compartir.equals("facebook")) {
			//	G.uid = G.facebookConnector.getUidFb();
			//	G.token = G.facebookConnector.getFacebook().getAccessToken();
			//	System.out.println("Token: " + G.token);
			} else {
				G.uid = "No Facebook";
				G.token = "NoFacebook";
			}
			response = G.ConexionWS(G.base + "guardarFotoGenerada.php", "Id_movil=" + G.cod + "&ID_producto=" + G.id_producto_elegido + "&token=" + G.token + "&tipo_compartir=" + tipo_compartir + "&color=" + G.color, G.bmp_foto_final, "imagen");
			System.out.println("RespuestaWS:" + response + "-FinRespuesta");

			if (tipo_compartir.equals("mail"))
				fotoFinal = ImageManager.getInstance(act).preloadImage(response, act);
			else
				fotoFinal = null;

			return response;
		}

		// -- called from the publish progress
		// -- notice that the datatype of the second param gets passed to this method
		// -- DESDE AC� SE PUEDEN LLAMAR A ELEMENTOS DE LA INTERFAZ GR�FICA
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			//System.out.println("onProgressUpdate(): " + String.valueOf(values[0]) +"/"+String.valueOf(values[1]));
			//dialog.setMax(values[1]);
			//dialog.setProgress(values[0]);

		}

		// -- called if the cancel button is pressed
		// -- ACTUALIZAR LA PANATALLA CON INFORMACI�N DE QUE CANCELARON
		@Override
		protected void onCancelled() {
			super.onCancelled();
			running = false;
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			System.out.println("CANCELADO");
			at2 = null;
		}

		// al finalizar
		@Override
		protected void onPostExecute(String result) {
			System.out.println("terminado: " + result);
			G.urlFoto = result;
			System.out.println("termiando compartir por " + tipo_compartir);
			if (tipo_compartir.equals("facebook")) {
				Toast.makeText(act, "Imagen publicada en Facebook", Toast.LENGTH_LONG).show();
			}

			if (tipo_compartir.equals("mail")) {
				String arr_response[] = null;
				String title = "";
				String body = "";
				//String link = "";
				String responseMail = G.ConexionWS(G.base + "getInfoMail.php", "Id_movil=" + G.cod + "&ID_producto=" + G.id_producto_elegido + "&pais=" + G.pais_elegido);
				arr_response = responseMail.split("&");
				title = arr_response[0];
				body = arr_response[1].replaceAll("<br>", "\n");
				//link = arr_response[2];

				//cacheDir is file path to your application's cache directory where image is located.
				//You can provide your own file object by replacing object f.
				File cacheDir = ImageManager.getInstance(act.getApplicationContext()).getCacheDir();
				File f = new File(cacheDir, "decomovil_falabella.jpg");
				FileOutputStream out = null;
				try {
					out = new FileOutputStream(f);
					fotoFinal.compress(Bitmap.CompressFormat.JPEG, 80, out);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (out != null)
							out.close();
					} catch (Exception ex) {
					}
				}

				Intent intent = new Intent(Intent.ACTION_SEND);
				//intent.setType("image/jpeg");
				intent.setType("application/octet-stream");
				intent.putExtra(Intent.EXTRA_TEXT, body);
				intent.putExtra(Intent.EXTRA_SUBJECT, title);
				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
				startActivity(Intent.createChooser(intent, "Compartir Imagen:"));

				/*
				//enviar mail
				//String archiveFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + G.nameFoto;
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("application/octet-stream");
				//i.setType("text/plain"); //use this line for testing in the emulator  
				//i.setType("message/rfc822") ; // use from live device
				i.putExtra(Intent.EXTRA_SUBJECT, "Falabella Hot 2012");//File root = Environment.getExternalStorageDirectory();
				//File file = new File(root, G.pathFoto);
				//Uri uri = Uri.fromFile(new File(root, G.nameFoto));
				Uri screenshotUri = Uri.parse(G.urlFoto);
				i.putExtra(Intent.EXTRA_STREAM, screenshotUri);
				i.setType("image/jpg");
				i.putExtra(Intent.EXTRA_TEXT, "Te env�o esta foto desde mi dispositivo Android, donde me pruebo una prenda de la nueva colecc�n Falabella Hot 2012");
				i = Intent.createChooser(i, "Seleccione aplicaci�n de correo");
				//startActivity(Intent.createChooser(i, "Seleccione aplicaci�n de correo."));
				*/
			}

			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			at2 = null;
		}

		public boolean isRunning() {
			return running;
		}

		public CompartirAsyncTask(Activity a) {
			super();
			act = a;
			running = false;
			dialog = new ProgressDialog(a);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// OPCIONAL: personalizar la barra de progreso v�a XML
			//dialog.setProgressDrawable(a.getResources().getDrawable(R.drawable.my_progress));
		}
	}
}
