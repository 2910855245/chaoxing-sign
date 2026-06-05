package com.chaoxing.sign.api

import android.content.Context
import android.content.SharedPreferences
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ChaoxingSession(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("chaoxing", Context.MODE_PRIVATE)

    private val cookieJar = PersistentCookieJar(prefs)
    val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(false)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("User-Agent", UA)
                .header("Accept", "*/*")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Connection", "keep-alive")
                .build()
            chain.proceed(request)
        }
        .build()

    var uid: String = prefs.getString("uid", "") ?: ""
        private set
    var fid: String = prefs.getString("fid", "") ?: ""
        private set
    var name: String = prefs.getString("name", "") ?: ""
        private set
    // 检查是否有保存的登录凭证（uid不为空且有保存的cookie）
    var isLoggedIn: Boolean = uid.isNotEmpty() && prefs.getString("cookies", null) != null
        private set

    fun login(username: String, password: String): Boolean {
        val body = "fid=-1&uname=$username&password=$password" +
                "&refer=https%3A%2F%2Fi.chaoxing.com&t=true&forbidotherlogin=0&validate="

        val request = Request.Builder()
            .url("https://passport2.chaoxing.com/fanyalogin")
            .post(body.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
            .header("Referer", "https://passport2.chaoxing.com/login")
            .build()

        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            android.util.Log.d("ChaoxingSession", "登录响应: ${responseBody.take(200)}")

            val json = JSONObject(responseBody)
            val success = json.optInt("result") == 1 || json.optBoolean("status")

            if (success) {
                // 从 cookie 提取 uid
                try {
                    val allCookies = cookieJar.loadForRequest("https://chaoxing.com".toHttpUrl())
                    android.util.Log.d("ChaoxingSession", "Cookie数量: ${allCookies.size}")

                    for (c in allCookies) {
                        when (c.name) {
                            "UID", "_uid" -> uid = c.value
                            "fid" -> fid = c.value
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("ChaoxingSession", "Cookie提取异常: ${e.message}")
                }

                // 如果还是没有uid，尝试从响应中获取
                if (uid.isEmpty()) {
                    uid = json.optString("uid", "")
                }

                // 获取用户信息（不阻塞登录）
                try {
                    val info = getUserInfo()
                    name = info.optString("name", "")
                } catch (e: Exception) {
                    android.util.Log.w("ChaoxingSession", "获取用户信息失败: ${e.message}")
                    // 使用已保存的name或默认值
                    if (name.isEmpty()) name = username
                }

                android.util.Log.d("ChaoxingSession", "登录成功: uid=$uid, fid=$fid, name=$name")
                isLoggedIn = true
                saveSession(username, password)
                // 保存uid、fid、name
                prefs.edit()
                    .putString("uid", uid)
                    .putString("fid", fid)
                    .putString("name", name)
                    .apply()
            } else {
                android.util.Log.w("ChaoxingSession", "登录失败: ${json.optString("msg")}")
            }
            success
        } catch (e: Exception) {
            android.util.Log.e("ChaoxingSession", "登录异常: ${e.javaClass.simpleName}: ${e.message}")
            false
        }
    }

    fun loadSavedSession(): Boolean {
        val u = prefs.getString("username", null) ?: return false
        val p = prefs.getString("password", null) ?: return false
        // 只检查有没有保存的账号，不重新登录
        return u.isNotEmpty() && p.isNotEmpty()
    }

    fun autoLogin(): Boolean {
        val u = prefs.getString("username", null) ?: return false
        val p = prefs.getString("password", null) ?: return false
        return login(u, p)
    }

    fun saveSession(username: String, password: String) {
        prefs.edit()
            .putString("username", username)
            .putString("password", password)
            .apply()
    }

    fun getUserInfo(): JSONObject {
        return try {
            val req = Request.Builder()
                .url("https://sso.chaoxing.com/apis/login/userLogin4Uname.do")
                .build()
            val resp = client.newCall(req).execute()
            val json = JSONObject(resp.body?.string() ?: "")
            val msg = json.optJSONObject("msg") ?: JSONObject()
            name = msg.optString("name", "")
            JSONObject().apply {
                put("name", msg.optString("name", ""))
                put("uid", uid)
                put("schoolname", msg.optString("schoolname", ""))
                put("uname", msg.optString("uname", ""))
            }
        } catch (e: Exception) {
            JSONObject()
        }
    }

    fun get(url: String, referer: String? = null): String {
        val builder = Request.Builder().url(url)
        referer?.let { builder.addHeader("Referer", it) }
        val resp = client.newCall(builder.build()).execute()
        return resp.body?.string() ?: ""
    }

    fun getJson(url: String, referer: String? = null): JSONObject {
        val text = get(url, referer)
        return try { JSONObject(text) } catch (e: Exception) { JSONObject() }
    }

    fun post(url: String, formBody: Map<String, String>, referer: String? = null): String {
        val body = FormBody.Builder()
        formBody.forEach { (k, v) -> body.add(k, v) }
        val builder = Request.Builder().url(url).post(body.build())
        referer?.let { builder.addHeader("Referer", it) }
        builder.addHeader("X-Requested-With", "XMLHttpRequest")
        val resp = client.newCall(builder.build()).execute()
        return resp.body?.string() ?: ""
    }

    companion object {
        private val UA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36"
    }
}

class PersistentCookieJar(private val prefs: SharedPreferences) : CookieJar {
    private val store = mutableMapOf<String, MutableList<Cookie>>()

    init {
        // 从SharedPreferences加载cookies
        loadCookies()
    }

    private fun loadCookies() {
        val cookieJson = prefs.getString("cookies", null) ?: return
        try {
            val jsonObj = JSONObject(cookieJson)
            for (host in jsonObj.keys()) {
                val cookieArray = jsonObj.getJSONArray(host)
                val cookies = mutableListOf<Cookie>()
                for (i in 0 until cookieArray.length()) {
                    val cookieStr = cookieArray.getString(i)
                    val cookie = Cookie.parse(host.toHttpUrl(), cookieStr)
                    if (cookie != null) cookies.add(cookie)
                }
                store[host] = cookies
            }
        } catch (e: Exception) {
            android.util.Log.e("PersistentCookieJar", "加载cookies失败: ${e.message}")
        }
    }

    private fun saveCookies() {
        try {
            val jsonObj = JSONObject()
            for ((host, cookies) in store) {
                val cookieArray = org.json.JSONArray()
                for (cookie in cookies) {
                    cookieArray.put(cookie.toString())
                }
                jsonObj.put(host, cookieArray)
            }
            prefs.edit().putString("cookies", jsonObj.toString()).apply()
        } catch (e: Exception) {
            android.util.Log.e("PersistentCookieJar", "保存cookies失败: ${e.message}")
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val key = url.host
        store.getOrPut(key) { mutableListOf() }.addAll(cookies)
        saveCookies()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        // 返回所有域名的Cookie（学习通需要跨域Cookie）
        val allCookies = mutableListOf<Cookie>()
        store.values.forEach { allCookies.addAll(it) }
        return allCookies
    }
}
