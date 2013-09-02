package com.inqbarna.splyce.dragsortlistview;

import android.content.Context;
import android.util.AttributeSet;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

public class SplyceDragSortListView extends DragSortListView {

	private AutoSortListener mAutoSortListener;
	
	public SplyceDragSortListView(Context context, AttributeSet attrs) {
		super(context, attrs);	
	}
	
	 public interface AutoSortListener {
	        public void autoSort(int which);
	    }
	
	
}
