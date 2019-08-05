package com.muheda.mdsearchview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.muheda.mdsearchview.model.SearchItem;
import com.muheda.mdsearchview.view.HistoryView;
import com.muheda.stateview.StateView;

import java.util.HashMap;

import searchcut.airr.searchview.R;

import static com.muheda.mdsearchview.helper.AssembleDataUtil.HISTORY_RECORD;

/**
 * @author wangfei
 * @date 2019/7/8.
 */
public class DataAdapter extends BaseRecyclerAdapter<SearchItem, DataAdapter.ViewHolder> {
    private HashMap<String, Class> configViewMap = new HashMap<>();

    public DataAdapter(int layout) {
        super(null, layout);
        initMap(null, null);
    }

    public void initMap(String key, Class view) {
        configViewMap.put(HISTORY_RECORD, HistoryView.class);
        if (!TextUtils.isEmpty(key) && view != null) {
            configViewMap.put(key, view);
        }
    }


    @Override
    protected DataAdapter.ViewHolder createMHDViewHolder(Context mContext, View itemView, int viewType) {
        return new ViewHolder(itemView, viewType + "");
    }

    @Override
    protected void bindDate(DataAdapter.ViewHolder holder, SearchItem searchItem, int position) {
        holder.mStateView.setData(String.valueOf(getItemViewType(position)), configViewMap, searchItem.getSearchDataDto());
    }

    @Override
    protected void itemClick(Context context, SearchItem searchItem) {

    }

    @Override
    public int getItemViewType(int position) {
        if (list == null || list.size() == 0) {
            return super.getItemViewType(position);
        } else {
            try {
                return Integer.valueOf(list.get(position).getmViewTag());
            } catch (Exception e) {
                return super.getItemViewType(position);
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private StateView mStateView;

        public ViewHolder(View itemView, String viewType) {
            super(itemView);
            initView(itemView, viewType);
        }

        private void initView(View itemView, String viewType) {
            mStateView = itemView.findViewById(R.id.sv_data);
        }
    }
}
