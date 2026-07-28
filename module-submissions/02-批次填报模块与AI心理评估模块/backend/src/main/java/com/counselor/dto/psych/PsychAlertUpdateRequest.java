package com.counselor.dto.psych;

import com.counselor.enums.PsychAlertStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PsychAlertUpdateRequest {
    @NotNull private PsychAlertStatus status;
    private String followUpNote;
    private LocalDateTime contactedAt;
}
