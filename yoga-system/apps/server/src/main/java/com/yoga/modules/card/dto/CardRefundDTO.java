package com.yoga.modules.card.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardRefundDTO {

    @NotNull
    private Long cardId;

    private String reason;
}
