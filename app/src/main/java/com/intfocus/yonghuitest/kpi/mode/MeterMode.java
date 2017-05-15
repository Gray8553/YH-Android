package com.intfocus.yonghuitest.kpi.mode;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.asm.Label;
import com.intfocus.yonghuitest.kpi.entity.MererEntity;
import com.intfocus.yonghuitest.kpi.entity.msg.MeterRequestResult;
import com.intfocus.yonghuitest.kpi.utils.FileUtil;
import com.intfocus.yonghuitest.util.HttpUtil;
import com.intfocus.yonghuitest.util.K;
import com.intfocus.yonghuitest.util.URLs;
import com.zbl.lib.baseframe.core.AbstractMode;
import com.zbl.lib.baseframe.utils.StringUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.intfocus.yonghuitest.util.URLs.kGroupId;

/**
 * 仪表盘-数据处理模块
 * Created by zbaoliang on 17-4-28.
 */
public class MeterMode extends AbstractMode {

    Context ctx;
    String urlString;
    JSONObject user;

    public MeterMode(Context ctx) {
        this.ctx = ctx;
    }

    public String getKpiUrl() {
        String url;
        try {
            String userConfigPath = String.format("%s/%s", com.intfocus.yonghuitest.util.FileUtil.basePath(ctx), K.kUserConfigFileName);
            if ((new File(userConfigPath)).exists()) {
                user = com.intfocus.yonghuitest.util.FileUtil.readConfigFile(userConfigPath);
            }
            String currentUIVersion = URLs.currentUIVersion(ctx);
            url = String.format(K.kKPIMobilePath, K.kBaseUrl, currentUIVersion, user.getString(
                    kGroupId), user.getString(URLs.kRoleId));
        } catch (JSONException e) {
            url = null;
            e.printStackTrace();
        }
        return url;
    }

    @Override
    public void requestData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                urlString = getKpiUrl();
                if (!urlString.isEmpty()) {
                    Map<String, String> response = HttpUtil.httpGet(urlString, new HashMap<String, String>());
                    String result = response.get("body");
                    if (StringUtil.isEmpty(result)) {
                        MeterRequestResult result1 = new MeterRequestResult(true, 400);
                        EventBus.getDefault().post(result1);
                        return;
                    }
                    analysisData(result);
                }
                else {
                    MeterRequestResult result1 = new MeterRequestResult(true, 400);
                    EventBus.getDefault().post(result1);
                    return;
                }

            }
        }).start();
    }

    /**
     * 解析数据
     *
     * @param result
     */
    private MeterRequestResult analysisData(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.has("code")) {
                int code = jsonObject.getInt("code");
                if (code != 200) {
                    MeterRequestResult result1 = new MeterRequestResult(true, code);
                    EventBus.getDefault().post(result1);
                    return result1;
                }
            }

            if (jsonObject.has("data")) {
                String data = jsonObject.getString("data");
                data = data.replace("null", "0");
                ArrayList<MererEntity> datas = (ArrayList<MererEntity>) JSON.parseArray(data, MererEntity.class);
                ArrayList<MererEntity> topData = new ArrayList<>();
                Iterator<MererEntity> iterator = datas.iterator();
                while (iterator.hasNext()) {
                    MererEntity entity = iterator.next();
                    if (entity.is_stick) {
                        topData.add(entity);
                        iterator.remove();
                    }
                }
                MeterRequestResult result1 = new MeterRequestResult(true, 200);
                result1.setTopDatas(topData);
                result1.setBodyDatas(datas);
                EventBus.getDefault().post(result1);
                return result1;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            MeterRequestResult result1 = new MeterRequestResult(true, -1);
            EventBus.getDefault().post(result1);
        }
        MeterRequestResult result1 = new MeterRequestResult(true, 0);
        EventBus.getDefault().post(result1);
        return result1;
    }
}
