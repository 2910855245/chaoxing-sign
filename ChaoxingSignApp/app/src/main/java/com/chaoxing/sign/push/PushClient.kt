package com.chaoxing.sign.push

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * 签到推送客户端 - Android版
 *
 * 用法:
 *   val client = PushClient("ws://your-server:8765", "student_123")
 *   client.onSign = { data -> handleSign(data) }
 *   client.connect()
 */
class PushClient(
    private val serverUrl: String,
    private val clientId: String
) {
    private var webSocket: WebSocket? = null
    private var client: OkHttpClient? = null
    private var running = false

    var onSign: ((JSONObject) -> Unit)? = null
    var onConnect: (() -> Unit)? = null
    var onDisconnect: (() -> Unit)? = null

    private val wsUrl: String
        get() = "$serverUrl/ws/$clientId"

    fun connect() {
        running = true
        client = OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .build()

        doConnect()
    }

    private fun doConnect() {
        if (!running) return

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocket = client?.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "已连接: $wsUrl")
                onConnect?.invoke()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    when (json.optString("type")) {
                        "pong" -> { /* 忽略心跳 */ }
                        "sign" -> {
                            Log.d(TAG, "收到签到: $json")
                            onSign?.invoke(json)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "解析消息失败: $e")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "连接失败: ${t.message}")
                // 重连
                if (running) {
                    Thread.sleep(5000)
                    doConnect()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "连接关闭: $code $reason")
                onDisconnect?.invoke()
                // 重连
                if (running) {
                    Thread.sleep(5000)
                    doConnect()
                }
            }
        })
    }

    fun disconnect() {
        running = false
        webSocket?.close(1000, "bye")
        client?.dispatcher?.executorService?.shutdown()
    }

    fun send(data: JSONObject) {
        webSocket?.send(data.toString())
    }

    companion object {
        private const val TAG = "PushClient"
    }
}
