package com.yoga.modules.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingCreateDTO {

    @NotNull
    private Long scheduleId;

    @NotNull
    private Long cardId;
}
