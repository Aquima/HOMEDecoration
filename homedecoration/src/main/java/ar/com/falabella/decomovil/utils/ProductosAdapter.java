package ar.com.falabella.decomovil.utils;

import ar.com.falabella.decomovil.R;
import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProductosAdapter extends ArrayAdapter<String> {
	private final Activity context;
	private final String[] names;
	private final String[] logos;

	static class ViewHolder {
		public TextView text;
		public ImageView image;
	}

	public ProductosAdapter(Activity context, String[] names, String[] logos) {
		super(context, R.layout.elemento_lista, names);
		this.context = context;
		this.names = names;
		this.logos = logos;
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.elemento_lista, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) rowView.findViewById(R.id.label);
			viewHolder.image = (ImageView) rowView.findViewById(R.id.icon);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		String s = names[position];
		holder.text.setText(s);
		Uri l = Uri.parse(logos[position]);
		if (logos!=null){
			holder.image.setTag(logos[position]);
			ImageManager.getInstance(context).displayImage(logos[position], context, holder.image);
		}
		
		int cualFondo = position % 4;
		switch (cualFondo) {
		/**
		case 0:
			rowView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.categoria_verde));
			break;
		case 1:
			rowView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.categoria_amarilla));
			break;
		case 2:
			rowView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.categoria_roja));
			break;
		case 3:
			rowView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.categoria_amarilla_clara));
			break;
		**/
		default:
			rowView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.base_productos));
			break;
		}
		return rowView;
	}
}