package com.yoga.modules.card.controller;

import com.yoga.common.R;
import com.yoga.modules.card.dto.CardActivateDTO;
import com.yoga.modules.card.dto.CardRefundDTO;
import com.yoga.modules.card.dto.CardTypeUpsertDTO;
import com.yoga.modules.card.service.CardTypeService;
import com.yoga.modules.card.service.MemberCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardTypeService cardTypeService;
    private final MemberCardService memberCardService;

    // ==================== 卡类型(后台) ====================

    @GetMapping("/admin/card-type/list")
    public R<List<?>> listAll(@RequestParam(required = false) Integer status) {
        return R.ok(cardTypeService.listAll(status).getData());
    }

    @PostMapping("/admin/card-type/page")
    public R<Map<String, Object>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                       @RequestParam(defaultValue = "20") Long pageSize) {
        return cardTypeService.page(pageNum, pageSize);
    }

    @PostMapping("/admin/card-type/create")
    public R<?> create(@RequestBody @Valid CardTypeUpsertDTO dto) {
        return cardTypeService.create(dto);
    }

    @PostMapping("/admin/card-type/update")
    public R<?> update(@RequestBody @Valid CardTypeUpsertDTO dto) {
        return cardTypeService.update(dto);
    }

    @PostMapping("/admin/card-type/delete/{id}")
    public R<?> deleteType(@PathVariable Long id) {
        return cardTypeService.delete(id);
    }

    // ==================== 会员卡(后台) ====================

    @GetMapping("/admin/member-card/{id}")
    public R<?> cardDetail(@PathVariable Long id) {
        return memberCardService.detail(id);
    }

    @GetMapping("/admin/member-card/list-by-member/{memberId}")
    public R<?> listByMember(@PathVariable Long memberId) {
        return memberCardService.listByMember(memberId);
    }

    @PostMapping("/admin/member-card/activate")
    public R<?> activate(@RequestBody @Valid CardActivateDTO dto) {
        return memberCardService.activate(dto);
    }

    @PostMapping("/admin/member-card/refund")
    public R<?> refund(@RequestBody @Valid CardRefundDTO dto) {
        return memberCardService.refund(dto);
    }

    // ==================== H5 会员端 ====================

    @GetMapping("/h5/card-type/list")
    public R<List<?>> h5ListAll() {
        return R.ok(cardTypeService.listAll(1).getData());
    }
}
