package com.shortonspace;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;

public class ColorUtils {
	private TypedArray backgroundFoldersColors;
	private TypedArray textFoldersColors;
	private Drawable backgroundFileColor;
	private int textFileColor;
	private GradientDrawable[] gradients;
	private ClipDrawable[] clips;
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
		backgroundFileColor = createGradientFromColor(resources.getColor(R.color.background_file_color));
		textFileColor = resources.getColor(R.color.text_file_color);
	}

	public int getTextFolderColor(int index) {
		int safeIndex = Math.min(index, backgroundFoldersColors.length() - 1);
		return textFoldersColors.getColor(safeIndex, 0);
	}

	public Drawable getBackgroundFolderColor(int index) {
		int safeIndex = Math.min(index, backgroundFoldersColors.length() - 1);
		return getGradientColor(safeIndex);
	}

	public int getTextFileColor() {
		return textFileColor;
	}
	
	public Drawable getBackgroundFileColor() {
		return backgroundFileColor;
	}

	public GradientDrawable getGradientColor(int index) {
		if (gradients == null) {
			int length = backgroundFoldersColors.length();
			gradients = new GradientDrawable[length];

			for (int i = 0; i < length; i++) {
				int color = backgroundFoldersColors.getColor(i, 0);
				gradients[i] = createGradientFromColor(color);
			}
		}
		return gradients[index];
	}

	private GradientDrawable createGradientFromColor(int startColor) {
		float hsv[] = new float[3];
		Color.colorToHSV(startColor, hsv);
		// create the end color
		hsv[2] *= 0.7;
		int endColor = Color.HSVToColor(hsv);
		GradientDrawable gd = new GradientDrawable(
				GradientDrawable.Orientation.TOP_BOTTOM, new int[] {
						startColor, endColor });
		gd.setCornerRadius(5);
		return gd;
	}
	
	public ClipDrawable getClipDrawable(int index) {
		if (clips == null) {
			int length = backgroundFoldersColors.length();
			clips = new ClipDrawable[length];

			for (int i = 0; i < length; i++) {
				GradientDrawable gd = getGradientColor(i);
				ClipDrawable clip = new ClipDrawable(gd, Gravity.LEFT,
						ClipDrawable.HORIZONTAL);
				// colors are inverted, lower value less space used
				clips[length - 1 - i] = clip;
			}
		}
		return clips[index];
	}

	public int getColorsCount() {
		return backgroundFoldersColors.length();
	}

	public Context getContext() {
		return context;
	}
}
