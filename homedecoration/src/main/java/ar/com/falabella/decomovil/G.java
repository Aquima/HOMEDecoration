package ar.com.falabella.decomovil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

//import com.facebook.FacebookConnector;



import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

public class G {
//	public static FacebookConnector facebookConnector;
	public static final String FACEBOOK_APPID = "336781026357846"; // 336781026357846 DecoM�vil Per� <CAMBIAR AL DE CO>
	public static final String FACEBOOK_PERMISSION = "publish_stream, user_photos";
	public static String uid;
	public static String token;

	public static boolean multitouch=false;
	
	public static String base = "http://hidra.advante.cl/mobile/falabella_deco/WS/";
	public static String nombre_categoria = null;
	public static int numero_categoria;
	public static int screenW;
	public static int screenH;
	public static int screenG; // el m�s grande entre ancho y alto
	public static int screenC; // el chico

	public static String pais_elegido = null;

	public static String cod = null; // c�digo aleatorio que identifica al usuario
	public static String fechaUpdated = null; // fecha de actualizaci�n en el equipo
	public static String fechaUpdatedSrv = null; // fecha de actualizaci�n en el servidor
	public static String strCatalogo = null; // string con los cat�logos

	public static String arr_categorias_id[] = null; // categor�as
	public static String arr_categorias_nombre[] = null;
	//public static String arr_categorias_url_logo[] = null;
	

	public static String arr_productos_id[] = null; // productos
	public static String arr_productos_nombre[] = null;
	public static String arr_productos_descripcion[] = null;
	public static String arr_productos_url_mini[] = null;
	public static String arr_productos_url_grande[] = null;
	public static String arr_productos_id_categoria[] = null;
	public static boolean arr_productos_es_recomendado[] = null;
	public static String arr_productos_link[] = null;
	public static String url_producto[] = null;
	public static String url[] = null;
	public static String id_categoria_elegida = null;
	public static String id_producto_elegido = null;
	public static String descripcion_producto_elegido = null;
	public static int posicion = 0;
	public static Bitmap bmp_foto_final = null;
	public static String modelo = "";
	public static String urlFoto;
	public static String pathFoto;
	public static String color = "0";
	public static String link_falabella = "";
	public static Activity camActivity = null;
	
	/* ************************************* Funciones Globales ************************************************ */

	public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		try {
			Matrix matrix = new Matrix();
			// RESIZE THE BITMAP
			matrix.postScale(scaleWidth, scaleHeight);
			// RECREATE THE NEW BITMAP
			Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

			return resizedBitmap;
		} catch (Exception e) {
			return bm;
		}
	}

	/**
	 * Realiza conexi�n a WS usando POST sin argumentos. De todas maneras, env�a "Modelo=Android"
	 * 
	 * @param php
	 *            Ruta al php (ej: G.base + "script.php")
	 * @return La respuesta que entregue el PHP (los echo).
	 */
	public static String ConexionWS(String php) {
		return ConexionWS(php, null, null);
	}

	/**
	 * Realiza conexi�n a WS usando POST. Env�a "Modelo=Android" en los argumentos
	 * 
	 * @param php
	 *            Ruta al php (ej: G.base + "script.php")
	 * @param args
	 *            String con argumentos a enviar mediante POST (ej: "nombre=Juan&apellido=Perez&rut=11222333-9")
	 * @return La respuesta que entregue el PHP (los echo).
	 */
	public static String ConexionWS(String php, String args) {
		return ConexionWS(php, args, null);
	}

	/**
	 * Realiza conexi�n a WS usando POST, adem�s de subir un Bitmap. Env�a "Modelo=Android" en los argumentos
	 * 
	 * @param php
	 *            Ruta al php (ej: G.base + "script.php")
	 * @param args
	 *            String con argumentos a enviar mediante POST (ej: "nombre=Juan&apellido=Perez&rut=11222333-9")
	 * @param bmp
	 *            Bitmap que contiene la imagen a subir. Autom�ticamente se comprime en JPG antes de enviar. Puede ser NULL
	 * @return La respuesta que entregue el PHP (los echo).
	 */
	public static String ConexionWS(String php, String args, Bitmap bmp) {
		return ConexionWS(php, args, bmp, "bmp");
	}
	
	/**
	 * Realiza conexi�n a WS usando POST, adem�s de subir un Bitmap. Env�a "Modelo=Android" en los argumentos
	 * 
	 * @param php
	 *            Ruta al php (ej: G.base + "script.php")
	 * @param args
	 *            String con argumentos a enviar mediante POST (ej: "nombre=Juan&apellido=Perez&rut=11222333-9")
	 * @param bmp
	 *            Bitmap que contiene la imagen a subir. Autom�ticamente se comprime en JPG antes de enviar. Puede ser NULL
	 * @param nombreBitmap
	 *            El nombre que se env�a a php: $_FILES[nombreBitmap]
	 * @return La respuesta que entregue el PHP (los echo).
	 */
	public static String ConexionWS(String php, String args, Bitmap bmp, String nombreBitmap) {
		String ret = "";
		try { 
			int i;

			// Construct POST data
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(php);
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			if ((args != null) && !args.equals("")) {
				String[] d1 = args.split("&");
				for (i = 0; i < d1.length; i++) {
					String d[] = d1[i].split("=");
					if (!d[0].equals("Modelo")) {
						reqEntity.addPart(d[0], new StringBody(d[1]));
						// System.out.println("G: " + d[0] + "=" + d[1]);
					}
				}
			}
			reqEntity.addPart("Modelo", new StringBody(G.modelo));

			if (bmp != null) { // si hay bitmap
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bmp.compress(CompressFormat.JPEG, 80, bos);
				byte[] data = bos.toByteArray();
				ByteArrayBody bab = new ByteArrayBody(data, "fotows.jpg"); // ES
																			// COMO
																			// LO
																			// RECIBE
																			// EL
																			// SERVIDOR
				reqEntity.addPart(nombreBitmap, bab); // $_FILES['usrfile']
				// System.out.println("WS: " + "bmp" + "=" + bab.toString());
			}

			postRequest.setEntity(reqEntity);
			HttpResponse response = httpClient.execute(postRequest);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String sString = reader.readLine();
			//System.out.println("asd : " + sString);
			ret = sString;
		} catch (UnsupportedEncodingException e) {
			ret = "ERROR " + e.toString();
			e.printStackTrace();
		} catch (MalformedURLException e) {
			ret = "ERROR " + e.toString();
			e.printStackTrace();
		} catch (IOException e) {
			ret = "ERROR " + e.toString();
			e.printStackTrace();
		} catch (Exception e){
			ret = "ERROR " + e.toString();
			e.printStackTrace();
		}
		return ret;
	}

	// Verifica la conexion de internet
	public static boolean isNetworkAvailable(Activity activity) {
		ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	// Grabar TXT.
	public static void grabarTxt(String sFileName, String sBody, Context context) {
		try {
			String cache_url;
			File cacheDir;

			// revisar si la SD est� disponible
			boolean mExternalStorageAvailable = false;
			boolean mExternalStorageWriteable = false;
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				// We can read and write the media
				mExternalStorageAvailable = mExternalStorageWriteable = true;
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				// We can only read the media
				mExternalStorageAvailable = true;
				mExternalStorageWriteable = false;
			} else {
				// Something else is wrong. It may be one of many other states, but all we need
				// to know is we can neither read nor write
				mExternalStorageAvailable = mExternalStorageWriteable = false;
			}

			if (mExternalStorageAvailable && mExternalStorageWriteable) {
				File root = Environment.getExternalStorageDirectory();
				cache_url = "/Android/data/" + context.getPackageName() + "/cache/";
				cacheDir = new File(root, cache_url);
			} else {
				cacheDir = context.getCacheDir();
			}

			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}
			File gpxfile = new File(cacheDir, sFileName);
			FileWriter writer = new FileWriter(gpxfile);
			writer.append(sBody);
			writer.flush();
			writer.close();
			//return cacheDir+sFileName;
		} catch (IOException e) {
			e.printStackTrace();
			//return null;
		}
	}

	// Leer TXT.
	public static String leerTxt(String sFileName, Context context) {
		// revisar si la SD est� disponible
		String cache_url;
		File cacheDir;
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

		if (mExternalStorageAvailable && mExternalStorageWriteable) {
			File root = Environment.getExternalStorageDirectory();
			cache_url = "/Android/data/" + context.getPackageName() + "/cache/";
			cacheDir = new File(root, cache_url);
		} else {
			cacheDir = context.getCacheDir();
		}
		File file = new File(cacheDir, sFileName);
		StringBuilder text = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				text.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		return text.toString();
	}

	public static Bitmap resizeBitmap(Bitmap bitmapOrg, int newWidth, int newHeight) {
		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();

		// calculate the scale - in this case = 0.4f
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// createa matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);
		return resizedBitmap;
	}

	public static String arrToString(String[] arr) {
		String ret = "";
		int i;
		if (arr != null) {
			for (i = 0; i < arr.length; i++) {
				ret += arr[i] + " - ";
			}
		}
		return ret;
	}

	// c�digo aleatorio
	public static String generarCodigoAleatorio() {
		String clave = "";
		int i;
		int rnd;

		// caracteres aleatorios (entre 10 y 20)
		int max_chars = (int) Math.round(Math.random() * 10 + 10); // tendr entre 10 y 20 caracteres
		String[] vocales = { "a", "e", "i", "o", "u" };
		String[] consonantes = { "q", "w", "r", "t", "y", "p", "s", "d", "f", "g", "h", "j", "k", "l", "m", "n", "b", "v", "c", "x", "z" };
		int v = (int) Math.round(Math.random());
		for (i = 0; i < max_chars; i++) {
			if (v == 1) { // consonante
				rnd = (int) Math.round(Math.random() * (consonantes.length - 1));
				// System.out.println(consonantes.length+" "+rnd);
				clave += consonantes[rnd];
			} else { // vocal
				rnd = (int) Math.round(Math.random() * (vocales.length - 1));
				// System.out.println(vocales.length+" "+rnd);
				clave += vocales[rnd];
			}
			if (v == 1)
				v = 0;
			else
				v = 1;
		}

		// agregar timestamp
		long unixTime = System.currentTimeMillis() / 1000L;
		return clave + unixTime;

	}
}

