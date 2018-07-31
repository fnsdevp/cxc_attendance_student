package com.fnspl.hiplaedu_student.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;

/**
 * A linear layout that will contain views taken from an adapter. It differs from the list view in the fact that it will
 * not optimize anything and draw all the views from the adapter. It also does not provide scrolling. However, when you
 * need a layout that will render views horizontally and you know there are not many child views, this is a good
 * option.
 *
 * @author Vincent Mimoun-Prat @ MarvinLabs
 */
public class AdapterHorizontalLinearLayout extends LinearLayout {

    private Adapter adapter;
    private DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            reloadChildViews();
        }
    };
    private OnItemClickListener itemClickListener;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public AdapterHorizontalLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOrientation(LinearLayout.HORIZONTAL);
    }

    public AdapterHorizontalLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.HORIZONTAL);
    }

    public AdapterHorizontalLinearLayout(Context context) {
        super(context);
        setOrientation(LinearLayout.HORIZONTAL);
    }

    public void setAdapter(Adapter adapter) {

        try {

            if (this.adapter != null) {
                this.adapter.unregisterDataSetObserver(dataSetObserver);
            }


            if (this.adapter == adapter) return;
            this.adapter = adapter;
            if (adapter != null) adapter.registerDataSetObserver(dataSetObserver);
            reloadChildViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterObserver() {
        try {
            if (adapter != null)
                adapter.unregisterDataSetObserver(dataSetObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            if (adapter != null) adapter.unregisterDataSetObserver(dataSetObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reloadChildViews() {
        removeAllViews();

        if (adapter == null) return;

        int count = adapter.getCount();
        for (int position = 0; position < count; ++position) {
            View v = adapter.getView(position, null, this);
            if (v != null) {
                addView(v);
                if (null != this.itemClickListener) {
                    v.setClickable(true);
                    this.itemClickListener.onItemClick(v, position);
                }
            }
        }

        requestLayout();
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {

        this.itemClickListener = itemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
