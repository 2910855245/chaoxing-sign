package com.chaoxing.sign

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chaoxing.sign.activity.HomeActivity
import com.chaoxing.sign.activity.LoginActivity
import com.chaoxing.sign.api.ChaoxingSession

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = ChaoxingSession(this)
        if (session.loadSavedSession()) {
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
