package com.inqbarna.splyce.dragsortlistview;

import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

public class TwoActionDragSortController extends DragSortController {

	public TwoActionDragSortController(DragSortListView dslv) {
		super(dslv, 0, ON_LONG_PRESS, FLING_REMOVE);
		Log.d("M", "DragSortController constructor!!");
		mFlingRemoveDetector = new GestureDetector(dslv.getContext(), mFlingRemoveListener);
	}

	public TwoActionDragSortController(DragSortListView dslv, int dragHandleId, int dragInitMode, int removeMode, int clickRemoveId, int flingHandleId) {
		super(dslv, dragHandleId, dragInitMode, removeMode, clickRemoveId, flingHandleId);

	}

	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		if (!mDslv.isDragEnabled() || mDslv.listViewIntercepted()) {
			return false;
		}

		mDetector.onTouchEvent(ev);
		if (mRemoveEnabled && mDragging && mRemoveMode == FLING_REMOVE) {
			mFlingRemoveDetector.onTouchEvent(ev);
		}

		int action = ev.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mCurrX = (int) ev.getX();
				mCurrY = (int) ev.getY();
			break;
			case MotionEvent.ACTION_UP:
				Log.d("M", "Paso por aqui??!!!removeEnabled: " + mRemoveEnabled + " mIsremoving: " + mIsRemoving);
				if (mRemoveEnabled && mIsRemoving) {
					Log.d("M", "Paso por aqui??!!!");
					int removePoint = mDslv.getWidth() / 2;
					if (mPositionX > removePoint) {
						Log.d("M", "AutoSort!!!");
					} else if (mPositionX < -removePoint) {
						mDslv.stopDragWithVelocity(true, 0);
					} else {
						Log.d("M", "No fem res");
					}
				}
			case MotionEvent.ACTION_CANCEL:
				mIsRemoving = false;
				mDragging = false;
			break;
		}

		return false;

	}

	@Override
	public void onLongPress(MotionEvent e) {
		Log.d("mobeta", "lift listener long pressed");
		mHitPos = mDslv.pointToPosition((int)e.getX(), (int)e.getY());
		if (mHitPos != MISS && mDragInitMode == ON_LONG_PRESS) {
			mDslv.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
			startDrag(mHitPos, mCurrX - mItemX, mCurrY - mItemY);
		}
	}

	public GestureDetector.OnGestureListener mFlingRemoveListener =
			new GestureDetector.SimpleOnGestureListener() {
				@Override
				public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
						float velocityY) {
					Log.d("M", "OnFling!");
					if (mRemoveEnabled && mIsRemoving) {
						int w = mDslv.getWidth();
						int minPos = w / 5;
						if (velocityX < -mFlingSpeed) {
							if (mPositionX < minPos) {
								mDslv.stopDragWithVelocity(true, velocityX);
							}
						}
						mIsRemoving = false;
					}
					return false;
				}
			};

}
