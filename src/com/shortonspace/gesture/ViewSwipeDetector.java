package com.shortonspace.gesture;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

// Adapted from stackoverflow.com 
// http://stackoverflow.com/questions/937313/android-basic-gesture-detection?lq=1

public class ViewSwipeDetector implements View.OnTouchListener {
	static final String logTag = "ActivitySwipeDetector";
	static final int MIN_DISTANCE = 100;
	private float downX, downY, upX, upY;
	private View view;
	private final ViewSwipeListener viewSwipeListener;

	public ViewSwipeDetector(View view, ViewSwipeListener viewSwipeListener) {
		this.view = view;
		this.viewSwipeListener = viewSwipeListener;
		view.setOnTouchListener(this);    
	}

	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			downX = event.getX();
			downY = event.getY();
			return true;
		}
		case MotionEvent.ACTION_UP: {
			upX = event.getX();
			upY = event.getY();

			float deltaX = downX - upX;
			float deltaY = downY - upY;

			// swipe horizontal?
			if (Math.abs(deltaX) > MIN_DISTANCE) {
				// left or right
				if (deltaX < 0) {
					viewSwipeListener.onLeftToRightSwipe();
					return true;
				}
				if (deltaX > 0) {
					viewSwipeListener.onRightToLeftSwipe();
					return true;
				}
			} else {
				Log.i(logTag, "Swipe was only " + Math.abs(deltaX)
						+ " long, need at least " + MIN_DISTANCE);
				return false; // We don't consume the event
			}

			// swipe vertical?
			if (Math.abs(deltaY) > MIN_DISTANCE) {
				// top or down
				if (deltaY < 0) {
					viewSwipeListener.onTopToBottomSwipe();
					return true;
				}
				if (deltaY > 0) {
					viewSwipeListener.onBottomToTopSwipe();
					return true;
				}
			} else {
				Log.i(logTag, "Swipe was only " + Math.abs(deltaX)
						+ " long, need at least " + MIN_DISTANCE);
				return false; // We don't consume the event
			}

			return true;
		}
		}
		return false;
	}

	public View getView() {
		return view;
	}
}