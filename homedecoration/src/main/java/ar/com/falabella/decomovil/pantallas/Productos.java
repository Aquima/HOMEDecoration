package ar.com.falabella.decomovil.pantallas;

import ar.com.falabella.decomovil.G;
import ar.com.falabella.decomovil.utils.ImageManager;
import ar.com.falabella.decomovil.utils.ProductosAdapter;
import ar.com.falabella.decomovil.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class Productos extends Activity {

	private Activity thisActivity;
	private MyAsyncTask at1;

	/** Called when the thisActivity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.prendas);
		thisActivity = this;
		at1 = null;

		// Definici�n de variables
		int pos_elegidos[] = null;
		int i, j, cant;

		// viene por categor�as
		System.out.println("VIENE POR CAT. " + G.numero_categoria + ": "
				+ G.arr_categorias_nombre[G.numero_categoria]);

		// buscar los elementos de la categor�a y guardar sus posiciones en
		// pos_elegidos
		cant = 0;
		for (i = 0; i < G.arr_productos_id_categoria.length; i++) {
			// System.out.println("G.arr_productos_id_categoria["+i+"] = "+G.arr_productos_id_categoria[i]);
			if (G.arr_productos_id_categoria[i].equals(G.id_categoria_elegida)) {
				cant++;
			}
		}
		pos_elegidos = new int[cant];
		j = 0;
		for (i = 0; i < G.arr_productos_id_categoria.length; i++) {
			if (G.arr_productos_id_categoria[i].equals(G.id_categoria_elegida)) {
				pos_elegidos[j] = i;
				j++;
			}
		}

		// pos_elegidos ok: arreglo con las posiciones de los productos que se
		// van a desplegar
		System.out.print("PRODUCTOS ELEGIDOS: ");
		for (i = 0; i < cant; i++) {
			System.out.print(pos_elegidos[i] + " - ");
		}
		System.out.println("");

		/** Ajuste de ListView **/
		ListView lv = (ListView) findViewById(R.id.ListViewP);
		LinearLayout l2 = (LinearLayout) findViewById(R.id.linearLayoutP1);
		LayoutParams paramsL2 = (LayoutParams) l2
				.getLayoutParams();
		if(G.screenH > 960 && G.screenW > 640){ 
							/** TABLET **/ 
			paramsL2.topMargin = (int) (410.0 * G.screenG / 960.0);
			paramsL2.leftMargin = (int) (G.screenH/4 - G.screenH/10); 
		}else{ 									
							/** MOBILE **/
			paramsL2.topMargin = (int) (410.0 * G.screenG / 960.0);
			//paramsL2.leftMargin = (int) (50.0 * G.screenC / 640.0); // 25
		}
		
		System.out.println("Pantalla : Ancho : "+ G.screenW + " ...." + "Alto : " + G.screenH);
		int height_lv = G.screenH / 2 - G.screenH / 32;
		lv.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, height_lv));
		l2.setLayoutParams(paramsL2);

		/** Movement and Rezise TextView Titulo Categoria **/
		TextView txt_titulo = (TextView) findViewById(R.id.txt_titulo_p);
		Typeface tf = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/VistaSanSCBol.ttf");
		txt_titulo.setText("SELECCIONA TU PRODUCTO ");
		txt_titulo.setTypeface(tf);

		// ---------MOVEMENT TITULO CATEGORIA (TEXTO)---------
		LinearLayout lTxt = (LinearLayout) findViewById(R.id.lTxt2);
		LayoutParams params_txt = (LayoutParams) lTxt
				.getLayoutParams();
		params_txt.topMargin = (int) (G.screenG / 3);
		params_txt.leftMargin = (int) (G.screenW / 32);
		lTxt.setLayoutParams(params_txt);

		// llenar el listview con las prendas
		String[] values = new String[cant];
		final String[] ids = new String[cant];
		String[] urls = new String[cant];
		for (i = 0; i < cant; i++) {
			values[i] = G.arr_productos_nombre[pos_elegidos[i]];
			ids[i] = G.arr_productos_id[pos_elegidos[i]];
			urls[i] = G.arr_productos_url_mini[pos_elegidos[i]];
		}

		ListView listView = (ListView) findViewById(R.id.ListViewP);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				G.id_producto_elegido = ids[position];
				if (at1 == null) {
					at1 = new MyAsyncTask(thisActivity);
				}
				if (!at1.isRunning()) {
					at1.execute();
				} else {
					System.out.println("ya est� corriendo");
				}
			}
		});

		// crear adaptador para los ListItems del ListView
		ProductosAdapter adapter = new ProductosAdapter(this, values, urls);
		// Assign adapter to ListView
		listView.setAdapter(adapter);

	}

	// /////////////////////////// asynctask ////////////////////////
	private class MyAsyncTask extends AsyncTask<Integer, Integer, String> { // <params,
																			// progress,
																			// result>
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
					// //////////////////////////////////////////////
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

			// ///////////////////////////////////////////////
			// C�digo que se ejecuta en background
			String response = "";

			// descargar la foto grande y registrar prenda elegida en el
			// servidor (otro thread por si no hay internet)
			String url_productos = ""; 
			//String url[] = null;
			
			int i;
			for (i = 0; i < G.arr_productos_id.length; i++) {
				if (G.arr_productos_id[i].equals(G.id_producto_elegido)) {
					url_productos = G.arr_productos_url_grande[i];		
					G.url = url_productos.split("#"); 
					id_producto = G.arr_productos_id[i]; 
					G.descripcion_producto_elegido = G.arr_productos_descripcion[i];	
					G.link_falabella = G.arr_productos_link[i];
				}
			}
			
			

			// registrar en el servidor (fire and forget), s�lo si hay internet
			if (G.isNetworkAvailable(act)) {
				new Thread() {
					public void run() {
						G.ConexionWS(G.base + "guardarPrendaSeleccionada.php",
								"Pais_origen=" + G.pais_elegido
										+ "&ID_Producto=" + id_producto
										+ "&Id_movil=" + G.cod);
					}
				}.start();
			}

			// descargar las imagenes
			G.url_producto = new String[G.url.length];
			Bitmap bmp = null;
			for (int a = 0; a < G.url.length; a++){
				G.url_producto[a] = G.base+"../"+G.url[a];
				bmp = ImageManager.getInstance(act).preloadImage(G.url_producto[a], act); // descarga
			}		
			

			// //////////////
			// revisar si lo cancelaron, NO QUITAR
			if (!running) {
				return response;
			}

			return response;
		}

		// -- called from the publish progress
		// -- notice that the datatype of the second param gets passed to this
		// method
		// -- DESDE AC� SE PUEDEN LLAMAR A ELEMENTOS DE LA INTERFAZ GR�FICA
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

		public MyAsyncTask(Activity a) {
			super();
			act = a;
			running = false;
			dialog = new ProgressDialog(a);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// OPCIONAL: personalizar la barra de progreso v�a XML
			// dialog.setProgressDrawable(a.getResources().getDrawable(R.drawable.my_progress));
		}
	}
}
