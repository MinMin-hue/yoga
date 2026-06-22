package com.yoga.modules.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yoga.common.BizException;
import com.yoga.common.R;
import com.yoga.modules.auth.dto.LoginDTO;
import com.yoga.modules.auth.dto.MemberLoginDTO;
import com.yoga.modules.auth.entity.AdminUser;
import com.yoga.modules.auth.mapper.AdminUserMapper;
import com.yoga.modules.member.entity.Member;
import com.yoga.modules.member.mapper.MemberMapper;
import com.yoga.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserMapper adminUserMapper;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public R<Map<String, Object>> adminLogin(LoginDTO dto) {
        AdminUser user = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, dto.getUsername()));
        if (user == null) {
            throw BizException.badRequest("账号或密码错误");
        }
        if (user.getStatus() == 0) {
            throw BizException.forbidden("账号已停用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw BizException.badRequest("账号或密码错误");
        }
        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
        return R.ok(buildAdminInfo(user, token));
    }

    /**
     * 会员登录: 简化版 - 手机号直接登录, 无验证码(小瑜伽馆内部使用)
     * 后续可扩展为: 发送短信验证码 -> 验证 -> 登录
     */
    public R<Map<String, Object>> memberLogin(MemberLoginDTO dto) {
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getPhone, dto.getPhone()));
        if (member == null) {
            // 自动注册
            member = new Member();
            member.setPhone(dto.getPhone());
            member.setNickname("会员" + dto.getPhone().substring(7));
            member.setStatus(1);
            memberMapper.insert(member);
        }
        if (member.getStatus() == 0) {
            throw BizException.forbidden("账号已停用");
        }
        String token = jwtUtil.generate(member.getId(), member.getNickname(), "MEMBER");
        return R.ok(buildMemberInfo(member, token));
    }

    private Map<String, Object> buildAdminInfo(AdminUser user, String token) {
        Map<String, Object> info = new HashMap<>();
        info.put("token", token);
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("realName", user.getRealName());
        profile.put("role", user.getRole());
        profile.put("avatar", user.getAvatar());
        info.put("profile", profile);
        return info;
    }

    private Map<String, Object> buildMemberInfo(Member member, String token) {
        Map<String, Object> info = new HashMap<>();
        info.put("token", token);
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", member.getId());
        profile.put("phone", member.getPhone());
        profile.put("nickname", member.getNickname());
        profile.put("avatar", member.getAvatar());
        info.put("profile", profile);
        return info;
    }
}
