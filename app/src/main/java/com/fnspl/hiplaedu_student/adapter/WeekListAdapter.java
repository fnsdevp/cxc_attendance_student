package com.fnspl.hiplaedu_student.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.model.Subject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by FNSPL on 8/21/2017.
 */

public class WeekListAdapter extends BaseAdapter {
    private Context mContext;
    private List<Subject> mList;
    private OnDrawableBrowseItemClickListener mListener;
    private int selectedSubjectPosition=0;
    private String[] weekDay = {"Mon","Tue","Wed","Thu","Fri"};
    private String[] weekDates = new String[5];

    // Constructor
    public WeekListAdapter(Context c, List<Subject> mList) {
        mContext = c;
        this.mList = mList;
        getAllWeekAddress();
    }

    public int getCount() {
        return weekDates.length;
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
            grid = inflater.inflate(R.layout.date_row, null);
        } else {
            grid = (View) convertView;
        }

        final RelativeLayout rl_subject = (RelativeLayout) grid.findViewById(R.id.rl_item);
        TextView tv_date_week = (TextView) grid.findViewById(R.id.tv_date_week);
        TextView tv_date_date = (TextView) grid.findViewById(R.id.tv_date_date);

        Typeface custom_font = Typeface.createFromAsset(mContext.getAssets(), "fonts/futura_bk_bt.ttf");
        tv_date_week.setTypeface(custom_font);
        tv_date_date.setTypeface(custom_font);

        tv_date_week.setText(weekDay[position]);
        tv_date_date.setText(weekDates[position]);

        if(position==selectedSubjectPosition){
            rl_subject.setBackgroundColor(mContext.getResources().getColor(R.color.date_selected));
        }else{
            rl_subject.setBackgroundColor(mContext.getResources().getColor(R.color.date_normal));
        }

       // tv_name.setText(mList.get(position).getSubjectName());

        rl_subject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_subject.setBackgroundColor(mContext.getResources().getColor(R.color.date_selected));
                selectedSubjectPosition = position;

                if(mListener!=null){
                    mListener.onSubjectItemClick(weekDates[position]);
                }
                notifyDataSetChanged();
            }
        });

        return grid;
    }

    public interface OnDrawableBrowseItemClickListener{
        void onSubjectItemClick(String date);
    }

    public void setOnDrawableForYouClickListener(OnDrawableBrowseItemClickListener mListenere){
        this.mListener = mListenere;
    }

    public void notifyDataChange(List<String> data){
        notifyDataSetChanged();
    }

    public void getAllWeekAddress(){

        Calendar c1 = Calendar.getInstance();

        for (int i = 1; i < 6; i++) {
            //first day of week
            c1.set(Calendar.DAY_OF_WEEK, i+1);

            int year1 = c1.get(Calendar.YEAR);
            int month1 = c1.get(Calendar.MONTH)+1;
            int day1 = c1.get(Calendar.DAY_OF_MONTH);

            weekDates[i-1]=year1+"-"+month1+"-"+day1;

            if(new SimpleDateFormat("yyyy-M-d").format(new Date()).equalsIgnoreCase(weekDates[i-1])){
                selectedSubjectPosition = i-1;
            }

        }

    }

}