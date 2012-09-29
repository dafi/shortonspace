package com.shortonspace;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class FolderItemArrayAdapter extends ArrayAdapter<FolderItem> {
	private List<FolderItem> items;
	private ColorUtils colorUtils;

	public FolderItemArrayAdapter(Context context, int textViewResourceId,
			List<FolderItem> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		colorUtils = new ColorUtils(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.retriever_row, null);
		}
		FolderItem fi = items.get(position);
		if (fi != null) {
			int[] colors;
			
			if (fi.isFileObject()) {
				colors = colorUtils.getFileColors();
			} else {
				// empty folders are shown with same color
				colors = colorUtils.getFolderColors(fi.isFolderEmpty() ? colorUtils.getColorsCount() - 1 : position);
			}
			v.setBackgroundColor(colors[ColorUtils.BACKGROUND_COLOR]);

			TextView filename = (TextView) v.findViewById(R.id.filename);
			TextView filecount = (TextView) v.findViewById(R.id.filecount);
			TextView filesize = (TextView) v.findViewById(R.id.filesize);

			setRowText(getContext(), fi, colors[ColorUtils.TEXT_COLOR], filename, filecount, filesize);
		}
		return v;
	}
	
	public static void setRowText(Context context, FolderItem fi, int textColor,
			TextView filename, TextView filecount, TextView filesize) {
		if (filename != null) {
			filename.setText(fi.getFile().getName());
			filename.setTextColor(textColor);
		}
		boolean isFolderEmpty = fi.isFolderEmpty();
		if (filecount != null) {
			String text = null;
			Resources res = context.getResources();
			if (isFolderEmpty) {
				text = res.getString(R.string.folder_is_empty);
			} else {
				if (fi.getSubFilesCount() > 0) {
					text = String.format(res.getString(R.string.files_count),
							Utils.countFormatter.format(fi.getSubFilesCount()));
				}
				if (fi.getSubFoldersCount() > 0) {
					text = text == null ? "" : text + ", ";
					text += String.format(res.getString(R.string.folders_count),
								Utils.countFormatter.format(fi.getSubFoldersCount()));
				}
			}
			filecount.setText(text);
			filecount.setTextColor(textColor);
		}
		if (filesize != null) {
			if (isFolderEmpty) {
				filesize.setText("");
			} else {
				filesize.setText(Utils.stringFromFileSize(
						fi.isFileObject() ? fi.getFileSize() : fi.getSubFoldersSize(), false, false));
			}
			filesize.setTextColor(textColor);
		}
	}
}