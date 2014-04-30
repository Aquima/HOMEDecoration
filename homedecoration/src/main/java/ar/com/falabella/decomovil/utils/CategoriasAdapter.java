package ar.com.falabella.decomovil.utils;

import ar.com.falabella.decomovil.R;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CategoriasAdapter extends ArrayAdapter<String> {
	private final Activity context;
	private final String[] names;

	static class ViewHolder {
		public TextView text;
	}

	public CategoriasAdapter(Activity context, String[] names) {
		super(context, R.layout.elemento_lista, names);
		this.context = context;
		this.names = names;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.categorias_row, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) rowView.findViewById(R.id.labelc);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		String s = names[position];
		holder.text.setText(s);
		
		rowView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.categoria_btn));
		return rowView;
	}
}