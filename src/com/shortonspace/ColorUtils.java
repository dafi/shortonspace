package com.shortonspace;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;

public class ColorUtils {
	public static final int BACKGROUND_COLOR = 0;
	public static final int TEXT_COLOR = 1;

	private TypedArray backgroundFoldersColors;
	private TypedArray textFoldersColors;
	private int backgroundFileColor;
	private int textFileColor;
	private ClipDrawable[] gradients;
	private final Context context;

	public ColorUtils(Context context) {
		this.context = context;
		Resources resources = context.getResources();
		backgroundFoldersColors = resources
				.obtainTypedArray(R.array.background_folders_colors);
		textFoldersColors = resources
				.obtainTypedArray(R.array.text_folders_colors);

		if (backgroundFoldersColors.length() != textFoldersColors.length()) {
			throw new IndexOutOfBoundsException(
					"backgroundFoldersColors length differs from textFoldersColors");
		}
		backgroundFileColor = resources.getColor(R.color.background_file_color);
		textFileColor = resources.getColor(R.color.text_file_color);
	}

	public int[] getFolderColors(int index) {
		int length = backgroundFoldersColors.length();
		if (index < length) {
			return new int[] { backgroundFoldersColors.getColor(index, 0),
					textFoldersColors.getColor(index, 0) };
		}
		int last = length - 1;
		return new int[] { backgroundFoldersColors.getColor(last, 0),
				textFoldersColors.getColor(last, 0) };

	}

	public int[] getFileColors() {
		return new int[] { backgroundFileColor, textFileColor };
	}

	public ClipDrawable getGradientColor(int index) {
		if (gradients == null) {
			int length = backgroundFoldersColors.length();
			gradients = new ClipDrawable[backgroundFoldersColors.length()];
			int endColor = context.getResources().getColor(
					R.color.storage_bar_gradient_end_color);
			for (int i = 0; i < length; i++) {
				int color = backgroundFoldersColors.getColor(i, 0);
				GradientDrawable gd = new GradientDrawable(
						GradientDrawable.Orientation.TOP_BOTTOM, new int[] {
								color, endColor });
				gd.setCornerRadius(5);
				ClipDrawable clip = new ClipDrawable(gd, Gravity.LEFT,
						ClipDrawable.HORIZONTAL);
				// colors are inverted, lower value less space used
				gradients[length - 1 - i] = clip;
			}
		}
		return gradients[index];
	}
	
	public int getColorsCount() {
		return backgroundFoldersColors.length();
	}
}
