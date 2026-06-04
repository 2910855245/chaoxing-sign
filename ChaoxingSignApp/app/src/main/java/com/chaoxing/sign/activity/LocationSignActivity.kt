package com.chaoxing.sign.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CircleOptions
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.chaoxing.sign.R
import com.chaoxing.sign.api.ChaoxingApi
import com.chaoxing.sign.api.ChaoxingSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationSignActivity : AppCompatActivity() {
    private lateinit var session: ChaoxingSession
    private lateinit var mapView: MapView
    private lateinit var aMap: AMap
    private lateinit var tvInfo: TextView

    private var selectedLat = 30.5728  // 默认成都
    private var selectedLon = 104.0668
    private var selectedAddress = "点击地图选点"
    private var activeId: Long = 0
    private var courseId: String = ""
    private var classId: String = ""

    // 目标位置（老师设置的坐标）
    private var targetLat = 0.0
    private var targetLon = 0.0
    private var targetRange = 100  // 范围（米）
    private var targetName = ""

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_sign)

        // 设置高德地图隐私政策
        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)

        session = ChaoxingSession(this)
        session.autoLogin()

        activeId = intent.getLongExtra("activeId", 0)
        courseId = intent.getStringExtra("courseId") ?: ""
        classId = intent.getStringExtra("classId") ?: ""

        // 获取目标位置信息
        targetLat = intent.getDoubleExtra("targetLat", 0.0)
        targetLon = intent.getDoubleExtra("targetLon", 0.0)
        targetRange = intent.getIntExtra("targetRange", 100)
        targetName = intent.getStringExtra("targetName") ?: ""

        mapView = findViewById(R.id.map_view)
        tvInfo = findViewById(R.id.tv_info)
        val btnBack = findViewById<TextView>(R.id.tv_back)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm)

        btnBack.setOnClickListener { finish() }
        btnConfirm.setOnClickListener { doSign() }

        // 一键回到真实定位
        val btnMyLocation = findViewById<Button>(R.id.btn_my_location)
        btnMyLocation.setOnClickListener {
            tryGetCurrentLocation()
        }

        // 检查并请求权限
        if (checkPermissions()) {
            initMap()
        } else {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMap()
            } else {
                Toast.makeText(this, "需要位置权限才能使用位置签到", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun initMap() {
        mapView.onCreate(null)
        aMap = mapView.map
        aMap.uiSettings.isZoomControlsEnabled = true
        aMap.uiSettings.isScrollGesturesEnabled = true

        // 如果有目标位置，显示目标位置和范围
        if (targetLat != 0.0 && targetLon != 0.0) {
            showTargetLocation()
        }

        aMap.setOnMapClickListener { latLng ->
            selectedLat = latLng.latitude
            selectedLon = latLng.longitude
            aMap.clear()
            // 重新显示目标位置和范围
            if (targetLat != 0.0 && targetLon != 0.0) {
                showTargetLocation()
            }
            aMap.addMarker(MarkerOptions().position(latLng).title("选中位置").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            selectedAddress = "${latLng.latitude}, ${latLng.longitude}"
            updateInfo()
        }

        // 尝试获取当前位置
        tryGetCurrentLocation()
        updateInfo()
    }

    private fun showTargetLocation() {
        val targetLatLng = LatLng(targetLat, targetLon)

        // 添加目标位置标记
        aMap.addMarker(MarkerOptions()
            .position(targetLatLng)
            .title(targetName)
            .snippet("签到范围: ${targetRange}米")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

        // 添加范围圆圈
        aMap.addCircle(CircleOptions()
            .center(targetLatLng)
            .radius(targetRange.toDouble())
            .strokeColor(Color.RED)
            .strokeWidth(2f)
            .fillColor(Color.argb(50, 255, 0, 0)))

        // 移动到目标位置
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng, 15f))
    }

    private fun tryGetCurrentLocation() {
        try {
            val locationClient = AMapLocationClient(this)
            val option = AMapLocationClientOption()
            option.isOnceLocation = true
            locationClient.setLocationOption(option)
            locationClient.setLocationListener { location ->
                if (location != null && location.errorCode == 0) {
                    selectedLat = location.latitude
                    selectedLon = location.longitude
                    selectedAddress = location.address ?: "${selectedLat}, ${selectedLon}"
                    // 添加当前位置标记
                    aMap.addMarker(MarkerOptions()
                        .position(LatLng(selectedLat, selectedLon))
                        .title("当前位置")
                        .snippet(selectedAddress)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                    updateInfo()
                }
                locationClient.stopLocation()
            }
            locationClient.startLocation()
        } catch (e: Exception) {
            // 定位失败，使用默认位置
        }
    }

    private fun updateInfo() {
        val distance = if (targetLat != 0.0 && targetLon != 0.0) {
            calculateDistance(selectedLat, selectedLon, targetLat, targetLon)
        } else {
            0.0
        }

        val inRange = distance <= targetRange

        tvInfo.text = buildString {
            append("📍 当前位置\n")
            append("纬度: $selectedLat\n")
            append("经度: $selectedLon\n")
            append("地址: $selectedAddress\n")
            if (targetLat != 0.0 && targetLon != 0.0) {
                append("\n🎯 签到目标\n")
                append("位置: $targetName\n")
                append("范围: ${targetRange}米\n")
                append("距离: ${String.format("%.1f", distance)}米\n")
                append(if (inRange) "\n✅ 在签到范围内" else "\n❌ 不在签到范围内")
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // 地球半径（米）
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    private fun doSign() {
        // 检查是否在范围内
        if (targetLat != 0.0 && targetLon != 0.0) {
            val distance = calculateDistance(selectedLat, selectedLon, targetLat, targetLon)
            if (distance > targetRange) {
                Toast.makeText(this, "不在签到范围内（距离${String.format("%.1f", distance)}米）", Toast.LENGTH_LONG).show()
                return
            }
        }

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    ChaoxingApi.preSign(session, activeId, courseId, classId)
                    ChaoxingApi.signLocation(session, activeId, selectedLat, selectedLon, selectedAddress)
                }
                val success = result.contains("success") || result.contains("已签到")
                Toast.makeText(
                    this@LocationSignActivity,
                    if (success) "签到成功!" else "签到失败: $result",
                    Toast.LENGTH_SHORT
                ).show()
                if (success) finish()
            } catch (e: Exception) {
                Toast.makeText(this@LocationSignActivity, "错误: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
