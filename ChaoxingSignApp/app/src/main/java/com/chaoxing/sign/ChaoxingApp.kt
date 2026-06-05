package com.chaoxing.sign

import android.app.Application
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.MapsInitializer
import com.chaoxing.sign.api.ChaoxingSession

class ChaoxingApp : Application() {
    // 全局共享 session，避免每个 Activity 创建新实例导致 cookies 丢失
    var session: ChaoxingSession? = null

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化高德地图隐私合规
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)

        // 初始化高德定位隐私合规
        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)
    }

    companion object {
        lateinit var instance: ChaoxingApp
            private set
    }
}
