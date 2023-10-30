package com.wwj.customedview.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;

/**
 * @author WWJ
 * @detail: 自定义的流式布局，可以根据宽度自动换行
 * @date: 2023/10/30 11:05
 */
public class FlowLayout extends ViewGroup {

    private static final String TAG = "FlowLayout";

    private static final int HORIZON_SPACING = dp2px(10);

    private static final int VERTICAL_SPACING = dp2px(4);

    private List<List<View>> mAllLineViews;

    private List<Integer> mSingleLineMaxHeight;

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initList() {
        if (mAllLineViews == null) {
            mAllLineViews = new LinkedList<>();
        } else {
            mAllLineViews.clear();
        }

        if (mSingleLineMaxHeight == null) {
            mSingleLineMaxHeight = new LinkedList<>();
        } else {
            mSingleLineMaxHeight.clear();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        initList();
        int childCount = getChildCount();

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        // 本viewGroup通过子View计算得到的，需要的宽高
        int viewGroupNeedWidth = 0, viewGroupNeedHeight = 0;

        // 本viewGroup能过获取到的宽高，是从本viewGroup在xml中的父布局中推算出来的
        int viewGroupCanGetWidth = MeasureSpec.getSize(widthMeasureSpec);
        int viewGroupCanGetHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 当前行已经使用的宽度
        int currentLineWidth = 0;
        // 当前行最大的使用高度
        int currentLineMaxHeight = 0;

        // 本行的子view
        List<View> currentLineView = new LinkedList<>();

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            LayoutParams childViewLayoutParams = childView.getLayoutParams();

            int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, paddingLeft + paddingRight, childViewLayoutParams.width);
            int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, paddingTop + paddingBottom, childViewLayoutParams.height);

            // 度量子View
            childView.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            // 获取子View的宽高，推测本viewGroup的宽高
            int childViewMeasuredWidth = childView.getMeasuredWidth();
            int childViewMeasuredHeight = childView.getMeasuredHeight();

            Log.d(TAG, "index:" + i + " view's width: " + childViewMeasuredWidth + ", height: " + childViewMeasuredHeight);

            if (currentLineWidth + childViewMeasuredWidth + HORIZON_SPACING > viewGroupCanGetWidth) {
                Log.i(TAG, "get spacing max");
                // 当需要换行时
                currentLineWidth = currentLineWidth + HORIZON_SPACING;

                viewGroupNeedWidth = Math.max(viewGroupNeedWidth, currentLineWidth);
                viewGroupNeedHeight = viewGroupNeedHeight + currentLineMaxHeight + VERTICAL_SPACING;
                Log.i(TAG, "spacing max get width: " + viewGroupNeedWidth + ", get height: " + viewGroupNeedHeight);

                mAllLineViews.add(currentLineView);
                mSingleLineMaxHeight.add(currentLineMaxHeight);

                // 清空每行的数据
                currentLineView = new LinkedList<>();
                currentLineWidth = 0;
                currentLineMaxHeight = 0;
            }

            // 当不需要换行时
            currentLineView.add(childView);
            currentLineWidth = currentLineWidth + childViewMeasuredWidth + HORIZON_SPACING;
            currentLineMaxHeight = Math.max(currentLineMaxHeight, childViewMeasuredHeight);
            Log.d(TAG, "current line width: " + currentLineWidth + ", currentLineMaxHeight: " + currentLineMaxHeight);
        }

        Log.i(TAG, "get viewGroup need width: " + viewGroupNeedWidth + ", need height: " + viewGroupNeedHeight + " after loop");

        // 当仅有1行或最后1不足以换行时，不会进换行逻辑，此时 currentLineWidth 和 currentLineMaxHeight 还需要比较或累加最后一行的宽高
        mAllLineViews.add(currentLineView);
        mSingleLineMaxHeight.add(currentLineMaxHeight);
        viewGroupNeedWidth = Math.max(viewGroupNeedWidth, currentLineWidth);
        viewGroupNeedHeight = viewGroupNeedHeight + currentLineMaxHeight + VERTICAL_SPACING;

        int viewGroupWidthMeasureMode = MeasureSpec.getMode(widthMeasureSpec);
        int viewGroupHeightMeasureMode = MeasureSpec.getMode(heightMeasureSpec);

        int realWidth = viewGroupWidthMeasureMode == MeasureSpec.EXACTLY ? viewGroupCanGetWidth : viewGroupNeedWidth;
        int realHeight = viewGroupHeightMeasureMode == MeasureSpec.EXACTLY ? viewGroupCanGetHeight : viewGroupNeedHeight;

        Log.d(TAG, "on measure dimension width: " + realWidth + ", height: " + realHeight);

        // 最后度量自己的宽高
        setMeasuredDimension(realWidth, realHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int lineCount = mAllLineViews.size();
        Log.d(TAG, "lineCount: " + lineCount);

        int currentLeft = getPaddingLeft();
        int currentTop = getPaddingTop();

        for (int i = 0; i < lineCount; i++) {
            List<View> lineViews = mAllLineViews.get(i);
            Log.d(TAG, "single line view's count: " + lineViews.size());

            for (View singleChildView : lineViews) {
                int left = currentLeft;
                int top = currentTop;
                int right = currentLeft + singleChildView.getMeasuredWidth();
                int bottom = currentTop + singleChildView.getMeasuredHeight();

                singleChildView.layout(left, top, right, bottom);

                // 同一行的view在layout完后，left向右移动 初始left+自身宽度+水平间隔的距离
                currentLeft = left + singleChildView.getMeasuredWidth() + HORIZON_SPACING;
            }

            // 换了行之后
            // left回到起点
            currentLeft = getPaddingLeft();
            // top向下移动 当前top+行最大高度+竖直间隔的距离
            currentTop = currentTop + mSingleLineMaxHeight.get(i) + VERTICAL_SPACING;
        }
    }

    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }
}
