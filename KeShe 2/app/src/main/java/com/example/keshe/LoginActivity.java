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

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvGoRegister;
    private UserViewModel userViewModel;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        
        userViewModel = new UserViewModel(getApplication());
        initViews();
        setupListeners();
        
        // 不再自动跳转，让用户能看到登录界面
        // 如果需要自动登录，可以在MainActivity中处理
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_login_username);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login_submit);
        tvGoRegister = findViewById(R.id.tv_go_register);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> login());
        
        tvGoRegister.setOnClickListener(v -> {
            // 跳转到注册页面
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void login() {
        String identifier = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (identifier.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请填写用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 判断输入的是手机号、邮箱还是用户名
        String emailToTry = identifier;
        
        // 如果输入的是11位数字，可能是手机号，转换为邮箱格式
        if (identifier.matches("^\\d{11}$")) {
            emailToTry = identifier + "@phone.com";
        }
        
        // 先尝试作为邮箱登录（包括手机号转换后的邮箱）
        userViewModel.login(emailToTry, password, new UserViewModel.LoginCallback() {
            @Override
            public void onResult(User user) {
                if (user != null) {
                    // 登录成功
                    saveUserInfo(user);
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    startMainActivity(user.getId());
                } else {
                    // 如果邮箱登录失败，尝试通过用户名查找用户并验证密码
                    userViewModel.getUserByUsername(identifier, new UserViewModel.GetUserCallback() {
                        @Override
                        public void onResult(User user) {
                            if (user != null && user.getPassword().equals(password)) {
                                // 用户名和密码匹配，登录成功
                                saveUserInfo(user);
                                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                startMainActivity(user.getId());
                            } else {
                                Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
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