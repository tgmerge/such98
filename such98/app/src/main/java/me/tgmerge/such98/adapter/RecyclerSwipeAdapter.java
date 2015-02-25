package me.tgmerge.such98.adapter;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;

import me.tgmerge.such98.util.XMLUtil;

// T: type of XML data object
// V: type of ViewHolder
abstract public class RecyclerSwipeAdapter<T extends XMLUtil.XMLObj, V extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<V> {

    abstract public void setSwipeLayout(SwipeRefreshLayout swipeLayout);

    abstract public void appendData(XMLUtil.ArrayOf<T> data);

    abstract public void appendDataFront(XMLUtil.ArrayOf<T> data);
}
