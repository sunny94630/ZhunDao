package com.zhaohe.zhundao.ui.home.find;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhaohe.app.utils.IntentUtils;
import com.zhaohe.zhundao.R;


/**
 * @Description:
 * @Author:邹苏隆
 * @Since:2016/11/29 10:24
 */
public class FindFragment extends Fragment implements View.OnClickListener {
    protected View rootView;
    private TextView tv_find_custom;
    private TextView tv_find_shake;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = getLayoutInflater(null).inflate(R.layout.fragment_find,
                null);

        initView(rootView);
//        test();
    }

    protected void initView(View rootView) {
        tv_find_custom = (TextView) rootView.findViewById(R.id.tv_find_custom);
        tv_find_custom.setOnClickListener(this);
        tv_find_shake = (TextView) rootView.findViewById(R.id.tv_find_shake);
        tv_find_shake.setOnClickListener(this);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 在使用这个view之前首先判断其是否存在parent view，这调用getParent()方法可以实现。
        // 如果存在parent view，那么就调用removeAllViewsInLayout()方法
        Log.i("test", "AFragment-->onCreateView");
        ViewGroup perentView = (ViewGroup) rootView.getParent();
        if (perentView != null) {
            perentView.removeAllViewsInLayout();
        }
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_find_custom:
                IntentUtils.startActivity(getActivity(), CustomActivity.class);
                break;
            case R.id.tv_find_shake:
                IntentUtils.startActivity(getActivity(), BeaconListActivity.class);
                break;
        }

    }
}

