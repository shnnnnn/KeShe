package com.example.keshe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.keshe.entity.User;
import com.example.keshe.viewmodel.UserViewModel;

public class RegisterActivity extends AppCompatActivity {
    private EditText etPhone, etPassword, etUsername;
    private Button btnRegister;
    private TextView tvToLogin;
    private UserViewModel userViewModel;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        
        userViewModel = new UserViewModel(getApplication());
        initViews();
        setupListeners();
    }

    private void initViews() {
        etPhone = findViewById(R.id.et_register_phone);
        etPassword = findViewById(R.id.et_register_password);
        etUsername = findViewById(R.id.et_register_username);
        btnRegister = findViewById(R.id.btn_register_submit);
        tvToLogin = findViewById(R.id.tv_to_login);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> register());
        
        tvToLogin.setOnClickListener(v -> {
            // 跳转到登录页面
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void register() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        if (phone.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "请填写所有信息", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.length() != 11) {
            Toast.makeText(this, "手机号必须是11位", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8 || password.length() > 20) {
            Toast.makeText(this, "密码长度必须在8-20位之间", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查密码是否包含大小写字母和数字
        if (!password.matches(".*[a-z].*") || !password.matches(".*[A-Z].*") || !password.matches(".*[0-9].*")) {
            Toast.makeText(this, "密码必须包含大小写字母和数字", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 2 || username.length() > 20) {
            Toast.makeText(this, "用户名长度必须在2-20位之间", Toast.LENGTH_SHORT).show();
            return;
        }

        // 使用手机号作为邮箱（因为数据库要求邮箱唯一）
        String email = phone + "@phone.com";
        
        // 检查邮箱（手机号）是否已存在
        userViewModel.getUserByEmail(email, new UserViewModel.GetUserCallback() {
            @Override
            public void onResult(User existingUser) {
                if (existingUser != null) {
                    Toast.makeText(RegisterActivity.this, "该手机号已注册", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 创建新用户
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setPassword(password);
                newUser.setUsername(username);

                // 插入用户并等待完成
                userViewModel.insert(newUser, new UserViewModel.InsertCallback() {
                    @Override
                    public void onResult(User insertedUser) {
                        // 自动登录
                        saveUserInfo(insertedUser);
                        Toast.makeText(RegisterActivity.this, "注册成功，已自动登录", Toast.LENGTH_SHORT).show();
                        startMainActivity(insertedUser.getId());
                    }
                });
            }
        });
    }

    private void saveUserInfo(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("user_id", user.getId());
        editor.putString("user_email", user.getEmail());
        editor.putString("user_name", user.getUsername());
        editor.putBoolean("is_logged_in", true);
        editor.apply();

        userViewModel.setCurrentUser(user);
    }

    private void startMainActivity(int userId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
        finish();
    }
}

