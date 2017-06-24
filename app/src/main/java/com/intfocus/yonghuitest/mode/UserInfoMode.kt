package com.intfocus.yonghuitest.mode

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.intfocus.yonghuitest.bean.dashboard.UserInfoBean
import com.intfocus.yonghuitest.bean.dashboard.UserInfoRequest
import com.intfocus.yonghuitest.constant.Urls
import com.intfocus.yonghuitest.util.*
import com.intfocus.yonghuitest.util.K.*
import com.zbl.lib.baseframe.core.AbstractMode
import com.zbl.lib.baseframe.utils.StringUtil
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.experimental.EmptyCoroutineContext.plus

/**
 * Created by liuruilin on 2017/6/7.
 */
class UserInfoMode(var ctx: Context) : AbstractMode() {
    lateinit var urlString: String
    var result: String? = null
    val mSharedPreferences: SharedPreferences = ctx.getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
    var mUserSP = ctx.getSharedPreferences("UserBean", Context.MODE_PRIVATE)
    var gson = Gson()

    fun getUrl(): String {
        var url = String.format(K.KUserInfoPath, K.kBaseUrl, mUserSP.getString(URLs.kUserNum, ""), mUserSP.getInt(URLs.kGroupId, 0).toString(), mUserSP.getInt(URLs.kRoleId, 0).toString())
        return url
    }

    override fun requestData() {
        Thread(Runnable {
            urlString = getUrl()
            if (!urlString.isEmpty()) {
                val response = HttpUtil.httpGet(urlString, HashMap<String, String>())
                result = response["body"]
                if (StringUtil.isEmpty(result)) {
                    val result1 = UserInfoRequest(true, 400)
                    EventBus.getDefault().post(result1)
                    return@Runnable
                }
                analysisData(result)
            } else {
                val result1 = UserInfoRequest(true, 400)
                EventBus.getDefault().post(result1)
                return@Runnable
            }
        }).start()
    }

    /**
     * 解析数据
     * @param result
     */
    private fun analysisData(result: String?): UserInfoRequest {
        try {
            val jsonObject = JSONObject(result)
            if (jsonObject.has("code")) {
                val code = jsonObject.getInt("code")
                if (code != 200) {
                    val result1 = UserInfoRequest(true, code)
                    EventBus.getDefault().post(result1)
                    return result1
                }
            }

            mSharedPreferences.edit().putString("UserInfo", jsonObject.toString()).commit()
            var userInfo = gson.fromJson(jsonObject.toString(), UserInfoBean::class.java)
            val result1 = UserInfoRequest(true, 200)
            result1.userInfoBean = userInfo
            EventBus.getDefault().post(result1)
            return result1
        } catch (e: JSONException) {
            e.printStackTrace()
            val result1 = UserInfoRequest(true, -1)
            EventBus.getDefault().post(result1)
        }

        val result1 = UserInfoRequest(true, 0)
        EventBus.getDefault().post(result1)
        return result1
    }

    fun uplodeUserIcon(bitmap: Bitmap, imgPath: String) {
        Thread(Runnable {
            var format = SimpleDateFormat("yyyyMMddHHmmss")
            var date = Date(System.currentTimeMillis())
            var iconUpdateUrlString = String.format(K.kUploadGravatarAPIPath, PrivateURLs.kBaseUrl, mUserSP.getString(kUserDeviceId, ""), mUserSP.getInt(kUserId, 0).toString())
            File(imgPath).delete()
            var gravatarImgPath = FileUtil.dirPath(ctx, K.kConfigDirName, K.kAppCode + "_" + mUserSP.getString(URLs.kUserNum, "") + "_" + format.format(date) + ".jpg")
//            var gravatarFileName = gravatarImgPath.substring(gravatarImgPath.lastIndexOf("/") + 1, gravatarImgPath.length)
            FileUtil.saveImage(gravatarImgPath, bitmap)
            var response = HttpUtil.httpPostFile(iconUpdateUrlString, "image/jpg", "gravatar", gravatarImgPath)
            Log.i("testlog", response.toString())
        }).start()
    }
}