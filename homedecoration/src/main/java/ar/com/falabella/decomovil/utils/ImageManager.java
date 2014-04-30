package ar.com.falabella.decomovil.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import ar.com.falabella.decomovil.G;

import ar.com.falabella.decomovil.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.ImageView;


// fuente: https://github.com/cacois/TweetView/blob/master/src/com/example/ImageManager.java
public class ImageManager {

	// ///// singleton
	private static final ImageManager instance = new ImageManager();
	private static boolean isSet;

	private ImageManager() {
	}

	public static ImageManager getInstance(Context c) {
		if (!isSet) {
			instance.init(c);
		}

		return instance;
	}

	// /////////////

	// Just using a hashmap for the cache. SoftReferences would
	// be better, to avoid potential OutOfMemory exceptions
	private HashMap<String, Bitmap> imageMap = new HashMap<String, Bitmap>();

	private File cacheDir;
	private String cache_url;
	private ImageQueue imageQueue = new ImageQueue();
	private Thread imageLoaderThread = new Thread(new ImageQueueManager());

	private MapCleaner mapCleaner; // libera los bitmaps que no se han usado en un tiempo

	private long fechaUpdate = 0; // por defecto no usar fecha

	private void init(Context context) {
		// Make background thread low priority, to avoid affecting UI
		// performance
		// System.out.println("INICIANDO");
		imageLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);

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

		// Find the dir to save cached images
		if (mExternalStorageAvailable && mExternalStorageWriteable) {
			File sdDir = Environment.getExternalStorageDirectory();
			// url para cache en SD
			cache_url = "/Android/data/" + context.getPackageName() + "/cache/";
			cacheDir = new File(sdDir, cache_url);
		} else
			cacheDir = context.getCacheDir();

		if (!cacheDir.exists())
			cacheDir.mkdirs();

		// System.out.println("IMAGEMANAGER guardando en "+cacheDir.getAbsolutePath());
		// Toast.makeText(context, cacheDir.getAbsolutePath(), Toast.LENGTH_LONG).show();

		isSet = true;

		mapCleaner = new MapCleaner();
		mapCleaner.start();

	}

	public File getCacheDir() {
		return cacheDir;
	}

	public void setFechaUpdate(long fecha) {
		fechaUpdate = fecha;
	}

	public void displayImage(String url, Activity activity, ImageView imageView) {
		// System.out.println("mostrar = --" + url+"--");
		if (url == null) {
			System.out.println("Error en la fucking imagen");
		} else {
			if (imageMap.containsKey(url)) {
				// System.out.println("displayImage: desde cach� "+url+" actividad="+activity);
				Bitmap bmp = imageMap.get(url);
				imageView.setTag((String) imageView.getTag());
				imageView.setImageBitmap(bmp);
				
				// mandar intenet con el tama�o de la foto
				//System.out.println("foto en cach� 1, mandado intent "+bmp.getWidth()+" x "+bmp.getHeight());
				Intent i = new Intent("foto_cargada");
				i.putExtra("tag", (String) imageView.getTag());
				i.putExtra("w", bmp.getWidth());
				i.putExtra("h", bmp.getHeight());
				activity.sendBroadcast(i);

				

			} else {
				// System.out.println("displayImage: descargar "+url+" actividad="+activity);
				queueImage(url, activity, imageView);
				// System.out.println("IMG OK 2");
				imageView.setImageResource(R.drawable.loading2); // cargando...
			}
		}
	}

	public void displayImage(String url, Activity activity, ImageView imageView, int w, int h) {
		// System.out.println("mostrar = --" + url+"--");
		if (url == null) {
			System.out.println("Error en la fucking imagen");
		} else {
			if (imageMap.containsKey(url)) {
				// System.out.println("displayImage: desde cach� "+url+" actividad="+activity);
				Bitmap bmp = imageMap.get(url);
				bmp = G.resizeBitmap(bmp, w, h); // escalar
				imageView.setTag((String) imageView.getTag());
				imageView.setImageBitmap(bmp);
				
				// mandar intenet con el tama�o de la foto
				System.out.println("foto en cach� 2, mandado intent "+bmp.getWidth()+" x "+bmp.getHeight());
				Intent i = new Intent("foto_cargada");
				i.putExtra("tag", (String) imageView.getTag());
				i.putExtra("w", bmp.getWidth());
				i.putExtra("h", bmp.getHeight());
				activity.sendBroadcast(i);
				

			} else {
				// System.out.println("displayImage: descargar "+url+" actividad="+activity);
				queueImage(url, activity, imageView, w, h);
				// System.out.println("IMG OK 2");
				imageView.setImageResource(R.drawable.loading2); // cargando...
			}
		}
	}

	public Bitmap preloadImage(String url, Activity a) {
		Bitmap ret = null;
		if (imageMap.containsKey(url)) { // si est� en el map, retornarlo directo
			ret = imageMap.get(url);
		} else { // no est� en el map, ver si est� en el cach� (SD) o bajarlo 
			ret = getBitmap(url, fechaUpdate, a);
		}
		return ret;
	}

	private void queueImage(String url, Activity activity, ImageView imageView) {
		// This ImageView might have been used for other images, so we clear
		// the queue of old tasks before starting.
		imageQueue.Clean(imageView);
		ImageRef p = new ImageRef(url, imageView);

		synchronized (imageQueue.imageRefs) {
			imageQueue.imageRefs.push(p);
			imageQueue.imageRefs.notifyAll();
			// System.out.println("IMG OK");
		}

		// Start thread if it's not started yet
		if (imageLoaderThread.getState() == Thread.State.NEW)
			imageLoaderThread.start();
	}

	private void queueImage(String url, Activity activity, ImageView imageView, int w, int h) {
		// This ImageView might have been used for other images, so we clear
		// the queue of old tasks before starting.
		imageQueue.Clean(imageView);
		ImageRef p = new ImageRef(url, imageView, w, h);

		synchronized (imageQueue.imageRefs) {
			imageQueue.imageRefs.push(p);
			imageQueue.imageRefs.notifyAll();
			// System.out.println("IMG OK");
		}

		// Start thread if it's not started yet
		if (imageLoaderThread.getState() == Thread.State.NEW)
			imageLoaderThread.start();
	}

	private Bitmap getBitmap(String url, long fechaUpdate, Activity a) {
		// System.out.println("DESCARGAR: "+url);
		// Utils.grabarTxt(".aaafotos.txt", url);
		url = url.replaceAll("https", "http"); // PARCHE: algunos perfiles manadan con https y el certificado no est� aceptado

		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);

		// -- System.out.println("fecha archivo "+f.getPath()+" = "+ f.lastModified());

		// Is the bitmap in our cache?
		Bitmap bitmap = BitmapFactory.decodeFile(f.getPath());
		if (bitmap != null) {
			//System.out.println("IMG Cach� "+url);
			// Is the bitmap up-to-date?

			// -- System.out.println(f.lastModified()+" > "+fechaUpdate);
			// if (f.lastModified() > fechaUpdate) { // est� al d�a
			// -- System.out.println(f.getPath()+" est� al d�a (no recachear)");
			return bitmap;
			// }
		}

		// -- System.out.println(f.getPath()+" (re)cargar");

		// Nope, have to download it
		boolean bajo_ok = false;
		try {
			System.out.println("IMG descargar "+url);
			bitmap = BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());
			bajo_ok = true;
		} catch (Exception ex) {
			// ex.printStackTrace();
			System.err.println("excepci�n en url = " + url);
			System.err.println(ex.toString());
			bajo_ok = false;
			// return null;
		}

		// chequear que no haya bajado 0kb (por alg�n m�sitco motivo pasa igual)
		if (bitmap != null) {
			if (bitmap.getWidth() == 0) {
				bajo_ok = false;
			}
		} else {
			bajo_ok = false;
		}

		if (bajo_ok) {
			// save bitmap to cache for later
			writeFile(bitmap, f);
		} else {
			bitmap = BitmapFactory.decodeResource(a.getResources(), R.drawable.error);
		}
		return bitmap;
	}

	private void writeFile(Bitmap bmp, File f) {
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(f);
			bmp.compress(Bitmap.CompressFormat.PNG, 9, out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (Exception ex) {
			}
		}
	}

	/** Classes **/

	private class ImageRef {
		public String url;
		public ImageView imageView;
		public int w;
		public int h;

		public ImageRef(String u, ImageView i) {
			url = u;
			imageView = i;
			w = 0;
			h = 0;
		}

		public ImageRef(String u, ImageView i, int ancho, int alto) {
			url = u;
			imageView = i;
			w = ancho;
			h = alto;
		}
	}

	// stores list of images to download
	private class ImageQueue {
		private Stack<ImageRef> imageRefs = new Stack<ImageRef>();

		// removes all instances of this ImageView
		public void Clean(ImageView view) {

			for (int i = 0; i < imageRefs.size();) {
				if (imageRefs.get(i).imageView == view)
					imageRefs.remove(i);
				else
					++i;
			}
		}
	}

	private class ImageQueueManager implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					// Thread waits until there are images in the
					// queue to be retrieved
					if (imageQueue.imageRefs.size() == 0) {
						synchronized (imageQueue.imageRefs) {
							imageQueue.imageRefs.wait();
						}
					}

					// When we have images to be loaded
					if (imageQueue.imageRefs.size() != 0) {
						ImageRef imageToLoad;

						synchronized (imageQueue.imageRefs) {
							imageToLoad = imageQueue.imageRefs.pop();
						}

						Activity a = (Activity) imageToLoad.imageView.getContext();
						Bitmap bmp = getBitmap(imageToLoad.url, fechaUpdate, a);
						Bitmap error = BitmapFactory.decodeResource(a.getResources(), R.drawable.error);
						if (!error.equals(bmp)) {
							imageMap.put(imageToLoad.url, bmp); // sin escalar
							if ((imageToLoad.w > 0) && (imageToLoad.h > 0)) {
								// escalar el bmp
								bmp = G.resizeBitmap(bmp, imageToLoad.w, imageToLoad.h);
							}
						}
						Object tag = imageToLoad.imageView.getTag();

						// Make sure we have the right view - thread safety defender
						if (tag != null && (((String) tag).indexOf(imageToLoad.url) > -1)) { // indexOf, porque el tag puedetener ma�s informaci�n adem�s de la URL
							BitmapDisplayer bmpDisplayer = new BitmapDisplayer(bmp, imageToLoad.imageView);
							imageToLoad.imageView.setTag(tag);
							a.runOnUiThread(bmpDisplayer);
							
							
							// mandar intenet con el tama�o de la foto
							//System.out.println("foto en cach� 0, mandado intent "+bmp.getWidth()+" x "+bmp.getHeight());
							Intent i = new Intent("foto_cargada");
							i.putExtra("tag", (String)tag);
							i.putExtra("w", bmp.getWidth());
							i.putExtra("h", bmp.getHeight());
							a.sendBroadcast(i);
							
						    
						} else {
							// -- System.out.println("TAG NULL = " + (String) tag);
						}
					}

					if (Thread.interrupted())
						break;

					Thread.sleep(100);
				}
			} catch (InterruptedException e) {
			}
		}
	}

	// Used to display bitmap in the UI thread
	private class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		ImageView imageView;

		public BitmapDisplayer(Bitmap b, ImageView i) {
			bitmap = b;
			imageView = i;
		}

		public void run() {
			if (bitmap != null) {
				// System.out.println("IMG mostrar cargada");
				imageView.setImageBitmap(bitmap);
			} else {
				// System.out.println("IMG no cargo, mostrar icono");
				imageView.setImageResource(R.drawable.loading2);
			}
		}
	}

	// ////////////// liberar memoria de bitmaps que no se han usado en un rato
	private class MapCleaner extends Thread {

		public void run() {
			while (true) {
				try {
					int i;
					Thread.sleep(5000);

					ArrayList<String> keysAsList = new ArrayList<String>(imageMap.keySet());
					ArrayList<String> claves = (ArrayList<String>) keysAsList.clone();

					// calcular memoria usada por el imageMap
					int suma = 0;

					if (claves.size() > 30) {
						// m�s de 30 im�genes, limpiar las 20 primeras
						System.out.println("--- ImageManager --- liberar las 20 m�s antiguas");
						i = 0;
						Iterator<String> it = claves.iterator();
						while (it.hasNext()) {
							String element = it.next();
							imageMap.remove(element);
							i++;
							if (i > 20) {
								break;
							}
						}
					}

					//System.out.println("--- ImageManager --- hay " + imageMap.size() + " im�genes");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}

	////////////////////////////////////////////////////////////////////////////////////

}
