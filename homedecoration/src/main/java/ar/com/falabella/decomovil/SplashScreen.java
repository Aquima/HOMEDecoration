package ar.com.falabella.decomovil;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ar.com.falabella.decomovil.pantallas.*;
import ar.com.falabella.decomovil.utils.ImageManager;

import ar.com.falabella.decomovil.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.os.AsyncTask;

public class SplashScreen extends Activity {
	private Activity thisActivity;
	private ConectarAsyncTask at1 = null;
	private ActualizarAsyncTask at2 = null;
	private String mensaje;
	private ProgressBar spinner;

	//private SgteVezAsyncTask at3 = null;

	/** Called when the thisActivity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.splash_screen);
		thisActivity = this;

		G.multitouch = false; // desactivar multitouch

		spinner = (ProgressBar) findViewById(R.id.spinner1);
		spinner.setVisibility(ProgressBar.INVISIBLE);

		// PANTALLA
		Display display = getWindowManager().getDefaultDisplay();
		G.screenW = display.getWidth();
		G.screenH = display.getHeight();
		if (G.screenW > G.screenH) {
			G.screenG = G.screenW;
			G.screenC = G.screenH;
		} else {
			G.screenG = G.screenH;
			G.screenC = G.screenW;
		}

		// MODELO DEL TELÉFONO
		Build b = new Build();
		G.modelo = b.MANUFACTURER + "-" + b.DEVICE;

		// SECUENCIA DE INICIALIZACIÓN

		// Existe código aleatorio?
		G.cod = G.leerTxt("deco_appuid", thisActivity.getApplicationContext());
		if (G.cod.equalsIgnoreCase("") || (G.cod == null)) {
			// no existe
			System.out.println("(B)");
			G.cod = G.generarCodigoAleatorio();
			// obligar a elegir país también (instalación nueva o no se alcanzó a grabar)
			G.grabarTxt("deco_pais", "", thisActivity.getApplicationContext());
		}

		// ahora sí existe (si no había, sólo existe en RAM), continuar. al final se guarda en servidor (si ya estaba el guardar no hace nada)

		// existe país?
		G.pais_elegido = G.leerTxt("deco_pais", thisActivity.getApplicationContext());
		if (!G.pais_elegido.equals("")) {
			// sí existe 
			System.out.println("(C)");
			inicioSiguienteVez(); // (**)
		} else {
			// no existe
			System.out.println("(D)");
			// hay internet?
			if (G.isNetworkAvailable(thisActivity)) {
				// internet OK
				System.out.println("(E)");

				// hay un solo país
				System.out.println("(H)");
				G.pais_elegido = "ar";
				conectar(G.pais_elegido);

			} else {
				// no hay internet
				System.out.println("(F)");
				mensaje = "No hay conexión a Internet";
				threadMensaje.start(); //mensaje("No hay conexión a Internet");
				// salir
			}
		}
	}

	/* ************* METODOS ************ */
	public void inicioSiguienteVez() {
		Thread con1 = new Thread() {
			public void run() {
				Looper.prepare();
				inicioSiguienteVez_bg();
			}
		};
		spinner.setVisibility(ProgressBar.VISIBLE);
		con1.start();
		//inicioSiguienteVez_bg();
	}

	// se llama desde SgteVezAsyncTask
	public void inicioSiguienteVez_bg() {
		System.out.println("(**)");

		G.fechaUpdated = G.leerTxt("deco_fecha_updated", thisActivity.getApplicationContext());
		if (G.fechaUpdated.equals("") || (G.fechaUpdated == null)) {
			// no existe fecha de actualización
			System.out.println("(J)");
			actualizar();
		} else {
			//fecha de actualización conocida
			System.out.println("(K)");
			G.strCatalogo = G.leerTxt("deco_catalogo", thisActivity.getApplicationContext());
			if (!G.strCatalogo.equals("") && (G.strCatalogo != null)) {
				// sí existe deco_catalogo
				System.out.println("(L)");

				if (G.isNetworkAvailable(thisActivity)) {
					// hay internet
					System.out.println("(N)");

					// consultar fecha último update
					G.fechaUpdatedSrv = G.ConexionWS(G.base + "getFechaActualizacion.php", "Pais_origen=" + G.pais_elegido + "&Id_movil=" + G.cod);
					if (G.fechaUpdatedSrv.startsWith("ERROR")) {
						System.out.println("(N2)");
						avanzarPantalla();
					} else {

						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date fechaSrv = null;
						Date fechaCel = null;
						try {
							fechaSrv = df.parse(G.fechaUpdatedSrv);
							fechaCel = df.parse(G.fechaUpdated);
						} catch (ParseException e) {
							e.printStackTrace();
							// si hay algún error, forzar a que actualice
							fechaSrv = new Date((System.currentTimeMillis() / 1000L) + 1000);
							fechaCel = new Date((System.currentTimeMillis() / 1000L));
						}
						System.out.println("servidor: " + G.fechaUpdatedSrv);
						System.out.println("celular: " + G.fechaUpdated);

						// ver si está actualizado
						if (fechaSrv.after(fechaCel)) {
							// hay actualizaciones, actualizar
							System.out.println("(P)");
							actualizar();
						} else {
							// estamos al día
							System.out.println("(Q)");
							avanzarPantalla();
						}
					}
				} else {
					// no hay internet
					System.out.println("(O)");
					avanzarPantalla();
				}

			} else {
				// no existe deco_catalogo
				System.out.println("(M)");
				actualizar();
			}
		}
	}

	public void avanzarPantalla() {
		int i;
		String[] t;
		System.out.println("LEER TXT's, GENERAR DATOS GLOBALES Y AVANZAR A PANTALLA 2");
		String result;

		// si no hay internet y no existe alguno de los archivos, no se puede continuar
		boolean ok = true;
		if (!G.isNetworkAvailable(thisActivity)) {
			String r1 = null;
			String r2 = null;
			String r3 = null;
			String r4 = null;
			r1 = G.leerTxt("deco_appuid", thisActivity.getApplicationContext());
			r2 = G.leerTxt("deco_pais", thisActivity.getApplicationContext());
			r3 = G.leerTxt("deco_catalogo", thisActivity.getApplicationContext());
			r4 = G.leerTxt("deco_fecha_updated", thisActivity.getApplicationContext());

			if ((r1 == null) || (r1.equals("")) || (r2 == null) || (r2.equals("")) || (r3 == null) || (r3.equals("")) || (r4 == null) || (r4.equals(""))) {
				// no hay internet y no están los archivos. No se puede continuar
				ok = false;
			}
		}
		if (!ok) {
			mensaje = "Se necesita conexión a internet para continuar";
			threadMensaje.start();
			return;
		}

		result = G.leerTxt("deco_catalogo", thisActivity.getApplicationContext());

		String[] spl1 = result.split("@");
		// spl1[0] : categorías
		// spl1[1] : productos

		// armar categorías
		String[] cats = spl1[0].split("\\|");
		G.arr_categorias_id = new String[cats.length];
		G.arr_categorias_nombre = new String[cats.length];
		//G.arr_categorias_url_logo = new String[cats.length];

		for (i = 0; i < cats.length; i++) {
			t = cats[i].split("&");
			G.arr_categorias_id[i] = t[0];
			G.arr_categorias_nombre[i] = t[1];
			//G.arr_categorias_url_logo[i] = G.base + "../" + t[2];
		}

		// armar productos
		String[] prods = spl1[1].split("\\|");
		G.arr_productos_id = new String[prods.length];
		G.arr_productos_nombre = new String[prods.length];
		G.arr_productos_descripcion = new String[prods.length];
		G.arr_productos_url_grande = new String[prods.length];
		G.arr_productos_url_mini = new String[prods.length];
		G.arr_productos_es_recomendado = new boolean[prods.length];
		G.arr_productos_id_categoria = new String[prods.length];
		G.arr_productos_link = new String[prods.length];
		for (i = 0; i < prods.length; i++) {
			t = prods[i].split("&");
			G.arr_productos_id[i] = t[0];
			G.arr_productos_nombre[i] = t[1]; 
			G.arr_productos_descripcion[i] = t[2];
			//G.arr_productos_url_grande[i] = G.base + "../" + t[3];
			G.arr_productos_url_grande[i] = t[3];
			G.arr_productos_url_mini[i] = G.base + "../" + t[5];			
			if (t[6].equals("1")) {
				G.arr_productos_es_recomendado[i] = true;
			} else {
				G.arr_productos_es_recomendado[i] = false;
			}
			G.arr_productos_link[i] = t[7];
			G.arr_productos_id_categoria[i] = t[8];
		}

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setClassName(thisActivity, Categorias.class.getName());
		thisActivity.startActivity(intent);
	}

	public void conectar(String pais) {
		if (at1 == null) {
			at1 = new ConectarAsyncTask(thisActivity);
		}
		if (!at1.isRunning()) {
			at1.execute(pais);
		} else {
			System.out.println("ya está corriendo");
		}
	}

	public void actualizar() {
		thisActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (G.isNetworkAvailable(thisActivity)) {
					// hay internet
					System.out.println("(S)");
					if (at2 == null) {
						at2 = new ActualizarAsyncTask(thisActivity);
					}
					if (!at2.isRunning()) {
						at2.execute(G.pais_elegido);
					} else {
						System.out.println("ya está corriendo");
					}

				} else {
					// no hay internet
					System.out.println("(R)");
					mensaje = "Se necesita conexión a internet para continuar";
					threadMensaje.start();
				}
			}
		});

	}

	private class ActualizarAsyncTask extends AsyncTask<String, Integer, String> { // <params,
		// progress,
		// result>
		private boolean running;
		private ProgressDialog dialog;
		private Activity act;

		@Override
		protected void onPreExecute() {
			// antes de llamar a doInBackground
			System.out.println("Comenzado a ejecutar AsyncTask actualizar");
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface arg0) {
					// //////////////////////////////////////////////
					// Cancelar el progressDialog ==> cancelar todo
					System.out.println("Dialog: cancelado");
					if (dialog.isShowing()) {
						dialog.dismiss();
					}
					cancel(true); // detener la AsyncTask
				}
			});
			dialog.setMessage("Conectando...");
			dialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			// ejecución en hebra
			int i;
			String pais = args[0];
			String[] cat;
			String categorias;
			String productos;
			running = true;
			String str_responses = null;

			// ///////////////////////////////////////////////
			// Código que se ejecuta en background

			// descargar las categorías
			publishProgress(0, 3, 0, 0); // paso, total pasos, subpasos, total subpasos
			categorias = G.ConexionWS(G.base + "getCategorias.php", "Pais_origen=" + pais + "&Id_movil=" + G.cod);
			if (categorias.startsWith("ERROR")) {
				//Toast.makeText(thisActivity.getApplicationContext(), "Error de conexión", Toast.LENGTH_LONG).show();
				return "";
			}
			if (!running)
				return "";

			// descargar las categorías
			String url;
			Bitmap bmp;
			int cp;

			productos = "";
			publishProgress(1, 3, 0, 0);

			// por cada categoría, descargar los productos
			String arr_categorias[] = categorias.split("\\|");
			cat = new String[arr_categorias.length];
			for (i = 0; i < arr_categorias.length; i++) {
				cat[i] = arr_categorias[i].split("&")[0];
				//url = G.base + "../" + arr_categorias[i].split("&")[2];
				//bmp = ImageManager.getInstance(act.getApplicationContext()).preloadImage(url, act);
				publishProgress(1, 3, i, arr_categorias.length);
				productos += G.ConexionWS(G.base + "getProductosCategoria.php", "Pais_origen=" + pais + "&ID_categoria=" + cat[i] + "&Id_movil=" + G.cod);
				if (productos.startsWith("ERROR")) {
					//Toast.makeText(thisActivity.getApplicationContext(), "Error de conexión", Toast.LENGTH_LONG).show();
					return "";
				}
				if (!running) 
					return "";
			}

			// descargar las miniaturas 
			String[] prods = productos.split("\\|");
			String[] p;
			cp = prods.length;
			for (i = 0; i < cp; i++) {
				if (!running)
					return "";
				publishProgress(2, 3, i, cp);
				System.out.println("P " + i + "  " + prods[i]);
				p = prods[i].split("&");
				url = G.base + "../" + p[5]; // url_mini viene en [5]
				//System.out.println("URL = " + url);
				bmp = ImageManager.getInstance(act.getApplicationContext()).preloadImage(url, act);
			}
			publishProgress(3, 3, 0, 0);

			/*
			// descargar las imágenes
			prods = productos.split("\\|");
			cp = prods.length;
			for (i = 0; i < cp; i++) {
				if (!running)
					return "";
				publishProgress(3, 4, i, cp);
				p = prods[i].split("&");
				url = G.base + "../" + p[3]; // url viene en [3]
				System.out.println("URL = " + url);
				bmp = ImageManager.getInstance(act.getApplicationContext()).preloadImage(url, act);
			}
			publishProgress(4, 4, 0, 0);
			*/

			str_responses = categorias + "@" + productos;
			// //////////////
			// revisar si lo cancelaron, NO QUITAR
			if (!running)
				return "";

			return str_responses;
		}

		// -- called from the publish progress
		// -- notice that the datatype of the second param gets passed to this
		// method
		// -- DESDE ACÁ SE PUEDEN LLAMAR A ELEMENTOS DE LA INTERFAZ GRÁFICA
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			String subMsg = "";
			if ((values[2] == 0) || (values[3] == 0)) {
				// sin subpasos
				dialog.setMax(values[1]);
				dialog.setProgress(values[0]);
			} else {
				// con subpasos
				dialog.setMax(values[3]);
				dialog.setProgress(values[2]);
				subMsg = " (" + values[2] + " / " + values[3] + ")";
			}

			// cambiar mensaje según estado: primer WS, segundo WS, listo
			switch (values[0]) {
			case 0:
			case 1:
				dialog.setMessage("Descargando Catálogos");// + subMsg);
				break;
			case 2:
				dialog.setMessage("Descargando Miniaturas"); // + subMsg);
				break;
			case 3:
				dialog.setMessage("Guardando"); // + subMsg);
				break;
			case 4:
				dialog.setMessage("Guardando");
				break;
			}

			// dialog.setMessage(String.valueOf(values[0]+" / "+ values[1]));
		}

		// -- called if the cancel button is pressed
		// -- ACTUALIZAR LA PANATALLA CON INFORMACIÓN DE QUE CANCELARON
		@Override
		protected void onCancelled() {
			super.onCancelled();
			running = false;
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			mensaje = "Proceso Cancelado";
			threadMensaje.start();

			System.out.println("CANCELADO");
			at2 = null;
		}

		// al finalizar
		@Override
		protected void onPostExecute(String result) {
			int i;
			boolean ok;
			String t[] = null;
			//G.grabarTxt("deco_pais", G.pais_elegido, act.getApplicationContext());
			//G.grabarTxt("deco_appuid", G.cod, act.getApplicationContext());

			//System.out.println("terminado: " + result);
			// procesar result: * extraer catálogos y productos, si llegaron OK => guardar TXT deco_catalogo
			//                  * si las miniaturas llegaron OK => guardar deco_fecha_updated

			// result = 1&sport|2&invierno|3&verano|@1&Chaqueta&Chaqueta Hombre Approach &catalogo/chaketa.jpg&catalogo/chaketa_chica.jpg&catalogo/chaketa_mini.jpg&1&www.falabella.cl|2&polera&polera hombre&catalogo/polera.jpg&catalogo/polera_chica.jpg&catalogo/polera_mini.jpg&0&www.falabella.cl|3&polera2&polera hombre&catalogo/polera2.jpg&catalogo/polera_chica.jpg&catalogo/polera2_mini.jpg&1&www.falabella.cl|4&polera3&polera hombre&catalogo/polera2.jpg&catalogo/polera_chica.jpg&catalogo/polera2_mini.jpg&1&www.falabella.cl|

			ok = true;

			try {
				String[] spl1 = result.split("@");
				// spl1[0] : categorías
				// spl1[1] : productos

				// armar categorías
				String[] cats = spl1[0].split("\\|");
				G.arr_categorias_id = new String[cats.length];
				G.arr_categorias_nombre = new String[cats.length];
				//G.arr_categorias_url_logo = new String[cats.length];

				for (i = 0; i < cats.length; i++) {
					t = cats[i].split("&");
					G.arr_categorias_id[i] = t[0];
					G.arr_categorias_nombre[i] = t[1];
					//G.arr_categorias_url_logo[i] = G.base + "../" + t[2];
				}

				// armar productos
				String[] prods = spl1[1].split("\\|");
				G.arr_productos_id = new String[prods.length];
				G.arr_productos_nombre = new String[prods.length];
				G.arr_productos_descripcion = new String[prods.length];
				G.arr_productos_url_grande = new String[prods.length];
				G.arr_productos_url_mini = new String[prods.length];
				G.arr_productos_es_recomendado = new boolean[prods.length];
				G.arr_productos_id_categoria = new String[prods.length];
				for (i = 0; i < prods.length; i++) {
					t = prods[i].split("&");
					G.arr_productos_id[i] = t[0];
					G.arr_productos_nombre[i] = t[1];
					G.arr_productos_descripcion[i] = t[2];
					G.arr_productos_url_grande[i] = G.base + "../" + t[3];
					G.arr_productos_url_mini[i] = G.base + "../" + t[5];
					if (t[6].equals("1")) {
						G.arr_productos_es_recomendado[i] = true;
					} else {
						G.arr_productos_es_recomendado[i] = false;
					}
					G.arr_productos_id_categoria[i] = t[8];
				}

				//System.out.println("cat IDS = " + G.arrToString(G.arr_categorias_id));
				//System.out.println("cat NOM = " + G.arrToString(G.arr_categorias_nombre));
				//System.out.println("pro IDS = " + G.arrToString(G.arr_productos_id));
				//System.out.println("pro NOM = " + G.arrToString(G.arr_productos_nombre));

				// guardar los catálogos+productos en TXT
				G.grabarTxt("deco_catalogo", result, act.getApplicationContext());
				System.out.println("(T)");

				// guardar fecha de actualización a la del servidor recién bajada
				G.grabarTxt("deco_fecha_updated", G.fechaUpdatedSrv, act.getApplicationContext());
				System.out.println("(U)");

			} catch (Exception e) {
				ok = false;
				e.printStackTrace();
				Toast.makeText(act.getApplicationContext(), "Error al procesar datos recibidos", Toast.LENGTH_LONG).show();
			}

			if (ok) {
				// avanzar
				avanzarPantalla();
			} else {
				// error
				System.out.println("Error ok=false =(");
			}

			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			at2 = null;
		}

		public boolean isRunning() {
			return running;
		}

		public ActualizarAsyncTask(Activity a) {
			super();
			act = a;
			running = false;
			System.out.println("iniciando la fucking conexión");
			dialog = new ProgressDialog(a);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			// OPCIONAL: personalizar la barra de progreso vía XML
			// dialog.setProgressDrawable(a.getResources().getDrawable(R.drawable.my_progress));
		}
	}

	// AsyncTask para SplashScreen
	private class ConectarAsyncTask extends AsyncTask<String, Integer, String> { // <params,
		// progress,
		// result>
		private boolean running;
		private ProgressDialog dialog;
		private Activity act;

		@Override
		protected void onPreExecute() {
			// antes de llamar a doInBackground
			System.out.println("Comenzado a ejecutar AsyncTask");
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface arg0) {
					// //////////////////////////////////////////////
					// Cancelar el progressDialog ==> cancelar todo
					System.out.println("Dialog: cancelado");
					if (dialog.isShowing()) {
						dialog.dismiss();
					}
					cancel(true); // detener la AsyncTask
				}
			});
			dialog.setMessage("Conectando...");
			dialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			// ejecución en hebra
			running = true;
			String str_responses = null;

			// ///////////////////////////////////////////////
			// Código que se ejecuta en background
			str_responses = G.ConexionWS(G.base + "guardarUsuarioMovil.php", "Id_movil=" + G.cod + "&Pais_origen=" + G.pais_elegido);
			if (str_responses.startsWith("ERROR")) {
				Toast.makeText(thisActivity.getApplicationContext(), "Error de conexión", Toast.LENGTH_LONG).show();
				return "";
			}
			// //////////////
			// revisar si lo cancelaron, NO QUITAR
			if (!running) {
				return str_responses;
			}

			return str_responses;
		}

		// -- called from the publish progress
		// -- notice that the datatype of the second param gets passed to this
		// method
		// -- DESDE ACÁ SE PUEDEN LLAMAR A ELEMENTOS DE LA INTERFAZ GRÁFICA
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			dialog.setMax(values[1]);
			dialog.setProgress(values[0]);
		}

		// -- called if the cancel button is pressed
		// -- ACTUALIZAR LA PANATALLA CON INFORMACIÓN DE QUE CANCELARON
		@Override
		protected void onCancelled() {
			super.onCancelled();
			running = false;
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			mensaje = "Proceso Cancelado";
			threadMensaje.start();
			System.out.println("CANCELADO");
			at1 = null;
		}

		// al finalizar
		@Override
		protected void onPostExecute(String result) {
			G.grabarTxt("deco_pais", G.pais_elegido, act.getApplicationContext());
			G.grabarTxt("deco_appuid", G.cod, act.getApplicationContext());
			//System.out.println("terminado: " + result);
			// (**)
			inicioSiguienteVez_bg();
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			at1 = null;
		}

		public boolean isRunning() {
			return running;
		}

		public ConectarAsyncTask(Activity a) {
			super();
			act = a;
			running = false;
			dialog = new ProgressDialog(a);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// OPCIONAL: personalizar la barra de progreso vía XML
			// dialog.setProgressDrawable(a.getResources().getDrawable(R.drawable.my_progress));
		}
	}

	private Thread threadMensaje = new Thread() {
		public void run() {
			mensaje();
		}
	};

	public void mensaje() {
		System.out.println("CANCEL DIALOG");
		thisActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				spinner.setVisibility(ProgressBar.INVISIBLE);
				AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity);
				builder.setTitle("DecoMóvil Perú");
				builder.setMessage(mensaje);
				builder.setCancelable(false);
				builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						thisActivity.finish();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
	}

}
