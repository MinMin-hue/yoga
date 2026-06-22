package com.yoga.modules.auth.controller;

import com.yoga.common.R;
import com.yoga.modules.auth.dto.LoginDTO;
import com.yoga.modules.auth.dto.MemberLoginDTO;
import com.yoga.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 管理员/教练登录 */
    @PostMapping("/auth/admin/login")
    public R<Map<String, Object>> adminLogin(@RequestBody @Valid LoginDTO dto) {
        return authService.adminLogin(dto);
    }

    /** 会员登录(H5/小程序) */
    @PostMapping("/auth/member/login")
    public R<Map<String, Object>> memberLogin(@RequestBody @Valid MemberLoginDTO dto) {
        return authService.memberLogin(dto);
    }
}
