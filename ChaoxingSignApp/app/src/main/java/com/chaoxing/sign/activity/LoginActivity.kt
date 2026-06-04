package com.chaoxing.sign.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chaoxing.sign.R
import com.chaoxing.sign.api.ChaoxingSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入学号和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            btnLogin.text = "登录中..."

            lifecycleScope.launch {
                try {
                    val session = ChaoxingSession(this@LoginActivity)
                    val ok = withContext(Dispatchers.IO) {
                        session.login(username, password)
                    }

                    if (ok) {
                        session.saveSession(username, password)
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "登录失败", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "错误: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    btnLogin.isEnabled = true
                    btnLogin.text = "登录"
                }
            }
        }
    }
}
