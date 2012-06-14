package com.quanleimu.widget;


import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.quanleimu.activity.R;

public class PullToRefreshListView extends ListView implements OnScrollListener {

    private static final int TAP_TO_REFRESH = 1;
    private static final int PULL_TO_REFRESH = 2;
    private static final int RELEASE_TO_REFRESH = 3;
    private static final int REFRESHING = 4;
 
    private static final int SCROLLDOWN_TO_GETMORE = 5;
    private static final int GETTING_MORE = 6;
    
    private static final int DAY_MS = 24*60*60*1000;
    private static final int HOUR_MS = 60*60*1000;
    private static final int MINUTE_MS = 60*1000;
    private static final int SECOND_MS = 1000;

    private static final String TAG = "PullToRefreshListView";

    private OnRefreshListener mOnRefreshListener;
    private OnGetmoreListener mGetMoreListener;

    /**
     * Listener that will receive notifications every time the list scrolls.
     */
    private OnScrollListener mOnScrollListener;
    private LayoutInflater mInflater;

    private RelativeLayout mRefreshView;
    private TextView mRefreshViewText;
    private ImageView mRefreshViewImage;
    private ProgressBar mRefreshViewProgress;
    private TextView mRefreshViewLastUpdated;
    
    private RelativeLayout mGetmoreView;
    //private TextView mGetmoreViewText;

    private int mCurrentScrollState;
    private int mRefreshState;
    private int mGetMoreState = SCROLLDOWN_TO_GETMORE;

    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;

    private int mRefreshViewHeight;
    private int mRefreshOriginalTopPadding;
    private int mLastMotionY;

    private boolean mBounceHack;
    private boolean mTouchDown = false;
    private boolean mHasMore = true;
    
    private long mLastUpdateTimeMs;

    public PullToRefreshListView(Context context) {
        super(context);
        init(context);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        // Load all of the animations we need in code rather than through XML
        mFlipAnimation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(250);
        mFlipAnimation.setFillAfter(true);
        mReverseFlipAnimation = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(250);
        mReverseFlipAnimation.setFillAfter(true);

        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

//        RelativeLayout llHeaderFooter = (RelativeLayout) mInflater.inflate(R.layout.pull_to_refresh_header_footer, this, false);
//		mRefreshView = (RelativeLayout)mRefreshView.findViewById(R.id.pull_to_refresh_header); 
        
        mRefreshView = (RelativeLayout)mInflater.inflate(R.layout.pull_to_refresh_header, this, false);
        mRefreshViewText =
            (TextView) mRefreshView.findViewById(R.id.pull_to_refresh_text);
        mRefreshViewImage =
            (ImageView) mRefreshView.findViewById(R.id.pull_to_refresh_image);
        mRefreshViewProgress =
            (ProgressBar) mRefreshView.findViewById(R.id.pull_to_refresh_progress);
        mRefreshViewLastUpdated =
            (TextView) mRefreshView.findViewById(R.id.pull_to_refresh_updated_at);

        mRefreshViewImage.setMinimumHeight(50);
        mRefreshView.setOnClickListener(new OnClickRefreshListener());
        mRefreshOriginalTopPadding = mRefreshView.getPaddingTop();

        mRefreshState = TAP_TO_REFRESH;

        addHeaderView(mRefreshView);
        

        mGetmoreView = (RelativeLayout)mInflater.inflate(R.layout.pull_to_refresh_footer, this, false);
        //mGetmoreViewText = (TextView)mGetmoreView.findViewById(R.id.pulldown_to_getmore);
        addFooterView(mGetmoreView, null, true);

        super.setOnScrollListener(this);

        measureView(mRefreshView);
        mRefreshViewHeight = mRefreshView.getMeasuredHeight();
        
        mLastUpdateTimeMs = System.currentTimeMillis();
    }

    private void updateFooter(boolean hasMore){
    	mHasMore = hasMore;
    	if(mHasMore){
    		((TextView)mGetmoreView.findViewById(R.id.pulldown_to_getmore)).setText(R.string.scrolldown_to_getmore);
    	}else{
    		((TextView)mGetmoreView.findViewById(R.id.pulldown_to_getmore)).setText(R.string.scrolldown_to_getmore_nomore);
    	}
    }
    
    @Override
    protected void onAttachedToWindow() {
        setSelection(1);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);

        setSelection(1);
    }

    /**
     * Set the listener that will receive notifications every time the list
     * scrolls.
     * 
     * @param l The scroll listener. 
     */
    @Override
    public void setOnScrollListener(AbsListView.OnScrollListener l) {
        mOnScrollListener = l;
    }

    /**
     * Register a callback to be invoked when this list should be refreshed.
     * 
     * @param onRefreshListener The callback to run.
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }
    
    public void setOnGetMoreListener(OnGetmoreListener listener){
    	mGetMoreListener = listener; 
    }

    /**
     * Set a text to represent when the list was last updated. 
     * @param lastUpdated Last updated at.
     */
    public void checkLastUpdateTime() {    
    	long time_diff = System.currentTimeMillis() - mLastUpdateTimeMs;
    	long nDays = time_diff / DAY_MS;
    	time_diff %= DAY_MS;
    	long nHours = time_diff / HOUR_MS;
    	time_diff %= HOUR_MS;
    	long nMinutes = time_diff / MINUTE_MS;
    	time_diff %= MINUTE_MS;
    	long nSeconds = time_diff / SECOND_MS;
    	
    	String strLastUpdate = "最后更新于:";
    	if(nDays > 0){
    		strLastUpdate += nDays + "天";
    	}
    	if(nHours > 0){
    		strLastUpdate += nHours + "小时";
    	}
    	
   		strLastUpdate += nMinutes + "分";
    	
//   		strLastUpdate += nSeconds + "秒";
   		
    	strLastUpdate += "前";
    	
        mRefreshViewLastUpdated.setText(strLastUpdate);       
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int y = (int) event.getY();
        mBounceHack = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (!isVerticalScrollBarEnabled()) {
                    setVerticalScrollBarEnabled(true);
                }
                
                if (getFirstVisiblePosition() == 0 && mRefreshState != REFRESHING) {
                    if (mRefreshView.getTop() >= 0 && mRefreshState == RELEASE_TO_REFRESH){
                        // Initiate the refresh
                        mRefreshState = REFRESHING;
                        prepareForRefresh();
                        onRefresh();
                    } 
                    else if (mRefreshView.getBottom() < mRefreshViewHeight
                            || mRefreshView.getTop() <= 0) {
                        // Abort refresh and scroll down below the refresh view
                        resetHeader();
                        setSelection(1);
                    }
                }
                else if(this.getLastVisiblePosition() == this.getCount() - 1 && mGetMoreState != GETTING_MORE){
//                	int bottom1 = mGetmoreView.getBottom();
//                	int bottom2 = this.getBottom();
                    if ((mGetmoreView.getTop() + mGetmoreView.getHeight() *2 / 5 < this.getBottom())) {
                        // Initiate the refresh
                    	mGetMoreState = GETTING_MORE;                       
                        onGetMore();
                    }
                }
                mTouchDown = false;               
                
                break;
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = y;
                mTouchDown = true;
                break;
            case MotionEvent.ACTION_MOVE:
                applyHeaderPadding(event);
                mTouchDown = true;
                break;
        }
        
        return super.onTouchEvent(event);
    }

    private void applyHeaderPadding(MotionEvent ev) {
        // getHistorySize has been available since API 1
        int pointerCount = ev.getHistorySize();

        for (int p = 0; p < pointerCount; p++) {
            if (mRefreshState == RELEASE_TO_REFRESH) {
                if (isVerticalFadingEdgeEnabled()) {
                    setVerticalScrollBarEnabled(false);
                }

                int historicalY = (int) ev.getHistoricalY(p);

                // Calculate the padding to apply, we divide by 1.7 to
                // simulate a more resistant effect during pull.
                int topPadding = (int) (((historicalY - mLastMotionY)
                        - mRefreshViewHeight) / 1.7);

                mRefreshView.setPadding(
                        mRefreshView.getPaddingLeft(),
                        topPadding,
                        mRefreshView.getPaddingRight(),
                        mRefreshView.getPaddingBottom());
            }
        }
    }

    /**
     * Sets the header padding back to original size.
     */
    private void resetHeaderPadding() {
        mRefreshView.setPadding(
                mRefreshView.getPaddingLeft(),
                mRefreshOriginalTopPadding,
                mRefreshView.getPaddingRight(),
                mRefreshView.getPaddingBottom());
    }

    /**
     * Resets the header to the original state.
     */
    private void resetHeader() {
        if (mRefreshState != TAP_TO_REFRESH) {
            mRefreshState = TAP_TO_REFRESH;

            resetHeaderPadding();

            // Set refresh view text to the pull label
            mRefreshViewText.setText(R.string.pull_to_refresh_tap_label);
            // Replace refresh drawable with arrow drawable
            mRefreshViewImage.setImageResource(R.drawable.ic_pulltorefresh_arrow);
            // Clear the full rotation animation
            mRefreshViewImage.clearAnimation();
            // Hide progress bar and arrow.
            mRefreshViewImage.setVisibility(View.GONE);
            mRefreshViewProgress.setVisibility(View.GONE);
            
        }
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0,
                0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        // When the refresh view is completely visible, change the text to say
        // "Release to refresh..." and flip the arrow drawable.
        if (mCurrentScrollState == SCROLL_STATE_TOUCH_SCROLL
                && mRefreshState != REFRESHING) {
            if (firstVisibleItem == 0) {
                mRefreshViewImage.setVisibility(View.VISIBLE);
                if ((mRefreshView.getBottom() >= mRefreshViewHeight + 20
                        || mRefreshView.getTop() >= 0)
                        && mRefreshState != RELEASE_TO_REFRESH) {
                    mRefreshViewText.setText(R.string.pull_to_refresh_release_label);
                    mRefreshViewImage.clearAnimation();
                    mRefreshViewImage.startAnimation(mFlipAnimation);
                    checkLastUpdateTime();
                    mRefreshState = RELEASE_TO_REFRESH;
                }
                else if (mRefreshView.getBottom() < mRefreshViewHeight + 20
                        && mRefreshState != PULL_TO_REFRESH) {
                    mRefreshViewText.setText(R.string.pull_to_refresh_pull_label);
                    if (mRefreshState != TAP_TO_REFRESH) {
                        mRefreshViewImage.clearAnimation();
                        mRefreshViewImage.startAnimation(mReverseFlipAnimation);
                    }
                    checkLastUpdateTime();
                    mRefreshState = PULL_TO_REFRESH;
                }
            } else {
                mRefreshViewImage.setVisibility(View.GONE);
                resetHeader();
                
//                if(firstVisibleItem+visibleItemCount==totalItemCount
//                		&& mGetmoreView.getBottom() < this.getBottom() + 5){
//                	mRefreshState = SCROLLDOWN_TO_GETMORE;
//                }
            }
        }/* else if (mCurrentScrollState == SCROLL_STATE_FLING
                && firstVisibleItem == 0
                && mRefreshState != REFRESHING) {
            setSelection(1);
            mBounceHack = true;
        } else if (mBounceHack && mCurrentScrollState == SCROLL_STATE_FLING) {
            setSelection(1);
        }*/else if(mCurrentScrollState == SCROLL_STATE_FLING){
        	
            if (getLastVisiblePosition() == getCount() - 1 
            		&& mGetMoreState != GETTING_MORE 
            		&&  mGetmoreView.getBottom() <= this.getBottom()) {
                // Initiate the refresh
            	mGetMoreState = GETTING_MORE;                       
                onGetMore();
            }            
            else if (firstVisibleItem == 0) {
            	Log.d("on fling: ", "top y = " + mRefreshView.getTop());
            	if(mRefreshState != REFRESHING && mRefreshView.getTop() >= 0){
	                mRefreshState = REFRESHING;
	                checkLastUpdateTime();
	                prepareForRefresh();
	                onRefresh();
            	}
            	else if(mRefreshView.getTop() < 0){
                    mRefreshViewText.setText(R.string.pull_to_refresh_pull_label);
//                    if (mRefreshState != TAP_TO_REFRESH) {
//                        mRefreshViewImage.clearAnimation();
//                        mRefreshViewImage.startAnimation(mReverseFlipAnimation);
//                    }
                    mRefreshState = PULL_TO_REFRESH;
            	}
            	
            	mBounceHack = true;
            }
        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem,
                    visibleItemCount, totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mCurrentScrollState = scrollState;

        if (mCurrentScrollState == SCROLL_STATE_IDLE) {            
        	Log.d("on fling: ", "onScrollStateChanged to SCROLL_STATE_IDLE, mRefreshState="+mRefreshState);
        	
            if(mBounceHack && !mTouchDown &&mRefreshState == PULL_TO_REFRESH){
                //resetHeader();
                setSelection(1);
                mBounceHack = false;
            }           
        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    public void prepareForRefresh() {
        resetHeaderPadding();

        mRefreshViewImage.setVisibility(View.GONE);
        // We need this hack, otherwise it will keep the previous drawable.
        mRefreshViewImage.setImageDrawable(null);
        mRefreshViewProgress.setVisibility(View.VISIBLE);

        // Set refresh view text to the refreshing label
        mRefreshViewText.setText(R.string.pull_to_refresh_refreshing_label);

        mRefreshState = REFRESHING;
    }

    public void onRefresh() {
        Log.d(TAG, "onRefresh");

        if (mOnRefreshListener != null) {
            mOnRefreshListener.onRefresh();
        }
    }
    
    public void onGetMore(){
        Log.d(TAG, "onRefresh");

        if (mHasMore && mGetMoreListener != null) {
        	mGetMoreListener.onGetMore();
        }else{
        	mGetMoreState = SCROLLDOWN_TO_GETMORE;  
        }
    }

    /**
     * Resets the list to a normal state after a refresh.
     * @param lastUpdated Last updated at.
     */
//    public void onRefreshComplete(CharSequence lastUpdated) {
//        setLastUpdated(lastUpdated);
//        onRefreshComplete();
//    }

    /**
     * Resets the list to a normal state after a refresh.
     */
    public void onRefreshComplete() {        
        Log.d(TAG, "onRefreshComplete");
        
        mLastUpdateTimeMs = System.currentTimeMillis();

        resetHeader();

        // If refresh view is visible when loading completes, scroll down to
        // the next item.
        if (mRefreshView.getBottom() > 0) {
            invalidateViews();
            if(mRefreshView.getTop() >= 0)
            	setSelection(1);
        }
        
        updateFooter(true);        
    }
    
	public enum E_GETMORE{
		E_GETMORE_OK,
		E_GETMORE_NO_MORE
	};
    public void onGetMoreCompleted(E_GETMORE status){
    	boolean hasMore = true;
    	switch(status){
    	case E_GETMORE_OK:	    	
	    	invalidateViews();
	    	break;
    	case E_GETMORE_NO_MORE:
    		hasMore = false;
    		break;
    	}
    	
    	mGetMoreState = SCROLLDOWN_TO_GETMORE;
    	
    	updateFooter(hasMore);
    }

    /**
     * Invoked when the refresh view is clicked on. This is mainly used when
     * there's only a few items in the list and it's not possible to drag the
     * list.
     */
    private class OnClickRefreshListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (mRefreshState != REFRESHING) {
                prepareForRefresh();
                onRefresh();
            }
        }

    }

    /**
     * Interface definition for a callback to be invoked when list should be
     * refreshed.
     */
    public interface OnRefreshListener {
        /**
         * Called when the list should be refreshed.
         * <p>
         * A call to {@link PullToRefreshListView #onRefreshComplete()} is
         * expected to indicate that the refresh has completed.
         */
        public void onRefresh();
    }
    
    
    public interface OnGetmoreListener{
    	public void onGetMore();
    }
}