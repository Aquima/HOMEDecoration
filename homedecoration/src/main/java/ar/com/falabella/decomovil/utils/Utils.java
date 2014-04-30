package ar.com.falabella.decomovil.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;

public class Utils {
	public static void grabarTxt(String archivo, String texto) {

		try {

			File ruta = Environment.getExternalStorageDirectory();

			File gpxfile = new File(ruta, archivo);

			FileWriter escibir = new FileWriter(gpxfile);

			escibir.append(texto);

			escibir.flush();
			escibir.close();

		}

		catch (IOException e)

		{

			System.out.println("Excepción al escribir texto: " + e.toString());

		}

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

	public static String implode(String[] ary, String delim) {
		String out = "";
		for (int i = 0; i < ary.length; i++) {
			if (i != 0) {
				out += delim;
			}
			out += ary[i];
		}
		return out;
	}
	
	
	public static String correcionCaracteres(String palabra){
		String arrGood[] = new String[12];
		String arrBad[] = new String[12];
		arrBad[0] = "á"; arrGood[0] = "a";
		arrBad[1] = "é"; arrGood[1] = "e";
		arrBad[2] = "í"; arrGood[2] = "i";
		arrBad[3] = "ó"; arrGood[3] = "o";
		arrBad[4] = "ú"; arrGood[4] = "u";
		arrBad[5] = "Á"; arrGood[5] = "A";
		arrBad[6] = "É"; arrGood[6] = "E";
		arrBad[7] = "Í"; arrGood[7] = "I";
		arrBad[8] = "Ó"; arrGood[8] = "O";
		arrBad[9] = "Ú"; arrGood[9] = "U";
		arrBad[10] = "ñ"; arrGood[10] = "n";
		arrBad[11] = "Ñ"; arrGood[11] = "N"; 
		
		for(int i = 0; i < arrGood.length; i++){
			int cant_err = palabra.indexOf(arrBad[i]);
			if (cant_err > 0){
				palabra = palabra.replace(arrBad[i].toString(), arrGood[i].toString());
			}
		}	
		
		return palabra;
	}
}
