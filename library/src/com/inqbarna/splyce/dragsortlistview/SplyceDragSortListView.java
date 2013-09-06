package com.inqbarna.splyce.dragsortlistview;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.mobeta.android.dslv.DragSortListView;

public class SplyceDragSortListView extends DragSortListView {

	private AutoSortListener mAutoSortListener;
private RemoveAutoSortAnimator mRemoveAutoSortAnimator;

	private View left;
	private View right;

	public SplyceDragSortListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		float smoothness = 0.5f;
		mRemoveAutoSortAnimator = new RemoveAutoSortAnimator(smoothness, 150);
	}

	public void setViewLeft(View left) {
		this.left = left;
	}

	public void setViewRight(View right) {
		this.right = right;
	}

	public void setAutoSortListener(AutoSortListener l) {
		this.mAutoSortListener = l;
	}

	public boolean stopDragAutoSort(float velocityX) {
		 mUseRemoveVelocity = true;
	        return stopDrag(velocityX);
	}

	public void startAutoSortAnimation(Context context,int pos) {
				
	}

	public interface AutoSortListener {
		public void autoSort(int which);
	}

	public boolean stopDrag(float velocityX) {
        if (mFloatView != null) {
            mDragScroller.stopScrolling(true);
            removeItemForAutoSort(mSrcPos - getHeaderViewsCount(), velocityX);
           

            if (mTrackDragSort) {
                mDragSortTracker.stopTracking();
            }

            return true;
        } else {
            // stop failed
            return false;
        }
    }
	
	public boolean startDrag(int position, int dragFlags, int deltaX, int deltaY) {
		if (!mInTouchEvent || mFloatViewManager == null) {
			return false;
		}
		View v;
		if (left == null || right == null) {
			v = mFloatViewManager.onCreateFloatView(position);
		} else {
			v = mFloatViewManager.onCreateFloatCustomView(position, left, right);
		}

		if (v == null) {
			return false;
		} else {
			return startDrag(position, v, dragFlags, deltaX, deltaY);
		}

	}
	
	/**
     * Removes an item from the list and animates the removal.
     *
     * @param which Position to remove (NOTE: headers/footers ignored!
     * this is a position in your input ListAdapter).
     * @param velocityX 
     */
    public void removeItemForAutoSort(int which, float velocityX) {
        if (mDragState == IDLE || mDragState == DRAGGING) {

            if (mDragState == IDLE) {
                // called from outside drag-sort
                mSrcPos = getHeaderViewsCount() + which;
                mFirstExpPos = mSrcPos;
                mSecondExpPos = mSrcPos;
                mFloatPos = mSrcPos;
                View v = getChildAt(mSrcPos - getFirstVisiblePosition());
                if (v != null) {
                    v.setVisibility(View.INVISIBLE);
                }
            }

            mDragState = REMOVING;
            mRemoveVelocityX = Math.abs(velocityX);

            if (mInTouchEvent) {
                switch (mCancelMethod) {
                    case ON_TOUCH_EVENT:
                        super.onTouchEvent(mCancelEvent);
                        break;
                    case ON_INTERCEPT_TOUCH_EVENT:
                        super.onInterceptTouchEvent(mCancelEvent);
                        break;
                }
            }

            if (mRemoveAutoSortAnimator != null) {
            	mRemoveAutoSortAnimator.start();
            } else {
            	if (mAutoSortListener != null) {
        			mAutoSortListener.autoSort(mSrcPos - getHeaderViewsCount());
        		}
            	destroyFloatView();

                adjustOnReorder();
                clearPositions();

                // now the drag is done
                if (mInTouchEvent) {
                    mDragState = STOPPED;
                } else {
                    mDragState = IDLE;
                }
            }
        }
    }
	

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mFloatView != null) {
			// draw the float view over everything^M
			final int w = mFloatView.getWidth();
			final int h = mFloatView.getHeight();

			final int margin = left.getWidth();
			int x = mFloatLoc.x;
			int width = getWidth();
			if (x < 0)
				x = -x;
			float alphaMod;
			if (x < width) {
				alphaMod = ((float) (width - x)) / ((float) width);
				alphaMod *= alphaMod;
			} else {
				alphaMod = 0;
			}

			final int alpha = (int) (255f * mCurrFloatAlpha * alphaMod);

			//final int margin = 0;
			canvas.save();
			// Log.d("mobeta", "clip rect bounds: " + canvas.getClipBounds());^M
			canvas.translate(mFloatLoc.x - margin, mFloatLoc.y);
			canvas.clipRect(0, 0, w, h);

			mFloatView.draw(canvas);
			canvas.restore();
			canvas.restore();
		}
	}
	private class RemoveAutoSortAnimator extends SmoothAnimator {

        private float mFloatLocX;
        private float mFirstStartBlank;
        private float mSecondStartBlank;

        private int mFirstChildHeight = -1;
        private int mSecondChildHeight = -1;

        private int mFirstPos;
        private int mSecondPos;
        private int srcPos;

        public RemoveAutoSortAnimator(float smoothness, int duration) {
            super(smoothness, duration);
        }

        @Override
        public void onStart() {
            mFirstChildHeight = -1;
            mSecondChildHeight = -1;
            mFirstPos = mFirstExpPos;
            mSecondPos = mSecondExpPos;
            srcPos = mSrcPos;
            mDragState = REMOVING;
           /* Log.d("M","mFirstPos: "+mFirstPos);
            Log.d("M","mSecondPos: "+mSecondPos);
            Log.d("M","srcPos: "+mSrcPos);*/
            
            
            mFloatLocX = mFloatLoc.x;
            if (mUseRemoveVelocity) {
                float minVelocity = 2f * getWidth();
                if (mRemoveVelocityX == 0) {
                	Log.d("M","VelocityX: "+mRemoveVelocityX);
                    mRemoveVelocityX = (mFloatLocX < 0 ? -1 : 1) * minVelocity;
                } else {
                    minVelocity *= 2;
                    if (mRemoveVelocityX < 0 && mRemoveVelocityX > -minVelocity){
                        mRemoveVelocityX = -minVelocity;
                    	Log.d("M","VelocityX: "+mRemoveVelocityX);
                    }else if (mRemoveVelocityX > 0 && mRemoveVelocityX < minVelocity){
                        mRemoveVelocityX = minVelocity;
                        Log.d("M","VelocityX: "+mRemoveVelocityX);
                    }
                }
            } else {
                destroyFloatView();
            }
        }

        @Override
        public void onUpdate(float frac, float smoothFrac) {
            float f = 1f - smoothFrac;

            final int firstVis = getFirstVisiblePosition();
            View item = getChildAt(mFirstPos - firstVis);
            ViewGroup.LayoutParams lp;
            int blank;
        /*    Log.d("M","update mFirstPos: "+mFirstPos);
            Log.d("M","update mSecondPos: "+mSecondPos);
            Log.d("M","update srcPos: "+mSrcPos);*/
            if (mUseRemoveVelocity) {
                float dt = (float) (SystemClock.uptimeMillis() - mStartTime) / 1000;
                if (dt == 0)
                    return;
                float dx = mRemoveVelocityX * dt;
                int w = getWidth();
                mRemoveVelocityX += (mRemoveVelocityX > 0 ? 1 : -1) * dt * w;
                Log.d("M","VelocityX: "+mRemoveVelocityX);
                mFloatLocX += dx;
                mFloatLoc.x = (int) mFloatLocX;
                if (mFloatLocX < w && mFloatLocX > -w) {
                    mStartTime = SystemClock.uptimeMillis();
                    doDragFloatView(true);
                    return;
                }
            }

            if (item != null) {
                if (mFirstChildHeight == -1) {
                    mFirstChildHeight = getChildHeight(mFirstPos, item, false);
                    mFirstStartBlank = (float) (item.getHeight() - mFirstChildHeight);
                }
              /*  Log.d("M","update mFirstChildHeight: "+mFirstChildHeight);
                Log.d("M","update mFirstStartBlank: "+mFirstStartBlank);*/
                
                blank = Math.max((int) (f * mFirstStartBlank), 1);
                lp = item.getLayoutParams();
                lp.height = mFirstChildHeight + blank;
                item.setLayoutParams(lp);
            }
            if (mSecondPos != mFirstPos) {
                item = getChildAt(mSecondPos - firstVis);
                if (item != null) {
                    if (mSecondChildHeight == -1) {
                        mSecondChildHeight = getChildHeight(mSecondPos, item, false);
                        mSecondStartBlank = (float) (item.getHeight() - mSecondChildHeight);
                        
                    }
               /*     Log.d("M","update mSecondChildHeight: "+mSecondChildHeight);
                    Log.d("M","update mSecondStartBlank: "+mSecondStartBlank);*/
                    blank = Math.max((int) (f * mSecondStartBlank), 1);
                    lp = item.getLayoutParams();
                    lp.height = mSecondChildHeight + blank;
                    item.setLayoutParams(lp);
                }
            }
        }

        @Override
        public void onStop() {
        	if (mAutoSortListener != null) {
    			mAutoSortListener.autoSort(mSrcPos - getHeaderViewsCount());
    		}
        	destroyFloatView();

            adjustOnReorder();
            clearPositions();

            // now the drag is done
            if (mInTouchEvent) {
                mDragState = STOPPED;
            } else {
                mDragState = IDLE;
            }
        }
    }

}
