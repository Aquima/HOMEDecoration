package ar.com.falabella.decomovil.pantallas;

import ar.com.falabella.decomovil.G;
import ar.com.falabella.decomovil.SplashScreen;
import ar.com.falabella.decomovil.utils.CategoriasAdapter;
import ar.com.falabella.decomovil.utils.MyImageView;
import ar.com.falabella.decomovil.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class Categorias extends ListActivity {
	private Activity thisActivity;
	private int vecesTouch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.categorias);

		thisActivity = this;
		vecesTouch = 0;

		System.out.println("LLeg� a Categorias : ");
		System.out.println("-----------------------------------------------");
		System.out.println("Pais : " + G.pais_elegido);

		/** Movement and Rezise TextView Titulo Categoria **/
		// ---------MOVEMENT TITULO CATEGORIA (TEXTO)---------
		LinearLayout lTxt = (LinearLayout) findViewById(R.id.lTxt2);
		LayoutParams params_txt = (LayoutParams) lTxt
				.getLayoutParams();
		params_txt.topMargin = (int) (G.screenG / 3);
		params_txt.leftMargin = (int) (G.screenW / 32);
		lTxt.setLayoutParams(params_txt);

		TextView tv = (TextView) findViewById(R.id.txt_titulo);
		Typeface tf = Typeface.createFromAsset(getBaseContext().getAssets(),
				"fonts/VistaSanSCBol.ttf");
		tv.setTypeface(tf);

		/** Ajuste de ListView **/
		ListView lv = (ListView) findViewById(android.R.id.list);
		LinearLayout l2 = (LinearLayout) findViewById(R.id.linearLayoutP1);
		LayoutParams paramsL2 = (LayoutParams) l2
				.getLayoutParams();
		paramsL2.topMargin = (int) (410.0 * G.screenG / 960.0);
		// paramsL2.leftMargin = (int) (G.screenW / 4.5); // 25
		paramsL2.leftMargin = (int) (G.screenW / 3);

		if (G.screenH > 960 && G.screenW > 640) {
			/** TABLET **/
			paramsL2.topMargin = (int) (410.0 * G.screenG / 960.0);
			paramsL2.leftMargin = (int) (G.screenW / 3);
		} else {
			/** MOBILE **/
			paramsL2.topMargin = (int) (410.0 * G.screenG / 960.0);
			paramsL2.leftMargin = (int) (G.screenW / 4.5); // 25
		}
		int height_lv = G.screenH / 2 - G.screenH / 32;
		lv.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, height_lv));
		l2.setLayoutParams(paramsL2);

		/************************* Codificaci�n IMGs *********************************/
		// Movimiento instrucciones (fake-img)
		/**
		 * LinearLayout instrucciones_fake = (LinearLayout)
		 * findViewById(R.id.instrucciones_fake2); LinearLayout.LayoutParams
		 * params_inst_fake = (LayoutParams)
		 * instrucciones_fake.getLayoutParams(); params_inst_fake.topMargin =
		 * (int) (G.screenG/3.15); params_inst_fake.rightMargin = (int) (10);
		 * instrucciones_fake.setLayoutParams(params_inst_fake);
		 * instrucciones_fake.setVisibility(View.GONE);
		 **/

		// Movimiento instrucciones
		LinearLayout inst = (LinearLayout) findViewById(R.id.instrucciones2);
		LayoutParams params_inst = (LayoutParams) inst
				.getLayoutParams();
		params_inst.topMargin = (int) (G.screenG / 3.15);
		params_inst.rightMargin = (int) (10);
		inst.setLayoutParams(params_inst);

		// boton instrucciones
		LinearLayout lInstrucciones = (LinearLayout) findViewById(R.id.instrucciones);
		Bitmap bmp_instrucciones = BitmapFactory.decodeResource(getResources(),
				R.drawable.instrucciones);
		int altura_instrucciones = 60 * G.screenG / 960;
		int ancho_instrucciones = bmp_instrucciones.getWidth()
				* altura_instrucciones / bmp_instrucciones.getHeight();
		Bitmap bmp_instrucciones_a = G.getResizedBitmap(bmp_instrucciones,
				ancho_instrucciones, altura_instrucciones);
		// Bitmap bmp_instrucciones_pressed =
		// BitmapFactory.decodeResource(getResources(),
		// R.drawable.instrucciones_pressed);
		// Bitmap bmp_instrucciones_pressed_a =
		// G.getResizedBitmap(bmp_instrucciones_pressed, ancho_instrucciones ,
		// altura_instrucciones);
		MyImageView btn_instrucciones = new MyImageView(
				this.getApplicationContext(), bmp_instrucciones_a);
		lInstrucciones.addView(btn_instrucciones);

		/**
		 * Options options = new BitmapFactory.Options(); options.inScaled =
		 * false; // LATA int altura_inst = 400 * G.screenG / 960; Bitmap
		 * bmp_inst = BitmapFactory.decodeResource(getResources(),
		 * R.drawable.instrucciones); int ancho_inst = bmp_inst.getWidth() *
		 * altura_inst / bmp_inst.getHeight(); BitmapDrawable bmd_inst = new
		 * BitmapDrawable(Bitmap.createScaledBitmap(bmp_inst, ancho_inst,
		 * altura_inst, true)); //btn_lata.setImageDrawable(bmd_inst);
		 * MyImageView btn_instrucciones_b = new
		 * MyImageView(this.getApplicationContext(), bmd_inst,
		 * bmp_instrucciones_pressed_a);
		 * lInstrucciones.addView(btn_instrucciones);
		 **/

		// onClick
		btn_instrucciones.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					// Code TODO
					System.out.println("CLIC");
					final Dialog dialog = new Dialog(thisActivity,
							android.R.style.Theme_Black_NoTitleBar_Fullscreen);
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dialog.setContentView(R.layout.instrucciones);
					dialog.setCancelable(true);

					// ---------MOVEMENT TITULO CATEGORIA (TEXTO)---------
					LinearLayout lTxt = (LinearLayout) dialog
							.findViewById(R.id.lTxt2i);
					LayoutParams params_txt2 = (LayoutParams) lTxt
							.getLayoutParams();
					if (G.screenH > 960 && G.screenW > 640) {
						/** TABLET **/
						params_txt2.topMargin = (int) (G.screenG / 3);
					} else {
						/** MOBILE **/
						params_txt2.topMargin = (int) (G.screenG / 3);
						params_txt2.leftMargin = (int) (G.screenW / 27);
					}

					lTxt.setLayoutParams(params_txt2);

					TextView tv = (TextView) dialog
							.findViewById(R.id.txt_tituloi);
					Typeface tf = Typeface.createFromAsset(getBaseContext()
							.getAssets(), "fonts/VistaSanSCBol.ttf");
					tv.setTypeface(tf);

					// MOVIMIENTO SCROLL DE INSTRUCCIONES
					int top = 320; // relativo al alto general de 960, no son
									// pixeles
					int alto = 450;
					// Hide the Scollbar
					ScrollView sView = (ScrollView) dialog
							.findViewById(R.id.ScrollView01);
					sView.setVerticalScrollBarEnabled(false);
					sView.setHorizontalScrollBarEnabled(false);

					LinearLayout lScroll = (LinearLayout) dialog
							.findViewById(R.id.layScroll2);
					LayoutParams param_scroll = (LayoutParams) lScroll
							.getLayoutParams();
					param_scroll.leftMargin = (int) (40.0 * G.screenC / 640.0);
					param_scroll.rightMargin = (int) (40.0 * G.screenC / 640.0);
					param_scroll.topMargin = (int) (440.0 * G.screenG / 960.0);
					param_scroll.height = (int) (alto * G.screenG / 960.0);
					lScroll.setLayoutParams(param_scroll);

					LinearLayout linea_continuar = (LinearLayout) dialog
							.findViewById(R.id.linearC2);
					LayoutParams params_continuar = (LayoutParams) linea_continuar
							.getLayoutParams();
					params_continuar.bottomMargin = (int) (G.screenH / 6);
					linea_continuar.setLayoutParams(params_continuar);

					// bot�n COntinuar
					Bitmap bmp1 = BitmapFactory.decodeResource(getResources(),
							R.drawable.continuar);
					// Bitmap bmp2 =
					// BitmapFactory.decodeResource(getResources(),
					// R.drawable.continuar_press);
					MyImageView btn = new MyImageView(thisActivity
							.getApplicationContext(), bmp1);
					LinearLayout layBtnContinuar = (LinearLayout) dialog
							.findViewById(R.id.layBtnContinuar);
					layBtnContinuar.addView(btn);
					btn.setOnTouchListener(new OnTouchListener() {
						public boolean onTouch(View v, MotionEvent event) {
							switch (event.getAction() & MotionEvent.ACTION_MASK) {
							case MotionEvent.ACTION_UP:
							case MotionEvent.ACTION_POINTER_UP:
								// cerrar di�logo
								System.out.println("cerrar di�logo");
								dialog.dismiss();
								break;
							}
							return false;
						}
					});

					dialog.show();
					break;
				}
				return false;
			}
		});

		/*************** Fin Condificaci�n instrucciones ************************/

		// llenar ListView con Categor�as
		ListView listView = (ListView) findViewById(android.R.id.list);
		if (G.arr_categorias_id == null) {
			// parche: esto queda nulo por alg�n m�stico motivo, probablemente
			// relacionado con que el SO libera RAM.
			System.out.println("SE FUE AL INICIO");
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClassName(thisActivity, SplashScreen.class.getName());
			thisActivity.startActivity(intent);
			return;
		}
		int total = G.arr_categorias_id.length;
		String[] values = new String[total];
		// String[] logos = new String[total];

		for (int i = 0; i < G.arr_categorias_id.length; i++) {
			System.out.println("Categorias[" + i + "] : "
					+ G.arr_categorias_nombre[i]);
			values[i] = G.arr_categorias_nombre[i];
			// logos[i] = G.arr_categorias_url_logo[i];
			// System.out.println(logos[i]);
		}

		// crear adaptador para los ListItems del ListView, utilizando la clase
		// HotArrayAdapter
		CategoriasAdapter adapter = new CategoriasAdapter(this, values);

		// Assign adapter to ListView
		listView.setAdapter(adapter); 

		// Acci�n en Click
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Toast.makeText(getApplicationContext(),
				// "Click ListItem Number " + position,
				// Toast.LENGTH_LONG).show();
				G.numero_categoria = position;
				G.id_categoria_elegida = G.arr_categorias_id[position];
				G.nombre_categoria = G.arr_categorias_nombre[position];

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setClassName(thisActivity, Productos.class.getName());
				thisActivity.startActivity(intent);
			}
		});
	}

	@Override
	public void onBackPressed() {
		// ir al home en vez de al splash
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
