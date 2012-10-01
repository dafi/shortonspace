package com.shortonspace;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

class StorageArrayAdapter extends ArrayAdapter<Map<String, Object>> {
	public static final String STORAGE_PATH = "path";
	public static final String STORAGE_CAPACITY = "capacity";
	public static final String STORAGE_USED_SPACE = "usedSpace";
	public static final String STORAGE_FREE_SPACE = "freeSpace";
	public static final String STORAGE_DESCRIPTION = "description";
	
	private List<Map<String, Object>> items;
	private Drawable progressHorizontal;
	private ColorUtils colorUtils;

	public StorageArrayAdapter(Context context, int textViewResourceId,
			List<Map<String, Object>> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		
		colorUtils = new ColorUtils(context);
	    progressHorizontal = getContext().getResources().getDrawable(android.R.drawable.progress_horizontal);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.storage_row, null);
		}
		Map<String, Object> map = items.get(position);
		if (map != null) {
			TextView storagePath = (TextView) v.findViewById(R.id.storage_path);

			if (storagePath != null) {
				String path = (String)map.get(STORAGE_PATH);
				String description = (String)map.get(STORAGE_DESCRIPTION);
				storagePath.setText(description + " (" + path + ")");
			}
			Long capacity = (Long)map.get(STORAGE_CAPACITY);
			if (capacity == null) {
				v.findViewById(R.id.used_space_row).setVisibility(View.GONE);
				v.findViewById(R.id.free_space_row).setVisibility(View.GONE);
				v.findViewById(R.id.used_space_bar).setVisibility(View.GONE);
			
				TextView capacityView = (TextView) v.findViewById(R.id.capacity_space);
				
				capacityView.setText(getContext().getResources().getString(R.string.unable_to_determine_capacity));

			} else {
				v.findViewById(R.id.used_space_row).setVisibility(View.VISIBLE);
				v.findViewById(R.id.free_space_row).setVisibility(View.VISIBLE);
				v.findViewById(R.id.used_space_bar).setVisibility(View.VISIBLE);

				long freeSpace = (Long) map.get(STORAGE_FREE_SPACE);
				long usedSpace = (Long) map.get(STORAGE_USED_SPACE);
				int colorIndex = (int)(usedSpace * colorUtils.getColorsCount() / capacity);

				TextView capacityView = (TextView) v.findViewById(R.id.capacity_space);
				TextView usedView = (TextView) v.findViewById(R.id.used_space);
				TextView freeView = (TextView) v.findViewById(R.id.free_space);
				
				ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.used_space_bar);
				progressBar.setMax(colorUtils.getColorsCount());
			    progressBar.setProgressDrawable(colorUtils.getClipDrawable(colorIndex));
				progressBar.setBackgroundDrawable(progressHorizontal);
				progressBar.setProgress(colorIndex);
				
				if (capacityView != null) {
					capacityView.setText(Utils.stringFromFileSize(capacity));
				}
				if (usedView != null) {
					usedView.setText(Utils.stringFromFileSize(usedSpace));
				}
				if (freeView != null) {
					freeView.setText(Utils.stringFromFileSize(freeSpace));
				}
			}
		}
		return v;
	}
}