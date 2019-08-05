package com.muheda.mdsearchview.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.donkingliang.labels.LabelsView;
import com.muheda.mdsearchview.icallback.ICallBack;
import com.muheda.mdsearchview.model.SearchDataDto;
import com.muheda.stateview.BaseView;


import java.util.HashMap;

import searchcut.airr.searchview.R;
import searchcut.airr.searchview.databinding.ItemDataBinding;


/**
 * @author wangfei
 * @date 2019/7/29.
 */
public class HistoryView extends BaseView<SearchDataDto, ItemDataBinding> {
    private ICallBack mCallBack;

    public HistoryView(Context context, Object data, ViewGroup parent) {
        super(context, data, parent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_data;
    }

    @Override
    protected void initView(View view, boolean isUpdate) {
        if (data == null) {
            return;
        }
        mBinding.lvData.setLabels(data.getData());
        if (null != data.getOnClickListener()) {
            mBinding.ivDelete.setOnClickListener(data.getOnClickListener());
            mBinding.ivDelete.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void initListener(View view, boolean isUpdate) {
        if (data == null) {
            return;
        }
        mCallBack = data.getOnCallBackListener();
        mBinding.lvData.setOnLabelClickListener(new LabelsView.OnLabelClickListener() {
            @Override
            public void onLabelClick(TextView label, Object data, int position) {
                searchAciton((String) data);
            }
        });
    }

    @Override
    protected ConbinationBuilder combinationViewBuilder() {
        return null;
    }

    @Override
    protected void initViewConfigure(HashMap viewConfigure) {

    }
}
