package com.chaoxing.sign.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaoxing.sign.ChaoxingApp
import com.chaoxing.sign.R
import com.chaoxing.sign.api.ChaoxingApi
import com.chaoxing.sign.api.ChaoxingSession
import com.chaoxing.sign.api.Course
import com.chaoxing.sign.push.PushClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {
    private lateinit var session: ChaoxingSession
    private lateinit var rvCourses: RecyclerView
    private lateinit var llEmpty: View
    private lateinit var llLoading: View
    private var pushClient: PushClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 使用全局共享 session
        val app = application as ChaoxingApp
        if (app.session == null) {
            app.session = ChaoxingSession(this)
        }
        session = app.session!!
        if (!session.loadSavedSession()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        rvCourses = findViewById(R.id.rv_courses)
        llEmpty = findViewById(R.id.ll_empty)
        llLoading = findViewById(R.id.ll_loading)

        rvCourses.layoutManager = LinearLayoutManager(this)

        findViewById<TextView>(R.id.tv_refresh).setOnClickListener { loadCourses() }

        // 先自动登录，再加载课程
        lifecycleScope.launch {
            try {
                val ok = withContext(Dispatchers.IO) { session.autoLogin() }
                if (!ok) {
                    // 自动登录失败 → 跳转登录页
                    startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                    finish()
                    return@launch
                }
            } catch (e: Exception) {
                // 网络错误等 → 跳转登录页
                startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                finish()
                return@launch
            }
            // 登录成功 → 加载课程
            loadCourses()
            // 连接推送服务
            connectPush()
        }
    }

    private fun connectPush() {
        pushClient = PushClient("ws://38.76.190.251:8765", "student_${session.uid}")
        pushClient?.onSign = { data -> handleSignPush(data) }
        pushClient?.connect()
    }

    private fun handleSignPush(data: JSONObject) {
        val activeId = data.optLong("activeId", 0)
        val courseName = data.optString("courseName", "")
        val signType = data.optString("signType", "普通签到")
        val needMobile = data.optBoolean("need_mobile", false)
        val otherId = data.optInt("other_id", 0)

        runOnUiThread {
            Toast.makeText(this, "收到签到: $signType ($courseName)", Toast.LENGTH_LONG).show()
        }

        // 位置签到、拍照签到 → 打开对应页面
        if (otherId == 4) {
            lifecycleScope.launch {
                try {
                    val detail = withContext(Dispatchers.IO) {
                        ChaoxingApi.getActiveDetail(session, activeId)
                    }
                    val courseId = detail.optString("courseId", "")
                    val classId = detail.optString("clazzid", "")
                    val targetLat = detail.optDouble("locationLatitude", 0.0)
                    val targetLon = detail.optDouble("locationLongitude", 0.0)
                    val targetRange = detail.optInt("locationRange", 100)
                    val targetName = detail.optString("locationText", "")

                    runOnUiThread {
                        val intent = Intent(this@HomeActivity, LocationSignActivity::class.java)
                        intent.putExtra("activeId", activeId)
                        intent.putExtra("courseId", courseId)
                        intent.putExtra("classId", classId)
                        intent.putExtra("targetLat", targetLat)
                        intent.putExtra("targetLon", targetLon)
                        intent.putExtra("targetRange", targetRange)
                        intent.putExtra("targetName", targetName)
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@HomeActivity, "获取活动详情失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            return
        }

        // 其他签到类型 → 自动签到
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val detail = ChaoxingApi.getActiveDetail(session, activeId)
                    val courseId = detail.optString("courseId", "")
                    val classId = detail.optString("clazzid", "")
                    val oid = detail.optInt("otherId", 0)

                    ChaoxingApi.preSign(session, activeId, courseId, classId)

                    when (oid) {
                        0 -> ChaoxingApi.signNormal(session, activeId)
                        2 -> {
                            val enc = ChaoxingApi.getEncFromAnalysis(session, activeId)
                            if (enc.isNotEmpty()) ChaoxingApi.signQrCode(session, activeId, enc)
                            else "无法获取enc"
                        }
                        else -> ChaoxingApi.signNormal(session, activeId)
                    }
                }

                val success = result.contains("success") || result.contains("已签到")
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this@HomeActivity, "自动签到成功!", Toast.LENGTH_SHORT).show()
                        loadCourses()
                    } else {
                        Toast.makeText(this@HomeActivity, "签到失败: $result", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@HomeActivity, "签到错误: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pushClient?.disconnect()
    }

    private fun loadCourses() {
        llLoading.visibility = View.VISIBLE
        llEmpty.visibility = View.GONE
        rvCourses.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val courses = withContext(Dispatchers.IO) {
                    ChaoxingApi.getCourseList(session)
                }

                llLoading.visibility = View.GONE

                if (courses.isEmpty()) {
                    llEmpty.visibility = View.VISIBLE
                } else {
                    rvCourses.visibility = View.VISIBLE
                    rvCourses.adapter = CourseAdapter(courses) { course ->
                        val intent = Intent(this@HomeActivity, SignActivity::class.java)
                        intent.putExtra("courseId", course.courseId)
                        intent.putExtra("classId", course.classId)
                        intent.putExtra("courseName", course.name)
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                llLoading.visibility = View.GONE
                Toast.makeText(this@HomeActivity, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class CourseAdapter(
    private val courses: List<Course>,
    private val onClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_name)
        val tvId: TextView = view.findViewById(R.id.tv_id)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = courses[position]
        holder.tvName.text = course.name
        holder.tvId.text = "ID: ${course.courseId}"
        holder.itemView.setOnClickListener { onClick(course) }
    }

    override fun getItemCount() = courses.size
}
