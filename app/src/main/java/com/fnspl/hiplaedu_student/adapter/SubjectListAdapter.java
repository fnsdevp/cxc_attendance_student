package com.fnspl.hiplaedu_student.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.model.Subject;

import java.util.List;

/**
 * Created by FNSPL on 8/21/2017.
 */

public class SubjectListAdapter extends BaseAdapter {
    private Context mContext;
    private List<Subject> mList;
    private OnDrawableBrowseItemClickListener mListener;
    private int selectedSubjectPosition=0;

    // Constructor
    public SubjectListAdapter(Context c, List<Subject> mList) {
        mContext = c;
        this.mList = mList;
    }

    public int getCount() {
        return mList.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            grid = new View(mContext);
            grid = inflater.inflate(R.layout.subject, null);
        } else {
            grid = (View) convertView;
        }

        final LinearLayout rl_subject = (LinearLayout) grid.findViewById(R.id.rl_item);
        TextView tv_name = (TextView) grid.findViewById(R.id.tv_subject_name);

        Typeface custom_font = Typeface.createFromAsset(mContext.getAssets(), "fonts/futura_bk_bt.ttf");
        tv_name.setTypeface(custom_font);

        if(position==selectedSubjectPosition){
            rl_subject.setBackgroundColor(mContext.getResources().getColor(R.color.subject_selected));
        }else{
            rl_subject.setBackgroundColor(mContext.getResources().getColor(R.color.subject_normal));
        }

        tv_name.setText(mList.get(position).getName());

        rl_subject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_subject.setBackgroundColor(mContext.getResources().getColor(R.color.subject_selected));
                selectedSubjectPosition = position;

                if(mListener!=null){
                    mListener.onSubjectItemClick(position, mList.get(position));
                }
                notifyDataSetChanged();
            }
        });

        return grid;
    }

    public interface OnDrawableBrowseItemClickListener{
        void onSubjectItemClick(int position, Subject subject);
    }

    public void setOnDrawableForYouClickListener(OnDrawableBrowseItemClickListener mListenere){
        this.mListener = mListenere;
    }

    public void notifyDataChange(List<Subject> data){
        this.mList = data;
        notifyDataSetChanged();
    }

}