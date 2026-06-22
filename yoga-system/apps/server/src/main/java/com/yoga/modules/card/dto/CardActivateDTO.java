package com.yoga.modules.card.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardActivateDTO {

    @NotNull(message = "卡 id 必填")
    private Long cardId;
}
