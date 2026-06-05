package com.chaoxing.sign.api

import org.json.JSONObject

data class Course(
    val courseId: String,
    val classId: String,
    val name: String
)

data class SignActivityData(
    val activeId: Long,
    val otherId: Int,       // 0=普通 2=二维码 3=手势 4=位置 5=签到码
    val typeName: String,
    val nameOne: String,
    val startTime: Long,
    val status: Int,        // 0=未签 1=已签 2=已过期
    val userStatus: Int,
    val courseId: String,
    val classId: String
) {
    // 位置签到始终显示按钮（因为API返回的userStatus可能不准确）
    val isPending get() = (status == 0 && userStatus != 1) || (otherId == 4 && status != 2)
    val isBruteForce get() = otherId == 3 || otherId == 5
    val isLocation get() = otherId == 4
}

data class BruteForceResult(
    val found: Boolean,
    val code: String = "",
    val message: String = ""
)

object ChaoxingApi {

    // ─── 手势字典 ───
    private val GESTURE_DICT_4 = listOf(
        "1234", "2345", "3456", "4567", "5678", "6789",
        "1478", "2589", "1597", "3571", "7896", "3694",
        "1236", "4569", "7893", "1470", "2580", "3690",
        "1590", "3570", "1230", "4560", "7890",
        "1475", "1472", "3695", "3692", "7410", "8520", "9630",
        "1479", "1489", "2589", "3698", "3697", "7893",
        "1235", "1236", "1475", "1476", "3215", "3214",
        "7895", "7894", "9875", "9876", "1258", "1259",
        "3258", "3254", "7852", "7851", "9852", "9856",
        "1593", "3579", "1594", "1596", "3574", "3572",
        "1458", "1459", "2569", "2568", "3658", "3657",
        "1456", "3654", "1598", "1596", "3574", "3572",
        "1254", "1256", "3254", "3256", "7854", "7856",
        "9854", "9856", "1452", "3652", "7458", "7452",
        "9658", "9654", "1235", "1475", "7895", "3695",
        "1258", "1458", "3658", "7858"
    ).distinct()

    private val GESTURE_DICT_5 = listOf(
        "12345", "23456", "34567", "45678", "56789",
        "14789", "25896", "36987", "15963", "35741",
        "78963", "12369", "14785", "12347", "12348",
        "12349", "12358", "12359", "14569", "14589",
        "15698", "15987", "32147", "32159", "32547",
        "32569", "32587", "36541", "36587", "35789",
        "74123", "74125", "74159", "74569", "74589",
        "78541", "78563", "78941", "78951", "96321",
        "96325", "96541", "96587", "98741", "98753",
        "98763", "98541", "98563",
        "12357", "12358", "12359", "12368", "12369",
        "32157", "32158", "32159", "32148", "32147",
        "78951", "78952", "78953", "78962", "78963",
        "98751", "98752", "98753", "98742", "98741",
        "12589", "12587", "12569", "12567", "32589",
        "32587", "32569", "32567", "78521", "78523",
        "78541", "78543", "98521", "98523", "98541", "98543",
        "12365", "12364", "32145", "32147", "78965",
        "98745", "98741", "12547", "12548", "12549",
        "12569", "12567", "32547", "32548", "32549",
        "32569", "32567", "78541", "78542", "78543",
        "78563", "78561", "98541", "98542", "98543",
        "98563", "98561",
        "14785", "14786", "14782", "14783", "25896",
        "25897", "36984", "36981", "36982", "36985",
        "74126", "74128", "74129", "85236", "85231",
        "96324", "96327", "96328",
        "14563", "32541", "12547", "78523", "98541",
        "15987", "15964", "15962", "35742", "35748",
        "14528", "14529", "14536", "14539", "36528",
        "36527", "36514", "36517", "74528", "74521",
        "74536", "74531", "96528", "96521", "96514", "96517"
    ).distinct()

    // ─── 课程列表 ───
    fun getCourseList(session: ChaoxingSession): List<Course> {
        val body = "courseType=1&courseFolderId=0&baseEducation=0&superstarClass=&courseFolderSize=0"
        val html = session.post(
            "https://mooc1-1.chaoxing.com/mooc-ans/visit/courselistdata",
            body.split("&").associate { val (k, v) = it.split("="); k to v },
            "https://mooc1.chaoxing.com/"
        )

        val courses = mutableListOf<Course>()
        val regex = Regex("""courseId="(\d+)"[^>]*clazzId="(\d+)"[^>]*>""")
        for (m in regex.findAll(html)) {
            val cid = m.groupValues[1]
            val clid = m.groupValues[2]
            val nameRegex = Regex("""id="course_${cid}_${clid}"[^>]*>.*?title="([^"]+)"""", RegexOption.DOT_MATCHES_ALL)
            val name = nameRegex.find(html)?.groupValues?.get(1)?.trim() ?: "未知课程"
            courses.add(Course(cid, clid, name))
        }
        return courses.distinctBy { "${it.courseId}_${it.classId}" }
    }

    // ─── 签到活动列表 ───
    fun getActiveList(session: ChaoxingSession, courseId: String, classId: String): List<SignActivityData> {
        val ts = System.currentTimeMillis()
        val url = "https://mobilelearn.chaoxing.com/v2/apis/active/student/activelist" +
                "?fid=0&courseId=$courseId&classId=$classId&_=$ts"

        // 调试信息
        android.util.Log.d("ChaoxingApi", "请求URL: $url")
        android.util.Log.d("ChaoxingApi", "Session UID: ${session.uid}, FID: ${session.fid}, isLoggedIn: ${session.isLoggedIn}")

        val text = session.get(url, "https://mobilelearn.chaoxing.com/")
        android.util.Log.d("ChaoxingApi", "响应长度: ${text.length}")
        android.util.Log.d("ChaoxingApi", "响应内容: ${text.take(300)}")

        if (text.isEmpty()) {
            android.util.Log.e("ChaoxingApi", "响应为空!")
            return emptyList()
        }

        val json = try { org.json.JSONObject(text) } catch (e: Exception) {
            android.util.Log.e("ChaoxingApi", "JSON解析失败: ${e.message}")
            return emptyList()
        }

        val result = json.optInt("result", -1)
        android.util.Log.d("ChaoxingApi", "API result: $result")

        val data = json.optJSONObject("data")
        if (data == null) {
            android.util.Log.w("ChaoxingApi", "data为空, 完整响应: ${text.take(500)}")
            return emptyList()
        }

        val list = data.optJSONArray("activeList")
        if (list == null) {
            android.util.Log.w("ChaoxingApi", "activeList为空")
            return emptyList()
        }

        android.util.Log.d("ChaoxingApi", "活动数量: ${list.length()}")

        val activities = mutableListOf<SignActivityData>()
        for (i in 0 until list.length()) {
            val item = list.getJSONObject(i)
            if (item.optInt("activeType") != 2) continue

            val otherId = item.optInt("otherId", -1)
            val typeName = when (otherId) {
                0 -> "普通签到"
                2 -> "二维码签到"
                3 -> "手势签到"
                4 -> "位置签到"
                5 -> "签到码签到"
                else -> "未知($otherId)"
            }

            activities.add(SignActivityData(
                activeId = item.optLong("id"),
                otherId = otherId,
                typeName = typeName,
                nameOne = item.optString("nameOne", ""),
                startTime = item.optLong("startTime"),
                status = item.optInt("status"),
                userStatus = item.optInt("userStatus"),
                courseId = courseId,
                classId = classId
            ))
        }
        return activities
    }

    // ─── 活动详情 ───
    fun getActiveDetail(session: ChaoxingSession, activeId: Long): JSONObject {
        return session.getJson(
            "https://mobilelearn.chaoxing.com/v2/apis/active/getPPTActiveInfo?activeId=$activeId",
            "https://mobilelearn.chaoxing.com/"
        ).optJSONObject("data") ?: JSONObject()
    }

    // ─── 检查真实签到状态 ───
    // 注意：getPPTActiveInfo API 对位置签到返回错误的 userStatus
    // 优先使用 activelist API 检查
    fun checkRealSignStatus(session: ChaoxingSession, activeId: Long,
                            courseId: String? = null, classId: String? = null): Boolean {
        // 如果有课程信息，用 activelist API（更准确）
        if (courseId != null && classId != null) {
            val acts = getActiveList(session, courseId, classId)
            val act = acts.find { it.activeId == activeId }
            if (act != null) {
                android.util.Log.d("ChaoxingApi", "检查签到状态(activelist): activeId=$activeId, userStatus=${act.userStatus}")
                return act.userStatus == 1
            }
        }
        // 回退到详情 API
        val detail = getActiveDetail(session, activeId)
        val userStatus = detail.optInt("userStatus", 0)
        android.util.Log.d("ChaoxingApi", "检查签到状态(detail): activeId=$activeId, userStatus=$userStatus")
        return userStatus == 1
    }

    // ─── 预签到 ───
    fun preSign(session: ChaoxingSession, activeId: Long, courseId: String, classId: String) {
        session.get(
            "https://mobilelearn.chaoxing.com/newsign/preSign" +
                    "?courseId=$courseId&classId=$classId" +
                    "&activePrimaryId=$activeId&general=1&sys=1&ls=1&appType=15" +
                    "&uid=${session.uid}&ut=s",
            "https://mobilelearn.chaoxing.com/"
        )
    }

    // ─── 校验手势/签到码 ───
    fun checkSignCode(session: ChaoxingSession, activeId: Long, signCode: String): Boolean {
        val json = session.getJson(
            "https://mobilelearn.chaoxing.com/widget/sign/pcStuSignController/checkSignCode" +
                    "?activeId=$activeId&signCode=$signCode",
            "https://mobilelearn.chaoxing.com/"
        )
        return json.optInt("result") == 1
    }

    // ─── 获取 enc（二维码签到用）───
    fun getEncFromAnalysis(session: ChaoxingSession, activeId: Long): String {
        val text = session.get(
            "https://mobilelearn.chaoxing.com/pptSign/analysis?vs=1&DB_STRATEGY=RANDOM&aid=$activeId",
            "https://mobilelearn.chaoxing.com/"
        )
        val match = Regex("'([a-f0-9]{32})'").find(text) ?: return ""
        val code = match.groupValues[1]

        val enc = session.get(
            "https://mobilelearn.chaoxing.com/pptSign/analysis2?DB_STRATEGY=RANDOM&code=$code",
            "https://mobilelearn.chaoxing.com/"
        ).trim()

        return if (enc.length > 5 && enc != "success") enc else ""
    }

    // ─── 普通签到 ───
    fun signNormal(session: ChaoxingSession, activeId: Long): String {
        val name = session.name
        val url = "https://mobilelearn.chaoxing.com/pptSign/stuSignajax" +
                "?activeId=$activeId&uid=${session.uid}&clientip=" +
                "&latitude=-1&longitude=-1&appType=15&fid=${session.fid}&name=$name"
        return session.get(url, "https://mobilelearn.chaoxing.com/")
    }

    // ─── 位置签到 ───
    fun signLocation(session: ChaoxingSession, activeId: Long,
                     lat: Double, lon: Double, address: String): String {
        val name = session.name
        val url = "https://mobilelearn.chaoxing.com/pptSign/stuSignajax" +
                "?activeId=$activeId&uid=${session.uid}&clientip=" +
                "&latitude=$lat&longitude=$lon&appType=15&fid=${session.fid}" +
                "&name=$name&address=$address&ifTiJiao=1"
        return session.get(url, "https://mobilelearn.chaoxing.com/")
    }

    // ─── 手势/签到码签到 ───
    fun signWithCode(session: ChaoxingSession, activeId: Long, signCode: String): String {
        val name = session.name
        val url = "https://mobilelearn.chaoxing.com/pptSign/stuSignajax" +
                "?activeId=$activeId&uid=${session.uid}&clientip=" +
                "&latitude=-1&longitude=-1&appType=15&fid=${session.fid}" +
                "&name=$name&signCode=$signCode"
        return session.get(url, "https://mobilelearn.chaoxing.com/")
    }

    // ─── 二维码签到 ───
    fun signQrCode(session: ChaoxingSession, activeId: Long, enc: String): String {
        val name = session.name
        val url = "https://mobilelearn.chaoxing.com/pptSign/stuSignajax" +
                "?enc=$enc&name=$name&activeId=$activeId&uid=${session.uid}" +
                "&clientip=&latitude=-1&longitude=-1&fid=${session.fid}&appType=15"
        return session.get(url, "https://mobilelearn.chaoxing.com/")
    }

    // ─── 拍照签到 ───
    fun signPhoto(session: ChaoxingSession, activeId: Long, objectId: String): String {
        val name = session.name
        val url = "https://mobilelearn.chaoxing.com/pptSign/stuSignajax" +
                "?activeId=$activeId&uid=${session.uid}&clientip=" +
                "&latitude=-1&longitude=-1&appType=15&fid=${session.fid}" +
                "&objectId=$objectId&name=$name"
        return session.get(url, "https://mobilelearn.chaoxing.com/")
    }

    // ─── 暴力破解手势 ───
    fun bruteForceGesture(
        session: ChaoxingSession,
        activeId: Long,
        numberCount: Int,
        onProgress: (attempted: Int, total: Int) -> Unit = { _, _ -> }
    ): BruteForceResult {
        val dict = when (numberCount) {
            4 -> GESTURE_DICT_4
            5 -> GESTURE_DICT_5
            3 -> listOf("123", "147", "159", "258", "369", "789", "321", "951")
            else -> GESTURE_DICT_5
        }

        // Phase 1: 字典
        for ((i, code) in dict.withIndex()) {
            if (checkSignCode(session, activeId, code)) {
                return BruteForceResult(true, code, "字典命中")
            }
            onProgress(i + 1, dict.size)
            Thread.sleep(30)
        }

        // Phase 2: 全排列
        val points = "123456789"
        val total = permCount(9, numberCount)
        val tried = dict.toHashSet()
        var count = 0

        val perms = generatePermutations(points.toList(), numberCount)
        for (perm in perms) {
            val code = perm.joinToString("")
            if (code !in tried) {
                tried.add(code)
                count++
                if (checkSignCode(session, activeId, code)) {
                    return BruteForceResult(true, code, "全排列命中")
                }
                if (count % 50 == 0) {
                    onProgress(dict.size + count, dict.size + total)
                    Thread.sleep(10)
                }
            }
        }

        return BruteForceResult(false, "", "暴力破解失败")
    }

    // ─── 暴力破解签到码 ───
    fun bruteForceCode(
        session: ChaoxingSession,
        activeId: Long,
        onProgress: (attempted: Int, total: Int) -> Unit = { _, _ -> }
    ): BruteForceResult {
        for (digits in listOf(4, 5, 6)) {
            val total = Math.pow(10.0, digits.toDouble()).toInt()
            for (i in 0 until total) {
                val code = i.toString().padStart(digits, '0')
                if (checkSignCode(session, activeId, code)) {
                    return BruteForceResult(true, code, "${digits}位命中")
                }
                if (i % 100 == 0) {
                    onProgress(i, total)
                    Thread.sleep(10)
                }
            }
        }
        return BruteForceResult(false, "", "暴力破解失败")
    }

    // ─── 工具函数 ───
    private fun permCount(n: Int, r: Int): Int {
        var result = 1
        for (i in 0 until r) result *= (n - i)
        return result
    }

    private fun generatePermutations(list: List<Char>, r: Int): List<List<Char>> {
        val result = mutableListOf<List<Char>>()
        val used = BooleanArray(list.size)
        val current = mutableListOf<Char>()

        fun dfs() {
            if (current.size == r) {
                result.add(current.toList())
                return
            }
            for (i in list.indices) {
                if (!used[i]) {
                    used[i] = true
                    current.add(list[i])
                    dfs()
                    current.removeAt(current.lastIndex)
                    used[i] = false
                }
            }
        }
        dfs()
        return result
    }
}
