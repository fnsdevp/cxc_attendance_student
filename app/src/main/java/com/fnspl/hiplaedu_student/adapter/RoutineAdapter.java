package com.fnspl.hiplaedu_student.adapter;

/**
 * Created by User on 8/3/2017.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.model.RoutinePeriod;

import java.util.List;


public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.ViewHolder> {
    private List<RoutinePeriod> values;
    private Context context ;
    private OnProductClickListener mListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tv_teacher_name, tv_subject, tv_start_time, tv_end_time;
        public ImageView iv_navigation;

        public View layout;

        public ViewHolder(View v) {
            super(v);

            layout = v;
            tv_teacher_name = (TextView) v.findViewById(R.id.tv_teacher_name);
            tv_subject = (TextView) v.findViewById(R.id.tv_subject);
            tv_start_time = (TextView) v.findViewById(R.id.tv_start_time);
            tv_end_time = (TextView) v.findViewById(R.id.tv_end_time);
            iv_navigation = (ImageView) v.findViewById(R.id.iv_navigation);

            Typeface custom_font = Typeface.createFromAsset(context.getAssets(), "fonts/futura_md_bt.ttf");
            tv_teacher_name.setTypeface(custom_font);
            tv_start_time.setTypeface(custom_font);
            tv_end_time.setTypeface(custom_font);
            tv_subject.setTypeface(custom_font);
        }
    }

    public void notifyDataChange(List<RoutinePeriod> data) {
        this.values = data ;
        notifyDataSetChanged();
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RoutineAdapter(Context context, List<RoutinePeriod> myDataset) {
        this.context = context ;
        values = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RoutineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v =
                inflater.inflate(R.layout.item_routine, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.tv_teacher_name.setText(""+values.get(position).getTeacher_details().getName());
        holder.tv_subject.setText(""+values.get(position).getSubject_name());
        holder.tv_start_time.setText(" "+values.get(position).getStartTime());
        holder.tv_end_time.setText(" "+values.get(position).getEndTime());

        holder.iv_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mListener!=null){
                    mListener.onNavigate(values.get(position),position);
                }

            }
        });

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mListener!=null){
                    mListener.onRoutineClick(values.get(position),position);
                }

            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }

    public interface OnProductClickListener{
        void onNavigate(RoutinePeriod routinePeriod, int position);
        void onRoutineClick(RoutinePeriod routinePeriod, int position);
    }

    public void setOnProductClickListener(OnProductClickListener mListener){
        this.mListener = mListener;
    }
}