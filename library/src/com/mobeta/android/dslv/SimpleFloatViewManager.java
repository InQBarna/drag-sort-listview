package com.mobeta.android.dslv;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ImageView;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.util.Log;

/**
 * Simple implementation of the FloatViewManager class. Uses list items as they
 * appear in the ListView to create the floating View.
 */
public class SimpleFloatViewManager implements DragSortListView.FloatViewManager {

	 private Bitmap mFloatBitmap;
	private ImageView cellListViewIV;
	private Bitmap mCellBitmap;
	private ImageView mImageView;
	
	private LinearLayout layout;
	
	private int mFloatBGColor = Color.BLACK;
	
	private ListView mListView;


	public SimpleFloatViewManager(ListView lv) {
		mListView = lv;
	}

	public void setBackgroundColor(int color) {
		mFloatBGColor = color;
	}

	/**
	 * This simple implementation creates a Bitmap copy of the list item
	 * currently shown at ListView <code>position</code>.
	 */
	@Override
	public View onCreateFloatView(int position) {
		// Guaranteed that this will not be null? I think so. Nope, got
		// a NullPointerException once...
		View v = mListView.getChildAt(position + mListView.getHeaderViewsCount() - mListView.getFirstVisiblePosition());

		if (v == null) {
			return null;
		}

		v.setPressed(false);

		// Create a copy of the drawing cache so that it does not get
		// recycled by the framework when the list tries to clean up memory
		//v.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
		v.setDrawingCacheEnabled(true);
		mFloatBitmap = Bitmap.createBitmap(v.getDrawingCache());
		v.setDrawingCacheEnabled(false);

		if (mImageView == null) {
			mImageView = new ImageView(mListView.getContext());
		}
		mImageView.setBackgroundColor(mFloatBGColor);
		mImageView.setPadding(0, 0, 0, 0);
		mImageView.setImageBitmap(mFloatBitmap);
		mImageView.setLayoutParams(new ViewGroup.LayoutParams(v.getWidth(), v.getHeight()));

		return mImageView;
	}

	@Override
	public View onCreateFloatCustomView(int position, View left, View right) {
		View v = mListView.getChildAt(position + mListView.getHeaderViewsCount() - mListView.getFirstVisiblePosition());

		if (v == null) {
			return null;
		}

		v.setPressed(false);
		Log.d("M","CustomView??");
		// Create a copy of the drawing cache so that it does not get
		// recycled by the framework when the list tries to clean up memory
		//v.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

		if (cellListViewIV == null) {
			cellListViewIV = new ImageView(mListView.getContext());
		}
		cellListViewIV.setBackgroundColor(mFloatBGColor);
		cellListViewIV.setPadding(0, 0, 0, 0);
		v.setDrawingCacheEnabled(true);
		mCellBitmap = Bitmap.createBitmap(v.getDrawingCache());
		v.setDrawingCacheEnabled(false);
		cellListViewIV.setImageBitmap(mCellBitmap);
		cellListViewIV.setLayoutParams(new ViewGroup.LayoutParams(v.getWidth(), v.getHeight()));
		if (layout == null) {
			layout = new LinearLayout(mListView.getContext());
		}
		left.measure(0, 0);
		int totalWidth = v.getWidth() + left.getMeasuredWidth() + right.getMeasuredWidth();
		layout.setLayoutParams(new LinearLayout.LayoutParams(totalWidth, LayoutParams.WRAP_CONTENT));
		layout.removeAllViews();
		layout.addView(left);

		layout.addView(cellListViewIV);
		layout.addView(right);
		layout.setDrawingCacheEnabled(true);
		layout.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		layout.layout(0, 0, totalWidth, layout.getMeasuredHeight());
		layout.buildDrawingCache(true);
		mFloatBitmap = Bitmap.createBitmap(layout.getDrawingCache());
		layout.setDrawingCacheEnabled(false);

		if (mImageView == null) {
			mImageView = new ImageView(mListView.getContext());
		}

		mImageView.setBackgroundColor(mFloatBGColor);
		mImageView.setPadding(0, 0, 0, 0);

		mImageView.setImageBitmap(mFloatBitmap);
		mImageView.setLayoutParams(new ViewGroup.LayoutParams(layout.getWidth(), layout.getHeight()));

		return mImageView;

	}

	/**
	 * This does nothing
	 */
	@Override
	public void onDragFloatView(View floatView, Point position, Point touch) {
		// do nothing
	}

	/**
	 * Removes the Bitmap from the ImageView created in onCreateFloatView() and
	 * tells the system to recycle it.
	 */
	@Override
	public void onDestroyFloatView(View floatView) {
		((ImageView) floatView).setImageDrawable(null);
		if(mCellBitmap!=null){
			mCellBitmap.recycle();
			mCellBitmap = null;
		}
		mFloatBitmap.recycle();
		mFloatBitmap = null;
	}

}
