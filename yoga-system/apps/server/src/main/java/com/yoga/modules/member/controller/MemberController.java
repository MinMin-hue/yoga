package com.yoga.modules.member.controller;

import com.yoga.common.R;
import com.yoga.modules.member.dto.MemberQuery;
import com.yoga.modules.member.dto.MemberUpsertDTO;
import com.yoga.modules.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /** 管理后台: 会员列表 */
    @PostMapping("/admin/member/page")
    public R<Map<String, Object>> page(@RequestBody MemberQuery query) {
        return memberService.page(query);
    }

    @GetMapping("/admin/member/{id}")
    public R<?> detail(@PathVariable Long id) {
        return memberService.detail(id);
    }

    @PostMapping("/admin/member/create")
    public R<?> create(@RequestBody @Valid MemberUpsertDTO dto) {
        return memberService.create(dto);
    }

    @PostMapping("/admin/member/update")
    public R<?> update(@RequestBody @Valid MemberUpsertDTO dto) {
        return memberService.update(dto);
    }

    @PostMapping("/admin/member/delete/{id}")
    public R<?> delete(@PathVariable Long id) {
        return memberService.delete(id);
    }

    /** H5: 会员个人中心 */
    @GetMapping("/h5/member/profile")
    public R<?> myProfile() {
        return memberService.myProfile();
    }

    @GetMapping("/h5/member/cards")
    public R<?> myCards() {
        return memberService.myCards();
    }

    @GetMapping("/h5/member/records")
    public R<?> myRecords() {
        return memberService.myRecords();
    }
}
