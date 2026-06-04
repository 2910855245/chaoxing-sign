package com.chaoxing.sign.util

import com.chaoxing.sign.api.ChaoxingApi
import com.chaoxing.sign.api.ChaoxingSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

data class BruteForceResult(
    val found: Boolean,
    val code: String = "",
    val attempted: Int = 0,
    val total: Int = 0,
    val message: String = ""
)

object BruteForce {

    suspend fun crackGesture(
        session: ChaoxingSession,
        activeId: Long,
        numberCount: Int,
        onProgress: (attempted: Int, total: Int) -> Unit = { _, _ -> }
    ): BruteForceResult = withContext(Dispatchers.IO) {
        val dict = GestureDict.getDict(numberCount)

        // Phase 1: 字典
        for ((i, code) in dict.withIndex()) {
            coroutineContext.ensureActive()
            if (ChaoxingApi.checkSignCode(session, activeId, code)) {
                return@withContext BruteForceResult(true, code, i + 1, dict.size, "字典命中")
            }
            onProgress(i + 1, dict.size)
            Thread.sleep(30)
        }

        // Phase 2: 全排列
        val points = "123456789"
        val total = permCount(9, numberCount)
        val tried = dict.toHashSet()
        var count = 0
        var foundCode: String? = null

        permute(points.toList(), numberCount) { perm ->
            if (foundCode != null) return@permute
            coroutineContext.ensureActive()
            val code = perm.joinToString("")
            if (code !in tried) {
                tried.add(code)
                count++
                if (ChaoxingApi.checkSignCode(session, activeId, code)) {
                    foundCode = code
                }
                if (count % 50 == 0) {
                    onProgress(dict.size + count, dict.size + total)
                    Thread.sleep(10)
                }
            }
        }

        if (foundCode != null) {
            BruteForceResult(true, foundCode!!, dict.size + count, total, "全排列命中")
        } else {
            BruteForceResult(false, "", dict.size + total, dict.size + total, "暴力破解失败")
        }
    }

    suspend fun crackCode(
        session: ChaoxingSession,
        activeId: Long,
        onProgress: (attempted: Int, total: Int) -> Unit = { _, _ -> }
    ): BruteForceResult = withContext(Dispatchers.IO) {
        // 依次尝试 4位 -> 5位 -> 6位
        for (digits in listOf(4, 5, 6)) {
            coroutineContext.ensureActive()
            val total = Math.pow(10.0, digits.toDouble()).toInt()

            for (i in 0 until total) {
                coroutineContext.ensureActive()
                val code = i.toString().padStart(digits, '0')
                if (ChaoxingApi.checkSignCode(session, activeId, code)) {
                    return@withContext BruteForceResult(true, code, i + 1, total, "${digits}位命中")
                }
                if (i % 100 == 0) {
                    onProgress(i, total)
                    Thread.sleep(10)
                }
            }
        }

        BruteForceResult(false, message = "暴力破解失败")
    }

    private fun permCount(n: Int, r: Int): Int {
        var result = 1
        for (i in 0 until r) result *= (n - i)
        return result
    }

    private fun <T> permute(list: List<T>, r: Int, block: (List<T>) -> Unit) {
        val used = BooleanArray(list.size)
        val current = mutableListOf<T>()
        fun dfs() {
            if (current.size == r) {
                block(current.toList())
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
    }
}
