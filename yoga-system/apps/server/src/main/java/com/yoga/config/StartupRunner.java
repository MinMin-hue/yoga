package com.yoga.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yoga.modules.auth.entity.AdminUser;
import com.yoga.modules.auth.mapper.AdminUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时: 重置 admin/coach 默认密码, 确保 BCrypt 正确
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupRunner implements CommandLineRunner {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        resetPassword("admin", "admin123");
        resetPassword("coach_wang", "coach123");
        resetPassword("coach_li", "coach123");
    }

    private void resetPassword(String username, String newPwd) {
        AdminUser u = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, username));
        if (u == null) return;
        u.setPassword(passwordEncoder.encode(newPwd));
        adminUserMapper.updateById(u);
        log.info("已重置账号 [{}] 的密码", username);
    }
}
