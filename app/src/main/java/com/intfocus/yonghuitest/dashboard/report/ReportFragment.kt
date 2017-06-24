package com.intfocus.yonghuitest.dashboard.report

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.intfocus.yonghuitest.R
import com.intfocus.yonghuitest.dashboard.report.adapter.ReportsLeftListAdapter
import com.intfocus.yonghuitest.dashboard.report.adapter.ReportsRightGVAdapter
import com.intfocus.yonghuitest.dashboard.report.adapter.ReportsRightRVAdapter
import com.intfocus.yonghuitest.base.BaseModeFragment
import com.intfocus.yonghuitest.bean.dashboard.CategoryBean
import com.intfocus.yonghuitest.bean.dashboard.ReportListPageRequest
import com.intfocus.yonghuitest.mode.ReportsListMode
import com.intfocus.yonghuitest.subject.HomeTricsActivity
import com.intfocus.yonghuitest.subject.SubjectActivity
import com.intfocus.yonghuitest.subject.TableActivity
import com.intfocus.yonghuitest.util.K
import com.intfocus.yonghuitest.util.URLs
import com.intfocus.yonghuitest.util.URLs.kGroupId
import com.zbl.lib.baseframe.core.Subject
import kotlinx.android.synthetic.main.fragment_reports.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException

/**
 * Created by liuruilin on 2017/6/15.
 */
class ReportFragment: BaseModeFragment<ReportsListMode>(), ReportsLeftListAdapter.ReportLeftListListener, ReportsRightGVAdapter.ItemListener{
    lateinit var ctx: Context
    var rootView : View? = null
    var datas: List<CategoryBean>? = null
    lateinit var reportsRightAdapter: ReportsRightRVAdapter
    lateinit var reportsLeftAdapter: ReportsLeftListAdapter

    override fun setSubject(): Subject {
        ctx = act.applicationContext
        return ReportsListMode(ctx, "reports")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        EventBus.getDefault().register(this)
        if (rootView == null) {
            rootView = inflater!!.inflate(R.layout.fragment_reports, container, false)
            model.requestData()
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun initView(requestReport: ReportListPageRequest) {
        if (requestReport.isSuccess) {
            datas = requestReport.categroy_list
            reportsLeftAdapter = ReportsLeftListAdapter(ctx, datas, this)
            ll_reports_category_list.adapter = reportsLeftAdapter
            val mLayoutManager = LinearLayoutManager(ctx)
            mLayoutManager.orientation = LinearLayoutManager.VERTICAL
            rv_reports_group_list.layoutManager = mLayoutManager
            reportsRightAdapter = ReportsRightRVAdapter(ctx, datas!![0].data, this)
            rv_reports_group_list.adapter = reportsRightAdapter
        }
    }

    override fun reportLeftItemClick(sign: ImageView, position: Int) {
        reportsRightAdapter.setData(datas!![position].data)
        reportsLeftAdapter.refreshListItemState(position)
    }

    override fun reportItemClick(bannerName: String?, link: String?) {
        if (!link!!.isEmpty()) {
            if (link.indexOf("template") > 0 && link.indexOf("group") > 0) {
                try {
                    val templateID = TextUtils.split(link, "/")[6]
                    val groupID = act.getSharedPreferences("UserBean", Context.MODE_PRIVATE).getInt(kGroupId,0)
                    val reportID = TextUtils.split(link, "/")[8]
                    var urlString: String
                    val intent: Intent

                    when (templateID) {
                        "-1", "2", "4" -> {
                            intent = Intent(activity, SubjectActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            intent.putExtra(URLs.kBannerName, bannerName)
                            intent.putExtra(URLs.kLink, link)
                            intent.putExtra(URLs.kObjectId, 1)
                            intent.putExtra(URLs.kObjectType, 1)
                            intent.putExtra("groupID", groupID)
                            intent.putExtra("reportID", reportID)
                            startActivity(intent)
                        }

                        "3" -> {
                            intent = Intent(ctx, HomeTricsActivity::class.java)
                            urlString = String.format("%s/api/v1/group/%d/template/%s/report/%s/json",
                                    K.kBaseUrl, groupID, templateID, reportID)
                            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            intent.putExtra(URLs.kBannerName, bannerName)
                            intent.putExtra(URLs.kObjectId, 1)
                            intent.putExtra(URLs.kObjectType, 1)
                            intent.putExtra("groupID", groupID)
                            intent.putExtra("reportID", reportID)
                            intent.putExtra("urlString", urlString)
                            startActivity(intent)
                        }

                        "5" -> {
                            intent = Intent(ctx, TableActivity::class.java)
                            urlString = String.format("%s/api/v1/group/%d/template/%s/report/%s/json",
                                    K.kBaseUrl, groupID, templateID, reportID)
                            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            intent.putExtra(URLs.kBannerName, bannerName)
                            intent.putExtra(URLs.kObjectId, 1)
                            intent.putExtra(URLs.kObjectType, 1)
                            intent.putExtra("groupID", groupID)
                            intent.putExtra("reportID", reportID)
                            intent.putExtra("urlString", urlString)
                            startActivity(intent)
                        }
                        else -> showTemplateErrorDialog()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else {
                val intent = Intent(activity, SubjectActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra(URLs.kBannerName, bannerName)
                intent.putExtra(URLs.kLink, link)
                startActivity(intent)
            }
        }
    }

    internal fun showTemplateErrorDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("温馨提示")
                .setMessage("当前版本暂不支持该模板, 请升级应用后查看")
                .setPositiveButton("前去升级") { _, _ ->
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(K.kPgyerUrl))
                    startActivity(browserIntent)
                }
                .setNegativeButton("稍后升级") { _, _ ->
                    // 返回 LoginActivity
                }
        builder.show()
    }
}