package com.intfocus.yonghuitest.kpi.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.intfocus.yonghuitest.R;
import com.intfocus.yonghuitest.kpi.entity.MererEntity;
import com.intfocus.yonghuitest.kpi.entity.MeterClickEventEntity;
import com.intfocus.yonghuitest.kpi.view.RingChart;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;


/**
 * 环形统计图
 */
public class RingFragment extends Fragment {
    private MererEntity entity;

    private View rootView;

    @ViewInject(R.id.ll_ragment_ring)
    private View ll_ragmentlayout;

    @ViewInject(R.id.tv_title_vpitem)
    private TextView tv_title;

    @ViewInject(R.id.ringChart)
    RingChart ringChart;

    public RingFragment() {
    }

    public static RingFragment newInstance(MererEntity entity) {
        RingFragment fragment = new RingFragment();
        Bundle args = new Bundle();
        args.putSerializable("Entity", entity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            entity = (MererEntity) getArguments().getSerializable("Entity");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_ring, container, false);
            x.view().inject(this, rootView);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    init();
                }
            }, 100);
        }
        return rootView;
    }

    private void init() {
        tv_title.setText(entity.title);
        MererEntity.LineEntity.HighLight highLight = entity.data.high_light;
        ringChart.setMaxProgress(highLight.compare);
        ringChart.setShowProgress(highLight.number);
        String unit;
        if (highLight.percentage)
            unit = "%";
        else
            unit = entity.unit;
        ringChart.setUnit(unit);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MeterClickEventEntity(entity));
            }
        });
    }
}