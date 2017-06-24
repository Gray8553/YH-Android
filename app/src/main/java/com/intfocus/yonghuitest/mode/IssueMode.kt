package com.intfocus.yonghuitest.mode

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.intfocus.yonghuitest.bean.dashboard.*
import com.intfocus.yonghuitest.util.HttpUtil
import com.zbl.lib.baseframe.core.AbstractMode
import com.zbl.lib.baseframe.utils.StringUtil
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

/**
 * Created by liuruilin on 2017/6/11.
 */
class IssueMode(ctx: Context) : AbstractMode() {
    lateinit var urlString: String
    var result: String? = null
    val mIssueSP: SharedPreferences = ctx.getSharedPreferences("IssueList", Context.MODE_PRIVATE)
    var mIssueListBean: UserInfoBean? = null
    var mIssueListBeanString: String? = null
    var gson = Gson()

    fun getUrl(): String {
        var url = "http://development.shengyiplus.com/api/v1/user/123456/page/1/limit/10/problems"
        return url
    }

    override fun requestData() {
        Thread(Runnable {
            urlString = getUrl()
            if (!urlString.isEmpty()) {
                val response = HttpUtil.httpGet(urlString, HashMap<String, String>())
                result = response["body"]
                if (StringUtil.isEmpty(result)) {
                    val result1 = IssueListRequest(false, 400)
                    EventBus.getDefault().post(result1)
                    return@Runnable
                }
                analysisData(result)
            } else {
                val result1 = IssueListRequest(false, 400)
                EventBus.getDefault().post(result1)
                return@Runnable
            }
        }).start()
    }

    /**
     * 解析数据
     * @param result
     */
    private fun analysisData(result: String?): IssueListRequest {
        try {
            val jsonObject = JSONObject(result)
            if (jsonObject.has("code")) {
                val code = jsonObject.getInt("code")
                if (code != 200) {
                    val result1 = IssueListRequest(false, code)
                    EventBus.getDefault().post(result1)
                    return result1
                }
            }

            var resultStr = jsonObject.toString()
            mIssueSP.edit().putString("IssueList", resultStr).commit()
            var issueListBean = gson.fromJson(resultStr, IssueListBean::class.java)
            val result1 = IssueListRequest(true, 200)
            result1.issueList = issueListBean
            EventBus.getDefault().post(result1)
            return result1
        } catch (e: JSONException) {
            e.printStackTrace()
            val result1 = IssueListRequest(false, -1)
            EventBus.getDefault().post(result1)
        }

        val result1 = IssueListRequest(false, 0)
        EventBus.getDefault().post(result1)
        return result1
    }
}