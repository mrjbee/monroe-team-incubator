package org.monroe.team.smooker.app.android.view;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import org.monroe.team.corebox.utils.DateUtils;

import java.util.Date;

public abstract class DateListAdapter extends BaseAdapter{

    private Date startDate;
    private Date endDate;

    public DateListAdapter(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public int getCount() {
        return calculateCount();
    }

    private int calculateCount() {
        long deltaMs = endDate.getTime() - startDate.getTime();
        return (int) DateUtils.asDays(deltaMs, false) +1;
    }

    @Override
    public Object getItem(int position) {
        return DateUtils.mathDays(startDate, position);
    }

    @Override
    public long getItemId(int position) {
        return DateUtils.mathDays(startDate, position).toString().hashCode();
    }

}
