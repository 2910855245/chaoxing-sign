package com.chaoxing.sign.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaoxing.sign.ChaoxingApp
import com.chaoxing.sign.R
import com.chaoxing.sign.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignActivity : AppCompatActivity() {
    private lateinit var session: ChaoxingSession
    private lateinit var rvActivities: RecyclerView
    private lateinit var tvStatus: TextView
    private lateinit var llLoading: View
    private var courseId: String = ""
    private var classId: String = ""
    private var dataLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)

        // 使用全局共享 session
        val app = application as ChaoxingApp
        session = app.session ?: ChaoxingSession(this).also { app.session = it }
        courseId = intent.getStringExtra("courseId") ?: ""
        classId = intent.getStringExtra("classId") ?: ""

        android.util.Log.d("SignActivity", "onCreate: courseId=$courseId, classId=$classId")

        findViewById<TextView>(R.id.tv_back).setOnClickListener { finish() }

        rvActivities = findViewById(R.id.rv_activities)
        tvStatus = findViewById(R.id.tv_status)
        llLoading = findViewById(R.id.ll_loading)
        rvActivities.layoutManager = LinearLayoutManager(this)

        // 先自动登录，再加载活动
        lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) { session.autoLogin() }
            if (!ok) {
                Toast.makeText(this@SignActivity, "登录失败，请重新登录", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            android.util.Log.d("SignActivity", "登录成功: uid=${session.uid}, fid=${session.fid}")
            loadActivities()
        }
    }

    override fun onResume() {
        super.onResume()
        // 从 LocationSignActivity 返回时刷新列表（仅在数据已加载过的情况下）
        if (dataLoaded && ::session.isInitialized && session.isLoggedIn) {
            loadActivities()
        }
    }

    private fun loadActivities() {
        // 显示加载动画
        llLoading.visibility = View.VISIBLE
        rvActivities.visibility = View.GONE
        tvStatus.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val activities = withContext(Dispatchers.IO) {
                    ChaoxingApi.getActiveList(session, courseId, classId)
                }

                // 调试信息
                android.util.Log.d("SignActivity", "获取到 ${activities.size} 个活动, courseId=$courseId, classId=$classId")

                // 隐藏加载动画
                llLoading.visibility = View.GONE
                dataLoaded = true

                if (activities.isEmpty()) {
                    tvStatus.text = "暂无签到活动\n课程ID: $courseId\n班级ID: $classId"
                    tvStatus.visibility = View.VISIBLE
                } else {
                    tvStatus.visibility = View.GONE
                    rvActivities.visibility = View.VISIBLE
                    rvActivities.adapter = ActivityAdapter(activities, ::handleSign)
                }
            } catch (e: Exception) {
                llLoading.visibility = View.GONE
                android.util.Log.e("SignActivity", "加载失败: ${e.message}", e)
                Toast.makeText(this@SignActivity, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSign(act: SignActivityData) {
        // 位置签到需要特殊处理，即使显示"已签"也要打开地图
        if (act.otherId == 4) {
            doLocationSign(act)
            return
        }

        // 其他签到类型检查是否已签
        lifecycleScope.launch {
            val realSigned = withContext(Dispatchers.IO) {
                ChaoxingApi.checkRealSignStatus(session, act.activeId)
            }
            if (realSigned) {
                Toast.makeText(this@SignActivity, "已签到", Toast.LENGTH_SHORT).show()
                return@launch
            }

            when (act.otherId) {
                0 -> doNormalSign(act)
                2 -> doQrCodeSign(act)
                3 -> doGestureSign(act)
                5 -> doCodeSign(act)
                else -> doNormalSign(act)
            }
        }
    }

    // ─── 普通签到 ───
    private fun doNormalSign(act: SignActivityData) {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    ChaoxingApi.preSign(session, act.activeId, act.courseId, act.classId)
                    ChaoxingApi.signNormal(session, act.activeId)
                }
                showResult(result, act)
            } catch (e: Exception) {
                Toast.makeText(this@SignActivity, "错误: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── 位置签到 ───
    private fun doLocationSign(act: SignActivityData) {
        lifecycleScope.launch {
            try {
                // 获取活动详情，包含目标位置信息
                val detail = withContext(Dispatchers.IO) {
                    ChaoxingApi.getActiveDetail(session, act.activeId)
                }

                val intent = Intent(this@SignActivity, LocationSignActivity::class.java)
                intent.putExtra("activeId", act.activeId)
                intent.putExtra("courseId", act.courseId)
                intent.putExtra("classId", act.classId)

                // 传递目标位置信息
                val targetLat = detail.optDouble("locationLatitude", 0.0)
                val targetLon = detail.optDouble("locationLongitude", 0.0)
                val targetRange = detail.optInt("locationRange", 100)
                val targetName = detail.optString("locationText", "")

                intent.putExtra("targetLat", targetLat)
                intent.putExtra("targetLon", targetLon)
                intent.putExtra("targetRange", targetRange)
                intent.putExtra("targetName", targetName)

                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this@SignActivity, "获取活动详情失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── 手势签到（暴力破解）───
    private fun doGestureSign(act: SignActivityData) {
        lifecycleScope.launch {
            try {
                val detail = withContext(Dispatchers.IO) {
                    ChaoxingApi.getActiveDetail(session, act.activeId)
                }
                val numberCount = detail.optInt("numberCount", 5)

                // 显示进度对话框
                val progressDialog = AlertDialog.Builder(this@SignActivity)
                    .setTitle("手势签到")
                    .setMessage("正在暴力破解 $numberCount 点手势...\n字典优先，然后全排列")
                    .setCancelable(false)
                    .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                    .create()
                progressDialog.show()

                val result = withContext(Dispatchers.IO) {
                    ChaoxingApi.preSign(session, act.activeId, act.courseId, act.classId)
                    ChaoxingApi.bruteForceGesture(session, act.activeId, numberCount) { attempted, total ->
                        runOnUiThread {
                            progressDialog.setMessage("正在暴力破解...\n$attempted / $total")
                        }
                    }
                }

                progressDialog.dismiss()

                if (result.found) {
                    val signResult = withContext(Dispatchers.IO) {
                        ChaoxingApi.signWithCode(session, act.activeId, result.code)
                    }
                    Toast.makeText(this@SignActivity, "破解成功! code=${result.code}", Toast.LENGTH_LONG).show()
                    showResult(signResult, act)
                } else {
                    Toast.makeText(this@SignActivity, "暴力破解失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignActivity, "错误: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── 签到码签到（暴力破解）───
    private fun doCodeSign(act: SignActivityData) {
        lifecycleScope.launch {
            try {
                // 显示进度对话框
                val progressDialog = AlertDialog.Builder(this@SignActivity)
                    .setTitle("签到码签到")
                    .setMessage("正在暴力破解签到码...\n4位→5位→6位数字")
                    .setCancelable(false)
                    .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                    .create()
                progressDialog.show()

                val result = withContext(Dispatchers.IO) {
                    ChaoxingApi.preSign(session, act.activeId, act.courseId, act.classId)
                    ChaoxingApi.bruteForceCode(session, act.activeId) { attempted, total ->
                        runOnUiThread {
                            progressDialog.setMessage("正在暴力破解...\n$attempted / $total")
                        }
                    }
                }

                progressDialog.dismiss()

                if (result.found) {
                    val signResult = withContext(Dispatchers.IO) {
                        ChaoxingApi.signWithCode(session, act.activeId, result.code)
                    }
                    Toast.makeText(this@SignActivity, "破解成功! code=${result.code}", Toast.LENGTH_LONG).show()
                    showResult(signResult, act)
                } else {
                    Toast.makeText(this@SignActivity, "暴力破解失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignActivity, "错误: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── 二维码签到 ───
    private fun doQrCodeSign(act: SignActivityData) {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    ChaoxingApi.preSign(session, act.activeId, act.courseId, act.classId)
                    val enc = ChaoxingApi.getEncFromAnalysis(session, act.activeId)
                    if (enc.isEmpty()) {
                        "无法获取enc"
                    } else {
                        ChaoxingApi.signQrCode(session, act.activeId, enc)
                    }
                }
                showResult(result, act)
            } catch (e: Exception) {
                Toast.makeText(this@SignActivity, "错误: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showResult(result: String, act: SignActivityData) {
        val success = result.contains("success") || result.contains("已签到")
        val msg = if (success) "签到成功!" else "签到失败: $result"

        AlertDialog.Builder(this)
            .setTitle(if (success) "成功" else "失败")
            .setMessage(msg)
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                if (success) loadActivities()
            }
            .show()
    }
}

// ─── Adapter ───
class ActivityAdapter(
    private val activities: List<SignActivityData>,
    private val onSign: (SignActivityData) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.iv_icon)
        val tvType: TextView = view.findViewById(R.id.tv_type)
        val tvId: TextView = view.findViewById(R.id.tv_id)
        val ivStatus: ImageView = view.findViewById(R.id.iv_status)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val btnSign: Button = view.findViewById(R.id.btn_sign)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val act = activities[position]
        val ctx = holder.itemView.context

        val (iconRes, bgRes) = when (act.otherId) {
            0 -> R.drawable.ic_sign_normal to R.drawable.bg_icon_normal
            2 -> R.drawable.ic_sign_qrcode to R.drawable.bg_icon_qrcode
            3 -> R.drawable.ic_sign_gesture to R.drawable.bg_icon_gesture
            4 -> R.drawable.ic_sign_location to R.drawable.bg_icon_location
            5 -> R.drawable.ic_sign_code to R.drawable.bg_icon_code
            else -> R.drawable.ic_sign_normal to R.drawable.bg_icon_normal
        }
        holder.ivIcon.setImageResource(iconRes)
        holder.ivIcon.setBackgroundResource(bgRes)

        holder.tvType.text = act.typeName
        holder.tvId.text = "ID: ${act.activeId}"

        // 位置签到显示特殊状态
        val (statusText, statusColor, statusIcon) = when {
            act.isLocation && act.status != 2 -> Triple("待签到", R.color.warning, R.drawable.ic_pending)
            act.userStatus == 1 -> Triple("已签到", R.color.success, R.drawable.ic_check)
            act.status == 2 -> Triple("已过期", R.color.text_muted, R.drawable.ic_expired)
            act.status == 0 -> Triple("待签到", R.color.warning, R.drawable.ic_pending)
            else -> Triple("未知", R.color.text_muted, R.drawable.ic_expired)
        }
        holder.tvStatus.text = statusText
        holder.tvStatus.setTextColor(ContextCompat.getColor(ctx, statusColor))
        holder.ivStatus.setImageResource(statusIcon)

        if (act.isPending) {
            holder.btnSign.visibility = View.VISIBLE
            holder.btnSign.setOnClickListener { onSign(act) }
        } else {
            holder.btnSign.visibility = View.GONE
        }
    }

    override fun getItemCount() = activities.size
}
