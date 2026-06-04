package com.chaoxing.sign

import android.app.Application
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.MapsInitializer

class ChaoxingApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 初始化高德地图隐私合规
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)

        // 初始化高德定位隐私合规
        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)
    }
}
